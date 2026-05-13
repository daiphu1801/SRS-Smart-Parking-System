# Smart Parking System — API Specification

> Tài liệu này được trích xuất từ SRS, Workflow V2, Physical ERD và cấu trúc Spring Boot hiện tại.
> Base URL: `/api/v1`

---

## Mục lục Nhóm Chức năng

| # | Nhóm | Mô tả | Số API |
|---|------|-------|--------|
| 1 | [Authentication & Authorization](#1-authentication--authorization) | Đăng nhập, OTP, JWT, phân quyền | 8 |
| 2 | [Customer Group Management](#2-customer-group-management) | Quản lý Căn hộ / Doanh nghiệp (Admin) | 7 |
| 3 | [Employee Management](#3-employee-management) | Quản lý tài khoản nhân viên nội bộ | 5 |
| 4 | [Vehicle Management](#4-vehicle-management) | Đăng ký, xóa, tra cứu phương tiện | 5 |
| 5 | [Pricing & Packages](#5-pricing--packages) | Gói cước vé tháng + Biểu giá vãng lai | 8 |
| 6 | [Booking & Subscription](#6-booking--subscription) | Đăng ký gói, gia hạn, pro-rate | 5 |
| 7 | [Parking Operations](#7-parking-operations) | Check-in/out, Session, Kiosk | 8 |
| 8 | [Payment & Billing](#8-payment--billing) | Thanh toán QR, tiền mặt, Webhook | 6 |
| 9 | [Guard Operations](#9-guard-operations) | Nhận ca, chốt ca, mở cổng thủ công | 7 |
| 10 | [Topology & IoT Devices](#10-topology--iot-devices) | Zone, Floor, Camera, Barie, LED | 8 |
| 11 | [Monitoring & Navigation](#11-monitoring--navigation) | Chỗ trống realtime, LED rollup | 4 |
| 12 | [Alerts & Violations](#12-alerts--violations) | Cảnh báo đỗ sai, broadcast bảo vệ | 5 |
| 13 | [Complaints](#13-complaints) | Khiếu nại từ khách hàng | 5 |
| 14 | [Reports & Audit](#14-reports--audit) | Doanh thu, log hậu kiểm, xuất file | 5 |
| 15 | [Notifications](#15-notifications) | Thông báo in-app, nhắc nợ | 4 |
| 16 | [System Settings](#16-system-settings) | Tham số động (Grace Period, Buffer) | 3 |
| 17 | [AI Calibration](#17-ai-calibration) | ROI, Confidence, đồng bộ Edge | 3 |

**Tổng cộng: ~96 endpoints**

---

## Mục lục Chi tiết

### 1. Authentication & Authorization

> **UC liên quan:** UC2.1, UC3.1, UC4.2 | **WF:** WF2.1, WF3.1, Guard_Onboarding, Guard_Daily_Login, SubUser_FirstLogin

| # | Method | Endpoint | Mô tả | Actor | UC |
|---|--------|----------|-------|-------|----|
| 1.1 | `POST` | `/auth/otp/send` | Gửi OTP qua SMS/Zalo theo SĐT | Customer, Guard | UC2.1 |
| 1.2 | `POST` | `/auth/otp/verify` | Xác thực mã OTP, trả JWT nếu tài khoản đã kích hoạt | Customer, Guard | UC2.1 |
| 1.3 | `POST` | `/auth/register/password` | Thiết lập mật khẩu lần đầu (Master Account / Guard) | Customer, Guard | WF2.1 |
| 1.4 | `POST` | `/auth/login` | Đăng nhập bằng username + password (Admin/Employee) | Admin, Employee | UC4.2 |
| 1.5 | `POST` | `/auth/guard/pin-login` | Bảo vệ nhập PIN tại Kiosk Desktop → nhận ca | Guard | UC3.1 |
| 1.6 | `POST` | `/auth/refresh` | Làm mới Access Token bằng Refresh Token | All | - |
| 1.7 | `POST` | `/auth/logout` | Đăng xuất, thu hồi token | All | - |
| 1.8 | `GET` | `/auth/me` | Lấy thông tin user hiện tại (profile + role + group) | All | - |

**Request/Response mẫu — 1.1 Gửi OTP:**
```json
// POST /api/v1/auth/otp/send
{ "phone": "0901234567" }

// Response 200
{ "message": "OTP sent", "expires_in": 60 }
```

**Request/Response mẫu — 1.5 Guard PIN Login:**
```json
// POST /api/v1/auth/guard/pin-login
{ "employee_id": 5, "pin": "1234" }

// Response 200
{
  "access_token": "eyJ...",
  "guard_name": "Nguyễn Văn A",
  "shift_started_at": "2026-04-29T07:00:00Z"
}
```

---

### 2. Customer Group Management

> **UC liên quan:** UC4.1 | **WF:** WF2.1, Resident_Onboarding

| # | Method | Endpoint | Mô tả | Actor |
|---|--------|----------|-------|-------|
| 2.1 | `GET` | `/admin/groups` | Danh sách tất cả Group (phân trang, tìm kiếm) | Admin |
| 2.2 | `GET` | `/admin/groups/{id}` | Chi tiết Group (kèm members + vehicles) | Admin |
| 2.3 | `POST` | `/admin/groups` | Tạo Group mới (Căn hộ/DN) + chỉ định Owner SĐT | Admin |
| 2.4 | `PUT` | `/admin/groups/{id}` | Cập nhật thông tin Group | Admin |
| 2.5 | `DELETE` | `/admin/groups/{id}` | Xóa Group | Admin |
| 2.6 | `POST` | `/admin/groups/{id}/members` | Thêm Sub-User (thành viên phụ) vào Group | Admin, Owner |
| 2.7 | `DELETE` | `/admin/groups/{id}/members/{customerId}` | Xóa thành viên khỏi Group | Admin, Owner |

---

### 3. Employee Management

> **UC liên quan:** UC4.2 | **WF:** Guard_Account_Creation

| # | Method | Endpoint | Mô tả | Actor |
|---|--------|----------|-------|-------|
| 3.1 | `GET` | `/admin/employees` | Danh sách nhân viên (Guard, Kế toán) | Admin |
| 3.2 | `GET` | `/admin/employees/{id}` | Chi tiết nhân viên | Admin |
| 3.3 | `POST` | `/admin/employees` | Tạo tài khoản nhân viên (SĐT + Role) | Admin |
| 3.4 | `PUT` | `/admin/employees/{id}` | Cập nhật thông tin nhân viên | Admin |
| 3.5 | `DELETE` | `/admin/employees/{id}` | Vô hiệu hóa tài khoản | Admin |

---

### 4. Vehicle Management

> **UC liên quan:** UC2.2 | **WF:** Vehicle_Registration

| # | Method | Endpoint | Mô tả | Actor |
|---|--------|----------|-------|-------|
| 4.1 | `GET` | `/vehicles` | Danh sách xe của Group hiện tại | Customer |
| 4.2 | `GET` | `/vehicles/{plate}` | Tra cứu xe theo biển số | Customer, Guard |
| 4.3 | `POST` | `/vehicles` | Đăng ký xe mới (kiểm tra quota + trùng biển) | Customer |
| 4.4 | `PUT` | `/vehicles/{id}` | Cập nhật thông tin xe | Customer |
| 4.5 | `DELETE` | `/vehicles/{id}` | Xóa xe khỏi Group | Customer |

---

### 5. Pricing & Packages

> **UC liên quan:** UC4.4 | **WF:** WF4.2

| # | Method | Endpoint | Mô tả | Actor |
|---|--------|----------|-------|-------|
| 5.1 | `GET` | `/admin/packages` | Danh sách Gói cước vé tháng | Admin |
| 5.2 | `POST` | `/admin/packages` | Tạo Gói cước mới | Admin |
| 5.3 | `PUT` | `/admin/packages/{id}` | Sửa Gói cước | Admin |
| 5.4 | `DELETE` | `/admin/packages/{id}` | Xóa / Deactive Gói cước | Admin |
| 5.5 | `GET` | `/admin/tariffs` | Danh sách Biểu giá vãng lai | Admin |
| 5.6 | `POST` | `/admin/tariffs` | Tạo rule giá vãng lai (block giờ) | Admin |
| 5.7 | `PUT` | `/admin/tariffs/{id}` | Sửa rule giá | Admin |
| 5.8 | `DELETE` | `/admin/tariffs/{id}` | Xóa rule giá | Admin |

---

### 6. Booking & Subscription

> **UC liên quan:** UC1.5, UC1.6, UC2.3 | **WF:** Package_Registering, Package_Synchronizing_And_Renew

| # | Method | Endpoint | Mô tả | Actor |
|---|--------|----------|-------|-------|
| 6.1 | `GET` | `/subscriptions` | Danh sách xe + trạng thái gói cước của user | Customer |
| 6.2 | `GET` | `/subscriptions/available-packages` | Lấy gói cước phù hợp (theo quota còn lại) | Customer |
| 6.3 | `POST` | `/subscriptions/register` | Đăng ký gói mới (tính pro-rate + tạo BookingDetail) | Customer |
| 6.4 | `POST` | `/subscriptions/renew` | Gia hạn nhiều xe (batch) → 1 mã QR duy nhất | Customer |
| 6.5 | `GET` | `/subscriptions/renewal-cart` | Danh sách xe sắp hết hạn cần gia hạn | Customer |

---

### 7. Parking Operations

> **UC liên quan:** UC1.1, UC1.3, UC1.4 | **WF:** Auto_CheckIn, Auto_CheckOut, Kiosk_Payment

| # | Method | Endpoint | Mô tả | Actor |
|---|--------|----------|-------|-------|
| 7.1 | `POST` | `/parking/check-in` | Edge gửi event check-in (plate, vehicle_type, zone_id) | Edge/Kafka |
| 7.2 | `POST` | `/parking/check-out` | Edge gửi event check-out (plate, zone_id) | Edge/Kafka |
| 7.3 | `GET` | `/parking/sessions/active` | Danh sách phiên đỗ đang hoạt động | Admin, Guard |
| 7.4 | `GET` | `/parking/sessions/{id}` | Chi tiết phiên đỗ (ảnh check-in/out, phí) | Admin, Guard |
| 7.5 | `GET` | `/parking/lookup/{plate}` | Tra cứu phiên đỗ theo biển số | Guard, Kiosk |
| 7.6 | `POST` | `/parking/kiosk/calculate` | Kiosk nhập biển → tính phí → trả hóa đơn | Kiosk |
| 7.7 | `GET` | `/parking/history` | Lịch sử ra/vào của user (kèm ảnh LPR) | Customer |
| 7.8 | `POST` | `/parking/gate/open` | Lệnh mở barie (backend → Edge qua WebSocket) | System |

---

### 8. Payment & Billing

> **UC liên quan:** UC1.3, UC3.2 | **WF:** Kiosk_Payment, Package_Registering

| # | Method | Endpoint | Mô tả | Actor |
|---|--------|----------|-------|-------|
| 8.1 | `POST` | `/payments/create-qr` | Tạo mã QR thanh toán (gọi PayOS API) | Customer, Kiosk |
| 8.2 | `POST` | `/payments/webhook` | PayOS gọi webhook xác nhận thanh toán | PayOS Gateway |
| 8.3 | `POST` | `/payments/cash-confirm` | Bảo vệ xác nhận thu tiền mặt | Guard |
| 8.4 | `GET` | `/payments/history` | Lịch sử thanh toán của user | Customer |
| 8.5 | `GET` | `/payments/{id}` | Chi tiết giao dịch (kèm payment_items) | Customer, Admin |
| 8.6 | `GET` | `/admin/payments` | Tất cả giao dịch (filter theo ngày, loại, guard) | Admin |

---

### 9. Guard Operations

> **UC liên quan:** UC3.1–UC3.5 | **WF:** Guard_Daily_Login, Gate_Fallback, Shift_Reconciliation

| # | Method | Endpoint | Mô tả | Actor |
|---|--------|----------|-------|-------|
| 9.1 | `POST` | `/guard/start-shift` | Nhận ca trực (PIN xác thực tại Kiosk) | Guard |
| 9.2 | `POST` | `/guard/end-shift` | Kết thúc ca + chốt báo cáo doanh thu | Guard |
| 9.3 | `POST` | `/guard/manual-entry` | Nhập tay biển số + mở cổng vào (Manual Override) | Guard |
| 9.4 | `POST` | `/guard/manual-exit` | Nhập tay biển số + mở cổng ra | Guard |
| 9.5 | `GET` | `/guard/shift-log` | Log thao tác ca trực hiện tại | Guard |
| 9.6 | `GET` | `/guard/shift-summary` | Tóm tắt ca (tổng mở cổng, tổng tiền mặt) | Guard |
| 9.7 | `POST` | `/guard/shift-report` | Nộp báo cáo tiền mặt thực tế cuối ca | Guard |

---

### 10. Topology & IoT Devices

> **UC liên quan:** UC4.3 | **WF:** WF4.1, AI_IoT_Calibration

| # | Method | Endpoint | Mô tả | Actor |
|---|--------|----------|-------|-------|
| 10.1 | `GET` | `/admin/zones` | Cây Zone (tree hierarchy) | Admin |
| 10.2 | `GET` | `/admin/zones/{id}` | Chi tiết Zone (capacity, occupancy, devices) | Admin |
| 10.3 | `POST` | `/admin/zones` | Tạo Zone mới (tên, tầng, parent, capacity) | Admin |
| 10.4 | `PUT` | `/admin/zones/{id}` | Cập nhật Zone | Admin |
| 10.5 | `DELETE` | `/admin/zones/{id}` | Xóa Zone | Admin |
| 10.6 | `GET` | `/admin/devices` | Danh sách thiết bị IoT (filter theo zone) | Admin |
| 10.7 | `POST` | `/admin/devices` | Khai báo thiết bị (IP, type, direction, zone) | Admin |
| 10.8 | `POST` | `/admin/devices/ping` | Ping kiểm tra kết nối tất cả thiết bị | Admin |

---

### 11. Monitoring & Navigation

> **UC liên quan:** UC1.2 | **WF:** LED_Navigation_Rollup

| # | Method | Endpoint | Mô tả | Actor |
|---|--------|----------|-------|-------|
| 11.1 | `GET` | `/monitoring/occupancy` | Số chỗ trống realtime toàn bãi (roll-up) | All |
| 11.2 | `GET` | `/monitoring/zones/{id}/status` | Trạng thái chi tiết 1 zone | Admin, Guard |
| 11.3 | `POST` | `/monitoring/zone-event` | Edge báo xe qua zone (trigger roll-up + LED) | Edge/Kafka |
| 11.4 | `WebSocket` | `/ws/led-updates` | Push cập nhật chỗ trống xuống LED/Dashboard | LED, Admin |

---

### 12. Alerts & Violations

> **UC liên quan:** UC3.5 | **WF:** Violation_Alert

| # | Method | Endpoint | Mô tả | Actor |
|---|--------|----------|-------|-------|
| 12.1 | `POST` | `/alerts` | AI tạo cảnh báo mới (đỗ sai, chắn lối) | Edge/AI |
| 12.2 | `GET` | `/alerts` | Danh sách cảnh báo (filter: active/resolved) | Guard, Admin |
| 12.3 | `GET` | `/alerts/{id}` | Chi tiết cảnh báo (ảnh, vị trí, thời gian) | Guard, Admin |
| 12.4 | `PUT` | `/alerts/{id}/resolve` | Bảo vệ đánh dấu "Đã xử lý" | Guard |
| 12.5 | `WebSocket` | `/ws/guard-alerts` | Broadcast cảnh báo realtime cho Guard Online | Guard |

---

### 13. Complaints

> **UC liên quan:** UC2.4, UC4.9

| # | Method | Endpoint | Mô tả | Actor |
|---|--------|----------|-------|-------|
| 13.1 | `GET` | `/complaints` | Danh sách khiếu nại (phân trang) | Admin |
| 13.2 | `GET` | `/complaints/{id}` | Chi tiết khiếu nại (ảnh đính kèm) | Admin |
| 13.3 | `POST` | `/complaints` | Khách gửi khiếu nại mới | Customer |
| 13.4 | `PUT` | `/complaints/{id}/status` | Admin cập nhật trạng thái (Open→In Progress→Resolved) | Admin |
| 13.5 | `POST` | `/complaints/{id}/reply` | Admin phản hồi khiếu nại | Admin |

---

### 14. Reports & Audit

> **UC liên quan:** UC4.7, UC4.8 | **WF:** Audit_Reconciliation

| # | Method | Endpoint | Mô tả | Actor |
|---|--------|----------|-------|-------|
| 14.1 | `GET` | `/reports/revenue` | Tổng hợp doanh thu (filter ngày, kênh thu) | Admin |
| 14.2 | `GET` | `/reports/revenue/breakdown` | Phân tách: Vé tháng / Vãng lai QR / Tiền mặt | Admin |
| 14.3 | `GET` | `/reports/audit-log` | Log hậu kiểm (Manual Override, Cash, ảnh LPR) | Admin |
| 14.4 | `GET` | `/reports/guard-shifts` | Báo cáo ca trực theo Guard ID | Admin |
| 14.5 | `GET` | `/reports/export` | Xuất báo cáo CSV/Excel | Admin |

---

### 15. Notifications

> **UC liên quan:** UC2.1 (nhắc nợ), Kiến trúc Multi-tier

| # | Method | Endpoint | Mô tả | Actor |
|---|--------|----------|-------|-------|
| 15.1 | `GET` | `/notifications` | Danh sách thông báo của user (phân trang) | Customer |
| 15.2 | `PUT` | `/notifications/{id}/read` | Đánh dấu đã đọc | Customer |
| 15.3 | `PUT` | `/notifications/read-all` | Đánh dấu tất cả đã đọc | Customer |
| 15.4 | `GET` | `/notifications/unread-count` | Số thông báo chưa đọc (badge count) | Customer |

---

### 16. System Settings

> **UC liên quan:** UC4.5, UC4.6 | **WF:** WF4.2

| # | Method | Endpoint | Mô tả | Actor |
|---|--------|----------|-------|-------|
| 16.1 | `GET` | `/admin/settings` | Lấy tham số hiện tại (Grace Period, Buffer %) | Admin |
| 16.2 | `PUT` | `/admin/settings` | Cập nhật tham số → ghi đè settings.json | Admin |
| 16.3 | `PUT` | `/admin/settings/payment-gateway` | Cấu hình API Key PayOS/VNPay | Admin |

---

### 17. AI Calibration

> **UC liên quan:** UC4.10 | **WF:** AI_IoT_Calibration

| # | Method | Endpoint | Mô tả | Actor |
|---|--------|----------|-------|-------|
| 17.1 | `GET` | `/admin/ai/cameras/{deviceId}/stream` | Lấy URL stream RTSP của camera | Admin |
| 17.2 | `POST` | `/admin/ai/cameras/{deviceId}/roi` | Lưu vùng ROI + Confidence Score | Admin |
| 17.3 | `POST` | `/admin/ai/sync-config` | Đồng bộ cấu hình xuống Edge Server | Admin |

---

## Ma trận Phân quyền (RBAC)

| API Group | Admin | Manager | Guard | Customer | Edge/System |
|-----------|:-----:|:-------:|:-----:|:--------:|:-----------:|
| 1. Auth | ✅ | ✅ | ✅ | ✅ | — |
| 2. Group Mgmt | ✅ | ❌ | ❌ | ❌ | — |
| 3. Employee Mgmt | ✅ | ❌ | ❌ | ❌ | — |
| 4. Vehicle | ✅ | ❌ | 🔍 | ✅ | — |
| 5. Pricing | ✅ | ❌ | ❌ | ❌ | — |
| 6. Subscription | ❌ | ❌ | ❌ | ✅ | — |
| 7. Parking Ops | ✅ | 🔍 | ✅ | 🔍 | ✅ |
| 8. Payment | ✅ | 🔍 | ✅ | ✅ | ✅ |
| 9. Guard Ops | ❌ | ❌ | ✅ | ❌ | — |
| 10. Topology | ✅ | ❌ | ❌ | ❌ | — |
| 11. Monitoring | ✅ | ✅ | ✅ | ❌ | ✅ |
| 12. Alerts | ✅ | ✅ | ✅ | ❌ | ✅ |
| 13. Complaints | ✅ | ✅ | ❌ | ✅ | — |
| 14. Reports | ✅ | ✅ | ❌ | ❌ | — |
| 15. Notifications | ❌ | ❌ | ❌ | ✅ | — |
| 16. Settings | ✅ | ❌ | ❌ | ❌ | — |
| 17. AI Calibration | ✅ | ❌ | ❌ | ❌ | — |

> ✅ = Full access | 🔍 = Read-only | ❌ = No access

---

## Quy ước chung

### Response Format
```json
{
  "status": 200,
  "message": "Success",
  "data": { },
  "timestamp": "2026-04-29T14:00:00Z"
}
```

### Error Format
```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Biển số xe đã tồn tại trong hệ thống",
  "timestamp": "2026-04-29T14:00:00Z"
}
```

### Phân trang
```
GET /api/v1/resource?page=0&size=20&sort=createdAt,desc
```

### Authentication Header
```
Authorization: Bearer <access_token>
```

---

## Mapping: Workflow → API

| Workflow | API Endpoints sử dụng |
|----------|----------------------|
| Auto_CheckIn | 7.1 → 11.3 → 7.8 |
| Auto_CheckOut | 7.2 → 8.1 hoặc 8.3 → 7.8 → 11.3 |
| Kiosk_Payment | 7.6 → 8.1 → 8.2 |
| Package_Registering | 6.2 → 6.3 → 8.1 → 8.2 |
| Package_Synchronizing | 6.5 → 6.4 → 8.1 → 8.2 |
| Resident_Onboarding | 2.3 → 1.1 → 1.2 → 1.3 → 2.6 |
| SubUser_FirstLogin | 1.1 → 1.2 |
| Vehicle_Registration | 4.3 |
| Guard_Account_Creation | 3.3 → 1.1 → 1.2 → 1.3 |
| Guard_Daily_Login | 1.5 (Kiosk) → 1.4 (Mobile) |
| Guard_Onboarding | 3.3 → 1.1 → 1.2 → 9.1 |
| Gate_Fallback | 9.3/9.4 hoặc 8.3 → 7.8 |
| Violation_Alert | 12.1 → 12.5 → 12.4 |
| Shift_Reconciliation | 9.6 → 9.7 → 9.2 |
| Audit_Reconciliation | 14.1 → 14.3 → 14.5 |
| AI_IoT_Calibration | 17.1 → 17.2 → 17.3 |
| LED_Navigation | 11.3 → 11.1 → 11.4 |
