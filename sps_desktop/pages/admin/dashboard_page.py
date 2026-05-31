"""
dashboard_page.py - Admin dashboard pulling live data from backend:
  GET /api/v1/admin/reports/occupancy
  GET /api/v1/admin/reports/revenue
  GET /api/v1/admin/reports/traffic
  GET /api/v1/admin/devices
  GET /api/v1/admin/parking-sessions
"""
import threading
import time
import flet as ft
from decimal import Decimal, InvalidOperation
from datetime import date, datetime, timedelta
from core.design_tokens import *
from core import api_client
from core import app_events
from core.settings import get_settings
from pages.admin.admin_ui import message_row


def build_dashboard(page: ft.Page) -> ft.Control:
    today     = date.today().isoformat()
    week_ago  = (date.today() - timedelta(days=6)).isoformat()
    state = {"refreshing": False}

    stat_values: dict[str, ft.Text] = {}

    def _set_stat(key: str, value: str):
        control = stat_values.get(key)
        if control is not None:
            control.value = value

    # ── Stat card builder ─────────────────────────────
    def stat_card(key: str, label: str, icon, color: str, sub: str = "") -> ft.Container:
        val_text = ft.Text("...", font_family=FONT_FAMILY, size=SIZE_H1,
                           weight=W_MEDIUM, color=color)
        stat_values[key] = val_text
        return ft.Container(
            expand=True,
            height=156,
            bgcolor=BG_CARD,
            border_radius=RADIUS_MD,
            border=border_all(1, BORDER),
            padding=PAD_LG,
            content=ft.Column(
                spacing=8,
                controls=[
                    ft.Row(
                        controls=[
                            ft.Container(
                                width=40, height=40,
                                bgcolor=WHITE,
                                border_radius=RADIUS_MD,
                                alignment=ft.Alignment(0, 0),
                                content=ft.Icon(icon, size=20, color=color),
                            ),
                        ],
                    ),
                    val_text,
                    ft.Text(label, font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY),
                    ft.Text(sub or " ", font_family=FONT_FAMILY, size=SIZE_CAPTION, color=TEXT_DISABLED),
                ],
            ),
        )

    card_inside    = stat_card("inside",     "Xe Đang Trong Bãi", ft.Icons.DIRECTIONS_CAR_ROUNDED, PRIMARY)
    card_available = stat_card("available",  "Chỗ Còn Trống",     ft.Icons.GARAGE_ROUNDED, SUCCESS)
    card_revenue   = stat_card("revenue",    "Doanh Thu Hôm Nay", ft.Icons.ATTACH_MONEY_ROUNDED, WARNING)
    card_total_tx  = stat_card("total_tx",   "Tổng Giao Dịch",    ft.Icons.RECEIPT_ROUNDED, INFO)
    card_success_tx= stat_card("success_tx", "GD Thành Công",     ft.Icons.CHECK_CIRCLE_ROUNDED, SUCCESS)

    # ── Active sessions table ─────────────────────────
    session_rows_col = ft.Column(spacing=0, controls=[message_row("Đang Tải...")])
    session_loading  = ft.ProgressRing(width=24, height=24, stroke_width=2, color=PRIMARY)
    dashboard_loading = ft.ProgressRing(width=24, height=24, stroke_width=2, color=PRIMARY, visible=False)

    sessions_section = ft.Container(
        bgcolor=BG_CARD,
        border_radius=RADIUS_MD,
        border=border_all(1, BORDER),
        padding=PAD_LG,
        expand=True,
        content=ft.Column(
            spacing=PAD_MD,
            controls=[
                ft.Row(
                    controls=[
                        ft.Icon(ft.Icons.DIRECTIONS_CAR_ROUNDED, color=PRIMARY, size=18),
                        ft.Text("Xe Đang Đỗ", font_family=FONT_FAMILY,
                                size=SIZE_H3, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=True),
                        session_loading,
                    ],
                ),
                ft.Divider(color=BORDER, height=1),
                # Header
                ft.Container(
                    bgcolor=BG_ELEVATED, border_radius=RADIUS_MD,
                    padding=ft.Padding(PAD_MD, PAD_SM, PAD_MD, PAD_SM),
                    content=ft.Row(
                        controls=[
                            ft.Text("Biển Số",     font_family=FONT_FAMILY, size=SIZE_CAPTION, weight=W_MEDIUM, color=TEXT_SECONDARY, expand=2),
                            ft.Text("Loại Xe",     font_family=FONT_FAMILY, size=SIZE_CAPTION, weight=W_MEDIUM, color=TEXT_SECONDARY, expand=1),
                            ft.Text("Giờ Vào",     font_family=FONT_FAMILY, size=SIZE_CAPTION, weight=W_MEDIUM, color=TEXT_SECONDARY, expand=2),
                            ft.Text("Trạng Thái",  font_family=FONT_FAMILY, size=SIZE_CAPTION, weight=W_MEDIUM, color=TEXT_SECONDARY, expand=1),
                        ],
                    ),
                ),
                ft.Container(height=4),
                session_rows_col,
            ],
        ),
    )

    def _status_badge(booking_detail_id):
        if booking_detail_id:
            return ft.Container(
                bgcolor=WHITE, border_radius=RADIUS_MD,
                padding=ft.Padding(8, 3, 8, 3),
                content=ft.Text("Thuê Bao", font_family=FONT_FAMILY, size=SIZE_CAPTION, color=INFO),
            )
        return ft.Container(
            bgcolor=WHITE, border_radius=RADIUS_MD,
            padding=ft.Padding(8, 3, 8, 3),
            content=ft.Text("Vãng Lai", font_family=FONT_FAMILY, size=SIZE_CAPTION, color=WARNING),
        )

    def _session_row(s: dict) -> ft.Container:
        entry = s.get("entry_time", "")[:16].replace("T", " ") if s.get("entry_time") else "-"
        vehicle_type = ui_title(s.get("vehicle_type_name") or s.get("vehicle_name") or "-")
        return ft.Container(
            padding=ft.Padding(PAD_MD, PAD_SM + 2, PAD_MD, PAD_SM + 2),
            border=border_only(bottom=ft.BorderSide(1, BORDER)),
            content=ft.Row(
                controls=[
                    ft.Text(s.get("vehicle_no", ""), font_family=FONT_FAMILY, size=SIZE_BODY,
                            weight=W_MEDIUM, color=TEXT_PRIMARY, expand=2),
                    ft.Text(vehicle_type, font_family=FONT_FAMILY,
                            size=SIZE_SMALL, color=TEXT_SECONDARY, expand=1),
                    ft.Text(entry, font_family=FONT_FAMILY, size=SIZE_SMALL, color=TEXT_SECONDARY, expand=2),
                    ft.Container(expand=1, content=_status_badge(s.get("booking_detail_id"))),
                ],
            ),
        )

    def _page_items(resp: dict | list) -> list:
        if isinstance(resp, list):
            return resp
        if not isinstance(resp, dict):
            return []
        data = resp.get("data")
        if isinstance(data, list):
            return data
        if isinstance(data, dict) and isinstance(data.get("content"), list):
            return data["content"]
        return []

    def _flatten_zones(zones: list[dict]) -> list[dict]:
        flat = []
        for zone in zones:
            flat.append(zone)
            flat.extend(_flatten_zones(zone.get("children") or []))
        return flat

    def _money_value(value) -> Decimal:
        if value is None or value == "" or isinstance(value, bool):
            return Decimal("0")
        if isinstance(value, Decimal):
            return value
        if isinstance(value, (int, float)):
            return Decimal(str(value))
        try:
            return Decimal(str(value).strip().replace(",", ""))
        except InvalidOperation:
            return Decimal("0")

    def _amount(row: dict) -> Decimal:
        if not isinstance(row, dict):
            return Decimal("0")
        return _money_value(row.get("total_amount"))

    def _date_label(row: dict) -> str:
        if not isinstance(row, dict):
            return today
        raw = row.get("time_label") or ""
        return raw[:10] if raw else today

    def _revenue_report(start_date: str, end_date: str) -> list:
        rows = api_client.admin_report_revenue(start_date, end_date, "DAY").get("data", [])
        return rows if isinstance(rows, list) else []

    # ── Revenue chart (simple bar-like text representation) ──
    revenue_col = ft.Column(spacing=4, controls=[message_row("Đang Tải...")])
    revenue_section = ft.Container(
        bgcolor=BG_CARD,
        border_radius=RADIUS_MD,
        border=border_all(1, BORDER),
        padding=PAD_LG,
        content=ft.Column(
            spacing=PAD_MD,
            controls=[
                ft.Row(
                    controls=[
                        ft.Icon(ft.Icons.BAR_CHART_ROUNDED, color=WARNING, size=18),
                        ft.Text("Doanh Thu 7 Ngày Qua", font_family=FONT_FAMILY,
                                size=SIZE_H3, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=True),
                    ],
                ),
                ft.Divider(color=BORDER, height=1),
                revenue_col,
            ],
        ),
    )

    def _revenue_bar(label: str, amount: float, max_amount: float) -> ft.Column:
        amount = _money_value(amount)
        max_amount = _money_value(max_amount)
        ratio = float(amount / max_amount) if max_amount > 0 else 0
        bar_width = max(4, int(ratio * 260))
        return ft.Column(
            spacing=4,
            controls=[
                ft.Row(
                    controls=[
                        ft.Text(label, font_family=FONT_FAMILY, size=SIZE_CAPTION, color=TEXT_SECONDARY, width=28),
                        ft.Container(
                            width=bar_width, height=18,
                            bgcolor=PRIMARY, border_radius=RADIUS_MD,
                        ),
                        ft.Text(_fmt_vnd_short(amount), font_family=FONT_FAMILY,
                                size=SIZE_CAPTION, color=TEXT_SECONDARY),
                    ],
                ),
            ],
        )

    # ── Fetch data ────────────────────────────────────
    def fetch_all():
        try:
            current_today = date.today().isoformat()
            current_week_ago = (date.today() - timedelta(days=6)).isoformat()
            total_cap = 0

            # Occupancy
            try:
                occ = api_client.admin_report_occupancy()
                zones = _flatten_zones(_page_items(occ))
                total_cap = sum(z.get("capacity", 0) or 0 for z in zones)
                total_occ = sum(z.get("current_occupancy", 0) or 0 for z in zones)
                _set_stat("inside", str(total_occ))
                _set_stat("available", str(max(0, total_cap - total_occ)))
            except Exception:
                _set_stat("inside", "Lỗi")
                _set_stat("available", "Lỗi")

            # Payment KPIs today
            try:
                kpi_resp = api_client.admin_report_kpi(current_today, current_today)
                kpi = kpi_resp.get("data", {}) if isinstance(kpi_resp, dict) else {}
                
                total_rev = _money_value(kpi.get("total_revenue") or kpi.get("totalRevenue"))
                _set_stat("revenue", _fmt_vnd_short(total_rev))
                
                total_tx = kpi.get("total_transactions") or kpi.get("totalTransactions") or 0
                _set_stat("total_tx", str(total_tx))
                
                success_tx = kpi.get("successful_transactions") or kpi.get("successfulTransactions") or 0
                _set_stat("success_tx", str(success_tx))
            except Exception:
                _set_stat("revenue", "Lỗi")
                _set_stat("total_tx", "Lỗi")
                _set_stat("success_tx", "Lỗi")

            # Active sessions
            try:
                # 1. Active sessions list (for the table)
                sess = api_client.admin_get_parking_sessions(size=10, is_currently_parked=True)
                active = _page_items(sess)
                
                session_rows = []
                for s in active:
                    session_rows.append(_session_row(s))
                if not active:
                    session_rows.append(message_row())
                session_rows_col.controls = session_rows



            except Exception as e:
                session_rows_col.controls = [message_row(f"Lỗi Tải Dữ Liệu: {e}", DANGER)]

            # Revenue chart (7 days)
            try:
                data7 = _revenue_report(current_week_ago, current_today)
                max_amount = max((_amount(r) for r in data7), default=Decimal("1"))
                revenue_rows = []
                revenue_section.visible = True
                if data7:
                    for r in data7:
                        label  = _date_label(r)[-5:]
                        amount = _amount(r)
                        revenue_rows.append(_revenue_bar(label, amount, max_amount))
                else:
                    revenue_rows.append(message_row())
                revenue_col.controls = revenue_rows
            except Exception:
                revenue_section.visible = True
                revenue_col.controls = [message_row("Không Có Kết Quả.", DANGER)]
        finally:
            session_loading.visible = False
            state["refreshing"] = False
            dashboard_loading.visible = False
            refresh_btn.disabled = False
            request_page_update(page)

    def refresh_dashboard(_=None):
        if state["refreshing"]:
            return
        state["refreshing"] = True
        dashboard_loading.visible = True
        session_loading.visible = True
        refresh_btn.disabled = True
        request_page_update(page)
        page.run_thread(fetch_all)

    refresh_btn = ft.OutlinedButton(
        content="Làm Mới",
        icon=ft.Icons.REFRESH_ROUNDED,
        on_click=refresh_dashboard,
        style=outlined_button_style(ft.Padding(12, 10, 12, 10)),
    )

    refresh_dashboard()

    # ── Layout ────────────────────────────────────────
    return ft.Column(
        expand=True,
        spacing=PAD_LG,
        controls=[
            ft.Row(
                controls=[
                    ft.Text("Tổng Quan Hệ Thống", font_family=FONT_FAMILY,
                            size=SIZE_H2, weight=W_MEDIUM, color=TEXT_PRIMARY, expand=True),
                    dashboard_loading,
                    refresh_btn,
                ],
            ),
            # Stat cards row
            ft.Row(spacing=PAD_MD, controls=[
                card_inside, card_available, card_revenue, card_total_tx, card_success_tx
            ]),
            # Lower section
            ft.Row(
                expand=True,
                spacing=PAD_LG,
                vertical_alignment=ft.CrossAxisAlignment.START,
                controls=[
                    ft.Container(expand=3, content=sessions_section, alignment=ft.Alignment(0, -1)),
                    ft.Container(expand=2, content=revenue_section, alignment=ft.Alignment(0, -1)),
                ],
            ),
        ],
    )


def _fmt_vnd_short(amount) -> str:
    try:
        if amount is None or amount == "":
            v = Decimal("0")
        elif isinstance(amount, Decimal):
            v = amount
        else:
            v = Decimal(str(amount))
        if v >= Decimal("1000000"):
            return f"{v / Decimal('1000000'):.1f}M ₫"
        if v >= Decimal("1000"):
            return f"{v / Decimal('1000'):.0f}k ₫"
        return f"{v:.0f} ₫"
    except Exception:
        return str(amount)
