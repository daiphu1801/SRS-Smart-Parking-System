"""
staff_page.py - Employee administration.
"""
import flet as ft

from core import api_client
from core.design_tokens import *
from pages.admin.admin_ui import (
    clean_body, close_dialog, confirm_dialog, dropdown, int_value, message_row, page_items,
    show_dialog, show_snack, text_field, validate_required,
    validate_required_int, list_of
)


def build_staff_page(page: ft.Page) -> ft.Control:
    loading = ft.ProgressRing(width=24, height=24, stroke_width=2, color=PRIMARY, visible=True)
    search_field = text_field("Họ Tên", expand=True)
    employee_phone_filter = text_field("Số Điện Thoại", width=170)
    employee_col = ft.Column(spacing=0, controls=[message_row("Đang Tải...")])

    def header_row(headers, expands):
        return ft.Container(
            bgcolor=BG_ELEVATED,
            border_radius=RADIUS_MD,
            padding=ft.Padding(PAD_MD, PAD_SM, PAD_MD, PAD_SM),
            content=ft.Row(controls=[
                ft.Text(h, font_family=FONT_FAMILY, size=SIZE_CAPTION,
                        weight=W_MEDIUM, color=TEXT_SECONDARY, expand=e)
                for h, e in zip(headers, expands)
            ]),
        )

    def table_panel(content):
        return ft.Container(
            bgcolor=BG_CARD,
            border_radius=RADIUS_MD,
            border=border_all(1, BORDER),
            padding=PAD_LG,
            content=content,
        )

    def icon_button(icon, tooltip, callback):
        return ft.OutlinedButton(
            content="",
            icon=icon,
            tooltip=tooltip,
            on_click=callback,
            style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
        )

    def employee_row(emp: dict):
        is_deleted = emp.get("deleted", False)
        status_text = "Đã Xóa" if is_deleted else "Hoạt Động"
        status_color = DANGER if is_deleted else SUCCESS

        return ft.Container(
            padding=ft.Padding(PAD_MD, PAD_SM + 2, PAD_MD, PAD_SM + 2),
            border=border_only(bottom=ft.BorderSide(1, BORDER)),
            content=ft.Row(controls=[
                ft.Text(str(emp.get("id", "")), font_family=FONT_FAMILY, size=SIZE_SMALL,
                        color=TEXT_DISABLED, expand=1),
                ft.Text(ui_title(emp.get("full_name", "-")), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=3),
                ft.Text(emp.get("phone", "-"), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, color=TEXT_SECONDARY, expand=2),
                ft.Text(status_text, font_family=FONT_FAMILY,
                        size=SIZE_SMALL, color=status_color, expand=2),
                ft.Row(expand=1, spacing=6, controls=[
                    icon_button(ft.Icons.DELETE_ROUNDED, "Xóa Nhân Viên", lambda _, row=emp: delete_employee(row)) if not is_deleted else ft.Container()
                ]),
            ]),
        )

    def open_employee_dialog():
        name_field = text_field("Họ Tên", "")
        phone_field = text_field("Số Điện Thoại", "")
        
        dialog_ref = {"dialog": None}

        def _open():
            try:
                roles = list_of(api_client.admin_get_roles())
            except Exception as ex:
                roles = []
                print(f"DEBUG: Failed to fetch roles: {ex}")

            role_options = [(str(r["id"]), r.get("role_name", f"Role {r['id']}")) for r in roles]
            role_val = role_options[0][0] if role_options else ""

            role_field = dropdown("Vai Trò", role_options, role_val, width=220)

            def submit(_):
                required = [name_field, phone_field, role_field]
                if not validate_required(page, *required):
                    return
                if not validate_required_int(page, role_field):
                    return

                def save():
                    try:
                        api_client.admin_create_employee(
                            name_field.value.strip(),
                            phone_field.value.strip(),
                            role_id=int_value(role_field.value)
                        )
                        close_dialog(page, dialog_ref["dialog"])
                        load()
                    except Exception:
                        pass
                page.run_thread(save)

            controls = [name_field, phone_field, role_field]
            dialog_ref["dialog"] = show_dialog(
                page,
                "Thêm Nhân Viên",
                controls,
                [
                    ft.OutlinedButton(content="Hủy", on_click=lambda _: close_dialog(page, dialog_ref["dialog"]),
                                      style=outlined_button_style()),
                    ft.ElevatedButton(content="Lưu", icon=ft.Icons.SAVE_ROUNDED, on_click=submit,
                                      style=filled_button_style()),
                ],
            )

        page.run_thread(_open)

    def delete_employee(emp: dict):
        def do_delete():
            def run():
                try:
                    api_client.admin_delete_employee(emp["id"])
                    load()
                except Exception:
                    pass
            page.run_thread(run)

        confirm_dialog(page, "Xóa Nhân Viên", "Xác Nhận Xóa Nhân Viên Này?", do_delete)

    def load():
        loading.visible = True
        request_page_update(page)

        def fetch():
            employee_col.controls = [header_row(["ID", "Họ Tên", "Số Điện Thoại", "Trạng Thái", "Thao Tác"], [1, 3, 2, 2, 1])]
            try:
                employees, _, _ = page_items(api_client.admin_get_employees(
                    page=0, size=100,
                    full_name=search_field.value.strip() or None,
                    phone=employee_phone_filter.value.strip() or None,
                ))
                employee_col.controls.extend(employee_row(item) for item in employees)
                if not employees:
                    employee_col.controls.append(message_row())
            except Exception as ex:
                employee_col.controls.append(message_row(f"Lỗi Tải Nhân Viên: {ex}", DANGER))

            loading.visible = False
            request_page_update(page)

        page.run_thread(fetch)

    search_field.on_submit = lambda _: load()
    employee_phone_filter.on_submit = lambda _: load()
    load()

    return ft.Column(
        expand=True,
        spacing=PAD_LG,
        controls=[
            ft.Row(controls=[
                ft.Text("Nhân Sự", font_family=FONT_FAMILY,
                        size=SIZE_H2, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=True),
                loading,
                ft.ElevatedButton(content="Thêm Nhân Viên", icon=ft.Icons.PERSON_ADD_ROUNDED,
                                  on_click=lambda _: open_employee_dialog(), style=filled_button_style()),
                ft.OutlinedButton(content="Làm Mới", icon=ft.Icons.REFRESH_ROUNDED,
                                  on_click=lambda _: load(), style=outlined_button_style()),
            ]),
            ft.Row(controls=[
                search_field,
                ft.ElevatedButton(content="Tìm Kiếm", icon=ft.Icons.SEARCH_ROUNDED,
                                  on_click=lambda _: load(), style=filled_button_style()),
            ]),
            ft.Row(
                scroll=ft.ScrollMode.AUTO,
                spacing=PAD_SM,
                controls=[
                    employee_phone_filter,
                ],
            ),
            ft.Text("Nhân Viên", font_family=FONT_FAMILY,
                    size=SIZE_H3, weight=W_MEDIUM, color=TEXT_PRIMARY),
            table_panel(employee_col),
        ],
    )
