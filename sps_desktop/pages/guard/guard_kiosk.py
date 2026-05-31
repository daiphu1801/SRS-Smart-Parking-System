"""
guard_kiosk.py - Guard Kiosk UI
Calls real backend APIs:
  POST /api/v1/iot/parking/entry
  PUT  /api/v1/iot/parking/exit
  POST /api/v1/guard/payments/cash
"""
import io
import threading
import time
from decimal import Decimal, InvalidOperation, ROUND_CEILING
import flet as ft
from core.design_tokens import *
from core import api_client, app_events
from core.settings import get_settings
from ai.lpr_engine import lpr_engine

ENTRY_DEVICE_CODE = "CAM-IN-01"
EXIT_DEVICE_CODE  = "CAM-OUT-01"
BARRIER_IN_CODE   = "BAR-IN-01"
BARRIER_OUT_CODE  = "BAR-OUT-01"
EMPTY_IMAGE_SRC   = "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw=="
DEMO_VEHICLE_TYPE_ID = 1


def build_guard_kiosk(page: ft.Page, on_logout, show_logout: bool = True) -> ft.Control:
    def _cfg(key: str, default=None):
        return get_settings().get(key, default)

    def _cfg_int(key: str, default: int, minimum: int | None = None) -> int:
        try:
            value = int(float(_cfg(key, default)))
        except (TypeError, ValueError):
            value = default
        return max(minimum, value) if minimum is not None else value

    def _cfg_float(key: str, default: float, minimum: float | None = None) -> float:
        try:
            value = float(_cfg(key, default))
        except (TypeError, ValueError):
            value = default
        return max(minimum, value) if minimum is not None else value

    def _cfg_bool(key: str, default: bool) -> bool:
        value = _cfg(key, default)
        if isinstance(value, bool):
            return value
        return str(value).lower() in ("true", "1", "yes", "on")

    # ── Camera feed image ─────────────────────────────
    camera_img = ft.Image(
        src=EMPTY_IMAGE_SRC,
        fit=ft.BoxFit.CONTAIN,
        expand=True,
        border_radius=RADIUS_MD,
        gapless_playback=True,
        filter_quality=ft.FilterQuality.LOW,
    )

    camera_placeholder = ft.Container(
        expand=True,
        bgcolor=PRIMARY,
        border_radius=RADIUS_MD,
        border=border_all(1, BORDER),
        alignment=ft.Alignment(0, 0),
        content=ft.Column(
            alignment=ft.MainAxisAlignment.CENTER,
            horizontal_alignment=ft.CrossAxisAlignment.CENTER,
            controls=[
                ft.Icon(ft.Icons.VIDEOCAM_OFF_ROUNDED, size=48, color=WHITE),
            ],
        ),
    )

    camera_stack = ft.Stack(controls=[camera_placeholder, camera_img], expand=True)

    # ── Detected plate display ────────────────────────
    plate_label = ft.Text(
        "---", font_family=FONT_FAMILY, size=32, weight=W_MEDIUM,
        color=TEXT_PRIMARY, text_align=ft.TextAlign.CENTER,
    )
    plate_status = ft.Text(
        "Đang Chờ Nhận Diện...", font_family=FONT_FAMILY,
        size=SIZE_SMALL, color=TEXT_SECONDARY, text_align=ft.TextAlign.CENTER,
    )
    plate_badge = ft.Container(
        width=160, height=8, border_radius=RADIUS_MD, bgcolor=TEXT_DISABLED,
    )

    # ── Log list ──────────────────────────────────────
    log_column = ft.Column(spacing=6, scroll=ft.ScrollMode.AUTO, expand=True)
    log_header = ft.Row(
        visible=False,
        controls=[
            ft.Icon(ft.Icons.RECEIPT_LONG_ROUNDED, size=16, color=TEXT_SECONDARY),
            ft.Text("Nhật Ký Sự Kiện", font_family=FONT_FAMILY,
                    size=SIZE_SMALL, weight=W_MEDIUM, color=TEXT_SECONDARY),
        ],
    )
    log_box = ft.Container(
        visible=False,
        height=188,
        bgcolor=BG_ELEVATED,
        border_radius=RADIUS_MD,
        border=border_all(1, BORDER),
        padding=PAD_MD,
        content=log_column,
    )
    log_divider = ft.Divider(color=BORDER, height=1, visible=False)

    def add_log(msg: str, color: str = TEXT_SECONDARY, icon=ft.Icons.INFO_ROUNDED):
        ts = time.strftime("%H:%M:%S")
        log_divider.visible = True
        log_header.visible = True
        log_box.visible = True
        log_column.controls.insert(0, ft.Row(
            spacing=8,
            controls=[
                ft.Icon(icon, size=14, color=color),
                ft.Text(f"[{ts}] {msg}", font_family=FONT_FAMILY, size=SIZE_CAPTION, color=color, expand=True),
            ],
        ))
        # Keep history small; the box itself scrolls instead of growing the page.
        if len(log_column.controls) > 50:
            log_column.controls.pop()
        request_page_update(page)

    # ── Manual plate input ────────────────────────────
    manual_plate_field = ft.TextField(
        label="Nhập Biển Số Thủ Công",
        border_color=BORDER,
        focused_border_color=PRIMARY,
        label_style=ft.TextStyle(color=TEXT_SECONDARY, font_family=FONT_FAMILY),
        text_style=ft.TextStyle(color=TEXT_PRIMARY, font_family=FONT_FAMILY, size=SIZE_H3, weight=W_MEDIUM),
        bgcolor=BG_ELEVATED,
        border_radius=RADIUS_MD,
        cursor_color=PRIMARY,
        expand=True,
    )
    zone_dropdown = ft.Dropdown(
        label="Vùng Cổng",
        options=[],
        border_color=BORDER,
        focused_border_color=PRIMARY,
        border_radius=RADIUS_MD,
        bgcolor=BG_ELEVATED,
        color=TEXT_PRIMARY,
        fill_color=WHITE,
        menu_style=ft.MenuStyle(
            bgcolor=WHITE,
            side=ft.BorderSide(1, BORDER),
            shape=ft.RoundedRectangleBorder(radius=RADIUS_MD),
        ),
        focused_border_width=1,
        content_padding=ft.Padding(14, 0, 14, 0),
        text_style=ft.TextStyle(color=TEXT_PRIMARY, font_family=FONT_FAMILY, size=SIZE_BODY),
        label_style=ft.TextStyle(color=TEXT_SECONDARY, font_family=FONT_FAMILY, size=SIZE_SMALL),
        trailing_icon=ft.Icon(ft.Icons.EXPAND_MORE_ROUNDED, color=PRIMARY),
        expand=True,
    )
    zone_status = ft.Text(
        "Chưa Chọn Vùng Cổng.",
        font_family=FONT_FAMILY,
        size=SIZE_CAPTION,
        color=TEXT_SECONDARY,
    )

    # ── Session state ─────────────────────────────────
    _state = {
        "current_plate": "",
        "session_id": None,
        "amount_left": 0,
        "last_frame": None,  # raw numpy frame for upload
        "busy": False,
        "scan_mode": None,
        "last_mode": None,
        "vehicle_type_id": _cfg_int("demo_vehicle_type_id", DEMO_VEHICLE_TYPE_ID, 1),
        "vehicle_label": "Vehicle",
        "session_type": "",
        "qr_pay_code": "",
        "zones": [],
        "selected_zone_id": None,
        "selected_zone_name": "",
        "zone_devices": [],
    }

    def _set_busy(is_busy: bool, mode: str | None = None):
        _state["busy"] = is_busy
        _state["scan_mode"] = mode
        if mode:
            _state["last_mode"] = mode
        play_entry_btn.disabled = is_busy
        play_exit_btn.disabled = is_busy
        entry_btn.disabled = is_busy
        exit_btn.disabled = is_busy
        cash_btn.disabled = is_busy
        payment_btn.disabled = is_busy

        if not is_busy:
            play_entry_btn.content = "Play Vào"
            play_exit_btn.content = "Play Ra"
        elif mode == "ENTRY":
            play_entry_btn.content = "Đang Quét..."
        elif mode == "EXIT":
            play_exit_btn.content = "Đang Quét..."

    def _upload_frame_and_get_url() -> str | None:
        """Upload the latest camera frame and return the image URL."""
        # TODO: The image upload endpoint is not yet available in Java backend.
        # Temporarily return None so the entry/exit flows can succeed without images.
        return None

    def _page_items(resp: dict | list) -> list:
        if isinstance(resp, list):
            return resp
        if not isinstance(resp, dict):
            return []
        data = resp.get("data")
        if isinstance(data, list):
            return data
        if isinstance(data, dict) and isinstance(data.get("content"), list):
            return data["content"]
        return []

    def _vehicle_type_name(vehicle_type_id: int | None) -> str:
        if vehicle_type_id == 2:
            return "Xe Máy"
        return "Ô Tô"

    def _ensure_session_vehicle_type(plate: str):
        vehicle_type_id = _state.get("vehicle_type_id")
        if not vehicle_type_id:
            return
        try:
            sessions = _page_items(api_client.admin_get_parking_sessions(vehicle_no=plate, size=10))
            open_sessions = [s for s in sessions if not s.get("exit_time")]
            if not open_sessions:
                return
            session = open_sessions[0]
            session_id = session.get("id")
            current_vehicle_type_id = session.get("vehicle_type_id") or session.get("vehicleTypeId")
            if session_id and current_vehicle_type_id != vehicle_type_id:
                api_client.admin_update_parking_session(
                    session_id,
                    update_vehicle_type_id=vehicle_type_id,
                )
                add_log(f"Đã Cập Nhật Loại Xe: {_vehicle_type_name(vehicle_type_id)}.", SUCCESS, ft.Icons.DIRECTIONS_CAR_ROUNDED)
        except Exception:
            pass

    def _friendly_error(exc: Exception) -> str:
        response = getattr(exc, "response", None)
        if response is not None:
            status = getattr(response, "status_code", None)
            message = ""
            try:
                body = response.json()
                message = body.get("message") or body.get("error") or body.get("data") or ""
            except Exception:
                message = ""
            if message:
                return f"Backend {status}: {_vi_backend_message(message)}"
            if status:
                return f"Backend {status}"
        return _vi_backend_message(str(exc).split(" for url:")[0])

    def _vi_backend_message(message) -> str:
        raw = str(message or "").strip()
        if not raw:
            return ""
        normalized = raw.lower().replace("_", " ").replace("-", " ")
        mappings = (
            ("entry processed successfully", "Xử Lý Vào Thành Công"),
            ("exit processed successfully", "Xử Lý Ra Thành Công"),
            ("payment processed successfully", "Xử Lý Thanh Toán Thành Công"),
            ("parking entry processed successfully", "Xử Lý Vào Thành Công"),
            ("parking exit processed successfully", "Xử Lý Ra Thành Công"),
            ("manual open processed successfully", "Mở Barrier Thành Công"),
            ("barrier opened successfully", "Mở Barrier Thành Công"),
            ("vehicle already in parking", "Xe Đã Ở Trong Bãi"),
            ("already in parking", "Xe Đã Ở Trong Bãi"),
            ("not found", "Không Tìm Thấy"),
            ("invalid", "Không Hợp Lệ"),
            ("successfully", "Thành Công"),
            ("success", "Thành Công"),
            ("processed", "Đã Xử Lý"),
            ("payment required", "Cần Thanh Toán"),
        )
        for english, vietnamese in mappings:
            if english in normalized:
                return vietnamese
        return ui_title(raw)

    def _money_decimal(value) -> Decimal:
        if value is None or value == "" or isinstance(value, bool):
            return Decimal("0")
        if isinstance(value, Decimal):
            return value
        try:
            return Decimal(str(value).replace(",", "").strip())
        except (InvalidOperation, ValueError):
            return Decimal("0")

    def _payable_amount(value) -> Decimal:
        amount = _money_decimal(value)
        if amount <= 0:
            return Decimal("0")
        return amount.to_integral_value(rounding=ROUND_CEILING)

    def _notify_parking_changed():
        app_events.notify_parking_changed()

    def _page_data_list(resp: dict | list) -> list:
        if isinstance(resp, list):
            return resp
        if isinstance(resp, dict):
            data = resp.get("data")
            if isinstance(data, list):
                return data
        return []

    def _value(item: dict, *keys, default=None):
        for key in keys:
            value = item.get(key)
            if value not in (None, ""):
                return value
        return default

    def _device_id(device: dict | None):
        return _value(device or {}, "id", "device_id", "deviceId")

    def _device_code(device: dict | None):
        return _value(device or {}, "device_code", "deviceCode")

    def _device_name(device: dict | None):
        device = device or {}
        return _value(
            device,
            "device_name",
            "deviceName",
            "name",
            "display_name",
            "displayName",
            default=_device_code(device),
        )

    def _device_type(device: dict | None) -> str:
        return str(_value(device or {}, "device_type", "deviceType", default="")).upper()

    def _device_direction(device: dict | None) -> str:
        return str(_value(device or {}, "direction", default="BOTH")).upper()

    def _zone_name(zone_id: int | None) -> str:
        for zone in _state.get("zones") or []:
            if zone.get("id") == zone_id:
                return zone.get("zone_name") or zone.get("zoneName") or f"Vùng #{zone_id}"
        return f"Vùng #{zone_id}" if zone_id else ""

    def _direction_matches(device: dict, direction: str) -> bool:
        device_direction = _device_direction(device)
        return device_direction in (direction, "BOTH", "")

    def _pick_device(device_type: str, direction: str | None = None) -> dict | None:
        device_type = device_type.upper()
        devices = _state.get("zone_devices") or []
        candidates = [
            device for device in devices
            if _device_type(device) == device_type
        ]
        if direction:
            direction = direction.upper()
            directed = [device for device in candidates if _direction_matches(device, direction)]
            if directed:
                exact = [device for device in directed if _device_direction(device) == direction]
                return exact[0] if exact else directed[0]
        return candidates[0] if candidates else None

    def _pick_lpr_camera(direction: str) -> dict | None:
        devices = _state.get("zone_devices") or []
        candidates = [
            device for device in devices
            if _device_type(device) in ("LPR_CAM", "LPR_CAM_AI", "LPR_CAM_SECURITY")
            and _direction_matches(device, direction)
        ]
        exact = [device for device in candidates if _device_direction(device) == direction]
        return exact[0] if exact else (candidates[0] if candidates else None)

    def _selected_zone_id() -> int | None:
        try:
            return int(zone_dropdown.value or _state.get("selected_zone_id"))
        except (TypeError, ValueError):
            return None

    def _set_zone_devices(zone_id: int | None, zone_name: str, devices: list[dict]):
        _state["selected_zone_id"] = zone_id
        _state["selected_zone_name"] = zone_name
        _state["zone_devices"] = devices
        if zone_id is not None:
            zone_dropdown.value = str(zone_id)

        entry_camera = _pick_lpr_camera("IN")
        exit_camera = _pick_lpr_camera("OUT")
        barrier_in = _pick_device("BARRIER", "IN")
        barrier_out = _pick_device("BARRIER", "OUT")
        parts = [
            f"Cam Vào: {ui_title(_device_name(entry_camera)) if entry_camera else '-'}",
            f"Cam Ra: {ui_title(_device_name(exit_camera)) if exit_camera else '-'}",
            f"Barrier Vào: {ui_title(_device_name(barrier_in)) if barrier_in else '-'}",
            f"Barrier Ra: {ui_title(_device_name(barrier_out)) if barrier_out else '-'}",
        ]
        zone_status.value = " | ".join(parts)
        zone_status.color = TEXT_SECONDARY if devices else WARNING

    def _load_zone_devices(zone_id: int, zone_name: str = "", announce: bool = False):
        try:
            devices = _page_data_list(api_client.admin_get_devices_by_zone(zone_id))
            _set_zone_devices(zone_id, zone_name or _zone_name(zone_id), devices)
            if announce:
                add_log(f"Đã Chọn Vùng [{ui_title(zone_name or _zone_name(zone_id))}].", PRIMARY, ft.Icons.LOCATION_ON_ROUNDED)
        except Exception as ex:
            zone_status.value = f"Lỗi Tải Thiết Bị Theo Vùng: {_friendly_error(ex)}"
            zone_status.color = DANGER
        request_page_update(page)

    def _load_zone_selector():
        def fetch():
            try:
                zones = _page_data_list(api_client.admin_get_zones())
                _state["zones"] = zones
                zone_dropdown.options = [
                    ft.dropdown.Option(
                        key=str(zone.get("id")),
                        text=ui_title(zone.get("zone_name") or zone.get("zoneName") or f"Vùng #{zone.get('id')}"),
                        content=ft.Text(
                            ui_title(zone.get("zone_name") or zone.get("zoneName") or f"Vùng #{zone.get('id')}"),
                            font_family=FONT_FAMILY,
                            size=SIZE_BODY,
                            color=TEXT_PRIMARY,
                            weight=W_REGULAR,
                        ),
                    )
                    for zone in zones
                    if zone.get("id") is not None
                ]
                if not zone_dropdown.options:
                    _set_zone_devices(None, "", [])
                    zone_status.value = "Chưa Có Vùng Cổng."
                    zone_status.color = WARNING
                    request_page_update(page)
                    return

                selected = _selected_zone_id()
                option_ids = {int(option.key) for option in zone_dropdown.options}
                if selected not in option_ids:
                    gate_zone = next(
                        (zone for zone in zones if str(zone.get("zone_type") or zone.get("zoneType") or "").upper() == "GATE"),
                        zones[0],
                    )
                    selected = int(gate_zone.get("id"))
                zone_dropdown.value = str(selected)
                _load_zone_devices(selected, _zone_name(selected))
            except Exception as ex:
                zone_status.value = f"Lỗi Tải Danh Sách Vùng: {_friendly_error(ex)}"
                zone_status.color = DANGER
                request_page_update(page)

        page.run_thread(fetch)

    def _response_data(resp: dict) -> dict:
        if isinstance(resp, dict) and isinstance(resp.get("data"), dict):
            return resp["data"]
        return resp if isinstance(resp, dict) else {}

    def _resolve_barrier_code(preferred_code: str, direction: str | None = None) -> str | None:
        if direction:
            selected_barrier = _pick_device("BARRIER", direction)
            selected_code = _device_code(selected_barrier)
            if selected_code:
                return selected_code
        try:
            devices = _state.get("zone_devices") or _page_data_list(api_client.admin_get_devices())
            barriers = [
                d for d in devices
                if str(d.get("device_type") or d.get("deviceType") or "").upper() == "BARRIER"
            ]
            if not barriers:
                return None
            for device in barriers:
                code = device.get("device_code") or device.get("deviceCode")
                if code == preferred_code:
                    return code
            if direction:
                directed = [device for device in barriers if _direction_matches(device, direction)]
                if directed:
                    return directed[0].get("device_code") or directed[0].get("deviceCode")
            return barriers[0].get("device_code") or barriers[0].get("deviceCode")
        except Exception:
            return None

    def _auto_confirm_exit_after_payment(plate: str, source: str):
        if not plate:
            add_log("Không Có Biển Số Để Xác Nhận Xe Ra.", DANGER, ft.Icons.WARNING_ROUNDED)
            return
        add_log(f"{source} Đã Xác Nhận. Tự Động Mở Cổng Ra.", SUCCESS, ft.Icons.CHECK_CIRCLE_ROUNDED)
        handle_exit(plate)

    # ── Entry (xe vào) ────────────────────────────────
    def handle_entry(plate: str):
        _state["current_plate"] = plate
        session_info_panel.visible = False
        image_url = _upload_frame_and_get_url()
        zone_id = _selected_zone_id()
        entry_camera = _pick_lpr_camera("IN")

        try:
            resp = api_client.iot_parking_entry(
                device_code=_device_code(entry_camera) or _cfg("entry_device_code", ENTRY_DEVICE_CODE),
                vehicle_no=plate,
                vehicle_type_id=_state.get("vehicle_type_id") or _cfg_int("demo_vehicle_type_id", DEMO_VEHICLE_TYPE_ID, 1),
                zone_id=zone_id,
                device_id=_device_id(entry_camera),
                image_in_url=image_url,
            )
            data   = resp.get("data", {})
            print(f"DEBUG - entry response data: {data}")
            action = data.get("action", "") or data.get("command", "")
            
            if action:
                barrier = _pick_device("BARRIER", "IN")
                ip_addr = barrier.get("ipAddress") or barrier.get("ip_address") or "N/A" if barrier else "N/A"
                add_log(f"Lệnh: {action} - IP Thiết Bị: {ip_addr}", PRIMARY, ft.Icons.WIFI_TETHERING_ROUNDED)

            msg    = data.get("message", "")
            session_data = data.get("session") or {}
            sid    = session_data.get("id") or session_data.get("sessionId") or data.get("sessionId") or data.get("id")
            _state["session_id"] = sid
            _state["session_type"] = str(session_data.get("type") or data.get("type") or "").upper()
            _state["amount_left"] = Decimal("0")
            _state["qr_pay_code"] = ""

            if action in ("OPEN_BARRIER", "OPEN"):
                barrier = _pick_device("BARRIER", "IN")
                ip_addr = barrier.get("ipAddress") or barrier.get("ip_address") or "N/A" if barrier else "N/A"
                barrier_name = _device_name(barrier) if barrier else "N/A"
                plate_badge.bgcolor = SUCCESS
                duplicate_entry = bool(data.get("duplicate")) or "đã ở trong bãi" in str(msg).lower()
                backend_msg = _vi_backend_message(msg) or msg or "Xử Lý Vào Thành Công"
                plate_status.value  = f"{backend_msg} - Mở {barrier_name} (IP: {ip_addr})"
                plate_status.color  = SUCCESS
                customer_type = "Thuê Bao" if _state["session_type"] == "SUBSCRIBER" else "Vãng Lai"
                log_icon = ft.Icons.INFO_ROUNDED if duplicate_entry else ft.Icons.CHECK_CIRCLE_ROUNDED
                log_color = WARNING if duplicate_entry else SUCCESS
                add_log(f"[{plate}] {customer_type} - {backend_msg}", log_color, log_icon)
                add_log(f"Barrier Vào Đã Mở Tự Động [{plate}] - {barrier_name} (IP: {ip_addr})", SUCCESS, ft.Icons.SENSOR_DOOR_ROUNDED)

                # Async upload image if assignedImageName exists
                assigned_img = data.get("assignedImageName")
                frame = _state.get("last_frame")
                if assigned_img and frame is not None:
                    import cv2
                    success, buffer = cv2.imencode('.jpg', frame)
                    if success:
                        from core.supabase_client import upload_image_to_storage
                        import core.api_client as ac
                        page.run_thread(upload_image_to_storage, buffer.tobytes(), assigned_img, ac._token)
                        
                _notify_parking_changed()
            else:
                plate_badge.bgcolor = DANGER
                plate_status.value  = f"Từ Chối: {_vi_backend_message(msg)}"
                plate_status.color  = DANGER
                add_log(f"Vào Từ Chối [{plate}] {_vi_backend_message(msg)}", DANGER, ft.Icons.BLOCK_ROUNDED)

        except Exception as e:
            err = _friendly_error(e)
            plate_badge.bgcolor = DANGER
            plate_status.value  = f"Lỗi Xe Vào: {err}"
            plate_status.color  = DANGER
            add_log(f"Lỗi Xe Vào [{plate}]: {err}", DANGER, ft.Icons.ERROR_ROUNDED)

        request_page_update(page)

    # ── Exit (xe ra) ─────────────────────────────────
    def handle_exit(plate: str):
        _state["current_plate"] = plate
        image_url = _upload_frame_and_get_url()
        _ensure_session_vehicle_type(plate)
        zone_id = _selected_zone_id()
        exit_camera = _pick_lpr_camera("OUT")

        try:
            resp = api_client.iot_parking_exit(
                device_code=_device_code(exit_camera) or _cfg("exit_device_code", EXIT_DEVICE_CODE),
                vehicle_no=plate,
                vehicle_type_id=_state.get("vehicle_type_id") or _cfg_int("demo_vehicle_type_id", DEMO_VEHICLE_TYPE_ID, 1),
                zone_id=zone_id,
                device_id=_device_id(exit_camera),
                image_out_url=image_url,
            )
            data      = resp.get("data", {})
            action    = data.get("action", "") or data.get("command", "")
            
            if action:
                barrier = _pick_device("BARRIER", "OUT")
                ip_addr = barrier.get("ipAddress") or barrier.get("ip_address") or "N/A" if barrier else "N/A"
                add_log(f"Lệnh: {action} - IP Thiết Bị: {ip_addr}", PRIMARY, ft.Icons.WIFI_TETHERING_ROUNDED)

            msg       = data.get("message", "")
            session_data = data.get("session") or {}
            if not session_data and ("amountLeft" in data or "amount_left" in data):
                session_data = data
            
            amount    = session_data.get("amountLeft") or session_data.get("amount_left") or 0
            payable_amount = _payable_amount(amount)
            sid       = session_data.get("id") or session_data.get("sessionId") or data.get("id") or data.get("sessionId")
            
            if session_data and sid:
                session_val_in.value = str(session_data.get("entryTime") or "-").replace("T", " ")[:19]
                session_val_out.value = str(session_data.get("exitTime") or "-").replace("T", " ")[:19]
                session_val_due.value = _fmt_vnd(session_data.get("amountDue") or 0)
                session_val_paid.value = _fmt_vnd(session_data.get("amountPaid") or 0)
                session_val_left.value = _fmt_vnd(session_data.get("amountLeft") or 0)
                
                checkin_img = session_data.get("imageInUrl") or session_data.get("checkinImageUrl") or data.get("imageInUrl") or data.get("checkinImageUrl")
                print(f"DEBUG - exit checkin_img: {checkin_img}")
                if checkin_img:
                    from core.supabase_client import SUPABASE_URL
                    if str(checkin_img).startswith("http"):
                        session_val_img.src = checkin_img
                    else:
                        session_val_img.src = f"{SUPABASE_URL}/storage/v1/object/public/parking-images/{checkin_img}"
                    print(f"DEBUG - session_val_img.src = {session_val_img.src}")
                    session_val_img.visible = True
                else:
                    session_val_img.visible = False

                session_info_panel.visible = True
            else:
                session_info_panel.visible = False

            _state["session_id"] = sid
            _state["amount_left"] = payable_amount
            if payable_amount > 0:
                _state["session_type"] = "GUEST"

            if action in ("OPEN_BARRIER", "OPEN"):
                barrier = _pick_device("BARRIER", "OUT")
                ip_addr = barrier.get("ipAddress") or barrier.get("ip_address") or "N/A" if barrier else "N/A"
                barrier_name = _device_name(barrier) if barrier else "N/A"
                plate_badge.bgcolor = SUCCESS
                plate_status.value  = f"Xử Lý Ra Thành Công - Mở {barrier_name} (IP: {ip_addr})"
                plate_status.color  = SUCCESS
                _state["amount_left"] = Decimal("0")
                _state["qr_pay_code"] = ""
                add_log(f"Xử Lý Ra Thành Công [{plate}] - Mở Cổng Tự Động", SUCCESS, ft.Icons.EXIT_TO_APP_ROUNDED)
                add_log(f"Barrier Ra Đã Mở Tự Động [{plate}] - {barrier_name} (IP: {ip_addr})", SUCCESS, ft.Icons.SENSOR_DOOR_ROUNDED)

                # Async upload image if assignedImageName exists
                assigned_img = data.get("assignedImageName")
                frame = _state.get("last_frame")
                if assigned_img and frame is not None:
                    import cv2
                    success, buffer = cv2.imencode('.jpg', frame)
                    if success:
                        from core.supabase_client import upload_image_to_storage
                        import core.api_client as ac
                        page.run_thread(upload_image_to_storage, buffer.tobytes(), assigned_img, ac._token)
                        
                _notify_parking_changed()
            else:
                if payable_amount > 0:
                    plate_badge.bgcolor = WARNING
                    plate_status.value  = f"Cần Thanh Toán: {_fmt_vnd(payable_amount)}"
                    plate_status.color  = WARNING
                    add_log(f"Ra Nợ Phí [{plate}]: {_fmt_vnd(payable_amount)}. Chờ Tiền Mặt Hoặc Xác Nhận Thanh Toán.", WARNING, ft.Icons.PAYMENT_ROUNDED)
                    add_log(f"Barrier Ra Chưa Mở [{plate}]", WARNING, ft.Icons.SENSOR_DOOR_ROUNDED)
                    _notify_parking_changed()
                else:
                    plate_badge.bgcolor = DANGER
                    plate_status.value  = f"Ra Từ Chối: {_vi_backend_message(msg or 'Không Có Lệnh Mở Cổng')}"
                    plate_status.color  = DANGER
                    add_log(f"Ra Từ Chối [{plate}]: {_vi_backend_message(msg or 'Không Có Lệnh Mở Cổng')}", DANGER, ft.Icons.BLOCK_ROUNDED)

        except Exception as e:
            err = _friendly_error(e)
            plate_badge.bgcolor = DANGER
            plate_status.value  = f"Lỗi Xe Ra: {err}"
            plate_status.color  = DANGER
            add_log(f"Lỗi Xe Ra [{plate}]: {err}", DANGER, ft.Icons.ERROR_ROUNDED)

        request_page_update(page)

    # ── LPR callback (auto detected) ─────────────────
    def on_plate_detected(plate: str, frame):
        _state["last_frame"]    = frame
        _state["current_plate"] = plate
        plate_label.value       = plate
        plate_badge.bgcolor     = PRIMARY
        plate_status.value      = "Biển Số Đã Nhận Diện."
        plate_status.color      = TEXT_SECONDARY
        add_log(f"AI Nhận Diện: {plate}", PRIMARY, ft.Icons.DOCUMENT_SCANNER_ROUNDED)
        request_page_update(page)

    def on_frame_update(b64: str):
        camera_img.src       = b64
        camera_img.visible   = True
        request_page_update(page, retries=(0.0,))

    def on_vote_update(plate: str, votes: dict):
        _state["current_plate"] = plate
        plate_label.value = plate
        plate_badge.bgcolor = PRIMARY
        vote_text = ", ".join(f"{k}:{v}" for k, v in sorted(votes.items(), key=lambda item: item[1], reverse=True)[:3])
        plate_status.value = f"Đang Tổng Hợp: {vote_text}"
        plate_status.color = TEXT_SECONDARY
        request_page_update(page)

    def start_demo_scan(mode: str, bypass_sensor: bool = False):
        if _state.get("busy"):
            return
            
        direction = "IN" if mode == "ENTRY" else "OUT"
        sensor = _pick_device("SENSOR", direction)
        lpr_cam = _pick_lpr_camera(direction)

        if not bypass_sensor:
            if not sensor:
                add_log(f"Chưa cấu hình Cảm Biến (SENSOR) ở cổng {direction}.", DANGER, ft.Icons.WARNING_ROUNDED)
                request_page_update(page)
                return
            if not lpr_cam:
                add_log(f"Chưa cấu hình Camera (LPR_CAM) ở cổng {direction}.", DANGER, ft.Icons.WARNING_ROUNDED)
                request_page_update(page)
                return

        _set_busy(True, mode)
        manual_actions_row.visible = False
        plate_label.value = "---"
        plate_badge.bgcolor = PRIMARY
        plate_status.value = "Đang Xử Lý..."
        plate_status.color = TEXT_SECONDARY
        _state["qr_pay_code"] = ""
        
        mode_text = "Vào" if mode == "ENTRY" else "Ra"
        add_log(f"Bắt Đầu Demo {mode_text}", PRIMARY, ft.Icons.PLAY_CIRCLE_ROUNDED)
        
        if not bypass_sensor and sensor and lpr_cam:
            sensor_ip = sensor.get("ipAddress") or sensor.get("ip_address") or "N/A"
            sensor_name = _device_name(sensor)
            cam_ip = lpr_cam.get("ipAddress") or lpr_cam.get("ip_address") or "N/A"
            cam_name = _device_name(lpr_cam)
            add_log(f"[Tín Hiệu 1] SENSOR Kích Hoạt Cò: {sensor_name} (IP: {sensor_ip})", PRIMARY, ft.Icons.SENSORS_ROUNDED)
            add_log(f"Yêu cầu AI quét Camera: {cam_name} (IP: {cam_ip})", PRIMARY, ft.Icons.CAMERA_ALT_ROUNDED)
        elif bypass_sensor:
            add_log("Quét Lại Bằng Tay (Bỏ Qua SENSOR)", WARNING, ft.Icons.REFRESH_ROUNDED)
        request_page_update(page)

        def on_complete(result: dict):
            plate = result.get("plate") or ""
            votes = result.get("votes") or {}
            frame = result.get("frame")
            if frame is not None:
                _state["last_frame"] = frame

            if not plate:
                plate_badge.bgcolor = DANGER
                plate_status.value = "Không Đọc Được Biển Số. Nhập Tay Rồi Gửi Thủ Công."
                plate_status.color = DANGER
                manual_actions_row.visible = True
                if result.get("poll_started"):
                    add_log(f"AI Không Đọc Được Biển Số Trong {_cfg_float('scan_seconds', 2.0, 0.5):.1f} Giây Quét.", DANGER, ft.Icons.WARNING_ROUNDED)
                else:
                    add_log("AI Không Tìm Thấy Biển Số Trong Video.", DANGER, ft.Icons.WARNING_ROUNDED)
                request_page_update(page)
                return

            _state["current_plate"] = plate
            _state["vehicle_type_id"] = result.get("vehicle_type_id") or _cfg_int("demo_vehicle_type_id", DEMO_VEHICLE_TYPE_ID, 1)
            _state["vehicle_label"] = ui_title(result.get("vehicle_label") or "Vehicle")
            manual_plate_field.value = plate
            plate_label.value = plate
            plate_badge.bgcolor = SUCCESS
            vehicle_name = _vehicle_type_name(_state["vehicle_type_id"])
            plate_status.value = f"Kết Quả: {plate} - {vehicle_name} ({result.get('final_votes', 0)}/{result.get('total_votes', 0)} Lượt)"
            plate_status.color = SUCCESS
            add_log(f"Kết Quả Nhận Diện [{plate}] {vehicle_name} {votes}", SUCCESS, ft.Icons.HOW_TO_VOTE_ROUNDED)
            request_page_update(page)

            if mode == "ENTRY":
                if _cfg_bool("auto_entry_after_scan", True):
                    page.run_thread(handle_entry, plate)
                else:
                    manual_actions_row.visible = True
                    add_log("Tự Động Gửi Xe Vào Đang Tắt.", WARNING, ft.Icons.INFO_ROUNDED)
            else:
                if _cfg_bool("auto_exit_after_scan", True):
                    page.run_thread(handle_exit, plate)
                else:
                    manual_actions_row.visible = True
                    add_log("Tự Động Gửi Xe Ra Đang Tắt.", WARNING, ft.Icons.INFO_ROUNDED)
            
            lpr_engine.stop()

        def on_finish():
            _set_busy(False)
            request_page_update(page)

        def on_error(message: str):
            _set_busy(False)
            plate_badge.bgcolor = DANGER
            plate_status.value = f"Lỗi AI Demo: {message}"
            plate_status.color = DANGER
            add_log(f"Lỗi AI Demo: {message}", DANGER, ft.Icons.ERROR_ROUNDED)
            request_page_update(page)

        def on_scan_start():
            plate_status.value = "Đang Xử Lý..."
            plate_status.color = TEXT_SECONDARY
            request_page_update(page)

        started = lpr_engine.run_demo_scan(
            on_frame=on_frame_update,
            on_vote=on_vote_update,
            on_complete=on_complete,
            on_error=on_error,
            on_finish=on_finish,
            on_scan_start=on_scan_start,
        )
        if not started:
            _set_busy(False)
            add_log("AI Đang Chạy, Vui Lòng Đợi Lượt Hiện Tại Kết Thúc.", WARNING, ft.Icons.HOURGLASS_TOP_ROUNDED)
            request_page_update(page)

    # ── Button handlers ───────────────────────────────
    def btn_entry(_):
        plate = manual_plate_field.value.strip().upper() or _state["current_plate"]
        if not plate or plate == "---":
            add_log("Chưa Có Biển Số!", DANGER, ft.Icons.WARNING_ROUNDED)
            return
        page.run_thread(handle_entry, plate)

    def btn_exit(_):
        plate = manual_plate_field.value.strip().upper() or _state["current_plate"]
        if not plate or plate == "---":
            add_log("Chưa Có Biển Số!", DANGER, ft.Icons.WARNING_ROUNDED)
            return
        page.run_thread(handle_exit, plate)

    def _manual_payment_flow(source_label: str):
        sid = _state.get("session_id")
        plate = _state.get("current_plate")
        if _state.get("last_mode") != "EXIT":
            add_log("Hãy Quét Xe Ra Trước Khi Xác Nhận Thanh Toán.", DANGER, ft.Icons.WARNING_ROUNDED)
            return
        if not sid and _state.get("amount_left", 0) <= 0:
            add_log("Không Có Phiên Đỗ Xe Nào Cần Thanh Toán.", DANGER, ft.Icons.WARNING_ROUNDED)
            return

        def do_calculate():
            try:
                amount = _payable_amount(_state.get("amount_left", 0))
                if amount <= 0:
                    add_log("Không Cần Thu Tiền Cho Phiên Này.", PRIMARY, ft.Icons.CHECK_CIRCLE_ROUNDED)
                    return

                def show_payment_dialog(e=None):
                    amount_field = ft.TextField(
                        label="Số Tiền Thu",
                        value=str(int(amount)),
                        keyboard_type=ft.KeyboardType.NUMBER,
                        border_color=BORDER,
                        focused_border_color=SUCCESS,
                        label_style=ft.TextStyle(color=TEXT_SECONDARY, font_family=FONT_FAMILY),
                        text_style=ft.TextStyle(color=TEXT_PRIMARY, font_family=FONT_FAMILY),
                        bgcolor=BG_ELEVATED,
                        border_radius=RADIUS_MD,
                        cursor_color=SUCCESS,
                    )

                    note_field = ft.TextField(
                        label="Ghi chú (Tùy chọn, vd: MBBank)",
                        visible=(source_label != "Tiền Mặt"),
                        border_color=BORDER,
                        focused_border_color=SUCCESS,
                        label_style=ft.TextStyle(color=TEXT_SECONDARY, font_family=FONT_FAMILY),
                        text_style=ft.TextStyle(color=TEXT_PRIMARY, font_family=FONT_FAMILY),
                        bgcolor=BG_ELEVATED,
                        border_radius=RADIUS_MD,
                        cursor_color=SUCCESS,
                    )

                    def confirm_payment(_):
                        try:
                            collected = _payable_amount(amount_field.value)
                            if collected <= 0:
                                add_log("Số Tiền Thu Phải Lớn Hơn 0.", DANGER, ft.Icons.WARNING_ROUNDED)
                                return
                            
                            if source_label == "Tiền Mặt":
                                pay_resp = api_client.guard_confirm_cash(plate, float(collected))
                            else:
                                pay_resp = api_client.guard_confirm_bank_transfer(plate, float(collected), note=note_field.value)
                                
                            pay_data = _response_data(pay_resp)
                            remaining = _payable_amount(pay_data.get("amount_left") or pay_data.get("amountLeft"))
                            if remaining > 0:
                                _state["amount_left"] = remaining
                                _state["qr_pay_code"] = ""
                                _notify_parking_changed()
                                add_log(
                                    f"{source_label} Thành Công: {_fmt_vnd(collected)}. Còn Nợ: {_fmt_vnd(remaining)}",
                                    WARNING,
                                    ft.Icons.PAYMENT_ROUNDED,
                                )
                                dlg.open = False
                                request_page_update(page)
                                return

                            add_log(f"{source_label} Thành Công: {_fmt_vnd(collected)}", SUCCESS, ft.Icons.CHECK_CIRCLE_ROUNDED)
                            _state["amount_left"] = Decimal("0")
                            _state["qr_pay_code"] = ""
                            _notify_parking_changed()
                            dlg.open = False
                            request_page_update(page)
                            add_log("Vui lòng ấn 'Quét Lại Ra' để hệ thống kiểm tra và mở barrier.", WARNING, ft.Icons.TOUCH_APP_ROUNDED)
                        except Exception as ex:
                            add_log(f"Từ Chối Thu Tiền: {_friendly_error(ex)}", DANGER, ft.Icons.ERROR_ROUNDED)
                            dlg.open = False
                            request_page_update(page)

                    controls = [
                        ft.Text(f"Phí Tạm Tính: {_fmt_vnd(amount)}", font_family=FONT_FAMILY,
                                size=SIZE_BODY, color=WARNING),
                        amount_field,
                    ]
                    if source_label != "Tiền Mặt":
                        controls.append(note_field)

                    dlg = ft.AlertDialog(
                        modal=True,
                        title=ft.Text(source_label, font_family=FONT_FAMILY, color=TEXT_PRIMARY),
                        bgcolor=BG_CARD,
                        shape=ft.RoundedRectangleBorder(radius=RADIUS_MD),
                        content=ft.Column(tight=True, spacing=12, controls=controls),
                        actions=[
                            ft.OutlinedButton(content="Hủy", on_click=lambda _: setattr(dlg, "open", False) or request_page_update(page),
                                              style=outlined_button_style()),
                            ft.ElevatedButton(
                                content="Xác Nhận", icon=ft.Icons.CHECK_ROUNDED,
                                on_click=confirm_payment,
                                style=filled_button_style(),
                            ),
                        ],
                    )
                    page.overlay.append(dlg)
                    dlg.open = True
                    request_page_update(page)

                show_payment_dialog()
            except Exception as ex:
                add_log(f"Lỗi Tính Phí: {_friendly_error(ex)}", DANGER, ft.Icons.ERROR_ROUNDED)

        page.run_thread(do_calculate)

    def btn_cash(_):
        _manual_payment_flow("Tiền Mặt")

    def btn_confirm_payment(_):
        _manual_payment_flow("Thanh Toán")



    def btn_logout(_):
        lpr_engine.stop()
        try:
            api_client.logout()
        except Exception:
            pass
        api_client.clear_token()
        on_logout()

    def on_zone_change(event):
        raw_value = getattr(event, "data", None) or getattr(getattr(event, "control", None), "value", None) or zone_dropdown.value
        if not raw_value:
            return
        try:
            zone_id = int(raw_value)
        except (TypeError, ValueError):
            return
        zone_name = _zone_name(zone_id)
        zone_dropdown.value = str(zone_id)
        _state["selected_zone_id"] = zone_id
        _state["selected_zone_name"] = zone_name
        _state["zone_devices"] = []
        zone_status.value = f"Đang Tải Thiết Bị - {ui_title(zone_name)}..."
        zone_status.color = TEXT_SECONDARY
        request_page_update(page)
        page.run_thread(_load_zone_devices, zone_id, zone_name, True)

    # ── Action buttons ────────────────────────────────
    def _action_btn(label, icon, color, on_click, tooltip: str):
        return ft.ElevatedButton(
            content=label, icon=icon,
            tooltip=tooltip,
            style=filled_button_style(
                ft.Padding(left=0, right=0, top=12, bottom=12),
                ft.TextStyle(size=SIZE_SMALL, weight=W_MEDIUM, font_family=FONT_FAMILY),
            ),
            expand=True,
            on_click=on_click,
        )

    manual_plate_field.tooltip = "Nhập Hoặc Sửa Biển Số Trước Khi Gửi Thao Tác Thủ Công."
    zone_dropdown.tooltip = "Chọn Vùng Cổng Để Lấy Camera Và Barrier Theo Thiết Bị IoT."
    zone_status.tooltip = "Thiết Bị IoT Đang Gắn Với Vùng Cổng Đã Chọn."
    zone_dropdown.on_change = on_zone_change
    camera_stack.tooltip = "Khung Video Demo LPR Có Overlay Biển Số Và Bounding Box."
    log_box.tooltip = "Nhật Ký Các Sự Kiện Quét Biển Số, Mở Barrier, Và Thanh Toán."

    entry_btn    = _action_btn("Gửi Vào Tay",  ft.Icons.LOGIN_ROUNDED,         SUCCESS, btn_entry, "Gửi Biển Số Hiện Tại Vào Luồng Xe Vào.")
    exit_btn     = _action_btn("Gửi Ra Tay",    ft.Icons.LOGOUT_ROUNDED,        DANGER,  btn_exit, "Gửi Biển Số Hiện Tại Vào Luồng Xe Ra.")
    cash_btn     = _action_btn("Thu Tiền Mặt", ft.Icons.PAYMENTS_ROUNDED,       WARNING, btn_cash, "Thu Tiền Mặt Cho Phiên Xe Ra Đang Chọn.")
    payment_btn  = _action_btn("Xác Nhận Thanh Toán", ft.Icons.CHECK_CIRCLE_ROUNDED, INFO, btn_confirm_payment, "Nhập Số Tiền Khách Đã Thanh Toán Và Gửi Backend Xác Nhận.")

    play_entry_btn = _action_btn("Play Vào", ft.Icons.PLAY_CIRCLE_ROUNDED, SUCCESS, lambda _: start_demo_scan("ENTRY"), "Phát Video Demo Và Tự Động Xử Lý Xe Vào.")
    play_exit_btn = _action_btn("Play Ra", ft.Icons.PLAY_CIRCLE_ROUNDED, DANGER, lambda _: start_demo_scan("EXIT"), "Phát Video Demo Và Tự Động Xử Lý Xe Ra.")
    
    rescan_entry_btn = _action_btn("Quét Lại Vào", ft.Icons.REFRESH_ROUNDED, PRIMARY, lambda _: start_demo_scan("ENTRY", bypass_sensor=True), "Quét lại làn vào (Bỏ qua SENSOR).")
    rescan_exit_btn = _action_btn("Quét Lại Ra", ft.Icons.REFRESH_ROUNDED, WARNING, lambda _: start_demo_scan("EXIT", bypass_sensor=True), "Quét lại làn ra (Bỏ qua SENSOR).")
    
    manual_actions_row = ft.Row(visible=False, spacing=8, controls=[entry_btn, exit_btn])

    top_bar_controls = [
        ft.Icon(ft.Icons.VIDEOCAM_ROUNDED, color=PRIMARY, size=22),
        ft.Text("Video Demo LPR - Cổng Vào/Ra", font_family=FONT_FAMILY,
                size=SIZE_H3, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=True),
    ]
    if show_logout:
        top_bar_controls.append(
            ft.OutlinedButton(
                content="Đăng Xuất", icon=ft.Icons.LOGOUT_ROUNDED,
                tooltip="Đăng Xuất", on_click=btn_logout,
                style=outlined_button_style(ft.Padding(10, 8, 10, 8)),
            )
        )
    top_bar = ft.Row(visible=show_logout, controls=top_bar_controls)

    session_val_in = ft.Text("-", font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_PRIMARY, weight=W_MEDIUM)
    session_val_out = ft.Text("-", font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_PRIMARY, weight=W_MEDIUM)
    session_val_due = ft.Text("-", font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_PRIMARY, weight=W_MEDIUM)
    session_val_paid = ft.Text("-", font_family=FONT_FAMILY, size=SIZE_SMALL, color=SUCCESS, weight=W_MEDIUM)
    session_val_left = ft.Text("-", font_family=FONT_FAMILY, size=SIZE_BODY, weight=ft.FontWeight.BOLD, color=WARNING)
    session_val_img = ft.Image(src="", visible=False, fit=ft.BoxFit.CONTAIN, height=150, border_radius=4)

    def _info_row(icon, label, value_control):
        return ft.Row(
            spacing=12,
            controls=[
                ft.Row(spacing=8, controls=[
                    ft.Icon(icon, size=16, color=TEXT_SECONDARY),
                    ft.Text(label, font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY),
                ], expand=1),
                ft.Container(content=value_control, expand=2, alignment=ft.Alignment(1, 0))
            ]
        )

    session_info_panel = ft.Container(
        visible=False,
        bgcolor=BG_ELEVATED,
        border_radius=RADIUS_MD,
        border=border_all(1, BORDER),
        padding=0,
        content=ft.Column(
            spacing=0,
            controls=[
                ft.Container(
                    bgcolor=BG_CARD,
                    padding=ft.Padding(16, 12, 16, 12),
                    border=border_only(bottom=ft.BorderSide(1, BORDER)),
                    content=ft.Row(
                        controls=[
                            ft.Icon(ft.Icons.RECEIPT_LONG_ROUNDED, color=PRIMARY, size=20),
                            ft.Text("Chi Tiết Phiên", font_family=FONT_FAMILY, size=SIZE_BODY, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=True),
                        ]
                    )
                ),
                ft.Container(
                    padding=16,
                    content=ft.Column(
                        spacing=12,
                        controls=[
                            _info_row(ft.Icons.LOGIN_ROUNDED, "Giờ Vào:", session_val_in),
                            _info_row(ft.Icons.LOGOUT_ROUNDED, "Giờ Ra:", session_val_out),
                            ft.Divider(height=1, color=BORDER),
                            _info_row(ft.Icons.ACCOUNT_BALANCE_WALLET_ROUNDED, "Tổng Phí:", session_val_due),
                            _info_row(ft.Icons.PAYMENT_ROUNDED, "Đã Thu:", session_val_paid),
                            ft.Container(
                                bgcolor=BG_CARD,
                                border_radius=4,
                                border=border_all(1, WARNING),
                                padding=8,
                                content=ft.Row(
                                    controls=[
                                        ft.Text("Cần Thu:", font_family=FONT_FAMILY, size=SIZE_SMALL, weight=W_MEDIUM, color=WARNING, expand=True),
                                        session_val_left,
                                    ]
                                )
                            ),
                            session_val_img,
                        ]
                    )
                )
            ]
        )
    )

    _load_zone_selector()

    # ── Layout ────────────────────────────────────────
    return ft.Row(
        expand=True,
        spacing=0,
        vertical_alignment=ft.CrossAxisAlignment.START,
        controls=[
            # LEFT: camera feed
            ft.Container(
                expand=2,
                padding=PAD_LG,
                content=ft.Column(
                    expand=True,
                    spacing=PAD_MD,
                    controls=[
                        # Camera
                        camera_stack,
                        top_bar,
                        session_info_panel,
                    ],
                ),
            ),

            # RIGHT: controls panel
            ft.Container(
                width=360,
                bgcolor=BG_CARD,
                border=border_only(left=ft.BorderSide(1, BORDER)),
                padding=PAD_LG,
                content=ft.Column(
                    spacing=PAD_MD,
                    controls=[
                        ft.Container(
                            bgcolor=BG_ELEVATED,
                            border_radius=RADIUS_MD,
                            border=border_all(1, BORDER),
                            padding=PAD_MD,
                            content=ft.Column(
                                spacing=PAD_SM,
                                controls=[
                                    ft.Row(spacing=PAD_SM, controls=[
                                        ft.Icon(ft.Icons.LOCATION_ON_ROUNDED, size=18, color=PRIMARY),
                                        ft.Text("Vùng Cổng", font_family=FONT_FAMILY,
                                                size=SIZE_SMALL, weight=W_MEDIUM, color=TEXT_PRIMARY),
                                        ft.Container(expand=True),
                                        ft.OutlinedButton(
                                            content="",
                                            icon=ft.Icons.REFRESH_ROUNDED,
                                            tooltip="Làm Mới Vùng",
                                            on_click=lambda _: _load_zone_selector(),
                                            style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                                        ),
                                    ]),
                                    zone_dropdown,
                                    zone_status,
                                ],
                            ),
                        ),

                        # Plate display
                        ft.Container(
                            bgcolor=BG_ELEVATED,
                            border_radius=RADIUS_MD,
                            border=border_all(1, BORDER),
                            padding=PAD_LG,
                            content=ft.Column(
                                horizontal_alignment=ft.CrossAxisAlignment.CENTER,
                                spacing=8,
                                controls=[
                                    ft.Text("Biển Số Xe", font_family=FONT_FAMILY,
                                            size=SIZE_CAPTION, color=TEXT_SECONDARY),
                                    plate_label,
                                    plate_badge,
                                    plate_status,
                                ],
                            ),
                        ),

                        # Manual override input
                        ft.Text("Nhập Tay (Override AI)", font_family=FONT_FAMILY,
                                size=SIZE_CAPTION, color=TEXT_SECONDARY),
                        ft.Row(controls=[manual_plate_field]),

                        # Demo playback buttons
                        ft.Row(spacing=8, controls=[play_entry_btn, play_exit_btn]),
                        ft.Row(spacing=8, controls=[rescan_entry_btn, rescan_exit_btn]),

                        # Manual fallback buttons
                        manual_actions_row,
                        ft.Row(spacing=8, controls=[cash_btn, payment_btn]),


                        log_divider,

                        # Event log
                        log_header,
                        log_box,
                    ],
                ),
            ),
        ],
    )


def _fmt_vnd(amount) -> str:
    try:
        value = Decimal(str(amount))
        if value > 0:
            value = value.to_integral_value(rounding=ROUND_CEILING)
        return f"{int(value):,} ₫".replace(",", ".")
    except Exception:
        return str(amount)


