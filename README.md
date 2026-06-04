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

Kiến trúc này áp dụng luồng **GitOps** hoàn chỉnh (ArgoCD), chiến lược **Canary Deployment** (Argo Rollouts), quản lý mật mã (Sealed Secrets) và tích hợp sâu hệ thống **Observability** (Prometheus, Grafana, Loki).

### Yêu cầu hạ tầng (Prerequisites)
Bạn cần có một cụm Kubernetes (K8s) trắng. Sau đó, **bắt buộc** phải cài đặt các hạ tầng phụ trợ (Dependencies) sau đây bằng Helm trước khi triển khai ứng dụng:

*(Lưu ý: Hệ thống không cài đặt PostgreSQL vì bắt buộc sử dụng hạ tầng Database-as-a-Service của Supabase).*

#### 1. Mạng lưới & Bảo mật (Ingress & VPN)
- **Nginx Ingress Controller:** Quản lý định tuyến HTTP/HTTPS từ ngoài vào Cluster.
  ```bash
  helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx -n ingress-nginx --create-namespace
  ```
- **Tailscale Operator:** Dùng để tạo mạng VPN nội bộ, giúp ẩn Ingress của Grafana/ArgoCD khỏi public Internet.
  ```bash
  helm upgrade --install tailscale tailscale/tailscale-operator -n tailscale --create-namespace \
    --set oauth.clientId=<YOUR_TAILSCALE_CLIENT_ID> \
    --set oauth.clientSecret=<YOUR_TAILSCALE_CLIENT_SECRET>
  ```

#### 2. Dịch vụ Lõi (Message Broker & Cache)
- **Apache Kafka (Bitnami):**
  ```bash
  helm upgrade --install kafka oci://registry-1.docker.io/bitnamicharts/kafka -n default
  ```
- **Redis (Bitnami):**
  ```bash
  helm upgrade --install redis oci://registry-1.docker.io/bitnamicharts/redis -n default --set auth.password=SieuBaoMat123
  ```

#### 3. Hệ sinh thái Giám sát & Logs (Observability Stack)
Cài đặt vào Namespace `logging`. Hệ thống này **bắt buộc** phải có vì Argo Rollouts dựa vào Prometheus để chấm điểm Canary Deployment (File `analysis-template.yaml`).
- **Kube-Prometheus-Stack (Prometheus + Grafana + AlertManager):**
  ```bash
  helm upgrade --install prometheus prometheus-community/kube-prometheus-stack -n logging --create-namespace
  ```
- **Loki-Stack (Loki + Promtail):** Thu thập toàn bộ log của các Pod.
  ```bash
  helm upgrade --install loki grafana/loki-stack -n logging
  ```

#### 4. Hệ sinh thái GitOps & Controller
- **ArgoCD:** Đồng bộ trạng thái GitOps.
  ```bash
  helm upgrade --install argocd argo/argo-cd -n argocd --create-namespace
  ```
- **Argo Rollouts:** Quản lý Controller cho Canary Deployment.
  ```bash
  helm upgrade --install argo-rollouts argo/argo-rollouts -n argo-rollouts --create-namespace
  ```
- **Sealed Secrets:** Controller dùng để giải mã file `sps-sealed-secret.yaml` được lưu an toàn trên Github.
  ```bash
  helm upgrade --install sealed-secrets sealed-secrets/sealed-secrets -n kube-system
  ```

---

### Quản lý Cấu hình trong K8s (Quan trọng)

Trước khi chạy Kustomize để kích hoạt ứng dụng, bạn cần cấu hình lại các thông số.

**1. File ConfigMap (`k8s/base/config.yaml`)**
- Chứa các biến Public. Sửa lại `API_BASE_URL` cho đúng tên miền thật.
- Đảm bảo `SPRING_KAFKA_BOOTSTRAP_SERVERS` trỏ đúng vào Kafka Broker trong K8s (VD: `kafka-client.default.svc.cluster.local:9092`).

**2. File Secrets (`k8s/base/sps-sealed-secret.yaml`)**
- Nếu có Sealed Secrets: Dùng lệnh `kubeseal -o yaml < secret.yaml > k8s/base/sps-sealed-secret.yaml` để mã hóa mật khẩu mới.
- Nếu muốn chạy K8s thuần: Xóa file sealed-secret đi, tạo một file `secret.yaml` thường với `kind: Secret` và điền dạng Text (nhớ mã hóa base64 nếu dùng `data`, hoặc dùng `stringData`).

---

### Triển khai Ứng dụng (Deployment)

#### Bước 1: Khởi tạo Namespace
```bash
kubectl create namespace sps
```

#### Bước 2: Bơm ứng dụng vào Cluster
Dùng Kustomize để deploy toàn bộ ứng dụng (Backend, Kiosk, Mobile, Desktop):
```bash
kubectl apply -k k8s/base
```

#### Bước 3: Đấu nối vào ArgoCD
- Đăng nhập giao diện ArgoCD (có thể là `argocd.sps.local` qua Tailscale VPN).
- Tạo Application mới, trỏ Repository URL về nhánh `main`, thư mục là `k8s/base`.
- Kích hoạt Auto-Sync. Từ nay CI (Github Actions) build xong đẩy image mới lên, hệ thống sẽ tự update và Argo Rollouts sẽ tự động chia 20% traffic (Canary) để test.

### Cấu hình Tên miền cục bộ (Local DNS)
Để Ingress Controller phân luồng chính xác, hãy thêm vào file `/etc/hosts`:
```text
127.0.0.1   sps.local
127.0.0.1   api.sps.local
127.0.0.1   monitor.sps.local
127.0.0.1   argocd.sps.local
```

### Danh mục Truy cập Hệ thống (Qua Ingress)
- **Backend API:** `http://api.sps.local`
- **Mobile Web App:** `http://sps.local/mobile`
- **Kiosk Web App:** `http://sps.local/kiosk`
- **Grafana Dashboard:** `http://monitor.sps.local` (Phải bật Tailscale)
- **ArgoCD UI:** `http://argocd.sps.local`
