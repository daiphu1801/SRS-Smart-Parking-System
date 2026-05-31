"""
lpr_engine.py — License Plate Recognition pipeline.
  - YOLOv11n (lpr.pt) detects plate bounding boxes
  - EasyOCR reads the text from each detected plate
  - Runs on a video file (data.mp4) looping for demo
  - Runs in background threads, pushes frames + detected plates via callbacks
"""

import base64
import os
import queue
import threading
import time
from collections import Counter
from concurrent.futures import ThreadPoolExecutor, as_completed

import cv2
import easyocr
from ultralytics import YOLO
from core.settings import get_settings, resolve_path

# ── Tunable constants ─────────────────────────────────
INFERENCE_FPS    = 4     # YOLO11 samples while the demo video plays
STREAM_FPS       = 18    # Max UI refresh rate
JPEG_QUALITY     = 65
QUEUE_MAXSIZE    = 2     # Drop old frames to stay real-time
PLATE_DEBOUNCE   = 3.0   # Seconds before re-reporting the same plate
MIN_PLATE_LEN    = 5     # Minimum OCR characters to consider a valid plate
SCAN_SECONDS     = 2.0   # Voting window after YOLO11 finds the first plate candidate
MAX_PLATE_JOBS   = 2
WHITE_BGR        = (255, 255, 255)
GREEN_950_BGR    = (22, 46, 5)

ASSETS_DIR = os.path.join(os.path.dirname(os.path.dirname(__file__)), "assets")
MODEL_PATH = os.path.join(ASSETS_DIR, "yolo11n.pt")
VIDEO_PATH = os.path.join(ASSETS_DIR, "demo.mp4")

CAR_TYPE_ID = 1


def _cfg_float(key: str, default: float, minimum: float | None = None) -> float:
    try:
        value = float(get_settings().get(key, default))
    except (TypeError, ValueError):
        value = default
    return max(minimum, value) if minimum is not None else value


def _cfg_int(key: str, default: int, minimum: int | None = None, maximum: int | None = None) -> int:
    try:
        value = int(float(get_settings().get(key, default)))
    except (TypeError, ValueError):
        value = default
    if minimum is not None:
        value = max(minimum, value)
    if maximum is not None:
        value = min(maximum, value)
    return value


def _model_path() -> str:
    return resolve_path(get_settings().get("lpr_model_path"), "assets/yolo11n.pt")


def _video_source() -> str:
    settings = get_settings()
    if str(settings.get("camera_mode") or "").upper() == "RTSP" and settings.get("rtsp_url"):
        return str(settings["rtsp_url"])
    return resolve_path(settings.get("demo_video_path"), "assets/demo.mp4")


def _is_stream_source(source: str) -> bool:
    return "://" in str(source)


def _encode_frame(frame) -> str:
    """Encode OpenCV frame → base64 JPEG data-URI."""
    encode_params = [cv2.IMWRITE_JPEG_QUALITY, _cfg_int("jpeg_quality", JPEG_QUALITY, 10, 95)]
    _, buf = cv2.imencode(".jpg", frame, encode_params)
    return "data:image/jpeg;base64," + base64.b64encode(buf).decode("utf-8")


def _clean_plate(text: str) -> str:
    """Strip non-alphanumeric chars and uppercase the result."""
    cleaned = "".join(c for c in text if c.isalnum() or c in "-. ").strip().upper()
    return cleaned


class LPREngine:
    """
    Singleton LPR engine.

    Usage:
        engine = LPREngine()
        engine.start(
            on_frame=lambda b64: ...,       # called every STREAM_FPS with b64 image
            on_plate=lambda plate, frame: ...  # called when a new plate is detected
        )
        engine.stop()
    """

    def __init__(self):
        self._running = False
        self._model: YOLO | None = None
        self._ocr: easyocr.Reader | None = None
        self._thread: threading.Thread | None = None
        self._lock = threading.Lock()
        self._model_lock = threading.Lock()
        self._scan_lock = threading.Lock()
        self._model_path: str | None = None

    def _load_models(self):
        model_path = _model_path()
        if not os.path.exists(model_path):
            print(f"[LPREngine] Model not found: {model_path}")
            return False
        if self._model is None or self._model_path != model_path:
            try:
                print(f"[LPREngine] Loading YOLO model: {model_path}")
                self._model = YOLO(model_path)
                self._model_path = model_path
                if self._ocr is None:
                    print("[LPREngine] Loading EasyOCR...")
                    import torch
                    self._ocr = easyocr.Reader(["en"], gpu=torch.cuda.is_available())
                print("[LPREngine] Models ready.")
            except Exception as exc:
                self._model = None
                self._model_path = None
                self._ocr = None
                print(f"[LPREngine] Cannot load LPR models: {exc}")
                return False
        return True

    def run_demo_scan(
        self,
        on_frame=None,
        on_vote=None,
        on_complete=None,
        on_error=None,
        on_finish=None,
        on_scan_start=None,
    ) -> bool:
        """
        Play demo.mp4 once through the guard UI. YOLO11 samples frames during playback;
        the first detected plate starts a SCAN_SECONDS voting window.
        """
        with self._scan_lock:
            if self._running:
                return False
            self._running = True

        thread = threading.Thread(
            target=self._demo_scan_loop,
            args=(on_frame, on_vote, on_complete, on_error, on_finish, on_scan_start),
            daemon=True,
            name="lpr-demo-scan",
        )
        thread.start()
        return True

    def start(self, on_frame=None, on_plate=None):
        """Start the video + inference loop in background threads."""
        with self._lock:
            if self._running:
                return
            self._running = True

        models_ready = self._load_models()
        if not models_ready:
            with self._lock:
                self._running = False
            return

        frame_q = queue.Queue(maxsize=QUEUE_MAXSIZE)

        reader_t = threading.Thread(
            target=self._reader_loop, args=(frame_q,), daemon=True, name="lpr-reader"
        )
        infer_t = threading.Thread(
            target=self._infer_loop, args=(frame_q, on_frame, on_plate), daemon=True, name="lpr-infer"
        )
        reader_t.start()
        infer_t.start()

    def stop(self):
        with self._lock:
            self._running = False

    def _detect_plate_from_frame(self, frame):
        """Run YOLO11 + OCR on one frame. Returns (plate, annotated_frame)."""
        if self._model is None or self._ocr is None:
            return None, frame

        out_frame = frame.copy()
        model_frame = frame
        offset_x = 0
        offset_y = 0

        best_plate = None
        best_conf = 0.0
        min_plate_len = _cfg_int("min_plate_length", MIN_PLATE_LEN, 1)
        infer_opts = {"verbose": False, "conf": _cfg_float("plate_confidence", 0.35, 0.0)}

        with self._model_lock:
            results = self._model(model_frame, **infer_opts)

        if not results or results[0].boxes is None:
            return None, out_frame

        for box in results[0].boxes:
            x1, y1, x2, y2 = map(int, box.xyxy[0])
            conf = float(box.conf[0])
            draw_x1, draw_y1 = x1 + offset_x, y1 + offset_y
            draw_x2, draw_y2 = x2 + offset_x, y2 + offset_y

            cv2.rectangle(out_frame, (draw_x1, draw_y1), (draw_x2, draw_y2), GREEN_950_BGR, 2)
            frame_h, frame_w = model_frame.shape[:2]
            pad_x = max(2, int((x2 - x1) * 0.08))
            pad_y = max(2, int((y2 - y1) * 0.20))
            crop_x1 = max(0, x1 - pad_x)
            crop_y1 = max(0, y1 - pad_y)
            crop_x2 = min(frame_w, x2 + pad_x)
            crop_y2 = min(frame_h, y2 + pad_y)
            plate_crop = model_frame[crop_y1:crop_y2, crop_x1:crop_x2]
            if plate_crop.size == 0:
                continue

            gray = cv2.cvtColor(plate_crop, cv2.COLOR_BGR2GRAY)
            gray = cv2.equalizeHist(gray)
            enlarged = cv2.resize(gray, None, fx=3.0, fy=3.0, interpolation=cv2.INTER_CUBIC)
            _, thresholded = cv2.threshold(enlarged, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)

            ocr_results = []
            for ocr_frame in (enlarged, thresholded):
                ocr_results = self._ocr.readtext(
                    ocr_frame,
                    detail=0,
                    allowlist="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-.",
                )
                if ocr_results:
                    break
            if not ocr_results:
                continue

            plate_text = _clean_plate(" ".join(ocr_results))
            if len(plate_text) < min_plate_len:
                continue

            cv2.putText(
                out_frame, plate_text, (draw_x1, draw_y2 + 22),
                cv2.FONT_HERSHEY_SIMPLEX, 0.75, WHITE_BGR, 2,
            )
            cv2.putText(
                out_frame, f"{conf:.0%}", (draw_x1, draw_y1 - 6),
                cv2.FONT_HERSHEY_SIMPLEX, 0.5, WHITE_BGR, 1,
            )
            if conf >= best_conf:
                best_conf = conf
                best_plate = plate_text

        return best_plate, out_frame

    def _draw_status(self, frame, text: str):
        if text:
            cv2.putText(frame, text, (12, 28), cv2.FONT_HERSHEY_SIMPLEX, 0.8, WHITE_BGR, 2)
        return frame

    def _draw_final_plate(self, frame, plate: str):
        if plate:
            cv2.putText(frame, f"Plate {plate}", (12, 60), cv2.FONT_HERSHEY_SIMPLEX, 0.8, WHITE_BGR, 2)
        return frame

    def _demo_scan_loop(self, on_frame, on_vote, on_complete, on_error, on_finish, on_scan_start):
        last_frame = None
        best_frame = None
        votes = Counter()
        submitted = []
        cap = None
        scan_started = None
        settings = get_settings()
        scan_seconds = _cfg_float("scan_seconds", SCAN_SECONDS, 0.5)
        inference_fps = _cfg_float("inference_fps", INFERENCE_FPS, 0.5)
        stream_fps = _cfg_float("stream_fps", STREAM_FPS, 1.0)
        max_plate_jobs = _cfg_int("max_plate_jobs", MAX_PLATE_JOBS, 1, 8)
        min_votes = _cfg_int("min_votes", 1, 1)
        min_vote_ratio = _cfg_float("min_vote_ratio", 0.0, 0.0)
        video_source = _video_source()

        try:
            if not self._load_models():
                raise RuntimeError(f"Missing or invalid model: {_model_path()}")
            if not _is_stream_source(video_source) and not os.path.exists(video_source):
                raise RuntimeError(f"Missing demo video: {video_source}")

            cap = cv2.VideoCapture(video_source)
            if not cap.isOpened():
                raise RuntimeError(f"Cannot open video source: {video_source}")
            source_fps = cap.get(cv2.CAP_PROP_FPS) or 30
            frame_delay = 1.0 / max(1, min(source_fps, 30))

            last_stream_t = 0.0
            last_submit_t = 0.0

            with ThreadPoolExecutor(max_workers=max_plate_jobs, thread_name_prefix="lpr-vote") as pool:
                while self._running:
                    frame_started = time.time()
                    ret, frame = cap.read()
                    if not ret:
                        break

                    last_frame = frame.copy()
                    now = time.time()

                    if scan_started is None:
                        remaining = "Find Plate"
                    else:
                        remaining = f"Scanning {max(0, scan_seconds - (now - scan_started)):.1f}s"

                    out_frame = frame.copy()
                    self._draw_status(out_frame, remaining)

                    if on_frame and now - last_stream_t >= (1.0 / stream_fps):
                        last_stream_t = now
                        on_frame(_encode_frame(out_frame))

                    can_submit = scan_started is None or now - scan_started <= scan_seconds
                    if can_submit and len(submitted) < max_plate_jobs and now - last_submit_t >= (1.0 / inference_fps):
                        last_submit_t = now
                        submitted.append(pool.submit(self._detect_plate_from_frame, frame.copy()))

                    done = [f for f in submitted if f.done()]
                    submitted = [f for f in submitted if not f.done()]
                    for future in done:
                        plate, annotated = future.result()
                        if not plate:
                            continue

                        result_t = time.time()
                        if scan_started is None:
                            scan_started = result_t
                            if on_scan_start:
                                on_scan_start()

                        model_frame = annotated.copy()
                        self._draw_status(model_frame, f"Scanning {max(0, scan_seconds - (result_t - scan_started)):.1f}s")
                        last_stream_t = result_t
                        if on_frame:
                            on_frame(_encode_frame(model_frame))

                        votes[plate] += 1
                        best_frame = model_frame
                        if on_vote:
                            on_vote(plate, dict(votes))

                    if scan_started is not None and now - scan_started > scan_seconds:
                        break

                    elapsed = time.time() - frame_started
                    time.sleep(max(0, frame_delay - elapsed))

                if scan_started is not None:
                    try:
                        completed = as_completed(submitted, timeout=5)
                        for future in completed:
                            plate, annotated = future.result()
                            if not plate:
                                continue
                            model_frame = annotated.copy()
                            self._draw_final_plate(model_frame, plate)
                            votes[plate] += 1
                            best_frame = model_frame
                            if on_frame:
                                on_frame(_encode_frame(model_frame))
                            if on_vote:
                                on_vote(plate, dict(votes))
                    except Exception:
                        pass
                else:
                    for future in submitted:
                        future.cancel()

            final_plate = ""
            final_votes = 0
            total_votes = sum(votes.values())
            if votes:
                final_plate, final_votes = votes.most_common(1)[0]
                vote_ratio = (final_votes / total_votes) if total_votes else 0
                if final_votes < min_votes or vote_ratio < min_vote_ratio:
                    final_plate = ""

            if on_complete:
                on_complete({
                    "plate": final_plate,
                    "votes": dict(votes),
                    "final_votes": final_votes,
                    "total_votes": total_votes,
                    "confidence": (final_votes / total_votes) if total_votes else 0,
                    "frame": best_frame if best_frame is not None else last_frame,
                    "yolo11_enabled": True,
                    "poll_started": scan_started is not None,
                    "vehicle_type_id": _cfg_int("demo_vehicle_type_id", CAR_TYPE_ID, 1),
                    "vehicle_label": "Car",
                    "vehicle_box": None,
                })

            final_overlay_plate = final_plate
            while self._running:
                frame_started = time.time()
                ret, frame = cap.read()
                if not ret:
                    break

                model_frame = frame.copy()
                self._draw_final_plate(model_frame, final_overlay_plate)
                now = time.time()
                if on_frame and now - last_stream_t >= (1.0 / stream_fps):
                    last_stream_t = now
                    on_frame(_encode_frame(model_frame))
                elapsed = time.time() - frame_started
                time.sleep(max(0, frame_delay - elapsed))
        except Exception as exc:
            if on_error:
                on_error(str(exc))
            else:
                print(f"[LPREngine] Demo scan error: {exc}")
        finally:
            if cap is not None:
                cap.release()
            with self._scan_lock:
                self._running = False
            if on_finish:
                on_finish()

    # ── Internal loops ────────────────────────────────

    def _reader_loop(self, frame_q: queue.Queue):
        """Open data.mp4 and push frames into the queue, looping forever."""
        video_source = _video_source()
        if not _is_stream_source(video_source) and not os.path.exists(video_source):
            print(f"[LPREngine] Video not found: {video_source}")
            return

        cap = cv2.VideoCapture(video_source)
        if not cap.isOpened():
            print(f"[LPREngine] Cannot open: {video_source}")
            return

        while self._running:
            ret, frame = cap.read()
            if not ret:
                # Loop back to beginning
                cap.set(cv2.CAP_PROP_POS_FRAMES, 0)
                continue

            # Drop oldest frame if consumer is slow
            if frame_q.full():
                try:
                    frame_q.get_nowait()
                except queue.Empty:
                    pass
            try:
                frame_q.put_nowait(frame)
            except queue.Full:
                pass

            time.sleep(1 / 60)  # Cap read speed to 60 fps

        cap.release()

    def _infer_loop(self, frame_q: queue.Queue, on_frame, on_plate):
        """Pull frames, run YOLO + OCR, push annotated frames and detected plates."""
        inference_fps = _cfg_float("inference_fps", INFERENCE_FPS, 0.5)
        stream_fps = _cfg_float("stream_fps", STREAM_FPS, 1.0)
        plate_debounce = _cfg_float("plate_debounce_seconds", PLATE_DEBOUNCE, 0.0)
        min_plate_len = _cfg_int("min_plate_length", MIN_PLATE_LEN, 1)
        infer_interval  = 1.0 / inference_fps
        stream_interval = 1.0 / stream_fps

        last_stream_t   = 0.0
        last_plate      = ""
        last_plate_t    = 0.0

        infer_opts = {"verbose": False, "conf": _cfg_float("plate_confidence", 0.35, 0.0)}

        frame_count = 0

        while self._running:
            try:
                frame = frame_q.get(timeout=0.5)
            except queue.Empty:
                continue

            t0 = time.time()
            out_frame = frame.copy()
            detected_plate = None

            # ── Run YOLO on every nth frame ──────────
            frame_count += 1
            run_infer = (frame_count % max(1, round(60 / inference_fps)) == 0)

            if run_infer and self._model is not None:
                results = self._model(out_frame, **infer_opts)

                if results and results[0].boxes is not None:
                    for box in results[0].boxes:
                        x1, y1, x2, y2 = map(int, box.xyxy[0])
                        conf = float(box.conf[0])

                        # Draw plate bounding box
                        cv2.rectangle(out_frame, (x1, y1), (x2, y2), GREEN_950_BGR, 2)

                        # Crop & OCR
                        plate_crop = frame[y1:y2, x1:x2]
                        if plate_crop.size == 0:
                            continue

                        gray = cv2.cvtColor(plate_crop, cv2.COLOR_BGR2GRAY)
                        enlarged = cv2.resize(gray, None, fx=2.0, fy=2.0, interpolation=cv2.INTER_CUBIC)

                        if self._ocr is None:
                            continue
                        ocr_results = self._ocr.readtext(enlarged, detail=0, allowlist="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-.")
                        if ocr_results:
                            raw_text = " ".join(ocr_results)
                            plate_text = _clean_plate(raw_text)

                            if len(plate_text) >= min_plate_len:
                                detected_plate = plate_text
                                # Annotate on frame
                                cv2.putText(
                                    out_frame, plate_text,
                                    (x1, y2 + 22),
                                    cv2.FONT_HERSHEY_SIMPLEX, 0.75,
                                    WHITE_BGR, 2
                                )

                        # Confidence badge
                        cv2.putText(
                            out_frame, f"{conf:.0%}",
                            (x1, y1 - 6),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.5,
                            WHITE_BGR, 1
                        )

            # ── Overlay timestamp ─────────────────────
            ts = time.strftime("%H:%M:%S")
            cv2.putText(out_frame, ts, (10, out_frame.shape[0] - 10),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.5, WHITE_BGR, 1)

            # ── Push frame to UI ──────────────────────
            now = time.time()
            if on_frame and (now - last_stream_t) >= stream_interval:
                last_stream_t = now
                b64 = _encode_frame(out_frame)
                try:
                    on_frame(b64)
                except Exception as e:
                    print(f"[LPREngine] on_frame error: {e}")

            # ── Plate debounce callback ───────────────
            if detected_plate and on_plate:
                if detected_plate != last_plate or (now - last_plate_t) > plate_debounce:
                    last_plate   = detected_plate
                    last_plate_t = now
                    # Capture snapshot of annotated frame for upload
                    try:
                        on_plate(detected_plate, out_frame.copy())
                    except Exception as e:
                        print(f"[LPREngine] on_plate error: {e}")

            # ── Throttle ──────────────────────────────
            elapsed = time.time() - t0
            sleep_t = infer_interval - elapsed
            if sleep_t > 0:
                time.sleep(sleep_t)


# Singleton instance
lpr_engine = LPREngine()

