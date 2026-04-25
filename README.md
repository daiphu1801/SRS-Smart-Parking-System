# Smart Parking System — Monorepo 🚗

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)
![Flutter](https://img.shields.io/badge/Flutter-3.x-blue.svg)
![Python](https://img.shields.io/badge/Python-3.11-yellow.svg)

🌐 **Looking for the Vietnamese version?** Check out [README_vi.md](./README_vi.md).

## 📖 Project Overview
The Smart Parking Management System is a modern, enterprise-grade solution designed to completely automate parking lot operations using Artificial Intelligence (AI). Moving away from traditional ticketing and physical RFID cards, this system utilizes high-speed License Plate Recognition (LPR) technology integrated directly with barrier gates to provide a seamless, hands-free parking experience.

## ✨ Core Features
- **Automated Entrance & Exit:** AI automatically extracts license plates to open barriers in less than 2 seconds without requiring physical tickets.
- **Dynamic Pricing & Monthly Subscriptions:** Comprehensive pricing rules accommodating hourly, daily, and monthly VIP subscriptions across different vehicle types.
- **Real-Time Guard Interventions:** WebSockets instantly push violation alerts, capacity warnings, and anomaly notifications to the security guard's dashboard.
- **Unified Management Eco-system:** A single monolith that handles AI analytics at the edge, heavy business logic in the cloud, and sleek end-user experiences via a mobile app.

## 🔄 Operational Workflow (User Journey)
1. **Vehicle Arrival:** A vehicle approaches the entrance. The IP camera captures the frame.
2. **AI Inference:** The `python_edge_desktop` app runs YOLOv8 (vehicle detection) and EasyOCR (plate extraction) directly on the local edge computer.
3. **Logic Validation:** The result is sent via Kafka to the `java_spring_core` backend. The server validates if the vehicle is blacklisted or has an active subscription.
4. **Physical Actuation:** If valid, an HTTP command triggers the GPIO relays to lift the barrier gate, and an entry parking session is recorded in PostgreSQL.
5. **Customer Management:** The vehicle owner checks their session via the `flutter_mobile_app`, where they can view pricing, register monthly packages, or pay via QR code.
6. **Departure & Billing:** The exit camera scans the plate again. The server calculates the exact duration and deducts the fee from their wallet / expects a QR scan. The exit barrier automatically opens once payment clears.

---

## 📦 Repository Structure
This project is structured as a **Monorepo** consisting of three main environments:

```text
smart-parking-monorepo/
├── 📂 flutter_mobile_app/       # Customer App + Guard Patrol App (Flutter)
├── 📂 python_edge_desktop/      # AI Engine + Desktop UI (Python Flet)
└── 📂 java_spring_core/         # Backend API + Business Logic (Spring Boot)
```

---

## 🔑 System Architecture

```text
Mobile/Desktop ──HTTP──► Spring Boot ──Kafka──► Python Edge
                               │                      │
                           PostgreSQL              GPIO/Camera
```

- **Event-Driven:** Python AI generates parking events → Kafka → Spring Boot processes billing → Sends gate open commands.
- **WebSocket:** Real-time violation and alert broadcasting to Guard Apps.
- **Hot-reload Config:** `settings.json` allows operational parameter adjustments without restarting edge services.

---

## ⚙️ Detailed Installation Guide

### Prerequisites
Make sure you have the following installed on your machine before starting:
- **Java 21** (or compatible)
- **Python 3.11+**
- **Flutter SDK 3.x**
- **PostgreSQL** (Running on port `5432`)
- **Apache Kafka** (Zookeeper on `2181`, Kafka broker on `9092`)

### 1. Database Setup
1. Open PostgreSQL (via `psql` or pgAdmin).
2. Create a database for the project:
   ```sql
   CREATE DATABASE smart_parking_db;
   ```
3. Spring Boot will automatically run Hibernate auto-DDL to create the required tables upon the first launch.

### 2. Backend Initialization: Java Spring Core
1. Navigate to the `java_spring_core` folder:
   ```bash
   cd java_spring_core
   ```
2. Update database credentials and Kafka properties in `src/main/resources/application.properties` or `application.yml` if necessary.
3. Build and launch the application using Maven or Gradle:
   ```bash
   ./mvnw spring-boot:run
   ```
   *(The server will typically start on `http://localhost:8080`)*

### 3. AI Engine & Desktop UI: Python Flet
1. Navigate to the `python_edge_desktop` directory:
   ```bash
   cd python_edge_desktop
   ```
2. Create and activate a Python Virtual Environment:
   ```bash
   # Windows
   python -m venv .venv
   .venv\Scripts\activate
   
   # macOS/Linux
   python3 -m venv .venv
   source .venv/bin/activate
   ```
3. Install the dependencies:
   ```bash
   pip install -r requirements.txt
   ```
4. Run the Python Edge Application:
   ```bash
   flet run main_desktop.py
   ```
   *Note: Edit `main_desktop.py` to change the simulated role (Admin/Manager/Guard).*

### 4. Setup Mobile App: Flutter
1. Navigate to the `flutter_mobile_app` folder:
   ```bash
   cd flutter_mobile_app
   ```
2. Fetch required dependencies:
   ```bash
   flutter pub get
   ```
3. Run the application on an emulator or a connected physical device:
   ```bash
   flutter run
   ```

---

## 🎨 Design System

The entire UI strictly adheres to a minimalist 2-color palette:
- **Primary:** `#052e16` (Dark Green)
- **Background:** `#ffffff` (White)
- **Font:** Inter (Regular 400, Medium 500, Semibold 600) — *No extra bold fonts.*
- **Rules:** No Gradients | No Shadows | Uniform rounded corners (8px/6px/4px)
