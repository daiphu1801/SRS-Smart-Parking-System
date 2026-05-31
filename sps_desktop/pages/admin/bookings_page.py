"""
bookings_page.py - GET /api/v1/employee/bookings + /api/v1/employee/bookings/{id}/details
"""
import threading
from datetime import datetime
import flet as ft
from core.design_tokens import *
from core import api_client
from pages.admin.admin_ui import (
    clean_body, close_dialog, confirm_dialog, dropdown, int_value, show_dialog,
    list_of, message_row, show_snack, text_field, validate_required, validate_required_int,
)

_STATUS_COLORS = {
    "ACTIVE": SUCCESS, "EXPIRED": DANGER, "PENDING_PAYMENT": WARNING,
    "CANCELED": TEXT_DISABLED, "PENDING_ACTIVATION": INFO,
}


def build_bookings_page(page: ft.Page) -> ft.Control:
    def _now_iso():
        return datetime.now().replace(microsecond=0).isoformat()

    search_field = ft.TextField(
        hint_text="Tìm Hợp Đồng...",
        border_color=BORDER, focused_border_color=PRIMARY,
        hint_style=ft.TextStyle(color=TEXT_DISABLED, font_family=FONT_FAMILY),
        text_style=ft.TextStyle(color=TEXT_PRIMARY, font_family=FONT_FAMILY),
        bgcolor=BG_CARD, border_radius=RADIUS_MD, cursor_color=PRIMARY, expand=True,
        height=44,
        content_padding=ft.Padding(16, 0, 16, 0),
    )
    group_filter = text_field("ID Nhóm", number=True, width=150)
    package_filter = text_field("ID Gói", number=True, width=150)
    loading   = ft.ProgressRing(width=24, height=24, stroke_width=2, color=PRIMARY, visible=True)
    table_col = ft.Column(spacing=0, controls=[message_row("Đang Tải...")])
    page_info  = ft.Text("", font_family=FONT_FAMILY, size=SIZE_CAPTION, color=TEXT_SECONDARY)
    pagination_row = ft.Row(alignment=ft.MainAxisAlignment.CENTER, spacing=8, visible=False)
    state = {"page": 0, "total_pages": 0, "selected_booking": None}

    HEADERS = ["ID", "Nhóm Khách Hàng", "Gói Cước", "Ngày Tạo", "Thao Tác"]
    EXPANDS = [1, 3, 2, 2, 2]

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

    detail_col = ft.Column(spacing=0, visible=False, controls=[message_row()])
    detail_hdr = ft.Text("", font_family=FONT_FAMILY, size=SIZE_H3, weight=W_MEDIUM, color=TEXT_PRIMARY)
    detail_panel = ft.Container(
        bgcolor=BG_CARD, border_radius=RADIUS_MD,
        border=border_all(1, PRIMARY), padding=PAD_LG,
        content=detail_col,
        visible=False,
    )

    DHEADERS = ["Biển Số", "Gói Giá", "Khách Hàng", "Bắt Đầu", "Kết Thúc", "Trạng Thái", "Thao Tác"]
    DEXPANDS = [2, 1, 2, 2, 2, 1, 2]

    def _detail_header_row():
        return ft.Container(
            bgcolor=BG_ELEVATED, border_radius=RADIUS_MD,
            padding=ft.Padding(PAD_MD, PAD_SM, PAD_MD, PAD_SM),
            content=ft.Row(controls=[
                ft.Text(h, font_family=FONT_FAMILY, size=SIZE_CAPTION,
                        weight=W_MEDIUM, color=TEXT_SECONDARY, expand=e)
                for h, e in zip(DHEADERS, DEXPANDS)
            ]),
        )

    def _status_badge(status: str) -> ft.Container:
        color = _STATUS_COLORS.get(status, TEXT_DISABLED)
        label = ui_title(status)
        return ft.Container(
            bgcolor=WHITE, border_radius=RADIUS_MD,
            padding=ft.Padding(8, 3, 8, 3),
            content=ft.Text(label, font_family=FONT_FAMILY, size=SIZE_CAPTION, color=color),
        )

    def load_details(booking_id: int, booking_label: str):
        state["selected_booking"] = booking_id
        detail_hdr.value = f"Chi Tiết Hợp Đồng #{booking_id} - {ui_title(booking_label)}"
        detail_shell = [
            ft.Row(controls=[
                detail_hdr,
                ft.Container(expand=True),
                ft.ElevatedButton(
                    content="Thêm Xe",
                    icon=ft.Icons.ADD_ROUNDED,
                    on_click=lambda _: open_booking_detail_dialog({"booking_id": booking_id}),
                    style=filled_button_style(),
                ),
            ]),
            ft.Divider(color=BORDER, height=1),
            _detail_header_row(),
        ]
        detail_col.controls = [*detail_shell, message_row("Đang Tải...")]
        detail_col.visible = True
        detail_panel.visible = True
        request_page_update(page)

        def fetch():
            try:
                resp = api_client.admin_get_booking_and_details(booking_id)
                details = resp.get("data", {}).get("details", [])
                detail_rows = []
                for d in details:
                    s_date = (d.get("start_date") or "")[:10]
                    e_date = (d.get("end_date")   or "")[:10]
                    customer_name = ui_title(d.get("customer_name") or d.get("full_name") or "-")
                    package_name = ui_title(d.get("package_price_name") or d.get("vehicle_type_name") or "-")
                    detail_rows.append(ft.Container(
                        padding=ft.Padding(PAD_MD, PAD_SM + 2, PAD_MD, PAD_SM + 2),
                        border=border_only(bottom=ft.BorderSide(1, BORDER)),
                        content=ft.Row(controls=[
                            ft.Text(d.get("vehicle_no", "-"), font_family=FONT_FAMILY,
                                    size=SIZE_SMALL, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=2),
                            ft.Text(package_name, font_family=FONT_FAMILY,
                                    size=SIZE_SMALL, color=TEXT_SECONDARY, expand=1),
                            ft.Text(customer_name, font_family=FONT_FAMILY,
                                    size=SIZE_SMALL, color=TEXT_SECONDARY, expand=2),
                            ft.Text(s_date, font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY, expand=2),
                            ft.Text(e_date, font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY, expand=2),
                            ft.Container(expand=1, content=_status_badge(d.get("status", ""))),
                            ft.Row(expand=2, spacing=6, controls=[
                                ft.OutlinedButton(
                                    content="",
                                    icon=ft.Icons.EDIT_ROUNDED,
                                    tooltip="Sửa Xe",
                                    on_click=lambda _, row=d: open_booking_detail_dialog(row),
                                    style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                                ),
                                ft.OutlinedButton(
                                    content="",
                                    icon=ft.Icons.DELETE_ROUNDED,
                                    tooltip="Xóa Xe",
                                    on_click=lambda _, row=d: delete_booking_detail(row),
                                    style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                                ),
                            ]),
                        ]),
                    ))
                if not details:
                    detail_rows.append(message_row())
                detail_col.controls = [*detail_shell, *detail_rows]
            except Exception as e:
                detail_col.controls = [*detail_shell, message_row(f"Lỗi Tải Chi Tiết: {e}", DANGER)]
            request_page_update(page)

        page.run_thread(fetch)

    def _booking_row(b: dict) -> ft.Container:
        created = (b.get("created_at") or "")[:10]
        return ft.Container(
            padding=ft.Padding(PAD_MD, PAD_SM + 2, PAD_MD, PAD_SM + 2),
            border=border_only(bottom=ft.BorderSide(1, BORDER)),
            on_click=lambda _, bid=b["id"], lbl=b.get("group_name", ""): page.run_thread(load_details, bid, lbl),
            ink=True,
            content=ft.Row(controls=[
                ft.Text(str(b.get("id", "")), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, color=TEXT_DISABLED, expand=1),
                ft.Text(ui_title(b.get("group_name", "-")), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=3),
                ft.Text(ui_title(b.get("package_name", "-")), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, color=TEXT_SECONDARY, expand=2),
                ft.Text(created, font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY, expand=2),
                ft.Row(expand=2, spacing=6, controls=[
                    ft.OutlinedButton(
                        content="",
                        icon=ft.Icons.VISIBILITY_ROUNDED,
                        tooltip="Xem Chi Tiết",
                        on_click=lambda _, bid=b["id"], lbl=b.get("group_name", ""): page.run_thread(load_details, bid, lbl),
                        style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                    ),
                    ft.OutlinedButton(
                        content="",
                        icon=ft.Icons.EDIT_ROUNDED,
                        tooltip="Sửa Hợp Đồng",
                        on_click=lambda _, row=b: open_booking_dialog(row),
                        style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                    ),
                    ft.OutlinedButton(
                        content="",
                        icon=ft.Icons.DELETE_ROUNDED,
                        tooltip="Xóa Hợp Đồng",
                        on_click=lambda _, row=b: delete_booking(row),
                        style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                    ),
                ]),
            ]),
        )

    def open_booking_dialog(booking: dict | None = None):
        booking = booking or {}
        
        group_profiles = {}
        group_options = []
        try:
            groups = list_of(api_client.admin_get_customer_groups(page=0, size=1000))
            for g in groups:
                gid = str(g["id"])
                group_options.append((gid, f"{g.get('group_name', 'Không Tên')} (ID: {gid})"))
                group_profiles[gid] = g.get("profile_id")
        except Exception:
            pass
        group_field = dropdown("Chọn Nhóm Khách Hàng", group_options, str(booking.get("group_id") or ""), width=300)
        package_field = dropdown("Chọn Gói Cước", [], str(booking.get("package_id") or ""), width=300)
        
        def update_packages(e=None):
            package_field.options = []
            selected_group = group_field.value
            print(f"DEBUG: update_packages called. selected_group={selected_group}")
            if selected_group and selected_group in group_profiles:
                pid = group_profiles[selected_group]
                print(f"DEBUG: pid for selected_group={pid}")
                if pid:
                    try:
                        pkgs = list_of(api_client.admin_get_packages(search="", page=0, size=1000, group_profile_id=pid))
                        print(f"DEBUG: Fetched {len(pkgs)} packages")
                        for p in pkgs:
                            code = p.get('package_code', '')
                            name = p.get('package_name', 'Không Tên')
                            display_text = f"[{code}] {name}" if code else name
                            package_field.options.append(ft.dropdown.Option(key=str(p["id"]), text=display_text))
                        if e:
                            show_snack(page, f"Đã tải {len(pkgs)} gói cước cho profile {pid}", SUCCESS)
                    except Exception as ex:
                        print(f"DEBUG: Exception fetching packages: {ex}")
                        if e:
                            show_snack(page, f"Lỗi tải Gói Cước: {ex}", DANGER)
                else:
                    print("DEBUG: pid is None!")
                    if e:
                        show_snack(page, "Nhóm này chưa có Profile, không có Gói Cước", WARNING)
            else:
                print("DEBUG: selected_group not in group_profiles")
                if e:
                    show_snack(page, "Vui lòng chọn Nhóm Khách Hàng hợp lệ", WARNING)
                    
            if package_field.value not in [opt.key for opt in package_field.options]:
                package_field.value = None
            if e and dialog_ref["dialog"]:
                package_field.update()
        if hasattr(group_field, 'on_select'):
            group_field.on_select = update_packages
        if hasattr(group_field, 'on_change'):
            group_field.on_change = update_packages
        update_packages() # Initial load
        
        dialog_ref = {"dialog": None}
        is_update = bool(booking.get("id"))

        def submit(_):
            if not validate_required_int(page, group_field, package_field):
                return

            def save():
                try:
                    body = {
                        "group_id": int_value(group_field.value),
                        "package_id": int_value(package_field.value),
                    }
                    if is_update:
                        api_client.admin_update_booking(booking["id"], body)
                    else:
                        api_client.admin_create_booking(body["group_id"], body["package_id"], None)
                    close_dialog(page, dialog_ref["dialog"])
                    load(state["page"] if is_update else 0)
                except Exception:
                    pass
            page.run_thread(save)

        dialog_ref["dialog"] = show_dialog(
            page,
            "Sửa Hợp Đồng" if is_update else "Thêm Hợp Đồng",
            [group_field, package_field],
            [
                ft.OutlinedButton(content="Hủy", on_click=lambda _: close_dialog(page, dialog_ref["dialog"]),
                                  style=outlined_button_style()),
                ft.ElevatedButton(content="Lưu", icon=ft.Icons.SAVE_ROUNDED, on_click=submit,
                                  style=filled_button_style()),
            ],
        )

    def delete_booking(booking: dict):
        def do_delete(d):
            def run():
                try:
                    api_client.admin_delete_booking(booking["id"])
                    close_dialog(page, d)
                    load(state["page"])
                    detail_panel.visible = False
                    detail_col.visible = False
                except Exception:
                    pass
            page.run_thread(run)

        confirm_dialog(page, "Xóa Hợp Đồng", "Xác Nhận Xóa Hợp Đồng Này?", do_delete)

    def open_booking_detail_dialog(detail: dict | None = None):
        detail = detail or {}
        detail_id = detail.get("id")
        cus_options = []
        try:
            customers = list_of(api_client.admin_get_customers(page=0, size=500))
            for c in customers:
                cus_options.append((str(c["id"]), f"{c.get('full_name', 'Không Tên')} - {c.get('phone', '')} (ID: {c['id']})"))
        except Exception:
            pass

        type_options = []
        try:
            vtypes = list_of(api_client.admin_get_vehicle_types(page=0, size=100))
            for vt in vtypes:
                type_options.append((str(vt["id"]), f"{vt.get('type_name', 'Không Tên')} (ID: {vt['id']})"))
        except Exception:
            pass
            
        booking_field = text_field("ID Hợp Đồng", detail.get("booking_id") or detail.get("bookingId") or state["selected_booking"] or "", number=True)
        customer_field = dropdown("Chọn Khách Hàng", cus_options, str(detail.get("customer_id") or detail.get("customerId") or ""), width=260)
        price_field = text_field("ID Gói Giá", detail.get("package_price_id") or detail.get("packagePriceId") or "", number=True)
        price_name_field = text_field("Tên Gói Giá", detail.get("package_price_name") or detail.get("packagePriceName") or "")
        vehicle_field = text_field("Biển Số", detail.get("vehicle_no") or detail.get("vehicleNo") or "")
        start_field = text_field("Ngày Bắt Đầu", detail.get("start_date") or detail.get("startDate") or "")
        end_field = text_field("Ngày Kết Thúc", detail.get("end_date") or detail.get("endDate") or "")
        status_field = dropdown("Trạng Thái", [
            ("ACTIVE", "Active"), ("EXPIRED", "Expired"), ("PENDING_PAYMENT", "Pending Payment"),
            ("CANCELED", "Canceled"), ("PENDING_ACTIVATION", "Pending Activation"),
            ("NEEDS_ATTENTION", "Needs Attention"), ("PARTIAL_PAYMENT", "Partial Payment"),
            ("DRAFT", "Draft"), ("COMPLETE", "Complete"),
        ], detail.get("status") or "ACTIVE", width=260)
        created_at_field = text_field("Ngày Tạo", detail.get("created_at") or detail.get("createdAt") or _now_iso())
        type_field = dropdown("Chọn Loại Xe", type_options, str(detail.get("vehicle_type_id") or detail.get("vehicleTypeId") or ""), width=260)
        dialog_ref = {"dialog": None}

        def submit(_):
            if not validate_required(page, price_name_field, vehicle_field, start_field, end_field, status_field, created_at_field):
                return
            if not validate_required_int(page, booking_field, customer_field, price_field, type_field):
                return

            def save():
                try:
                    body = clean_body({
                        "booking_id": int_value(booking_field.value),
                        "customer_id": int_value(customer_field.value),
                        "package_price_id": int_value(price_field.value),
                        "package_price_name": price_name_field.value.strip(),
                        "vehicle_no": vehicle_field.value.strip(),
                        "start_date": start_field.value.strip(),
                        "end_date": end_field.value.strip(),
                        "status": status_field.value.strip().upper(),
                        "created_at": created_at_field.value.strip(),
                        "vehicle_type_id": int_value(type_field.value),
                    })
                    if detail_id:
                        api_client.admin_update_booking_detail(detail_id, body)
                    else:
                        api_client.admin_create_booking_detail(body)
                    close_dialog(page, dialog_ref["dialog"])
                    if body.get("booking_id"):
                        load_details(body["booking_id"], "")
                except Exception:
                    pass
            page.run_thread(save)

        dialog_ref["dialog"] = show_dialog(
            page,
            "Sửa Xe Trong Hợp Đồng" if detail_id else "Thêm Xe Vào Hợp Đồng",
            [booking_field, customer_field, price_field, price_name_field, vehicle_field, start_field, end_field, status_field, created_at_field, type_field],
            [
                ft.OutlinedButton(content="Hủy", on_click=lambda _: close_dialog(page, dialog_ref["dialog"]),
                                  style=outlined_button_style()),
                ft.ElevatedButton(content="Lưu", icon=ft.Icons.SAVE_ROUNDED, on_click=submit,
                                  style=filled_button_style()),
            ],
            width=520,
        )

    def delete_booking_detail(detail: dict):
        detail_id = detail.get("id")
        booking_id = detail.get("booking_id") or detail.get("bookingId") or state["selected_booking"]

        def do_delete(d):
            def run():
                try:
                    api_client.admin_delete_booking_detail(detail_id)
                    close_dialog(page, d)
                    if booking_id:
                        load_details(booking_id, "")
                except Exception:
                    pass
            page.run_thread(run)

        confirm_dialog(page, "Xóa Xe", "Xác Nhận Xóa Xe Khỏi Hợp Đồng?", do_delete)

    def load(pg: int = 0):
        loading.visible = True
        search_btn.disabled = True
        refresh_btn.disabled = True
        request_page_update(page)

        def fetch():
            try:
                resp = api_client.admin_get_bookings(
                    page=pg, size=20,
                    search=search_field.value.strip() or None,
                    group_id=int_value(group_filter.value),
                    package_id=int_value(package_filter.value),
                )
                data     = resp.get("data", {})
                bookings = data.get("content", [])
                total    = data.get("totalElements", 0)
                t_pages  = data.get("totalPages", 1)
                state["page"] = pg
                state["total_pages"] = t_pages

                rows = [_header_row()]
                for b in bookings:
                    rows.append(_booking_row(b))
                if not bookings:
                    rows.append(message_row())
                table_col.controls = rows
                page_info.value = f"Trang {pg + 1}/{t_pages} - {total} Hợp Đồng"
                update_pagination()
            except Exception as e:
                table_col.controls = [message_row(f"Lỗi Tải Hợp Đồng: {e}", DANGER)]
            loading.visible = False
            search_btn.disabled = False
            refresh_btn.disabled = False
            request_page_update(page)

        page.run_thread(fetch)

    search_field.on_submit = lambda _: load(0)
    group_filter.on_submit = lambda _: load(0)
    package_filter.on_submit = lambda _: load(0)

    def update_pagination():
        pagination_row.controls = build_pagination_controls(state["page"], state["total_pages"], load)
        pagination_row.visible = bool(pagination_row.controls)

    search_btn = ft.ElevatedButton(
        content="Tìm Kiếm",
        icon=ft.Icons.SEARCH_ROUNDED,
        on_click=lambda _: load(0),
        style=filled_button_style(),
    )
    refresh_btn = ft.OutlinedButton(
        content="Làm Mới",
        icon=ft.Icons.REFRESH_ROUNDED,
        on_click=lambda _: load(state["page"]),
        style=outlined_button_style(),
    )

    load(0)

    return ft.Column(
        expand=True,
        spacing=PAD_LG,
        controls=[
            ft.Text("Quản Lý Hợp Đồng", font_family=FONT_FAMILY,
                    size=SIZE_H2, weight=W_MEDIUM, color=TEXT_PRIMARY),
            ft.Text("Nhấn Vào Một Hợp Đồng Để Xem Danh Sách Xe Bên Trong.",
                    font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY),
            ft.Row(controls=[
                search_field,
                group_filter,
                package_filter,
                loading,
                ft.ElevatedButton(
                    content="Thêm Hợp Đồng",
                    icon=ft.Icons.ADD_ROUNDED,
                    on_click=lambda _: open_booking_dialog(),
                    style=filled_button_style(),
                ),
                search_btn,
                refresh_btn,
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
            detail_panel,
        ],
    )

