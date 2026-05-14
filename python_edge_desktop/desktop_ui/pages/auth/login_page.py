"""
login_page.py — Dev-mode login: 3 role buttons, no credentials needed
"""
import flet as ft
from desktop_ui.design_tokens import *


def build_login_page(page: ft.Page, on_login_success) -> None:
    page.title       = "Smart Parking — Đăng Nhập"
    page.bgcolor     = BACKGROUND
    page.fonts       = {"Inter": "https://fonts.gstatic.com/s/inter/v13/UcCO3FwrK3iLTeHuS_fvQtMwCp50KnMw2boKoduKmMEVuLyfAZ9hiA.woff2"}
    page.theme       = ft.Theme(font_family="Inter")
    page.window.min_width  = 960
    page.window.min_height = 600
    page.horizontal_alignment = ft.CrossAxisAlignment.CENTER
    page.vertical_alignment   = ft.MainAxisAlignment.CENTER

    def role_btn(label: str, role: str, icon) -> ft.Container:
        def on_click(_):
            on_login_success(role)

        return ft.GestureDetector(
            mouse_cursor=ft.MouseCursor.CLICK,
            content=ft.Container(
                on_click=on_click,
                border=ft.Border.all(1.5, PRIMARY),
                border_radius=RADIUS_CARD,
                padding=ft.Padding(left=24, right=32, top=20, bottom=20),
                bgcolor=BACKGROUND,
                ink=True,
            content=ft.Row(spacing=16, controls=[
                ft.Container(
                    width=44, height=44,
                    bgcolor=PRIMARY,
                    border_radius=RADIUS_BUTTON,
                    alignment=ft.Alignment(0, 0),
                    content=ft.Icon(icon, size=22, color=BACKGROUND),
                ),
                ft.Column(spacing=2, controls=[
                    ft.Text(label, font_family=FONT_FAMILY, size=SIZE_H3,
                            weight=W_SEMIBOLD, color=PRIMARY),
                    ft.Text(f"Đăng nhập với vai trò {role}", font_family=FONT_FAMILY, size=SIZE_BODY_SMALL,
                            color=ft.Colors.with_opacity(0.50, PRIMARY)),
                ]),
                ft.Container(expand=True),
                ft.Icon(ft.Icons.CHEVRON_RIGHT_ROUNDED, size=20,
                        color=ft.Colors.with_opacity(0.35, PRIMARY)),
            ]),
        ))

    page.add(
        ft.Column(
            expand=True,
            horizontal_alignment=ft.CrossAxisAlignment.CENTER,
            alignment=ft.MainAxisAlignment.CENTER,
            spacing=0,
            controls=[
                # Logo
                ft.Row(spacing=10, alignment=ft.MainAxisAlignment.CENTER, controls=[
                    ft.Icon(ft.Icons.LOCAL_PARKING_ROUNDED, size=28, color=PRIMARY),
                    ft.Text("Smart Parking", font_family=FONT_FAMILY, size=SIZE_H2,
                            weight=W_SEMIBOLD, color=PRIMARY),
                ]),
                ft.Container(height=8),
                ft.Text("Chọn vai trò để tiếp tục", font_family=FONT_FAMILY,
                        size=SIZE_BODY, color=ft.Colors.with_opacity(0.50, PRIMARY)),
                ft.Container(height=40),
                # Role buttons
                ft.Container(
                    width=420,
                    content=ft.Column(spacing=12, controls=[
                        role_btn("Quản trị viên", "Admin",   ft.Icons.ADMIN_PANEL_SETTINGS_OUTLINED),
                        role_btn("Quản lý",       "Manager", ft.Icons.MANAGE_ACCOUNTS_OUTLINED),
                        role_btn("Bảo vệ cổng",   "Guard",   ft.Icons.DOOR_FRONT_DOOR_OUTLINED),
                    ]),
                ),
                ft.Container(height=48),
                ft.Text("Chế độ nhà phát triển — không cần mật khẩu",
                        font_family=FONT_FAMILY, size=SIZE_CAPTION,
                        color=ft.Colors.with_opacity(0.30, PRIMARY)),
            ],
        )
    )
