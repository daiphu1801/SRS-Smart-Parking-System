\@startuml skinparam linetype ortho skinparam packageStyle rectangle
skinparam nodesep 50 skinparam ranksep 60

\' \-\-- 1. PHÂN QUYỀN & NHÂN SỰ \-\-- package \"Phân quyền & Nhân sự
(Auth & HR)\" { entity \"Roles\" as Role { \* ID \[PK\] \-- RoleName
\[nvarchar\] Description \[text\] }

entity \"Functions\" as Func { \* ID \[PK\] \-- FunctionName
\[nvarchar\] Description \[text\] }

entity \"Actions\" as Act { \* ID \[PK\] \-- ActionName \[nvarchar\]
Description \[text\] }

entity \"Roles_Functions_Actions\" as RFA { \* ID \[PK\] \-- RoleID
\[FK\] FunctionID \[FK\] ActionID \[FK\] }

entity \"Employees\" as Emp { \* ID \[PK\] \-- RoleID \[FK\] UserName
\[nvarchar\] Password \[nvarchar\] Fullname \[nvarchar\] Phone
\[nvarchar\] Email \[nvarchar\] Status \[Enum: Online/Offline\]
account_id \[PK\] } }

\' \-\-- 2. ĐỊNH DANH & KHÁCH HÀNG \-\-- package \"Định danh & Khách
hàng (Identity & Customers)\" { entity \"GroupsProfiles\" as GrpProfile
{ \* ID \[PK\] \-- GroupName \[nvarchar\] }

entity \"GroupsCustomers\" as GrpCust { \* ID \[PK\] \-- GroupProfileID
\[FK\] OwnerGroupID \[FK\] \<\<Owner\>\> GroupName \[nvarchar\] }

entity \"Customers\" as Cust { \* ID \[PK\] \-- GroupID \[FK\] RoleID
\[FK\] UserName \[nvarchar\] Password \[nvarchar\] Fullname \[nvarchar\]
Phone \[nvarchar\] Address \[nvarchar\] Email \[nvarchar\] account_id
\[PK\] } }

\' \-\-- 3. CẤU HÌNH GÓI CƯỚC \-\-- package \"Cấu hình Gói cước (Package
Config)\" { entity \"VehicleTypes\" as VehType { \* ID \[PK\] \-- Name
\[nvarchar\] }

entity \"Packages\" as Pkg { \* ID \[PK\] \-- Name \[nvarchar\] }

entity \"Package_VehicleType\" as PkgVehType { \* ID \[PK\] \--
PackageID \[FK\] VehicleTypeID \[FK\] MaxQuantity \[int\] }

entity \"PackagePrice\" as PkgPrice { \* ID \[PK\] \--
Package_VehicleTypeID \[FK\] Duration \[int\] Price \[double\] Status
\[Enum: Active/Deactive\] }

entity \"TemporaryPrice\" as TempPrice { \* ID \[PK\] \-- VehicleTypeID
\[FK\] StartTime \[time\] EndTime \[time\] Price \[double\] } }

\' \-\-- 4. QUẢN LÝ ĐẶT CHỖ/THUÊ BAO \-\-- package \"Quản lý Hợp đồng
(Bookings)\" { entity \"Bookings\" as Booking { \* ID \[PK\] \--
GroupCustomerID \[FK\] PackageID \[FK\] Status \[Enum: Online/Offline\]
}

entity \"BookingDetails\" as BookingDetail { \* ID \[PK\] \-- BookingID
\[FK\] CustomerID \[FK\] VehicleTypeID \[FK\] PackagePriceID \[FK\]
PayID \[FK\] VehicleNo \[varchar\] StartTime \[time\] EndTime \[time\]
TotalAmount \[double\] Status \[Enum: Pending/Active/Expired\] } }

\' \-\-- 5. VẬN HÀNH, THANH TOÁN & HỖ TRỢ \-\-- package \"Vận hành &
Thanh toán (Ops & Payments)\" { entity \"ParkingSessions\" as Sess { \*
ID \[PK\] \-- BookingDetailID \[FK\] VehicleTypeID \[FK\] PayID \[FK\]
VehicleNo \[varchar\] StartTime \[time\] EndTime \[time\] ImageCheckIn
\[blob\] ImageCheckOut \[blob\] TicketAmount \[double\] Paid \[double\]
RemainingAmount \[double\] }

entity \"Payments\" as Pay { \* ID \[PK\] \-- BookingDetailID \[FK\]
ParkingSessionID \[FK\] PayCode \[varchar\] Type \[Enum: QR/Cash\]
Amount \[double\] Time \[timestamp\] Status \[Enum: Pending/Success\] }

entity \"Claims\" as Claim { \* ID \[PK\] \-- UserID \[FK\] Content
\[text\] Status \[Enum: Completed/Uncompleted\] }

entity \"Notifications\" as Noti { \* ID \[PK\] \-- account_id \[FK\]
Content \[text\] IsRead \[boolean\] CreatedAt \[timestamp\] } }

\' \-\-- Tầng Định danh (Mở rộng) \-\-- entity \"Accounts\" as Acc { \*
account_id \[PK\] }

\' Bảng lưu trữ mã xác thực (Dùng cho đăng ký, quên mật khẩu) entity
\"OTP_Logs\" as OTP { \* otp_id \[PK\] \-- account_id \[FK\]

} entity \"Login_History\" as LogHist { \* history_id \[PK\] \--
account_id \[FK\] }

\' \-\-- 5. HẠ TẦNG IOT TỐI GIẢN (IoT) \-\-- package \"Hạ tầng
(Infrastructure)\" { entity \"Zones\" as Zone { \* ID \[PK\] \--
ZoneName \[nvarchar\] Floor \[int\] }

entity \"IoT_Devices\" as Device { \* ID \[PK\] \-- ZoneID \[FK\]
DeviceName \[nvarchar\] DeviceType \[Enum: Camera_In, Camera_Out,
Barrier, LED\] Direction \[Enum: IN, OUT\] \<\<Biết xe ra hay vào\>\> }
}

\' \-\-- MỐI QUAN HỆ (RELATIONSHIPS) \-\--

\' 1. Auth & HR Acc \|\|\--o{ Sess Acc \|\|\--o{ OTP Acc \|\|\--o{
LogHist Acc \|\|\--o\| Cust Acc \|\|\--o\| Emp

Role \|\|\--o{ RFA Func \|\|\--o{ RFA Act \|\|\--o{ RFA Role \|\|\--o{
Cust Role \|\|\--o{ Emp

\' 2. Identity GrpProfile \|\|\--o{ GrpCust GrpCust \|\|\--o{ Cust Cust
\|o\--o\| GrpCust : \"OwnerGroupID\"

\' 3. Package Config Pkg \|\|\--o{ PkgVehType VehType \|\|\--o{
PkgVehType PkgVehType \|\|\--o{ PkgPrice VehType \|\|\--o{ TempPrice \'
4. Bookings GrpCust \|\|\--o{ Booking Pkg \|\|\--o{ Booking Booking
\|\|\--o{ BookingDetail Cust \|\|\--o{ BookingDetail VehType \|\|\--o{
BookingDetail PkgPrice \|\|\--o{ BookingDetail

\' 5. Ops & Claims BookingDetail \|o\--o{ Sess VehType \|\|\--o{ Sess
Cust \|\|\--o{ Claim Emp \|\|\--o{ Claim Noti }o\--o\| Acc

\' 6. Payments (The Hub) BookingDetail \|o\--o\| Pay : \"Thanh toán gói
tháng\" Sess \|o\--o\| Pay : \"Thanh toán lượt vãng lai\"

Zone \|\|\--o{ Device Zone \|\|\--o{ Sess \@enduml
