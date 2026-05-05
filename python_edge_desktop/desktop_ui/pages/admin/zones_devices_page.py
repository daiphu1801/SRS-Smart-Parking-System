import flet as ft
from desktop_ui.design_tokens import *
from desktop_ui.components.ui.shared_widgets import text_label, outlined_button, section_divider, badge
from ai_engine.vision_service import vision_engine

def zones_devices_view(page: ft.Page = None) -> ft.Row:
    # Zone tree (left panel)
    zone_tree = ft.Container(
        width=240,
        bgcolor=BACKGROUND,
        border=ft.Border.only(right=ft.BorderSide(1, ft.Colors.with_opacity(0.10, PRIMARY))),
        content=ft.Column(
            scroll=ft.ScrollMode.AUTO,
            controls=[
                ft.Container(
                    padding=ft.Padding(left=PAGE_PADDING, right=PAGE_PADDING, top=PAGE_PADDING, bottom=8),
                    content=text_label("Quản Lý Khu Vực", size=SIZE_H3, weight=W_SEMIBOLD),
                ),
                *_zone_items(),
                ft.Container(padding=PAGE_PADDING, content=outlined_button("Thêm Khu Vực")),
            ],
        ),
    )

    # Zone detail (right panel)
    devices_table = ft.DataTable(expand=True, column_spacing=80,
        border=ft.Border.all(1, ft.Colors.with_opacity(0.10, PRIMARY)),
        border_radius=RADIUS_CARD,
        heading_row_height=40, data_row_min_height=48, divider_thickness=0.5,
        columns=[
            ft.DataColumn(ft.Text(h, font_family=FONT_FAMILY, size=SIZE_BODY_SMALL, weight=W_SEMIBOLD, color=PRIMARY))
            for h in ["Tên Thiết Bị", "Loại", "Hướng", "Địa chỉ IP", "Trạng Thái", "Ping Cuối"]
        ],
        rows=[
            ft.DataRow(cells=[
                ft.DataCell(text_label("CAM-001", size=SIZE_BODY)),
                ft.DataCell(text_label("Camera LPR", size=SIZE_BODY)),
                ft.DataCell(badge("VÀO")),
                ft.DataCell(text_label("192.168.1.10", size=SIZE_BODY)),
                ft.DataCell(ft.Row(spacing=6, controls=[
                    ft.Container(width=8, height=8, bgcolor=PRIMARY, border_radius=4),
                    text_label("Trực tuyến", size=SIZE_BODY),
                ])),
                ft.DataCell(text_label("10:32:01", size=SIZE_BODY)),
            ]),
            ft.DataRow(
                color=ft.Colors.with_opacity(0.03, PRIMARY),
                cells=[
                    ft.DataCell(text_label("BARRIER-001", size=SIZE_BODY)),
                    ft.DataCell(text_label("Cổng Barrier", size=SIZE_BODY)),
                    ft.DataCell(badge("VÀO")),
                    ft.DataCell(text_label("192.168.1.11", size=SIZE_BODY)),
                    ft.DataCell(ft.Row(spacing=6, controls=[
                        ft.Container(width=8, height=8, bgcolor=PRIMARY, border_radius=4),
                        text_label("Trực tuyến", size=SIZE_BODY),
                    ])),
                    ft.DataCell(text_label("10:32:00", size=SIZE_BODY)),
                ],
            ),
        ],
    )

    occupancy_text = text_label("Đang tải số chỗ...", size=SIZE_BODY, color=ft.Colors.with_opacity(0.60, PRIMARY))

    camera_feed = ft.Image(
        src="",
        fit="contain",
    )
    
    camera_container = ft.Container(
        content=camera_feed,
        bgcolor=ft.Colors.BLACK,
        height=320,
        border_radius=RADIUS_CARD,
        clip_behavior=ft.ClipBehavior.HARD_EDGE,
        alignment=ft.Alignment(0, 0),
    )

    detail_panel = ft.Container(
        expand=True,
        padding=PAGE_PADDING,
        content=ft.Column(spacing=16, scroll=ft.ScrollMode.AUTO, controls=[
            text_label("Tầng Hầm B1 — Cổng Vào", size=SIZE_H2, weight=W_SEMIBOLD),
            occupancy_text,
            camera_container,
            section_divider(),
            ft.Row(alignment=ft.MainAxisAlignment.SPACE_BETWEEN, controls=[
                text_label("Thiết Bị Gán Kèm", size=SIZE_H3, weight=W_SEMIBOLD),
                ft.Row(spacing=8, controls=[
                    outlined_button("Ping Tất Cả"),
                    outlined_button("Thêm Thiết Bị"),
                ]),
            ]),
            ft.Container(content=devices_table, clip_behavior=ft.ClipBehavior.ANTI_ALIAS),
        ]),
    )

    # Callbacks for VisionEngine (called from background thread)
    def on_parking_frame(b64_str):
        camera_feed.src = b64_str
        if camera_feed.page:
            try:
                camera_feed.update()
            except Exception:
                pass

    def on_parking_update(occupied, total):
        occupancy_text.value = f"{occupied} / {total} chỗ đã dùng"
        if occupancy_text.page:
            try:
                occupancy_text.update()
            except Exception:
                pass

    # Start stream
    vision_engine.start_parking_stream(
        video_source=r"f:\Project_personal\SRS_project\demo.mp4",
        on_frame=on_parking_frame,
        on_parking_update=on_parking_update,
    )

    return ft.Row(expand=True, spacing=0, controls=[zone_tree, detail_panel])


def _zone_items():
    items = [
        ("Tòa nhà A", ft.Icons.APARTMENT_OUTLINED, 0),
        ("Tầng Hầm B1", ft.Icons.FOUNDATION_OUTLINED, 1),
        ("Cổng Vào", ft.Icons.DOOR_FRONT_DOOR_OUTLINED, 2),
        ("Cổng Ra", ft.Icons.DOOR_BACK_DOOR_OUTLINED, 2),
        ("Tầng Hầm B2", ft.Icons.FOUNDATION_OUTLINED, 1),
    ]
    result = []
    for label, icon, depth in items:
        is_active = label == "Tầng Hầm B1"
        result.append(ft.Container(
            bgcolor=PRIMARY if is_active else ft.Colors.TRANSPARENT,
            border_radius=RADIUS_BUTTON,
            padding=ft.Padding(left=PAGE_PADDING + depth * 12, right=12, top=8, bottom=8),
            content=ft.Row(spacing=8, controls=[
                ft.Icon(icon, size=18, color=BACKGROUND if is_active else PRIMARY),
                ft.Text(label, font_family=FONT_FAMILY, size=SIZE_BODY,
                        color=BACKGROUND if is_active else PRIMARY),
            ]),
        ))
    return result
