"""
app_shell.py — Desktop App Shell with sidebar navigation
Sidebar: 240px fixed, nav items by role (Admin/Manager/Guard)
"""
import flet as ft
from desktop_ui.design_tokens import *
from desktop_ui.components.ui.shared_widgets import text_label, section_divider, badge
from desktop_ui.pages.admin.dashboard_page import dashboard_view
from desktop_ui.pages.admin.group_management_page import group_management_view
from desktop_ui.pages.admin.pricing_page import pricing_view
from desktop_ui.pages.admin.reports_page import reports_view
from desktop_ui.pages.admin.complaints_page import complaints_view
from desktop_ui.pages.admin.zones_devices_page import zones_devices_view

from desktop_ui.pages.guard.gate_control_page import gate_control_view
from desktop_ui.pages.guard.vehicle_lookup_page import vehicle_lookup_view
from desktop_ui.pages.guard.shift_log_page import shift_log_view

# ── Navigation Config ─────────────────────────────────────────────────────────

ADMIN_NAV = [
    ("Bảng Điều Khiển", ft.Icons.DASHBOARD_OUTLINED),
    ("Quản Lý Nhóm", ft.Icons.GROUPS_OUTLINED),
    ("Tài Khoản Nhân Viên", ft.Icons.BADGE_OUTLINED),
    ("Bảng Giá & Gói Dịch Vụ", ft.Icons.SELL_OUTLINED),
    ("Khu Vực & Thiết Bị", ft.Icons.DEVICE_HUB_OUTLINED),
    ("Báo Cáo & Kiểm Toán", ft.Icons.ANALYTICS_OUTLINED),
    ("Khiếu Nại", ft.Icons.FEEDBACK_OUTLINED),
    ("Cài Đặt", ft.Icons.SETTINGS_OUTLINED),
]

MANAGER_NAV = [
    ("Bảng Điều Khiển", ft.Icons.DASHBOARD_OUTLINED),
    ("Báo Cáo & Kiểm Toán", ft.Icons.ANALYTICS_OUTLINED),
    ("Khiếu Nại", ft.Icons.FEEDBACK_OUTLINED),
]

GUARD_NAV = [
    ("Kiểm Soát Cổng", ft.Icons.DOOR_FRONT_DOOR_OUTLINED),
    ("Cảnh Báo Trực Tiếp", ft.Icons.DOORBELL_OUTLINED),
    ("Tra Cứu Xe", ft.Icons.SEARCH_OUTLINED),
    ("Nhật Ký Ca Trực", ft.Icons.HISTORY_OUTLINED),
]


def get_view_for(role: str, page_title: str, page: ft.Page = None) -> ft.Control:
    """Return the content view for the given role and page title."""
    # Views that need page for thread-safe UI updates
    page_aware = {"Kiểm Soát Cổng", "Khu Vực & Thiết Bị"}

    mapping = {
        "Bảng Điều Khiển": dashboard_view,
        "Quản Lý Nhóm": group_management_view,
        "Bảng Giá & Gói Dịch Vụ": pricing_view,
        "Báo Cáo & Kiểm Toán": reports_view,
        "Khiếu Nại": complaints_view,
        "Khu Vực & Thiết Bị": zones_devices_view,
        "Kiểm Soát Cổng": gate_control_view,
        "Tra Cứu Xe": vehicle_lookup_view,
        "Nhật Ký Ca Trực": shift_log_view,
    }
    if page_title in mapping:
        result = mapping[page_title](page) if page_title in page_aware else mapping[page_title]()
        # Special case: Row-based layouts (Gate Control, Zones) need no extra padding container
        if isinstance(result, ft.Row):
            return ft.Container(content=result, expand=True)
        return ft.Container(
            content=result,
            padding=PAGE_PADDING,
            expand=True,
        )
    # Placeholder for unimplemented views
    return ft.Container(
        padding=PAGE_PADDING,
        content=ft.Column(controls=[
            text_label(page_title, size=SIZE_H1, weight=W_SEMIBOLD),
            ft.Container(height=24),
            ft.Column(
                horizontal_alignment=ft.CrossAxisAlignment.CENTER,
                controls=[
                    ft.Icon(ft.Icons.CONSTRUCTION_OUTLINED, size=40,
                            color=ft.Colors.with_opacity(0.30, PRIMARY)),
                    ft.Container(height=12),
                    text_label("Sắp ra mắt", size=SIZE_H3, weight=W_SEMIBOLD),
                    text_label("Màn hình này đang trong quá trình xây dựng.", size=SIZE_BODY,
                               color=ft.Colors.with_opacity(0.60, PRIMARY)),
                ],
            ),
        ]),
        expand=True,
    )


# ── App Shell ─────────────────────────────────────────────────────────────────

def build_app_shell(page: ft.Page, role: str = "Admin", on_logout: callable = None) -> None:
    page.title = "Smart Parking — Desktop"
    page.bgcolor = BACKGROUND
    page.fonts = {"Inter": "https://fonts.gstatic.com/s/inter/v13/UcCO3FwrK3iLTeHuS_fvQtMwCp50KnMw2boKoduKmMEVuLyfAZ9hiA.woff2"}
    page.theme = ft.Theme(font_family="Inter")
    page.window.min_width = 1024
    page.window.min_height = 640

    nav_items = {"Admin": ADMIN_NAV, "Manager": MANAGER_NAV, "Guard": GUARD_NAV}.get(role, ADMIN_NAV)
    current_page = [nav_items[0][0]]

    content_area = ft.Container(expand=True)

    def refresh_content():
        content_area.content = get_view_for(role, current_page[0], page)
        page.update()

    def nav_item(label: str, icon, index: int) -> ft.Container:
        def on_click(_):
            current_page[0] = label
            build_sidebar()
            refresh_content()

        is_active = current_page[0] == label
        return ft.Container(
            bgcolor=PRIMARY if is_active else ft.Colors.TRANSPARENT,
            border_radius=RADIUS_BUTTON,
            padding=ft.Padding.symmetric(horizontal=16, vertical=10),
            on_click=on_click,
            content=ft.Row(spacing=10, controls=[
                ft.Icon(icon, size=20, color=BACKGROUND if is_active else PRIMARY),
                ft.Text(label, font_family=FONT_FAMILY, size=SIZE_BODY,
                        color=BACKGROUND if is_active else PRIMARY),
            ]),
        )

    sidebar = ft.Container(
        width=240,
        bgcolor=BACKGROUND,
        border=ft.Border(right=ft.BorderSide(1, ft.Colors.with_opacity(0.10, PRIMARY))),
        padding=ft.Padding.symmetric(vertical=PAGE_PADDING, horizontal=12),
    )

    def build_sidebar():
        sidebar.content = ft.Column(
            expand=True,
            controls=[
                # Logo + App name
                ft.Container(
                    padding=ft.Padding(left=4, bottom=24),
                    content=ft.Row(spacing=10, controls=[
                        ft.Icon(ft.Icons.LOCAL_PARKING_ROUNDED, size=24, color=PRIMARY),
                        ft.Text("Smart Parking", font_family=FONT_FAMILY, size=SIZE_H3,
                                weight=W_SEMIBOLD, color=PRIMARY),
                    ]),
                ),
                # Nav items
                ft.Column(
                    spacing=2,
                    expand=True,
                    controls=[nav_item(label, icon, i) for i, (label, icon) in enumerate(nav_items)],
                ),
                # Role chip and Logout at bottom
                ft.Container(
                    padding=ft.Padding(left=4, top=16),
                    content=ft.Row(
                        alignment=ft.MainAxisAlignment.SPACE_BETWEEN,
                        controls=[
                            ft.Container(
                                content=ft.Text(role, font_family=FONT_FAMILY, size=SIZE_CAPTION,
                                                weight=W_MEDIUM, color=PRIMARY),
                                padding=ft.Padding.symmetric(horizontal=10, vertical=4),
                                border=ft.Border.all(1, PRIMARY),
                                border_radius=RADIUS_BUTTON,
                            ),
                            ft.IconButton(
                                icon=ft.Icons.LOGOUT_OUTLINED,
                                icon_color=ft.Colors.ERROR,
                                tooltip="Đăng xuất",
                                on_click=lambda _: on_logout() if on_logout else None,
                            )
                        ]
                    ),
                ),
            ],
        )

    build_sidebar()
    refresh_content()

    page.add(
        ft.Row(
            expand=True,
            spacing=0,
            vertical_alignment=ft.CrossAxisAlignment.START,
            controls=[sidebar, content_area],
        )
    )
