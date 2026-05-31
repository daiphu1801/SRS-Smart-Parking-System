"""
admin_ui.py - Small shared helpers for admin CRUD pages.
"""
import flet as ft

from core.design_tokens import *

def request_page_update(page: ft.Page):
    try:
        page.update()
    except Exception:
        pass


def data_of(resp):
    if isinstance(resp, dict):
        return resp.get("data", resp)
    return resp


def list_of(resp):
    data = data_of(resp)
    if isinstance(data, list):
        return data
    if isinstance(data, dict):
        for key in ("content", "items", "roles", "functions", "actions"):
            value = data.get(key)
            if isinstance(value, list):
                return value
    return []


def page_items(resp):
    data = data_of(resp)
    if isinstance(data, dict):
        return data.get("content", []), data.get("totalElements", 0), data.get("totalPages", 1)
    if isinstance(data, list):
        return data, len(data), 1
    return [], 0, 1


def clean_body(body: dict) -> dict:
    return {key: value for key, value in body.items() if value not in (None, "")}


def int_value(value, default=None):
    try:
        if value in (None, ""):
            return default
        return int(value)
    except (TypeError, ValueError):
        return default


def float_value(value, default=None):
    try:
        if value in (None, ""):
            return default
        return float(value)
    except (TypeError, ValueError):
        return default


def bool_value(value, default=None):
    if value in (None, ""):
        return default
    return str(value).upper() in ("TRUE", "1", "YES", "ACTIVE", "ONLINE")


def text_field(label: str, value: str = "", *, number: bool = False, password: bool = False,
               expand=False, width=None) -> ft.TextField:
    return ft.TextField(
        label=label,
        value="" if value is None else str(value),
        password=password,
        can_reveal_password=password,
        keyboard_type=ft.KeyboardType.NUMBER if number else ft.KeyboardType.TEXT,
        border_color=BORDER,
        focused_border_color=PRIMARY,
        label_style=ft.TextStyle(color=TEXT_SECONDARY, font_family=FONT_FAMILY, size=SIZE_SMALL),
        text_style=ft.TextStyle(color=TEXT_PRIMARY, font_family=FONT_FAMILY, size=SIZE_BODY),
        bgcolor=BG_CARD,
        border_radius=RADIUS_MD,
        cursor_color=PRIMARY,
        height=48,
        expand=expand,
        width=width,
        content_padding=ft.Padding(14, 0, 14, 0),
    )


def dropdown(label: str, options: list[tuple[str, str]], value: str = "", *, width=180, expand=False) -> ft.Dropdown:
    return ft.Dropdown(
        label=label,
        value="" if value is None else str(value),
        options=[
            ft.dropdown.Option(
                key=str(key),
                text=ui_title(label_text),
                content=ft.Text(
                    ui_title(label_text),
                    font_family=FONT_FAMILY,
                    size=SIZE_BODY,
                    color=TEXT_PRIMARY,
                    weight=W_REGULAR,
                ),
            )
            for key, label_text in options
        ],
        border_color=BORDER,
        focused_border_color=PRIMARY,
        border_radius=RADIUS_MD,
        bgcolor=BG_CARD,
        color=TEXT_PRIMARY,
        fill_color=WHITE,
        menu_style=ft.MenuStyle(
            bgcolor=WHITE,
            side=ft.BorderSide(1, BORDER),
            shape=ft.RoundedRectangleBorder(radius=RADIUS_MD),
        ),
        focused_border_width=1,
        content_padding=ft.Padding(14, 0, 14, 0),
        text_style=ft.TextStyle(color=TEXT_PRIMARY, font_family=FONT_FAMILY, size=SIZE_BODY),
        label_style=ft.TextStyle(color=TEXT_SECONDARY, font_family=FONT_FAMILY, size=SIZE_SMALL),
        trailing_icon=ft.Icon(ft.Icons.EXPAND_MORE_ROUNDED, color=PRIMARY),
        width=width,
        expand=expand,
    )


def show_snack(page: ft.Page, message: str, color: str = PRIMARY):
    sb = ft.SnackBar(
        content=ft.Text(message, font_family=FONT_FAMILY, color=WHITE),
        bgcolor=color,
        shape=ft.RoundedRectangleBorder(radius=RADIUS_MD),
    )
    page.overlay.append(sb)
    sb.open = True
    request_page_update(page)


def message_row(message: str = "Không Có Kết Quả.", color: str = TEXT_DISABLED) -> ft.Container:
    return ft.Container(
        bgcolor=WHITE,
        height=72,
        padding=PAD_LG,
        alignment=ft.Alignment(0, 0),
        content=ft.Text(
            message,
            font_family=FONT_FAMILY,
            size=SIZE_SMALL,
            color=color,
            text_align=ft.TextAlign.CENTER,
        ),
    )


def clear_field_errors(*controls: ft.Control):
    for control in controls:
        if hasattr(control, "error_text"):
            control.error_text = None


def validate_required(page: ft.Page, *controls: ft.Control) -> bool:
    ok = True
    for control in controls:
        value = getattr(control, "value", None)
        if value is None or str(value).strip() == "":
            if hasattr(control, "error_text"):
                control.error_text = "Bắt Buộc"
            ok = False
        elif hasattr(control, "error_text"):
            control.error_text = None
    request_page_update(page)
    return ok


def validate_required_int(page: ft.Page, *controls: ft.Control) -> bool:
    ok = validate_required(page, *controls)
    for control in controls:
        value = getattr(control, "value", None)
        if value is not None and str(value).strip() and int_value(value) is None:
            if hasattr(control, "error_text"):
                control.error_text = "Phải Là Số Nguyên"
            ok = False
    request_page_update(page)
    return ok


def validate_required_float(page: ft.Page, *controls: ft.Control) -> bool:
    ok = validate_required(page, *controls)
    for control in controls:
        value = getattr(control, "value", None)
        if value is not None and str(value).strip() and float_value(value) is None:
            if hasattr(control, "error_text"):
                control.error_text = "Phải Là Số"
            ok = False
    request_page_update(page)
    return ok


def filter_text(label: str, value: str = "", *, number: bool = False, width=180) -> ft.TextField:
    return text_field(label, value, number=number, width=width)


def filter_dropdown(label: str, options: list[tuple[str, str]], value: str = "", *, width=180) -> ft.Dropdown:
    return dropdown(label, options, value, width=width)


def close_dialog(page: ft.Page, dialog: ft.AlertDialog):
    dialog.open = False
    request_page_update(page)


def show_dialog(page: ft.Page, title: str, controls: list[ft.Control], actions: list[ft.Control], *, width=460):
    dialog = ft.AlertDialog(
        modal=True,
        title=ft.Text(title, font_family=FONT_FAMILY, size=SIZE_H3, weight=W_MEDIUM, color=TEXT_PRIMARY),
        bgcolor=BG_CARD,
        shape=ft.RoundedRectangleBorder(radius=RADIUS_MD),
        content=ft.Container(
            width=width,
            content=ft.Column(tight=True, spacing=PAD_MD, controls=controls),
        ),
        actions=actions,
        actions_alignment=ft.MainAxisAlignment.END,
    )
    page.overlay.append(dialog)
    dialog.open = True
    request_page_update(page)
    return dialog


def confirm_dialog(page: ft.Page, title: str, message: str, on_confirm):
    dialog_ref = {"dialog": None}

    def confirm(_):
        dialog_ref["dialog"].open = False
        request_page_update(page)
        on_confirm()

    dialog = show_dialog(
        page,
        title,
        [ft.Text(message, font_family=FONT_FAMILY, size=SIZE_BODY, color=TEXT_PRIMARY)],
        [
            ft.OutlinedButton(
                content="Hủy",
                on_click=lambda _: close_dialog(page, dialog_ref["dialog"]),
                style=outlined_button_style(),
            ),
            ft.ElevatedButton(
                content="Xác Nhận",
                icon=ft.Icons.CHECK_ROUNDED,
                on_click=confirm,
                style=filled_button_style(),
            ),
        ],
    )
    dialog_ref["dialog"] = dialog
    return dialog
