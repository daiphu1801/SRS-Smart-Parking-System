# Smart Parking System (Hệ thống Bãi đỗ xe Thông minh)

Tài liệu này cung cấp hướng dẫn chi tiết về 2 phương pháp triển khai dự án:
1. **Môi trường Phát triển (Development):** Chạy trực tiếp từ Source Code.
2. **Môi trường Thực tế (Production):** Triển khai lên cụm Kubernetes với kiến trúc GitOps.

---

## Phần 1: Phương pháp chạy từ Source Code (Môi trường Dev)

Phương pháp này dành cho các lập trình viên cần phát triển tính năng mới, gỡ lỗi (debug) và kiểm thử cục bộ.

### Yêu cầu hệ thống (Prerequisites)
- **Java 21+** và **Maven**: Dành cho khối Backend (Spring Boot).
- **Flutter SDK (Stable channel)**: Dành cho khối Mobile App và Kiosk App.
- **Python 3.10+**: Dành cho khối AI Desktop.
- **Cơ sở dữ liệu**: PostgreSQL (Lưu trữ chính), Redis (Cache & Lock phân tán).
- **Tài khoản Supabase**: Sử dụng cho xác thực và lưu trữ đối tượng (Storage).

### Hướng dẫn chạy từng dịch vụ

#### 1. Khối Backend (Spring Boot)
1. Mở Terminal và di chuyển vào thư mục: `cd SRS-Smart-Parking-System`
2. Tạo file cấu hình môi trường `.env` hoặc chỉnh sửa trực tiếp trong `application.yml` (bổ sung chuỗi kết nối DB, Redis, và thông tin Supabase).
3. Biên dịch và khởi chạy:
   ```bash
   mvn clean install -DskipTests
   mvn spring-boot:run
   ```
4. Backend sẽ hoạt động tại địa chỉ: `http://localhost:8080`

#### 2. Khối Frontend (Flutter Mobile & Kiosk)
1. Mở Terminal và di chuyển vào thư mục dự án tương ứng:
   ```bash
   cd flutter_mobile_app
   # hoặc
   cd kiosk_app
   ```
2. Cài đặt các gói phụ thuộc (Dependencies):
   ```bash
   flutter pub get
   ```
3. Chạy ứng dụng trên thiết bị ảo hoặc trình duyệt:
   ```bash
   flutter run
   ```

#### 3. Khối AI Desktop (Python)
1. Mở Terminal và di chuyển vào thư mục: `cd sps_desktop`
2. Khuyến nghị tạo môi trường ảo (Virtual Environment):
   ```bash
   python -m venv venv
   source venv/bin/activate  # (Dành cho Linux/Mac)
   venv\Scripts\activate     # (Dành cho Windows)
   ```
3. Cài đặt các thư viện cần thiết:
   ```bash
   pip install -r requirements.txt
   ```
4. Khởi chạy ứng dụng:
   ```bash
   python main.py
   ```

---

## Phần 2: Phương pháp triển khai lên Kubernetes (Môi trường Prod)

Kiến trúc này áp dụng luồng **GitOps** hoàn chỉnh. Hệ thống tự động đồng bộ mã nguồn thông qua ArgoCD, loại bỏ hoàn toàn các thao tác thủ công. Không yêu cầu cài đặt môi trường lập trình, chỉ cần có hạ tầng Container.

### Yêu cầu hạ tầng (Prerequisites)
- Cụm **Kubernetes** đang hoạt động (Ví dụ: K3s, Minikube, EKS, GKE, v.v.).
- Đã cài đặt **Nginx Ingress Controller** (Quản lý định tuyến HTTP).
- Đã cài đặt **ArgoCD** (Quản lý trạng thái GitOps).
- Đã cài đặt **ArgoCD Image Updater** (Tự động cập nhật phiên bản Docker Image mới nhất).

### Cấu hình Tên miền cục bộ (Local DNS)
Để Ingress Controller có thể phân luồng chính xác, bạn cần cấu hình giả lập DNS trên máy cá nhân. Mở file `/etc/hosts` (với Linux/Mac) hoặc `C:\Windows\System32\drivers\etc\hosts` (với Windows) bằng quyền Quản trị viên và thêm:
```text
127.0.0.1   sps.local
127.0.0.1   argocd.sps.local
```

### Các bước Triển khai (Deployment Steps)

#### Bước 1: Khởi tạo Namespace
Phân lập tài nguyên hệ thống vào một Namespace riêng biệt để dễ quản lý:
```bash
kubectl create namespace sps
```

#### Bước 2: Kích hoạt ứng dụng qua Kustomize
Hệ thống K8s đã được khai báo tập trung. Bạn chỉ cần đứng ở thư mục gốc của dự án và chạy duy nhất một lệnh:
```bash
kubectl apply -k k8s/base
```
*Lệnh trên sẽ tự động khởi tạo: Các Pod (Backend, Mobile, Kiosk, AI Desktop, PostgreSQL, Redis), Services, Ingress Routes và các ConfigMap liên quan.*

#### Bước 3: Cấu hình GitOps với ArgoCD (Khuyến nghị)
1. Truy cập trang quản trị ArgoCD tại `https://argocd.sps.local`
2. Tạo Application mới, trỏ kho (Repository URL) về dự án này và thiết lập Path là `k8s/base`.
3. Bật tính năng Auto-Sync.
4. ArgoCD Image Updater sẽ tự động theo dõi Github Actions CI. Khi có bản build mới đẩy lên Docker Hub, hệ thống sẽ tự động cập nhật Pods mà không cần sự can thiệp của con người.

### Danh mục Truy cập Hệ thống
Sau khi trạng thái các Pod chuyển sang `Running`, hệ thống có thể được truy cập qua các địa chỉ sau:
- **Backend API:** `http://sps.local/api`
- **Mobile Web App:** `http://sps.local/mobile`
- **Kiosk Web App:** `http://sps.local/kiosk`
