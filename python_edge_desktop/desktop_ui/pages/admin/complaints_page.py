import flet as ft
from desktop_ui.design_tokens import *
from desktop_ui.components.ui.shared_widgets import text_label, badge
from desktop_ui.data.mock_data import MOCK_COMPLAINTS_DATA

def complaints_view() -> ft.Column:
    rows = []
    for i, c in enumerate(MOCK_COMPLAINTS_DATA):
        is_resolved = c["status"] == "Resolved"
        rows.append(ft.DataRow(
            color=ft.Colors.with_opacity(0.03, PRIMARY) if i % 2 else ft.Colors.TRANSPARENT,
            cells=[
                ft.DataCell(text_label(c["id"], size=SIZE_BODY)),
                ft.DataCell(text_label(c["customer"], size=SIZE_BODY)),
                ft.DataCell(text_label(c["category"], size=SIZE_BODY)),
                ft.DataCell(text_label(c["submitted"], size=SIZE_BODY)),
                ft.DataCell(badge(c["status"], filled=not is_resolved)),
            ],
        ))

    table = ft.DataTable(expand=True, column_spacing=80,
        border=ft.Border.all(1, ft.Colors.with_opacity(0.10, PRIMARY)),
        border_radius=RADIUS_CARD,
        heading_row_height=40, data_row_min_height=48, divider_thickness=0.5,
        columns=[
            ft.DataColumn(ft.Text(h, font_family=FONT_FAMILY, size=SIZE_BODY_SMALL, weight=W_SEMIBOLD, color=PRIMARY))
            for h in ["Ticket Id", "Customer", "Category", "Submitted At", "Status"]
        ],
        rows=rows,
    )

    return ft.Column(spacing=SECTION_GAP, scroll=ft.ScrollMode.AUTO, controls=[
        text_label("Complaints", size=SIZE_H1, weight=W_SEMIBOLD),
        ft.Container(content=table, clip_behavior=ft.ClipBehavior.ANTI_ALIAS),
    ])
