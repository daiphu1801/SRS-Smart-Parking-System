"""
customers_page.py - GET /api/v1/admin/customer-groups + /api/v1/admin/customers
"""
import threading
from datetime import datetime
import flet as ft
from core.design_tokens import *
from core import api_client
from pages.admin.admin_ui import (
    clean_body, close_dialog, confirm_dialog, dropdown, int_value, list_of,
    message_row, show_dialog, show_snack, text_field, validate_required, validate_required_int,
    request_page_update
)


def build_customers_page(page: ft.Page) -> ft.Control:
    def _now_iso():
        return datetime.now().replace(microsecond=0).isoformat()

    def _current_account_id():
        return api_client.get_account_id() or ""

    search_field = ft.TextField(
        hint_text="Tìm Nhóm Khách Hàng...",
        border_color=BORDER, focused_border_color=PRIMARY,
        hint_style=ft.TextStyle(color=TEXT_DISABLED, font_family=FONT_FAMILY),
        text_style=ft.TextStyle(color=TEXT_PRIMARY, font_family=FONT_FAMILY),
        bgcolor=BG_CARD, border_radius=RADIUS_MD, cursor_color=PRIMARY, expand=True,
        height=44,
        content_padding=ft.Padding(16, 0, 16, 0),
    )
    profile_code_filter = text_field("Mã Profile", width=170)
    profile_name_filter = text_field("Tên Profile", width=190)
    group_id_filter = text_field("ID Nhóm", number=True, width=130)
    group_code_filter = text_field("Mã Nhóm", width=150)
    profile_id_filter = text_field("ID Profile", number=True, width=140)
    sync_filter = dropdown("Đồng Bộ", [("", "Tất Cả"), ("true", "Có"), ("false", "Không")], "", width=150)
    master_id_filter = text_field("ID Master", number=True, width=140)
    master_name_filter = text_field("Tên Master", width=180)
    master_phone_filter = text_field("SĐT Master", width=170)
    
    member_name_filter = text_field("Tên Thành Viên", width=180)
    member_phone_filter = text_field("SĐT Thành Viên", width=170)
    member_address_filter = text_field("Địa Chỉ Thành Viên", width=180)
    member_account_filter = text_field("ID Tài Khoản", number=True, width=150)

    # All Customers Filters
    all_name_filter = text_field("Họ Tên", width=180)
    all_phone_filter = text_field("Số Điện Thoại", width=170)
    all_address_filter = text_field("Địa Chỉ", width=180)
    all_account_filter = text_field("ID Tài Khoản", number=True, width=150)
    all_deleted_filter = dropdown("Trạng Thái", [("", "Tất Cả"), ("false", "Hoạt Động"), ("true", "Đã Xóa")], "", width=150)

    loading   = ft.ProgressRing(width=24, height=24, stroke_width=2, color=PRIMARY, visible=True)
    profile_col = ft.Column(spacing=0, controls=[message_row("Đang Tải...")])
    table_col   = ft.Column(spacing=0, controls=[message_row("Đang Tải...")])
    all_customer_col = ft.Column(spacing=0, controls=[message_row("Đang Tải...")])
    
    page_info  = ft.Text("", font_family=FONT_FAMILY, size=SIZE_CAPTION, color=TEXT_SECONDARY)
    pagination_row = ft.Row(alignment=ft.MainAxisAlignment.CENTER, spacing=8, visible=False)
    
    all_page_info = ft.Text("", font_family=FONT_FAMILY, size=SIZE_CAPTION, color=TEXT_SECONDARY)
    all_pagination_row = ft.Row(alignment=ft.MainAxisAlignment.CENTER, spacing=8, visible=False)
    
    state = {
        "page": 0, "total_pages": 0, 
        "selected_group": None, "selected_group_name": "",
        "all_page": 0, "all_total_pages": 0,
        "member_dialog": None
    }

    HEADERS = ["Mã Nhóm", "Tên Nhóm", "Loại Profile", "Ngày Tạo", "Thao Tác"]
    EXPANDS = [2, 3, 2, 2, 2]
    PHEADERS = ["Mã Profile", "Tên Profile", "Thao Tác"]
    PEXPANDS = [2, 3, 2]

    def _header_row():
        return ft.Container(
            bgcolor=BG_ELEVATED, border_radius=RADIUS_MD,
            padding=ft.Padding(PAD_MD, PAD_SM, PAD_MD, PAD_SM),
            content=ft.Row(controls=[
                ft.Text(h, font_family=FONT_FAMILY, size=SIZE_CAPTION,
                        weight=W_MEDIUM, color=TEXT_SECONDARY, expand=e)
                for h, e in zip(HEADERS, EXPANDS)
            ]),
        )

    def _profile_header_row():
        return ft.Container(
            bgcolor=BG_ELEVATED, border_radius=RADIUS_MD,
            padding=ft.Padding(PAD_MD, PAD_SM, PAD_MD, PAD_SM),
            content=ft.Row(controls=[
                ft.Text(h, font_family=FONT_FAMILY, size=SIZE_CAPTION,
                        weight=W_MEDIUM, color=TEXT_SECONDARY, expand=e)
                for h, e in zip(PHEADERS, PEXPANDS)
            ]),
        )

    def _profile_row(profile: dict) -> ft.Container:
        return ft.Container(
            padding=ft.Padding(PAD_MD, PAD_SM + 2, PAD_MD, PAD_SM + 2),
            border=border_only(bottom=ft.BorderSide(1, BORDER)),
            content=ft.Row(controls=[
                ft.Text(profile.get("profile_code", "-"), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=2),
                ft.Text(ui_title(profile.get("profile_name", "-")), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, color=TEXT_SECONDARY, expand=3),
                ft.Row(expand=2, spacing=6, controls=[
                    ft.OutlinedButton(
                        content="",
                        icon=ft.Icons.EDIT_ROUNDED,
                        tooltip="Sửa Profile",
                        on_click=lambda _, row=profile: open_profile_dialog(row),
                        style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                    ),
                    ft.OutlinedButton(
                        content="",
                        icon=ft.Icons.DELETE_ROUNDED,
                        tooltip="Xóa Profile",
                        on_click=lambda _, row=profile: delete_profile(row),
                        style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                    ),
                ]),
            ]),
        )

    def open_profile_dialog(profile: dict | None = None):
        profile = profile or {}
        profile_id = profile.get("id") or profile.get("profile_id")
        code_field = text_field("Mã Profile", profile.get("profile_code", ""))
        name_field = text_field("Tên Profile", profile.get("profile_name", ""))
        dialog_ref = {"dialog": None}

        def submit(_):
            required = [name_field] if profile_id else [code_field, name_field]
            if not validate_required(page, *required):
                return

            def save():
                try:
                    if profile_id:
                        api_client.admin_update_group_profile(
                            profile_id,
                            name_field.value.strip(),
                            profile_code=code_field.value.strip(),
                        )
                    else:
                        api_client.admin_create_group_profile(code_field.value.strip(), name_field.value.strip())
                    close_dialog(page, dialog_ref["dialog"])
                    load(state["page"])
                except Exception:
                    pass
            page.run_thread(save)

        controls = [code_field, name_field]
        dialog_ref["dialog"] = show_dialog(
            page,
            "Sửa Profile" if profile_id else "Thêm Profile",
            controls,
            [
                ft.OutlinedButton(content="Hủy", on_click=lambda _: close_dialog(page, dialog_ref["dialog"]),
                                  style=outlined_button_style()),
                ft.ElevatedButton(content="Lưu", icon=ft.Icons.SAVE_ROUNDED, on_click=submit,
                                  style=filled_button_style()),
            ],
        )

    def delete_profile(profile: dict):
        profile_id = profile.get("id") or profile.get("profile_id")

        def do_delete():
            def run():
                try:
                    api_client.admin_delete_group_profile(profile_id)
                    load(state["page"])
                except Exception:
                    pass
            page.run_thread(run)

        confirm_dialog(page, "Xóa Profile", "Xác Nhận Xóa Profile Này?", do_delete)


    MHEADERS = ["Họ Tên", "Số Điện Thoại", "Địa Chỉ", "Trạng Thái", "Thao Tác"]
    MEXPANDS = [3, 2, 2, 2, 2]

    def _member_header_row():
        return ft.Container(
            bgcolor=BG_ELEVATED, border_radius=RADIUS_MD,
            padding=ft.Padding(PAD_MD, PAD_SM, PAD_MD, PAD_SM),
            content=ft.Row(controls=[
                ft.Text(h, font_family=FONT_FAMILY, size=SIZE_CAPTION,
                        weight=W_MEDIUM, color=TEXT_SECONDARY, expand=e)
                for h, e in zip(MHEADERS, MEXPANDS)
            ]),
        )

    def load_members(group_id: int, group_name: str):
        state["selected_group"] = group_id
        state["selected_group_name"] = group_name

        member_list = ft.Column(spacing=0)
        
        # Build dialog UI
        dialog_content = ft.Column(
            spacing=PAD_LG,
            controls=[
                ft.Row(controls=[
                    ft.Text(f"Thành Viên - {ui_title(group_name)}", font_family=FONT_FAMILY, size=SIZE_H3, weight=W_MEDIUM, expand=True),
                    ft.ElevatedButton(
                        content="Thêm Khách Hàng",
                        icon=ft.Icons.PERSON_ADD_ROUNDED,
                        on_click=lambda _: open_customer_dialog({"group_id": group_id}),
                        style=filled_button_style(),
                    ),
                ]),
                ft.Row(
                    scroll=ft.ScrollMode.AUTO,
                    spacing=PAD_SM,
                    controls=[
                        member_name_filter,
                        member_phone_filter,
                        member_address_filter,
                        member_account_filter,
                        ft.ElevatedButton(
                            content="Tìm",
                            icon=ft.Icons.SEARCH_ROUNDED,
                            on_click=lambda _: load_members(group_id, group_name),
                            style=filled_button_style(),
                        ),
                    ],
                ),
                _member_header_row(),
                member_list,
            ]
        )
        
        if state["member_dialog"]:
            close_dialog(page, state["member_dialog"])
            
        state["member_dialog"] = show_dialog(
            page,
            "Danh Sách Thành Viên",
            [ft.Container(content=dialog_content, width=800, height=500)],
            [ft.OutlinedButton("Đóng", on_click=lambda _: close_dialog(page, state["member_dialog"]), style=outlined_button_style())],
            width=850
        )
        
        member_list.controls = [message_row("Đang Tải...")]
        request_page_update(page)

        def fetch():
            # The last item in member_col is the "Đang Tải..." placeholder.
            # Build the new rows then replace it atomically.
            new_rows = []
            try:
                resp = api_client.admin_get_customers(
                    group_id=group_id,
                    size=50,
                    full_name=member_name_filter.value.strip() or None,
                    phone=member_phone_filter.value.strip() or None,
                    address=member_address_filter.value.strip() or None,
                    account_id=int_value(member_account_filter.value),
                )
                members = resp.get("data", {}).get("content", [])
                for m in members:
                    new_rows.append(ft.Container(
                        padding=ft.Padding(PAD_MD, PAD_SM + 2, PAD_MD, PAD_SM + 2),
                        border=border_only(bottom=ft.BorderSide(1, BORDER)),
                        content=ft.Row(controls=[
                            ft.Text(ui_title(m.get("full_name", "-")), font_family=FONT_FAMILY,
                                    size=SIZE_SMALL, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=3),
                            ft.Text(m.get("phone", "-"), font_family=FONT_FAMILY,
                                    size=SIZE_SMALL, color=TEXT_SECONDARY, expand=2),
                            ft.Text(ui_title(m.get("address", "-") or "-"), font_family=FONT_FAMILY,
                                    size=SIZE_SMALL, color=TEXT_SECONDARY, expand=2),
                            ft.Text("Đã Xóa" if m.get("deleted") else "Hoạt Động", font_family=FONT_FAMILY,
                                    size=SIZE_SMALL, color=DANGER if m.get("deleted") else SUCCESS, expand=2),
                            ft.Row(expand=2, spacing=6, controls=[
                                ft.OutlinedButton(
                                    content="",
                                    icon=ft.Icons.EDIT_ROUNDED,
                                    tooltip="Sửa Khách Hàng",
                                    on_click=lambda _, row=m: open_customer_dialog(row),
                                    style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                                ),
                                ft.OutlinedButton(
                                    content="",
                                    icon=ft.Icons.DELETE_ROUNDED,
                                    tooltip="Xóa Khách Hàng",
                                    on_click=lambda _, row=m: delete_customer(row),
                                    style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                                ),
                            ]),
                        ]),
                    ))
                if not members:
                    new_rows.append(message_row("Không có thành viên."))
                member_list.controls = new_rows
            except Exception:
                member_list.controls = [message_row("Lỗi tải thành viên.", DANGER)]
            finally:
                request_page_update(page)

        page.run_thread(fetch)

    def _group_row(g: dict) -> ft.Container:
        created = (g.get("created_date") or g.get("created_at") or "")[:10]
        return ft.Container(
            padding=ft.Padding(PAD_MD, PAD_SM + 2, PAD_MD, PAD_SM + 2),
            border=border_only(bottom=ft.BorderSide(1, BORDER)),
            on_click=lambda _, gid=g["id"], gn=g.get("group_name", ""): page.run_thread(load_members, gid, gn),
            ink=True,
            content=ft.Row(controls=[
                ft.Text(g.get("group_code", "-"), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, weight=W_MEDIUM, color=PRIMARY, expand=2),
                ft.Text(ui_title(g.get("group_name", "-")), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, color=TEXT_PRIMARY, expand=3),
                ft.Text(ui_title(g.get("profile_name", "-")), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, color=TEXT_SECONDARY, expand=2),
                ft.Text(created, font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY, expand=2),
                ft.Row(expand=2, spacing=6, controls=[
                    ft.OutlinedButton(
                        content="",
                        icon=ft.Icons.VISIBILITY_ROUNDED,
                        tooltip="Xem Thành Viên",
                        on_click=lambda _, gid=g["id"], gn=g.get("group_name", ""): page.run_thread(load_members, gid, gn),
                        style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                    ),
                    ft.OutlinedButton(
                        content="",
                        icon=ft.Icons.EDIT_ROUNDED,
                        tooltip="Sửa Nhóm",
                        on_click=lambda _, row=g: open_group_dialog(row),
                        style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                    ),
                    ft.OutlinedButton(
                        content="",
                        icon=ft.Icons.DELETE_ROUNDED,
                        tooltip="Xóa Nhóm",
                        on_click=lambda _, row=g: delete_group(row),
                        style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                    ),
                ]),
            ]),
        )

    def open_group_filter_dialog():
        dialog_ref = {"dialog": None}
        def apply(_):
            close_dialog(page, dialog_ref["dialog"])
            load(0)
            
        dialog_ref["dialog"] = show_dialog(
            page,
            "Bộ Lọc Nhóm Khách Hàng",
            [
                group_id_filter,
                group_code_filter,
                profile_id_filter,
                sync_filter,
                master_id_filter,
                master_name_filter,
                master_phone_filter,
            ],
            [
                ft.OutlinedButton(content="Xóa Lọc", on_click=lambda _: clear_filters(dialog_ref["dialog"]),
                                  style=outlined_button_style()),
                ft.ElevatedButton(content="Áp Dụng", icon=ft.Icons.FILTER_LIST_ROUNDED, on_click=apply,
                                  style=filled_button_style()),
            ],
            width=400,
        )
        
    def clear_filters(dialog):
        for f in (group_id_filter, group_code_filter, profile_id_filter, master_id_filter, master_name_filter, master_phone_filter):
            f.value = ""
        sync_filter.value = ""
        request_page_update(page)
        
    def open_group_dialog(group: dict | None = None):
        group = group or {}
        group_id = group.get("id")
        code_field = text_field("Mã Nhóm", group.get("group_code", ""))
        name_field = text_field("Tên Nhóm", group.get("group_name", ""))
        
        profile_options = []
        try:
            # Fetch fresh list of all profiles without filter
            all_profiles = list_of(api_client.admin_get_group_profiles())
            for p in all_profiles:
                pid = str(p.get("id") or p.get("profile_id"))
                pname = p.get("profile_name", f"Profile {pid}")
                profile_options.append((pid, pname))
        except Exception:
            pass
            
        profile_field = dropdown("Chọn Profile", profile_options, str(group.get("profile_id") or ""), width=460)
        master_field = text_field("ID Master", group.get("master_account_id", ""), number=True)
        sync_field = dropdown("Đồng Bộ", [("true", "Có"), ("false", "Không")],
                              str(group.get("is_synchronize", group.get("isSynchronize", False))).lower(), width=160)
        created_by_field = text_field("ID Người Tạo", group.get("created_by") or _current_account_id(), number=True)
        created_at_field = text_field("Ngày Tạo", group.get("created_at") or group.get("createdDate") or _now_iso())
        dialog_ref = {"dialog": None}

        def submit(_):
            if not validate_required(page, code_field, name_field, sync_field, created_at_field):
                return
            required_ints = [profile_field, master_field]
            if not group_id:
                required_ints.append(created_by_field)
            if not validate_required_int(page, *required_ints):
                return

            def save():
                try:
                    if group_id:
                        api_client.admin_update_customer_group(group_id, clean_body({
                            "group_code": code_field.value.strip(),
                            "group_name": name_field.value.strip(),
                            "profile_id": int_value(profile_field.value),
                            "master_account_id": int_value(master_field.value),
                            "created_at": created_at_field.value.strip(),
                            "is_synchronize": str(sync_field.value).lower() == "true",
                        }))
                    else:
                        api_client.admin_create_customer_group(
                            int_value(profile_field.value),
                            code_field.value.strip(),
                            name_field.value.strip(),
                            int_value(created_by_field.value),
                            master_account_id=int_value(master_field.value),
                            is_synchronize=str(sync_field.value).lower() == "true",
                            created_at=created_at_field.value.strip(),
                        )
                    close_dialog(page, dialog_ref["dialog"])
                    load(state["page"] if group_id else 0)
                except Exception:
                    pass
            page.run_thread(save)

        controls = [code_field, name_field, profile_field, master_field, created_at_field, sync_field]
        if not group_id:
            controls.append(created_by_field)
        dialog_ref["dialog"] = show_dialog(
            page,
            "Sửa Nhóm Khách Hàng" if group_id else "Thêm Nhóm Khách Hàng",
            controls,
            [
                ft.OutlinedButton(content="Hủy", on_click=lambda _: close_dialog(page, dialog_ref["dialog"]),
                                  style=outlined_button_style()),
                ft.ElevatedButton(content="Lưu", icon=ft.Icons.SAVE_ROUNDED, on_click=submit,
                                  style=filled_button_style()),
            ],
        )

    def delete_group(group: dict):
        def do_delete():
            def run():
                try:
                    api_client.admin_delete_customer_group(group["id"])
                    load(state["page"])
                except Exception:
                    pass
            page.run_thread(run)

        confirm_dialog(page, "Xóa Nhóm Khách Hàng", "Xác Nhận Xóa Nhóm Khách Hàng Này?", do_delete)

    def open_customer_dialog(customer: dict | None = None):
        customer = customer or {}
        customer_id = customer.get("id")
        
        group_options = []
        try:
            resp = list_of(api_client.admin_get_customer_groups(page=0, size=1000))
            for g in resp:
                gid = str(g.get("id"))
                gname = g.get("group_name") or f"Nhóm {gid}"
                group_options.append((gid, gname))
        except Exception:
            pass
            
        group_field = dropdown("Chọn Nhóm", group_options, str(customer.get("group_id") or state["selected_group"] or ""), width=300)
        name_field = text_field("Họ Tên", customer.get("full_name", ""))
        phone_field = text_field("Số Điện Thoại", customer.get("phone", ""))
        address_field = text_field("Địa Chỉ", customer.get("address", ""))
        
        dialog_ref = {"dialog": None}

        def submit(_):
            required = [name_field, phone_field, address_field]
            if not validate_required(page, *required):
                return

            def save():
                try:
                    group_id = int_value(group_field.value)
                    if customer_id:
                        api_client.admin_update_customer(customer_id, clean_body({
                            "group_id": group_id,
                            "full_name": name_field.value.strip(),
                            "phone": phone_field.value.strip(),
                            "address": address_field.value.strip(),
                        }))
                    else:
                        api_client.admin_create_customer(
                            group_id,
                            name_field.value.strip(),
                            phone_field.value.strip(),
                            address=address_field.value.strip(),
                        )
                    close_dialog(page, dialog_ref["dialog"])
                    load(state.get("page", 0))
                    load_all_customers(state.get("all_page", 0))
                    # Only refresh the modal if it's currently showing THIS group's members
                    if state.get("member_dialog") and state.get("selected_group") == group_id:
                        load_members(group_id, state.get("selected_group_name", ""))
                except Exception:
                    pass
            page.run_thread(save)

        controls = [group_field, name_field, phone_field, address_field]
            
        dialog_ref["dialog"] = show_dialog(
            page,
            "Sửa Khách Hàng" if customer_id else "Thêm Khách Hàng",
            controls,
            [
                ft.OutlinedButton(content="Hủy", on_click=lambda _: close_dialog(page, dialog_ref["dialog"]),
                                  style=outlined_button_style()),
                ft.ElevatedButton(content="Lưu", icon=ft.Icons.SAVE_ROUNDED, on_click=submit,
                                  style=filled_button_style()),
            ],
            width=520,
        )

    def delete_customer(customer: dict):
        group_id = customer.get("group_id") or state["selected_group"]

        def do_delete():
            def run():
                try:
                    api_client.admin_delete_customer(customer["id"])
                    show_snack(page, "Đã Xóa Khách Hàng.")
                    load(state.get("page", 0))
                    load_all_customers(state.get("all_page", 0))
                    if state.get("member_dialog") and state.get("selected_group") == group_id:
                        load_members(group_id, state.get("selected_group_name", ""))
                except Exception as ex:
                    show_snack(page, f"Lỗi Xóa Khách Hàng: {ex}", DANGER)
            page.run_thread(run)

        confirm_dialog(page, "Xóa Khách Hàng", "Xác Nhận Xóa Khách Hàng Này?", do_delete)

    def load(pg: int = 0):
        loading.visible = True
        search_btn.disabled = True
        refresh_btn.disabled = True
        # Show loading placeholders immediately so panels never go blank
        profile_col.controls = [message_row("Đang Tải...")]
        table_col.controls   = [message_row("Đang Tải...")]
        request_page_update(page)

        def fetch():
            try:
                profiles = list_of(api_client.admin_get_group_profiles(
                    profile_code=profile_code_filter.value.strip() or None,
                    profile_name=profile_name_filter.value.strip() or None,
                ))
                new_profile = [_profile_header_row()]
                for profile in profiles:
                    new_profile.append(_profile_row(profile))
                if not profiles:
                    new_profile.append(message_row())
                profile_col.controls = new_profile

                resp = api_client.admin_get_customer_groups(
                    page=pg, size=20,
                    group_name=search_field.value.strip() or None,
                    group_id=int_value(group_id_filter.value),
                    group_code=group_code_filter.value.strip() or None,
                    profile_id=int_value(profile_id_filter.value),
                    is_synchronize=(str(sync_filter.value).lower() == "true") if sync_filter.value else None,
                    master_account_id=int_value(master_id_filter.value),
                    master_account_name=master_name_filter.value.strip() or None,
                    master_account_phone=master_phone_filter.value.strip() or None,
                )
                data    = resp.get("data", {})
                groups  = data.get("content", [])
                total   = data.get("totalElements", 0)
                t_pages = data.get("totalPages", 1)
                state["page"] = pg
                state["total_pages"] = t_pages

                new_table = [_header_row()]
                for g in groups:
                    new_table.append(_group_row(g))
                if not groups:
                    new_table.append(message_row())
                table_col.controls = new_table
                page_info.value = f"Trang {pg + 1}/{t_pages} - {total} Nhóm"
                update_pagination()
            except Exception as e:
                table_col.controls = [message_row(f"Lỗi Tải Khách Hàng: {e}", DANGER)]
                profile_col.controls = [message_row(f"Lỗi Tải Profile: {e}", DANGER)]
            loading.visible = False
            search_btn.disabled = False
            refresh_btn.disabled = False
            request_page_update(page)

        page.run_thread(fetch)

    search_field.on_submit = lambda _: load(0)
    for filter_control in (
        profile_code_filter, profile_name_filter,
    ):
        filter_control.on_submit = lambda _: load(0)
    for filter_control in (
        group_id_filter, group_code_filter, profile_id_filter, master_id_filter, master_name_filter, master_phone_filter
    ):
        filter_control.on_submit = lambda _: load(0)
    for filter_control in (
        member_name_filter, member_phone_filter, member_address_filter, member_account_filter,
    ):
        filter_control.on_submit = (
            lambda _: load_members(state["selected_group"], state["selected_group_name"])
            if state["selected_group"] else None
        )
    sync_filter.on_select = lambda _: load(0)

    def update_pagination():
        pagination_row.controls = build_pagination_controls(state["page"], state["total_pages"], load)
        pagination_row.visible = bool(pagination_row.controls)

    search_btn = ft.ElevatedButton(
        content="Tìm Kiếm",
        icon=ft.Icons.SEARCH_ROUNDED,
        on_click=lambda _: load(0),
        style=filled_button_style(),
    )
    refresh_btn = ft.OutlinedButton(
        content="Làm Mới",
        icon=ft.Icons.REFRESH_ROUNDED,
        on_click=lambda _: load(state["page"]),
        style=outlined_button_style(),
    )

    def update_all_pagination():
        all_pagination_row.controls = build_pagination_controls(state["all_page"], state["all_total_pages"], load_all_customers)
        all_pagination_row.visible = bool(all_pagination_row.controls)

    def load_all_customers(pg: int = 0):
        loading.visible = True
        all_customer_col.controls = [message_row("Đang Tải...")]
        request_page_update(page)

        def fetch():
            try:
                resp = api_client.admin_get_customers(
                    page=pg,
                    size=20,
                    full_name=all_name_filter.value.strip() or None,
                    phone=all_phone_filter.value.strip() or None,
                    address=all_address_filter.value.strip() or None,
                    account_id=int_value(all_account_filter.value),
                    deleted=(str(all_deleted_filter.value).lower() == "true") if all_deleted_filter.value else None,
                )
                members = resp.get("data", {}).get("content", [])
                total = resp.get("data", {}).get("totalElements", 0)
                t_pages = resp.get("data", {}).get("totalPages", 1)
                state["all_page"] = pg
                state["all_total_pages"] = t_pages

                new_rows = [_member_header_row()]
                for m in members:
                    new_rows.append(ft.Container(
                        padding=ft.Padding(PAD_MD, PAD_SM + 2, PAD_MD, PAD_SM + 2),
                        border=border_only(bottom=ft.BorderSide(1, BORDER)),
                        content=ft.Row(controls=[
                            ft.Text(ui_title(m.get("full_name", "-")), font_family=FONT_FAMILY,
                                    size=SIZE_SMALL, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=3),
                            ft.Text(m.get("phone", "-"), font_family=FONT_FAMILY,
                                    size=SIZE_SMALL, color=TEXT_SECONDARY, expand=2),
                            ft.Text(ui_title(m.get("address", "-") or "-"), font_family=FONT_FAMILY,
                                    size=SIZE_SMALL, color=TEXT_SECONDARY, expand=2),
                            ft.Text("Đã Xóa" if m.get("deleted") else "Hoạt Động", font_family=FONT_FAMILY,
                                    size=SIZE_SMALL, color=DANGER if m.get("deleted") else SUCCESS, expand=2),
                            ft.Row(expand=2, spacing=6, controls=[
                                ft.OutlinedButton(
                                    content="",
                                    icon=ft.Icons.EDIT_ROUNDED,
                                    tooltip="Sửa Khách Hàng",
                                    on_click=lambda _, row=m: open_customer_dialog(row),
                                    style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                                ),
                                ft.OutlinedButton(
                                    content="",
                                    icon=ft.Icons.DELETE_ROUNDED,
                                    tooltip="Xóa Khách Hàng",
                                    on_click=lambda _, row=m: delete_customer(row),
                                    style=outlined_button_style(ft.Padding(8, 6, 8, 6)),
                                ),
                            ]),
                        ]),
                    ))
                if not members:
                    new_rows.append(message_row("Không tìm thấy khách hàng."))
                
                all_customer_col.controls = new_rows
                all_page_info.value = f"Trang {pg + 1}/{t_pages} - {total} Khách Hàng"
                update_all_pagination()
            except Exception as e:
                all_customer_col.controls = [message_row(f"Lỗi: {e}", DANGER)]
                show_snack(page, f"Lỗi Tải Tất Cả Khách Hàng: {e}", DANGER)
            finally:
                loading.visible = False
                request_page_update(page)

        page.run_thread(fetch)

    for filter_control in (all_name_filter, all_phone_filter, all_address_filter, all_account_filter, all_deleted_filter):
        filter_control.on_submit = lambda _: load_all_customers(0)
    all_deleted_filter.on_select = lambda _: load_all_customers(0)

    load(0)
    load_all_customers(0)

    return ft.Column(
        expand=True,
        spacing=PAD_LG,
        controls=[
            ft.Row(controls=[
                ft.Text("Quản Lý Khách Hàng", font_family=FONT_FAMILY,
                        size=SIZE_H2, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=True),
                loading,
                refresh_btn,
            ]),
            ft.Tabs(
                length=2,
                expand=True,
                content=ft.Column(
                    expand=True,
                    controls=[
                        ft.TabBar(
                            tabs=[
                                ft.Tab(label="Nhóm Khách Hàng", icon=ft.Icons.GROUPS_ROUNDED),
                                ft.Tab(label="Tất Cả Khách Hàng", icon=ft.Icons.PEOPLE_ROUNDED),
                            ]
                        ),
                        ft.TabBarView(
                            expand=True,
                            controls=[
                                ft.Column(spacing=PAD_LG, expand=True, scroll=ft.ScrollMode.AUTO, controls=[
                                    ft.Row(controls=[
                                        ft.Text("Profile Khách Hàng", font_family=FONT_FAMILY,
                                                size=SIZE_H3, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=True),
                                        ft.ElevatedButton(
                                            content="Thêm Profile",
                                            icon=ft.Icons.ADD_ROUNDED,
                                            on_click=lambda _: open_profile_dialog(),
                                            style=filled_button_style(),
                                        ),
                                    ]),
                                    ft.Row(
                                        scroll=ft.ScrollMode.AUTO,
                                        spacing=PAD_SM,
                                        controls=[profile_code_filter, profile_name_filter],
                                    ),
                                    ft.Container(
                                        bgcolor=BG_CARD, border_radius=RADIUS_MD,
                                        border=border_all(1, BORDER), padding=PAD_LG,
                                        content=profile_col,
                                    ),
                                    ft.Row(controls=[
                                        search_field,
                                        ft.ElevatedButton(
                                            content="Lọc",
                                            icon=ft.Icons.FILTER_LIST_ROUNDED,
                                            on_click=lambda _: open_group_filter_dialog(),
                                            style=outlined_button_style(),
                                        ),
                                        ft.ElevatedButton(
                                            content="Thêm Nhóm",
                                            icon=ft.Icons.ADD_ROUNDED,
                                            on_click=lambda _: open_group_dialog(),
                                            style=filled_button_style(),
                                        ),
                                        search_btn,
                                    ]),
                                    ft.Container(
                                        bgcolor=BG_CARD, border_radius=RADIUS_MD,
                                        border=border_all(1, BORDER), padding=PAD_LG,
                                        content=ft.Column(spacing=0, controls=[
                                            table_col,
                                            ft.Container(height=PAD_MD),
                                            ft.Column(
                                                horizontal_alignment=ft.CrossAxisAlignment.CENTER,
                                                spacing=PAD_SM,
                                                controls=[page_info, pagination_row],
                                            ),
                                        ]),
                                    ),
                                ]),
                                ft.Column(spacing=PAD_LG, expand=True, scroll=ft.ScrollMode.AUTO, controls=[
                                    ft.Row(controls=[
                                        ft.Text("Tất Cả Khách Hàng", font_family=FONT_FAMILY,
                                                size=SIZE_H3, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=True),
                                        ft.ElevatedButton(
                                            content="Thêm Khách Hàng",
                                            icon=ft.Icons.PERSON_ADD_ROUNDED,
                                            on_click=lambda _: open_customer_dialog(),
                                            style=filled_button_style(),
                                        ),
                                    ]),
                                    ft.Row(
                                        scroll=ft.ScrollMode.AUTO,
                                        spacing=PAD_SM,
                                        controls=[
                                            all_name_filter, all_phone_filter, all_address_filter, all_account_filter, all_deleted_filter,
                                            ft.ElevatedButton(
                                                content="Tìm",
                                                icon=ft.Icons.SEARCH_ROUNDED,
                                                on_click=lambda _: load_all_customers(0),
                                                style=filled_button_style(),
                                            ),
                                        ],
                                    ),
                                    ft.Container(
                                        bgcolor=BG_CARD, border_radius=RADIUS_MD,
                                        border=border_all(1, BORDER), padding=PAD_LG,
                                        content=ft.Column(spacing=0, controls=[
                                            all_customer_col,
                                            ft.Container(height=PAD_MD),
                                            ft.Column(
                                                horizontal_alignment=ft.CrossAxisAlignment.CENTER,
                                                spacing=PAD_SM,
                                                controls=[all_page_info, all_pagination_row],
                                            ),
                                        ]),
                                        expand=True,
                                    ),
                                ])
                            ]
                        )
                    ]
                )
            )
        ],
    )
