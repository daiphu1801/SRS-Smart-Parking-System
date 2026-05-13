# Kế hoạch phát triển Backend (Spring Boot) - Smart Parking System

Kế hoạch này vạch ra lộ trình triển khai chi tiết cho hệ thống Backend Spring Boot dựa trên tài liệu **API Specification**, **SRS**, và **Workflow V2**.

## Phase 1: Chuẩn bị Hạ tầng & Cấu trúc Core (Tuần 1)
**Mục tiêu:** Xây dựng nền tảng vững chắc, kết nối Database, cấu hình Security và cấu trúc các package chuẩn.

1. **Thiết lập Database & Entity (JPA/Hibernate)**
   - Cấu hình kết nối MySQL/PostgreSQL.
   - Refactor và ánh xạ toàn bộ Physical ERD thành các Java Entities trong package `com.smartparking.entity`.
   - Thiết lập các quan hệ (OneToMany, ManyToOne) cho: Account, Role, Group, Customer, BookingDetail, ParkingSession, IoTDevice.
2. **Cấu hình Spring Security & JWT**
   - Viết JWT Filter để xác thực access_token.
   - Định nghĩa ma trận phân quyền (RBAC) bằng `@PreAuthorize` cho các API endpoints (Admin, Guard, Customer).
   - Tích hợp module gửi mã OTP (tạo mock service hoặc kết nối Twilio/Zalo ZNS).
3. **Thiết lập Kafka & WebSocket**
   - Cấu hình Spring Kafka (Producer & Consumer) để lắng nghe sự kiện từ Edge Server (Check-in, Check-out, LPR).
   - Cấu hình Spring WebSocket với STOMP để push thông báo realtime cho Guard App và Bảng LED.

## Phase 2: Core Business - Quản lý Dữ liệu Nội bộ (Tuần 2)
**Mục tiêu:** Xây dựng các API CRUD cơ bản để Admin có thể cấu hình hệ thống trước khi vận hành.

1. **Quản lý Tài khoản & Phân quyền (Auth & HR)**
   - API Login, Register (đã xử lý luồng OTP/PIN).
   - Cấu hình và quản lý nhân viên (Guard, Kế toán).
2. **Quản lý Cư dân & Nhóm (Group Management)**
   - API cho Admin tạo Group (Căn hộ/Doanh nghiệp) và phân bổ Quota.
   - API cho Owner thêm/xóa thành viên (Sub-users).
3. **Quản lý Gói cước & Biểu giá (Pricing)**
   - Xây dựng logic quản lý các Gói thuê bao (Package) theo loại xe.
   - Cấu hình Biểu giá vãng lai (Tariff Rules) tính theo block giờ.
4. **Quản lý Hạ tầng bãi đỗ (Topology & Devices)**
   - Xây dựng cây phân cấp (Tree structure): Floor -> Zone.
   - Đăng ký thiết bị IoT, liên kết Camera/Barrier vào Zone.

## Phase 3: Core Business - Vận hành & Thanh toán (Tuần 3)
**Mục tiêu:** Hiện thực hóa các nghiệp vụ phức tạp nhất của hệ thống (Check-in/out, Đăng ký vé tháng, Thanh toán).

1. **Nghiệp vụ Thuê bao (Subscription & Quota)**
   - Logic đăng ký xe mới (kiểm tra hạn mức quota của Group).
   - Thuật toán tính giá Pro-rate (ngày lẻ) khi mua gói cước lần đầu.
   - Logic gia hạn gói cước đồng loạt (Batch renewal).
2. **Nghiệp vụ Vận hành Cổng (Parking Operations)**
   - Xây dựng Consumer nhận Kafka Event (Plate, DeviceID) từ Edge.
   - Logic Check-in: Phân loại thuê bao/vãng lai, kiểm tra safety buffer, mở cổng.
   - Logic Check-out: Tính phí vãng lai, kiểm tra grace period (thời gian ân hạn).
3. **Tích hợp Thanh toán (Billing & Payment)**
   - Tích hợp cổng PayOS để tạo mã QR động.
   - Viết API Webhook bảo mật để nhận IPN từ PayOS và tự động chuyển trạng thái Parking Session / Booking.
   - API thu tiền mặt cho Bảo vệ (Manual Cash Collection).

## Phase 4: Tính năng Mở rộng & Vận hành (Tuần 4)
**Mục tiêu:** Giám sát thời gian thực, xử lý ngoại lệ và lập báo cáo.

1. **Giám sát thời gian thực (Monitoring & Roll-up)**
   - Thuật toán tính toán Roll-up số lượng chỗ trống.
   - Đẩy dữ liệu chỗ trống xuống bảng LED hiện trường qua WebSocket.
2. **Hỗ trợ Bảo vệ (Guard Operations & Alerts)**
   - Chức năng Manual Override (mở cổng bằng tay) cho Bảo vệ khi AI nhận diện sai.
   - Xử lý các luồng Violation Alerts (cảnh báo AI từ Edge đẩy lên).
   - Báo cáo chốt ca & đối soát tiền mặt.
3. **Hệ thống Khiếu nại & Nhắc nhở (Complaints & Noti)**
   - Module xử lý khiếu nại khách hàng.
   - Cronjobs: Quét vé tháng hết hạn để gửi thông báo nhắc nợ.
4. **Báo cáo & Hậu kiểm (Reports)**
   - API xuất báo cáo doanh thu chi tiết (Vé tháng vs Vãng lai).
   - Log hậu kiểm mọi hành động mở barie bằng tay.

## Giai đoạn Kiểm thử & Bàn giao (Tuần 5)
1. **Kiểm thử Tích hợp (Integration Testing)**
   - Giả lập Edge AI gửi Kafka messages (ảnh LPR, biển số) để test luồng Check-in tự động.
2. **Kiểm thử Tải (Load Testing)**
   - Kiểm tra khả năng chịu tải của thuật toán Roll-up và WebSocket khi hàng loạt xe ra/vào giờ cao điểm.
3. **Triển khai (Deployment)**
   - Đóng gói Docker file.
   - Triển khai Backend lên server staging.
