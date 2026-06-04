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
- **Apache Kafka**: Message Broker (Cần thiết cho xử lý sự kiện bất đồng bộ).

### Hướng dẫn chạy từng dịch vụ

#### 1. Khối Backend (Spring Boot)
1. Mở Terminal và di chuyển vào thư mục: `cd SRS-Smart-Parking-System`
2. Thiết lập cấu hình biến môi trường (BẮT BUỘC):
   - Truy cập vào thư mục `src/main/resources/`.
   - Tìm file `application-placeholder.yml` và đổi tên nó thành `application.yml` (hoặc copy nội dung sang file mới).
   - Mở file `application.yml` vừa tạo, tìm tất cả các chuỗi có định dạng `[YOUR_...]` và thay thế bằng Key thật của bạn. Hệ thống yêu cầu cấu hình các thành phần sau:
     - **PostgreSQL & Redis:** Chuỗi kết nối Database và Cache.
     - **Kafka:** Địa chỉ Broker (VD: `localhost:9092`).
     - **Supabase:** Nhập `url`, `anon-key`, `service-role-key`, `jwt-secret` và `jwk-set-uri`.
     - **Twilio:** Nhập các Key SMS (`account-sid`, `auth-token`, v.v.).
     - **Sepay:** Nhập thông tin tài khoản ngân hàng để nhận thanh toán.
3. Biên dịch dự án (bỏ qua bước chạy Unit Test cho nhanh):
   ```bash
   mvn clean install -DskipTests
   ```
4. Khởi chạy ứng dụng:
   ```bash
   mvn spring-boot:run
   ```
5. Backend sẽ hoạt động tại địa chỉ: `http://localhost:8080`

#### 2. Khối Frontend (Flutter Mobile & Kiosk)
1. Mở Terminal và di chuyển vào thư mục dự án tương ứng:
   ```bash
   cd flutter_mobile_app
   # hoặc
   cd kiosk_app
   ```
2. Thiết lập biến môi trường (BẮT BUỘC):
   - Mở thư mục gốc của dự án, tìm file `.env.example`, copy nó và đổi tên thành `.env`.
   - Mở file `.env` lên, cấu hình địa chỉ API của Backend. 
     * Ví dụ nếu chạy giả lập Android: `API_BASE_URL=http://10.0.2.2:8080/api/v1`
     * Ví dụ nếu chạy Web/iOS: `API_BASE_URL=http://localhost:8080/api/v1`
     * Cấu hình thêm các Key của Supabase (nếu có yêu cầu trong file).
3. Dọn dẹp cache và tải toàn bộ các thư viện (Packages) mới nhất:
   ```bash
   flutter clean
   flutter pub get
   ```
4. Khởi chạy ứng dụng:
   ```bash
   flutter run
   ```

#### 3. Khối AI Desktop (Python)
1. Mở Terminal và di chuyển vào thư mục: `cd sps_desktop`
2. Thiết lập cấu hình: Tạo file `.env` từ `.env.example` và điền biến môi trường.
3. Cài đặt các thư viện phụ thuộc:
   ```bash
   pip install -r requirements.txt
   ```
4. Khởi chạy ứng dụng:
   ```bash
   python main.py
   ```

---

## Phần 2: Phương pháp triển khai lên Kubernetes (Môi trường Prod)

Kiến trúc này áp dụng luồng **GitOps** hoàn chỉnh. Hệ thống tự động đồng bộ mã nguồn thông qua ArgoCD.

### Yêu cầu hạ tầng (Prerequisites)
Bạn cần có một cụm Kubernetes (K8s) trắng. Sau đó, **bắt buộc** phải cài đặt các hạ tầng phụ trợ (Dependencies) sau đây trước khi triển khai ứng dụng:

#### 1. Cài đặt Ingress Controller (Nginx)
Dùng để quản lý định tuyến HTTP/HTTPS từ ngoài vào Cluster.
```bash
helm upgrade --install ingress-nginx ingress-nginx \
  --repo https://kubernetes.github.io/ingress-nginx \
  --namespace ingress-nginx --create-namespace
```

#### 2. Cài đặt PostgreSQL (Bitnami)
```bash
helm install postgresql oci://registry-1.docker.io/bitnamicharts/postgresql \
  --set auth.postgresPassword=SieuBaoMat123 \
  --namespace default
```

#### 3. Cài đặt Redis (Bitnami)
```bash
helm install redis oci://registry-1.docker.io/bitnamicharts/redis \
  --set auth.password=SieuBaoMat123 \
  --namespace default
```

#### 4. Cài đặt Apache Kafka (Bitnami hoặc Strimzi)
Hệ thống bắn event bằng Kafka, do đó phải có Kafka Broker trong Cluster:
```bash
helm install kafka oci://registry-1.docker.io/bitnamicharts/kafka \
  --namespace default
```

### Quản lý Cấu hình & Bảo mật trong K8s (Quan trọng)

Trước khi chạy Kustomize, bạn cần đảm bảo cấu hình K8s đang trỏ đúng vào các dịch vụ hạ tầng vừa cài.

**1. File ConfigMap (`k8s/base/config.yaml`)**
- Mở file này ra, đây là nơi chứa các biến **KHÔNG NHẠY CẢM** (Public).
- Đảm bảo `SPRING_KAFKA_BOOTSTRAP_SERVERS` trỏ đúng vào Service của Kafka trong K8s (VD: `kafka-client.default.svc.cluster.local:9092`).
- Đảm bảo `API_BASE_URL` trỏ đúng Domain mà bạn sẽ dùng.

**2. File Secrets (`k8s/base/sps-sealed-secret.yaml`)**
- Ứng dụng quản lý mật khẩu qua **Sealed Secrets** để mã hóa an toàn trên Git. File này chứa Password DB, JWT Secret, Twilio Keys, v.v.
- **Nếu bạn không có Sealed Secrets Controller trong K8s:** Bạn có thể xóa file `sps-sealed-secret.yaml` và thay thế bằng một file `secret.yaml` thuần túy của K8s:
  ```yaml
  apiVersion: v1
  kind: Secret
  metadata:
    name: sps-backend-secrets
  type: Opaque
  stringData:
    SPRING_DATASOURCE_PASSWORD: "SieuBaoMat123"
    SPRING_DATASOURCE_URL: "jdbc:postgresql://postgresql.default.svc.cluster.local:5432/postgres"
    TWILIO_AUTH_TOKEN: "your_real_token"
    SUPABASE_JWT_SECRET: "your_real_secret"
    # (Khai báo tương tự cho tất cả các key bảo mật khác, ánh xạ 1-1 với application.yml)
  ```
- **Nếu bạn có Sealed Secrets:** Hãy dùng lệnh `kubeseal -o yaml < secret.yaml > k8s/base/sps-sealed-secret.yaml` để mã hóa file Secret của bạn trước khi Push lên Git.

### Triển khai Ứng dụng (Deployment)

#### Bước 1: Khởi tạo Namespace
```bash
kubectl create namespace sps
```

#### Bước 2: Kích hoạt ứng dụng qua Kustomize
Chạy lệnh sau tại thư mục gốc của dự án:
```bash
kubectl apply -k k8s/base
```
*Lệnh này sẽ quét toàn bộ file Yaml trong thư mục `k8s/base` và khởi tạo: Backend, Mobile Web, Kiosk Web, Desktop App và các Ingress Routes.*

#### Bước 3: Cấu hình GitOps với ArgoCD (Khuyến nghị)
1. Cài đặt ArgoCD vào Cluster.
2. Truy cập giao diện ArgoCD.
3. Tạo Application mới, trỏ Repository URL về dự án này và thiết lập Path là `k8s/base`.
4. Bật tính năng Auto-Sync. Từ nay, mọi thay đổi trên thư mục `k8s/base` hoặc Docker Hub đều sẽ được tự động đồng bộ xuống K8s mà không cần gõ lệnh.

### Cấu hình Tên miền cục bộ (Local DNS)
Để Ingress Controller phân luồng chính xác, cấu hình giả lập DNS trên máy cá nhân (`/etc/hosts` hoặc `C:\Windows\System32\drivers\etc\hosts`):
```text
127.0.0.1   sps.local
127.0.0.1   api.sps.local
127.0.0.1   argocd.sps.local
```

### Danh mục Truy cập Hệ thống
Sau khi Pods `Running`, truy cập qua các địa chỉ:
- **Backend API:** `http://api.sps.local`
- **Mobile Web App:** `http://sps.local/mobile`
- **Kiosk Web App:** `http://sps.local/kiosk`
