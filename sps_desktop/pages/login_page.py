"""
login_page.py - Real login form that calls POST /api/v1/auth/login and other auth endpoints
"""
import flet as ft
from core.design_tokens import *
from core import api_client
from core.supabase_client import start_realtime_listener

def build_login_page(page: ft.Page, on_login_success) -> None:
    page.title    = "Smart Parking - Đăng Nhập"
    page.bgcolor  = BG_BASE
    page.fonts    = {FONT_FAMILY: FONT_URL}
    page.theme    = ft.Theme(
        font_family=FONT_FAMILY,
        scaffold_bgcolor=WHITE,
        canvas_color=WHITE,
        card_bgcolor=WHITE,
    )
    page.window.min_width  = MIN_WINDOW_WIDTH
    page.window.min_height = MIN_WINDOW_HEIGHT
    page.window.width      = DEFAULT_WINDOW_WIDTH
    page.window.height     = DEFAULT_WINDOW_HEIGHT
    page.horizontal_alignment = ft.CrossAxisAlignment.CENTER
    page.vertical_alignment   = ft.MainAxisAlignment.CENTER

    state = {"phone": ""}

    # --- UI Elements ---
    phone_field = ft.TextField(
        label="Số Điện Thoại",
        border_color=BORDER, focused_border_color=PRIMARY,
        label_style=ft.TextStyle(color=TEXT_SECONDARY, font_family=FONT_FAMILY),
        text_style=ft.TextStyle(color=TEXT_PRIMARY, font_family=FONT_FAMILY),
        bgcolor=BG_ELEVATED, border_radius=RADIUS_MD, cursor_color=PRIMARY, expand=True,
    )

    password_field = ft.TextField(
        label="Mật Khẩu", password=True,
        border_color=BORDER, focused_border_color=PRIMARY,
        label_style=ft.TextStyle(color=TEXT_SECONDARY, font_family=FONT_FAMILY),
        text_style=ft.TextStyle(color=TEXT_PRIMARY, font_family=FONT_FAMILY),
        bgcolor=BG_ELEVATED, border_radius=RADIUS_MD, cursor_color=PRIMARY, expand=True,
    )

    otp_field = ft.TextField(
        label="Mã OTP",
        border_color=BORDER, focused_border_color=PRIMARY,
        label_style=ft.TextStyle(color=TEXT_SECONDARY, font_family=FONT_FAMILY),
        text_style=ft.TextStyle(color=TEXT_PRIMARY, font_family=FONT_FAMILY),
        bgcolor=BG_ELEVATED, border_radius=RADIUS_MD, cursor_color=PRIMARY, expand=True,
    )

    new_password_field = ft.TextField(
        label="Mật Khẩu Mới", password=True,
        border_color=BORDER, focused_border_color=PRIMARY,
        label_style=ft.TextStyle(color=TEXT_SECONDARY, font_family=FONT_FAMILY),
        text_style=ft.TextStyle(color=TEXT_PRIMARY, font_family=FONT_FAMILY),
        bgcolor=BG_ELEVATED, border_radius=RADIUS_MD, cursor_color=PRIMARY, expand=True,
    )

    error_text = ft.Text("", color=DANGER, size=SIZE_SMALL, font_family=FONT_FAMILY, visible=False)
    loading_ring_phone = ft.ProgressRing(width=20, height=20, stroke_width=2, color=PRIMARY, visible=False)
    loading_ring_login = ft.ProgressRing(width=20, height=20, stroke_width=2, color=PRIMARY, visible=False)
    loading_ring_register = ft.ProgressRing(width=20, height=20, stroke_width=2, color=PRIMARY, visible=False)

    # --- Handlers ---
    def switch_panel(panel_name):
        phone_panel.visible = (panel_name == "phone")
        login_panel.visible = (panel_name == "login")
        register_panel.visible = (panel_name == "register")
        error_text.visible = False
        loading_ring_phone.visible = False
        loading_ring_login.visible = False
        loading_ring_register.visible = False
        request_page_update(page)

    def do_check_phone(_):
        phone = phone_field.value.strip()
        if not phone:
            error_text.value = "Vui Lòng Nhập Số Điện Thoại."
            error_text.visible = True
            request_page_update(page)
            return

        error_text.visible = False
        loading_ring_phone.visible = True
        btn_check_phone.disabled = True
        request_page_update(page)

        try:
            resp = api_client.check_phone(phone)
            data = resp.get("data", {})
            action = data.get("action")
            
            state["phone"] = phone

            if action == "REQUIRE_LOGIN_PASSWORD":
                switch_panel("login")
            elif action == "REQUIRE_CREATE_PASSWORD" or action == "REQUIRE_OTP_ACTIVATION":
                switch_panel("register")
            else:
                raise ValueError(f"Hành động không xác định: {action}")
                
        except Exception as e:
            error_text.value = f"Lỗi: {e}"
            error_text.visible = True
        finally:
            loading_ring_phone.visible = False
            btn_check_phone.disabled = False
            request_page_update(page)

    def process_login_success(data):
        token = data.get("access_token")
        account_type = data.get("account_type")
        account_id = data.get("account_id")
        permissions = data.get("permissions") or []

        if not token:
            raise ValueError("Không Nhận Được Token Từ Server.")

        api_client.set_token(token, account_type, account_id, permissions)
        
        # Start Supabase realtime listener with the JWT token
        start_realtime_listener(token)

        on_login_success(account_type)

    def do_login(_):
        phone = state["phone"]
        password = password_field.value.strip()

        if not password:
            error_text.value = "Vui Lòng Nhập Mật Khẩu."
            error_text.visible = True
            request_page_update(page)
            return

        error_text.visible = False
        loading_ring_login.visible = True
        btn_login.disabled = True
        request_page_update(page)

        try:
            resp = api_client.login(phone, password)
            process_login_success(resp.get("data", {}))
        except Exception as e:
            error_text.value = f"Đăng Nhập Thất Bại: {e}"
            error_text.visible = True
            loading_ring_login.visible = False
            btn_login.disabled = False
            request_page_update(page)

    def do_register(_):
        phone = state["phone"]
        otp = otp_field.value.strip()
        password = new_password_field.value.strip()

        if not otp or not password:
            error_text.value = "Vui Lòng Nhập Đầy Đủ Thông Tin."
            error_text.visible = True
            request_page_update(page)
            return

        error_text.visible = False
        loading_ring_register.visible = True
        btn_register.disabled = True
        request_page_update(page)

        try:
            resp = api_client.register(phone, password, otp)
            process_login_success(resp.get("data", {}))
        except Exception as e:
            error_text.value = f"Kích Hoạt Thất Bại: {e}"
            error_text.visible = True
            loading_ring_register.visible = False
            btn_register.disabled = False
            request_page_update(page)

    phone_field.on_submit = do_check_phone
    password_field.on_submit = do_login
    new_password_field.on_submit = do_register

    btn_check_phone = ft.ElevatedButton("Tiếp Tục", icon=ft.Icons.ARROW_FORWARD_ROUNDED, expand=True, on_click=do_check_phone, style=filled_button_style(ft.Padding(0, 14, 0, 14)))
    btn_login = ft.ElevatedButton("Đăng Nhập", icon=ft.Icons.LOGIN_ROUNDED, expand=True, on_click=do_login, style=filled_button_style(ft.Padding(0, 14, 0, 14)))
    btn_register = ft.ElevatedButton("Kích Hoạt & Đăng Nhập", icon=ft.Icons.HOW_TO_REG_ROUNDED, expand=True, on_click=do_register, style=filled_button_style(ft.Padding(0, 14, 0, 14)))
    
    btn_back_login = ft.TextButton("Quay Lại", icon=ft.Icons.ARROW_BACK_ROUNDED, on_click=lambda _: switch_panel("phone"))
    btn_back_register = ft.TextButton("Quay Lại", icon=ft.Icons.ARROW_BACK_ROUNDED, on_click=lambda _: switch_panel("phone"))

    phone_panel = ft.Column(
        spacing=16,
        controls=[
            ft.Text("Đăng Nhập Hệ Thống", font_family=FONT_FAMILY, size=SIZE_H3, weight=W_MEDIUM, color=TEXT_PRIMARY),
            ft.Text("Nhập Số Điện Thoại Của Bạn.", font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY),
            ft.Container(height=4),
            phone_field,
            ft.Container(height=4),
            ft.Row(controls=[btn_check_phone, loading_ring_phone]),
        ],
        visible=True
    )

    login_panel = ft.Column(
        spacing=16,
        controls=[
            btn_back_login,
            ft.Text("Nhập Mật Khẩu", font_family=FONT_FAMILY, size=SIZE_H3, weight=W_MEDIUM, color=TEXT_PRIMARY),
            ft.Text("Tài khoản của bạn đã được đăng ký.", font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY),
            ft.Container(height=4),
            password_field,
            ft.Container(height=4),
            ft.Row(controls=[btn_login, loading_ring_login]),
        ],
        visible=False
    )

    register_panel = ft.Column(
        spacing=16,
        controls=[
            btn_back_register,
            ft.Text("Kích Hoạt Tài Khoản", font_family=FONT_FAMILY, size=SIZE_H3, weight=W_MEDIUM, color=TEXT_PRIMARY),
            ft.Text("Nhập mã OTP đã gửi đến điện thoại và tạo mật khẩu mới.", font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY),
            ft.Container(height=4),
            otp_field,
            new_password_field,
            ft.Container(height=4),
            ft.Row(controls=[btn_register, loading_ring_register]),
        ],
        visible=False
    )

    main_card = ft.Container(
        width=420,
        bgcolor=BG_CARD,
        border_radius=RADIUS_MD,
        border=border_all(1, BORDER),
        padding=PAD_XL,
        content=ft.Column(
            spacing=0,
            controls=[
                phone_panel,
                login_panel,
                register_panel,
                ft.Container(height=8),
                error_text,
            ],
        ),
    )

    page.add(
        ft.Column(
            expand=True,
            horizontal_alignment=ft.CrossAxisAlignment.CENTER,
            alignment=ft.MainAxisAlignment.CENTER,
            controls=[
                # Logo
                ft.Row(
                    spacing=10,
                    alignment=ft.MainAxisAlignment.CENTER,
                    controls=[
                        ft.Container(
                            width=44, height=44,
                            bgcolor=PRIMARY,
                            border_radius=RADIUS_MD,
                            alignment=ft.Alignment(0, 0),
                            content=ft.Icon(ft.Icons.GARAGE_ROUNDED, size=26, color=WHITE),
                        ),
                        ft.Column(spacing=0, controls=[
                            ft.Text("Smart Parking", font_family=FONT_FAMILY, size=SIZE_H2, weight=W_MEDIUM, color=TEXT_PRIMARY),
                            ft.Text("Desktop Portal", font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY),
                        ]),
                    ],
                ),
                ft.Container(height=40),
                main_card,
            ],
        )
    )

