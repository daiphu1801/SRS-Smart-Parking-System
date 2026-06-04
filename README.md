# Cẩm nang Vận hành Smart Parking System (Enterprise Edition)

Tài liệu này cung cấp toàn bộ tri thức kỹ thuật và quy trình vận hành Hệ thống Bãi đỗ xe Thông minh. Hệ thống được thiết kế theo chuẩn Enterprise, ứng dụng **GitOps**, **Canary Deployment** và kiến trúc **Hybrid-Cloud**.

---

## Chương 1: Kiến trúc Tổng thể (Architecture Overview)

Hệ thống bao gồm 4 ứng dụng cốt lõi:
1. **Backend (Spring Boot 3 + Java 21):** Xử lý nghiệp vụ lõi, giao tiếp với các hệ thống phụ trợ.
2. **Mobile App (Flutter Web):** Ứng dụng dành cho Khách hàng.
3. **Kiosk App (Flutter Web):** Ứng dụng dành cho máy quẹt thẻ tại cổng đỗ xe.
4. **AI Desktop (Python 3):** Xử lý nhận diện biển số tại biên (Edge Computing).

### Mô hình Hybrid-Cloud Data
- **Supabase (Cloud):** Đảm nhiệm vai trò cơ sở dữ liệu chính (PostgreSQL) và hệ thống Xác thực (Authentication). Dữ liệu được quản trị hoàn toàn trên Cloud, giảm tải cho K8s.
- **Kafka & Redis (On-Premise K8s):** Đảm nhiệm luồng Message Broker tốc độ cao (bắn event mở cổng, nhận diện biển số) và Caching phân tán.

### Luồng Triển khai Tự động (GitOps)
- Mã nguồn -> Github Actions (CI) -> Docker Hub.
- **ArgoCD** liên tục theo dõi Git Repository (thư mục `k8s/base`) và tự động kéo cấu hình mới nhất về Cluster.
- **Argo Rollouts** nhận Image mới, tiến hành **Canary Deployment** (chia 20% traffic) và đo lường tỷ lệ lỗi thông qua **Prometheus**.

---

## Chương 2: Môi trường Lập trình (Local Development)

Dành cho Developer muốn chạy và gỡ lỗi (Debug) hệ thống trên máy cá nhân.

### 2.1. Khối Backend (Spring Boot)
- **Yêu cầu:** Java 21+, Maven, IDE (IntelliJ/Eclipse).
- **Thiết lập Môi trường:**
  1. Di chuyển vào thư mục: `cd SRS-Smart-Parking-System/src/main/resources/`
  2. Copy file `application-placeholder.yml` thành `application.yml`.
  3. Mở `application.yml` và thay thế toàn bộ các biến `[YOUR_...]`:
     - Nhập chuỗi kết nối **PostgreSQL** và **Redis**.
     - Nhập URL của **Kafka Broker**.
     - Nhập các Key tích hợp của **Supabase** (URL, Anon Key, JWT Secret), **Twilio** (SMS), và **Sepay** (Thanh toán).
     *(Lưu ý: Spring Boot sử dụng tính năng Relaxed Binding, tự động ánh xạ cấu trúc Yaml).*
- **Khởi chạy:**
  ```bash
  mvn clean install -DskipTests
  mvn spring-boot:run
  ```

### 2.2. Khối Frontend (Flutter Mobile & Kiosk)
- **Yêu cầu:** Flutter SDK (Stable).
- **Thiết lập Môi trường:**
  1. Di chuyển vào thư mục `flutter_mobile_app` hoặc `kiosk_app`.
  2. Copy file `.env.example` thành `.env`.
  3. Mở `.env` và thiết lập `API_BASE_URL` trỏ về Backend (VD: `http://localhost:8080/api/v1`).
- **Khởi chạy:**
  ```bash
  flutter clean
  flutter pub get
  flutter run -d chrome
  ```

### 2.3. Khối AI Desktop (Python)
- **Yêu cầu:** Python 3.10+.
- **Khởi chạy:**
  ```bash
  cd sps_desktop
  python -m venv venv
  source venv/bin/activate
  pip install -r requirements.txt
  python main.py
  ```

---

## Chương 3: Khởi tạo Cụm K8s Trắng (Cluster Provisioning)

Tài liệu này giả định bạn đã có một cụm Kubernetes (EKS, GKE, K3s, Minikube).

### 3.1. Yêu cầu Công cụ CLI (Local Machine)
Đảm bảo máy tính quản trị của bạn đã cài đặt:
- `kubectl`: Giao tiếp với K8s.
- `helm`: Triển khai các gói hạ tầng.
- `kubeseal`: Công cụ sinh mã hóa bảo mật từ Bitnami.
- `kubectl argo rollouts`: Cài đặt từ [Argo Rollouts Github](https://github.com/argoproj/argo-rollouts/releases).

### 3.2. Cài đặt Hạ tầng (Bắt buộc chạy tuần tự bằng Helm)

#### A. Ingress & VPN
```bash
# Cài đặt Nginx Ingress Controller
helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx -n ingress-nginx --create-namespace

# Cài đặt Tailscale Operator (Ẩn các Dashboard Quản trị)
# Lấy Client ID/Secret từ https://login.tailscale.com/admin/settings/oauth
helm upgrade --install tailscale tailscale/tailscale-operator -n tailscale --create-namespace \
  --set oauth.clientId=<YOUR_TAILSCALE_CLIENT_ID> \
  --set oauth.clientSecret=<YOUR_TAILSCALE_CLIENT_SECRET>
```

#### B. Message Broker & Cache (Data Layer)
```bash
# Cài đặt Apache Kafka
helm upgrade --install kafka oci://registry-1.docker.io/bitnamicharts/kafka -n default

# Cài đặt Redis (Lưu ý đặt mật khẩu khớp với Cấu hình)
helm upgrade --install redis oci://registry-1.docker.io/bitnamicharts/redis -n default --set auth.password=SieuBaoMat123
```

#### C. Hệ sinh thái Giám sát (Observability Stack)
Namespace `logging` là bắt buộc vì Argo Rollouts gọi thẳng vào đây để lấy Metric.
```bash
# Kube-Prometheus-Stack (Cung cấp Prometheus & Grafana)
helm upgrade --install prometheus prometheus-community/kube-prometheus-stack -n logging --create-namespace

# Loki-Stack (Gom Log)
helm upgrade --install loki grafana/loki-stack -n logging
```
*(Tài khoản đăng nhập Grafana mặc định: `admin` / `prom-operator`)*

#### D. Hệ sinh thái GitOps
```bash
# Cài đặt ArgoCD
helm upgrade --install argocd argo/argo-cd -n argocd --create-namespace

# Cài đặt Argo Rollouts Controller
helm upgrade --install argo-rollouts argo/argo-rollouts -n argo-rollouts --create-namespace

# Cài đặt Sealed Secrets Controller
helm upgrade --install sealed-secrets sealed-secrets/sealed-secrets -n kube-system
```

---

## Chương 4: Quản lý Cấu hình K8s (Kustomize Overlays)

Dự án sử dụng kiến trúc Kustomize chia theo lớp:
- `k8s/base/`: Chứa định nghĩa chuẩn của các Deployment, Service, ConfigMap gốc.
- `k8s/overlays/dev/`: Ghi đè cấu hình cho môi trường Dev.
- `k8s/overlays/prod/`: Ghi đè cấu hình cho môi trường Prod.

Để thay đổi biến môi trường Public (VD: URL, Port):
1. Mở file `k8s/base/config.yaml`.
2. Thay đổi giá trị tương ứng (Đảm bảo tuân thủ cơ chế Relaxed Binding của Spring Boot, ví dụ `APP_TWILIO_MESSAGING_SERVICE_SID`).
3. (Tùy chọn) Có thể ghi đè thêm bằng file `config-patch.yaml` nằm trong các thư mục overlays.

---

## Chương 5: Quản lý Mật khẩu & Bảo mật (Sealed Secrets)

**Quy tắc tối thượng:** TUYỆT ĐỐI KHÔNG PUSH FILE SECRET THÔ DẠNG TEXT (HAY BASE64) LÊN GITHUB!

Hệ thống sử dụng **Sealed Secrets** mã hóa bất đối xứng. KubeSeal sẽ mã hóa mật khẩu bằng Public Key, và chỉ có Controller nằm sâu trong K8s (có Private Key) mới giải mã được.

### Hướng dẫn cập nhật Password / API Keys mới:
1. Tạo một file tên là `secret.yaml` (KHÔNG đẩy file này lên Git) có định dạng:
   ```yaml
   apiVersion: v1
   kind: Secret
   metadata:
     name: sps-backend-secrets
     namespace: default
   type: Opaque
   stringData:
     SPRING_DATASOURCE_PASSWORD: "Mat_Khau_Supabase_Moi"
     SUPABASE_JWT_SECRET: "Secret_Moi"
     APP_TWILIO_AUTH_TOKEN: "Token_Moi"
   ```
2. Mã hóa file bằng lệnh `kubeseal`:
   ```bash
   kubeseal -o yaml < secret.yaml > k8s/base/sps-sealed-secret.yaml
   ```
3. Lúc này file `sps-sealed-secret.yaml` đã biến thành một mớ ký tự mã hóa an toàn. Bạn thực hiện `git push` file này lên kho lưu trữ.
4. ArgoCD sẽ quét thấy thay đổi và đồng bộ xuống K8s, K8s Controller sẽ âm thầm giải mã nó thành Secret thật cho các Pods sử dụng.

---

## Chương 6: Vòng đời GitOps & Canary Deployment

Khi một Developer hoàn tất Code và Push lên nhánh `main`:
1. **Github Actions (CI)** tiến hành build, chạy Linting (Fast-Fail), và đóng gói thành Docker Image mới (Gắn thẻ bằng Git SHA).
2. Nó sẽ cập nhật thẻ tag mới này vào file của Kustomize.
3. **ArgoCD** phát hiện Kustomize có sự thay đổi Image, lập tức ra lệnh cho K8s cập nhật.
4. **Argo Rollouts** can thiệp vào quá trình này:
   - Nó KHÔNG đập bỏ ngay bản cũ.
   - Thay vào đó, nó khởi tạo 1 Pod bản mới, và định tuyến **20% người dùng** sang bản mới này (Canary).
   - Rollouts sẽ truy vấn vào **Prometheus** (`analysis-template.yaml`) để xem trong 1 phút vừa qua, tỷ lệ lỗi HTTP 500 của bản mới có vượt quá 5% hay không.

### Thao tác của DevOps/Admin:
- Theo dõi tiến độ cập nhật:
  ```bash
  kubectl argo rollouts get rollout sps-backend-deployment -w -n default
  ```
- Nếu bản mới hoạt động ổn định và bạn muốn mở 100% cho toàn bộ user:
  ```bash
  kubectl argo rollouts promote sps-backend-deployment -n default
  ```
- Nếu thấy Grafana báo lỗi đỏ rực, lập tức thu hồi về bản cũ:
  ```bash
  kubectl argo rollouts abort sps-backend-deployment -n default
  ```

---

## Chương 7: Chính sách Quản lý Database (No-Flyway Policy)

**Lịch sử Kiến trúc:** Từ ngày 01/06/2026, dự án chính thức chuyển dịch Data Layer sang Supabase và **tắt hoàn toàn Flyway** trong Spring Boot (`flyway.enabled: false`).

**Quy trình Cập nhật Schema:**
- Bất kỳ thay đổi DDL nào (Thêm cột, Xóa bảng, Tạo View) hoặc thay đổi RLS Policy đều **phải được thao tác trực tiếp trên giao diện Supabase Dashboard** hoặc thông qua công cụ Supabase CLI.
- Spring Boot khởi động ở chế độ `ddl-auto: validate`, nó chỉ kiểm tra xem bảng trong Supabase có khớp với Entity trong Java hay không, nếu sai lệch nó sẽ báo lỗi và tắt ứng dụng. Tuyệt đối không bật lại Flyway để tránh phá hỏng cấu trúc phân quyền (RLS) của Supabase.

---

## Chương 8: Runbook Xử lý Sự cố K8s (Troubleshooting)

### 8.1. Kiểm tra trạng thái Cluster cơ bản
- Xem toàn bộ Pods: `kubectl get pods -A`
- Xem lý do Pod bị lỗi (CrashLoopBackOff): `kubectl describe pod <pod-name> -n default`
- Xem các sự kiện hệ thống K8s (để biết tại sao không kéo được Image): `kubectl get events --sort-by='.metadata.creationTimestamp'`

### 8.2. Hệ thống Log Tập trung (Loki + Grafana)
Thay vì gõ lệnh `kubectl logs` cho từng Pod rất cực khổ, hãy dùng Grafana:
1. Truy cập: `http://monitor.sps.local` (Bật Tailscale VPN).
2. Đăng nhập với tài khoản: `admin` / `prom-operator`.
3. Chuyển sang mục **Explore** (Thanh menu bên trái).
4. Tại ô Data Source ở trên cùng, chọn **Loki**.
5. Chọn nút **Log browser** -> Mở mục `app` -> Chọn `sps-backend` hoặc `sps-mobile`.
6. Bấm **Show Logs** để xem toàn bộ log theo thời gian thực của các ứng dụng.

### 8.3. Truy cập ArgoCD
1. Truy cập `http://argocd.sps.local` (Bật Tailscale VPN).
2. Tài khoản mặc định là `admin`. Mật khẩu lấy bằng lệnh:
   ```bash
   kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
   ```

### 8.4. Cấu hình DNS Truy cập (Client Side)
Sửa file `/etc/hosts` (Mac/Linux) hoặc `C:\Windows\System32\drivers\etc\hosts` (Windows):
```text
127.0.0.1   sps.local
127.0.0.1   api.sps.local
127.0.0.1   monitor.sps.local
127.0.0.1   argocd.sps.local
```
- **Mobile App:** `http://sps.local/mobile`
- **Kiosk App:** `http://sps.local/kiosk`
- **Backend API:** `http://api.sps.local`

---
*Tài liệu được cập nhật lần cuối vào tháng 06/2026. Mọi thay đổi về hạ tầng vui lòng đối chiếu và cập nhật file này.*
