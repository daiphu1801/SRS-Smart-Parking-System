# SRS Smart Parking System

## Overview
The SRS (Smart Parking System) is a comprehensive parking management solution designed with a service-oriented architecture (Microservices/Monorepo). The system integrates Artificial Intelligence for Automatic License Plate Recognition (ALPR), combined with a centralized management backend and cross-platform client applications.

## Architecture
The system consists of 04 independent core modules:

1. **SRS-Smart-Parking-System (Core Backend)**
   - Language & Framework: Java, Spring Boot.
   - Functionality: Processes core business logic, manages relational databases (PostgreSQL), integrates in-memory caching (Redis), handles security authentication, and orchestrates system transactions.

2. **sps_desktop (AI Computer Vision Module)**
   - Language & Framework: Python, Flet, OpenCV, YOLO.
   - Functionality: Processes real-time video streams (RTSP), extracts and analyzes license plate data using Deep Learning models.

3. **kiosk_app (Kiosk Web Terminal)**
   - Language & Framework: Dart, Flutter Web.
   - Functionality: A touch-enabled control terminal for security personnel, communicating directly with the Backend API to control barriers and manage vehicle traffic.

4. **flutter_mobile_app (Customer Application)**
   - Language & Framework: Dart, Flutter (Cross-platform).
   - Functionality: An interactive portal for customers, supporting parking lot discovery, vehicle management, and digital payments.

## System Requirements
- Docker Engine & Docker Compose (Latest version recommended).
- Docker Desktop (WSL 2 integration required if deploying on Windows).
- Git Version Control.

## Environment Configuration
To ensure information security, sensitive configuration parameters (Credentials, API Keys) are managed via static environment variables. The setup process requires generating actual configuration files from provided templates (Placeholder files).

**1. Backend Module (Java)**
- Path: `SRS-Smart-Parking-System/src/main/resources/`
- Action: Duplicate the `application-placeholder.yml` file and rename it to `application.yml`. Assign authentication values for the database and network infrastructure to variables marked with `[YOUR_...]`.

**2. Client Modules (Python & Flutter)**
- Path: Root directories of `sps_desktop`, `kiosk_app`, and `flutter_mobile_app`.
- Action: Duplicate the `.env.example` file and rename it to `.env`. Configure the Supabase authentication keys and the Backend Endpoint (`API_BASE_URL`).

## Deployment Instructions
Each module has been configured for containerization via a `Dockerfile`. Initiate the deployment process by sequentially executing the following scripts in their respective directories:

### 1. Deploy Backend Core
```bash
cd SRS-Smart-Parking-System
docker build -t srs-backend:latest .
docker run -d -p 8080:8080 --name sps-backend srs-backend:latest
```

### 2. Deploy AI Computer Vision Module
```bash
cd sps_desktop
docker build -t sps-desktop-app:latest .
docker run -d -p 8550:8550 -e API_BASE_URL=http://host.docker.internal:8080/api/v1 --name sps-desktop-container sps-desktop-app:latest
```

### 3. Deploy Kiosk Web Terminal
```bash
cd kiosk_app
docker build -t srs-kiosk-app:latest .
docker run -d -p 8081:80 --name sps-kiosk-container srs-kiosk-app:latest
```

### 4. Deploy Customer Application (PWA)
```bash
cd flutter_mobile_app
docker build -t srs-mobile-app:latest .
docker run -d -p 8082:80 --name sps-mobile-container srs-mobile-app:latest
```

## Network Topology & Ports

| Service Name | Platform | TCP Port | Deployment Endpoint (Local) |
|--------------|----------|----------|-----------------------------|
| Backend API | Spring Boot | 8080 | http://localhost:8080/api/v1 |
| Kiosk Terminal | Flutter Web | 8081 | http://localhost:8081 |
| Customer Application | Flutter Web | 8082 | http://localhost:8082 |
| AI Camera Dashboard | Python Flet | 8550 | http://localhost:8550 |

*Note: In a Production environment (Kubernetes/Cloud), environment variable configuration and routing will be independently managed by the Ingress Controller and Secret Manager.*
