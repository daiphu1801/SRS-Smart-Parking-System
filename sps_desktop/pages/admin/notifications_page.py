import json
from datetime import datetime
import flet as ft
from core.design_tokens import *
from core.supabase_client import register_realtime_callback, unregister_realtime_callback

# Store messages in memory so they persist when switching tabs
_message_log = []
_active_list_view: ft.ListView = None

def _handle_realtime_payload(payload: dict):
    """Callback fired when a message is received from Supabase."""
    now_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    # Extract the inner data object if wrapped by realtime-py
    data = payload.get("data", payload)
    
    event_type = data.get("type") or data.get("eventType") or data.get("event_type") or "UNKNOWN"
    record = data.get("record") or data.get("new") or data.get("old") or {}
    
    # Extract core data
    title = record.get("title", "Không có tiêu đề")
    content = record.get("content", "Không có nội dung")
    notif_type = record.get("type", "N/A")
    created_at = str(record.get("created_at", now_str))[:16].replace("T", " ")
    
    msg = {
        "timestamp": now_str,
        "event_type": event_type,
        "title": title,
        "content": content,
        "notif_type": notif_type,
        "created_at": created_at,
        "raw": json.dumps(payload, indent=2, ensure_ascii=False)
    }
    
    _message_log.append(msg)
    
    # Limit log size to prevent memory leak
    if len(_message_log) > 100:
        _message_log.pop(0)
        
    # Update UI if it's currently active
    if _active_list_view and _active_list_view.page:
        _add_message_to_view(_active_list_view, msg)
        try:
            _active_list_view.update()
        except Exception:
            pass

# Register it globally once when the module is imported
register_realtime_callback(_handle_realtime_payload)

def _add_message_to_view(list_view: ft.ListView, msg: dict):
    color = PRIMARY
    if msg["event_type"] == "INSERT": color = SUCCESS
    elif msg["event_type"] == "DELETE": color = DANGER
    elif msg["event_type"] == "UPDATE": color = WARNING
    icon = ft.Icons.NOTIFICATIONS_ACTIVE_ROUNDED
    if msg["notif_type"] == "BROADCAST_ALERT":
        icon = ft.Icons.WARNING_ROUNDED
        color = DANGER
        
    card = ft.Container(
        bgcolor=BG_ELEVATED,
        border_radius=RADIUS_MD,
        padding=PAD_MD,
        border=border_only(left=ft.BorderSide(4, color)),
        content=ft.Row(
            vertical_alignment=ft.CrossAxisAlignment.START,
            spacing=PAD_MD,
            controls=[
                ft.Container(
                    width=40, height=40,
                    bgcolor=PRIMARY_GLOW if color == PRIMARY else color + "20", # Soft background
                    border_radius=40,
                    alignment=ft.Alignment(0, 0),
                    content=ft.Icon(icon, color=color, size=20)
                ),
                ft.Column(
                    expand=True,
                    spacing=4,
                    controls=[
                        ft.Row(
                            alignment=ft.MainAxisAlignment.SPACE_BETWEEN,
                            controls=[
                                ft.Text(msg["title"], font_family=FONT_FAMILY, size=SIZE_BODY, weight=W_MEDIUM, color=TEXT_PRIMARY),
                                ft.Text(msg["created_at"], font_family=FONT_FAMILY, size=SIZE_CAPTION, color=TEXT_SECONDARY),
                            ]
                        ),
                        ft.Text(msg["content"], font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY),
                        ft.Text(f"Thao tác DB: {msg['event_type']} | Loại: {msg['notif_type']}", font_family=FONT_FAMILY, size=SIZE_CAPTION, color=TEXT_DISABLED),
                    ]
                )
            ]
        )
    )
    # Insert at the top so newest notifications are visible first
    list_view.controls.insert(0, card)

def build_notifications_page(page: ft.Page) -> ft.Control:
    global _active_list_view
    
    clear_btn = ft.OutlinedButton(
        content="Xóa Lịch Sử",
        icon=ft.Icons.CLEAR_ALL_ROUNDED,
        style=outlined_button_style(),
        on_click=lambda _: clear_log()
    )
    
    _active_list_view = ft.ListView(
        expand=True,
        spacing=PAD_MD,
        auto_scroll=True
    )
    
    def clear_log():
        _message_log.clear()
        _active_list_view.controls.clear()
        page.update()
        
    # Load historical messages
    for msg in _message_log:
        _add_message_to_view(_active_list_view, msg)

    return ft.Column(
        expand=True,
        spacing=PAD_LG,
        controls=[
            ft.Row(controls=[
                ft.Icon(ft.Icons.NOTIFICATIONS_ACTIVE_ROUNDED, color=PRIMARY, size=24),
                ft.Text("Thông Báo (Realtime)", font_family=FONT_FAMILY, size=SIZE_H2, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=True),
                clear_btn
            ]),
            ft.Text("Lắng nghe thay đổi trực tiếp từ Supabase Realtime (bảng notifications).", font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY),
            ft.Container(
                bgcolor=BG_CARD,
                border_radius=RADIUS_MD,
                border=border_all(1, BORDER),
                padding=PAD_LG,
                expand=True,
                content=_active_list_view
            )
        ]
    )
