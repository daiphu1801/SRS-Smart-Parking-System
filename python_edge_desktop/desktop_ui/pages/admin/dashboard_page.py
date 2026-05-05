import flet as ft
from desktop_ui.design_tokens import *
from desktop_ui.components.ui.shared_widgets import text_label, stat_card, badge

def dashboard_view() -> ft.Column:
    stats_row = ft.Row(
        spacing=16,
        controls=[
            ft.Container(expand=True, content=stat_card("Tổng Doanh Thu Hôm Nay", "4,250,000 đ", "+12% so với hôm qua", positive=True)),
            ft.Container(expand=True, content=stat_card("Phiên Đang Hoạt Động", "38", "+5 so với sáng nay", positive=True)),
            ft.Container(expand=True, content=stat_card("Chỗ Đã Có Xe", "142 / 200", "Tỷ lệ lấp đầy 71%", positive=True)),
            ft.Container(expand=True, content=stat_card("Khiếu Nại Đang Mở", "3", "-1 so với hôm qua", positive=False)),
        ],
    )

    chart_placeholder = ft.Container(
        height=280,
        border=ft.Border.all(1, ft.Colors.with_opacity(0.15, PRIMARY)),
        border_radius=RADIUS_CARD,
        content=ft.Column(
            alignment=ft.MainAxisAlignment.CENTER,
            horizontal_alignment=ft.CrossAxisAlignment.CENTER,
            controls=[
                ft.Icon(ft.Icons.SHOW_CHART, size=40, color=ft.Colors.with_opacity(0.30, PRIMARY)),
                ft.Container(height=8),
                text_label("Tổng Quan Doanh Thu", size=SIZE_H2, weight=W_SEMIBOLD),
            ],
        ),
    )

    legend_row = ft.Row(spacing=8, controls=[
        ft.Container(
            content=text_label("Thuê Bao", size=SIZE_CAPTION),
            padding=ft.Padding.symmetric(horizontal=10, vertical=4),
            border=ft.Border.all(1, PRIMARY), border_radius=RADIUS_BUTTON,
        ),
        ft.Container(
            content=text_label("Khách Lẻ", size=SIZE_CAPTION),
            padding=ft.Padding.symmetric(horizontal=10, vertical=4),
            border=ft.Border.all(1, PRIMARY), border_radius=RADIUS_BUTTON,
        ),
    ])

    # Recent sessions table
    headers = ["Biển Số", "Vào", "Ra", "Thời Lượng", "Phí", "Trạng Thái", "Loại"]
    table = ft.DataTable(expand=True, column_spacing=80,
        border_radius=RADIUS_CARD,
        border=ft.Border.all(1, ft.Colors.with_opacity(0.10, PRIMARY)),
        heading_row_height=40,
        data_row_min_height=48,
        data_row_max_height=48,
        divider_thickness=0.5,
        columns=[
            ft.DataColumn(ft.Text(h, font_family=FONT_FAMILY, size=SIZE_BODY_SMALL, weight=W_SEMIBOLD, color=PRIMARY))
            for h in headers
        ],
        rows=[
            ft.DataRow(cells=[
                ft.DataCell(text_label("51A-123.45", size=SIZE_BODY)),
                ft.DataCell(text_label("08:30", size=SIZE_BODY)),
                ft.DataCell(text_label("10:45", size=SIZE_BODY)),
                ft.DataCell(text_label("2h 15m", size=SIZE_BODY)),
                ft.DataCell(text_label("22,000 đ", size=SIZE_BODY)),
                ft.DataCell(badge("Đã Xong", filled=True)),
                ft.DataCell(text_label("Khách Lẻ", size=SIZE_BODY)),
            ]),
            ft.DataRow(
                color=ft.Colors.with_opacity(0.03, PRIMARY),
                cells=[
                    ft.DataCell(text_label("59B-678.90", size=SIZE_BODY)),
                    ft.DataCell(text_label("07:15", size=SIZE_BODY)),
                    ft.DataCell(text_label("—", size=SIZE_BODY, color=ft.Colors.with_opacity(0.40, PRIMARY))),
                    ft.DataCell(text_label("3h 25m", size=SIZE_BODY)),
                    ft.DataCell(text_label("0 đ", size=SIZE_BODY)),
                    ft.DataCell(badge("Trong Bãi")),
                    ft.DataCell(text_label("Cư Dân", size=SIZE_BODY)),
                ],
            ),
        ],
    )

    return ft.Column(
        spacing=SECTION_GAP,
        scroll=ft.ScrollMode.AUTO,
        controls=[
            stats_row,
            ft.Column(spacing=12, controls=[
                text_label("Tổng Quan Doanh Thu", size=SIZE_H2, weight=W_SEMIBOLD),
                chart_placeholder,
                legend_row,
            ]),
            ft.Column(spacing=12, controls=[
                text_label("Phiên Gần Đây", size=SIZE_H2, weight=W_SEMIBOLD),
                ft.Container(content=table, clip_behavior=ft.ClipBehavior.ANTI_ALIAS),
            ]),
        ],
    )
