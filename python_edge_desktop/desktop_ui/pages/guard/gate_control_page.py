import flet as ft
from desktop_ui.design_tokens import *
from desktop_ui.components.ui.shared_widgets import text_label, filled_button, outlined_button, card, badge, section_divider, empty_state
from ai_engine.vision_service import vision_engine

def gate_control_view(page: ft.Page = None) -> ft.Row:
    """Left 60% — Gate Control Panel | Right 40% — Live Alert Feed + LPR Camera"""
    plate_input = ft.TextField(
        hint_text="Nhập biển số",
        hint_style=ft.TextStyle(font_family=FONT_FAMILY, size=36,
                                color=ft.Colors.with_opacity(0.30, PRIMARY)),
        text_style=ft.TextStyle(font_family=FONT_FAMILY, size=36, weight=W_SEMIBOLD, color=PRIMARY),
        border_color=ft.Colors.with_opacity(0.12, PRIMARY),
        focused_border_color=PRIMARY,
        border_radius=RADIUS_CARD,
        border_width=1.5,
        height=72,
        content_padding=ft.Padding.symmetric(horizontal=16, vertical=0),
    )

    result_card = card(
        padding=CARD_PADDING,
        content=ft.Column(spacing=8, controls=[
            ft.Row(alignment=ft.MainAxisAlignment.SPACE_BETWEEN, controls=[
                text_label("51A-123.45", size=SIZE_H1, weight=W_SEMIBOLD),
                badge("Thuê Bao — Hợp Lệ", filled=True),
            ]),
            text_label("Chủ xe: Nguyễn Văn A | Căn hộ 1502", size=SIZE_BODY,
                       color=ft.Colors.with_opacity(0.60, PRIMARY)),
            text_label("Vào lúc: 08:30 hôm nay | Đã đỗ: 2h 15m", size=SIZE_BODY),
            ft.Container(height=4),
            ft.Row(spacing=8, controls=[
                ft.Container(expand=True, content=filled_button("Mở Cổng")),
                ft.Container(expand=True, content=outlined_button("Đã Thu Tiền Mặt")),
            ]),
        ]),
    )

    # Override log
    log_items = [
        ("10:05", "Mở cổng tay — 60B-111.22 (camera mờ)", ft.Icons.DOOR_FRONT_DOOR_OUTLINED),
        ("09:32", "Đã thu tiền mặt — 51A-999.88 — 25,000 đ", ft.Icons.PAYMENTS_OUTLINED),
        ("08:50", "Mở cổng tay — 29A-555.11 (thuê bao)", ft.Icons.DOOR_FRONT_DOOR_OUTLINED),
    ]

    log_controls = [
        ft.Row(spacing=12, controls=[
            ft.Icon(icon, size=16, color=ft.Colors.with_opacity(0.60, PRIMARY)),
            ft.Column(spacing=2, expand=True, controls=[
                text_label(desc, size=SIZE_BODY),
                text_label(time, size=SIZE_BODY_SMALL, color=ft.Colors.with_opacity(0.60, PRIMARY)),
            ]),
        ])
        for time, desc, icon in log_items
    ]

    left_panel = ft.Container(
        expand=3,
        padding=PAGE_PADDING,
        content=ft.Column(spacing=16, scroll=ft.ScrollMode.AUTO, controls=[
            text_label("Kiểm Soát Cổng", size=SIZE_H1, weight=W_SEMIBOLD),
            plate_input,
            ft.Row(spacing=8, controls=[
                ft.Container(expand=True, content=filled_button("Xe Vào")),
                ft.Container(expand=True, content=filled_button("Xe Ra")),
            ]),
            result_card,
            section_divider(),
            text_label("Lịch Sử Ghi Đè Hôm Nay", size=SIZE_H3, weight=W_SEMIBOLD),
            *log_controls,
        ]),
    )

    # ── Right: Live Camera & Alert Feed ───────────────────────────────────────
    camera_feed = ft.Image(
        src="",
        fit="contain",
    )
    
    camera_container = ft.Container(
        content=camera_feed,
        bgcolor=ft.Colors.BLACK,
        height=240,
        border_radius=RADIUS_CARD,
        clip_behavior=ft.ClipBehavior.HARD_EDGE,
        alignment=ft.Alignment(0, 0),
    )

    alert_cards = [
        {"plate": "59B-678.90", "zone": "Hàng 3 B1", "type": "Chắn lối đi", "time": "10:32"},
        {"plate": "41A-333.22", "zone": "Cổng Vào B", "type": "Lỗi nhận dạng LPR", "time": "10:28"},
    ]

    alert_items = []
    for a in alert_cards:
        alert_items.append(
            card(
                padding=CARD_PADDING,
                content=ft.Column(spacing=8, controls=[
                    ft.Row(alignment=ft.MainAxisAlignment.SPACE_BETWEEN, controls=[
                        text_label(a["plate"], size=SIZE_H3, weight=W_SEMIBOLD),
                        text_label(a["time"], size=SIZE_BODY_SMALL, color=ft.Colors.with_opacity(0.60, PRIMARY)),
                    ]),
                    text_label(f"{a['zone']} · {a['type']}", size=SIZE_BODY,
                               color=ft.Colors.with_opacity(0.60, PRIMARY)),
                    filled_button("Xử Lý"),
                ]),
            )
        )
        alert_items.append(ft.Container(height=12))

    right_panel = ft.Container(
        expand=2,
        bgcolor=SURFACE,
        border=ft.Border.only(left=ft.BorderSide(1, ft.Colors.with_opacity(0.10, PRIMARY))),
        padding=PAGE_PADDING,
        content=ft.Column(
            spacing=16,
            scroll=ft.ScrollMode.AUTO,
            controls=[
                text_label("Luồng Camera Trực Tiếp (LPR)", size=SIZE_H2, weight=W_SEMIBOLD),
                camera_container,
                section_divider(),
                text_label("Cảnh Báo", size=SIZE_H2, weight=W_SEMIBOLD),
                *(alert_items if alert_items else [empty_state(
                    ft.Icons.CHECK_CIRCLE_OUTLINE, "Không có cảnh báo", "Mọi thứ ổn định."
                )]),
            ],
        ),
    )

    # Callbacks for VisionEngine (called from background thread)
    def on_lpr_frame(b64_str):
        camera_feed.src = b64_str
        if camera_feed.page:
            try:
                camera_feed.update()
            except Exception:
                pass

    def on_plate_detected(plate):
        plate_input.value = plate
        if plate_input.page:
            try:
                plate_input.update()
            except Exception:
                pass

    # Start stream
    vision_engine.start_lpr_stream(
        video_source=r"f:\Project_personal\SRS_project\demo.mp4",
        on_frame=on_lpr_frame,
        on_plate_detected=on_plate_detected,
    )

    return ft.Row(expand=True, spacing=0, controls=[left_panel, right_panel])
