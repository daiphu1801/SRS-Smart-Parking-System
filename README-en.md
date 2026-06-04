# Smart Parking System Operations Manual (Enterprise Edition)

This document provides comprehensive technical knowledge and operational procedures for the Smart Parking System. The system is designed to Enterprise standards, utilizing **GitOps**, **Canary Deployment**, and a **Hybrid-Cloud** architecture.

---

## Chapter 1: Architecture Overview

The system consists of 4 core applications:
1. **Backend (Spring Boot 3 + Java 21):** Handles core business logic and communicates with auxiliary systems.
2. **Mobile App (Flutter Web):** Application for customers.
3. **Kiosk App (Flutter Web):** Application for card swiping machines at the parking gates.
4. **AI Desktop (Python 3):** Handles license plate recognition at the edge (Edge Computing).

### Hybrid-Cloud Data Model
- **Supabase (Cloud):** Serves as the primary database (PostgreSQL) and Authentication system. Data is fully managed on the Cloud, reducing the load on K8s.
- **Kafka & Redis (On-Premise K8s):** Handles high-speed Message Brokering (gate open events, plate recognition) and distributed Caching.

### Automated Deployment Flow (GitOps)
- Source Code -> Github Actions (CI) -> Docker Hub.
- **ArgoCD** continuously monitors the Git Repository (`k8s/base` folder) and automatically pulls the latest configuration to the Cluster.
- **Argo Rollouts** receives the new Image, executes **Canary Deployment** (directing 20% traffic), and measures error rates via **Prometheus**.

---

## Chapter 2: Local Development

For developers who want to run and debug the system on their personal machines.

### 2.1. Backend (Spring Boot)
- **Requirements:** Java 21+, Maven, IDE (IntelliJ/Eclipse).
- **Environment Setup:**
  1. Navigate to: `cd SRS-Smart-Parking-System/src/main/resources/`
  2. Copy `application-placeholder.yml` to `application.yml`.
  3. Open `application.yml` and replace all `[YOUR_...]` variables:
     - Enter connection strings for **PostgreSQL** and **Redis**.
     - Enter the **Kafka Broker** URL.
     - Enter integration Keys for **Supabase** (URL, Anon Key, JWT Secret), **Twilio** (SMS), and **Sepay** (Payment).
     *(Note: Spring Boot uses Relaxed Binding, automatically mapping the Yaml structure).*
- **Run:**
  ```bash
  mvn clean install -DskipTests
  mvn spring-boot:run
  ```

### 2.2. Frontend (Flutter Mobile & Kiosk)
- **Requirements:** Flutter SDK (Stable).
- **Environment Setup:**
  1. Navigate to the `flutter_mobile_app` or `kiosk_app` directory.
  2. Copy the `.env.example` file to `.env`.
  3. Open `.env` and set `API_BASE_URL` to point to the Backend (e.g., `http://localhost:8080/api/v1`).
- **Run:**
  ```bash
  flutter clean
  flutter pub get
  flutter run -d chrome
  ```

### 2.3. AI Desktop (Python)
- **Requirements:** Python 3.10+.
- **Run:**
  ```bash
  cd sps_desktop
  python -m venv venv
  source venv/bin/activate
  pip install -r requirements.txt
  python main.py
  ```

---

## Chapter 3: Bare Metal K8s Cluster Provisioning

This document assumes you already have a Kubernetes cluster (EKS, GKE, K3s, Minikube).

### 3.1. CLI Tooling Requirements (Local Machine)
Ensure your management machine has the following installed:
- `kubectl`: Communicate with K8s.
- `helm`: Deploy infrastructure packages.
- `kubeseal`: Security encryption tool from Bitnami.
- `kubectl argo rollouts`: Install from [Argo Rollouts Github](https://github.com/argoproj/argo-rollouts/releases).

### 3.2. Infrastructure Setup (Must run sequentially using Helm)

#### A. Ingress & VPN
```bash
# Install Nginx Ingress Controller
helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx -n ingress-nginx --create-namespace

# Install Tailscale Operator (Hides Management Dashboards)
# Get Client ID/Secret from https://login.tailscale.com/admin/settings/oauth
helm upgrade --install tailscale tailscale/tailscale-operator -n tailscale --create-namespace \
  --set oauth.clientId=<YOUR_TAILSCALE_CLIENT_ID> \
  --set oauth.clientSecret=<YOUR_TAILSCALE_CLIENT_SECRET>
```

#### B. Message Broker & Cache (Data Layer)
```bash
# Install Apache Kafka
helm upgrade --install kafka oci://registry-1.docker.io/bitnamicharts/kafka -n default

# Install Redis (Ensure password matches Configuration)
helm upgrade --install redis oci://registry-1.docker.io/bitnamicharts/redis -n default --set auth.password=SieuBaoMat123
```

#### C. Observability Stack
The `logging` namespace is mandatory because Argo Rollouts directly queries it for Metrics.
```bash
# Kube-Prometheus-Stack (Provides Prometheus & Grafana)
helm upgrade --install prometheus prometheus-community/kube-prometheus-stack -n logging --create-namespace

# Loki-Stack (Log Aggregation)
helm upgrade --install loki grafana/loki-stack -n logging
```
*(Default Grafana login: `admin` / `prom-operator`)*

#### D. GitOps Ecosystem
```bash
# Install ArgoCD
helm upgrade --install argocd argo/argo-cd -n argocd --create-namespace

# Install Argo Rollouts Controller
helm upgrade --install argo-rollouts argo/argo-rollouts -n argo-rollouts --create-namespace

# Install Sealed Secrets Controller
helm upgrade --install sealed-secrets sealed-secrets/sealed-secrets -n kube-system
```

---

## Chapter 4: K8s Configuration Management (Kustomize Overlays)

The project uses a layered Kustomize architecture:
- `k8s/base/`: Contains standard definitions for root Deployments, Services, and ConfigMaps.
- `k8s/overlays/dev/`: Overrides configurations for the Dev environment.
- `k8s/overlays/prod/`: Overrides configurations for the Prod environment.

To change Public environment variables (e.g., URL, Port):
1. Open `k8s/base/config.yaml`.
2. Change the corresponding values (Ensure compliance with Spring Boot's Relaxed Binding mechanism, e.g., `APP_TWILIO_MESSAGING_SERVICE_SID`).
3. (Optional) You can further override using `config-patch.yaml` located in the overlays folders.

---

## Chapter 5: Password & Security Management (Sealed Secrets)

**Golden Rule:** NEVER PUSH RAW TEXT (OR BASE64) SECRET FILES TO GITHUB!

The system uses **Sealed Secrets** for asymmetric encryption. KubeSeal encrypts the password with a Public Key, and only the Controller deep inside K8s (holding the Private Key) can decrypt it.

### How to update new Passwords / API Keys:
1. Create a file named `secret.yaml` (DO NOT push this file to Git) with the following format:
   ```yaml
   apiVersion: v1
   kind: Secret
   metadata:
     name: sps-backend-secrets
     namespace: default
   type: Opaque
   stringData:
     SPRING_DATASOURCE_PASSWORD: "New_Supabase_Password"
     SUPABASE_JWT_SECRET: "New_Secret"
     APP_TWILIO_AUTH_TOKEN: "New_Token"
   ```
2. Encrypt the file using the `kubeseal` command:
   ```bash
   kubeseal -o yaml < secret.yaml > k8s/base/sps-sealed-secret.yaml
   ```
3. Now, `sps-sealed-secret.yaml` has become a securely encrypted string. `git push` this file to the repository.
4. ArgoCD will detect the change and sync it down to K8s; the K8s Controller will silently decrypt it into a real Secret for Pods to use.

---

## Chapter 6: GitOps Lifecycle & Canary Deployment

When a Developer finishes coding and pushes to the `main` branch:
1. **Github Actions (CI)** builds, runs Linting (Fast-Fail), and packages a new Docker Image (Tagged with Git SHA).
2. It updates this new tag in the Kustomize files.
3. **ArgoCD** detects the Image change in Kustomize and immediately orders K8s to update.
4. **Argo Rollouts** intervenes in this process:
   - It DOES NOT immediately tear down the old version.
   - Instead, it spins up 1 new version Pod and routes **20% of users** to this new version (Canary).
   - Rollouts queries **Prometheus** (`analysis-template.yaml`) to check if the HTTP 500 error rate of the new version over the past minute exceeds 5%.

### DevOps/Admin Operations:
- Monitor rollout progress:
  ```bash
  kubectl argo rollouts get rollout sps-backend-deployment -w -n default
  ```
- If the new version is stable and you want to open 100% to all users:
  ```bash
  kubectl argo rollouts promote sps-backend-deployment -n default
  ```
- If Grafana shows red errors, immediately rollback to the old version:
  ```bash
  kubectl argo rollouts abort sps-backend-deployment -n default
  ```

---

## Chapter 7: Database Management Policy (No-Flyway Policy)

**Architecture History:** Since June 1, 2026, the project officially migrated its Data Layer to Supabase and **completely disabled Flyway** in Spring Boot (`flyway.enabled: false`).

**Schema Update Process:**
- Any DDL changes (Add column, Drop table, Create View) or RLS Policy changes **must be manipulated directly on the Supabase Dashboard** or via the Supabase CLI.
- Spring Boot starts in `ddl-auto: validate` mode; it only checks if tables in Supabase match the Java Entities. If there is a mismatch, it throws an error and shuts down. Never re-enable Flyway to avoid breaking Supabase's Row Level Security (RLS) policies.

---

## Chapter 8: K8s Troubleshooting Runbook

### 8.1. Basic Cluster Status Check
- View all Pods: `kubectl get pods -A`
- Find out why a Pod failed (CrashLoopBackOff): `kubectl describe pod <pod-name> -n default`
- View K8s system events (to know why Images aren't pulling): `kubectl get events --sort-by='.metadata.creationTimestamp'`

### 8.2. Centralized Logging System (Loki + Grafana)
Instead of painfully typing `kubectl logs` for each Pod, use Grafana:
1. Access: `http://monitor.sps.local` (Enable Tailscale VPN).
2. Login with credentials: `admin` / `prom-operator`.
3. Switch to the **Explore** section (Left menu bar).
4. In the Data Source box at the top, select **Loki**.
5. Click **Log browser** -> Open `app` -> Select `sps-backend` or `sps-mobile`.
6. Click **Show Logs** to view real-time logs for the applications.

### 8.3. Accessing ArgoCD
1. Access `http://argocd.sps.local` (Enable Tailscale VPN).
2. The default account is `admin`. Retrieve the password using:
   ```bash
   kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
   ```

### 8.4. Access DNS Configuration (Client Side)
Modify the `/etc/hosts` file (Mac/Linux) or `C:\Windows\System32\drivers\etc\hosts` (Windows):
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
*Document last updated in June 2026. For any infrastructure changes, please cross-reference and update this file.*
