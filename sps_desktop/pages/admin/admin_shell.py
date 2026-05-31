"""
admin_shell.py - Admin Portal shell with sidebar routing.
All sub-pages pull live data from the Spring Boot backend.
"""
from types import SimpleNamespace

import flet as ft
from core.design_tokens import *
from core import api_client

from pages.admin.dashboard_page  import build_dashboard
from pages.admin.sessions_page   import build_sessions_page
from pages.admin.payments_page   import build_payments_page
from pages.admin.bookings_page   import build_bookings_page
from pages.admin.customers_page  import build_customers_page
from pages.admin.subscriptions_page import build_subscriptions_page
from pages.admin.staff_page import build_staff_page
from pages.admin.devices_page    import build_devices_page
from pages.admin.permissions_page import build_permissions_page
from pages.admin.complaints_page import build_complaints_page
from pages.admin.settings_page import build_settings_page
from pages.admin.configs_page import build_configs_page
from pages.admin.notifications_page import build_notifications_page
from pages.admin.admin_ui import message_row


NAV_ITEMS = [
    ("dashboard",  "Tổng Quan",         ft.Icons.DASHBOARD_ROUNDED),
    ("guard",      "Cổng Bảo Vệ",       ft.Icons.SECURITY_ROUNDED),
    ("sessions",   "Phiên Đỗ Xe",       ft.Icons.DIRECTIONS_CAR_ROUNDED),
    ("bookings",   "Hợp Đồng",          ft.Icons.DESCRIPTION_ROUNDED),
    ("customers",  "Khách Hàng",        ft.Icons.GROUP_ROUNDED),
    ("subscriptions", "Gói Cước",       ft.Icons.LOCAL_OFFER_ROUNDED),
    ("staff",      "Nhân Sự",           ft.Icons.BADGE_ROUNDED),
    ("payments",   "Thanh Toán",        ft.Icons.PAYMENTS_ROUNDED),
    ("devices",    "Thiết Bị IoT",      ft.Icons.DEVICE_HUB_ROUNDED),
    ("complaints", "Khiếu Nại",         ft.Icons.REPORT_PROBLEM_ROUNDED),
    ("system_configs", "CH Hệ Thống",   ft.Icons.TUNE_ROUNDED),
    ("settings",   "Cấu Hình Local",    ft.Icons.SETTINGS_ROUNDED),
    ("permissions", "Phân Quyền",       ft.Icons.ADMIN_PANEL_SETTINGS_ROUNDED),
    ("notifications", "Thông Báo",      ft.Icons.NOTIFICATIONS_ACTIVE_ROUNDED),
]

PAGE_BUILDERS = {
    "dashboard": build_dashboard,
    "sessions":  build_sessions_page,
    "payments":  build_payments_page,
    "bookings":  build_bookings_page,
    "customers": build_customers_page,
    "subscriptions": build_subscriptions_page,
    "staff": build_staff_page,
    "devices":   build_devices_page,
    ("complaints"): build_complaints_page,
    "system_configs": build_configs_page,
    "settings": build_settings_page,
    "permissions": build_permissions_page,
    "notifications": build_notifications_page,
}


def build_admin_portal(page: ft.Page, on_logout) -> ft.Control:
    current_route = SimpleNamespace(current="dashboard")
    
    # --- Notification Badge Logic ---
    unread_notifications = [0]
    badge_text = ft.Text("", font_family=FONT_FAMILY, size=10, color=WHITE, weight=ft.FontWeight.BOLD)
    badge_container = ft.Container(
        content=badge_text,
        bgcolor=DANGER,
        border_radius=10,
        padding=ft.Padding(6, 2, 6, 2),
        visible=False,
        alignment=ft.Alignment(0, 0)
    )

    def on_new_notification(payload):
        if current_route.current != "notifications":
            unread_notifications[0] += 1
            badge_text.value = str(unread_notifications[0])
            badge_container.visible = True
            try:
                badge_container.update()
            except Exception:
                pass

    from core.supabase_client import register_realtime_callback
    register_realtime_callback(on_new_notification)
    # --------------------------------
    
    content_area = ft.Column(expand=True, scroll=ft.ScrollMode.AUTO)

    def build_admin_guard_page(host_page: ft.Page) -> ft.Control:
        from pages.guard.guard_kiosk import build_guard_kiosk

        return build_guard_kiosk(host_page, on_logout=do_logout, show_logout=False)

    page_builders = dict(PAGE_BUILDERS)
    page_builders["guard"] = build_admin_guard_page
    route_cache: dict[str, ft.Control] = {}

    # ── Nav item builder ──────────────────────────────
    nav_refs: dict[str, ft.Container] = {}

    def _nav_item(route: str, label: str, icon) -> ft.Container:
        is_active = route == current_route.current
        fg = WHITE if is_active else PRIMARY

        row_controls = [
            ft.Icon(icon, size=18, color=fg),
            ft.Text(label, font_family=FONT_FAMILY, size=SIZE_BODY,
                    color=fg, weight=W_MEDIUM if is_active else W_REGULAR, expand=True),
        ]
        if route == "notifications":
            row_controls.append(badge_container)

        container = ft.Container(
            padding=ft.Padding(PAD_MD, PAD_SM, PAD_MD, PAD_SM),
            border_radius=RADIUS_MD,
            bgcolor=PRIMARY_GLOW if is_active else WHITE,
            on_click=lambda _, r=route: navigate(r),
            ink=True,
            content=ft.Row(spacing=12, controls=row_controls),
        )
        nav_refs[route] = container
        return container

    def navigate(route: str):
        current_route.current = route
        
        if route == "notifications":
            unread_notifications[0] = 0
            badge_container.visible = False
            try:
                badge_container.update()
            except Exception:
                pass

        # Restyle nav items
        for r, c in nav_refs.items():
            is_a = r == route
            c.bgcolor = PRIMARY_GLOW if is_a else WHITE
            row = c.content
            fg = WHITE if is_a else PRIMARY
            row.controls[0].color = fg
            row.controls[1].color = fg
            row.controls[1].weight = W_MEDIUM if is_a else W_REGULAR

        # Build the page first — only THEN replace content so there is no
        # empty-frame between the clear and the new content appearing.
        try:
            if route not in route_cache:
                builder = page_builders.get(route, build_dashboard)
                route_cache[route] = builder(page)
            next_content = route_cache[route]
        except Exception as ex:
            next_content = ft.Container(
                bgcolor=BG_CARD,
                border_radius=RADIUS_MD,
                border=border_all(1, BORDER),
                content=message_row(f"Lỗi Tải Trang: {ex}", DANGER),
            )

        content_area.controls = [
            ft.Container(expand=True, bgcolor=WHITE, content=next_content)
        ]
        page.update()

    def do_logout(_):
        try:
            api_client.logout()
        except Exception:
            pass
        api_client.clear_token()
        on_logout()

    # ── Sidebar ───────────────────────────────────────
    nav_controls = [_nav_item(r, l, i) for r, l, i in NAV_ITEMS]

    try:
        me = api_client.get_me()
        me_data = me.get("data", {})
        username  = me_data.get("full_name") or me_data.get("username", "Admin")
        role_name = me_data.get("account_type", "ADMIN")
    except Exception:
        username  = "Admin"
        role_name = "ADMIN"
    username = str(username).title()
    role_name = str(role_name).title()

    sidebar = ft.Container(
        width=SIDEBAR_W,
        bgcolor=BG_SIDEBAR,
        border=border_only(right=ft.BorderSide(1, BORDER)),
        padding=ft.Padding(PAD_SM, PAD_LG, PAD_SM, PAD_LG),
        content=ft.Column(
            expand=True,
            spacing=4,
            controls=[
                # Logo
                ft.Row(
                    spacing=10,
                    controls=[
                        ft.Container(
                            width=36, height=36, bgcolor=PRIMARY,
                            border_radius=RADIUS_MD, alignment=ft.Alignment(0, 0),
                            content=ft.Icon(ft.Icons.GARAGE_ROUNDED, size=20, color=WHITE),
                        ),
                        ft.Column(spacing=0, tight=True, controls=[
                            ft.Text("Smart", font_family=FONT_FAMILY, size=SIZE_SMALL,
                                    weight=W_MEDIUM, color=TEXT_PRIMARY),
                            ft.Text("Parking", font_family=FONT_FAMILY, size=SIZE_CAPTION,
                                    color=TEXT_SECONDARY),
                        ]),
                    ],
                ),
                ft.Container(height=PAD_LG),
                ft.Text("Điều Hướng", font_family=FONT_FAMILY, size=SIZE_CAPTION,
                        color=TEXT_DISABLED, weight=W_MEDIUM),
                ft.Container(height=4),
                *nav_controls,
                ft.Container(expand=True),   # Push user section to bottom
                ft.Divider(color=BORDER, height=1),
                # Logout button
                ft.Column(
                    spacing=2,
                    horizontal_alignment=ft.CrossAxisAlignment.STRETCH,
                    controls=[
                        ft.OutlinedButton(
                            content="Đăng Xuất", icon=ft.Icons.LOGOUT_ROUNDED,
                            tooltip="Đăng Xuất", on_click=do_logout,
                            style=outlined_button_style(ft.Padding(10, 8, 10, 8)),
                        ),
                    ],
                ),
            ],
        ),
    )

    # Load initial page
    try:
        route_cache["dashboard"] = page_builders.get("dashboard", build_dashboard)(page)
        content_area.controls.append(
            ft.Container(expand=True, bgcolor=WHITE, content=route_cache["dashboard"])
        )
    except Exception as ex:
        content_area.controls.append(
            ft.Container(
                bgcolor=BG_CARD,
                border_radius=RADIUS_MD,
                border=border_all(1, BORDER),
                content=message_row(f"Lỗi Tải Trang: {ex}", DANGER),
            )
        )

    return ft.Row(
        expand=True,
        spacing=0,
        controls=[
            sidebar,
            ft.Container(expand=True, bgcolor=BG_BASE, content=content_area, padding=PAD_XL),
        ],
    )

