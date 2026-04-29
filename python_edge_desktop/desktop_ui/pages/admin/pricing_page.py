import flet as ft
from desktop_ui.design_tokens import *
from desktop_ui.components.ui.shared_widgets import text_label, filled_button
from desktop_ui.data.mock_data import MOCK_SUBSCRIPTION_PACKAGES, MOCK_GUEST_TARIFFS

def pricing_view() -> ft.Column:
    def _make_tab_content(tab_title, columns, rows_data):
        row_controls = []
        for i, row in enumerate(rows_data):
            row_controls.append(ft.DataRow(
                color=ft.Colors.with_opacity(0.03, PRIMARY) if i % 2 else ft.Colors.TRANSPARENT,
                cells=[ft.DataCell(text_label(v, size=SIZE_BODY)) for v in row],
            ))
        return ft.DataTable(expand=True, column_spacing=80,
            border=ft.Border.all(1, ft.Colors.with_opacity(0.10, PRIMARY)),
            border_radius=RADIUS_CARD,
            heading_row_height=40, data_row_min_height=48, divider_thickness=0.5,
            columns=[ft.DataColumn(ft.Text(c, font_family=FONT_FAMILY, size=SIZE_BODY_SMALL, weight=W_SEMIBOLD, color=PRIMARY)) for c in columns],
            rows=row_controls,
        )

    sub_table = _make_tab_content("Subscription Packages",
        ["Package Name", "Vehicle Type", "Duration", "Price", "Status", "Actions"],
        MOCK_SUBSCRIPTION_PACKAGES
    )
    guest_table = _make_tab_content("Guest Tariffs",
        ["Vehicle Type", "Day Type", "Time Range", "Base Block", "Price", "Max Daily"],
        MOCK_GUEST_TARIFFS
    )

    sub_col = ft.Container(
        padding=ft.Padding(top=16),
        content=ft.Container(content=sub_table, clip_behavior=ft.ClipBehavior.ANTI_ALIAS),
        visible=True
    )
    guest_col = ft.Container(
        padding=ft.Padding(top=16),
        content=ft.Container(content=guest_table, clip_behavior=ft.ClipBehavior.ANTI_ALIAS),
        visible=False
    )

    def on_tab_change(e):
        sub_col.visible = (e.control.selected_index == 0)
        guest_col.visible = (e.control.selected_index == 1)
        sub_col.update()
        guest_col.update()

    tabs_ctrl = ft.Tabs(
        length=2,
        selected_index=0,
        on_change=on_tab_change,
        content=ft.Column(spacing=0, controls=[
            ft.TabBar(
                tab_alignment=ft.TabAlignment.START,
                tabs=[
                    ft.Tab(label="Subscription Packages"),
                    ft.Tab(label="Guest Tariffs"),
                ]
            ),
            sub_col,
            guest_col,
        ])
    )

    return ft.Column(spacing=SECTION_GAP, scroll=ft.ScrollMode.AUTO, controls=[
        ft.Row(alignment=ft.MainAxisAlignment.SPACE_BETWEEN, controls=[
            text_label("Pricing and Packages", size=SIZE_H1, weight=W_SEMIBOLD),
            filled_button("Add Package"),
        ]),
        tabs_ctrl,
    ])
