import flet as ft
from desktop_ui.design_tokens import *
from desktop_ui.components.ui.shared_widgets import text_label, stat_card, section_divider

def shift_log_view() -> ft.Column:
    summary_row = ft.Row(spacing=16, controls=[
        ft.Container(expand=True, content=stat_card("Total Gate Opens", "24", "", positive=True)),
        ft.Container(expand=True, content=stat_card("Cash Collected", "175,000 đ", "", positive=True)),
        ft.Container(expand=True, content=stat_card("Alerts Resolved", "5", "", positive=True)),
        ft.Container(expand=True, content=stat_card("Shift Start", "08:00", "", positive=True)),
    ])

    timeline_items = [
        (ft.Icons.DOOR_FRONT_DOOR_OUTLINED, "10:05", "Manual gate open — 60B-111.22 (camera LPR failure)"),
        (ft.Icons.PAYMENTS_OUTLINED, "09:45", "Cash collected — 51A-999.88 — 25,000 đ"),
        (ft.Icons.WARNING_AMBER_OUTLINED, "09:32", "Alert resolved — 59B-678.90 blocking aisle at B1 Row 3"),
        (ft.Icons.DOOR_FRONT_DOOR_OUTLINED, "08:50", "Manual gate open — 29A-555.11 (subscriber override)"),
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
        text_label("Shift Log", size=SIZE_H1, weight=W_SEMIBOLD),
        summary_row,
        section_divider(),
        text_label("Today's Activity", size=SIZE_H2, weight=W_SEMIBOLD),
        ft.Column(spacing=0, controls=timeline_controls),
    ])
