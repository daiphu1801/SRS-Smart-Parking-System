# Hệ thống Đỗ xe Thông minh — Monorepo 🚗

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)
![Flutter](https://img.shields.io/badge/Flutter-3.x-blue.svg)
![Python](https://img.shields.io/badge/Python-3.11-yellow.svg)

🌐 **English version?** Vui lòng xem [README.md](./README.md).

Hệ thống quản lý bãi đỗ xe thông minh sử dụng AI Camera, được tổ chức dạng **Monorepo** với 3 thư mục chính:

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
