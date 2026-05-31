"""
settings_page.py - Local desktop runtime configuration.
"""
import flet as ft

from core.design_tokens import *
from core.settings import DEFAULT_SETTINGS, get_settings, reset_settings, save_settings
from pages.admin.admin_ui import (
    bool_value, close_dialog, confirm_dialog, dropdown, float_value, int_value,
    show_snack, text_field, validate_required, validate_required_float,
    validate_required_int,
)


def build_settings_page(page: ft.Page) -> ft.Control:
    int_setting_keys = {
        "min_plate_length", "min_votes", "jpeg_quality", "max_plate_jobs",
        "demo_vehicle_type_id", "dashboard_refresh_interval_seconds",
    }
    state = {"fields": {}, "checks": {}, "int_fields": [], "float_fields": []}
    status_text = ft.Text("", font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY)
    demo_video_row = ft.Row(spacing=PAD_MD)
    rtsp_row = ft.Row(spacing=PAD_MD)

    def _field(key: str, label: str, tooltip: str, *, number: bool = False):
        field = text_field(label, "", number=number)
        field.tooltip = tooltip
        state["fields"][key] = field
        if number:
            (state["int_fields"] if key in int_setting_keys else state["float_fields"]).append(field)
        return field

    def _select(key: str, label: str, options: list[tuple[str, str]], tooltip: str):
        control = dropdown(label, options, "", width=240)
        control.tooltip = tooltip
        state["fields"][key] = control
        return control

    def _check(key: str, label: str, tooltip: str):
        control = ft.Checkbox(
            label=label,
            value=False,
            tooltip=tooltip,
            fill_color={
                ft.ControlState.DEFAULT: WHITE,
                ft.ControlState.SELECTED: PRIMARY,
                ft.ControlState.HOVERED: WHITE,
            },
            check_color=WHITE,
            active_color=PRIMARY,
            hover_color=WHITE,
            focus_color=WHITE,
            overlay_color=WHITE,
            border_side=ft.BorderSide(1, PRIMARY),
            mouse_cursor=ft.MouseCursor.CLICK,
            label_style=ft.TextStyle(color=TEXT_PRIMARY, font_family=FONT_FAMILY, size=SIZE_BODY),
        )
        state["checks"][key] = control
        return control

    def _section(title: str, controls: list[ft.Control]):
        return ft.Container(
            bgcolor=BG_CARD,
            border_radius=RADIUS_MD,
            border=border_all(1, BORDER),
            padding=PAD_LG,
            content=ft.Column(
                spacing=PAD_MD,
                controls=[
                    ft.Text(title, font_family=FONT_FAMILY, size=SIZE_H3,
                            weight=W_MEDIUM, color=TEXT_PRIMARY),
                    *controls,
                ],
            ),
        )

    def _load_values(values: dict, *, update: bool = True):
        for key, field in state["fields"].items():
            value = values.get(key, DEFAULT_SETTINGS.get(key, ""))
            field.value = "" if value is None else str(value)
        for key, check in state["checks"].items():
            check.value = bool(values.get(key, DEFAULT_SETTINGS.get(key, False)))
        _sync_camera_mode(update=False)
        status_text.value = "Cấu Hình Đã Tải."
        if update:
            request_page_update(page)

    def _sync_camera_mode(update: bool = True):
        mode = str(state["fields"].get("camera_mode").value or "DEMO_VIDEO").upper()
        is_rtsp = mode == "RTSP"
        demo_video_row.visible = not is_rtsp
        rtsp_row.visible = is_rtsp
        if update:
            request_page_update(page)

    def _collect() -> dict:
        current = get_settings()
        text_values = {
            key: field.value
            for key, field in state["fields"].items()
        }
        for key, default in DEFAULT_SETTINGS.items():
            text_values.setdefault(key, current.get(key, default))
        camera_mode = str(text_values["camera_mode"] or "DEMO_VIDEO").upper()
        current.update({
            "camera_mode": text_values["camera_mode"],
            "demo_video_path": text_values["demo_video_path"].strip() if camera_mode == "DEMO_VIDEO" else current.get("demo_video_path", DEFAULT_SETTINGS["demo_video_path"]),
            "rtsp_url": text_values["rtsp_url"].strip() if camera_mode == "RTSP" else current.get("rtsp_url", DEFAULT_SETTINGS["rtsp_url"]),
            "lpr_model_path": text_values["lpr_model_path"].strip(),
            "vehicle_model_path": text_values["vehicle_model_path"].strip(),
            "plate_confidence": float_value(text_values["plate_confidence"], DEFAULT_SETTINGS["plate_confidence"]),
            "min_plate_length": int_value(text_values["min_plate_length"], DEFAULT_SETTINGS["min_plate_length"]),
            "scan_seconds": float_value(text_values["scan_seconds"], DEFAULT_SETTINGS["scan_seconds"]),
            "min_votes": int_value(text_values["min_votes"], DEFAULT_SETTINGS["min_votes"]),
            "min_vote_ratio": float_value(text_values["min_vote_ratio"], DEFAULT_SETTINGS["min_vote_ratio"]),
            "inference_fps": float_value(text_values["inference_fps"], DEFAULT_SETTINGS["inference_fps"]),
            "stream_fps": float_value(text_values["stream_fps"], DEFAULT_SETTINGS["stream_fps"]),
            "jpeg_quality": int_value(text_values["jpeg_quality"], DEFAULT_SETTINGS["jpeg_quality"]),
            "max_plate_jobs": int_value(text_values["max_plate_jobs"], DEFAULT_SETTINGS["max_plate_jobs"]),
            "plate_debounce_seconds": float_value(text_values["plate_debounce_seconds"], DEFAULT_SETTINGS["plate_debounce_seconds"]),
            "entry_device_code": text_values["entry_device_code"].strip(),
            "exit_device_code": text_values["exit_device_code"].strip(),
            "barrier_in_code": text_values["barrier_in_code"].strip(),
            "barrier_out_code": text_values["barrier_out_code"].strip(),
            "demo_vehicle_type_id": int_value(text_values["demo_vehicle_type_id"], DEFAULT_SETTINGS["demo_vehicle_type_id"]),
            "qr_poll_interval_seconds": float_value(text_values["qr_poll_interval_seconds"], DEFAULT_SETTINGS["qr_poll_interval_seconds"]),
            "qr_timeout_seconds": float_value(text_values["qr_timeout_seconds"], DEFAULT_SETTINGS["qr_timeout_seconds"]),
            "dashboard_refresh_interval_seconds": int_value(text_values["dashboard_refresh_interval_seconds"], DEFAULT_SETTINGS["dashboard_refresh_interval_seconds"]),
        })
        for key, check in state["checks"].items():
            current[key] = bool_value(check.value, DEFAULT_SETTINGS.get(key, False))
        return current

    def save(_):
        camera_mode = str(state["fields"]["camera_mode"].value or "DEMO_VIDEO").upper()
        fields = [
            field for key, field in state["fields"].items()
            if key not in ("demo_video_path", "rtsp_url")
        ]
        fields.append(state["fields"]["rtsp_url"] if camera_mode == "RTSP" else state["fields"]["demo_video_path"])
        if not validate_required(page, *fields):
            status_text.value = "Thiếu Trường Bắt Buộc."
            request_page_update(page)
            return
        if state["int_fields"] and not validate_required_int(page, *state["int_fields"]):
            status_text.value = "Sai Định Dạng Số Nguyên."
            request_page_update(page)
            return
        if state["float_fields"] and not validate_required_float(page, *state["float_fields"]):
            status_text.value = "Sai Định Dạng Số."
            request_page_update(page)
            return
        try:
            _load_values(save_settings(_collect()))
            status_text.value = "Đã Lưu Cấu Hình."
            show_snack(page, "Đã Lưu Cấu Hình.")
        except Exception as ex:
            status_text.value = "Lỗi Lưu Cấu Hình."
            show_snack(page, f"Lỗi Lưu Cấu Hình: {ex}", DANGER)

    def reload(_=None):
        _load_values(get_settings())

    def restore_defaults(_):
        def do_reset():
            try:
                _load_values(reset_settings())
                status_text.value = "Đã Khôi Phục Mặc Định."
                show_snack(page, "Đã Khôi Phục Mặc Định.")
            except Exception as ex:
                show_snack(page, f"Lỗi Khôi Phục: {ex}", DANGER)

        confirm_dialog(page, "Khôi Phục Mặc Định", "Xác Nhận Khôi Phục Toàn Bộ Cấu Hình?", do_reset)

    ai_section = _section("AI Và Nhận Diện", [
        ft.Row(spacing=PAD_MD, controls=[
            _field("lpr_model_path", "Đường Dẫn Model Lpr", "Model Yolo Dùng Để Nhận Diện Biển Số.", number=False),
            _field("vehicle_model_path", "Đường Dẫn Model Xe", "Model Xe Dự Phòng Cho Tương Lai.", number=False),
        ]),
        ft.Row(spacing=PAD_MD, controls=[
            _field("plate_confidence", "Confidence Biển Số", "Ngưỡng Confidence Tối Thiểu Của Yolo Lpr.", number=True),
            _field("min_plate_length", "Ký Tự Tối Thiểu", "Số Ký Tự Tối Thiểu Sau OCR.", number=True),
            _field("scan_seconds", "Thời Gian Vote", "Số Giây Vote Sau Khi Tìm Thấy Biển Số.", number=True),
        ]),
        ft.Row(spacing=PAD_MD, controls=[
            _field("min_votes", "Vote Tối Thiểu", "Số Vote Tối Thiểu Để Chấp Nhận Biển Số.", number=True),
            _field("min_vote_ratio", "Tỷ Lệ Vote Tối Thiểu", "Tỷ Lệ Vote Của Biển Số Thắng, Từ 0 Đến 1.", number=True),
            _field("plate_debounce_seconds", "Giãn Cách Biển Số", "Số Giây Trước Khi Báo Lại Cùng Một Biển Số.", number=True),
        ]),
        ft.Row(spacing=PAD_MD, controls=[
            _field("inference_fps", "FPS AI", "Số Lần Chạy Model Mỗi Giây.", number=True),
            _field("stream_fps", "FPS UI", "Số Frame Tối Đa Đẩy Lên Giao Diện Mỗi Giây.", number=True),
            _field("jpeg_quality", "Chất Lượng JPEG", "Chất Lượng Frame JPEG Từ 10 Đến 95.", number=True),
            _field("max_plate_jobs", "Luồng AI", "Số Job Nhận Diện Biển Số Chạy Song Song.", number=True),
        ]),
    ])

    camera_mode_select = _select(
        "camera_mode",
        "Chế Độ Camera",
        [("DEMO_VIDEO", "Demo Video"), ("RTSP", "RTSP")],
        "Chọn Demo Video Hoặc RTSP.",
    )
    for option in camera_mode_select.options:
        if option.key == "RTSP":
            option.text = "RTSP"
            option.content = ft.Text(
                "RTSP",
                font_family=FONT_FAMILY,
                size=SIZE_BODY,
                color=TEXT_PRIMARY,
                weight=W_REGULAR,
            )
    camera_mode_select.on_select = lambda _: _sync_camera_mode()
    demo_video_row.controls = [
        _field("demo_video_path", "Đường Dẫn Video Demo", "Video Demo Dùng Cho Nút Play Vào Và Play Ra."),
    ]
    rtsp_row.controls = [
        _field("rtsp_url", "RTSP URL", "Luồng Camera RTSP."),
    ]

    camera_section = _section("Video Và Camera", [
        ft.Row(spacing=PAD_MD, controls=[
            camera_mode_select,
        ]),
        demo_video_row,
        rtsp_row,
    ])

    refresh_section = _section("Làm Mới Dữ Liệu", [
        ft.Row(spacing=PAD_MD, controls=[
            _field("dashboard_refresh_interval_seconds", "Chu Kỳ Tổng Quan", "Số Giây Giữa Mỗi Lần Tự Làm Mới Tổng Quan.", number=True),
        ]),
    ])

    _load_values(get_settings(), update=False)

    return ft.Column(
        expand=True,
        spacing=PAD_LG,
        controls=[
            ft.Row(controls=[
                ft.Icon(ft.Icons.SETTINGS_ROUNDED, color=PRIMARY, size=24),
                ft.Text("Cấu Hình", font_family=FONT_FAMILY,
                        size=SIZE_H2, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=True),
                status_text,
                ft.OutlinedButton(content="Làm Mới", icon=ft.Icons.REFRESH_ROUNDED,
                                  tooltip="Tải Lại Cấu Hình Từ File.",
                                  on_click=reload, style=outlined_button_style()),
                ft.OutlinedButton(content="Khôi Phục", icon=ft.Icons.RESTORE_ROUNDED,
                                  tooltip="Khôi Phục Giá Trị Mặc Định.",
                                  on_click=restore_defaults, style=outlined_button_style()),
                ft.ElevatedButton(content="Lưu", icon=ft.Icons.SAVE_ROUNDED,
                                  tooltip="Lưu Cấu Hình Vào File Local.",
                                  on_click=save, style=filled_button_style()),
            ]),
            ai_section,
            camera_section,
            refresh_section,
        ],
    )
