"""
main.py — SPS Desktop App entry point.
Handles routing between login, guard kiosk, and admin portal.
"""
import flet as ft
from core.design_tokens import *
from core import api_client
from core.api_client import set_notification_callback
from pages.admin.admin_ui import show_snack
from core.supabase_client import start_realtime_listener

# Supabase listener will be started in login_page.py after successful login
# start_realtime_listener()


ADMIN_PERMISSIONS = {
    "ACCOUNT_READ",
    "ROLE_READ",
    "EMPLOYEE_READ",
    "CUSTOMER_READ",
    "CUSTOMER_GROUP_READ",
    "BOOKING_READ",
    "PAYMENT_READ",
    "DEVICE_READ",
    "ZONE_READ",
    "TARIFF_READ",
    "PACKAGE_READ",
    "VEHICLE_TYPE_READ",
}


def main(page: ft.Page):
    page.title   = "Smart Parking Desktop"
    page.bgcolor = BG_BASE
    page.fonts   = {FONT_FAMILY: FONT_URL}
    page.theme   = ft.Theme(
        font_family=FONT_FAMILY,
        scaffold_bgcolor=WHITE,
        canvas_color=WHITE,
        card_bgcolor=WHITE,
    )
    page.window.min_width  = MIN_WINDOW_WIDTH
    page.window.min_height = MIN_WINDOW_HEIGHT
    page.window.width      = DEFAULT_WINDOW_WIDTH
    page.window.height     = DEFAULT_WINDOW_HEIGHT
    page.padding = 0

    # Global API Notifications
    set_notification_callback(
        lambda msg, is_error: show_snack(page, msg, DANGER if is_error else PRIMARY)
    )

    def show_login():
        page.controls.clear()
        from pages.login_page import build_login_page
        build_login_page(page, on_login_success=on_login_success)
        page.update()

    def on_login_success(account_type: str):
        page.controls.clear()
        role = (account_type or "").upper()
        permissions = set(api_client.get_permissions())

        if role == "CUSTOMER":
            # Desktop has no customer portal; keep customer accounts out of admin/guard workflows.
            show_login()
            return

        try:
            if role == "ADMIN" or permissions.intersection(ADMIN_PERMISSIONS):
                from pages.admin.admin_shell import build_admin_portal
                portal = build_admin_portal(page, on_logout=show_login)
                page.add(ft.Container(content=portal, expand=True))
            elif role == "GUARD" or role == "EMPLOYEE":
                from pages.guard.guard_kiosk import build_guard_kiosk
                kiosk = build_guard_kiosk(page, on_logout=show_login)
                page.add(ft.Container(content=kiosk, expand=True))
            else:
                # Fallback: show admin portal for unknown roles
                from pages.admin.admin_shell import build_admin_portal
                portal = build_admin_portal(page, on_logout=show_login)
                page.add(ft.Container(content=portal, expand=True))
        except Exception as e:
            import traceback
            traceback.print_exc()
            raise e

        page.update()

    show_login()


if __name__ == "__main__":
    try:
        ft.app(main)
    except Exception:
        ft.run(main)
