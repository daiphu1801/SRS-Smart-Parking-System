
Hệ thống quản lý bãi đỗ xe thông minh toàn diện, tích hợp AI nhận diện biển số, Backend mạnh mẽ và giao diện đa nền tảng dành cho cả Quản trị viên, Bảo vệ và Khách hàng.
Hệ thống được chia thành 4 phân hệ độc lập (Microservices/Monorepo):

1. **Backend Core (`SRS-Smart-Parking-System`)**: 
   - Ngôn ngữ: Java (Spring Boot)
   - Chức năng: Xử lý logic nghiệp vụ cốt lõi, quản lý Database (PostgreSQL), kết nối Redis, xử lý giao dịch thanh toán và xác thực người dùng.
2. **AI & Camera Processing (`sps_desktop`)**: 
   - Ngôn ngữ: Python (Flet, OpenCV, YOLO)
   - Chức năng: Xử lý luồng Camera theo thời gian thực (RTSP), nhận diện biển số xe bằng AI, và gửi dữ liệu về Backend.
3. **Kiosk Web App (`kiosk_app`)**:
   - Ngôn ngữ: Flutter Web
   - Chức năng: Giao diện màn hình Kiosk đặt tại bãi đỗ xe dành cho nhân viên bảo vệ kiểm soát vào ra.
4. **Customer Mobile App (`flutter_mobile_app`)**:
   - Ngôn ngữ: Flutter (Android/iOS/Web)
   - Chức năng: Ứng dụng dành cho khách hàng tìm bãi đỗ, quản lý xe, và thanh toán online.

---

## ⚙️ Hướng Dẫn Cài Đặt (Setup)

### 1. Yêu cầu hệ thống
- Tải và cài đặt [Docker Desktop](https://www.docker.com/products/docker-desktop/) (Yêu cầu bật WSL 2 trên Windows).
- Git.

### 2. Thiết lập Biến Môi Trường (Mật khẩu & API Keys)
Để bảo mật, toàn bộ mật khẩu không được đưa lên Git. Bạn cần tự tạo các file cấu hình tại máy tính cá nhân (Local) dựa trên các file mẫu (Placeholder) có sẵn:

*Lưu ý: Mở file `.gitignore` để đảm bảo các file `.env` và `application.yml` không bị đẩy lên Git.*

**A. Đối với Backend Java:**
- Truy cập vào thư mục `SRS-Smart-Parking-System/src/main/resources/`.
- Copy file `application-placeholder.yml` và đổi tên thành `application.yml`.
- Mở file `application.yml` và điền các thông tin thật vào các chỗ có chữ `[YOUR_...]` (Ví dụ: DB_PASSWORD, REDIS_PASSWORD).

**B. Đối với 3 App còn lại (Python & Flutter):**
- Trong thư mục gốc của mỗi App (`sps_desktop`, `kiosk_app`, `flutter_mobile_app`), đều có một file tên là `.env.example`.
- Copy file đó và đổi tên thành `.env`.
- Mở file `.env` vừa tạo và điền các khóa API của Supabase và đường link Backend (`API_BASE_URL`).
  *(Ví dụ nếu chạy Local thì API_BASE_URL=http://localhost:8080)*

---

## 🚀 Hướng Dẫn Chạy Bằng Docker (Build & Run)

Mỗi dự án đều đã được đóng gói sẵn Dockerfile tối ưu. Hãy mở Terminal tại thư mục của từng dự án và chạy các lệnh tương ứng:

### 1. Khởi động Backend (Spring Boot)
```bash
cd SRS-Smart-Parking-System
docker build -t srs-backend:latest .
docker run -p 8080:8080 --name sps-backend srs-backend:latest
```

### 2. Khởi động AI Camera (Python Flet)
```bash
cd sps_desktop
docker build -t sps-desktop-app:latest .
docker run -p 8550:8550 -e API_BASE_URL=http://host.docker.internal:8080/api/v1 --name sps-desktop-container sps-desktop-app:latest
```

### 3. Khởi động Kiosk App (Flutter Web)
```bash
cd kiosk_app
docker build -t srs-kiosk-app:latest .
docker run -p 8081:80 --name sps-kiosk-container srs-kiosk-app:latest
```

### 4. Khởi động Mobile App (Flutter Web PWA)
```bash
cd flutter_mobile_app
docker build -t srs-mobile-app:latest .
docker run -p 8082:80 --name sps-mobile-container srs-mobile-app:latest
```

---

## 🌐 Sơ Đồ Cổng Kết Nối (Ports)

Sau khi chạy thành công 4 lệnh trên, bạn có thể truy cập hệ thống qua trình duyệt Web theo các cổng sau:

| Dịch Vụ | Nền tảng | Cổng (Port) | Link Truy Cập Local |
|---------|---------|-------------|---------------------|
| **Backend API** | Java Spring | `8080` | `http://localhost:8080/api/v1` |
| **Kiosk App** | Flutter Web | `8081` | `http://localhost:8081` |
| **Mobile App** | Flutter Web | `8082` | `http://localhost:8082` |
| **AI Camera** | Python Web | `8550` | `http://localhost:8550` |

*Lưu ý: Ứng dụng Mobile có thể được Build ra file `.apk` bằng lệnh `flutter build apk` nếu muốn cài đặt lên điện thoại Android thực tế.*
