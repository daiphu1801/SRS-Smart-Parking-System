# Hệ thống Đỗ xe Thông minh — Monorepo 🚗

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)
![Flutter](https://img.shields.io/badge/Flutter-3.x-blue.svg)
![Python](https://img.shields.io/badge/Python-3.11-yellow.svg)

🌐 **English version?** Vui lòng xem [README.md](./README.md).

## 📖 Giới thiệu Dự án
Hệ thống quản lý bãi đỗ xe thông minh (Smart Parking System) là một giải pháp tự động hóa hoàn toàn luồng đỗ xe bằng Trí tuệ Nhân tạo (AI). Thay vì sử dụng thẻ từ (RFID) thủ công và vé giấy truyền thống, hệ thống sử dụng module Camera Ai nhận diện biển số xe (LPR) tốc độ cao để tự động đóng/mở barie, mang lại trải nghiệm ra vào rảnh tay (hands-free) 100% cho cư dân và khách hàng.

## ✨ Tính năng Cốt lõi
- **Ra Vào Tự Động:** AI trích xuất biển số tự động mở cổng trong chưa tới 2 giây, loại bỏ hoàn toàn vé vật lý.
- **Tính cước Động & Gói Thuê bao:** Cấu trúc tính phí đa dạng theo giờ, ngày, và các gói thuê bao VIP/tháng áp dụng cho nhiều loại phương tiện khác nhau.
- **Can thiệp Kịp thời từ Bảo vệ:** Sử dụng WebSocket để đẩy cảnh báo vi phạm, cảnh báo hết chỗ, và tín hiệu bất thường theo thời gian thực tới màn hình trạm bảo vệ.
- **Hệ Sinh thái Quản trị Thống nhất:** Một hệ thống duy nhất kết hợp giữa xử lý AI tại biên (edge), luồng nghiệp vụ kinh doanh dày đặc trên cloud, và giao diện siêu mượt cho người dùng cuối qua app di động.

## 🔄 Luồng Hoạt động (User Journey)
1. **Xe vào trạm:** Khách hàng điều khiển phương tiện tiến vào làn đỗ xe. IP Camera chụp lại khung hình.
2. **Suy luận AI:** Ứng dụng `python_edge_desktop` tại trạm chạy mô hình YOLOv8 để định vị xe và EasyOCR để đọc biển số hoàn toàn offline.
3. **Kiểm tra Nghiệp vụ:** Kết quả biển số được ném qua Kafka về Backend `java_spring_core`. Máy chủ kiểm tra xe có nằm trong sổ đen hoặc có gói thuê bao hợp lệ hay không.
4. **Mở cổng Vật lý:** Nếu hợp lệ, hệ thống bắn lệnh điều khiển relay GPIO mở barie, đồng thời ghi nhận phiên đỗ xe (Parking session) vào Database PostgreSQL.
5. **Theo dõi qua App:** Khách hàng mở ứng dụng `flutter_mobile_app` để xem phiên đỗ xe hiện tại, kiểm tra cước phí, đăng ký gói tháng hoặc trả tiền trực tiếp qua mã QR.
6. **Rời đi & Trừ tiền:** Camera luồng ra quét lại biển số. Server tính toán tổng thời gian và tự động trừ tiền trong ví (hoặc khách phải quét QR). Nếu tài khoản đủ tiền, barie lối ra tự động mở.

---

## 📦 Cấu trúc Kho Mã nguồn
Dự án được tổ chức theo kiến trúc **Monorepo** bao gồm 3 môi trường chính:

```text
smart-parking-monorepo/
├── 📂 flutter_mobile_app/       # Ứng dụng Khách hàng + Bảo vệ (Flutter)
├── 📂 python_edge_desktop/      # AI Engine + Giao diện Desktop (Python Flet)
└── 📂 java_spring_core/         # Backend API + Nghiệp vụ lõi (Spring Boot)
```

---

## 🔑 Kiến Trúc Hệ Thống

```text
Mobile/Desktop ──HTTP──► Spring Boot ──Kafka──► Python Edge
                               │                      │
                           PostgreSQL              GPIO/Camera
```

- **Event-Driven:** Python AI tạo sự kiện nhận diện → Kafka → Spring Boot tính toán cước → Ra lệnh mở barie.
- **WebSocket:** Broadcast cảnh báo vi phạm realtime trực tiếp tới Guard App.
- **Hot-reload Config:** `settings.json` — Giúp Admin điều chỉnh độ trễ barie, hệ số nhận dạng LPR... mà không cần khởi động lại dịch vụ vùng biên (edge).

---

## ⚙️ Hướng dẫn Cài đặt Chi tiết

### Yêu cầu hệ thống (Prerequisites)
Vui lòng đảm bảo máy tính đã cài đặt các công cụ sau chuẩn bị chạy dự án:
- **Java 21**
- **Python 3.11+**
- **Flutter SDK 3.x**
- **PostgreSQL** (chạy tại port mặc định `5432`)
- **Apache Kafka** (Zookeeper trên port `2181`, Kafka broker trên `9092`)

### 1. Cài đặt Cơ sở dữ liệu
1. Mở phần mềm quản trị PostgreSQL (ví dụ pgAdmin hoặc chạy lệnh từ Command Line).
2. Tạo database trắng cho dự án:
   ```sql
   CREATE DATABASE smart_parking_db;
   ```
3. *Lưu ý: Bạn không cần nạp Script SQL thủ công, Spring Boot (Hibernate) sẽ tự động sinh và cập nhật bảng (Auto-DDL) trong lần đầu chạy khởi động.*

### 2. Khởi chạy Backend: Java Spring Core
1. Truy cập thư mục backend:
   ```bash
   cd java_spring_core
   ```
2. Mở file `src/main/resources/application.properties` (hoặc `.yml`) và cập nhật tài khoản/mật khẩu PostgreSQL, địa chỉ Kafka nếu khác cài đặt mặc định máy bạn.
3. Chờ công cụ build cập nhật các the viện (Maven/Gradle) rồi chạy:
   ```bash
   ./mvnw spring-boot:run
   ```
   *(Backend API sẽ hoạt động tại `http://localhost:8080`)*

### 3. Cài đặt AI Platform & Desktop UI: Python Flet
1. Mở thư mục chứa Edge App:
   ```bash
   cd python_edge_desktop
   ```
2. Tạo và kích hoạt môi trường ảo ảo (Virtual Environment):
   ```bash
   # Dành cho Windows:
   python -m venv .venv
   .venv\Scripts\activate
   
   # Dành cho MacOS/Linux:
   python3 -m venv .venv
   source .venv/bin/activate
   ```
3. Cài đặt các thư viện bắt buộc (như Flet, YOLOv8, OpenCV,...):
   ```bash
   pip install -r requirements.txt
   ```
4. Chạy giao diện quản lý / bốt trực bảo vệ:
   ```bash
   flet run main_desktop.py
   ```
   *(Để đổi giao diện giữa Admin, Quản lý hoặc Bảo vệ, hãy sửa biến `role` nằm trong file `main_desktop.py`)*

### 4. Cài đặt Ứng dụng Di động: Flutter
1. Trỏ con trỏ cmd vào thư mục ứng dụng Mobile:
   ```bash
   cd flutter_mobile_app
   ```
2. Yêu cầu tải thư viện Dart/Flutter về máy:
   ```bash
   flutter pub get
   ```
3. Nối thiết bị gắn ngoài qua cáp hoặc mở Emulator rồi dùng lệnh:
   ```bash
   flutter run
   ```

---

## 🎨 Design System (Hệ thống thiết kế)

Toàn bộ ứng dụng (kể cả Mobile lẫn Desktop Admin) phải tuân thủ nghiêm ngặt hệ bảng 2 tông màu chính:
- **Primary (Màu chính):** `#052e16` (Xanh lá đậm)
- **Background (Nền):** `#ffffff` (Trắng)
- **Font chữ:** Dòng chữ Inter định dạng sẵn (Regular 400, Medium 500, Semibold 600) — *Tuyệt đối không dùng in đậm (Bold).*
- **Quy tắc thiết kế UI:** Tuyệt đối không xài Gradient | Không đổ bóng (Shadow) | Các panel, thẻ card, nút bấm tùy độ lớn mà bo viền 8px/6px/4px đồng đều.
