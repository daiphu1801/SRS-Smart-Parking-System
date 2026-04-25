"""
admin_portal.py — Admin & Manager Desktop Screens (Python Flet)
Screens: Dashboard, Group Management, Pricing & Packages, Zones & Devices, Reports, Complaints, Settings
"""
import flet as ft
from ..design_tokens import *
from ..shared_widgets import (
    filled_button, outlined_button, card, badge, section_divider,
    input_field, empty_state, stat_card, text_label
)


# ─── Dashboard ────────────────────────────────────────────────────────────────

def dashboard_view() -> ft.Column:
    stats_row = ft.Row(
        spacing=16,
        controls=[
            ft.Expanded(stat_card("Total Revenue Today", "4,250,000 đ", "+12% vs yesterday", positive=True)),
            ft.Expanded(stat_card("Active Sessions", "38", "+5 from this morning", positive=True)),
            ft.Expanded(stat_card("Occupied Slots", "142 / 200", "71% occupancy", positive=True)),
            ft.Expanded(stat_card("Open Complaints", "3", "-1 since yesterday", positive=False)),
        ],
    )

    chart_placeholder = ft.Container(
        height=280,
        border=ft.border.all(1, ft.Colors.with_opacity(0.15, PRIMARY)),
        border_radius=RADIUS_CARD,
        content=ft.Column(
            alignment=ft.MainAxisAlignment.CENTER,
            horizontal_alignment=ft.CrossAxisAlignment.CENTER,
            controls=[
                ft.Icon(ft.Icons.SHOW_CHART, size=40, color=ft.Colors.with_opacity(0.30, PRIMARY)),
                ft.Container(height=8),
                text_label("Revenue Overview", size=SIZE_H2, weight=W_SEMIBOLD),
            ],
        ),
    )

    legend_row = ft.Row(spacing=8, controls=[
        ft.Container(
            content=text_label("Subscription", size=SIZE_CAPTION),
            padding=ft.padding.symmetric(horizontal=10, vertical=4),
            border=ft.border.all(1, PRIMARY), border_radius=RADIUS_BUTTON,
        ),
        ft.Container(
            content=text_label("Guest", size=SIZE_CAPTION),
            padding=ft.padding.symmetric(horizontal=10, vertical=4),
            border=ft.border.all(1, PRIMARY), border_radius=RADIUS_BUTTON,
        ),
    ])

    # Recent sessions table
    headers = ["Plate", "Entry", "Exit", "Duration", "Fee", "Status", "Type"]
    table = ft.DataTable(
        border_radius=RADIUS_CARD,
        border=ft.border.all(1, ft.Colors.with_opacity(0.10, PRIMARY)),
        heading_row_height=40,
        data_row_min_height=48,
        data_row_max_height=48,
        divider_thickness=0.5,
        column_spacing=20,
        columns=[
            ft.DataColumn(ft.Text(h, font_family=FONT_FAMILY, size=SIZE_BODY_SMALL, weight=W_SEMIBOLD, color=PRIMARY))
            for h in headers
        ],
        rows=[
            ft.DataRow(cells=[
                ft.DataCell(text_label("51A-123.45", size=SIZE_BODY)),
                ft.DataCell(text_label("08:30", size=SIZE_BODY)),
                ft.DataCell(text_label("10:45", size=SIZE_BODY)),
                ft.DataCell(text_label("2h 15m", size=SIZE_BODY)),
                ft.DataCell(text_label("22,000 đ", size=SIZE_BODY)),
                ft.DataCell(badge("Completed", filled=True)),
                ft.DataCell(text_label("Guest", size=SIZE_BODY)),
            ]),
            ft.DataRow(
                color=ft.Colors.with_opacity(0.03, PRIMARY),
                cells=[
                    ft.DataCell(text_label("59B-678.90", size=SIZE_BODY)),
                    ft.DataCell(text_label("07:15", size=SIZE_BODY)),
                    ft.DataCell(text_label("—", size=SIZE_BODY, color=ft.Colors.with_opacity(0.40, PRIMARY))),
                    ft.DataCell(text_label("3h 25m", size=SIZE_BODY)),
                    ft.DataCell(text_label("0 đ", size=SIZE_BODY)),
                    ft.DataCell(badge("In Parking")),
                    ft.DataCell(text_label("Resident", size=SIZE_BODY)),
                ],
            ),
        ],
    )

    return ft.Column(
        spacing=SECTION_GAP,
        scroll=ft.ScrollMode.AUTO,
        controls=[
            stats_row,
            ft.Column(spacing=12, controls=[
                text_label("Revenue Overview", size=SIZE_H2, weight=W_SEMIBOLD),
                chart_placeholder,
                legend_row,
            ]),
            ft.Column(spacing=12, controls=[
                text_label("Recent Sessions", size=SIZE_H2, weight=W_SEMIBOLD),
                ft.Container(content=table, clip_behavior=ft.ClipBehavior.ANTI_ALIAS),
            ]),
        ],
    )


# ─── Group Management ─────────────────────────────────────────────────────────

def group_management_view() -> ft.Column:
    search = ft.TextField(
        hint_text="Search by name or code",
        hint_style=ft.TextStyle(font_family=FONT_FAMILY, size=SIZE_BODY,
                                color=ft.Colors.with_opacity(0.30, PRIMARY)),
        border_color=ft.Colors.with_opacity(0.12, PRIMARY),
        focused_border_color=PRIMARY,
        border_radius=RADIUS_CARD,
        border_width=1.5,
        height=40,
        width=300,
        content_padding=ft.padding.symmetric(horizontal=16, vertical=0),
        text_style=ft.TextStyle(font_family=FONT_FAMILY, size=SIZE_BODY, color=PRIMARY),
        prefix_icon=ft.Icons.SEARCH,
    )

    top_bar = ft.Row(
        alignment=ft.MainAxisAlignment.SPACE_BETWEEN,
        controls=[
            text_label("Group Management", size=SIZE_H1, weight=W_SEMIBOLD),
            ft.Row(spacing=12, controls=[search, filled_button("Add Group")]),
        ],
    )

    groups_data = [
        {"name": "Tower A — Apt 1502", "code": "GRP001", "profile": "Household", "owner": "Nguyen Van A", "vehicles": "3"},
        {"name": "Tech Corp Ltd.", "code": "GRP002", "profile": "Corporate", "owner": "Le Thi B", "vehicles": "8"},
    ]
    rows = []
    for i, g in enumerate(groups_data):
        rows.append(ft.DataRow(
            color=ft.Colors.with_opacity(0.03, PRIMARY) if i % 2 else ft.Colors.TRANSPARENT,
            cells=[
                ft.DataCell(text_label(g["name"], size=SIZE_BODY)),
                ft.DataCell(text_label(g["code"], size=SIZE_BODY)),
                ft.DataCell(text_label(g["profile"], size=SIZE_BODY)),
                ft.DataCell(text_label(g["owner"], size=SIZE_BODY)),
                ft.DataCell(text_label(g["vehicles"], size=SIZE_BODY)),
                ft.DataCell(ft.Row(spacing=8, controls=[
                    outlined_button("View", width=64),
                    outlined_button("Edit", width=64),
                ])),
            ],
        ))

    table = ft.DataTable(
        border=ft.border.all(1, ft.Colors.with_opacity(0.10, PRIMARY)),
        border_radius=RADIUS_CARD,
        heading_row_height=40,
        data_row_min_height=48,
        divider_thickness=0.5,
        columns=[
            ft.DataColumn(ft.Text(h, font_family=FONT_FAMILY, size=SIZE_BODY_SMALL, weight=W_SEMIBOLD, color=PRIMARY))
            for h in ["Group Name", "Code", "Profile", "Owner", "Vehicles", "Actions"]
        ],
        rows=rows,
    )

    return ft.Column(spacing=SECTION_GAP, scroll=ft.ScrollMode.AUTO, controls=[
        top_bar,
        ft.Container(content=table, clip_behavior=ft.ClipBehavior.ANTI_ALIAS),
    ])


# ─── Pricing & Packages ───────────────────────────────────────────────────────

def pricing_view() -> ft.Column:
    def _make_tab_content(tab_title, columns, rows_data):
        row_controls = []
        for i, row in enumerate(rows_data):
            row_controls.append(ft.DataRow(
                color=ft.Colors.with_opacity(0.03, PRIMARY) if i % 2 else ft.Colors.TRANSPARENT,
                cells=[ft.DataCell(text_label(v, size=SIZE_BODY)) for v in row],
            ))
        return ft.DataTable(
            border=ft.border.all(1, ft.Colors.with_opacity(0.10, PRIMARY)),
            border_radius=RADIUS_CARD,
            heading_row_height=40, data_row_min_height=48, divider_thickness=0.5,
            columns=[ft.DataColumn(ft.Text(c, font_family=FONT_FAMILY, size=SIZE_BODY_SMALL, weight=W_SEMIBOLD, color=PRIMARY)) for c in columns],
            rows=row_controls,
        )

    sub_table = _make_tab_content("Subscription Packages",
        ["Package Name", "Vehicle Type", "Duration", "Price", "Status", "Actions"],
        [
            ["Monthly — Standard", "Motorbike", "1 month", "200,000 đ", "Active", "Edit"],
            ["Monthly — Premium", "Car", "1 month", "450,000 đ", "Active", "Edit"],
            ["Quarterly — Standard", "Motorbike", "3 months", "550,000 đ", "Active", "Edit"],
        ]
    )
    guest_table = _make_tab_content("Guest Tariffs",
        ["Vehicle Type", "Day Type", "Time Range", "Base Block", "Price", "Max Daily"],
        [
            ["Motorbike", "Weekday", "06:00–22:00", "30 min", "5,000 đ", "50,000 đ"],
            ["Car", "Weekday", "06:00–22:00", "30 min", "15,000 đ", "120,000 đ"],
        ]
    )

    return ft.Column(spacing=SECTION_GAP, scroll=ft.ScrollMode.AUTO, controls=[
        ft.Row(alignment=ft.MainAxisAlignment.SPACE_BETWEEN, controls=[
            text_label("Pricing and Packages", size=SIZE_H1, weight=W_SEMIBOLD),
            filled_button("Add Package"),
        ]),
        ft.Tabs(
            tab_alignment=ft.TabAlignment.LEADING,
            tabs=[
                ft.Tab(text="Subscription Packages", content=ft.Container(
                    padding=ft.padding.only(top=16),
                    content=ft.Container(content=sub_table, clip_behavior=ft.ClipBehavior.ANTI_ALIAS),
                )),
                ft.Tab(text="Guest Tariffs", content=ft.Container(
                    padding=ft.padding.only(top=16),
                    content=ft.Container(content=guest_table, clip_behavior=ft.ClipBehavior.ANTI_ALIAS),
                )),
            ],
        ),
    ])


# ─── Reports & Audit ──────────────────────────────────────────────────────────

def reports_view() -> ft.Column:
    filter_bar = ft.Row(
        spacing=12,
        controls=[
            ft.TextField(
                hint_text="Start date",
                width=140, height=40, border_radius=RADIUS_CARD, border_width=1.5,
                border_color=ft.Colors.with_opacity(0.12, PRIMARY),
                text_style=ft.TextStyle(font_family=FONT_FAMILY, size=SIZE_BODY, color=PRIMARY),
                content_padding=ft.padding.symmetric(horizontal=12, vertical=0),
            ),
            ft.TextField(
                hint_text="End date",
                width=140, height=40, border_radius=RADIUS_CARD, border_width=1.5,
                border_color=ft.Colors.with_opacity(0.12, PRIMARY),
                text_style=ft.TextStyle(font_family=FONT_FAMILY, size=SIZE_BODY, color=PRIMARY),
                content_padding=ft.padding.symmetric(horizontal=12, vertical=0),
            ),
            ft.Dropdown(
                hint_text="All guards", width=160, height=40,
                border_radius=RADIUS_CARD, border_width=1.5,
                border_color=ft.Colors.with_opacity(0.12, PRIMARY),
                text_style=ft.TextStyle(font_family=FONT_FAMILY, size=SIZE_BODY, color=PRIMARY),
                options=[ft.dropdown.Option("Guard A"), ft.dropdown.Option("Guard B")],
            ),
            filled_button("Generate Report", width=160),
        ],
    )

    summary_cards = ft.Row(spacing=16, controls=[
        ft.Expanded(stat_card("Subscription Revenue", "3,200,000 đ", "", positive=True)),
        ft.Expanded(stat_card("Guest Revenue (QR)", "850,000 đ", "", positive=True)),
        ft.Expanded(stat_card("Cash Revenue", "200,000 đ", "", positive=True)),
    ])

    audit_table = ft.DataTable(
        border=ft.border.all(1, ft.Colors.with_opacity(0.10, PRIMARY)),
        border_radius=RADIUS_CARD,
        heading_row_height=40, data_row_min_height=48, divider_thickness=0.5,
        columns=[
            ft.DataColumn(ft.Text(h, font_family=FONT_FAMILY, size=SIZE_BODY_SMALL, weight=W_SEMIBOLD, color=PRIMARY))
            for h in ["Timestamp", "Guard", "Action Type", "Plate", "Notes"]
        ],
        rows=[
            ft.DataRow(cells=[
                ft.DataCell(text_label("24/04 09:32", size=SIZE_BODY)),
                ft.DataCell(text_label("Guard A", size=SIZE_BODY)),
                ft.DataCell(badge("Manual Gate Open")),
                ft.DataCell(text_label("51A-999.88", size=SIZE_BODY)),
                ft.DataCell(text_label("Camera blur", size=SIZE_BODY)),
            ]),
            ft.DataRow(
                color=ft.Colors.with_opacity(0.03, PRIMARY),
                cells=[
                    ft.DataCell(text_label("24/04 10:05", size=SIZE_BODY)),
                    ft.DataCell(text_label("Guard B", size=SIZE_BODY)),
                    ft.DataCell(badge("Cash Collected")),
                    ft.DataCell(text_label("60B-111.22", size=SIZE_BODY)),
                    ft.DataCell(text_label("25,000 đ collected", size=SIZE_BODY)),
                ],
            ),
        ],
    )

    return ft.Column(spacing=SECTION_GAP, scroll=ft.ScrollMode.AUTO, controls=[
        ft.Row(alignment=ft.MainAxisAlignment.SPACE_BETWEEN, controls=[
            text_label("Reports and Audit", size=SIZE_H1, weight=W_SEMIBOLD),
            outlined_button("Export to CSV"),
        ]),
        filter_bar,
        summary_cards,
        section_divider(),
        text_label("Audit Log", size=SIZE_H2, weight=W_SEMIBOLD),
        ft.Container(content=audit_table, clip_behavior=ft.ClipBehavior.ANTI_ALIAS),
    ])


# ─── Complaints ───────────────────────────────────────────────────────────────

def complaints_view() -> ft.Column:
    complaints_data = [
        {"id": "TICK-001", "customer": "Nguyen Van A", "category": "Gate not opening", "submitted": "24/04 08:15", "status": "Open"},
        {"id": "TICK-002", "customer": "Le Thi B", "category": "Invoice error", "submitted": "23/04 17:30", "status": "In Progress"},
        {"id": "TICK-003", "customer": "Tran Van C", "category": "Plate recognition", "submitted": "22/04 11:00", "status": "Resolved"},
    ]

    rows = []
    for i, c in enumerate(complaints_data):
        is_resolved = c["status"] == "Resolved"
        rows.append(ft.DataRow(
            color=ft.Colors.with_opacity(0.03, PRIMARY) if i % 2 else ft.Colors.TRANSPARENT,
            cells=[
                ft.DataCell(text_label(c["id"], size=SIZE_BODY)),
                ft.DataCell(text_label(c["customer"], size=SIZE_BODY)),
                ft.DataCell(text_label(c["category"], size=SIZE_BODY)),
                ft.DataCell(text_label(c["submitted"], size=SIZE_BODY)),
                ft.DataCell(badge(c["status"], filled=not is_resolved)),
            ],
        ))

    table = ft.DataTable(
        border=ft.border.all(1, ft.Colors.with_opacity(0.10, PRIMARY)),
        border_radius=RADIUS_CARD,
        heading_row_height=40, data_row_min_height=48, divider_thickness=0.5,
        columns=[
            ft.DataColumn(ft.Text(h, font_family=FONT_FAMILY, size=SIZE_BODY_SMALL, weight=W_SEMIBOLD, color=PRIMARY))
            for h in ["Ticket Id", "Customer", "Category", "Submitted At", "Status"]
        ],
        rows=rows,
    )

    return ft.Column(spacing=SECTION_GAP, scroll=ft.ScrollMode.AUTO, controls=[
        text_label("Complaints", size=SIZE_H1, weight=W_SEMIBOLD),
        ft.Container(content=table, clip_behavior=ft.ClipBehavior.ANTI_ALIAS),
    ])


# ─── Zones & Devices ─────────────────────────────────────────────────────────

def zones_devices_view() -> ft.Row:
    # Zone tree (left panel)
    zone_tree = ft.Container(
        width=240,
        bgcolor=BACKGROUND,
        border=ft.border.only(right=ft.border.BorderSide(1, ft.Colors.with_opacity(0.10, PRIMARY))),
        content=ft.Column(
            scroll=ft.ScrollMode.AUTO,
            controls=[
                ft.Container(
                    padding=ft.padding.only(left=PAGE_PADDING, right=PAGE_PADDING, top=PAGE_PADDING, bottom=8),
                    content=text_label("Zone Management", size=SIZE_H3, weight=W_SEMIBOLD),
                ),
                *_zone_items(),
                ft.Container(padding=PAGE_PADDING, content=outlined_button("Add Zone")),
            ],
        ),
    )

    # Zone detail (right panel)
    devices_table = ft.DataTable(
        border=ft.border.all(1, ft.Colors.with_opacity(0.10, PRIMARY)),
        border_radius=RADIUS_CARD,
        heading_row_height=40, data_row_min_height=48, divider_thickness=0.5,
        columns=[
            ft.DataColumn(ft.Text(h, font_family=FONT_FAMILY, size=SIZE_BODY_SMALL, weight=W_SEMIBOLD, color=PRIMARY))
            for h in ["Device Name", "Type", "Direction", "IP Address", "Status", "Last Ping"]
        ],
        rows=[
            ft.DataRow(cells=[
                ft.DataCell(text_label("CAM-001", size=SIZE_BODY)),
                ft.DataCell(text_label("LPR Camera", size=SIZE_BODY)),
                ft.DataCell(badge("IN")),
                ft.DataCell(text_label("192.168.1.10", size=SIZE_BODY)),
                ft.DataCell(ft.Row(spacing=6, controls=[
                    ft.Container(width=8, height=8, bgcolor=PRIMARY, border_radius=4),
                    text_label("Online", size=SIZE_BODY),
                ])),
                ft.DataCell(text_label("10:32:01", size=SIZE_BODY)),
            ]),
            ft.DataRow(
                color=ft.Colors.with_opacity(0.03, PRIMARY),
                cells=[
                    ft.DataCell(text_label("BARRIER-001", size=SIZE_BODY)),
                    ft.DataCell(text_label("Barrier Gate", size=SIZE_BODY)),
                    ft.DataCell(badge("IN")),
                    ft.DataCell(text_label("192.168.1.11", size=SIZE_BODY)),
                    ft.DataCell(ft.Row(spacing=6, controls=[
                        ft.Container(width=8, height=8, bgcolor=PRIMARY, border_radius=4),
                        text_label("Online", size=SIZE_BODY),
                    ])),
                    ft.DataCell(text_label("10:32:00", size=SIZE_BODY)),
                ],
            ),
        ],
    )

    detail_panel = ft.Container(
        expand=True,
        padding=PAGE_PADDING,
        content=ft.Column(spacing=16, scroll=ft.ScrollMode.AUTO, controls=[
            text_label("Basement B1 — Entry Gate", size=SIZE_H2, weight=W_SEMIBOLD),
            text_label("18 / 40 slots occupied", size=SIZE_BODY, color=ft.Colors.with_opacity(0.60, PRIMARY)),
            section_divider(),
            ft.Row(alignment=ft.MainAxisAlignment.SPACE_BETWEEN, controls=[
                text_label("Assigned Devices", size=SIZE_H3, weight=W_SEMIBOLD),
                ft.Row(spacing=8, controls=[
                    outlined_button("Ping All"),
                    outlined_button("Add Device"),
                ]),
            ]),
            ft.Container(content=devices_table, clip_behavior=ft.ClipBehavior.ANTI_ALIAS),
        ]),
    )

    return ft.Row(expand=True, spacing=0, controls=[zone_tree, detail_panel])


def _zone_items():
    items = [
        ("Building A", ft.Icons.APARTMENT_OUTLINED, 0),
        ("Basement B1", ft.Icons.FOUNDATION_OUTLINED, 1),
        ("Entry Gate", ft.Icons.DOOR_FRONT_DOOR_OUTLINED, 2),
        ("Exit Gate", ft.Icons.DOOR_BACK_DOOR_OUTLINED, 2),
        ("Basement B2", ft.Icons.FOUNDATION_OUTLINED, 1),
    ]
    result = []
    for label, icon, depth in items:
        is_active = label == "Basement B1"
        result.append(ft.Container(
            bgcolor=PRIMARY if is_active else ft.Colors.TRANSPARENT,
            border_radius=RADIUS_BUTTON,
            padding=ft.padding.only(left=PAGE_PADDING + depth * 12, right=12, top=8, bottom=8),
            content=ft.Row(spacing=8, controls=[
                ft.Icon(icon, size=18, color=BACKGROUND if is_active else PRIMARY),
                ft.Text(label, font_family=FONT_FAMILY, size=SIZE_BODY,
                        color=BACKGROUND if is_active else PRIMARY),
            ]),
        ))
    return result
