"""
sessions_page.py - GET /api/v1/admin/parking-sessions  (paginated, filterable)
"""
import threading
import time
import flet as ft
from core.design_tokens import *
from core import api_client, app_events
from core.settings import get_settings
from pages.admin.admin_ui import (
    bool_value, close_dialog, dropdown, float_value, int_value, show_dialog,
    message_row, show_snack, text_field, validate_required, validate_required_float,
    validate_required_int,
)


def build_sessions_page(page: ft.Page) -> ft.Control:
    search_field = ft.TextField(
        hint_text="Tìm Biển Số...",
        border_color=BORDER, focused_border_color=PRIMARY,
        hint_style=ft.TextStyle(color=TEXT_DISABLED, font_family=FONT_FAMILY),
        text_style=ft.TextStyle(color=TEXT_PRIMARY, font_family=FONT_FAMILY),
        bgcolor=BG_CARD, border_radius=RADIUS_MD, cursor_color=PRIMARY, expand=True,
        height=44,
        content_padding=ft.Padding(16, 0, 16, 0),
    )
    manual_filter = dropdown("Thủ Công", [("", "Tất Cả"), ("true", "Có"), ("false", "Không")], "", width=150)
    def _date_picker_field(label: str, is_from: bool = True, width=170):
        tf = ft.TextField(
            label=label, 
            width=width,
            read_only=True,
            border_color=BORDER, focused_border_color=PRIMARY,
            label_style=ft.TextStyle(color=TEXT_SECONDARY, font_family=FONT_FAMILY),
            text_style=ft.TextStyle(color=TEXT_PRIMARY, font_family=FONT_FAMILY),
            bgcolor=BG_ELEVATED, border_radius=RADIUS_MD,
            content_padding=ft.Padding(12, 0, 12, 0),
            height=40,
        )
        state = {"date": None}

        def on_time_change(e):
            if not e.control.value or not state["date"]:
                return
            t = e.control.value
            d = state["date"]
            dt_str = f"{d.strftime('%Y-%m-%d')}T{t.strftime('%H:%M:00')}"
            tf.value = dt_str
            page.update()

        tp = ft.TimePicker(
            on_change=on_time_change,
            help_text="Chọn giờ phút",
        )
        page.overlay.append(tp)

        def on_date_change(e):
            if not e.control.value:
                return
            d = e.control.value
            state["date"] = d
            # Set default time in case they cancel the time picker
            tf.value = d.strftime("%Y-%m-%dT00:00:00") if is_from else d.strftime("%Y-%m-%dT23:59:59")
            page.update()
            # Immediately open the time picker
            tp.open = True
            page.update()
        
        dp = ft.DatePicker(on_change=on_date_change)
        page.overlay.append(dp)
            
        def _on_clear(e):
            tf.value = ""
            state["date"] = None
            page.update()
            
        def _on_pick(e):
            dp.open = True
            page.update()
            
        tf.suffix = ft.Row(
            spacing=0, tight=True,
            controls=[
                ft.IconButton(ft.Icons.CLEAR_ROUNDED, on_click=_on_clear, icon_size=16, tooltip="Xóa", width=24, height=24),
                ft.IconButton(ft.Icons.CALENDAR_MONTH_ROUNDED, on_click=_on_pick, icon_size=16, tooltip="Chọn Ngày Giờ", width=24, height=24),
            ]
        )
        return tf

    entry_from_filter = _date_picker_field("Vào Từ", is_from=True, width=190)
    entry_to_filter = _date_picker_field("Vào Đến", is_from=False, width=190)
    exit_from_filter = _date_picker_field("Ra Từ", is_from=True, width=190)
    exit_to_filter = _date_picker_field("Ra Đến", is_from=False, width=190)
    booking_filter = text_field("ID Chi Tiết", number=True, width=150)
    type_filter = text_field("ID Loại Xe", number=True, width=140)
    zone_in_filter = text_field("ID Vùng Vào", number=True, width=140)
    zone_out_filter = text_field("ID Vùng Ra", number=True, width=140)
    paid_min_filter = text_field("Đã Thu Từ", number=True, width=150)
    paid_max_filter = text_field("Đã Thu Đến", number=True, width=150)

    loading = ft.ProgressRing(width=24, height=24, stroke_width=2, color=PRIMARY, visible=True)
    table_col = ft.Column(spacing=0, controls=[message_row("Đang Tải...")])
    page_info  = ft.Text("", font_family=FONT_FAMILY, size=SIZE_CAPTION, color=TEXT_SECONDARY)
    pagination_row = ft.Row(alignment=ft.MainAxisAlignment.CENTER, spacing=8, visible=False)

    state = {"page": 0, "total_pages": 0, "loading": False}

    HEADERS = ["Biển Số", "Loại Xe", "Vào", "Ra", "Phí (₫)", "Còn Lại (₫)", "Thủ Công", "Thao Tác"]
    EXPANDS = [2, 1, 2, 2, 1, 1, 1, 1]

    def _header_row():
        return ft.Container(
            bgcolor=BG_ELEVATED, border_radius=RADIUS_MD,
            padding=ft.Padding(PAD_MD, PAD_SM, PAD_MD, PAD_SM),
            content=ft.Row(controls=[
                ft.Text(h, font_family=FONT_FAMILY, size=SIZE_CAPTION,
                        weight=W_MEDIUM, color=TEXT_SECONDARY, expand=e)
                for h, e in zip(HEADERS, EXPANDS)
            ]),
        )

    def _data_row(s: dict) -> ft.Container:
        def _t(key): return (s.get(key) or "")[:16].replace("T", " ") or "-"
        def _m(key):
            try: return f"{float(s.get(key, 0)):,.0f}"
            except: return "0"
        flag = s.get("flag_manual", False)
        vehicle_type = ui_title(s.get("vehicle_type_name") or s.get("vehicle_name") or "-")
        return ft.Container(
            padding=ft.Padding(PAD_MD, PAD_SM + 2, PAD_MD, PAD_SM + 2),
            border=border_only(bottom=ft.BorderSide(1, BORDER)),
            content=ft.Row(controls=[
                ft.Text(s.get("vehicle_no", "-"), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=2),
                ft.Text(vehicle_type, font_family=FONT_FAMILY,
                        size=SIZE_SMALL, color=TEXT_SECONDARY, expand=1),
                ft.Text(_t("entry_time"), font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY, expand=2),
                ft.Text(_t("exit_time"),  font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY, expand=2),
                ft.Text(_m("amount_paid"), font_family=FONT_FAMILY, size=SIZE_SMALL, color=SUCCESS, expand=1),
                ft.Text(_m("amount_left"), font_family=FONT_FAMILY, size=SIZE_SMALL,
                        color=DANGER if float(s.get("amount_left", 0) or 0) > 0 else TEXT_DISABLED, expand=1),
                ft.Container(expand=1, content=ft.Icon(
                    ft.Icons.WARNING_AMBER_ROUNDED if flag else ft.Icons.CHECK_CIRCLE_ROUNDED,
                    size=16, color=WARNING if flag else SUCCESS
                )),
                ft.Row(expand=1, spacing=6, controls=[
                    ft.OutlinedButton(
                        content="",
                        icon=ft.Icons.VISIBILITY_ROUNDED,
                        tooltip="Xem Chi Tiết",
                        on_click=lambda _, row=s: open_session_detail(row),
                        style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                    ),
                    ft.OutlinedButton(
                        content="",
                        icon=ft.Icons.EDIT_ROUNDED,
                        tooltip="Sửa Phiên",
                        on_click=lambda _, row=s: open_session_dialog(row),
                        style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                    ),
                ]),
            ]),
        )

    def open_session_detail(session: dict):
        from core.supabase_client import SUPABASE_URL
        
        dlg = ft.AlertDialog(
            modal=True,
            title=ft.Text("Chi Tiết Phiên Đỗ Xe", font_family=FONT_FAMILY, color=TEXT_PRIMARY, size=SIZE_H3),
            bgcolor=BG_ELEVATED,
            shape=ft.RoundedRectangleBorder(radius=RADIUS_MD),
            content=ft.Column(tight=True, spacing=12, controls=[
                ft.ProgressRing(width=24, height=24, stroke_width=2, color=PRIMARY)
            ], width=700),
            actions=[
                ft.OutlinedButton("Đóng", on_click=lambda _: setattr(dlg, "open", False) or request_page_update(page), style=outlined_button_style())
            ],
            actions_alignment=ft.MainAxisAlignment.END,
        )
        page.overlay.append(dlg)
        dlg.open = True
        request_page_update(page)
        
        def fetch():
            try:
                resp = api_client.admin_get_parking_session(session["id"])
                detail = resp.get("data") or resp
                
                def _make_url(img):
                    if not img: return ""
                    if str(img).startswith("http"): return img
                    return f"{SUPABASE_URL}/storage/v1/object/public/parking-images/{img}"
                    
                in_img = detail.get("imageInUrl") or detail.get("image_in_url") or detail.get("checkinImageUrl")
                out_img = detail.get("imageOutUrl") or detail.get("image_out_url") or detail.get("checkoutImageUrl")
                in_url = _make_url(in_img)
                out_url = _make_url(out_img)
                
                print(f"DEBUG fetch in_img={in_img}, out_img={out_img}")
                
                def _t(key): return (detail.get(key) or session.get(key) or "")[:19].replace("T", " ") or "-"
                def _m(key):
                    val = detail.get(key)
                    if val is None: val = session.get(key, 0)
                    try: return f"{float(val):,.0f} ₫"
                    except: return "0 ₫"
                    
                vno = detail.get('vehicleNo') or detail.get('vehicle_no') or session.get('vehicle_no', '-')
                entry = _t('entryTime') if detail.get('entryTime') else _t('entry_time')
                exit_t = _t('exitTime') if detail.get('exitTime') else _t('exit_time')
                paid = _m('amountPaid') if detail.get('amountPaid') is not None else _m('amount_paid')
                left = _m('amountLeft') if detail.get('amountLeft') is not None else _m('amount_left')
                
                details = [
                    ft.Text(f"Biển số: {vno}", font_family=FONT_FAMILY, size=SIZE_H3, weight=W_MEDIUM),
                    ft.Text(f"Vào: {entry}  |  Ra: {exit_t}", font_family=FONT_FAMILY, size=SIZE_BODY, color=TEXT_SECONDARY),
                    ft.Text(f"Đã thu: {paid}  |  Còn lại: {left}", font_family=FONT_FAMILY, size=SIZE_BODY, color=TEXT_SECONDARY),
                    ft.Divider(height=1, color=BORDER),
                    ft.Row(
                        spacing=PAD_MD,
                        controls=[
                            ft.Column(expand=1, controls=[
                                ft.Text("Ảnh Lúc Vào", font_family=FONT_FAMILY, weight=W_MEDIUM),
                                ft.Image(src=in_url, fit=ft.BoxFit.CONTAIN, height=250, border_radius=RADIUS_MD) if in_url else ft.Container(content=ft.Text("Không có ảnh", color=TEXT_DISABLED), height=250, alignment=ft.Alignment(0, 0), bgcolor=BG_CARD)
                            ]),
                            ft.Column(expand=1, controls=[
                                ft.Text("Ảnh Lúc Ra", font_family=FONT_FAMILY, weight=W_MEDIUM),
                                ft.Image(src=out_url, fit=ft.BoxFit.CONTAIN, height=250, border_radius=RADIUS_MD) if out_url else ft.Container(content=ft.Text("Không có ảnh", color=TEXT_DISABLED), height=250, alignment=ft.Alignment(0, 0), bgcolor=BG_CARD)
                            ])
                        ]
                    )
                ]
                dlg.content.controls = details
            except Exception as e:
                dlg.content.controls = [ft.Text(f"Lỗi tải chi tiết: {e}", color=DANGER)]
            request_page_update(page)
            
        page.run_thread(fetch)
        request_page_update(page)

    def open_session_dialog(session: dict):
        vehicle_field = text_field("Biển Số Đúng", session.get("vehicle_no", ""))
        amount_field = text_field("Số Tiền Đã Thu", session.get("amount_paid", ""), number=True)
        type_field = text_field("ID Loại Xe", session.get("vehicle_type_id", ""), number=True)
        dialog_ref = {"dialog": None}

        def submit(_):
            if not validate_required(page, vehicle_field):
                return
            if not validate_required_float(page, amount_field):
                return
            if not validate_required_int(page, type_field):
                return

            def save():
                try:
                    api_client.admin_update_parking_session(
                        session["id"],
                        correct_vehicle_no=vehicle_field.value.strip(),
                        update_amount_paid=float_value(amount_field.value),
                        update_vehicle_type_id=int_value(type_field.value),
                    )
                    close_dialog(page, dialog_ref["dialog"])
                    load(state["page"])
                except Exception:
                    pass
            page.run_thread(save)

        dialog_ref["dialog"] = show_dialog(
            page,
            "Cập Nhật Phiên Đỗ Xe",
            [vehicle_field, amount_field, type_field],
            [
                ft.OutlinedButton(content="Hủy", on_click=lambda _: close_dialog(page, dialog_ref["dialog"]),
                                  style=outlined_button_style()),
                ft.ElevatedButton(content="Lưu", icon=ft.Icons.SAVE_ROUNDED, on_click=submit,
                                  style=filled_button_style()),
            ],
        )

    def load(pg: int = 0):
        if state["loading"]:
            return
        state["loading"] = True
        loading.visible = True
        refresh_btn.disabled = True
        request_page_update(page)

        def fetch():
            try:
                resp = api_client.admin_get_parking_sessions(
                    page=pg, size=20,
                    vehicle_no=search_field.value.strip() or None,
                    flag_manual=bool_value(manual_filter.value) if manual_filter.value else None,
                    entry_time_from=entry_from_filter.value.strip() or None,
                    entry_time_to=entry_to_filter.value.strip() or None,
                    exit_time_from=exit_from_filter.value.strip() or None,
                    exit_time_to=exit_to_filter.value.strip() or None,
                    booking_detail_id=int_value(booking_filter.value),
                    vehicle_type_id=int_value(type_filter.value),
                    zone_in_id=int_value(zone_in_filter.value),
                    zone_out_id=int_value(zone_out_filter.value),
                    paid_greater_than=float_value(paid_min_filter.value),
                    paid_less_than=float_value(paid_max_filter.value),
                )
                data     = resp.get("data", {})
                sessions = data.get("content", [])
                total    = data.get("totalElements", 0)
                t_pages  = data.get("totalPages", 1)
                state["page"] = pg
                state["total_pages"] = t_pages

                rows = [_header_row()]
                for s in sessions:
                    rows.append(_data_row(s))
                if not sessions:
                    rows.append(message_row())
                table_col.controls = rows
                page_info.value = f"Trang {pg + 1}/{t_pages} - {total} Bản Ghi"
                update_pagination()
            except Exception as e:
                table_col.controls = [message_row(f"Lỗi Tải Dữ Liệu: {e}", DANGER)]
            finally:
                loading.visible = False
                refresh_btn.disabled = False
                state["loading"] = False
                request_page_update(page)

        page.run_thread(fetch)

    search_field.on_submit = lambda _: load(0)

    def auto_refresh_loop():
        last_parking_version = app_events.parking_version()
        refresh_interval = max(5, int(get_settings().get("dashboard_refresh_interval_seconds", 60) or 60))
        next_timed_refresh = time.time() + refresh_interval
        while True:
            time.sleep(1)
            refresh_interval = max(5, int(get_settings().get("dashboard_refresh_interval_seconds", 60) or 60))
            current_version = app_events.parking_version()
            now = time.time()
            if current_version != last_parking_version:
                last_parking_version = current_version
                next_timed_refresh = now + refresh_interval
                load(state["page"])
            elif now >= next_timed_refresh:
                next_timed_refresh = now + refresh_interval
                load(state["page"])

    def update_pagination():
        pagination_row.controls = build_pagination_controls(state["page"], state["total_pages"], load)
        pagination_row.visible = bool(pagination_row.controls)

    refresh_btn = ft.OutlinedButton(
        content="Làm Mới", icon=ft.Icons.REFRESH_ROUNDED,
        on_click=lambda _: load(state["page"]),
        style=outlined_button_style(ft.Padding(10, 8, 10, 8)),
    )

    def apply_filters(_):
        filter_dialog.open = False
        request_page_update(page)
        load(0)

    filter_dialog = ft.AlertDialog(
        modal=True,
        title=ft.Text("Bộ Lọc Phiên Đỗ Xe", font_family=FONT_FAMILY, color=TEXT_PRIMARY, size=SIZE_H3, weight=W_MEDIUM),
        bgcolor=BG_CARD,
        shape=ft.RoundedRectangleBorder(radius=RADIUS_MD),
        content=ft.Column(
            tight=True,
            spacing=PAD_SM,
            controls=[
                ft.Row([entry_from_filter, entry_to_filter]),
                ft.Row([exit_from_filter, exit_to_filter]),
                ft.Row([zone_in_filter, zone_out_filter]),
                ft.Row([paid_min_filter, paid_max_filter]),
                ft.Row([booking_filter, type_filter]),
                manual_filter,
            ]
        ),
        actions=[
            ft.OutlinedButton("Đóng", on_click=lambda _: setattr(filter_dialog, "open", False) or request_page_update(page), style=outlined_button_style()),
            ft.ElevatedButton("Áp Dụng", icon=ft.Icons.FILTER_ALT_ROUNDED, on_click=apply_filters, style=filled_button_style()),
        ],
        actions_alignment=ft.MainAxisAlignment.END,
    )

    def open_filters(_):
        if filter_dialog not in page.overlay:
            page.overlay.append(filter_dialog)
        filter_dialog.open = True
        request_page_update(page)

    filter_btn = ft.OutlinedButton(
        content="Bộ Lọc",
        icon=ft.Icons.FILTER_LIST_ROUNDED,
        on_click=open_filters,
        style=outlined_button_style(ft.Padding(10, 8, 10, 8)),
    )

    load(0)
    page.run_thread(auto_refresh_loop)

    return ft.Column(
        expand=True,
        spacing=PAD_LG,
        controls=[
            ft.Text("Lịch Sử Phiên Đỗ Xe", font_family=FONT_FAMILY,
                    size=SIZE_H2, weight=W_MEDIUM, color=TEXT_PRIMARY),
            ft.Row(controls=[
                search_field,
                loading,
                filter_btn,
                refresh_btn,
                ft.ElevatedButton(
                    content="Tìm Kiếm",
                    icon=ft.Icons.SEARCH_ROUNDED,
                    on_click=lambda _: load(0),
                    style=filled_button_style(),
                ),
            ]),
            ft.Container(
                bgcolor=BG_CARD, border_radius=RADIUS_MD,
                border=border_all(1, BORDER), padding=PAD_LG,
                content=ft.Column(spacing=0, controls=[
                    table_col,
                    ft.Container(height=PAD_MD),
                    ft.Column(
                        horizontal_alignment=ft.CrossAxisAlignment.CENTER,
                        spacing=PAD_SM,
                        controls=[page_info, pagination_row],
                    ),
                ]),
            ),
        ],
    )

