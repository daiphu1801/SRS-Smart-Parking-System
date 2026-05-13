# Hướng Dẫn Khởi Chạy Dự Án Smart Parking System (Local)

Dưới đây là tài liệu được tùy chỉnh dựa trên môi trường hiện tại của máy tính anh để đảm bảo cấu trúc dự án hoạt động trơn tru. Hệ thống bao gồm 3 phân hệ chính: **Backend (Java Spring)**, **Edge AI (Python Flet)** và **Mobile App (Flutter)** chạy trên Windows.

---

## 1. Dịch vụ nền tảng (BẮT BUỘC CHẠY TRƯỚC)

Trước khi chạy code ứng dụng, hệ thống cần có cơ sở dữ liệu và message broker đang hoạt động.

- **PostgreSQL (5432):** Đảm bảo dịch vụ PostgreSQL đang chạy. Hệ thống sử dụng database `smart_parking_db` (sẽ được tự động tạo/cập nhật bảng nhờ Hibernate auto-DDL).
- **Apache Kafka (9092):** 
  - Khởi động Zookeeper: `zookeeper-server-start.bat config\zookeeper.properties`
  - Khởi động Kafka Broker: `kafka-server-start.bat config\server.properties`

> **Lưu ý:** Nếu Kafka không chạy, Spring Boot sẽ liên tục văng lỗi hoặc không thể khởi động do không kết nối được tới cổng 9092.

---

## 2. Khởi chạy Backend (Java Spring Core)

Do bộ lệnh Maven (`mvn` hoặc `./mvnw`) hiện **không được cài đặt** ở biến môi trường hoặc thư mục gốc, anh sẽ khởi chay Backend bằng IDE (VS Code):

1. Mở thư mục `java_spring_core` bằng VS Code.
2. Tại cây thư mục bên trái (Explorer), mở file `src/main/java/com/smartparking/SmartParkingApplication.java`.
3. Bấm **"Run"** hoặc **"Debug"** ở góc trên bên phải hoặc ở đầu hàm `main()` (Cần có **Extension Pack for Java** của VS Code).
4. Đợi server khởi chạy thành công trên màn hình Terminal (thường ở cổng 8080).

---

## 3. Khởi chạy Edge AI Engine & Desktop UI (Python Flet)

Máy anh hiện đang sử dụng **Python 3.15.0a3**. Vì phiên bản này khá mới, một số thư viện như `numpy` chưa có bản pre-build nên sẽ tự động biên dịch lại từ source C/C++ mỗi khi cài, quá trình này có thể tốn vài phút.

**Các lệnh thực hiện dưới cửa sổ Terminal PowerShell:**
```powershell
cd f:\Project_personal\SRS_project\python_edge_desktop

# 1. Kích hoạt môi trường ảo (.venv)
.\.venv\Scripts\activate

# 2. Cài đặt các thư viện cần thiết (Chờ numpy build xong nếu là lần đầu chạy)
pip install -r requirements.txt

# 3. Chạy Desktop App
flet run main_desktop.py
```
> Khi load lần đầu tiên, model AI YOLOv8 `yolov8n.pt` có thể sẽ được tự động tải về thư mục máy.

---

## 4. Khởi chạy Ứng dụng Khách / Bảo vệ (Flutter Mobile App)

Thư mục dự án mặc định chưa có thư mục cấu trúc cho Windows. Để test được trên Desktop Windows như một App bình thường, chúng ta làm theo các lệnh sau:

**Các lệnh thực hiện dưới cửa sổ Terminal PowerShell:**
```powershell
cd f:\Project_personal\SRS_project\flutter_mobile_app

# 1. Bổ sung nền tảng Windows cho Flutter
flutter create . --platforms windows

# 2. Bắt đầu build và khởi chạy ứng dụng Flutter trên Windows
flutter run -d windows
```
> Để hot-reload trong lúc debug, anh có thể ấn phím `r` ngay tại Terminal dùng lệnh `flutter run`.

---
**Troubleshooting:**
* Màn hình đen Flutter lúc mở: Đợi 1 chút để engine load.
* Lỗi YOLOv8 Not Found Pytorch: Check lại file `requirements.txt` trong file Python nhớ có thư viện `ultralytics` và `torch`.

---

## 5. Chạy Flutter App trên Android Emulator

Máy đã có Android SDK & 2 emulator sẵn. Vì lệnh `emulator` chưa có trong PATH, dùng lệnh của Flutter thay thế.

### Bước 1: Xem danh sách emulator hiện có
```powershell
flutter emulators
```
Kết quả hiện tại trên máy:
```
Id                    • Name
Medium_Phone_API_36.1 • Medium Phone API 36.1
Pixel_4_XL            • Pixel 4 XL
```

### Bước 2: Khởi động emulator
```powershell
# Khởi động Pixel 4 XL (khuyến nghị dùng cái này để test UI mobile)
flutter emulators --launch Pixel_4_XL

# Hoặc khởi động Medium Phone
flutter emulators --launch Medium_Phone_API_36.1
```
> Đợi khoảng 30–60 giây để emulator khởi động hoàn toàn.

### Bước 3: Chạy app trên emulator vừa khởi động
```powershell
cd f:\Project_personal\SRS_project\flutter_mobile_app

# Flutter tự phát hiện emulator đang chạy và cài app lên đó
flutter run
```

> Nếu có nhiều device (Windows + emulator), chỉ định rõ bằng `-d android`:
> ```powershell
> flutter run -d android
> ```

### (Tùy chọn) Thêm emulator mới
```powershell
# Xem danh sách device definitions có sẵn
flutter emulators --create --name "MyPhone"

# Hoặc mở Android Studio → Device Manager → Create Device
```

---

**Troubleshooting (Emulator):**
* **`emulator` không nhận lệnh:** Dùng `flutter emulators --launch <id>` thay vì gọi thẳng `emulator`.
* **Emulator khởi động chậm:** Bật **Hardware Acceleration (HAXM / HyperV)** trong BIOS và Android Studio → SDK Manager → SDK Tools → Android Emulator Hypervisor Driver.
* **App cài lên emulator nhưng crash ngay:** Chạy `flutter clean && flutter run` để build lại từ đầu.
* **Emulator không hiện sau khi launch:** Đợi thêm 1–2 phút rồi chạy `flutter devices` để kiểm tra.

