import threading

_LOCK = threading.Lock()
_PARKING_VERSION = 0


def notify_parking_changed() -> None:
    global _PARKING_VERSION
    with _LOCK:
        _PARKING_VERSION += 1


def parking_version() -> int:
    with _LOCK:
        return _PARKING_VERSION
