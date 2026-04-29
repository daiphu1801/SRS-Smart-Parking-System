"""
mock_data.py — Static mock data for UI development
"""

MOCK_GROUPS_DATA = [
    {"name": "Tower A — Apt 1502", "code": "GRP001", "profile": "Household", "owner": "Nguyen Van A", "vehicles": "3"},
    {"name": "Tech Corp Ltd.", "code": "GRP002", "profile": "Corporate", "owner": "Le Thi B", "vehicles": "8"},
]

MOCK_COMPLAINTS_DATA = [
    {"id": "TICK-001", "customer": "Nguyen Van A", "category": "Gate not opening", "submitted": "24/04 08:15", "status": "Open"},
    {"id": "TICK-002", "customer": "Le Thi B", "category": "Invoice error", "submitted": "23/04 17:30", "status": "In Progress"},
    {"id": "TICK-003", "customer": "Tran Van C", "category": "Plate recognition", "submitted": "22/04 11:00", "status": "Resolved"},
]

MOCK_SUBSCRIPTION_PACKAGES = [
    ["Monthly — Standard", "Motorbike", "1 month", "200,000 đ", "Active", "Edit"],
    ["Monthly — Premium", "Car", "1 month", "450,000 đ", "Active", "Edit"],
    ["Quarterly — Standard", "Motorbike", "3 months", "550,000 đ", "Active", "Edit"],
]

MOCK_GUEST_TARIFFS = [
    ["Motorbike", "Weekday", "06:00–22:00", "30 min", "5,000 đ", "50,000 đ"],
    ["Car", "Weekday", "06:00–22:00", "30 min", "15,000 đ", "120,000 đ"],
]
