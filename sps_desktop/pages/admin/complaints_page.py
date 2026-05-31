"""
complaints_page.py - Admin complaint review and resolution.
"""
import flet as ft

from core import api_client
from core.design_tokens import *
from pages.admin.admin_ui import (
    close_dialog, data_of, dropdown, int_value, message_row, page_items, show_dialog,
    show_snack, text_field,
)


def build_complaints_page(page: ft.Page) -> ft.Control:
    loading = ft.ProgressRing(width=24, height=24, stroke_width=2, color=PRIMARY, visible=True)
    table_col = ft.Column(spacing=0, controls=[message_row("Đang Tải...")])
    page_info = ft.Text("", font_family=FONT_FAMILY, size=SIZE_CAPTION, color=TEXT_SECONDARY)
    pagination_row = ft.Row(alignment=ft.MainAxisAlignment.CENTER, spacing=8, visible=False)
    state = {"page": 0, "total_pages": 0}
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
        state_dp = {"date": None}

        def on_time_change(e):
            if not e.control.value or not state_dp["date"]:
                return
            t = e.control.value
            d = state_dp["date"]
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
            state_dp["date"] = d
            tf.value = d.strftime("%Y-%m-%dT00:00:00") if is_from else d.strftime("%Y-%m-%dT23:59:59")
            page.update()
            tp.open = True
            page.update()
        
        dp = ft.DatePicker(on_change=on_date_change)
        page.overlay.append(dp)
            
        def _on_clear(e):
            tf.value = ""
            state_dp["date"] = None
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

    created_from_filter = _date_picker_field("Tạo Từ", is_from=True, width=190)
    created_to_filter = _date_picker_field("Tạo Đến", is_from=False, width=190)
    solved_filter = dropdown("Trạng Thái", [
        ("", "Tất Cả"), ("true", "Đã Xử Lý"), ("false", "Chưa Xử Lý"),
    ], "", width=180)

    def header_row():
        return ft.Container(
            bgcolor=BG_ELEVATED,
            border_radius=RADIUS_MD,
            padding=ft.Padding(PAD_MD, PAD_SM, PAD_MD, PAD_SM),
            content=ft.Row(controls=[
                ft.Text("Khách Hàng", font_family=FONT_FAMILY, size=SIZE_CAPTION,
                        weight=W_MEDIUM, color=TEXT_SECONDARY, expand=2),
                ft.Text("Nội Dung", font_family=FONT_FAMILY, size=SIZE_CAPTION,
                        weight=W_MEDIUM, color=TEXT_SECONDARY, expand=4),
                ft.Text("Trạng Thái", font_family=FONT_FAMILY, size=SIZE_CAPTION,
                        weight=W_MEDIUM, color=TEXT_SECONDARY, expand=1),
                ft.Text("Ngày Tạo", font_family=FONT_FAMILY, size=SIZE_CAPTION,
                        weight=W_MEDIUM, color=TEXT_SECONDARY, expand=2),
                ft.Text("Thao Tác", font_family=FONT_FAMILY, size=SIZE_CAPTION,
                        weight=W_MEDIUM, color=TEXT_SECONDARY, expand=2),
            ]),
        )

    def complaint_row(item: dict):
        solved = bool(item.get("is_solved") or item.get("isSolved"))
        created = (item.get("created_at") or item.get("createdAt") or "")[:16].replace("T", " ")
        return ft.Container(
            padding=ft.Padding(PAD_MD, PAD_SM + 2, PAD_MD, PAD_SM + 2),
            border=border_only(bottom=ft.BorderSide(1, BORDER)),
            content=ft.Row(controls=[
                ft.Text(ui_title(item.get("customer_name") or item.get("customerName") or "-"),
                        font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_PRIMARY, expand=2),
                ft.Text(item.get("content", "-"), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, color=TEXT_SECONDARY, expand=4),
                ft.Text("Đã Xử Lý" if solved else "Chưa Xử Lý", font_family=FONT_FAMILY,
                        size=SIZE_SMALL, color=TEXT_SECONDARY, expand=1),
                ft.Text(created, font_family=FONT_FAMILY,
                        size=SIZE_SMALL, color=TEXT_SECONDARY, expand=2),
                ft.Row(expand=2, spacing=6, controls=[
                    ft.OutlinedButton(
                        content="",
                        icon=ft.Icons.VISIBILITY_ROUNDED,
                        tooltip="Xem Chi Tiết",
                        on_click=lambda _, row=item: open_detail(row),
                        style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                    ),
                    ft.ElevatedButton(
                        content="",
                        icon=ft.Icons.CHECK_ROUNDED,
                        tooltip="Xử Lý",
                        disabled=solved,
                        on_click=lambda _, row=item: solve_complaint(row),
                        style=filled_button_style(ft.Padding(8, 6, 8, 6)),
                    ),
                ]),
            ]),
        )

    def open_detail(item: dict):
        dialog_ref = {"dialog": None}
        content = item.get("content", "-")
        controls = [
            ft.Text(content, font_family=FONT_FAMILY, size=SIZE_BODY, color=TEXT_PRIMARY),
            ft.Divider(color=BORDER, height=1),
            ft.Text(f"Khách Hàng: {ui_title(item.get('customer_name') or item.get('customerName') or '-')}",
                    font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY),
            ft.Text(f"Số Điện Thoại: {item.get('customer_phone') or item.get('customerPhone') or '-'}",
                    font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY),
            ft.Text(f"Nhân Viên Xử Lý: {ui_title(item.get('employee_name') or item.get('employeeName') or '-')}",
                    font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY),
        ]
        
        img = item.get("imgUrl") or item.get("img_url") or item.get("imageUrl") or item.get("image_url")
        if img:
            from core.supabase_client import SUPABASE_URL
            src = img if str(img).startswith("http") else f"{SUPABASE_URL}/storage/v1/object/public/complaint-images/{img}"
            controls.insert(1, ft.Image(src=src, height=300, fit="contain", border_radius=RADIUS_MD))
        dialog_ref["dialog"] = show_dialog(
            page,
            "Chi Tiết Khiếu Nại",
            controls,
            [
                ft.OutlinedButton(content="Đóng", on_click=lambda _: close_dialog(page, dialog_ref["dialog"]),
                                  style=outlined_button_style()),
            ],
            width=620,
        )

    def solve_complaint(item: dict):
        def run():
            try:
                api_client.admin_solve_complaint(item["id"])
                show_snack(page, "Đã Xử Lý Khiếu Nại.")
                load(state["page"])
            except Exception as ex:
                show_snack(page, f"Lỗi Xử Lý Khiếu Nại: {ex}", DANGER)
        page.run_thread(run)

    def update_pagination():
        pagination_row.controls = build_pagination_controls(state["page"], state["total_pages"], load)
        pagination_row.visible = bool(pagination_row.controls)

    def load(pg: int = 0):
        loading.visible = True
        request_page_update(page)

        def fetch():
            try:
                items, total, total_pages = page_items(api_client.admin_get_complaints(
                    page=pg,
                    size=20,
                    created_from=created_from_filter.value.strip() or None,
                    created_to=created_to_filter.value.strip() or None,
                    is_solved=(str(solved_filter.value).lower() == "true") if solved_filter.value else None,
                ))
                state["page"] = pg
                state["total_pages"] = total_pages
                table_col.controls = [header_row()]
                table_col.controls.extend(complaint_row(item) for item in items)
                if not items:
                    table_col.controls.append(message_row())
                page_info.value = f"Trang {pg + 1}/{total_pages} - {total} Khiếu Nại"
                update_pagination()
            except Exception as ex:
                table_col.controls = [message_row(f"Lỗi Tải Khiếu Nại: {ex}", DANGER)]
            loading.visible = False
            request_page_update(page)

        page.run_thread(fetch)

    load(0)
    for filter_control in (created_from_filter, created_to_filter):
        # We don't have on_submit for date picker text fields since they are read_only
        pass
    solved_filter.on_select = lambda _: load(0)

    return ft.Column(
        expand=True,
        spacing=PAD_LG,
        controls=[
            ft.Row(controls=[
                ft.Text("Khiếu Nại", font_family=FONT_FAMILY,
                        size=SIZE_H2, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=True),
                loading,
                ft.OutlinedButton(content="Làm Mới", icon=ft.Icons.REFRESH_ROUNDED,
                                  on_click=lambda _: load(state["page"]), style=outlined_button_style()),
            ]),
            ft.Row(
                scroll=ft.ScrollMode.AUTO,
                spacing=PAD_SM,
                controls=[
                    created_from_filter,
                    created_to_filter,
                    solved_filter,
                    ft.ElevatedButton(
                        content="Tìm Kiếm",
                        icon=ft.Icons.SEARCH_ROUNDED,
                        on_click=lambda _: load(0),
                        style=filled_button_style(),
                    ),
                ],
            ),
            ft.Container(
                bgcolor=BG_CARD,
                border_radius=RADIUS_MD,
                border=border_all(1, BORDER),
                padding=PAD_LG,
                content=ft.Column(spacing=0, controls=[
                    table_col,
                    ft.Container(height=PAD_MD),
                    ft.Column(horizontal_alignment=ft.CrossAxisAlignment.CENTER,
                              spacing=PAD_SM, controls=[page_info, pagination_row]),
                ]),
            ),
        ],
    )
