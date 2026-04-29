import flet as ft
from desktop_ui.design_tokens import *
from desktop_ui.components.ui.shared_widgets import text_label, filled_button, outlined_button, stat_card, section_divider, badge

def reports_view() -> ft.Column:
    filter_bar = ft.Row(
        spacing=12,
        controls=[
            ft.TextField(
                hint_text="Start date",
                width=140, height=40, border_radius=RADIUS_CARD, border_width=1.5,
                border_color=ft.Colors.with_opacity(0.12, PRIMARY),
                text_style=ft.TextStyle(font_family=FONT_FAMILY, size=SIZE_BODY, color=PRIMARY),
                content_padding=ft.Padding.symmetric(horizontal=12, vertical=0),
            ),
            ft.TextField(
                hint_text="End date",
                width=140, height=40, border_radius=RADIUS_CARD, border_width=1.5,
                border_color=ft.Colors.with_opacity(0.12, PRIMARY),
                text_style=ft.TextStyle(font_family=FONT_FAMILY, size=SIZE_BODY, color=PRIMARY),
                content_padding=ft.Padding.symmetric(horizontal=12, vertical=0),
            ),
            ft.Dropdown(
                hint_text="All guards", width=160, height=40,
                border_radius=RADIUS_CARD, border_width=1.5,
                border_color=ft.Colors.with_opacity(0.12, PRIMARY),
                text_style=ft.TextStyle(font_family=FONT_FAMILY, size=SIZE_BODY, color=PRIMARY),
                options=[ft.dropdown.Option("Guard A"), ft.dropdown.Option("Guard B")],
            ),
            filled_button("Generate Report", width=160),
        ],
    )

    summary_cards = ft.Row(spacing=16, controls=[
        ft.Container(expand=True, content=stat_card("Subscription Revenue", "3,200,000 đ", "", positive=True)),
        ft.Container(expand=True, content=stat_card("Guest Revenue (QR)", "850,000 đ", "", positive=True)),
        ft.Container(expand=True, content=stat_card("Cash Revenue", "200,000 đ", "", positive=True)),
    ])

    audit_table = ft.DataTable(expand=True, column_spacing=80,
        border=ft.Border.all(1, ft.Colors.with_opacity(0.10, PRIMARY)),
        border_radius=RADIUS_CARD,
        heading_row_height=40, data_row_min_height=48, divider_thickness=0.5,
        columns=[
            ft.DataColumn(ft.Text(h, font_family=FONT_FAMILY, size=SIZE_BODY_SMALL, weight=W_SEMIBOLD, color=PRIMARY))
            for h in ["Timestamp", "Guard", "Action Type", "Plate", "Notes"]
        ],
        rows=[
            ft.DataRow(cells=[
                ft.DataCell(text_label("24/04 09:32", size=SIZE_BODY)),
                ft.DataCell(text_label("Guard A", size=SIZE_BODY)),
                ft.DataCell(badge("Manual Gate Open")),
                ft.DataCell(text_label("51A-999.88", size=SIZE_BODY)),
                ft.DataCell(text_label("Camera blur", size=SIZE_BODY)),
            ]),
            ft.DataRow(
                color=ft.Colors.with_opacity(0.03, PRIMARY),
                cells=[
                    ft.DataCell(text_label("24/04 10:05", size=SIZE_BODY)),
                    ft.DataCell(text_label("Guard B", size=SIZE_BODY)),
                    ft.DataCell(badge("Cash Collected")),
                    ft.DataCell(text_label("60B-111.22", size=SIZE_BODY)),
                    ft.DataCell(text_label("25,000 đ collected", size=SIZE_BODY)),
                ],
            ),
        ],
    )

    return ft.Column(spacing=SECTION_GAP, scroll=ft.ScrollMode.AUTO, controls=[
        ft.Row(alignment=ft.MainAxisAlignment.SPACE_BETWEEN, controls=[
            text_label("Reports and Audit", size=SIZE_H1, weight=W_SEMIBOLD),
            outlined_button("Export to CSV"),
        ]),
        filter_bar,
        summary_cards,
        section_divider(),
        text_label("Audit Log", size=SIZE_H2, weight=W_SEMIBOLD),
        ft.Container(content=audit_table, clip_behavior=ft.ClipBehavior.ANTI_ALIAS),
    ])
