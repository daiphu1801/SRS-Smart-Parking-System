import flet as ft
from desktop_ui.design_tokens import *
from desktop_ui.components.ui.shared_widgets import text_label, stat_card, section_divider

def shift_log_view() -> ft.Column:
    summary_row = ft.Row(spacing=16, controls=[
        ft.Container(expand=True, content=stat_card("Số lần mở cổng", "24", "", positive=True)),
        ft.Container(expand=True, content=stat_card("Thu tiền mặt", "175,000 đ", "", positive=True)),
        ft.Container(expand=True, content=stat_card("Cảnh báo đã xử lý", "5", "", positive=True)),
        ft.Container(expand=True, content=stat_card("Ca làm việc bắt đầu", "08:00", "", positive=True)),
    ])

    timeline_items = [
        (ft.Icons.DOOR_FRONT_DOOR_OUTLINED, "10:05", "Mở cổng tay — 60B-111.22 (lỗi camera LPR)"),
        (ft.Icons.PAYMENTS_OUTLINED, "09:45", "Thu tiền mặt — 51A-999.88 — 25,000 đ"),
        (ft.Icons.WARNING_AMBER_OUTLINED, "09:32", "Cảnh báo đã xử lý — 59B-678.90 chắn lối đi tại Hàng 3 B1"),
        (ft.Icons.DOOR_FRONT_DOOR_OUTLINED, "08:50", "Mở cổng tay — 29A-555.11 (ghi đè thuê bao)"),
    ]

    timeline_controls = []
    for icon, time_val, desc in timeline_items:
        timeline_controls.append(ft.Row(spacing=16, vertical_alignment=ft.CrossAxisAlignment.START, controls=[
            text_label(time_val, size=SIZE_BODY_SMALL, color=ft.Colors.with_opacity(0.60, PRIMARY)),
            ft.Container(width=1, height=36, bgcolor=ft.Colors.with_opacity(0.15, PRIMARY)),
            ft.Icon(icon, size=18, color=PRIMARY),
            ft.Container(expand=True, content=text_label(desc, size=SIZE_BODY)),
        ]))
        timeline_controls.append(ft.Container(height=8))

    return ft.Column(spacing=SECTION_GAP, scroll=ft.ScrollMode.AUTO, controls=[
        text_label("Nhật Ký Ca Trực", size=SIZE_H1, weight=W_SEMIBOLD),
        summary_row,
        section_divider(),
        text_label("Hoạt Động Hôm Nay", size=SIZE_H2, weight=W_SEMIBOLD),
        ft.Column(spacing=0, controls=timeline_controls),
    ])
