import flet as ft
from desktop_ui.design_tokens import *
from desktop_ui.components.ui.shared_widgets import text_label, outlined_button, section_divider, badge

def zones_devices_view() -> ft.Row:
    # Zone tree (left panel)
    zone_tree = ft.Container(
        width=240,
        bgcolor=BACKGROUND,
        border=ft.border.only(right=ft.border.BorderSide(1, ft.Colors.with_opacity(0.10, PRIMARY))),
        content=ft.Column(
            scroll=ft.ScrollMode.AUTO,
            controls=[
                ft.Container(
                    padding=ft.Padding(left=PAGE_PADDING, right=PAGE_PADDING, top=PAGE_PADDING, bottom=8),
                    content=text_label("Zone Management", size=SIZE_H3, weight=W_SEMIBOLD),
                ),
                *_zone_items(),
                ft.Container(padding=PAGE_PADDING, content=outlined_button("Add Zone")),
            ],
        ),
    )

    # Zone detail (right panel)
    devices_table = ft.DataTable(expand=True, column_spacing=80,
        border=ft.Border.all(1, ft.Colors.with_opacity(0.10, PRIMARY)),
        border_radius=RADIUS_CARD,
        heading_row_height=40, data_row_min_height=48, divider_thickness=0.5,
        columns=[
            ft.DataColumn(ft.Text(h, font_family=FONT_FAMILY, size=SIZE_BODY_SMALL, weight=W_SEMIBOLD, color=PRIMARY))
            for h in ["Device Name", "Type", "Direction", "IP Address", "Status", "Last Ping"]
        ],
        rows=[
            ft.DataRow(cells=[
                ft.DataCell(text_label("CAM-001", size=SIZE_BODY)),
                ft.DataCell(text_label("LPR Camera", size=SIZE_BODY)),
                ft.DataCell(badge("IN")),
                ft.DataCell(text_label("192.168.1.10", size=SIZE_BODY)),
                ft.DataCell(ft.Row(spacing=6, controls=[
                    ft.Container(width=8, height=8, bgcolor=PRIMARY, border_radius=4),
                    text_label("Online", size=SIZE_BODY),
                ])),
                ft.DataCell(text_label("10:32:01", size=SIZE_BODY)),
            ]),
            ft.DataRow(
                color=ft.Colors.with_opacity(0.03, PRIMARY),
                cells=[
                    ft.DataCell(text_label("BARRIER-001", size=SIZE_BODY)),
                    ft.DataCell(text_label("Barrier Gate", size=SIZE_BODY)),
                    ft.DataCell(badge("IN")),
                    ft.DataCell(text_label("192.168.1.11", size=SIZE_BODY)),
                    ft.DataCell(ft.Row(spacing=6, controls=[
                        ft.Container(width=8, height=8, bgcolor=PRIMARY, border_radius=4),
                        text_label("Online", size=SIZE_BODY),
                    ])),
                    ft.DataCell(text_label("10:32:00", size=SIZE_BODY)),
                ],
            ),
        ],
    )

    detail_panel = ft.Container(
        expand=True,
        padding=PAGE_PADDING,
        content=ft.Column(spacing=16, scroll=ft.ScrollMode.AUTO, controls=[
            text_label("Basement B1 — Entry Gate", size=SIZE_H2, weight=W_SEMIBOLD),
            text_label("18 / 40 slots occupied", size=SIZE_BODY, color=ft.Colors.with_opacity(0.60, PRIMARY)),
            section_divider(),
            ft.Row(alignment=ft.MainAxisAlignment.SPACE_BETWEEN, controls=[
                text_label("Assigned Devices", size=SIZE_H3, weight=W_SEMIBOLD),
                ft.Row(spacing=8, controls=[
                    outlined_button("Ping All"),
                    outlined_button("Add Device"),
                ]),
            ]),
            ft.Container(content=devices_table, clip_behavior=ft.ClipBehavior.ANTI_ALIAS),
        ]),
    )

    return ft.Row(expand=True, spacing=0, controls=[zone_tree, detail_panel])


def _zone_items():
    items = [
        ("Building A", ft.Icons.APARTMENT_OUTLINED, 0),
        ("Basement B1", ft.Icons.FOUNDATION_OUTLINED, 1),
        ("Entry Gate", ft.Icons.DOOR_FRONT_DOOR_OUTLINED, 2),
        ("Exit Gate", ft.Icons.DOOR_BACK_DOOR_OUTLINED, 2),
        ("Basement B2", ft.Icons.FOUNDATION_OUTLINED, 1),
    ]
    result = []
    for label, icon, depth in items:
        is_active = label == "Basement B1"
        result.append(ft.Container(
            bgcolor=PRIMARY if is_active else ft.Colors.TRANSPARENT,
            border_radius=RADIUS_BUTTON,
            padding=ft.Padding(left=PAGE_PADDING + depth * 12, right=12, top=8, bottom=8),
            content=ft.Row(spacing=8, controls=[
                ft.Icon(icon, size=18, color=BACKGROUND if is_active else PRIMARY),
                ft.Text(label, font_family=FONT_FAMILY, size=SIZE_BODY,
                        color=BACKGROUND if is_active else PRIMARY),
            ]),
        ))
    return result
