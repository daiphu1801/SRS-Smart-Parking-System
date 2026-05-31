"""
subscriptions_page.py - Admin subscription, package, price, and tariff management.
"""
import flet as ft

from core import api_client
from core.design_tokens import *
from pages.admin.admin_ui import (
    bool_value, clean_body, close_dialog, confirm_dialog, data_of, dropdown,
    float_value, int_value, list_of, message_row, page_items, show_dialog, show_snack, text_field,
    validate_required, validate_required_float, validate_required_int,
)


def build_subscriptions_page(page: ft.Page) -> ft.Control:
    loading = ft.ProgressRing(width=24, height=24, stroke_width=2, color=PRIMARY, visible=True)
    vehicle_col = ft.Column(spacing=0, controls=[message_row("Đang Tải...")])
    package_col = ft.Column(spacing=0, controls=[message_row("Đang Tải...")])
    tariff_col = ft.Column(spacing=0, controls=[message_row("Đang Tải...")])
    state = {"selected_package_id": None, "selected_package": None, "tariffs": [], "detail_dialog": None}
    vehicle_search_filter = text_field("Tìm Loại Xe", width=190)
    package_search_filter = text_field("Tìm Gói Cước", width=190)
    package_status_filter = dropdown("Trạng Thái Gói", [
        ("true", "Hoạt Động"), ("false", "Không Hoạt Động"), ("", "Tất Cả"),
    ], "true", width=190)
    tariff_vehicle_filter = text_field("ID Loại Xe Giá Vé", number=True, width=190)
    tariff_day_filter = dropdown("Ngày Giá Vé", [
        ("", "Tất Cả"), ("MONDAY", "Monday"), ("TUESDAY", "Tuesday"),
        ("WEDNESDAY", "Wednesday"), ("THURSDAY", "Thursday"),
        ("FRIDAY", "Friday"), ("SATURDAY", "Saturday"), ("SUNDAY", "Sunday"),
    ], "", width=190)
    tariff_active_filter = dropdown("Trạng Thái Giá Vé", [
        ("true", "Hoạt Động"), ("false", "Không Hoạt Động"),
    ], "true", width=190)

    def section_title(title: str, action: ft.Control | None = None):
        controls = [
            ft.Text(title, font_family=FONT_FAMILY, size=SIZE_H3,
                    weight=W_MEDIUM, color=TEXT_PRIMARY, expand=True)
        ]
        if action is not None:
            controls.append(action)
        return ft.Row(controls=controls)

    def header_row(headers: list[str], expands: list[int]):
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

    def table_panel(content: ft.Control):
        return ft.Container(
            bgcolor=BG_CARD,
            border_radius=RADIUS_MD,
            border=border_all(1, BORDER),
            padding=PAD_LG,
            content=content,
        )

    def action_button(icon, tooltip, callback, *, filled=False):
        button = ft.ElevatedButton if filled else ft.OutlinedButton
        return button(
            content="",
            icon=icon,
            tooltip=tooltip,
            on_click=callback,
            style=(filled_button_style if filled else outlined_button_style)(ft.Padding(8, 6, 8, 6)),
        )

    def vehicle_row(item: dict):
        return ft.Container(
            padding=ft.Padding(PAD_MD, PAD_SM + 2, PAD_MD, PAD_SM + 2),
            border=border_only(bottom=ft.BorderSide(1, BORDER)),
            content=ft.Row(controls=[
                ft.Text(str(item.get("id", "")), font_family=FONT_FAMILY, size=SIZE_SMALL,
                        color=TEXT_DISABLED, expand=1),
                ft.Text(item.get("typeCode") or item.get("type_code", "-"), font_family=FONT_FAMILY, size=SIZE_SMALL,
                        weight=W_MEDIUM, color=TEXT_PRIMARY, expand=2),
                ft.Text(ui_title(item.get("typeName") or item.get("type_name", "-")), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, color=TEXT_SECONDARY, expand=3),
                ft.Row(expand=2, spacing=6, controls=[
                    action_button(ft.Icons.EDIT_ROUNDED, "Sửa Loại Xe", lambda _, row=item: open_vehicle_dialog(row)),
                    action_button(ft.Icons.DELETE_ROUNDED, "Xóa Loại Xe", lambda _, row=item: delete_vehicle(row)),
                ]),
            ]),
        )

    def package_row(item: dict):
        return ft.Container(
            padding=ft.Padding(PAD_MD, PAD_SM + 2, PAD_MD, PAD_SM + 2),
            border=border_only(bottom=ft.BorderSide(1, BORDER)),
            content=ft.Row(controls=[
                ft.Text(str(item.get("id", "")), font_family=FONT_FAMILY, size=SIZE_SMALL,
                        color=TEXT_DISABLED, expand=1),
                ft.Text(item.get("packageCode") or item.get("package_code", "-"), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=2),
                ft.Text(ui_title(item.get("packageName") or item.get("package_name", "-")), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, color=TEXT_SECONDARY, expand=3),
                ft.Text(ui_title(item.get("isAvailable") or item.get("is_available", "-")), font_family=FONT_FAMILY,
                        size=SIZE_SMALL, color=TEXT_SECONDARY, expand=1),
                ft.Row(expand=3, spacing=6, controls=[
                    action_button(ft.Icons.VISIBILITY_ROUNDED, "Xem Chi Tiết", lambda _, row=item: load_package_detail(row)),
                    action_button(ft.Icons.EDIT_ROUNDED, "Sửa Gói", lambda _, row=item: open_package_dialog(row)),
                    action_button(ft.Icons.DELETE_ROUNDED, "Xóa Gói", lambda _, row=item: delete_package(row)),
                ]),
            ]),
        )

    def tariff_row(item: dict):
        return ft.Container(
            padding=ft.Padding(PAD_MD, PAD_SM + 2, PAD_MD, PAD_SM + 2),
            border=border_only(bottom=ft.BorderSide(1, BORDER)),
            content=ft.Row(controls=[
                ft.Text(str(item.get("id", "")), font_family=FONT_FAMILY, size=SIZE_SMALL,
                        color=TEXT_DISABLED, expand=1),
                ft.Text(str(item.get("vehicle_type_id") or item.get("vehicleTypeId") or "-"),
                        font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_PRIMARY, expand=1),
                ft.Text(ui_title(item.get("day_type") or item.get("dayType") or "-"),
                        font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY, expand=2),
                ft.Text(str(item.get("start_time") or item.get("startTime") or "-"),
                        font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY, expand=1),
                ft.Text(str(item.get("end_time") or item.get("endTime") or "-"),
                        font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY, expand=1),
                ft.Text(str(item.get("base_price") or item.get("basePrice") or "0"),
                        font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY, expand=1),
                ft.Row(expand=2, spacing=6, controls=[
                    action_button(ft.Icons.EDIT_ROUNDED, "Sửa Giá Vé", lambda _, row=item: open_tariff_dialog(row)),
                    action_button(ft.Icons.BLOCK_ROUNDED, "Tắt Giá Vé", lambda _, row=item: disable_tariff(row)),
                ]),
            ]),
        )

    def open_vehicle_dialog(vehicle: dict | None = None):
        vehicle = vehicle or {}
        vehicle_id = vehicle.get("id")
        code_field = text_field("Mã Loại Xe", vehicle.get("typeCode") or vehicle.get("type_code", ""))
        name_field = text_field("Tên Loại Xe", vehicle.get("typeName") or vehicle.get("type_name", ""))
        dialog_ref = {"dialog": None}

        def submit(_):
            if not validate_required(page, code_field, name_field):
                return

            def save():
                try:
                    if vehicle_id:
                        api_client.admin_update_vehicle_type(
                            vehicle_id, name_field.value.strip(), code_field.value.strip()
                        )
                    else:
                        api_client.admin_create_vehicle_type(code_field.value.strip(), name_field.value.strip())
                    close_dialog(page, dialog_ref["dialog"])
                    load()
                except Exception:
                    pass
            page.run_thread(save)

        dialog_ref["dialog"] = show_dialog(
            page,
            "Sửa Loại Xe" if vehicle_id else "Thêm Loại Xe",
            [code_field, name_field],
            [
                ft.OutlinedButton(content="Hủy", on_click=lambda _: close_dialog(page, dialog_ref["dialog"]),
                                  style=outlined_button_style()),
                ft.ElevatedButton(content="Lưu", icon=ft.Icons.SAVE_ROUNDED, on_click=submit,
                                  style=filled_button_style()),
            ],
        )

    def delete_vehicle(vehicle: dict):
        def do_delete():
            def run():
                try:
                    api_client.admin_delete_vehicle_type(vehicle["id"])
                    load()
                except Exception:
                    pass
            page.run_thread(run)

        confirm_dialog(page, "Xóa Loại Xe", "Xác Nhận Xóa Loại Xe Này?", do_delete)

    def open_package_dialog(pkg: dict | None = None):
        pkg = pkg or {}
        pkg_id = pkg.get("id")
        code_field = text_field("Mã Gói", pkg.get("packageCode") or pkg.get("package_code", ""))
        name_field = text_field("Tên Gói", pkg.get("packageName") or pkg.get("package_name", ""))
        desc_field = text_field("Mô Tả", pkg.get("description", ""))
        
        profile_options = []
        try:
            all_profiles = list_of(api_client.admin_get_group_profiles())
            for p in all_profiles:
                pid = str(p.get("id") or p.get("profileId") or p.get("profile_id"))
                pname = p.get("profileName") or p.get("profile_name") or f"Profile {pid}"
                profile_options.append((pid, pname))
        except Exception:
            pass
            
        profile_field = dropdown("Chọn Profile", profile_options, str(pkg.get("profileId") or pkg.get("profile_id") or ""), width=300)
        active_field = dropdown(
            "Trạng Thái",
            [("true", "Active"), ("false", "Inactive")],
            str(pkg.get("isAvailable", pkg.get("is_available", True))).lower(),
            width=180,
        )
        dialog_ref = {"dialog": None}

        def submit(_):
            if not validate_required(page, code_field, name_field, desc_field, active_field, profile_field):
                return

            def save():
                try:
                    if pkg_id:
                        api_client.admin_update_package(
                            pkg_id,
                            name_field.value.strip(),
                            desc_field.value.strip(),
                            package_code=code_field.value.strip(),
                            profile_id=int_value(profile_field.value),
                            is_available=bool_value(active_field.value, True),
                        )
                    else:
                        api_client.admin_create_package(
                            code_field.value.strip(),
                            name_field.value.strip(),
                            desc_field.value.strip(),
                            profile_id=int_value(profile_field.value),
                            is_available=bool_value(active_field.value, True),
                        )
                    close_dialog(page, dialog_ref["dialog"])
                    load()
                except Exception:
                    pass
            page.run_thread(save)

        dialog_ref["dialog"] = show_dialog(
            page,
            "Sửa Gói Cước" if pkg_id else "Thêm Gói Cước",
            [code_field, name_field, desc_field, profile_field, active_field],
            [
                ft.OutlinedButton(content="Hủy", on_click=lambda _: close_dialog(page, dialog_ref["dialog"]),
                                  style=outlined_button_style()),
                ft.ElevatedButton(content="Lưu", icon=ft.Icons.SAVE_ROUNDED, on_click=submit,
                                  style=filled_button_style()),
            ],
        )

    def delete_package(pkg: dict):
        def do_delete():
            def run():
                try:
                    api_client.admin_delete_package(pkg["id"])
                    load()
                except Exception:
                    pass
            page.run_thread(run)

        confirm_dialog(page, "Xóa Gói Cước", "Xác Nhận Xóa Gói Cước Này?", do_delete)

    def render_package_detail(detail: dict):
        state["selected_package"] = detail
        state["selected_package_id"] = detail.get("packageId") or detail.get("package_id") or detail.get("id")
        rows = [ft.Row(controls=[
            ft.Text(
                f"Chi Tiết Gói - {ui_title(detail.get('packageName') or detail.get('package_name'))}",
                font_family=FONT_FAMILY, size=SIZE_H3, weight=W_MEDIUM,
                color=TEXT_PRIMARY, expand=True,
            ),
            ft.ElevatedButton(
                content="Thêm Loại Xe",
                icon=ft.Icons.ADD_ROUNDED,
                on_click=lambda _: open_package_vehicle_dialog(),
                style=filled_button_style(),
            ),
        ]), ft.Divider(color=BORDER, height=1)]

        vehicle_types = detail.get("vehicleTypes") or detail.get("vehicle_types") or []
        vehicle_type_ids: list[int] = []
        for vehicle_type in vehicle_types:
            vehicle_type_id = int_value(vehicle_type.get("vehicleTypeId") or vehicle_type.get("vehicle_type_id"))
            if vehicle_type_id and vehicle_type_id not in vehicle_type_ids:
                vehicle_type_ids.append(vehicle_type_id)

        for vehicle_type in vehicle_types:
            pvt_id = vehicle_type.get("pkgVehTypeId") or vehicle_type.get("pkg_veh_type_id")
            rows.append(ft.Container(
                padding=ft.Padding(PAD_MD, PAD_SM, PAD_MD, PAD_SM),
                border=border_only(bottom=ft.BorderSide(1, BORDER)),
                content=ft.Column(spacing=PAD_SM, controls=[
                    ft.Row(controls=[
                        ft.Text(
                            f"{ui_title(vehicle_type.get('vehicleTypeName') or vehicle_type.get('vehicle_type_name'))} "
                            f"- Tối Đa {vehicle_type.get('maxQuantity') or vehicle_type.get('max_quantity')}",
                            font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_PRIMARY,
                            weight=W_MEDIUM, expand=True,
                        ),
                        action_button(ft.Icons.EDIT_ROUNDED, "Sửa Loại Xe Trong Gói",
                                      lambda _, row=vehicle_type: open_package_vehicle_dialog(row)),
                        action_button(ft.Icons.DELETE_ROUNDED, "Xóa Loại Xe Trong Gói",
                                      lambda _, row=vehicle_type: delete_package_vehicle(row)),
                        ft.ElevatedButton(
                            content="Thêm Giá",
                            icon=ft.Icons.ADD_ROUNDED,
                            on_click=lambda _, pid=pvt_id: open_price_dialog({"pkgVehTypeId": pid}),
                            style=filled_button_style(ft.Padding(8, 6, 8, 6)),
                        ),
                    ]),
                    *[
                        ft.Row(controls=[
                            ft.Text(ui_title(price.get("packagePriceName") or price.get("package_price_name") or "-"),
                                    font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY, expand=2),
                            ft.Text(f"{price.get('durationMonths') or price.get('duration_months') or 0} Tháng",
                                    font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY, expand=1),
                            ft.Text(str(price.get("price") or 0),
                                    font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY, expand=1),
                            ft.Row(expand=1, spacing=6, controls=[
                                action_button(ft.Icons.EDIT_ROUNDED, "Sửa Giá",
                                              lambda _, row=price: open_price_dialog(row)),
                                action_button(ft.Icons.DELETE_ROUNDED, "Xóa Giá",
                                              lambda _, row=price: delete_price(row)),
                            ]),
                        ])
                        for price in (vehicle_type.get("prices") or [])
                    ],
                ]),
            ))
        if not vehicle_types:
            rows.append(message_row("Chưa có loại xe nào trong gói này."))

        if state["detail_dialog"]:
            close_dialog(page, state["detail_dialog"])
        
        state["detail_dialog"] = show_dialog(
            page,
            "Chi Tiết Gói Cước",
            [ft.Container(
                content=ft.Column(controls=rows, scroll=ft.ScrollMode.AUTO),
                height=500,
                width=800,
            )],
            [ft.OutlinedButton("Đóng", on_click=lambda _: close_dialog(page, state["detail_dialog"]), style=outlined_button_style())],
            width=850,
        )

    def load_package_detail(pkg: dict):
        loading.visible = True
        request_page_update(page)

        def fetch():
            try:
                detail = data_of(api_client.admin_get_package_details(pkg["id"]))
                render_package_detail(detail if isinstance(detail, dict) else {})
                pass
            finally:
                loading.visible = False
                request_page_update(page)
        page.run_thread(fetch)

    def open_package_vehicle_dialog(item: dict | None = None):
        item = item or {}
        pvt_id = item.get("pkg_veh_type_id") or item.get("pkgVehTypeId")
        package_field = text_field("ID Gói", item.get("package_id") or item.get("packageId") or state["selected_package_id"] or "", number=True)
        vehicle_field = text_field("ID Loại Xe", item.get("vehicle_type_id") or item.get("vehicleTypeId") or "", number=True)
        max_field = text_field("Số Lượng Tối Đa", item.get("max_quantity") or item.get("maxQuantity") or "", number=True)
        dialog_ref = {"dialog": None}

        def submit(_):
            if not validate_required_int(page, package_field, vehicle_field, max_field):
                return

            def save():
                try:
                    if pvt_id:
                        api_client.admin_update_package_vehicle_type(
                            pvt_id,
                            int_value(max_field.value, 0),
                            package_id=int_value(package_field.value),
                            vehicle_type_id=int_value(vehicle_field.value),
                        )
                    else:
                        api_client.admin_create_package_vehicle_type(
                            None,
                            int_value(package_field.value),
                            int_value(vehicle_field.value),
                            int_value(max_field.value, 0),
                        )
                    close_dialog(page, dialog_ref["dialog"])
                    if int_value(package_field.value):
                        load_package_detail({"id": int_value(package_field.value)})
                except Exception:
                    pass
            page.run_thread(save)

        dialog_ref["dialog"] = show_dialog(
            page,
            "Sửa Loại Xe Trong Gói" if pvt_id else "Thêm Loại Xe Vào Gói",
            [package_field, vehicle_field, max_field],
            [
                ft.OutlinedButton(content="Hủy", on_click=lambda _: close_dialog(page, dialog_ref["dialog"]),
                                  style=outlined_button_style()),
                ft.ElevatedButton(content="Lưu", icon=ft.Icons.SAVE_ROUNDED, on_click=submit,
                                  style=filled_button_style()),
            ],
        )

    def delete_package_vehicle(item: dict):
        pvt_id = item.get("pkg_veh_type_id") or item.get("pkgVehTypeId")

        def do_delete():
            def run():
                try:
                    api_client.admin_delete_package_vehicle_type(pvt_id)
                    if state["selected_package_id"]:
                        load_package_detail({"id": state["selected_package_id"]})
                except Exception:
                    pass
            page.run_thread(run)

        confirm_dialog(page, "Xóa Loại Xe Trong Gói", "Xác Nhận Xóa Cấu Hình Này?", do_delete)

    def open_price_dialog(price: dict | None = None):
        price = price or {}
        price_id = price.get("id")
        pvt_field = text_field("ID Loại Xe Trong Gói", price.get("pkg_veh_type_id") or price.get("pkgVehTypeId") or "", number=True)
        name_field = text_field("Tên Gói Giá", price.get("package_price_name") or price.get("packagePriceName") or "")
        duration_field = text_field("Số Tháng", price.get("duration_months") or price.get("durationMonths") or "", number=True)
        price_field = text_field("Giá", price.get("price", ""), number=True)
        active_field = dropdown("Hoạt Động", [("true", "Active"), ("false", "Inactive")],
                                str(price.get("is_active", price.get("isActive", True))).lower(), width=180)
        dialog_ref = {"dialog": None}

        def submit(_):
            if not validate_required(page, name_field, active_field):
                return
            if not validate_required_int(page, pvt_field, duration_field):
                return
            if not validate_required_float(page, price_field):
                return

            def save():
                try:
                    if price_id:
                        api_client.admin_update_package_price(
                            price_id,
                            float_value(price_field.value, 0),
                            bool_value(active_field.value, True),
                            pkg_veh_type_id=int_value(pvt_field.value),
                            package_price_name=name_field.value.strip(),
                            duration_months=int_value(duration_field.value),
                        )
                    else:
                        api_client.admin_create_package_price(
                            int_value(pvt_field.value),
                            name_field.value.strip(),
                            int_value(duration_field.value, 0),
                            float_value(price_field.value, 0),
                            bool_value(active_field.value, True),
                        )
                    close_dialog(page, dialog_ref["dialog"])
                    if state["selected_package_id"]:
                        load_package_detail({"id": state["selected_package_id"]})
                except Exception:
                    pass
            page.run_thread(save)

        dialog_ref["dialog"] = show_dialog(
            page,
            "Sửa Gói Giá" if price_id else "Thêm Gói Giá",
            [pvt_field, name_field, duration_field, price_field, active_field],
            [
                ft.OutlinedButton(content="Hủy", on_click=lambda _: close_dialog(page, dialog_ref["dialog"]),
                                  style=outlined_button_style()),
                ft.ElevatedButton(content="Lưu", icon=ft.Icons.SAVE_ROUNDED, on_click=submit,
                                  style=filled_button_style()),
            ],
        )

    def delete_price(price: dict):
        def do_delete():
            def run():
                try:
                    api_client.admin_delete_package_price(price["id"])
                    if state["selected_package_id"]:
                        load_package_detail({"id": state["selected_package_id"]})
                except Exception:
                    pass
            page.run_thread(run)

        confirm_dialog(page, "Xóa Gói Giá", "Xác Nhận Xóa Gói Giá Này?", do_delete)

    def open_tariff_dialog(rule: dict | None = None):
        rule = rule or {}
        rule_id = rule.get("id")
        vehicle_field = text_field("ID Loại Xe", rule.get("vehicle_type_id") or rule.get("vehicleTypeId") or "", number=True)
        day_field = dropdown("Loại Ngày", [
            ("MONDAY", "Monday"), ("TUESDAY", "Tuesday"), ("WEDNESDAY", "Wednesday"),
            ("THURSDAY", "Thursday"), ("FRIDAY", "Friday"),
            ("SATURDAY", "Saturday"), ("SUNDAY", "Sunday"),
        ], rule.get("day_type") or rule.get("dayType") or "MONDAY", width=220)
        start_field = text_field("Giờ Bắt Đầu", rule.get("start_time") or rule.get("startTime") or "00:00:00")
        end_field = text_field("Giờ Kết Thúc", rule.get("end_time") or rule.get("endTime") or "23:59:59")
        price_field = text_field("Giá Cơ Bản", rule.get("base_price") or rule.get("basePrice") or "", number=True)
        active_field = dropdown("Hoạt Động", [("true", "Active"), ("false", "Inactive")],
                                str(rule.get("is_active", rule.get("isActive", True))).lower(), width=180)
        dialog_ref = {"dialog": None}

        def submit(_):
            if not validate_required(page, day_field, start_field, end_field, active_field):
                return
            if not validate_required_int(page, vehicle_field):
                return
            if not validate_required_float(page, price_field):
                return

            def save():
                try:
                    if rule_id:
                        api_client.admin_update_tariff_rule(rule_id, clean_body({
                            "vehicle_type_id": int_value(vehicle_field.value),
                            "day_type": day_field.value,
                            "start_time": start_field.value.strip(),
                            "end_time": end_field.value.strip(),
                            "base_price": float_value(price_field.value, 0),
                            "is_active": bool_value(active_field.value, True),
                        }))
                    else:
                        api_client.admin_create_tariff_rule(
                            int_value(vehicle_field.value),
                            day_field.value,
                            start_field.value.strip(),
                            end_field.value.strip(),
                            float_value(price_field.value, 0),
                            bool_value(active_field.value, True),
                        )
                    close_dialog(page, dialog_ref["dialog"])
                    load()
                except Exception:
                    pass
            page.run_thread(save)

        dialog_ref["dialog"] = show_dialog(
            page,
            "Sửa Giá Vé" if rule_id else "Thêm Giá Vé",
            [vehicle_field, day_field, start_field, end_field, price_field, active_field],
            [
                ft.OutlinedButton(content="Hủy", on_click=lambda _: close_dialog(page, dialog_ref["dialog"]),
                                  style=outlined_button_style()),
                ft.ElevatedButton(content="Lưu", icon=ft.Icons.SAVE_ROUNDED, on_click=submit,
                                  style=filled_button_style()),
            ],
            width=520,
        )

    def disable_tariff(rule: dict):
        def do_disable():
            def run():
                try:
                    api_client.admin_disable_tariff_rule(rule["id"])
                    load()
                except Exception:
                    pass
            page.run_thread(run)

        confirm_dialog(page, "Tắt Giá Vé", "Xác Nhận Tắt Giá Vé Này?", do_disable)

    def load():
        loading.visible = True
        request_page_update(page)

        def fetch():
            try:
                vehicles, _, _ = page_items(api_client.admin_get_vehicle_types(
                    search=vehicle_search_filter.value.strip() or None,
                    page=0,
                    size=100,
                ))
                packages, _, _ = page_items(api_client.admin_get_packages(
                    search=package_search_filter.value.strip() or None,
                    status=bool_value(package_status_filter.value) if package_status_filter.value else None,
                    page=0,
                    size=100,
                ))
                tariffs = data_of(api_client.admin_get_tariff_rules(
                    vehicle_type_id=int_value(tariff_vehicle_filter.value),
                    day_type=tariff_day_filter.value or None,
                    is_active=bool_value(tariff_active_filter.value, True),
                    page=0,
                    size=100,
                ))
                if isinstance(tariffs, dict):
                    tariffs = tariffs.get("content", []) or tariffs.get("items", [])
                if not isinstance(tariffs, list):
                    tariffs = []
                state["tariffs"] = tariffs

                vehicle_col.controls = [header_row(["ID", "Mã", "Tên Loại Xe", "Thao Tác"], [1, 2, 3, 2])]
                vehicle_col.controls.extend(vehicle_row(item) for item in vehicles)
                if not vehicles:
                    vehicle_col.controls.append(message_row())

                package_col.controls = [header_row(["ID", "Mã", "Tên Gói", "Trạng Thái", "Thao Tác"], [1, 2, 3, 1, 3])]
                package_col.controls.extend(package_row(item) for item in packages)
                if not packages:
                    package_col.controls.append(message_row())

                tariff_col.controls = [header_row(["ID", "Loại Xe", "Ngày", "Từ", "Đến", "Giá", "Thao Tác"], [1, 1, 2, 1, 1, 1, 2])]
                tariff_col.controls.extend(tariff_row(item) for item in tariffs)
                if not tariffs:
                    tariff_col.controls.append(message_row())

                if state["selected_package_id"]:
                    load_package_detail({"id": state["selected_package_id"]})
            except Exception as ex:
                vehicle_col.controls = [message_row(f"Lỗi Tải Loại Xe: {ex}", DANGER)]
                package_col.controls = [message_row(f"Lỗi Tải Gói Cước: {ex}", DANGER)]
                tariff_col.controls = [message_row(f"Lỗi Tải Giá Vé: {ex}", DANGER)]
                show_snack(page, f"Lỗi Tải Dữ Liệu: {ex}", DANGER)
            finally:
                loading.visible = False
                request_page_update(page)

        page.run_thread(fetch)

    load()
    vehicle_search_filter.on_submit = lambda _: load()
    package_search_filter.on_submit = lambda _: load()
    tariff_vehicle_filter.on_submit = lambda _: load()
    package_status_filter.on_select = lambda _: load()
    tariff_day_filter.on_select = lambda _: load()
    tariff_active_filter.on_select = lambda _: load()

    return ft.Column(
        expand=True,
        spacing=PAD_LG,
        controls=[
            ft.Row(controls=[
                ft.Text("Quản Lý Dịch Vụ & Bảng Giá", font_family=FONT_FAMILY,
                        size=SIZE_H2, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=True),
                loading,
                ft.OutlinedButton(content="Làm Mới", icon=ft.Icons.REFRESH_ROUNDED,
                                  on_click=lambda _: load(), style=outlined_button_style()),
            ]),
            ft.Tabs(
                length=3,
                expand=True,
                content=ft.Column(
                    expand=True,
                    controls=[
                        ft.TabBar(
                            tabs=[
                                ft.Tab(label="Gói Cước", icon=ft.Icons.LOCAL_OFFER_ROUNDED),
                                ft.Tab(label="Loại Phương Tiện", icon=ft.Icons.DIRECTIONS_CAR_ROUNDED),
                                ft.Tab(label="Giá Vé Vãng Lai", icon=ft.Icons.ATTACH_MONEY_ROUNDED),
                            ]
                        ),
                        ft.TabBarView(
                            expand=True,
                            controls=[
                                ft.Column(spacing=PAD_LG, expand=True, controls=[
                                    ft.Container(height=PAD_SM),
                                    ft.Row(scroll=ft.ScrollMode.AUTO, spacing=PAD_SM, controls=[package_search_filter, package_status_filter]),
                                    section_title("Danh Sách Gói Cước", ft.ElevatedButton(content="Thêm Gói", icon=ft.Icons.ADD_ROUNDED, on_click=lambda _: open_package_dialog(), style=filled_button_style())),
                                    table_panel(package_col),
                                ]),
                                ft.Column(spacing=PAD_LG, expand=True, controls=[
                                    ft.Container(height=PAD_SM),
                                    ft.Row(scroll=ft.ScrollMode.AUTO, spacing=PAD_SM, controls=[vehicle_search_filter]),
                                    section_title("Danh Sách Loại Xe", ft.ElevatedButton(content="Thêm Loại Xe", icon=ft.Icons.ADD_ROUNDED, on_click=lambda _: open_vehicle_dialog(), style=filled_button_style())),
                                    table_panel(vehicle_col),
                                ]),
                                ft.Column(spacing=PAD_LG, expand=True, controls=[
                                    ft.Container(height=PAD_SM),
                                    ft.Row(scroll=ft.ScrollMode.AUTO, spacing=PAD_SM, controls=[tariff_vehicle_filter, tariff_day_filter, tariff_active_filter]),
                                    section_title("Bảng Giá Vãng Lai", ft.ElevatedButton(content="Thêm Giá Vé", icon=ft.Icons.ADD_ROUNDED, on_click=lambda _: open_tariff_dialog(), style=filled_button_style())),
                                    table_panel(tariff_col),
                                ])
                            ]
                        )
                    ]
                )
            )
        ],
    )
