"""
guard_kiosk.py — Guard Desktop Screens (Python Flet)
Screens: Gate Control (2-column), Vehicle Lookup, Shift Log
"""
import flet as ft
from ..design_tokens import *
from ..shared_widgets import (
    filled_button, outlined_button, card, badge, section_divider,
    input_field, empty_state, stat_card, text_label
)


# ─── Gate Control (2-column layout) ──────────────────────────────────────────

def gate_control_view() -> ft.Row:
    """Left 60% — Gate Control Panel | Right 40% — Live Alert Feed"""
    plate_input = ft.TextField(
        hint_text="Enter plate number",
        hint_style=ft.TextStyle(font_family=FONT_FAMILY, size=36,
                                color=ft.Colors.with_opacity(0.30, PRIMARY)),
        text_style=ft.TextStyle(font_family=FONT_FAMILY, size=36, weight=W_SEMIBOLD, color=PRIMARY),
        border_color=ft.Colors.with_opacity(0.12, PRIMARY),
        focused_border_color=PRIMARY,
        border_radius=RADIUS_CARD,
        border_width=1.5,
        height=72,
        content_padding=ft.padding.symmetric(horizontal=16, vertical=0),
    )

    result_card = card(
        padding=CARD_PADDING,
        content=ft.Column(spacing=8, controls=[
            ft.Row(alignment=ft.MainAxisAlignment.SPACE_BETWEEN, controls=[
                text_label("51A-123.45", size=SIZE_H1, weight=W_SEMIBOLD),
                badge("Subscriber — Valid", filled=True),
            ]),
            text_label("Owner: Nguyen Van A | Unit 1502", size=SIZE_BODY,
                       color=ft.Colors.with_opacity(0.60, PRIMARY)),
            text_label("Entry: 08:30 today | Duration: 2h 15m", size=SIZE_BODY),
            ft.Container(height=4),
            ft.Row(spacing=8, controls=[
                ft.Expanded(filled_button("Open Gate")),
                ft.Expanded(outlined_button("Mark Cash Collected")),
            ]),
        ]),
    )

    # Override log
    log_items = [
        ("10:05", "Manual open — 60B-111.22 (camera blur)", ft.Icons.DOOR_FRONT_DOOR_OUTLINED),
        ("09:32", "Cash collected — 51A-999.88 — 25,000 đ", ft.Icons.PAYMENTS_OUTLINED),
        ("08:50", "Manual open — 29A-555.11 (subscriber)", ft.Icons.DOOR_FRONT_DOOR_OUTLINED),
    ]

    log_controls = [
        ft.Row(spacing=12, controls=[
            ft.Icon(icon, size=16, color=ft.Colors.with_opacity(0.60, PRIMARY)),
            ft.Column(spacing=2, expand=True, controls=[
                text_label(desc, size=SIZE_BODY),
                text_label(time, size=SIZE_BODY_SMALL, color=ft.Colors.with_opacity(0.60, PRIMARY)),
            ]),
        ])
        for time, desc, icon in log_items
    ]

    left_panel = ft.Container(
        width_percentage=0.60 if hasattr(ft, 'width_percentage') else None,
        expand=3,
        padding=PAGE_PADDING,
        content=ft.Column(spacing=16, scroll=ft.ScrollMode.AUTO, controls=[
            text_label("Gate Control", size=SIZE_H1, weight=W_SEMIBOLD),
            plate_input,
            ft.Row(spacing=8, controls=[
                ft.Expanded(filled_button("Check In")),
                ft.Expanded(filled_button("Check Out")),
            ]),
            result_card,
            section_divider(),
            text_label("Today's Override Log", size=SIZE_H3, weight=W_SEMIBOLD),
            *log_controls,
        ]),
    )

    # ── Right: Live Alert Feed ────────────────────────────────────────────────
    alert_cards = [
        {"plate": "59B-678.90", "zone": "B1 Row 3", "type": "Blocking aisle", "time": "10:32"},
        {"plate": "41A-333.22", "zone": "Gate B Entry", "type": "LPR failure", "time": "10:28"},
    ]

    alert_items = []
    for a in alert_cards:
        alert_items.append(
            card(
                padding=CARD_PADDING,
                content=ft.Column(spacing=8, controls=[
                    ft.Row(alignment=ft.MainAxisAlignment.SPACE_BETWEEN, controls=[
                        text_label(a["plate"], size=SIZE_H3, weight=W_SEMIBOLD),
                        text_label(a["time"], size=SIZE_BODY_SMALL, color=ft.Colors.with_opacity(0.60, PRIMARY)),
                    ]),
                    text_label(f"{a['zone']} · {a['type']}", size=SIZE_BODY,
                               color=ft.Colors.with_opacity(0.60, PRIMARY)),
                    filled_button("Resolve"),
                ]),
            )
        )
        alert_items.append(ft.Container(height=12))

    right_panel = ft.Container(
        expand=2,
        bgcolor=SURFACE,
        border=ft.border.only(left=ft.border.BorderSide(1, ft.Colors.with_opacity(0.10, PRIMARY))),
        padding=PAGE_PADDING,
        content=ft.Column(
            spacing=12,
            scroll=ft.ScrollMode.AUTO,
            controls=[
                text_label("Live Alerts", size=SIZE_H2, weight=W_SEMIBOLD),
                *(alert_items if alert_items else [empty_state(
                    ft.Icons.CHECK_CIRCLE_OUTLINE, "No active alerts", "All clear."
                )]),
            ],
        ),
    )

    return ft.Row(expand=True, spacing=0, controls=[left_panel, right_panel])


# ─── Vehicle Lookup ───────────────────────────────────────────────────────────

def vehicle_lookup_view() -> ft.Column:
    plate_field = ft.TextField(
        hint_text="Search plate number",
        hint_style=ft.TextStyle(font_family=FONT_FAMILY, size=SIZE_BODY,
                                color=ft.Colors.with_opacity(0.30, PRIMARY)),
        text_style=ft.TextStyle(font_family=FONT_FAMILY, size=SIZE_BODY, color=PRIMARY),
        border_color=ft.Colors.with_opacity(0.12, PRIMARY),
        focused_border_color=PRIMARY,
        border_radius=RADIUS_CARD,
        border_width=1.5,
        height=48,
        expand=True,
        content_padding=ft.padding.symmetric(horizontal=16, vertical=0),
    )

    result = card(
        padding=CARD_PADDING,
        content=ft.Column(spacing=10, controls=[
            ft.Row(alignment=ft.MainAxisAlignment.SPACE_BETWEEN, controls=[
                text_label("51A-123.45", size=24, weight=W_SEMIBOLD),
                badge("In Parking", filled=True),
            ]),
            section_divider(),
            ft.Row(spacing=32, controls=[
                ft.Column(spacing=4, controls=[
                    text_label("Session Start", size=SIZE_BODY_SMALL, color=ft.Colors.with_opacity(0.60, PRIMARY)),
                    text_label("08:30 today", size=SIZE_BODY),
                ]),
                ft.Column(spacing=4, controls=[
                    text_label("Duration", size=SIZE_BODY_SMALL, color=ft.Colors.with_opacity(0.60, PRIMARY)),
                    text_label("2h 15m", size=SIZE_BODY),
                ]),
                ft.Column(spacing=4, controls=[
                    text_label("Vehicle Type", size=SIZE_BODY_SMALL, color=ft.Colors.with_opacity(0.60, PRIMARY)),
                    text_label("Motorbike", size=SIZE_BODY),
                ]),
                ft.Column(spacing=4, controls=[
                    text_label("Owner", size=SIZE_BODY_SMALL, color=ft.Colors.with_opacity(0.60, PRIMARY)),
                    text_label("Nguyen Van A", size=SIZE_BODY),
                ]),
                ft.Column(spacing=4, controls=[
                    text_label("Unit", size=SIZE_BODY_SMALL, color=ft.Colors.with_opacity(0.60, PRIMARY)),
                    text_label("1502 Tower A", size=SIZE_BODY),
                ]),
            ]),
            outlined_button("View Full Session"),
        ]),
    )

    return ft.Column(spacing=24, controls=[
        text_label("Vehicle Lookup", size=SIZE_H1, weight=W_SEMIBOLD),
        ft.Row(spacing=12, controls=[plate_field, filled_button("Search", width=120)]),
        result,
    ])


# ─── Shift Log ───────────────────────────────────────────────────────────────

def shift_log_view() -> ft.Column:
    summary_row = ft.Row(spacing=16, controls=[
        ft.Expanded(stat_card("Total Gate Opens", "24", "", positive=True)),
        ft.Expanded(stat_card("Cash Collected", "175,000 đ", "", positive=True)),
        ft.Expanded(stat_card("Alerts Resolved", "5", "", positive=True)),
        ft.Expanded(stat_card("Shift Start", "08:00", "", positive=True)),
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
            ft.Expanded(text_label(desc, size=SIZE_BODY)),
        ]))
        timeline_controls.append(ft.Container(height=8))

    return ft.Column(spacing=SECTION_GAP, scroll=ft.ScrollMode.AUTO, controls=[
        text_label("Shift Log", size=SIZE_H1, weight=W_SEMIBOLD),
        summary_row,
        section_divider(),
        text_label("Today's Activity", size=SIZE_H2, weight=W_SEMIBOLD),
        ft.Column(spacing=0, controls=timeline_controls),
    ])
