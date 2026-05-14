// ========================================= // ENUMS (Định nghĩa các
kiểu dữ liệu chuẩn) // ========================================= Enum
account_type { ADMIN GUARD CUSTOMER }

Enum general_status { ACTIVE LOCKED PENDING }

Enum device_status { ONLINE OFFLINE MAINTENANCE }

Enum device_type { LPR_CAM BARRIER LED }

Enum direction { IN OUT BOTH // Dành cho các cổng chạy 2 chiều }

Enum zone_type { BUILDING FLOOR AREA GATE }

Enum day_type { WEEKDAY WEEKEND HOLIDAY }

Enum booking_status { ACTIVE EXPIRED PENDINGEnum payment_method { CASH PAYOS VNPAY VIETQR }

Enum payment_status { PENDING SUCCESS FAILED REFUNDED }

// ========================================= // 1. MODULE:
AUTHENTICATION & RBAC // ========================================= Table
Roles { id int \[pk, increment\] role_name varchar(50) \[unique\]
description nvarchar(255) is_active boolean \[default: true\] }

Table Functions { id int \[pk, increment\] func_name varchar(50)
description nvarchar(255) }

Table Actions { id int \[pk, increment\] action_code varchar(20)
description nvarchar(255) }

Table Role_Function_Action { role_id int \[pk\] func_id int \[pk\]
action_id int \[pk\] }

Table Accounts { id int \[pk, increment\] username varchar(50)
\[unique\] password_hash varchar(255) role_id int account_type
account_type status general_status last_login timestamp created_at
timestamp \[default: `now()`\] updated_at timestamp \[default:
`now()`\] }

// ========================================= // 2. MODULE: IDENTITY & HR
// ========================================= Table Employees { id int
\[pk, increment\] account_id int \[unique\] full_name nvarchar(100)
phone varchar(15) \[unique\] email varchar(100) is_online boolean
\[default: false\] created_by int \[note: 'Nhân viên nào tạo nhân viên
này (nếu có)'\] created_at timestamp }

Table GroupsProfiles { id int \[pk, increment\] profile_code varchar(20)
\[unique, note: 'RESIDENT, BUSINESS, VIP...'\] profile_name
nvarchar(50) }

Table GroupsCustomers { id int \[pk, increment\] profile_id int
group_code varchar(50) \[unique, note: 'Mã Căn hộ (A101) hoặc Mã Cty
(FPT_01)'\] group_name nvarchar(100) master_account_id int \[note:
'Chủ hộ/Đại diện doanh nghiệp - Trỏ về Accounts'\] created_by int
\[note: 'ID của Employee tạo group này'\] created_at timestamp }

Table Customers { id int \[pk, increment\] account_id int \[unique\]
group_id int full_name nvarchar(100) phone varchar(15) \[unique\]
address nvarchar(255) created_at timestamp }

// ========================================= // 3. MODULE: PRICING &
SUBSCRIPTION (Gói cước & Quota) //
========================================= Table VehicleTypes { id int
\[pk, increment\] type_code varchar(20) \[unique, note: 'CAR, BIKE'\]
type_name nvarchar(50) }

Table Packages { id int \[pk, increment\] package_code varchar(50)
\[unique\] package_name nvarchar(100) description nvarchar(255) }

Table Package_VehicleType { id int \[pk, increment\] profile_id int
\[note: 'Profile nào được mua. VD: Cư dân mua gói tháng Ô tô thì max =
2'\] package_id int vehicle_type_id int max_quantity int \[note: 'Hạn
mức (Quota) xe tối đa'\] }

Table PackagePrice { id int \[pk, increment\] pkg_veh_type_id int
duration_days int price decimal(15,2) is_active boolean \[default:
true\] created_at timestamp }

Table Tariff_Rules { id int \[pk, increment\] vehicle_type_id int
day_type day_type start_time time end_time time base_block_mins int
base_price decimal(15,2) next_block_mins int next_block_price
decimal(15,2) max_price_per_day decimal(15,2) is_active boolean
\[default: true\] }

// ========================================= // 4. MODULE: CORE
OPERATIONS (Vận hành Hầm đỗ) //
========================================= Table Bookings { id int \[pk,
increment\] group_id int package_id int created_by int \[note: 'ID của
Employee tạo hợp đồng này'\] created_at timestamp }

Table BookingDetails { id int \[pk, increment\] booking_id int
customer_id int package_price_id int vehicle_no varchar(20) start_date
timestamp end_date timestamp status booking_status created_at timestamp
}

Table ParkingSessions { id bigint \[pk, increment\] booking_detail_id
int \[note: 'Null nếu là xe vãng lai'\] vehicle_no varchar(20)
vehicle_type_id int zone_in_id int \[note: 'Gate/Zone đầu vào'\]
zone_out_id int \[note: 'Gate/Zone lúc xuất bến (Null nếu chưa ra)'\]
entry_time timestamp exit_time timestamp image_in_url varchar(255)
image_out_url varchar(255) grace_period_end timestamp \[note: 'Lưu giờ
ra cổng sau khi thanh toán Kiosk'\] amount_due decimal(15,2) \[default:
0\] is_paid boolean \[default: false\] flag_manual boolean \[default:
false, note: 'Đánh cờ nếu có can thiệp mở cổng bằng tay'\] }

// ========================================= // 5. MODULE: TOPOLOGY &
INFRASTRUCTURE (Không gian & IoT) //
========================================= Table Zones { id int \[pk,
increment\] parent_zone_id int \[note: 'Đệ quy: Trỏ về chính Zones.id
(Null nếu là Root/Building)'\] zone_name nvarchar(100) zone_type
zone_type \[note: 'BUILDING, FLOOR, AREA, GATE'\] capacity int \[note:
'Sức chứa tối đa (Thường áp dụng cho AREA hoặc FLOOR)'\]
current_occupancy int \[default: 0, note: 'Số chỗ đang chiếm (Tính toán
Roll-up)'\] }

Table IoT_Devices { id int \[pk, increment\] zone_id int \[note: 'Gắn
trực tiếp vào Zone (Thường là GATE hoặc AREA)'\] device_code
varchar(50) \[unique\] device_name nvarchar(100) ip_address varchar(20)
device_type device_type direction direction status device_status
last_ping timestamp }

// ========================================= // 6. MODULE: BILLING
(Thanh toán chuyên sâu) // =========================================
Table Payments { id bigint \[pk, increment\] pay_code varchar(50)
\[unique, note: 'Mã giao dịch nội bộ sinh tự động'\] amount
decimal(15,2) \[note: 'Tổng tiền của toàn bộ giao dịch'\] method
payment_method status payment_status gateway_response json \[note: 'Lưu
Payload Webhook hoặc URL File để đối soát'\] created_by_emp_id int
\[note: 'ID Bảo vệ nếu thu tiền mặt. Null nếu thanh toán Online'\]
created_at timestamp updated_at timestamp }

Table Payment_Details { id bigint [pk, increment] payment_id bigint
[note: 'Trỏ về hóa đơn tổng'] booking_detail_id int [note: 'Trả
tiền gia hạn/đăng ký cho chiếc xe nào (có thể null)'] parking_session_id bigint [note: 'Trả tiền cho lượt đỗ xe vãng lai (có thể null)'] item_amount decimal(15,2)
[note: 'Số tiền thu thực tế của riêng xe này (đã tính pro-rate)']
  applied_start_date timestamp [note: 'Bắt đầu chu kỳ của hóa đơn này']
  applied_end_date timestamp [note: 'Kết thúc chu kỳ của hóa đơn này (Dùng làm Sổ cái lịch sử)']
}

// =========================================
// 7. MODULE: CUSTOMER SUPPORT (Hỗ trợ khách hàng)
// =========================================
Table Complaints {
  id int [pk, increment]
  customer_id int [note: 'Người gửi khiếu nại']
  title nvarchar(255) [note: 'Tiêu đề']
  description text [note: 'Nội dung chi tiết']
  image_url varchar(255) [note: 'Link ảnh/video đính kèm']
  status varchar(20) [default: 'PENDING', note: 'PENDING, PROCESSING, RESOLVED, REJECTED']
  resolved_by int [note: 'ID của Guard/Admin xử lý']
  resolution_note text [note: 'Ghi chú khi xử lý xong']
  created_at timestamp [default: `now()`]
  updated_at timestamp [default: `now()`]
} 

// ========================================= // RELATIONSHIPS (KHÓA NGOẠI)
// =========================================

// RBAC & Auth Ref: Accounts.role_id \> Roles.id Ref:
Role_Function_Action.role_id \> Roles.id Ref:
Role_Function_Action.func_id \> Functions.id Ref:
Role_Function_Action.action_id \> Actions.id

// HR & Identity Ref: Employees.account_id - Accounts.id Ref:
Employees.created_by \> Employees.id // Đệ quy nhân viên tạo nhân viên

Ref: Customers.account_id - Accounts.id Ref: GroupsCustomers.profile_id
\> GroupsProfiles.id Ref: GroupsCustomers.master_account_id -
Accounts.id // 1-1 với Account đại diện Ref: GroupsCustomers.created_by
\> Employees.id Ref: Customers.group_id \> GroupsCustomers.id

// Pricing Ref: Package_VehicleType.profile_id \> GroupsProfiles.id //
Đính Quota theo Profile Ref: Package_VehicleType.package_id \>
Packages.id Ref: Package_VehicleType.vehicle_type_id \> VehicleTypes.id
Ref: PackagePrice.pkg_veh_type_id \> Package_VehicleType.id

Ref: Tariff_Rules.vehicle_type_id \> VehicleTypes.id

// Operations Ref: Bookings.group_id \> GroupsCustomers.id Ref:
Bookings.created_by \> Employees.id Ref: Bookings.package_id -
Packages.id

Ref: BookingDetails.booking_id \> Bookings.id Ref:
BookingDetails.customer_id \> Customers.id Ref:
BookingDetails.package_price_id \> PackagePrice.id

Ref: ParkingSessions.booking_detail_id \> BookingDetails.id Ref:
ParkingSessions.vehicle_type_id \> VehicleTypes.id Ref:
ParkingSessions.zone_in_id \> Zones.id // Cổng/Khu vực IN Ref:
ParkingSessions.zone_out_id \> Zones.id // Cổng/Khu vực OUT

// Topology & Infra Ref: Zones.parent_zone_id \> Zones.id // Đệ quy
Không gian (Tree Structure) Ref: IoT_Devices.zone_id \> Zones.id

// Billing

// Khóa ngoại của bảng con Payment_Details Ref:
Payment_Details.payment_id \> Payments.id Ref:
Payment_Details.booking_detail_id - BookingDetails.id

// Customer Support
Ref: Complaints.customer_id > Customers.id
Ref: Complaints.resolved_by > Employees.id
