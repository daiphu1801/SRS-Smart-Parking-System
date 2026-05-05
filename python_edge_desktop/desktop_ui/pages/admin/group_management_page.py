import flet as ft
from desktop_ui.design_tokens import *
from desktop_ui.components.ui.shared_widgets import text_label, filled_button, outlined_button
from desktop_ui.data.mock_data import MOCK_GROUPS_DATA

def group_management_view() -> ft.Column:
    search = ft.TextField(
        hint_text="Tìm theo tên hoặc mã",
        hint_style=ft.TextStyle(font_family=FONT_FAMILY, size=SIZE_BODY,
                                color=ft.Colors.with_opacity(0.30, PRIMARY)),
        border_color=ft.Colors.with_opacity(0.12, PRIMARY),
        focused_border_color=PRIMARY,
        border_radius=RADIUS_CARD,
        border_width=1.5,
        height=40,
        width=300,
        content_padding=ft.Padding.symmetric(horizontal=16, vertical=0),
        text_style=ft.TextStyle(font_family=FONT_FAMILY, size=SIZE_BODY, color=PRIMARY),
        prefix_icon=ft.Icons.SEARCH,
    )

    top_bar = ft.Row(
        alignment=ft.MainAxisAlignment.SPACE_BETWEEN,
        controls=[
            text_label("Quản Lý Nhóm", size=SIZE_H1, weight=W_SEMIBOLD),
            ft.Row(spacing=12, controls=[search, filled_button("Thêm Nhóm")]),
        ],
    )

    rows = []
    for i, g in enumerate(MOCK_GROUPS_DATA):
        rows.append(ft.DataRow(
            color=ft.Colors.with_opacity(0.03, PRIMARY) if i % 2 else ft.Colors.TRANSPARENT,
            cells=[
                ft.DataCell(text_label(g["name"], size=SIZE_BODY)),
                ft.DataCell(text_label(g["code"], size=SIZE_BODY)),
                ft.DataCell(text_label(g["profile"], size=SIZE_BODY)),
                ft.DataCell(text_label(g["owner"], size=SIZE_BODY)),
                ft.DataCell(text_label(g["vehicles"], size=SIZE_BODY)),
                ft.DataCell(ft.Row(spacing=8, controls=[
                    outlined_button("Xem"),
                    outlined_button("Sửa"),
                ])),
            ],
        ))

    table = ft.DataTable(expand=True, column_spacing=80,
        border=ft.Border.all(1, ft.Colors.with_opacity(0.10, PRIMARY)),
        border_radius=RADIUS_CARD,
        heading_row_height=40,
        data_row_min_height=48,
        divider_thickness=0.5,
        columns=[
            ft.DataColumn(ft.Text(h, font_family=FONT_FAMILY, size=SIZE_BODY_SMALL, weight=W_SEMIBOLD, color=PRIMARY))
            for h in ["Tên Nhóm", "Mã", "Hồ Sơ", "Chủ Sở Hữu", "Số Xe", "Hành Động"]
        ],
        rows=rows,
    )

    return ft.Column(spacing=SECTION_GAP, scroll=ft.ScrollMode.AUTO, controls=[
        top_bar,
        ft.Container(content=table, clip_behavior=ft.ClipBehavior.ANTI_ALIAS),
    ])
