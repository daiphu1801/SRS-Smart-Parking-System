"""
main_desktop.py — Entry point for Smart Parking Desktop App (Python Flet)
Run: flet run python_edge_desktop/main_desktop.py
"""
import flet as ft
from desktop_ui.pages.auth.login_page import build_login_page
from desktop_ui.components.layout.app_shell import build_app_shell


def main(page: ft.Page):
    def on_login_success(role: str):
        """Called by login page once credentials are verified."""
        page.clean()
        build_app_shell(page, role=role, on_logout=on_logout)

    def on_logout():
        page.clean()
        build_login_page(page, on_login_success)

    build_login_page(page, on_login_success)


if __name__ == "__main__":
    ft.run(main)
