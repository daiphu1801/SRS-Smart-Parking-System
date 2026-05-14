\@startuml \' \-\-- CẤU HÌNH GIAO DIỆN CHUẨN ERD (KHÔNG LỖI ENGINE)
\-\-- hide circle hide empty members skinparam linetype ortho skinparam
nodesep 50 skinparam ranksep 50 skinparam shadowing false

\' 1. Style cho các BẢNG DỮ LIỆU (Hình chữ nhật xám) skinparam entity {
BackgroundColor #F8F9FA BorderColor #343A40 FontName Arial FontSize 14
FontStyle bold }

\' 2. Style cho các KHỐI QUAN HỆ (Hình Bo tròn - Viên thuốc màu xanh)
skinparam entity\<\<Rel\>\> { BackgroundColor #E3F2FD BorderColor
#1565C0 FontColor #1565C0 FontStyle italic RoundCorner 40 }

\' \-\-- KHAI BÁO CÁC BẢNG DỮ LIỆU (TABLES) \-\-- entity \"Roles\" as
Role entity \"Functions\" as Func entity \"Actions\" as Act entity
\"Roles_Functions_Actions\" as RFA entity \"Employees\" as Emp

entity \"Accounts\" as Acc entity \"OTP_Logs\" as OTP entity
\"Login_History\" as LogHist

entity \"GroupsProfiles\" as GrpProfile entity \"GroupsCustomers\" as
GrpCust entity \"Customers\" as Cust

entity \"VehicleTypes\" as VehType entity \"Packages\" as Pkg entity
\"Package_VehicleType\" as PkgVehType entity \"PackagePrice\" as
PkgPrice entity \"TemporaryPrice\" as TempPrice

entity \"Bookings\" as Booking entity \"BookingDetails\" as
BookingDetail

entity \"ParkingSessions\" as Sess entity \"Payments\" as Pay entity
\"Claims\" as Claim entity \"Notifications\" as Noti

entity \"Zones\" as Zone entity \"IoT_Devices\" as Device

\' \-\-- KHAI BÁO CÁC KHỐI QUAN HỆ VÀ KẾT NỐI CHÂN CHIM \-\--

\' 1. Auth, HR & Identity entity \"Nhận\" as r_acc_otp \<\<Rel\>\> Acc
\|\|\-- r_acc_otp r_acc_otp \--o{ OTP

entity \"Lưu trữ\" as r_acc_log \<\<Rel\>\> Acc \|\|\-- r_acc_log
r_acc_log \--o{ LogHist

entity \"Định danh\" as r_acc_cust \<\<Rel\>\> Acc \|\|\-- r_acc_cust
r_acc_cust \--o\| Cust

entity \"Định danh \" as r_acc_emp \<\<Rel\>\> Acc \|\|\-- r_acc_emp
r_acc_emp \--o\| Emp

entity \"Bao gồm\" as r_role_rfa \<\<Rel\>\> Role \|\|\-- r_role_rfa
r_role_rfa \--o{ RFA

entity \"Thuộc\" as r_func_rfa \<\<Rel\>\> Func \|\|\-- r_func_rfa
r_func_rfa \--o{ RFA

entity \"Thuộc \" as r_act_rfa \<\<Rel\>\> Act \|\|\-- r_act_rfa
r_act_rfa \--o{ RFA

entity \"Cấp quyền\" as r_role_cust \<\<Rel\>\> Role \|\|\-- r_role_cust
r_role_cust \--o{ Cust

entity \"Cấp quyền \" as r_role_emp \<\<Rel\>\> Role \|\|\-- r_role_emp
r_role_emp \--o{ Emp

\' 2. Group & Customers entity \"Phân loại\" as r_grp_grpcust
\<\<Rel\>\> GrpProfile \|\|\-- r_grp_grpcust r_grp_grpcust \--o{ GrpCust

entity \"Bao gồm \" as r_grpcust_cust \<\<Rel\>\> GrpCust \|\|\--
r_grpcust_cust r_grpcust_cust \--o{ Cust

entity \"Làm chủ\" as r_cust_owner \<\<Rel\>\> Cust \|\|\-- r_cust_owner
r_cust_owner \--o\| GrpCust

\' 3. Package Config entity \"Có các\" as r_pkg_pkgveh \<\<Rel\>\> Pkg
\|\|\-- r_pkg_pkgveh r_pkg_pkgveh \--o{ PkgVehType

entity \"Dành cho\" as r_veh_pkgveh \<\<Rel\>\> VehType \|\|\--
r_veh_pkgveh r_veh_pkgveh \--o{ PkgVehType

entity \"Có bảng giá\" as r_pkgveh_price \<\<Rel\>\> PkgVehType \|\|\--
r_pkgveh_price r_pkgveh_price \--o{ PkgPrice

entity \"Có giá vãng lai\" as r_veh_temp \<\<Rel\>\> VehType \|\|\--
r_veh_temp r_veh_temp \--o{ TempPrice

\' 4. Bookings entity \"Sở hữu\" as r_grp_book \<\<Rel\>\> GrpCust
\|\|\-- r_grp_book r_grp_book \--o{ Booking

entity \"Đăng ký\" as r_pkg_book \<\<Rel\>\> Pkg \|\|\-- r_pkg_book
r_pkg_book \--o{ Booking

entity \"Có chi tiết\" as r_book_detail \<\<Rel\>\> Booking \|\|\--
r_book_detail r_book_detail \--o{ BookingDetail

entity \"Đứng tên\" as r_cust_detail \<\<Rel\>\> Cust \|\|\--
r_cust_detail r_cust_detail \--o{ BookingDetail

entity \"Thuộc loại\" as r_veh_detail \<\<Rel\>\> VehType \|\|\--
r_veh_detail r_veh_detail \--o{ BookingDetail

entity \"Áp dụng giá\" as r_price_detail \<\<Rel\>\> PkgPrice \|\|\--
r_price_detail r_price_detail \--o{ BookingDetail

\' 5. Operations & Payments entity \"Sinh ra lượt\" as r_detail_sess
\<\<Rel\>\> BookingDetail \|\|\-- r_detail_sess r_detail_sess \--o{ Sess

entity \"Là loại xe\" as r_veh_sess \<\<Rel\>\> VehType \|\|\--
r_veh_sess r_veh_sess \--o{ Sess

entity \"Gửi khiếu nại\" as r_cust_claim \<\<Rel\>\> Cust \|\|\--
r_cust_claim r_cust_claim \--o{ Claim

entity \"Xử lý khiếu nại\" as r_emp_claim \<\<Rel\>\> Emp \|\|\--
r_emp_claim r_emp_claim \--o{ Claim

entity \"Nhận thông báo\" as r_acc_noti \<\<Rel\>\> Acc \|\|\--
r_acc_noti r_acc_noti \--o{ Noti

entity \"Thanh toán (Tháng)\" as r_detail_pay \<\<Rel\>\> BookingDetail
\|\|\-- r_detail_pay r_detail_pay \--o\| Pay

entity \"Thanh toán (Lượt)\" as r_sess_pay \<\<Rel\>\> Sess \|\|\--
r_sess_pay r_sess_pay \--o\| Pay

\' 6. Infrastructure entity \"Lắp đặt\" as r_zone_device \<\<Rel\>\>
Zone \|\|\-- r_zone_device r_zone_device \--o{ Device

entity \"Nơi đỗ xe\" as r_zone_sess \<\<Rel\>\> Zone \|\|\-- r_zone_sess
r_zone_sess \--o{ Sess

\@enduml
