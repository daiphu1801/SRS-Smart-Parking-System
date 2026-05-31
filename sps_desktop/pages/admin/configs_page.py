import flet as ft
from core.design_tokens import *
from core import api_client
from pages.admin.admin_ui import show_snack, text_field

def build_configs_page(page: ft.Page) -> ft.Control:
    
    # --- State ---
    state = {
        "configs": [],
        "loading": False,
    }

    status_text = ft.Text("", font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY)
    content_area = ft.Column(expand=True, scroll=ft.ScrollMode.AUTO, spacing=PAD_LG)

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

    def do_update(cfg, new_value):
        try:
            api_client.admin_update_system_config(cfg["id"], new_value, cfg.get("description"))
            status_text.value = f"Đã cập nhật {cfg.get('configKey')}."
            page.update()
        except Exception:
            status_text.value = f"Lỗi cập nhật {cfg.get('configKey')}."
            page.update()

    def build_ui():
        content_area.controls.clear()
        
        if state["loading"]:
            content_area.controls.append(
                ft.Row([ft.ProgressRing(color=PRIMARY)], alignment=ft.MainAxisAlignment.CENTER)
            )
            page.update()
            return
            
        if not state["configs"]:
            content_area.controls.append(
                ft.Container(
                    padding=PAD_XL,
                    alignment=ft.Alignment(0, 0),
                    content=ft.Text("Không Có Dữ Liệu.", font_family=FONT_FAMILY, color=TEXT_SECONDARY)
                )
            )
            page.update()
            return

        # Build fields in rows of 2 or 3
        field_rows = []
        current_row_controls = []
        
        for c in state["configs"]:
            field = text_field(
                label=c.get("configKey"), 
                value=c.get("configValue", ""),
                expand=True
            )
            field.tooltip = c.get("description", "Nhấn Enter để lưu")
            field.on_submit = lambda e, cfg=c: do_update(cfg, e.control.value.strip())
            
            current_row_controls.append(field)
            if len(current_row_controls) == 2:
                field_rows.append(ft.Row(spacing=PAD_MD, controls=current_row_controls))
                current_row_controls = []
                
        if current_row_controls:
            # fill the rest of the row with empty containers to maintain width
            while len(current_row_controls) < 2:
                current_row_controls.append(ft.Container(expand=True))
            field_rows.append(ft.Row(spacing=PAD_MD, controls=current_row_controls))
            
        section = _section("Cấu Hình Từ Server", field_rows)
        content_area.controls.append(section)
        page.update()

    def load_data(_=None):
        state["loading"] = True
        status_text.value = "Đang tải cấu hình..."
        build_ui()
        
        try:
            resp = api_client.admin_get_system_configs()
            data = resp.get("data", [])
            state["configs"] = data
            status_text.value = "Cấu Hình Đã Tải."
        except Exception as e:
            status_text.value = "Lỗi tải cấu hình."
            state["configs"] = []
        finally:
            state["loading"] = False
            build_ui()

    # Initial Load
    load_data()

    return ft.Column(
        expand=True,
        spacing=PAD_LG,
        controls=[
            ft.Row(controls=[
                ft.Icon(ft.Icons.TUNE_ROUNDED, color=PRIMARY, size=24),
                ft.Text("Cấu Hình Hệ Thống", font_family=FONT_FAMILY,
                        size=SIZE_H2, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=True),
                status_text,
                ft.OutlinedButton(content="Làm Mới", icon=ft.Icons.REFRESH_ROUNDED,
                                  tooltip="Tải Lại Cấu Hình Từ Server.",
                                  on_click=load_data, style=outlined_button_style()),
            ]),
            ft.Divider(height=1, color=BORDER),
            content_area
        ]
    )
