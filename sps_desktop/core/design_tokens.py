"""
design_tokens.py — Shared design system for SPS Desktop App
"""
import threading
import time

import flet as ft

_UPDATE_LOCK = threading.Lock()
_UPDATE_PENDING: set[int] = set()

# ──────────────────── Color Palette ────────────────────
# UI rule: only white and green-950 are allowed.
WHITE     = "#FFFFFF"
GREEN_950 = "#052E16"

BG_BASE       = WHITE
BG_CARD       = WHITE
BG_ELEVATED   = WHITE
BG_SIDEBAR    = WHITE
BORDER        = GREEN_950
BORDER_FOCUS  = GREEN_950

PRIMARY       = GREEN_950
PRIMARY_DARK  = GREEN_950
PRIMARY_GLOW  = GREEN_950

SUCCESS       = GREEN_950
SUCCESS_GLOW  = GREEN_950
WARNING       = GREEN_950
WARNING_GLOW  = GREEN_950
DANGER        = GREEN_950
DANGER_GLOW   = GREEN_950
INFO          = GREEN_950

TEXT_PRIMARY   = GREEN_950
TEXT_SECONDARY = GREEN_950
TEXT_DISABLED  = GREEN_950

# ──────────────────── Typography ───────────────────────
FONT_FAMILY = "Inter"
FONT_URL    = "https://fonts.gstatic.com/s/inter/v13/UcCO3FwrK3iLTeHuS_fvQtMwCp50KnMw2boKoduKmMEVuLyfAZ9hiA.woff2"

SIZE_DISPLAY = 32
SIZE_H1      = 24
SIZE_H2      = 20
SIZE_H3      = 16
SIZE_BODY    = 14
SIZE_SMALL   = 12
SIZE_CAPTION = 11

W_REGULAR  = "normal"
W_MEDIUM   = "w500"

# ──────────────────── Spacing & Radius ─────────────────
RADIUS_MD = 10

PAD_XS = 4
PAD_SM = 8
PAD_MD = 16
PAD_LG = 24
PAD_XL = 32

SIDEBAR_W = 220
TOPBAR_H  = 60

DEFAULT_WINDOW_WIDTH = 1920
DEFAULT_WINDOW_HEIGHT = 1080
MIN_WINDOW_WIDTH = 1280
MIN_WINDOW_HEIGHT = 720


def border_all(width=1, color=BORDER) -> ft.Border:
    side = ft.BorderSide(width, color)
    return ft.Border(side, side, side, side)


def border_only(left=None, top=None, right=None, bottom=None) -> ft.Border:
    return ft.Border(left=left, top=top, right=right, bottom=bottom)


def ui_title(value) -> str:
    if value is None or value == "":
        return "-"
    return str(value).replace("_", " ").title()


def _state_color(value: str) -> dict:
    return {
        ft.ControlState.DEFAULT: value,
        ft.ControlState.HOVERED: value,
        ft.ControlState.FOCUSED: value,
        ft.ControlState.PRESSED: value,
        ft.ControlState.DISABLED: value,
    }


def _state_transition_color(default: str, hovered: str) -> dict:
    return {
        ft.ControlState.DEFAULT: default,
        ft.ControlState.HOVERED: hovered,
        ft.ControlState.FOCUSED: hovered,
        ft.ControlState.PRESSED: hovered,
        ft.ControlState.DISABLED: default,
    }


def _state_cursor(value) -> dict:
    return {
        ft.ControlState.DEFAULT: value,
        ft.ControlState.HOVERED: value,
        ft.ControlState.FOCUSED: value,
        ft.ControlState.PRESSED: value,
        ft.ControlState.DISABLED: value,
    }


def filled_button_style(padding=None, text_style=None) -> ft.ButtonStyle:
    return ft.ButtonStyle(
        bgcolor=_state_color(PRIMARY),
        color=_state_color(WHITE),
        icon_color=_state_color(WHITE),
        elevation=_state_color(0),
        overlay_color=_state_color(PRIMARY),
        shadow_color=_state_color(WHITE),
        mouse_cursor=_state_cursor(ft.MouseCursor.CLICK),
        shape=ft.RoundedRectangleBorder(radius=RADIUS_MD),
        padding=padding,
        text_style=text_style,
    )


def outlined_button_style(padding=None, text_style=None) -> ft.ButtonStyle:
    return ft.ButtonStyle(
        bgcolor=_state_transition_color(WHITE, PRIMARY),
        color=_state_transition_color(PRIMARY, WHITE),
        icon_color=_state_transition_color(PRIMARY, WHITE),
        mouse_cursor=_state_cursor(ft.MouseCursor.CLICK),
        side=ft.BorderSide(1, PRIMARY),
        shape=ft.RoundedRectangleBorder(radius=RADIUS_MD),
        padding=padding,
        text_style=text_style,
    )


def _pagination_button(content, on_click, *, icon=None, active=False, disabled=False, tooltip=None):
    style = filled_button_style(ft.Padding(10, 8, 10, 8)) if active else outlined_button_style(ft.Padding(10, 8, 10, 8))
    button_cls = ft.ElevatedButton if active else ft.OutlinedButton
    kwargs = {
        "content": content,
        "tooltip": tooltip,
        "disabled": disabled,
        "on_click": None if disabled else on_click,
        "style": style,
    }
    if icon is not None:
        kwargs["content"] = ""
        kwargs["icon"] = icon
    return button_cls(**kwargs)


def build_pagination_controls(current_page: int, total_pages: int, on_page_change) -> list[ft.Control]:
    if total_pages <= 1:
        return []

    current_page = max(0, min(current_page, total_pages - 1))
    display_page = current_page + 1
    last_page = total_pages

    def go(target_page: int):
        return lambda _: on_page_change(max(0, min(target_page, total_pages - 1)))

    def nav_button(icon, target_page: int, disabled: bool, tooltip: str):
        return _pagination_button(
            "",
            go(target_page),
            icon=icon,
            disabled=disabled,
            tooltip=tooltip,
        )

    def page_button(page_number: int, active: bool = False):
        return _pagination_button(
            str(page_number),
            go(page_number - 1),
            active=active,
        )

    controls: list[ft.Control] = [
        nav_button(ft.Icons.KEYBOARD_DOUBLE_ARROW_LEFT_ROUNDED, 0, current_page == 0, "Trang Đầu"),
        nav_button(ft.Icons.CHEVRON_LEFT_ROUNDED, current_page - 1, current_page == 0, "Trang Trước"),
    ]

    if display_page > 2:
        controls.append(ft.Text("...", font_family=FONT_FAMILY, size=SIZE_BODY, color=TEXT_SECONDARY))

    if display_page > 1:
        controls.append(page_button(display_page - 1))

    controls.append(page_button(display_page, active=True))

    if display_page < last_page:
        controls.append(page_button(display_page + 1))

    if display_page < last_page - 1:
        controls.append(ft.Text("...", font_family=FONT_FAMILY, size=SIZE_BODY, color=TEXT_SECONDARY))

    controls.extend([
        nav_button(ft.Icons.CHEVRON_RIGHT_ROUNDED, current_page + 1, current_page >= total_pages - 1, "Trang Sau"),
        nav_button(ft.Icons.KEYBOARD_DOUBLE_ARROW_RIGHT_ROUNDED, total_pages - 1, current_page >= total_pages - 1, "Trang Cuối"),
    ])

    return controls


def request_page_update(page: ft.Page, retries=(0.0, 0.05, 0.2)) -> None:
    page_key = id(page)
    with _UPDATE_LOCK:
        if page_key in _UPDATE_PENDING:
            return
        _UPDATE_PENDING.add(page_key)

    def update_after():
        try:
            for attempt_delay in retries:
                if attempt_delay > 0:
                    time.sleep(attempt_delay)
                try:
                    page.update()
                except Exception:
                    try:
                        page.schedule_update()
                    except Exception:
                        pass
        finally:
            with _UPDATE_LOCK:
                _UPDATE_PENDING.discard(page_key)

    try:
        page.run_thread(update_after)
    except Exception:
        threading.Thread(target=update_after, daemon=True).start()

