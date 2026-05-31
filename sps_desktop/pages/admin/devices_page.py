"""
devices_page.py - GET /api/v1/admin/devices
"""
import threading
from datetime import datetime
import flet as ft
from core.design_tokens import *
from core import api_client
from pages.admin.admin_ui import (
    clean_body, close_dialog, confirm_dialog, dropdown, int_value,
    message_row, show_dialog, show_snack, text_field, validate_required,
    validate_required_int,
)

_STATUS_COLORS = {
    "ONLINE":      SUCCESS,
    "OFFLINE":     DANGER,
    "MAINTENANCE": WARNING,
}
_TYPE_ICONS = {
    "LPR_CAM": ft.Icons.VIDEOCAM_ROUNDED,
    "LPR_CAM_AI": ft.Icons.VIDEOCAM_ROUNDED,
    "LPR_CAM_SECURITY": ft.Icons.VIDEOCAM_ROUNDED,
    "BARRIER":  ft.Icons.SENSOR_DOOR_ROUNDED,
    "LED":      ft.Icons.LIGHT_MODE_ROUNDED,
    "SENSOR":   ft.Icons.SENSORS_ROUNDED,
}


def build_devices_page(page: ft.Page) -> ft.Control:
    def _now_iso():
        return datetime.now().replace(microsecond=0).isoformat()

    loading   = ft.ProgressRing(width=24, height=24, stroke_width=2, color=PRIMARY, visible=True)
    zones_col = ft.Column(spacing=0, controls=[message_row("Đang Tải...")])
    cards_row = ft.Row(spacing=PAD_MD, scroll=ft.ScrollMode.AUTO, controls=[message_row("Đang Tải...")])
    device_title = ft.Text("Thiết Bị Theo Vùng", font_family=FONT_FAMILY,
                           size=SIZE_H3, weight=W_MEDIUM, color=TEXT_PRIMARY)
    state = {"selected_zone_id": None, "selected_zone_name": ""}

    ZONE_HEADERS = ["ID", "Tên Vùng", "Loại", "Sức Chứa", "Đang Đỗ", "Thao Tác"]
    ZONE_EXPANDS = [1, 3, 2, 1, 1, 3]

    def _zone_header_row():
        return ft.Container(
            bgcolor=BG_ELEVATED,
            border_radius=RADIUS_MD,
            padding=ft.Padding(PAD_MD, PAD_SM, PAD_MD, PAD_SM),
            content=ft.Row(controls=[
                ft.Text(h, font_family=FONT_FAMILY, size=SIZE_CAPTION,
                        weight=W_MEDIUM, color=TEXT_SECONDARY, expand=e)
                for h, e in zip(ZONE_HEADERS, ZONE_EXPANDS)
            ]),
        )

    def _status_badge(status: str):
        color = _STATUS_COLORS.get(status, TEXT_DISABLED)
        label = ui_title(status)
        return ft.Container(
            bgcolor=WHITE, border_radius=RADIUS_MD,
            padding=ft.Padding(8, 3, 8, 3),
            content=ft.Row(spacing=4, tight=True, controls=[
                ft.Container(width=6, height=6, bgcolor=color, border_radius=RADIUS_MD),
                ft.Text(label, font_family=FONT_FAMILY, size=SIZE_CAPTION, color=color),
            ]),
        )

    def _device_card(d: dict) -> ft.Container:
        dtype  = d.get("device_type", "")
        status = d.get("status", "OFFLINE")
        color  = _STATUS_COLORS.get(status, TEXT_DISABLED)
        icon   = _TYPE_ICONS.get(dtype, ft.Icons.DEVICE_UNKNOWN_ROUNDED)
        last   = (d.get("last_ping") or "")[:16].replace("T", " ")
        zone_label = ui_title(d.get("zone_name_from") or d.get("zone_in_name") or d.get("zone_id_from") or "-")

        controls = [
            ft.Row(
                controls=[
                    ft.Container(
                        width=40, height=40, bgcolor=WHITE,
                        border_radius=RADIUS_MD, alignment=ft.Alignment(0, 0),
                        content=ft.Icon(icon, size=20, color=color),
                    ),
                    ft.Container(expand=True),
                    _status_badge(status),
                ],
            ),
            ft.Container(height=PAD_SM),
            ft.Text(ui_title(d.get("device_name", "-")), font_family=FONT_FAMILY,
                    size=SIZE_BODY, weight=W_MEDIUM, color=TEXT_PRIMARY),
            ft.Text(d.get("device_code", "-"), font_family=FONT_FAMILY,
                    size=SIZE_CAPTION, color=TEXT_SECONDARY),
            ft.Container(height=4),
            ft.Text(f"Vùng: {zone_label}", font_family=FONT_FAMILY,
                    size=SIZE_CAPTION, color=TEXT_DISABLED),
            ft.Text(f"IP: {d.get('ip_address', '-')}", font_family=FONT_FAMILY,
                    size=SIZE_CAPTION, color=TEXT_DISABLED),
            ft.Text(f"Ping: {last or '-'}", font_family=FONT_FAMILY,
                    size=SIZE_CAPTION, color=TEXT_DISABLED),
        ]

        if not d.get("_readonly", False):
            controls.extend([
                ft.Container(height=PAD_SM),
                ft.Row(spacing=6, controls=[
                    ft.OutlinedButton(
                        content="",
                        icon=ft.Icons.EDIT_ROUNDED,
                        tooltip="Sửa Thiết Bị",
                        on_click=lambda _, row=d: open_device_dialog(row),
                        style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                    ),
                    ft.OutlinedButton(
                        content="",
                        icon=ft.Icons.DELETE_ROUNDED,
                        tooltip="Xóa Thiết Bị",
                        on_click=lambda _, row=d: delete_device(row),
                        style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                    ),
                ]),
            ])

        return ft.Container(
            width=240,
            bgcolor=BG_CARD,
            border_radius=RADIUS_MD,
            border=border_all(1, color),
            padding=PAD_LG,
            content=ft.Column(spacing=4, controls=controls),
        )

    def _zone_row(z: dict) -> ft.Container:
        zone_id = z.get("id")
        is_selected = zone_id == state["selected_zone_id"]
        return ft.Container(
            bgcolor=BG_ELEVATED if is_selected else BG_CARD,
            padding=ft.Padding(PAD_MD, PAD_SM + 2, PAD_MD, PAD_SM + 2),
            border=border_only(bottom=ft.BorderSide(1, BORDER)),
            on_click=lambda _, row=z: select_zone(row),
            ink=True,
            content=ft.Row(controls=[
                ft.Text(str(z.get("id", "")), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, color=TEXT_DISABLED, expand=1),
                ft.Text(ui_title(z.get("zone_name", "-")), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=3),
                ft.Text(ui_title(z.get("zone_type", "-")), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, color=TEXT_SECONDARY, expand=2),
                ft.Text(str(z.get("capacity", "-")), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, color=TEXT_SECONDARY, expand=1),
                ft.Text(str(z.get("current_occupancy", "-")), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, color=TEXT_SECONDARY, expand=1),
                ft.Row(expand=3, spacing=6, controls=[
                    ft.OutlinedButton(
                        content="",
                        icon=ft.Icons.VISIBILITY_ROUNDED,
                        tooltip="Xem Thiết Bị",
                        on_click=lambda _, row=z: select_zone(row),
                        style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                    ),
                    ft.OutlinedButton(
                        content="",
                        icon=ft.Icons.EDIT_ROUNDED,
                        tooltip="Sửa Vùng",
                        on_click=lambda _, row=z: open_zone_dialog(row),
                        style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                    ),
                    ft.OutlinedButton(
                        content="",
                        icon=ft.Icons.DELETE_ROUNDED,
                        tooltip="Xóa Vùng",
                        on_click=lambda _, row=z: delete_zone(row),
                        style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                    ),
                ]),
            ]),
        )

    def open_zone_dialog(zone: dict | None = None):
        zone = zone or {}
        zone_id = zone.get("id")
        parent_field = text_field("ID Vùng Cha", zone.get("parent_zone_id", ""), number=True)
        name_field = text_field("Tên Vùng", zone.get("zone_name", ""))
        type_field = dropdown(
            "Loại Vùng",
            [("BUILDING", "Building"), ("FLOOR", "Floor"), ("BLOCK", "Block"),
             ("AREA", "Area"), ("GATE", "Gate"), ("SLOT", "Slot")],
            zone.get("zone_type") or "AREA",
            width=220,
        )
        capacity_field = text_field("Sức Chứa", zone.get("capacity", "0"), number=True)
        occupancy_field = text_field("Đang Đỗ", zone.get("current_occupancy", "0"), number=True)
        dialog_ref = {"dialog": None}

        def submit(_):
            if not validate_required(page, name_field, type_field):
                return
            if not validate_required_int(page, capacity_field, occupancy_field):
                return
            
            if parent_field.value.strip() and int_value(parent_field.value) is None:
                parent_field.error_text = "Phải Là Số Nguyên"
                request_page_update(page)
                return
            else:
                parent_field.error_text = None

            def save():
                try:
                    body = clean_body({
                        "parent_zone_id": int_value(parent_field.value),
                        "zone_name": name_field.value.strip(),
                        "zone_type": type_field.value,
                        "capacity": int_value(capacity_field.value, 0),
                        "current_occupancy": int_value(occupancy_field.value, 0),
                    })
                    if zone_id:
                        api_client.admin_update_zone(zone_id, body)
                        message = "Đã Cập Nhật Vùng."
                    else:
                        api_client.admin_create_zone(
                            body.get("parent_zone_id"),
                            body.get("zone_name"),
                            body.get("zone_type"),
                            body.get("capacity", 0),
                            body.get("current_occupancy", 0),
                        )
                    close_dialog(page, dialog_ref["dialog"])
                    load()
                except Exception:
                    pass
            page.run_thread(save)

        dialog_ref["dialog"] = show_dialog(
            page,
            "Sửa Vùng" if zone_id else "Thêm Vùng",
            [parent_field, name_field, type_field, capacity_field, occupancy_field],
            [
                ft.OutlinedButton(content="Hủy", on_click=lambda _: close_dialog(page, dialog_ref["dialog"]),
                                  style=outlined_button_style()),
                ft.ElevatedButton(content="Lưu", icon=ft.Icons.SAVE_ROUNDED, on_click=submit,
                                  style=filled_button_style()),
            ],
        )

    def delete_zone(zone: dict):
        def do_delete():
            def run():
                try:
                    api_client.admin_delete_zone(zone["id"])
                    if state["selected_zone_id"] == zone.get("id"):
                        state["selected_zone_id"] = None
                        state["selected_zone_name"] = ""
                    load()
                except Exception:
                    pass
            page.run_thread(run)

        confirm_dialog(page, "Xóa Vùng", "Xác Nhận Xóa Vùng Này?", do_delete)

    def open_device_dialog(device: dict | None = None):
        device = device or {}
        device_id = device.get("id")
        code_field = text_field("Mã Thiết Bị", device.get("device_code", ""))
        name_field = text_field("Tên Thiết Bị", device.get("device_name", ""))
        ip_field = text_field("IP", device.get("ip_address", ""))
        last_ping_field = text_field("Lần Ping Cuối", device.get("last_ping") or device.get("lastPing") or _now_iso())
        type_field = dropdown(
            "Loại Thiết Bị",
            [("LPR_CAM_AI", "LPR Cam AI"), ("LPR_CAM_SECURITY", "LPR Cam Security"),
             ("BARRIER", "Barrier"), ("LED", "LED"), ("SENSOR", "Sensor")],
            device.get("device_type") or "LPR_CAM_AI",
            width=260,
        )
        direction_field = dropdown(
            "Hướng",
            [("IN", "In"), ("OUT", "Out"), ("BOTH", "Both")],
            device.get("direction") or "IN",
            width=180,
        )
        status_field = dropdown(
            "Trạng Thái",
            [("ONLINE", "Online"), ("OFFLINE", "Offline"), ("MAINTENANCE", "Maintenance")],
            device.get("status") or "OFFLINE",
            width=220,
        )
        dialog_ref = {"dialog": None}

        def _open():
            try:
                zones_resp = api_client.admin_get_zones()
                zones = zones_resp.get("data", zones_resp)
                zone_list = zones if isinstance(zones, list) else []
            except Exception as ex:
                zone_list = []
                print(f"DEBUG: Failed to fetch zones: {ex}")

            zone_options = [("", "-- Không Có --")] + [(str(z["id"]), ui_title(z.get("zone_name", f"Zone {z['id']}"))) for z in zone_list]
            
            val_from = str(device.get("zone_id_from", "")) if device.get("zone_id_from") else str(state["selected_zone_id"] or "")
            if val_from not in [o[0] for o in zone_options]: val_from = ""
            val_to = str(device.get("zone_id_to", "")) if device.get("zone_id_to") else str(state["selected_zone_id"] or "")
            if val_to not in [o[0] for o in zone_options]: val_to = ""

            zone_from_field = dropdown("Vùng Từ", zone_options, val_from, width=220)
            zone_to_field = dropdown("Vùng Đến", zone_options, val_to, width=220)

            def submit(_):
                if not validate_required(page, code_field, name_field, ip_field, last_ping_field, type_field, direction_field, status_field):
                    return

                def save():
                    try:
                        body = clean_body({
                            "device_code": code_field.value.strip(),
                            "device_name": name_field.value.strip(),
                            "ip_address": ip_field.value.strip(),
                            "zone_id_from": int_value(zone_from_field.value),
                            "zone_id_to": int_value(zone_to_field.value),
                            "device_type": type_field.value,
                            "direction": direction_field.value,
                            "status": status_field.value,
                            "last_ping": last_ping_field.value.strip(),
                        })
                        if device_id:
                            api_client.admin_update_device(device_id, body)
                        else:
                            api_client.admin_create_device(body)
                        close_dialog(page, dialog_ref["dialog"])
                        if tab_state["current"] == "zones":
                            load(state["selected_zone_id"])
                        else:
                            load_all_devices()
                    except Exception:
                        pass
                page.run_thread(save)

            dialog_ref["dialog"] = show_dialog(
                page,
                "Sửa Thiết Bị" if device_id else "Thêm Thiết Bị",
                [code_field, name_field, ip_field, zone_from_field, zone_to_field, type_field, direction_field, status_field, last_ping_field],
                [
                    ft.OutlinedButton(content="Hủy", on_click=lambda _: close_dialog(page, dialog_ref["dialog"]),
                                      style=outlined_button_style()),
                    ft.ElevatedButton(content="Lưu", icon=ft.Icons.SAVE_ROUNDED, on_click=submit,
                                      style=filled_button_style()),
                ],
                width=520,
            )
            
        page.run_thread(_open)

    def delete_device(device: dict):
        def do_delete():
            def run():
                try:
                    api_client.admin_delete_device(device["id"])
                    load()
                except Exception:
                    pass
            page.run_thread(run)

        confirm_dialog(page, "Xóa Thiết Bị", "Xác Nhận Xóa Thiết Bị Này?", do_delete)

    def select_zone(zone: dict):
        load(zone.get("id"), zone.get("zone_name") or zone.get("zoneName") or "")

    def load(selected_zone_id=None, selected_zone_name: str = ""):
        if selected_zone_id is not None:
            state["selected_zone_id"] = selected_zone_id
            state["selected_zone_name"] = selected_zone_name
        loading.visible = True
        request_page_update(page)

        def fetch():
            try:
                zones_resp = api_client.admin_get_zones()
                zones = zones_resp.get("data", zones_resp)
                zone_list = zones if isinstance(zones, list) else []
                zone_ids = {z.get("id") for z in zone_list}
                if state["selected_zone_id"] not in zone_ids:
                    default_zone = next(
                        (z for z in zone_list if str(z.get("zone_type") or z.get("zoneType") or "").upper() == "GATE"),
                        zone_list[0] if zone_list else None,
                    )
                    state["selected_zone_id"] = default_zone.get("id") if default_zone else None
                    state["selected_zone_name"] = (
                        default_zone.get("zone_name") or default_zone.get("zoneName") or ""
                    ) if default_zone else ""
                elif not state["selected_zone_name"]:
                    selected_zone = next((z for z in zone_list if z.get("id") == state["selected_zone_id"]), {})
                    state["selected_zone_name"] = selected_zone.get("zone_name") or selected_zone.get("zoneName") or ""

                zones_col.controls = [_zone_header_row()]
                for z in zone_list:
                    zones_col.controls.append(_zone_row(z))
                if not zone_list:
                    zones_col.controls.append(
                        message_row()
                    )

                selected_label = ui_title(state["selected_zone_name"] or f"Vùng #{state['selected_zone_id']}")
                device_title.value = f"Thiết Bị - {selected_label}" if state["selected_zone_id"] else "Thiết Bị Theo Vùng"
                if state["selected_zone_id"]:
                    resp = api_client.admin_get_devices_by_zone(state["selected_zone_id"])
                    devices = resp.get("data", [])
                else:
                    devices = []
                device_cards = []
                for d in devices:
                    # Inject a readonly flag so Sửa/Xóa buttons hide in Zone view
                    d["_readonly"] = True
                    device_cards.append(_device_card(d))
                if not devices:
                    device_cards.append(message_row("Không có thiết bị nào trong vùng này."))
                cards_row.controls = device_cards
            except Exception as e:
                zones_col.controls = zones_col.controls or [message_row()]
                cards_row.controls = [message_row(f"Lỗi Tải Thiết Bị: {e}", DANGER)]
            loading.visible = False
            request_page_update(page)

        page.run_thread(fetch)

    def load_all_devices():
        loading.visible = True
        request_page_update(page)
        def fetch_devices():
            try:
                resp = api_client.admin_get_devices()
                devices = resp.get("data", resp) if isinstance(resp, dict) else resp
                device_list = devices if isinstance(devices, list) else []
                
                cards = []
                for d in device_list:
                    cards.append(_device_card(d))
                if not device_list:
                    cards.append(message_row("Không có thiết bị nào."))
                all_devices_row.controls = cards
            except Exception as e:
                all_devices_row.controls = [message_row(f"Lỗi Tải Thiết Bị: {e}", DANGER)]
            loading.visible = False
            request_page_update(page)
        page.run_thread(fetch_devices)

    all_devices_row = ft.Row(spacing=PAD_MD, scroll=ft.ScrollMode.AUTO, wrap=True, controls=[message_row("Đang Tải...")])

    tab_state = {"current": "zones"}
    
    zone_tab_content = ft.Column(
        spacing=PAD_LG,
        expand=True,
        controls=[
            ft.Row(
                controls=[
                    ft.Text("Quản Lý Vùng Đỗ Xe", font_family=FONT_FAMILY,
                            size=SIZE_H3, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=True),
                    ft.ElevatedButton(
                        content="Thêm Vùng",
                        icon=ft.Icons.ADD_LOCATION_ALT_ROUNDED,
                        on_click=lambda _: open_zone_dialog(),
                        style=filled_button_style(),
                    ),
                ],
            ),
            ft.Container(
                bgcolor=BG_CARD, border_radius=RADIUS_MD,
                border=border_all(1, BORDER), padding=PAD_LG,
                content=zones_col,
            ),
            device_title,
            cards_row,
        ]
    )
    
    device_tab_content = ft.Column(
        spacing=PAD_LG,
        expand=True,
        controls=[
            ft.Row(
                controls=[
                    ft.Text("Tất Cả Thiết Bị", font_family=FONT_FAMILY,
                            size=SIZE_H3, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=True),
                    ft.ElevatedButton(
                        content="Thêm Thiết Bị",
                        icon=ft.Icons.ADD_ROUNDED,
                        on_click=lambda _: open_device_dialog(),
                        style=filled_button_style(),
                    ),
                ],
            ),
            all_devices_row,
        ]
    )

    content_area = ft.Container(content=zone_tab_content, expand=True)
    
    btn_zones = ft.ElevatedButton("Vùng Đỗ Xe", icon=ft.Icons.LOCATION_ON_ROUNDED, style=filled_button_style())
    btn_devices = ft.OutlinedButton("Thiết Bị IoT", icon=ft.Icons.DEVICES_ROUNDED, style=outlined_button_style())

    def switch_tab(tab_name):
        tab_state["current"] = tab_name
        if tab_name == "zones":
            btn_zones.style = filled_button_style()
            btn_devices.style = outlined_button_style()
            content_area.content = zone_tab_content
            load()
        else:
            btn_zones.style = outlined_button_style()
            btn_devices.style = filled_button_style()
            content_area.content = device_tab_content
            load_all_devices()
        request_page_update(page)

    btn_zones.on_click = lambda _: switch_tab("zones")
    btn_devices.on_click = lambda _: switch_tab("devices")

    load()

    return ft.Column(
        expand=True,
        spacing=PAD_LG,
        controls=[
            ft.Row(
                controls=[
                    ft.Text("Thiết Bị IoT & Vùng", font_family=FONT_FAMILY,
                            size=SIZE_H2, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=True),
                    loading,
                    ft.OutlinedButton(
                        content="Làm Mới", icon=ft.Icons.REFRESH_ROUNDED,
                        on_click=lambda _: load() if tab_state["current"] == "zones" else load_all_devices(),
                        style=outlined_button_style(),
                    ),
                ],
            ),
            ft.Row(
                spacing=PAD_MD,
                controls=[btn_zones, btn_devices]
            ),
            content_area,
        ],
    )

