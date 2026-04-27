"""
shared_widgets.py — Reusable Flet components following Smart Parking Design System
2-color palette: #052e16 (primary) / #ffffff (background)
"""
import flet as ft
from .design_tokens import (
    PRIMARY, BACKGROUND, SURFACE, BORDER, BORDER_STRONG, DISABLED, SUBTLE,
    RADIUS_CARD, RADIUS_BUTTON, RADIUS_BADGE,
    FONT_FAMILY, SIZE_BODY, SIZE_LABEL, SIZE_CAPTION, SIZE_H3,
    W_MEDIUM, W_SEMIBOLD, W_REGULAR, CARD_PADDING,
)


def filled_button(label: str, on_click=None, width=None, loading=False) -> ft.Button:
    """Primary action — dark green background, white text."""
    return ft.Button(
        content=ft.ProgressRing(color=BACKGROUND, width=18, height=18) if loading
                else ft.Text(label, font_family=FONT_FAMILY, size=SIZE_LABEL, weight=W_MEDIUM, color=BACKGROUND),
        on_click=on_click,
        width=width,
        height=40,
        bgcolor=PRIMARY,
        color=BACKGROUND,
        elevation=0,
        style=ft.ButtonStyle(
            shape=ft.RoundedRectangleBorder(radius=RADIUS_BUTTON),
            overlay_color=ft.Colors.with_opacity(0.08, BACKGROUND),
        ),
    )


def outlined_button(label: str, on_click=None, width=None) -> ft.OutlinedButton:
    """Secondary action — white background, green border."""
    return ft.OutlinedButton(
        content=ft.Text(label, font_family=FONT_FAMILY, size=SIZE_LABEL, weight=W_MEDIUM, color=PRIMARY),
        on_click=on_click,
        width=width,
        height=40,
        style=ft.ButtonStyle(
            shape=ft.RoundedRectangleBorder(radius=RADIUS_BUTTON),
            side=ft.BorderSide(1.5, PRIMARY),
            overlay_color=ft.Colors.with_opacity(0.06, PRIMARY),
        ),
    )


def text_label(value: str, size=SIZE_BODY, weight=W_REGULAR, color=None, opacity=1.0) -> ft.Text:
    return ft.Text(value, font_family=FONT_FAMILY, size=size, weight=weight,
                   color=color or PRIMARY, opacity=opacity)


def card(content: ft.Control, padding=CARD_PADDING, on_click=None) -> ft.Container:
    """White card with 1px border, rounded 8px, zero elevation."""
    return ft.Container(
        content=content,
        padding=padding,
        bgcolor=BACKGROUND,
        border=ft.Border.all(1, ft.Colors.with_opacity(0.10, PRIMARY)),
        border_radius=RADIUS_CARD,
        on_click=on_click,
    )


def badge(label: str, filled=False) -> ft.Container:
    """Status chip — 4px radius, white bg / filled primary."""
    return ft.Container(
        content=ft.Text(label, font_family=FONT_FAMILY, size=SIZE_CAPTION, weight=W_MEDIUM,
                        color=BACKGROUND if filled else PRIMARY),
        padding=ft.Padding.symmetric(horizontal=8, vertical=3),
        bgcolor=PRIMARY if filled else BACKGROUND,
        border=ft.Border.all(1, PRIMARY),
        border_radius=RADIUS_BADGE,
    )


def section_divider() -> ft.Divider:
    return ft.Divider(height=1, thickness=1, color=ft.Colors.with_opacity(0.10, PRIMARY))


def input_field(label: str, placeholder="", on_change=None, value="", read_only=False,
                password=False, keyboard_type=None) -> ft.Column:
    """Labelled input — 44px height, 1.5px border, 8px radius."""
    return ft.Column(
        spacing=6,
        controls=[
            ft.Text(label, font_family=FONT_FAMILY, size=SIZE_LABEL, weight=W_MEDIUM, color=PRIMARY),
            ft.TextField(
                value=value,
                hint_text=placeholder,
                read_only=read_only,
                password=password,
                keyboard_type=keyboard_type,
                on_change=on_change,
                height=44,
                text_style=ft.TextStyle(font_family=FONT_FAMILY, size=SIZE_BODY, color=PRIMARY),
                hint_style=ft.TextStyle(font_family=FONT_FAMILY, size=SIZE_BODY,
                                        color=ft.Colors.with_opacity(0.30, PRIMARY)),
                border_color=ft.Colors.with_opacity(0.12, PRIMARY),
                focused_border_color=PRIMARY,
                border_radius=RADIUS_CARD,
                border_width=1.5,
                focused_border_width=1.5,
                content_padding=ft.Padding.symmetric(horizontal=16, vertical=0),
            ),
        ],
    )


def empty_state(icon: str, title: str, subtitle="", action: ft.Control = None) -> ft.Column:
    controls = [
        ft.Icon(icon, size=40, color=ft.Colors.with_opacity(0.30, PRIMARY)),
        ft.Container(height=12),
        ft.Text(title, font_family=FONT_FAMILY, size=SIZE_H3, weight=W_SEMIBOLD, color=PRIMARY),
    ]
    if subtitle:
        controls += [
            ft.Container(height=6),
            ft.Text(subtitle, font_family=FONT_FAMILY, size=SIZE_BODY, color=ft.Colors.with_opacity(0.60, PRIMARY)),
        ]
    if action:
        controls += [ft.Container(height=20), action]
    return ft.Column(controls=controls, horizontal_alignment=ft.CrossAxisAlignment.CENTER)


def stat_card(label: str, value: str, change: str = "", positive: bool = True) -> ft.Container:
    """Dashboard stat card: label top, large number, change indicator."""
    return card(
        padding=CARD_PADDING,
        content=ft.Column(
            spacing=4,
            controls=[
                ft.Text(label, font_family=FONT_FAMILY, size=SIZE_CAPTION, weight=W_MEDIUM,
                        color=ft.Colors.with_opacity(0.60, PRIMARY)),
                ft.Text(value, font_family=FONT_FAMILY, size=32, weight=W_SEMIBOLD, color=PRIMARY),
                ft.Row(spacing=4, controls=[
                    ft.Icon(ft.Icons.ARROW_UPWARD if positive else ft.Icons.ARROW_DOWNWARD,
                            size=12, color=ft.Colors.with_opacity(1.0 if positive else 0.5, PRIMARY)),
                    ft.Text(change, font_family=FONT_FAMILY, size=SIZE_CAPTION,
                            color=ft.Colors.with_opacity(1.0 if positive else 0.5, PRIMARY)),
                ]) if change else ft.Container(),
            ],
        ),
    )
