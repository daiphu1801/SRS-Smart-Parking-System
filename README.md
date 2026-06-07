# Hệ Thống Quản Lý Bãi Đỗ Xe Thông Minh (SPS) — Tài Liệu Vận Hành

Tài liệu này mô tả đầy đủ kiến trúc kỹ thuật và quy trình vận hành hệ thống Smart Parking System. Hệ thống được xây dựng theo chuẩn Enterprise với **GitOps**, **Canary Deployment**, và kiến trúc **Hybrid-Cloud**.

---

## Chương 1: Tổng Quan Kiến Trúc

Hệ thống gồm 4 ứng dụng cốt lõi:
1. **Backend (Spring Boot 3 + Java 21):** Xử lý toàn bộ logic nghiệp vụ, giao tiếp với các hệ thống phụ trợ.
2. **Mobile App (Flutter Web):** Ứng dụng dành cho khách hàng.
3. **Kiosk App (Flutter Web):** Ứng dụng cho máy quẹt thẻ tại cổng bãi đỗ.
4. **AI Desktop (Python 3):** Nhận diện biển số xe tại điểm biên (Edge Computing) bằng YOLO.

### Mô Hình Dữ Liệu Hybrid-Cloud
- **Supabase (Cloud):** Database chính (PostgreSQL) và hệ thống xác thực. Dữ liệu quản lý hoàn toàn trên Cloud.
- **Kafka & Redis (On-Premise K8s):** Xử lý Message Broker tốc độ cao (sự kiện mở cổng, nhận diện biển số) và Cache phân tán.

### Luồng Triển Khai Tự Động (GitOps)
```
Source Code → Github Actions (CI) → Docker Hub → ArgoCD → K8s Cluster
```
- **ArgoCD** liên tục giám sát Git Repository (thư mục `k8s/overlays/prod`) và tự động kéo cấu hình mới nhất về Cluster.
- **Argo Image Updater** phát hiện image mới trên DockerHub và tự động cập nhật vào ArgoCD Application.
- **Argo Rollouts** nhận Image mới, thực thi **Canary Deployment** (điều hướng 20% traffic) và đo lỗi qua **Prometheus**.

---

## Chương 2: Phát Triển Local (Chạy bằng Source Code)

Dành cho lập trình viên muốn chạy và debug hệ thống trực tiếp trên máy cá nhân.

### 2.1. Backend (Spring Boot)

**Yêu cầu:** Java 21+, Maven, IDE (IntelliJ/Eclipse).

**Cài đặt môi trường:**
1. Di chuyển vào thư mục resource:
   ```bash
   cd SRS-Smart-Parking-System/src/main/resources/
   ```
2. Copy file cấu hình mẫu:
   ```bash
   cp application-placeholder.yml application.yml
   ```
3. Mở `application.yml` và điền đầy đủ các giá trị thực vào các biến `[YOUR_...]`:

| Biến | Mô tả | Ví dụ |
|---|---|---|
| `[YOUR_REDIS_HOST_OR_IP]` | Host Redis | `localhost` hoặc IP server |
| `[YOUR_REDIS_PASSWORD]` | Mật khẩu Redis | `SieuBaoMat123` |
| `[YOUR_SUPABASE_PROJECT_ID]` | ID project Supabase | `rrmhwltgofgtvxrmfxpl` |
3. Mở `application.yml` và điền đầy đủ tất cả biến theo bảng dưới đây:

---

#### 🔴 Redis / Upstash Cache
> Hệ thống dùng Redis để cache Rate Limit, Zone, System Config. Có thể dùng Redis local hoặc **Upstash** (cloud, miễn phí).
> - **Upstash:** Tạo database tại [upstash.com](https://upstash.com) → lấy `Endpoint` và `Password`. Bật `TLS/SSL`.
> - **Local Redis:** Tắt `ssl.enabled: false`, để `host: localhost`, `password: ""`.

| Biến trong `application.yml` | Mô tả | Ví dụ |
|---|---|---|
| `spring.data.redis.host` | Host Redis hoặc Upstash endpoint | `caring-lab-12345.upstash.io` |
| `spring.data.redis.port` | Port (Upstash dùng 6379) | `6379` |
| `spring.data.redis.password` | Mật khẩu Redis / Upstash token | `AXxx...` |
| `spring.data.redis.ssl.enabled` | Bật SSL (bắt buộc với Upstash) | `true` / `false` |

---

#### 🔵 Supabase (Database + Auth)
> Tạo project tại [supabase.com](https://supabase.com). Tất cả thông tin lấy tại **Project Settings → API**.

| Biến trong `application.yml` | Mô tả | Lấy ở đâu |
|---|---|---|
| `supabase.url` | URL project Supabase | Settings → API → Project URL |
| `supabase.anon-key` | Anon/Public Key | Settings → API → anon public |
| `supabase.service-role-key` | Service Role Key (quyền admin) | Settings → API → service_role |
| `spring.datasource.url` | JDBC URL kết nối PostgreSQL | Settings → Database → Connection string (Java/JDBC) |
| `spring.datasource.username` | Username DB | `postgres.[YOUR_PROJECT_ID]` |
| `spring.datasource.password` | Mật khẩu DB | Settings → Database → Database password |
| `spring.security.oauth2...jwk-set-uri` | URL Public Key JWT Supabase | `https://[ID].supabase.co/auth/v1/.well-known/jwks.json` |

> ⚠️ `jwk-set-uri` dùng thuật toán **ES256** — đã được cấu hình sẵn trong `SecurityConfig.java`.

---

#### 🟡 Kafka (Message Broker)
> Dùng để gửi sự kiện xe vào/ra giữa AI Desktop và Backend.
> - **Local:** Cài Kafka Docker: `docker run -p 9092:9092 apache/kafka:latest`
> - **Trong K8s:** Kafka đã được deploy sẵn, dùng `host.docker.internal:9092`

| Biến trong `application.yml` | Mô tả | Ví dụ |
|---|---|---|
| `spring.kafka.bootstrap-servers` | URL Kafka Broker | `localhost:9092` hoặc `host.docker.internal:9092` |

---

#### 📱 Twilio (Gửi SMS OTP)
> Tạo tài khoản tại [twilio.com](https://twilio.com). Lấy thông tin tại Console Dashboard.

| Biến trong `application.yml` | Mô tả | Lấy ở đâu |
|---|---|---|
| `app.twilio.account-sid` | Account SID | Twilio Console → Account Info |
| `app.twilio.auth-token` | Auth Token | Twilio Console → Account Info |
| `app.twilio.messaging-service-sid` | Messaging Service SID | Messaging → Services |
| `app.twilio.from-number` | Số điện thoại gửi (nếu có) | Phone Numbers (để trống nếu dùng Messaging Service) |

---

#### 💳 Sepay (Thanh toán QR)
> Tạo tài khoản tại [sepay.vn](https://sepay.vn). Cấu hình Webhook để nhận callback thanh toán.

| Biến trong `application.yml` | Mô tả | Ví dụ |
|---|---|---|
| `app.sepay.bank-account` | Số tài khoản ngân hàng nhận tiền | `0859226688` |
| `app.sepay.bank-name` | Tên ngân hàng | `MBBank` |
| `app.sepay.account-name` | Tên chủ tài khoản | `NGUYEN VAN A` |
| `app.sepay.webhook-secret` | Secret xác thực Webhook Sepay | Sepay Dashboard → Webhook Settings |

---

#### ⚙️ Cấu hình ứng dụng

| Biến trong `application.yml` | Mô tả | Ví dụ |
|---|---|---|
| `app.cors.allowed-origins` | Danh sách domain frontend được phép gọi API (ngăn cách bằng dấu phẩy) | `http://localhost:3000,http://mobile.sps.local` |
| `app.settings.file-path` | Đường dẫn file/thư mục lưu cài đặt hệ thống | `./settings` hoặc `/app/data/settings` |
| `app.demo.admin-phone` | Số điện thoại admin dùng cho demo (nhận OTP test) | `0901234567` |
| `app.demo.otp-expiration-minutes` | Thời gian hết hạn OTP (phút) | `5` |
| `app.demo.otp-max-try` | Số lần nhập OTP tối đa | `5` |

---

**Chạy:**
```bash
mvn clean install -DskipTests
mvn spring-boot:run
```
Backend sẽ chạy tại: `http://localhost:8080`

---

### 2.2. Frontend Flutter (Mobile & Kiosk)

**Yêu cầu:** Flutter SDK (Stable channel).

**Cài đặt môi trường:**
1. Di chuyển vào thư mục ứng dụng:
   ```bash
   cd flutter_mobile_app   # hoặc cd kiosk_app
   ```
2. Copy file env mẫu:
   ```bash
   cp .env.example .env
   ```
3. Mở `.env` và điền:
   ```env
   SUPABASE_URL=https://[YOUR_SUPABASE_PROJECT_ID].supabase.co
   SUPABASE_ANON_KEY=[YOUR_SUPABASE_ANON_KEY]
   API_BASE_URL=http://localhost:8080   # Trỏ tới Backend đang chạy local
   ```

**Chạy:**
```bash
flutter clean
flutter pub get
flutter run -d chrome
```

---

### 2.3. AI Desktop (Python)

**Yêu cầu:** Python 3.10+. File model YOLO (`yolo11n.pt`, `yolo26n.pt`) nằm sẵn trong thư mục `sps_desktop/assets/`.

**Cài đặt môi trường:**
1. Di chuyển vào thư mục:
   ```bash
   cd sps_desktop
   ```
2. Copy file env mẫu:
   ```bash
   cp .env.example .env
   ```
3. Mở `.env` và điền:
   ```env
   SUPABASE_URL=https://[YOUR_SUPABASE_PROJECT_ID].supabase.co
   SUPABASE_ANON_KEY=[YOUR_SUPABASE_ANON_KEY]
   API_BASE_URL=http://localhost:8080
   ```

**Chạy:**
```bash
python -m venv venv
source venv/bin/activate      # Windows: venv\Scripts\activate
pip install -r requirements.txt
python main.py
```

---

## Chương 3: Provisioning Cluster K8s (Chạy bằng K8s)

Tài liệu này giả định bạn đã có một Kubernetes cluster (K3s, Minikube, EKS, GKE...).

### 3.1. Yêu Cầu CLI (Máy Quản Lý)

Đảm bảo máy quản lý đã cài đặt:
- `kubectl`: Giao tiếp với K8s.
- `helm`: Triển khai các package infrastructure.
- `kubeseal`: Công cụ mã hoá secret của Bitnami.
- `kubectl argo rollouts`: Tải từ [Argo Rollouts Github](https://github.com/argoproj/argo-rollouts/releases).

### 3.2. Cài Đặt Infrastructure (Chạy tuần tự bằng Helm)

#### A. Ingress & VPN
```bash
# Cài Nginx Ingress Controller
helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx -n ingress-nginx --create-namespace

# Cài Tailscale Operator (ẩn các Dashboard quản trị)
# Lấy Client ID/Secret tại https://login.tailscale.com/admin/settings/oauth
helm upgrade --install tailscale tailscale/tailscale-operator -n tailscale --create-namespace \
  --set oauth.clientId=<YOUR_TAILSCALE_CLIENT_ID> \
  --set oauth.clientSecret=<YOUR_TAILSCALE_CLIENT_SECRET>
```

#### B. Message Broker & Cache (Data Layer)
```bash
# Cài Apache Kafka
helm upgrade --install kafka oci://registry-1.docker.io/bitnamicharts/kafka -n default

# Cài Redis (mật khẩu phải khớp với cấu hình trong ConfigMap)
helm upgrade --install redis oci://registry-1.docker.io/bitnamicharts/redis -n default \
  --set auth.password=SieuBaoMat123
```

#### C. Observability Stack
Namespace `logging` là bắt buộc vì Argo Rollouts truy vấn trực tiếp Metrics từ đây.
```bash
# Kube-Prometheus-Stack (cung cấp Prometheus & Grafana)
helm upgrade --install prometheus prometheus-community/kube-prometheus-stack -n logging --create-namespace

# Loki-Stack (tổng hợp log)
helm upgrade --install loki grafana/loki-stack -n logging
```
*(Đăng nhập Grafana mặc định: `admin` / `prom-operator`)*

#### D. GitOps Ecosystem
```bash
# Cài ArgoCD
helm upgrade --install argocd argo/argo-cd -n argocd --create-namespace

# Cài Argo Rollouts Controller
helm upgrade --install argo-rollouts argo/argo-rollouts -n argo-rollouts --create-namespace

# Cài Sealed Secrets Controller
helm upgrade --install sealed-secrets sealed-secrets/sealed-secrets -n kube-system

# Cài Argo CD Image Updater
helm upgrade --install argocd-image-updater argo/argocd-image-updater -n argocd
```

---

## Chương 4: Cấu Hình K8s (Kustomize Overlays)

Dự án sử dụng kiến trúc Kustomize theo layer:
- `k8s/base/`: Định nghĩa gốc cho Deployments, Services, ConfigMaps.
- `k8s/overlays/prod/`: Override cấu hình cho môi trường Production (thêm prefix `prod-` vào tất cả tên resource).

### Biến Môi Trường Public (ConfigMap)

Chỉnh sửa `k8s/base/config.yaml` để thay đổi biến public:

| Biến | Giá trị hiện tại | Mô tả |
|---|---|---|
| `API_BASE_URL` | `http://api.sps.local` | URL Backend API (dùng cho Flutter web) |
| `SUPABASE_URL` | `https://rrmhwltgofgtvxrmfxpl.supabase.co` | URL project Supabase |
| `SUPABASE_ANON_KEY` | `sb_publishable_...` | Anon Key Supabase (public) |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `host.docker.internal:9092` | URL Kafka Broker |
| `APP_TWILIO_MESSAGING_SERVICE_SID` | `MG104...` | Twilio Messaging SID |
| `APP_SEPAY_BANK_ACCOUNT` | `0859226688` | Số tài khoản thanh toán |

> **Lưu ý:** Sau khi sửa `config.yaml`, commit và push lên Git. ArgoCD sẽ tự động sync.

### Cấu Hình DNS Local (Client Side)

Thêm vào file hosts:
- **Windows:** `C:\Windows\System32\drivers\etc\hosts`
- **Mac/Linux:** `/etc/hosts`

```text
127.0.0.1   api.sps.local
127.0.0.1   mobile.sps.local
127.0.0.1   kiosk.sps.local
127.0.0.1   ai.sps.local
127.0.0.1   monitor.sps.local
127.0.0.1   argocd.sps.local
```

Sau đó truy cập:
- **Mobile App:** `http://mobile.sps.local`
- **Kiosk App:** `http://kiosk.sps.local`
- **Backend API:** `http://api.sps.local`
- **Grafana:** `http://monitor.sps.local` _(cần bật Tailscale VPN)_
- **ArgoCD:** `http://argocd.sps.local` _(cần bật Tailscale VPN)_

---

## Chương 5: Quản Lý Mật Khẩu & Bảo Mật (Sealed Secrets)

> ⚠️ **NGUYÊN TẮC VÀNG:** TUYỆT ĐỐI KHÔNG PUSH FILE SECRET (kể cả base64) LÊN GITHUB!

Hệ thống dùng **Sealed Secrets** để mã hoá bất đối xứng. KubeSeal mã hoá bằng Public Key, chỉ Controller trong K8s (giữ Private Key) mới giải mã được.

### Cách Cập Nhật Mật Khẩu / API Key Mới:

1. Tạo file `secret.yaml` **(KHÔNG push file này lên Git)**:
   ```yaml
   apiVersion: v1
   kind: Secret
   metadata:
     name: sps-backend-secrets
     namespace: default
   type: Opaque
   stringData:
     SPRING_DATASOURCE_PASSWORD: "Mat_Khau_Moi"
     SUPABASE_JWT_SECRET: "Secret_Moi"
     APP_TWILIO_AUTH_TOKEN: "Token_Moi"
     APP_SEPAY_WEBHOOK_SECRET: "Webhook_Secret_Moi"
   ```

2. Mã hoá bằng `kubeseal`:
   ```bash
   kubeseal -o yaml < secret.yaml > k8s/base/sps-sealed-secret.yaml
   ```

3. File `sps-sealed-secret.yaml` đã được mã hoá an toàn. Commit và push lên repo.

4. ArgoCD phát hiện thay đổi, sync xuống K8s; Controller K8s giải mã thành Secret thật cho Pod sử dụng.

---

## Chương 6: GitOps Lifecycle & Canary Deployment

Khi Developer push code lên nhánh `main`:

1. **Github Actions (CI)** build, chạy Linting (Fast-Fail), đóng gói Docker Image với 2 tag:
   - Tag SHA: `ichbineinhund/<app>:<git-sha>` — định danh chính xác phiên bản
   - Tag latest: `ichbineinhund/<app>:latest` — để Image Updater tracking

2. **Argo CD Image Updater** phát hiện tag `latest` thay đổi → tự động cập nhật `spec.source.kustomize.images` trong ArgoCD Application.

3. **ArgoCD** sync → K8s cập nhật Deployment/Rollout.

4. **Argo Rollouts** can thiệp vào quá trình này:
   - KHÔNG ngay lập tức xoá phiên bản cũ.
   - Khởi động 1 Pod phiên bản mới, điều hướng **20% người dùng** vào phiên bản mới (Canary).
   - Truy vấn **Prometheus** (`analysis-template.yaml`) — nếu tỷ lệ lỗi HTTP 500 vượt 5% trong 1 phút → tự động Rollback.

### Lệnh DevOps/Admin:
```bash
# Theo dõi tiến trình rollout
kubectl argo rollouts get rollout prod-sps-backend-deployment -w -n default

# Nếu phiên bản mới ổn định, mở 100% traffic cho tất cả người dùng
kubectl argo rollouts promote prod-sps-backend-deployment -n default

# Nếu phát hiện lỗi trên Grafana, rollback ngay lập tức
kubectl argo rollouts abort prod-sps-backend-deployment -n default
```

---

## Chương 7: Chính Sách Quản Lý Database (No-Flyway Policy)

**Lịch sử kiến trúc:** Kể từ ngày 1/6/2026, dự án chính thức chuyển Data Layer sang Supabase và **tắt hoàn toàn Flyway** trong Spring Boot (`flyway.enabled: false`).

**Quy trình cập nhật Schema:**
- Mọi thay đổi DDL (thêm cột, xoá bảng, tạo View) hoặc thay đổi RLS Policy **phải thực hiện trực tiếp trên Supabase Dashboard** hoặc qua Supabase CLI.
- Spring Boot khởi động ở chế độ `ddl-auto: validate` — chỉ kiểm tra xem bảng trong Supabase có khớp với Java Entity không. Nếu không khớp → throw lỗi và tắt.
- **Tuyệt đối không bật lại Flyway** để tránh phá vỡ Row Level Security (RLS) của Supabase.

---

## Chương 8: Xử Lý Sự Cố K8s (Troubleshooting Runbook)

### 8.1. Kiểm Tra Cơ Bản
```bash
# Xem toàn bộ Pod
kubectl get pods -A

# Xem lý do Pod lỗi (CrashLoopBackOff, ImagePullBackOff)
kubectl describe pod <tên-pod> -n default

# Xem image mà mỗi Deployment đang dùng
kubectl get deployments -o custom-columns="NAME:.metadata.name,IMAGE:.spec.template.spec.containers[0].image"

# Xem sự kiện hệ thống K8s (lý do không pull được Image)
kubectl get events --sort-by='.metadata.creationTimestamp'

# Kiểm tra Argo CD Image Updater có lỗi gì không
kubectl logs -n argocd -l app.kubernetes.io/name=argocd-image-updater --tail=20
```

### 8.2. Hệ Thống Log Tập Trung (Loki + Grafana)

Thay vì gõ `kubectl logs` cho từng Pod, dùng Grafana:
1. Truy cập: `http://monitor.sps.local` (bật Tailscale VPN).
2. Đăng nhập: `admin` / `prom-operator`.
3. Chuyển sang phần **Explore** (thanh menu trái).
4. Ô Data Source chọn **Loki**.
5. Click **Log browser** → Mở `app` → Chọn `sps-backend` hoặc `sps-mobile`.
6. Click **Show Logs** để xem log realtime.

### 8.3. Truy Cập ArgoCD Dashboard
1. Truy cập `http://argocd.sps.local` (bật Tailscale VPN).
2. Tài khoản mặc định là `admin`. Lấy mật khẩu:
   ```bash
   kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
   ```

### 8.4. Debug Canary Rollout Backend
```bash
# Xem chi tiết trạng thái rollout
kubectl argo rollouts get rollout prod-sps-backend-deployment -n default

# Xem log Argo Rollouts Controller
kubectl logs -n argo-rollouts -l app.kubernetes.io/name=argo-rollouts --tail=50
```

---
