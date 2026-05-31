"""
settings.py - Local desktop runtime settings.
"""
from __future__ import annotations

import json
from pathlib import Path
from typing import Any


APP_ROOT = Path(__file__).resolve().parents[1]
CONFIG_PATH = APP_ROOT / "config" / "settings.json"

DEFAULT_SETTINGS: dict[str, Any] = {
    "camera_mode": "DEMO_VIDEO",
    "demo_video_path": "assets/demo.mp4",
    "rtsp_url": "",
    "lpr_model_path": "assets/yolo11n.pt",
    "vehicle_model_path": "assets/yolo26n.pt",
    "plate_confidence": 0.35,
    "min_plate_length": 5,
    "scan_seconds": 2.0,
    "min_votes": 1,
    "min_vote_ratio": 0.0,
    "inference_fps": 4,
    "stream_fps": 18,
    "jpeg_quality": 65,
    "max_plate_jobs": 2,
    "plate_debounce_seconds": 3.0,
    "entry_device_code": "CAM-IN-01",
    "exit_device_code": "CAM-OUT-01",
    "barrier_in_code": "BAR-IN-01",
    "barrier_out_code": "BAR-OUT-01",
    "demo_vehicle_type_id": 1,
    "auto_entry_after_scan": True,
    "auto_exit_after_scan": True,
    "qr_poll_interval_seconds": 2,
    "qr_timeout_seconds": 180,
    "dashboard_refresh_interval_seconds": 60,
}


def _merged(data: dict[str, Any] | None = None) -> dict[str, Any]:
    merged = dict(DEFAULT_SETTINGS)
    if isinstance(data, dict):
        merged.update({key: value for key, value in data.items() if key in DEFAULT_SETTINGS})
    return merged


def load_settings() -> dict[str, Any]:
    if not CONFIG_PATH.exists():
        save_settings(DEFAULT_SETTINGS)
        return dict(DEFAULT_SETTINGS)
    try:
        with CONFIG_PATH.open("r", encoding="utf-8") as fh:
            return _merged(json.load(fh))
    except Exception:
        return dict(DEFAULT_SETTINGS)


def save_settings(data: dict[str, Any]) -> dict[str, Any]:
    settings = _merged(data)
    CONFIG_PATH.parent.mkdir(parents=True, exist_ok=True)
    with CONFIG_PATH.open("w", encoding="utf-8") as fh:
        json.dump(settings, fh, indent=2, ensure_ascii=False)
        fh.write("\n")
    return settings


def reset_settings() -> dict[str, Any]:
    return save_settings(DEFAULT_SETTINGS)


def get_settings() -> dict[str, Any]:
    return load_settings()


def resolve_path(value: str | None, fallback: str = "") -> str:
    raw = value or fallback
    path = Path(raw)
    if path.is_absolute() or "://" in raw:
        return raw
    return str(APP_ROOT / path)

