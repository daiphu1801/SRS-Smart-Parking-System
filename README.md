# SRS Smart Parking System

## Overview
Hệ thống SRS (Smart Parking System) là một giải pháp quản lý bãi đỗ xe toàn diện, được thiết kế dựa trên kiến trúc hướng dịch vụ (Microservices/Monorepo). Hệ thống tích hợp năng lực nhận diện khuôn mẫu tự động (ALPR - Automatic License Plate Recognition) ứng dụng trí tuệ nhân tạo, kết hợp cùng hệ thống quản trị trung tâm và các ứng dụng giao diện người dùng đa nền tảng.

## Architecture
Hệ thống bao gồm 04 phân hệ cốt lõi hoạt động độc lập:

1. **SRS-Smart-Parking-System (Core Backend)**
   - Ngôn ngữ & Framework: Java, Spring Boot.
   - Chức năng: Xử lý logic nghiệp vụ, quản lý cơ sở dữ liệu quan hệ (PostgreSQL), tích hợp bộ nhớ đệm (Redis), xác thực bảo mật và điều phối giao dịch hệ thống.

2. **sps_desktop (AI Computer Vision Module)**
   - Ngôn ngữ & Framework: Python, Flet, OpenCV, YOLO.
   - Chức năng: Xử lý luồng dữ liệu hình ảnh thời gian thực (RTSP), bóc tách và phân tích dữ liệu biển số xe thông qua các mô hình Deep Learning.

3. **kiosk_app (Kiosk Web Terminal)**
   - Ngôn ngữ & Framework: Dart, Flutter Web.
   - Chức năng: Trạm điều khiển cảm ứng dành cho bộ phận an ninh, giao tiếp trực tiếp với Backend API để kiểm soát rào chắn (Barrier) và luồng phương tiện.

4. **flutter_mobile_app (Customer Application)**
   - Ngôn ngữ & Framework: Dart, Flutter (Cross-platform).
   - Chức năng: Cổng thông tin tương tác dành cho khách hàng, hỗ trợ định vị bãi đỗ, quản lý phương tiện và thanh toán điện tử.

## System Requirements
- Docker Engine & Docker Compose (Khuyến nghị phiên bản mới nhất).
- Docker Desktop (yêu cầu kích hoạt WSL 2 integration nếu triển khai trên môi trường Windows).
- Git Version Control.

## Environment Configuration
Để đảm bảo an toàn thông tin, các tham số cấu hình nhạy cảm (Credentials, API Keys) được quản lý thông qua biến môi trường tĩnh. Quá trình thiết lập yêu cầu khởi tạo các tệp cấu hình thực tế từ các tệp mẫu (Placeholder files).

**1. Phân hệ Backend (Java)**
- Định tuyến: `SRS-Smart-Parking-System/src/main/resources/`
- Thao tác: Nhân bản tệp `application-placeholder.yml` thành `application.yml`. Cấp phát các giá trị xác thực cho cơ sở dữ liệu và hạ tầng mạng tại các biến có đánh dấu `[YOUR_...]`.

**2. Các phân hệ Client (Python & Flutter)**
- Định tuyến: Thư mục gốc của `sps_desktop`, `kiosk_app`, và `flutter_mobile_app`.
- Thao tác: Nhân bản tệp `.env.example` thành `.env`. Thiết lập các khóa xác thực của Supabase và Endpoint của Backend (`API_BASE_URL`).

## Deployment Instructions
Mỗi phân hệ đã được cấu hình Containerization thông qua `Dockerfile`. Khởi tạo quy trình triển khai bằng cách thực thi tuần tự các tập lệnh sau tại thư mục tương ứng:

### 1. Triển khai Backend Core
```bash
cd SRS-Smart-Parking-System
docker build -t srs-backend:latest .
docker run -d -p 8080:8080 --name sps-backend srs-backend:latest
```

### 2. Triển khai AI Computer Vision Module
```bash
cd sps_desktop
docker build -t sps-desktop-app:latest .
docker run -d -p 8550:8550 -e API_BASE_URL=http://host.docker.internal:8080/api/v1 --name sps-desktop-container sps-desktop-app:latest
```

### 3. Triển khai Kiosk Web Terminal
```bash
cd kiosk_app
docker build -t srs-kiosk-app:latest .
docker run -d -p 8081:80 --name sps-kiosk-container srs-kiosk-app:latest
```

### 4. Triển khai Customer Application (PWA)
```bash
cd flutter_mobile_app
docker build -t srs-mobile-app:latest .
docker run -d -p 8082:80 --name sps-mobile-container srs-mobile-app:latest
```

## Network Topology & Ports

| Tên Dịch Vụ | Nền Tảng | TCP Port | Endpoint Triển Khai (Local) |
|-------------|----------|----------|-----------------------------|
| Backend API | Spring Boot | 8080 | http://localhost:8080/api/v1 |
| Kiosk Terminal | Flutter Web | 8081 | http://localhost:8081 |
| Customer Application | Flutter Web | 8082 | http://localhost:8082 |
| AI Camera Dashboard | Python Flet | 8550 | http://localhost:8550 |

*Ghi chú: Đối với môi trường Production (Kubernetes/Cloud), quy trình thiết lập biến môi trường và điều hướng (Routing) sẽ do Ingress Controller và Secret Manager quản lý độc lập.*
