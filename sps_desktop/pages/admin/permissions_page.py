"""
permissions_page.py - Role permission matrix.
"""
import threading

import flet as ft

from core import api_client
from core.design_tokens import *
from pages.admin.admin_ui import (
    close_dialog, confirm_dialog, message_row, show_dialog, show_snack, text_field,
    validate_required,
)


def _data(resp):
    if isinstance(resp, dict):
        return resp.get("data", resp)
    return resp


def _as_list(value):
    value = _data(value)
    if isinstance(value, list):
        return value
    if isinstance(value, dict):
        for key in ("content", "items", "roles"):
            items = value.get(key)
            if isinstance(items, list):
                return items
    return []


def _item_id(item: dict):
    return item.get("id") or item.get("role_id") or item.get("roleId")


def _role_name(role: dict) -> str:
    return role.get("role_name") or role.get("roleName") or role.get("name") or ""


def _role_description(role: dict):
    return role.get("description")


def _permission_key(permission: dict):
    func_id = permission.get("func_id") or permission.get("funcId")
    action_id = permission.get("action_id") or permission.get("actionId")
    if func_id is None or action_id is None:
        return None
    return int(func_id), int(action_id)


def build_permissions_page(page: ft.Page) -> ft.Control:
    state = {
        "roles": [],
        "role_index": {},
        "functions": [],
        "actions": [],
        "selected": set(),
        "role_id": None,
        "role_name": "",
        "description": None,
    }
    checkbox_refs: dict[tuple[int, int], ft.Checkbox] = {}

    loading = ft.ProgressRing(width=24, height=24, stroke_width=2, color=PRIMARY, visible=True)
    role_search_field = text_field("Tìm Vai Trò", width=220)
    status_text = ft.Text(
        "Đang Tải Quyền...",
        font_family=FONT_FAMILY,
        size=SIZE_SMALL,
        color=TEXT_SECONDARY,
    )
    matrix_col = ft.Column(spacing=0, controls=[message_row("Chọn Vai Trò Để Xem Quyền.")])
    matrix_panel = ft.Container(
        visible=False,
        bgcolor=BG_CARD,
        border_radius=RADIUS_MD,
        border=border_all(1, BORDER),
        padding=PAD_LG,
        content=matrix_col,
    )

    role_dropdown = ft.Dropdown(
        label="Vai Trò",
        width=320,
        height=48,
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
        content_padding=ft.Padding(16, 0, 16, 0),
        text_style=ft.TextStyle(color=TEXT_PRIMARY, font_family=FONT_FAMILY, size=SIZE_BODY),
        label_style=ft.TextStyle(color=TEXT_SECONDARY, font_family=FONT_FAMILY, size=SIZE_SMALL),
    )

    def _show_snack(message: str, color: str = SUCCESS):
        page.snack_bar = ft.SnackBar(
            ft.Text(message, font_family=FONT_FAMILY, color=WHITE),
            bgcolor=color,
            shape=ft.RoundedRectangleBorder(radius=RADIUS_MD),
        )
        page.snack_bar.open = True

    def _set_busy(is_busy: bool, lock_role: bool = False):
        loading.visible = is_busy
        role_dropdown.disabled = is_busy and lock_role
        save_btn.disabled = is_busy or state["role_id"] is None
        select_all_btn.disabled = is_busy or not state["functions"] or not state["actions"]
        clear_btn.disabled = select_all_btn.disabled

    def _set_status(message: str, color: str = TEXT_SECONDARY):
        status_text.value = message
        status_text.color = color
        status_text.visible = bool(message)

    def _selected_count_label():
        return f"{len(state['selected'])} Quyền Đang Bật."

    def _on_permission_change(event, key: tuple[int, int]):
        if event.control.value:
            state["selected"].add(key)
        else:
            state["selected"].discard(key)
        _set_status(_selected_count_label())
        request_page_update(page)

    def _permission_checkbox(func_id: int, action_id: int, tooltip: str) -> ft.Checkbox:
        key = (func_id, action_id)
        checkbox = ft.Checkbox(
            value=key in state["selected"],
            fill_color={
                ft.ControlState.DEFAULT: WHITE,
                ft.ControlState.SELECTED: PRIMARY,
                ft.ControlState.HOVERED: WHITE,
            },
            check_color=WHITE,
            active_color=PRIMARY,
            hover_color=WHITE,
            focus_color=WHITE,
            overlay_color=WHITE,
            border_side=ft.BorderSide(1, PRIMARY),
            mouse_cursor=ft.MouseCursor.CLICK,
            tooltip=tooltip,
            on_change=lambda e, k=key: _on_permission_change(e, k),
        )
        checkbox_refs[key] = checkbox
        return checkbox

    def _build_matrix():
        checkbox_refs.clear()
        functions = state["functions"]
        actions = state["actions"]
        has_matrix = bool(functions and actions)
        if not has_matrix:
            matrix_panel.visible = True
            matrix_col.controls = [message_row("Không Có Dữ Liệu Quyền.", DANGER)]
            _set_status("Không Có Dữ Liệu Quyền.", DANGER)
            return

        matrix_panel.visible = True
        rows = []
        action_headers = [
            ft.Container(
                width=120,
                alignment=ft.Alignment(0, 0),
                content=ft.Text(
                    ui_title(action.get("name") or action.get("code")),
                    font_family=FONT_FAMILY,
                    size=SIZE_CAPTION,
                    weight=W_MEDIUM,
                    color=TEXT_SECONDARY,
                    text_align=ft.TextAlign.CENTER,
                ),
            )
            for action in actions
        ]
        rows.append(
            ft.Container(
                bgcolor=BG_ELEVATED,
                border_radius=RADIUS_MD,
                padding=ft.Padding(PAD_MD, PAD_SM, PAD_MD, PAD_SM),
                content=ft.Row(
                    spacing=0,
                    controls=[
                        ft.Text(
                            "Chức Năng",
                            font_family=FONT_FAMILY,
                            size=SIZE_CAPTION,
                            weight=W_MEDIUM,
                            color=TEXT_SECONDARY,
                            width=320,
                        ),
                        *action_headers,
                    ],
                ),
            )
        )

        for function in functions:
            func_id = function.get("id")
            if func_id is None:
                continue
            func_id = int(func_id)
            func_label = ui_title(function.get("name") or function.get("code"))
            cells = []
            for action in actions:
                action_id = action.get("id")
                if action_id is None:
                    continue
                action_id = int(action_id)
                action_label = ui_title(action.get("name") or action.get("code"))
                cells.append(
                    ft.Container(
                        width=120,
                        alignment=ft.Alignment(0, 0),
                        content=_permission_checkbox(
                            func_id,
                            action_id,
                            f"{func_label} {action_label}",
                        ),
                    )
                )

            if not cells:
                continue
            rows.append(
                ft.Container(
                    padding=ft.Padding(PAD_MD, PAD_SM, PAD_MD, PAD_SM),
                    border=border_only(bottom=ft.BorderSide(1, BORDER)),
                    content=ft.Row(
                        spacing=0,
                        controls=[
                            ft.Text(
                                func_label,
                                font_family=FONT_FAMILY,
                                size=SIZE_SMALL,
                                weight=W_MEDIUM,
                                color=TEXT_PRIMARY,
                                width=320,
                            ),
                            *cells,
                        ],
                    ),
                )
            )

        matrix_col.controls = rows or [message_row("Không Có Dữ Liệu Quyền.", DANGER)]
        _set_status(_selected_count_label())

    def _apply_role_detail(detail: dict):
        state["role_id"] = detail.get("role_id") or detail.get("roleId") or state["role_id"]
        state["role_name"] = detail.get("role_name") or detail.get("roleName") or state["role_name"]
        if detail.get("description") is not None:
            state["description"] = detail.get("description")
        selected = set()
        for permission in detail.get("permissions") or []:
            key = _permission_key(permission)
            if key is not None:
                selected.add(key)
        state["selected"] = selected
        _build_matrix()

    def _load_role_detail(role_id: int):
        _set_busy(True)
        _set_status("Đang Tải Quyền...")
        request_page_update(page)

        def fetch():
            try:
                role = state["role_index"].get(role_id, {})
                state["role_id"] = role_id
                state["role_name"] = _role_name(role)
                state["description"] = _role_description(role)
                detail = _data(api_client.admin_get_role(role_id))
                _apply_role_detail(detail if isinstance(detail, dict) else {})
            except Exception:
                matrix_panel.visible = True
                matrix_col.controls = [message_row("Lỗi Tải Quyền.", DANGER)]
                _set_status("Lỗi Tải Quyền.", DANGER)
            _set_busy(False)
            request_page_update(page)

        page.run_thread(fetch)

    def _on_role_select(event):
        raw_value = getattr(event, "data", None) or getattr(getattr(event, "control", None), "value", None) or role_dropdown.value
        if not raw_value:
            return
        try:
            role_id = int(raw_value)
        except (TypeError, ValueError):
            return
        role_dropdown.value = str(role_id)
        state["role_id"] = role_id
        role = state["role_index"].get(role_id, {})
        state["role_name"] = _role_name(role)
        state["description"] = _role_description(role)
        matrix_panel.visible = True
        matrix_col.controls = [message_row("Đang Tải Quyền...")]
        _set_status("Đang Tải Quyền...")
        request_page_update(page)
        _load_role_detail(role_id)

    role_dropdown.on_select = _on_role_select

    def _load_all():
        previous_role_id = state.get("role_id")
        _set_busy(True, lock_role=True)
        _set_status("Đang Tải Quyền...")
        matrix_panel.visible = True
        matrix_col.controls = [message_row("Đang Tải Quyền...")]
        role_dropdown.options = []
        role_dropdown.value = None
        request_page_update(page)

        def fetch():
            try:
                roles = _as_list(api_client.admin_get_roles(role_search_field.value.strip() or None))
                roles = sorted(roles, key=lambda role: _role_name(role).lower())
                meta = _data(api_client.admin_get_role_functions_actions())
                state["functions"] = _as_list(meta.get("functions", []) if isinstance(meta, dict) else [])
                state["actions"] = _as_list(meta.get("actions", []) if isinstance(meta, dict) else [])
                state["roles"] = roles
                state["role_index"] = {int(_item_id(role)): role for role in roles if _item_id(role) is not None}

                role_dropdown.options = [
                    ft.dropdown.Option(
                        key=str(_item_id(role)),
                        text=ui_title(_role_name(role)),
                        content=ft.Text(
                            ui_title(_role_name(role)),
                            font_family=FONT_FAMILY,
                            size=SIZE_BODY,
                            color=TEXT_PRIMARY,
                            weight=W_REGULAR,
                        ),
                    )
                    for role in roles
                    if _item_id(role) is not None
                ]

                if not role_dropdown.options:
                    state["role_id"] = None
                    matrix_panel.visible = True
                    matrix_col.controls = [message_row("Không Tìm Thấy Vai Trò.", DANGER)]
                    _set_status("Không Tìm Thấy Vai Trò.", DANGER)
                    _set_busy(False)
                    request_page_update(page)
                    return

                option_ids = {int(option.key) for option in role_dropdown.options}
                if previous_role_id in option_ids:
                    selected_role_id = int(previous_role_id)
                else:
                    selected_role_id = int(role_dropdown.options[0].key)
                role_dropdown.value = str(selected_role_id)
                state["role_id"] = selected_role_id
                role = state["role_index"].get(selected_role_id, {})
                state["role_name"] = _role_name(role)
                state["description"] = _role_description(role)
                detail = _data(api_client.admin_get_role(selected_role_id))
                _apply_role_detail(detail if isinstance(detail, dict) else {})
            except Exception:
                matrix_panel.visible = True
                matrix_col.controls = [message_row("Lỗi Tải Quyền.", DANGER)]
                _set_status("Lỗi Tải Quyền.", DANGER)
            _set_busy(False)
            request_page_update(page)

        page.run_thread(fetch)

    def _set_all(value: bool):
        all_keys = {
            (int(function["id"]), int(action["id"]))
            for function in state["functions"]
            for action in state["actions"]
            if function.get("id") is not None and action.get("id") is not None
        }
        state["selected"] = all_keys if value else set()
        for key, checkbox in checkbox_refs.items():
            checkbox.value = key in state["selected"]
        _set_status(_selected_count_label())
        request_page_update(page)

    def _save_permissions(_):
        role_id = state["role_id"]
        if role_id is None:
            return
        _set_busy(True)
        _set_status("Đang Lưu Quyền...")
        request_page_update(page)

        permissions = [
            {"func_id": func_id, "action_id": action_id}
            for func_id, action_id in sorted(state["selected"])
        ]

        def save():
            try:
                api_client.admin_update_role(
                    role_id,
                    state["role_name"],
                    state["description"],
                    permissions,
                )
                _set_status(_selected_count_label())
            except Exception:
                _set_status("Lỗi Lưu Quyền.", DANGER)
            _set_busy(False)
            request_page_update(page)

        page.run_thread(save)

    def _current_permissions():
        return [
            {"func_id": func_id, "action_id": action_id}
            for func_id, action_id in sorted(state["selected"])
        ]

    def _open_role_dialog(role: dict | None = None):
        is_update = role is not None
        role = role or {}
        role_id = _item_id(role) if is_update else None
        name_field = text_field("Tên Vai Trò", _role_name(role) if is_update else "")
        desc_field = text_field("Mô Tả", (_role_description(role) or "") if is_update else "")
        dialog_ref = {"dialog": None}

        def submit(_):
            if not validate_required(page, name_field, desc_field):
                return

            def save():
                try:
                    if is_update:
                        api_client.admin_update_role(
                            int(role_id),
                            name_field.value.strip(),
                            desc_field.value.strip(),
                            _current_permissions(),
                        )
                        message = "Đã Cập Nhật Vai Trò."
                    else:
                        api_client.admin_create_role(
                            name_field.value.strip(),
                            desc_field.value.strip(),
                            [],
                        )
                        message = "Đã Tạo Vai Trò."
                    close_dialog(page, dialog_ref["dialog"])
                    _load_all()
                except Exception:
                    pass
            page.run_thread(save)

        dialog_ref["dialog"] = show_dialog(
            page,
            "Sửa Vai Trò" if is_update else "Thêm Vai Trò",
            [name_field, desc_field],
            [
                ft.OutlinedButton(content="Hủy", on_click=lambda _: close_dialog(page, dialog_ref["dialog"]),
                                  style=outlined_button_style()),
                ft.ElevatedButton(content="Lưu", icon=ft.Icons.SAVE_ROUNDED, on_click=submit,
                                  style=filled_button_style()),
            ],
        )

    def _delete_current_role(_):
        role_id = state["role_id"]
        if role_id is None:
            return

        def do_delete():
            def run():
                try:
                    api_client.admin_delete_role(role_id)
                    _load_all()
                except Exception:
                    pass
            page.run_thread(run)

        confirm_dialog(page, "Xóa Vai Trò", "Xác Nhận Xóa Vai Trò Đang Chọn?", do_delete)

    def _hard_delete_current_role(_):
        role_id = state["role_id"]
        if role_id is None:
            return

        def do_delete():
            def run():
                try:
                    api_client.admin_hard_delete_role(role_id)
                    _load_all()
                except Exception:
                    _load_all()
            page.run_thread(run)

        confirm_dialog(page, "Xóa Trực Tiếp Vai Trò", "Xác Nhận XÓA TRỰC TIẾP Vai Trò Đang Chọn khỏi CSDL?", do_delete)

    select_all_btn = ft.OutlinedButton(
        content="Chọn Tất Cả",
        icon=ft.Icons.DONE_ALL_ROUNDED,
        on_click=lambda _: _set_all(True),
        style=outlined_button_style(ft.Padding(12, 10, 12, 10)),
    )
    clear_btn = ft.OutlinedButton(
        content="Bỏ Chọn",
        icon=ft.Icons.REMOVE_DONE_ROUNDED,
        on_click=lambda _: _set_all(False),
        style=outlined_button_style(ft.Padding(12, 10, 12, 10)),
    )
    save_btn = ft.ElevatedButton(
        content="Lưu Quyền",
        icon=ft.Icons.SAVE_ROUNDED,
        on_click=_save_permissions,
        style=filled_button_style(ft.Padding(16, 10, 16, 10)),
    )
    create_role_btn = ft.ElevatedButton(
        content="Thêm Vai Trò",
        icon=ft.Icons.ADD_ROUNDED,
        on_click=lambda _: _open_role_dialog(),
        style=filled_button_style(ft.Padding(16, 10, 16, 10)),
    )
    edit_role_btn = ft.OutlinedButton(
        content="Sửa Vai Trò",
        icon=ft.Icons.EDIT_ROUNDED,
        on_click=lambda _: _open_role_dialog(state["role_index"].get(state["role_id"], {})),
        style=outlined_button_style(ft.Padding(12, 10, 12, 10)),
    )
    delete_role_btn = ft.OutlinedButton(
        content="Xóa Vai Trò",
        icon=ft.Icons.DELETE_ROUNDED,
        on_click=_delete_current_role,
        style=outlined_button_style(ft.Padding(12, 10, 12, 10)),
    )
    hard_delete_role_btn = ft.OutlinedButton(
        content="Xóa Trực Tiếp",
        icon=ft.Icons.DELETE_FOREVER_ROUNDED,
        on_click=_hard_delete_current_role,
        style=outlined_button_style(ft.Padding(12, 10, 12, 10)),
    )

    _set_busy(True)
    _load_all()
    role_search_field.on_submit = lambda _: _load_all()

    return ft.Column(
        expand=True,
        spacing=PAD_LG,
        controls=[
            ft.Row(
                controls=[
                    ft.Icon(ft.Icons.ADMIN_PANEL_SETTINGS_ROUNDED, color=PRIMARY, size=24),
                    ft.Text(
                        "Phân Quyền",
                        font_family=FONT_FAMILY,
                        size=SIZE_H2,
                        weight=W_MEDIUM,
                        color=TEXT_PRIMARY,
                        expand=True,
                    ),
                    loading,
                    create_role_btn,
                    edit_role_btn,
                    delete_role_btn,
                    hard_delete_role_btn,
                    ft.OutlinedButton(
                        content="Làm Mới",
                        icon=ft.Icons.REFRESH_ROUNDED,
                        on_click=lambda _: _load_all(),
                        style=outlined_button_style(ft.Padding(12, 10, 12, 10)),
                    ),
                    save_btn,
                ],
            ),
            ft.Text(
                "Cấu Hình Quyền Cho Vai Trò.",
                font_family=FONT_FAMILY,
                size=SIZE_SMALL,
                color=TEXT_SECONDARY,
            ),
            ft.Row(
                spacing=PAD_MD,
                controls=[
                    role_search_field,
                    role_dropdown,
                    select_all_btn,
                    clear_btn,
                    ft.Container(expand=True),
                    status_text,
                ],
            ),
            matrix_panel,
        ],
    )
