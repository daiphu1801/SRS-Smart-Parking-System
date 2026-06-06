# 🚗 Smart Parking System (SPS)

Hệ thống quản lý bãi đỗ xe thông minh, tích hợp nhận diện biển số xe bằng AI, thanh toán tự động qua SePay, xác thực OTP qua Supabase + Twilio, và vận hành trên Kubernetes với GitOps (Argo CD).

---

## 📐 Kiến trúc tổng quan

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                          Kubernetes Cluster (local / cloud)                  │
│                                                                              │
│  ┌─────────────┐   ┌─────────────┐   ┌─────────────┐   ┌─────────────────┐ │
│  │ Flutter     │   │ Flutter     │   │ AI Desktop  │   │  Spring Boot    │ │
│  │ Mobile Web  │   │ Kiosk Web   │   │ (Python)    │   │  Backend        │ │
│  │ :80         │   │ :80         │   │ :8550       │   │  :8080          │ │
│  └──────┬──────┘   └──────┬──────┘   └──────┬──────┘   └────────┬────────┘ │
│         │                 │                  │                   │           │
│         └─────────────────┴──────────────────┴───────────────────┘           │
│                                    │                                          │
│                          ┌─────────▼──────────┐                              │
│                          │   Nginx Ingress     │                              │
│                          │  mobile.sps.local   │                              │
│                          │  kiosk.sps.local    │                              │
│                          │  ai.sps.local       │                              │
│                          │  api.sps.local      │                              │
│                          └────────────────────┘                              │
└──────────────────────────────────────────────────────────────────────────────┘
         │                            │
         ▼                            ▼
  ☁️ Supabase (Auth)         ☁️ Upstash Redis (Rate Limit)
  ☁️ PostgreSQL (DB)         ☁️ Twilio (OTP SMS)
  ☁️ SePay (Payment)         ☁️ Kafka (Events)
```

---

## 📦 Các thành phần

| Thư mục | Công nghệ | Truy cập (local) | Mô tả |
|---|---|---|---|
| `SRS-Smart-Parking-System/` | Spring Boot 3, Java 21 | `api.sps.local` | Backend REST API chính |
| `flutter_mobile_app/` | Flutter Web | `mobile.sps.local` | App di động cho người dùng |
| `kiosk_app/` | Flutter Web | `kiosk.sps.local` | Màn hình kiosk tại bãi xe |
| `sps_desktop/` | Python (AI + GUI) | `ai.sps.local` | Nhận diện biển số xe, quản lý barrier |
| `k8s/` | Kubernetes + Kustomize | — | Toàn bộ manifest triển khai |

---

## ⚙️ Cấu hình môi trường

### ConfigMap (`k8s/base/config.yaml`)
Chứa các config **không nhạy cảm**, dùng chung cho backend, mobile, kiosk:

```yaml
API_BASE_URL: "http://api.sps.local"
SUPABASE_URL: "https://<project>.supabase.co"
SUPABASE_ANON_KEY: "sb_publishable_..."
SPRING_KAFKA_BOOTSTRAP_SERVERS: "..."
APP_SEPAY_BANK_ACCOUNT: "..."
```

### Secret (`sps-secret`)
Chứa các credentials **nhạy cảm**, chỉ mount vào backend pod:

```
DB_URL, DB_USERNAME, DB_PASSWORD
SUPABASE_SERVICE_ROLE_KEY, SUPABASE_JWT_URI
```

> ⚠️ **Không commit secret vào git.** Tạo thủ công bằng `kubectl create secret`.

### Desktop AI (`k8s/base/desktop.yaml`)
Desktop khai báo config **trực tiếp trong deployment** (độc lập với ConfigMap):
```yaml
env:
  - name: API_BASE_URL
    value: "http://api.sps.local/api/v1"
  - name: SUPABASE_URL
    value: "..."
  - name: SUPABASE_ANON_KEY
    value: "..."
```

---

## 🏗️ Kustomize Overlays

```
k8s/
├── base/               # Config chung cho mọi môi trường
│   ├── config.yaml     # ConfigMap
│   ├── backend.yaml
│   ├── mobile.yaml
│   ├── kiosk.yaml
│   ├── desktop.yaml
│   └── ingress.yaml
└── overlays/
    └── prod/           # Override cho production
        ├── kustomization.yaml   # namePrefix: prod-
        └── config-patch.yaml   # Override API_BASE_URL nếu cần
```

Kustomize tự động thêm prefix `prod-` vào tất cả resource names khi deploy production.

---

## 🚀 CI/CD Pipeline

Mỗi component có workflow riêng trong `.github/workflows/`:

| Workflow | Trigger | Tác vụ |
|---|---|---|
| `ci-backend.yml` | Push vào `SRS-Smart-Parking-System/**` | Build JAR → Docker image → Push Docker Hub → Argo CD sync |
| `ci-mobile.yml` | Push vào `flutter_mobile_app/**` | `flutter build web` → Docker image → Push → Argo CD sync |
| `ci-kiosk.yml` | Push vào `kiosk_app/**` | `flutter build web` → Docker image → Push → Argo CD sync |
| `ci-desktop.yml` | Push vào `sps_desktop/**` | Docker image → Push → Argo CD sync |

**Argo CD** tự động sync khi phát hiện image mới trên Docker Hub.

---

## 🌐 Flutter Web — Runtime Config Injection

Flutter build web tạo file tĩnh. Config được inject **lúc container khởi động** qua `entrypoint.sh`:

**Mobile & Kiosk:**
```sh
# Inject API_BASE_URL vào compiled JS
find /usr/share/nginx/html -name "*.js" \
  -exec sed -i "s|__API_BASE_URL_PLACEHOLDER__|${API_BASE_URL}|g" {} +

# Ghi SUPABASE credentials vào assets/.env cho flutter_dotenv đọc
echo "SUPABASE_URL=${SUPABASE_URL}" > /usr/share/nginx/html/assets/.env
echo "SUPABASE_ANON_KEY=${SUPABASE_ANON_KEY}" >> /usr/share/nginx/html/assets/.env
```

---

## 🔒 Rate Limiting

Backend dùng **Redisson + Upstash Redis** để giới hạn request cho auth endpoints:

- `3 requests / 60 giây` cho `/api/v1/auth/send-otp` và `/api/v1/auth/login`, theo từng IP
- Key tự động **xóa sau 60 giây** (TTL) → IP bị block sẽ tự unblock sau đúng 1 window
- Fallback về in-memory Bucket4j nếu Upstash không khả dụng

---

## 🖥️ Chạy local (Development)

### Yêu cầu
- Docker Desktop with Kubernetes enabled
- `kubectl`, `kustomize`
- Argo CD (optional, có thể apply thủ công)
- Thêm vào `hosts` file:
```
127.0.0.1  api.sps.local mobile.sps.local kiosk.sps.local ai.sps.local
```

### Deploy
```bash
# Tạo secret trước
kubectl create secret generic sps-secret \
  --from-literal=DB_URL=... \
  --from-literal=DB_USERNAME=... \
  --from-literal=DB_PASSWORD=... \
  --from-literal=SUPABASE_SERVICE_ROLE_KEY=... \
  --from-literal=SUPABASE_JWT_URI=...

# Apply toàn bộ (production overlay)
kubectl apply -k k8s/overlays/prod/

# Kiểm tra pods
kubectl get pods
```

### Truy cập
| URL | Service |
|---|---|
| `http://mobile.sps.local` | Mobile App |
| `http://kiosk.sps.local` | Kiosk App |
| `http://ai.sps.local` | Desktop AI Portal |
| `http://api.sps.local/swagger-ui.html` | API Docs |

---

## 🗂️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3, Java 21, Spring Security, JPA/Hibernate |
| Database | PostgreSQL (via Supabase), Flyway migrations |
| Auth | Supabase JWT, Twilio OTP SMS |
| Cache / Rate Limit | Upstash Redis, Redisson, Bucket4j |
| Events | Apache Kafka |
| Payment | SePay webhook |
| Mobile / Kiosk | Flutter Web, flutter_dotenv, supabase_flutter |
| Desktop AI | Python, YOLO (biển số), OpenCV |
| Infrastructure | Kubernetes, Nginx Ingress, Kustomize, Argo CD |
| CI | GitHub Actions, Docker Hub |
