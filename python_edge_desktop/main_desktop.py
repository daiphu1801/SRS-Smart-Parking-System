"""
main_desktop.py — Entry point for Smart Parking Desktop App (Python Flet)
Run: flet run python_edge_desktop/main_desktop.py
"""
import flet as ft
from desktop_ui.app_shell import build_app_shell


def main(page: ft.Page):
    # For development — change role to "Manager" or "Guard" to test different shells
    role = "Admin"  # Options: "Admin" | "Manager" | "Guard"
    build_app_shell(page, role=role)


if __name__ == "__main__":
    ft.run(main)
