import flet as ft
from desktop_ui.design_tokens import *
from desktop_ui.components.ui.shared_widgets import text_label, filled_button, outlined_button, card, badge, section_divider

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
