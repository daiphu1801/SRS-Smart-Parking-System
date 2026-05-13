"""
vision_service.py — Smart two-stage LPR pipeline
Stage 1 (every frame): YOLOv26n detects vehicles + measures motion via bbox displacement
Stage 2 (only when STATIONARY): yolo11n detects license plate → EasyOCR reads text
"""
import base64
import threading
import time
import queue
import cv2
import json
import os
import numpy as np
from collections import deque
from ultralytics import YOLO
import easyocr
import torch


# ──────────────────────────────────────────────
# Tuneable constants
# ──────────────────────────────────────────────
INFERENCE_FPS       = 10        # Stage-1 inference rate (vehicles)
STREAM_FPS          = 25        # Max UI frame push rate
JPEG_QUALITY        = 70        # Encode quality 0-100
QUEUE_MAXSIZE       = 2         # Drop old frames to keep latency low

# Motion / stationary thresholds
STILL_FRAMES_NEEDED = 4         # How many consecutive "not moved" frames = stationary
MOVE_PX_THRESHOLD   = 12        # Pixel displacement of bbox center to count as "moved"
IOU_THRESHOLD       = 0.75      # Alternative: IoU between consecutive boxes

PLATE_DEBOUNCE      = 3.0       # Seconds before re-reporting same plate


def _box_center(x1, y1, x2, y2):
    return ((x1 + x2) // 2, (y1 + y2) // 2)


def _iou(a, b):
    """Compute IoU between two boxes (x1,y1,x2,y2)."""
    ax1, ay1, ax2, ay2 = a
    bx1, by1, bx2, by2 = b
    ix1 = max(ax1, bx1); iy1 = max(ay1, by1)
    ix2 = min(ax2, bx2); iy2 = min(ay2, by2)
    inter = max(0, ix2 - ix1) * max(0, iy2 - iy1)
    area_a = (ax2 - ax1) * (ay2 - ay1)
    area_b = (bx2 - bx1) * (by2 - by1)
    union = area_a + area_b - inter
    return inter / union if union > 0 else 0.0


class VehicleTracker:
    """
    Lightweight per-vehicle motion tracker.
    Keeps a short history of bounding boxes for each detected vehicle
    and exposes `is_stationary` when the box hasn't moved for STILL_FRAMES_NEEDED frames.
    """
    def __init__(self):
        # key: track_id (or bbox hash), value: deque of (cx, cy)
        self._history: dict[str, deque] = {}

    def update(self, box_id: str, x1, y1, x2, y2) -> bool:
        """
        Update history for box_id.
        Returns True if the vehicle is considered stationary.
        """
        cx, cy = _box_center(x1, y1, x2, y2)
        if box_id not in self._history:
            self._history[box_id] = deque(maxlen=STILL_FRAMES_NEEDED)
        self._history[box_id].append((cx, cy))

        hist = self._history[box_id]
        if len(hist) < STILL_FRAMES_NEEDED:
            return False  # Not enough history yet

        # All consecutive displacements must be below threshold
        for i in range(1, len(hist)):
            dx = hist[i][0] - hist[i - 1][0]
            dy = hist[i][1] - hist[i - 1][1]
            if (dx * dx + dy * dy) ** 0.5 > MOVE_PX_THRESHOLD:
                return False
        return True

    def remove_stale(self, active_ids: set):
        stale = [k for k in self._history if k not in active_ids]
        for k in stale:
            del self._history[k]


class VisionEngine:
    def __init__(self):
        self._lpr_running      = False
        self._parking_running  = False

        self.gpu_device = "cuda" if torch.cuda.is_available() else "cpu"

        base_dir        = os.path.dirname(__file__)
        self.v_path     = os.path.join(base_dir, "models", "yolo26n.pt")   # vehicle detector (Stage-1 + parking)
        self.p_path     = os.path.join(base_dir, "models", "yolo11n.pt")   # plate detector  (Stage-2)
        self.json_file  = os.path.join(base_dir, "data.json")

        self.vehicle_model  = None   # yolo26n — Stage-1
        self.plate_model    = None   # yolo11n — Stage-2
        self.ocr_reader     = None
        self.parking_model  = None   # yolo26n reused for parking

        # Callbacks
        self.on_lpr_frame = None
        self.on_lpr_plate = None
        self.on_parking_frame = None
        self.on_parking_update = None

    # ──────────────────────────────────────────
    # LPR Stream (Gate Control)
    # ──────────────────────────────────────────
    def start_lpr_stream(self, video_source=0, on_frame=None, on_plate_detected=None):
        self.on_lpr_frame = on_frame
        self.on_lpr_plate = on_plate_detected
        if self._lpr_running:
            return

        if not self.vehicle_model:
            print("[VisionEngine] Loading vehicle model (yolo26n)…")
            self.vehicle_model = YOLO(self.v_path)
            print("[VisionEngine] Loading plate model (yolo11n)…")
            self.plate_model = YOLO(self.p_path)
            print("[VisionEngine] Loading EasyOCR…")
            self.ocr_reader = easyocr.Reader(["en"], gpu=torch.cuda.is_available())
            print("[VisionEngine] LPR models ready.")

        self._lpr_running = True
        frame_q = queue.Queue(maxsize=QUEUE_MAXSIZE)

        threading.Thread(target=self._reader_loop,    args=(video_source, frame_q, "_lpr_running"),    daemon=True).start()
        threading.Thread(target=self._lpr_infer_loop, args=(frame_q,), daemon=True).start()

    def stop_lpr_stream(self):
        self._lpr_running = False

    # ──────────────────────────────────────────
    # Parking Stream (Zones & Devices)
    # ──────────────────────────────────────────
    def start_parking_stream(self, video_source=0, on_frame=None, on_parking_update=None):
        self.on_parking_frame = on_frame
        self.on_parking_update = on_parking_update
        if self._parking_running:
            return

        if not self.parking_model:
            print("[VisionEngine] Loading parking model (yolo26n)…")
            self.parking_model = YOLO(self.v_path)
            print("[VisionEngine] Parking model ready.")

        self._parking_running = True
        frame_q = queue.Queue(maxsize=QUEUE_MAXSIZE)

        threading.Thread(target=self._reader_loop,        args=(video_source, frame_q, "_parking_running"), daemon=True).start()
        threading.Thread(target=self._parking_infer_loop, args=(frame_q,), daemon=True).start()

    def stop_parking_stream(self):
        self._parking_running = False

    # ──────────────────────────────────────────
    # Shared reader thread
    # ──────────────────────────────────────────
    def _reader_loop(self, video_source, frame_q: queue.Queue, running_flag: str):
        cap = cv2.VideoCapture(video_source)
        if not cap.isOpened():
            print(f"[VisionEngine] Cannot open source: {video_source}")
            return

        while getattr(self, running_flag) and cap.isOpened():
            ret, frame = cap.read()
            if not ret:
                if isinstance(video_source, str):
                    cap.set(cv2.CAP_PROP_POS_FRAMES, 0)
                    continue
                break

            if frame_q.full():
                try:
                    frame_q.get_nowait()
                except queue.Empty:
                    pass
            try:
                frame_q.put_nowait(frame)
            except queue.Full:
                pass

            time.sleep(1 / 60)

        cap.release()

    # ──────────────────────────────────────────
    # LPR inference — smart two-stage pipeline
    # ──────────────────────────────────────────
    def _lpr_infer_loop(self, frame_q: queue.Queue):
        infer_interval  = 1.0 / INFERENCE_FPS
        stream_interval = 1.0 / STREAM_FPS
        last_stream_t   = 0
        last_plate      = ""
        last_plate_t    = 0

        # Stage-1 options (vehicle detection with yolo26n)
        v_opts = {"verbose": False, "conf": 0.40, "device": self.gpu_device}
        # Stage-2 options (plate detection with yolo11n)
        p_opts = {"verbose": False, "conf": 0.35, "device": self.gpu_device}
        encode_params = [cv2.IMWRITE_JPEG_QUALITY, JPEG_QUALITY]

        tracker = VehicleTracker()

        while self._lpr_running:
            try:
                frame = frame_q.get(timeout=0.5)
            except queue.Empty:
                continue

            t0        = time.time()
            out_frame = frame.copy()

            # ── Stage 1: Detect vehicles with yolo26n ──
            v_results = self.vehicle_model(out_frame, **v_opts)
            active_ids = set()
            plate_text = None

            if v_results and v_results[0].boxes is not None and len(v_results[0].boxes) > 0:
                for idx, v_box in enumerate(v_results[0].boxes):
                    vX1, vY1, vX2, vY2 = map(int, v_box.xyxy[0])
                    box_id = f"v{idx}"
                    active_ids.add(box_id)

                    is_still = tracker.update(box_id, vX1, vY1, vX2, vY2)

                    # Color: blue = moving, yellow = stationary
                    box_color = (0, 220, 0) if is_still else (100, 180, 255)
                    label     = "STOPPED" if is_still else "MOVING"
                    cv2.rectangle(out_frame, (vX1, vY1), (vX2, vY2), box_color, 2)
                    cv2.putText(out_frame, label, (vX1, vY1 - 6),
                                cv2.FONT_HERSHEY_SIMPLEX, 0.45, box_color, 1)

                    # ── Stage 2: Only run plate model when STOPPED ──
                    if is_still:
                        v_crop = frame[vY1:vY2, vX1:vX2]
                        if v_crop.size == 0:
                            continue

                        p_results = self.plate_model(v_crop, **p_opts)
                        if p_results and p_results[0].boxes is not None and len(p_results[0].boxes) > 0:
                            for p_box in p_results[0].boxes:
                                pX1, pY1, pX2, pY2 = map(int, p_box.xyxy[0])
                                gX1 = vX1 + pX1; gY1 = vY1 + pY1
                                gX2 = vX1 + pX2; gY2 = vY1 + pY2
                                cv2.rectangle(out_frame, (gX1, gY1), (gX2, gY2), (0, 255, 80), 2)

                                # OCR
                                p_img = v_crop[pY1:pY2, pX1:pX2]
                                if p_img.size > 0:
                                    p_gray  = cv2.cvtColor(p_img, cv2.COLOR_BGR2GRAY)
                                    p_large = cv2.resize(p_gray, None, fx=2.0, fy=2.0,
                                                         interpolation=cv2.INTER_CUBIC)
                                    ocr_out = self.ocr_reader.readtext(p_large, detail=0)
                                    if ocr_out:
                                        plate_text = ocr_out[0].upper()
                                        cv2.putText(out_frame, plate_text,
                                                    (gX1, gY2 + 18),
                                                    cv2.FONT_HERSHEY_SIMPLEX, 0.6,
                                                    (0, 255, 80), 2)

            # Cleanup stale track ids
            tracker.remove_stale(active_ids)

            # ── Stream frame to UI ──
            now = time.time()
            if self.on_lpr_frame and (now - last_stream_t) >= stream_interval:
                last_stream_t = now
                _, buf = cv2.imencode(".jpg", out_frame, encode_params)
                b64 = "data:image/jpeg;base64," + base64.b64encode(buf).decode("utf-8")
                self.on_lpr_frame(b64)

            # ── Plate debounce callback ──
            if plate_text and self.on_lpr_plate:
                if plate_text != last_plate or (now - last_plate_t) > PLATE_DEBOUNCE:
                    last_plate   = plate_text
                    last_plate_t = now
                    self.on_lpr_plate(plate_text)

            # ── Throttle inference ──
            elapsed = time.time() - t0
            sleep_t = infer_interval - elapsed
            if sleep_t > 0:
                time.sleep(sleep_t)

    # ──────────────────────────────────────────
    # Parking inference loop
    # ──────────────────────────────────────────
    def _parking_infer_loop(self, frame_q: queue.Queue):
        infer_interval  = 1.0 / INFERENCE_FPS
        stream_interval = 1.0 / STREAM_FPS
        last_stream_t   = 0
        encode_params   = [cv2.IMWRITE_JPEG_QUALITY, JPEG_QUALITY]

        slots = []
        if os.path.exists(self.json_file):
            with open(self.json_file, "r") as f:
                slots = json.load(f)

        smooth_frames = 8
        slot_history  = [deque(maxlen=smooth_frames) for _ in slots]

        while self._parking_running:
            try:
                frame = frame_q.get(timeout=0.5)
            except queue.Empty:
                continue

            t0        = time.time()
            out_frame = frame.copy()

            res = self.parking_model(out_frame, verbose=False, conf=0.1,
                                     classes=[2, 3, 5, 7],
                                     device=self.gpu_device)[0]

            car_centers = []
            if res.boxes is not None and len(res.boxes) > 0:
                for box in res.boxes:
                    x1, y1, x2, y2 = map(int, box.xyxy[0])
                    cx = (x1 + x2) // 2
                    cy = (y1 + y2) // 2
                    car_centers.append((cx, cy))
                    cv2.rectangle(out_frame, (x1, y1), (x2, y2), (100, 180, 255), 1)

            occupied_count = 0
            for i, slot in enumerate(slots):
                pts    = np.array(slot["points"], np.int32)
                is_now = any(cv2.pointPolygonTest(pts, c, False) >= 0 for c in car_centers)
                slot_history[i].append(1 if is_now else 0)
                is_used = sum(slot_history[i]) > len(slot_history[i]) / 2
                if is_used:
                    occupied_count += 1
                color = (0, 0, 220) if is_used else (0, 220, 0)
                cv2.polylines(out_frame, [pts.reshape((-1, 1, 2))], True, color, 2)

            now = time.time()
            if self.on_parking_frame and (now - last_stream_t) >= stream_interval:
                last_stream_t = now
                _, buf = cv2.imencode(".jpg", out_frame, encode_params)
                b64 = "data:image/jpeg;base64," + base64.b64encode(buf).decode("utf-8")
                self.on_parking_frame(b64)

            if self.on_parking_update:
                self.on_parking_update(occupied_count, len(slots))

            elapsed = time.time() - t0
            sleep_t = infer_interval - elapsed
            if sleep_t > 0:
                time.sleep(sleep_t)


# Singleton
vision_engine = VisionEngine()
