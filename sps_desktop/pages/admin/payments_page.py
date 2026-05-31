"""
payments_page.py - GET /api/v1/admin/payments (paginated, filterable)
"""
import threading
import time
import flet as ft
from core.design_tokens import *
from core import api_client, app_events
from core.settings import get_settings
from pages.admin.admin_ui import (
    close_dialog, data_of, dropdown, float_value, int_value, message_row, show_dialog,
    show_snack, text_field,
)


_STATUS_COLORS = {"SUCCESS": SUCCESS, "PENDING": WARNING, "FAILED": DANGER, "CANCELED": TEXT_DISABLED}


def build_payments_page(page: ft.Page) -> ft.Control:
    search_field = ft.TextField(
        hint_text="Tìm Mã Giao Dịch (Pay Code)...",
        border_color=BORDER, focused_border_color=PRIMARY,
        hint_style=ft.TextStyle(color=TEXT_DISABLED, font_family=FONT_FAMILY),
        text_style=ft.TextStyle(color=TEXT_PRIMARY, font_family=FONT_FAMILY),
        bgcolor=BG_CARD, border_radius=RADIUS_MD, cursor_color=PRIMARY, expand=True,
        height=44,
        content_padding=ft.Padding(16, 0, 16, 0),
    )
    status_dd = dropdown("Trạng Thái", [
        ("", "Tất Cả"), ("PENDING", "Pending"), ("SUCCESS", "Success"),
        ("FAILED", "Failed"), ("PARTIAL_PAYMENT", "Partial Payment"),
        ("CANCELED", "Canceled"), ("EXPIRED", "Expired"),
        ("NEEDS_ATTENTION", "Needs Attention"), ("REFUND_PENDING", "Refund Pending"),
    ], "", width=190)
    customer_id_filter = text_field("ID Khách Hàng", number=True, width=160)
    customer_phone_filter = text_field("Số Điện Thoại", width=170)
    gateway_filter = text_field("Gateway", width=150)
    method_filter = dropdown("Phương Thức", [
        ("", "Tất Cả"), ("CASH", "Cash"), ("BANK_TRANSFER", "Bank Transfer"),
        ("CREDIT_CARD", "Credit Card"), ("E_WALLET", "E Wallet"),
    ], "", width=190)
    session_filter = text_field("ID Phiên Đỗ", number=True, width=160)
    amount_min_filter = text_field("Số Tiền Từ", number=True, width=150)
    amount_max_filter = text_field("Số Tiền Đến", number=True, width=150)
    created_from_filter = text_field("Tạo Từ", width=170)
    created_to_filter = text_field("Tạo Đến", width=170)
    updated_from_filter = text_field("Cập Nhật Từ", width=170)
    updated_to_filter = text_field("Cập Nhật Đến", width=170)

    loading   = ft.ProgressRing(width=24, height=24, stroke_width=2, color=PRIMARY, visible=True)
    table_col = ft.Column(spacing=0, controls=[message_row("Đang Tải...")])
    page_info  = ft.Text("", font_family=FONT_FAMILY, size=SIZE_CAPTION, color=TEXT_SECONDARY)
    pagination_row = ft.Row(alignment=ft.MainAxisAlignment.CENTER, spacing=8, visible=False)
    state = {"page": 0, "total_pages": 0, "loading": False, "tab": "ALL"}

    HEADERS = ["Mã GD", "Người Thanh Toán", "Số Tiền (₫)", "Phương Thức", "Trạng Thái", "Ngày Tạo", "Thao Tác"]
    EXPANDS = [2, 2, 1, 1, 1, 2, 1]
    
    EXC_HEADERS = ["Mã GD", "Phiên Đỗ", "Số Tiền (₫)", "Trạng Thái", "Cảnh Báo", "Nội Dung Lỗi", "Thao Tác"]
    EXC_EXPANDS = [2, 2, 1, 1, 1, 2, 2]

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

    def _header_row_exceptions():
        return ft.Container(
            bgcolor=BG_ELEVATED, border_radius=RADIUS_MD,
            padding=ft.Padding(PAD_MD, PAD_SM, PAD_MD, PAD_SM),
            content=ft.Row(controls=[
                ft.Text(h, font_family=FONT_FAMILY, size=SIZE_CAPTION,
                        weight=W_MEDIUM, color=TEXT_SECONDARY, expand=e)
                for h, e in zip(EXC_HEADERS, EXC_EXPANDS)
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

    def _data_row(p: dict) -> ft.Container:
        created = (p.get("created_at") or "")[:16].replace("T", " ")
        payer = ui_title(p.get("payer_name") or p.get("customer_full_name") or p.get("customer_phone") or "-")
        try: amount = f"{float(p.get('amount', 0)):,.0f}"
        except: amount = "0"
        return ft.Container(
            padding=ft.Padding(PAD_MD, PAD_SM + 2, PAD_MD, PAD_SM + 2),
            border=border_only(bottom=ft.BorderSide(1, BORDER)),
            content=ft.Row(controls=[
                ft.Text(p.get("pay_code", "-"), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, weight=W_MEDIUM, color=PRIMARY, expand=2),
                ft.Text(payer, font_family=FONT_FAMILY,
                        size=SIZE_SMALL, color=TEXT_PRIMARY, expand=2),
                ft.Text(amount, font_family=FONT_FAMILY, size=SIZE_SMALL, color=SUCCESS, expand=1),
                ft.Text(ui_title(p.get("method", "-")), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, color=TEXT_SECONDARY, expand=1),
                ft.Container(expand=1, content=_status_badge(p.get("status", ""))),
                ft.Text(created, font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY, expand=2),
                ft.Row(expand=1, spacing=6, controls=[
                    ft.OutlinedButton(
                        content="",
                        icon=ft.Icons.VISIBILITY_ROUNDED,
                        tooltip="Xem Chi Tiết",
                        on_click=lambda _, row=p: open_payment_detail(row),
                        style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                    ),
                ]),
            ]),
        )

    def _exception_row(p: dict) -> ft.Container:
        flag = p.get("reconciliationFlag") or p.get("reconciliation_flag") or ""
        bg_color = DANGER if flag == "RED_FLAG" else WARNING if flag == "YELLOW_FLAG" else TEXT_DISABLED
        flag_label = "NGUY HIỂM" if flag == "RED_FLAG" else "CẢNH BÁO" if flag == "YELLOW_FLAG" else "LƯU Ý"
        
        try: amount = f"{float(p.get('amount', 0)):,.0f}"
        except: amount = "0"
        
        session_id = str(p.get("parkingSessionId") or p.get("parking_session_id") or "-")
        warning_msg = p.get("warningMessage") or p.get("warning_message") or ""
        
        return ft.Container(
            padding=ft.Padding(PAD_MD, PAD_SM + 2, PAD_MD, PAD_SM + 2),
            border=border_only(bottom=ft.BorderSide(1, BORDER)),
            content=ft.Row(controls=[
                ft.Text(p.get("payCode") or p.get("pay_code") or "-", font_family=FONT_FAMILY,
                        size=SIZE_SMALL, weight=W_MEDIUM, color=PRIMARY, expand=2),
                ft.Text(session_id, font_family=FONT_FAMILY,
                        size=SIZE_SMALL, color=TEXT_PRIMARY, expand=2),
                ft.Text(amount, font_family=FONT_FAMILY, size=SIZE_SMALL, color=SUCCESS, expand=1),
                ft.Container(expand=1, content=_status_badge(p.get("status", ""))),
                ft.Container(
                    bgcolor=bg_color, border_radius=RADIUS_MD, padding=ft.Padding(8, 3, 8, 3),
                    content=ft.Text(flag_label, font_family=FONT_FAMILY, size=SIZE_CAPTION, color=WHITE, weight=W_MEDIUM),
                    expand=1
                ),
                ft.Text(warning_msg, font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY, expand=2, max_lines=2, overflow=ft.TextOverflow.ELLIPSIS),
                ft.Row(expand=2, spacing=6, controls=[
                    ft.OutlinedButton(
                        content="",
                        icon=ft.Icons.VISIBILITY_ROUNDED,
                        tooltip="Xem Chi Tiết",
                        on_click=lambda _, row=p: open_payment_detail({"id": row.get("id")}),
                        style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                    ),
                ] + ([
                    ft.ElevatedButton(
                        content="",
                        icon=ft.Icons.BUILD_ROUNDED,
                        tooltip="Xử Lí",
                        on_click=lambda _, row=p: resolve_payment({"id": row.get("id"), "status": row.get("status")}),
                        style=filled_button_style(ft.Padding(8, 6, 8, 6)),
                    ),
                    ft.ElevatedButton(
                        content="",
                        icon=ft.Icons.CANCEL_ROUNDED,
                        tooltip="Hủy Giao Dịch",
                        on_click=lambda _, row=p: cancel_payment({"id": row.get("id"), "status": row.get("status")}),
                        bgcolor=DANGER,
                        color=WHITE,
                        style=ft.ButtonStyle(padding=ft.Padding(8, 6, 8, 6), shape=ft.RoundedRectangleBorder(radius=RADIUS_MD)),
                    ),
                ] if str(p.get("status", "")).upper() == "NEEDS_ATTENTION" else [])),
            ]),
        )

    def open_payment_detail(payment: dict):
        dialog_ref = {"dialog": None}
        detail_col = ft.Column(tight=True, spacing=PAD_SM, controls=[
            ft.ProgressRing(width=24, height=24, stroke_width=2, color=PRIMARY),
        ])

        dialog_ref["dialog"] = show_dialog(
            page,
            "Chi Tiết Thanh Toán",
            [detail_col],
            [
                ft.OutlinedButton(content="Đóng", on_click=lambda _: close_dialog(page, dialog_ref["dialog"]),
                                  style=outlined_button_style()),
            ],
            width=620,
        )

        def fetch():
            try:
                detail = data_of(api_client.admin_get_payment_details_tree(payment["id"]))
                info = detail.get("payment_info") or detail.get("paymentInfo") or payment
                details = detail.get("details") or []
                rows = [
                    ("Mã Giao Dịch", info.get("pay_code") or info.get("payCode")),
                    ("Khách Hàng", info.get("customer_full_name") or info.get("customerFullName") or "-"),
                    ("Số Điện Thoại", info.get("customer_phone") or info.get("customerPhone") or "-"),
                    ("Phiên Đỗ Xe", info.get("parking_session_id") or info.get("parkingSessionId") or "-"),
                    ("Số Tiền", info.get("amount") or "0"),
                    ("Trạng Thái", ui_title(info.get("status") or "-")),
                ]
                new_detail = []
                for label, value in rows:
                    new_detail.append(ft.Row(controls=[
                        ft.Text(label, font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY, expand=1),
                        ft.Text(str(value), font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_PRIMARY, expand=2),
                    ]))
                if details:
                    new_detail.append(ft.Divider(color=BORDER, height=1))
                    for item in details:
                        new_detail.append(ft.Text(
                            f"Chi Tiết #{item.get('id', '-')}: {item.get('item_amount') or item.get('itemAmount') or 0}",
                            font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_PRIMARY,
                        ))
                detail_col.controls = new_detail or [message_row()]
            except Exception as ex:
                detail_col.controls = [message_row(f"Lỗi Tải Chi Tiết: {ex}", DANGER)]
            request_page_update(page)

        page.run_thread(fetch)

    def resolve_payment(payment: dict):
        def do():
            try:
                api_client.admin_resolve_payment(payment["id"])
                load(state["page"])
            except Exception:
                pass
        page.run_thread(do)

    def cancel_payment(payment: dict):
        def do():
            try:
                api_client.admin_cancel_payment(payment["id"])
                load(state["page"])
            except Exception:
                pass
        page.run_thread(do)

    def load(pg: int = 0):
        if state["loading"]:
            return
        state["loading"] = True
        loading.visible = True
        refresh_btn.disabled = True
        request_page_update(page)

        def fetch():
            try:
                if state["tab"] == "ALL":
                    resp = api_client.admin_get_payments(
                        page=pg, size=20,
                        pay_code=search_field.value.strip() or None,
                        status=status_dd.value or None,
                        customer_id=int_value(customer_id_filter.value),
                        customer_phone=customer_phone_filter.value.strip() or None,
                        gateway=gateway_filter.value.strip() or None,
                        method=method_filter.value or None,
                        parking_session_id=int_value(session_filter.value),
                        min_amount=float_value(amount_min_filter.value),
                        max_amount=float_value(amount_max_filter.value),
                        created_at_from=created_from_filter.value.strip() or None,
                        created_at_to=created_to_filter.value.strip() or None,
                        updated_at_from=updated_from_filter.value.strip() or None,
                        updated_at_to=updated_to_filter.value.strip() or None,
                    )
                else:
                    resp = api_client.admin_get_reconciliation_exceptions(page=pg, size=20)
                
                data     = resp.get("data", {})
                payments = data.get("content", [])
                total    = data.get("totalElements", 0)
                t_pages  = data.get("totalPages", 1)
                state["page"] = pg
                state["total_pages"] = t_pages

                if state["tab"] == "ALL":
                    rows = [_header_row()]
                    for p_ in payments:
                        rows.append(_data_row(p_))
                else:
                    rows = [_header_row_exceptions()]
                    for p_ in payments:
                        rows.append(_exception_row(p_))
                        
                if not payments:
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
    status_dd.on_select    = lambda _: load(0)
    method_filter.on_select = lambda _: load(0)
    for filter_control in (
        customer_id_filter, customer_phone_filter, gateway_filter, session_filter,
        amount_min_filter, amount_max_filter, created_from_filter, created_to_filter,
        updated_from_filter, updated_to_filter,
    ):
        filter_control.on_submit = lambda _: load(0)

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

    btn_all = ft.ElevatedButton(
        content="Lịch Sử Thanh Toán",
        icon=ft.Icons.HISTORY_ROUNDED,
        style=filled_button_style(ft.Padding(16, 10, 16, 10)),
        on_click=lambda _: set_tab("ALL")
    )
    btn_exceptions = ft.OutlinedButton(
        content="Ngoại Lệ Đối Soát",
        icon=ft.Icons.WARNING_ROUNDED,
        style=outlined_button_style(ft.Padding(16, 10, 16, 10)),
        on_click=lambda _: set_tab("EXCEPTIONS")
    )

    search_controls_row = ft.Row(controls=[
        search_field,
        status_dd,
        loading,
        refresh_btn,
        ft.ElevatedButton(
            content="Tìm Kiếm",
            icon=ft.Icons.SEARCH_ROUNDED,
            on_click=lambda _: load(0),
            style=filled_button_style(),
        ),
    ])
    
    filter_scroll_row = ft.Row(
        scroll=ft.ScrollMode.AUTO,
        spacing=PAD_SM,
        controls=[
            customer_id_filter,
            customer_phone_filter,
            gateway_filter,
            method_filter,
            session_filter,
            amount_min_filter,
            amount_max_filter,
            created_from_filter,
            created_to_filter,
            updated_from_filter,
            updated_to_filter,
        ],
    )

    def set_tab(tab_name: str):
        if state["tab"] == tab_name: return
        state["tab"] = tab_name
        if tab_name == "ALL":
            btn_all.style = filled_button_style(ft.Padding(16, 10, 16, 10))
            btn_exceptions.style = outlined_button_style(ft.Padding(16, 10, 16, 10))
            search_controls_row.visible = True
            filter_scroll_row.visible = True
            refresh_btn.visible = True
        else:
            btn_all.style = outlined_button_style(ft.Padding(16, 10, 16, 10))
            btn_exceptions.style = filled_button_style(ft.Padding(16, 10, 16, 10))
            search_controls_row.visible = False
            filter_scroll_row.visible = False
            refresh_btn.visible = False # we put refresh logic elsewhere or keep a global refresh
            
        load(0)
        request_page_update(page)

    load(0)
    page.run_thread(auto_refresh_loop)

    return ft.Column(
        expand=True,
        spacing=PAD_LG,
        controls=[
            ft.Row(controls=[
                ft.Text("Thanh Toán", font_family=FONT_FAMILY, size=SIZE_H2, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=True),
                btn_all,
                btn_exceptions,
                ft.Container(width=16, visible=False) if state["tab"] == "ALL" else refresh_btn, # Just in case we want refresh in exceptions
            ]),
            search_controls_row,
            filter_scroll_row,
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

