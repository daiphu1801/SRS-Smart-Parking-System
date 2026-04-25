"""
app_shell.py — Desktop App Shell with sidebar navigation
Sidebar: 240px fixed, nav items by role (Admin/Manager/Guard)
"""
import flet as ft
from .design_tokens import *
from .shared_widgets import text_label, section_divider, badge
from .admin_portal.admin_views import (
    dashboard_view, group_management_view, pricing_view,
    reports_view, complaints_view, zones_devices_view
)
from .guard_kiosk.guard_views import (
    gate_control_view, vehicle_lookup_view, shift_log_view
)

# ── Navigation Config ─────────────────────────────────────────────────────────

ADMIN_NAV = [
    ("Dashboard", ft.Icons.DASHBOARD_OUTLINED),
    ("Group Management", ft.Icons.GROUPS_OUTLINED),
    ("Employee Accounts", ft.Icons.BADGE_OUTLINED),
    ("Pricing and Packages", ft.Icons.SELL_OUTLINED),
    ("Zones and Devices", ft.Icons.DEVICE_HUB_OUTLINED),
    ("Reports and Audit", ft.Icons.ANALYTICS_OUTLINED),
    ("Complaints", ft.Icons.FEEDBACK_OUTLINED),
    ("Settings", ft.Icons.SETTINGS_OUTLINED),
]

MANAGER_NAV = [
    ("Dashboard", ft.Icons.DASHBOARD_OUTLINED),
    ("Reports and Audit", ft.Icons.ANALYTICS_OUTLINED),
    ("Complaints", ft.Icons.FEEDBACK_OUTLINED),
]

GUARD_NAV = [
    ("Gate Control", ft.Icons.DOOR_FRONT_DOOR_OUTLINED),
    ("Alert Feed", ft.Icons.DOORBELL_OUTLINED),
    ("Vehicle Lookup", ft.Icons.SEARCH_OUTLINED),
    ("Shift Log", ft.Icons.HISTORY_OUTLINED),
]


def get_view_for(role: str, page_title: str) -> ft.Control:
    """Return the content view for the given role and page title."""
    mapping = {
        "Dashboard": dashboard_view,
        "Group Management": group_management_view,
        "Pricing and Packages": pricing_view,
        "Reports and Audit": reports_view,
        "Complaints": complaints_view,
        "Zones and Devices": zones_devices_view,
        "Gate Control": gate_control_view,
        "Vehicle Lookup": vehicle_lookup_view,
        "Shift Log": shift_log_view,
    }
    if page_title in mapping:
        result = mapping[page_title]()
        # Special case: Zones has its own Row layout
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
                    text_label("Coming soon", size=SIZE_H3, weight=W_SEMIBOLD),
                    text_label("This screen is under construction.", size=SIZE_BODY,
                               color=ft.Colors.with_opacity(0.60, PRIMARY)),
                ],
            ),
        ]),
        expand=True,
    )


# ── App Shell ─────────────────────────────────────────────────────────────────

def build_app_shell(page: ft.Page, role: str = "Admin") -> None:
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
        content_area.content = get_view_for(role, current_page[0])
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
            padding=ft.padding.symmetric(horizontal=16, vertical=10),
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
        border=ft.border.only(right=ft.border.BorderSide(1, ft.Colors.with_opacity(0.10, PRIMARY))),
        padding=ft.padding.symmetric(vertical=PAGE_PADDING, horizontal=12),
    )

    def build_sidebar():
        sidebar.content = ft.Column(
            expand=True,
            controls=[
                # Logo + App name
                ft.Padding(
                    padding=ft.padding.only(left=4, bottom=24),
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
                # Role chip at bottom
                ft.Container(
                    padding=ft.padding.only(left=4, top=16),
                    content=ft.Container(
                        content=ft.Text(role, font_family=FONT_FAMILY, size=SIZE_CAPTION,
                                        weight=W_MEDIUM, color=PRIMARY),
                        padding=ft.padding.symmetric(horizontal=10, vertical=4),
                        border=ft.border.all(1, PRIMARY),
                        border_radius=RADIUS_BUTTON,
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
            controls=[sidebar, content_area],
        )
    )
