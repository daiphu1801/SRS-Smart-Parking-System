# Sử dụng base image Python 3.10 mỏng nhẹ
FROM python:3.10-slim

# Ngăn Python sinh ra các file .pyc thừa thãi và cho phép in log trực tiếp ra màn hình
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

WORKDIR /app

# BƯỚC 1: Khắc phục Bức tường số 2 (Cài đặt các thư viện lõi hệ thống C++ cho OpenCV)
RUN apt-get update && apt-get install -y \
    libgl1 \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

# Sao chép danh sách thư viện
COPY requirements.txt .

# BƯỚC 2: Khắc phục Bức tường số 3 (Cài đặt PyTorch bản CPU)
# Mẹo: Cài riêng bản CPU trước để tránh việc pip tự động tải nhầm bản GPU (CUDA) nặng tới 3-4 Gigabyte!
RUN pip install --no-cache-dir torch torchvision --index-url https://download.pytorch.org/whl/cpu

# BƯỚC 3: Cài đặt các thư viện còn lại (Flet, OpenCV, YOLO...)
RUN pip install --upgrade pip && pip install --no-cache-dir -r requirements.txt

# Sao chép toàn bộ mã nguồn
COPY . .

# BƯỚC 4: Khắc phục Bức tường số 1 (Mở cổng 8550 cho giao diện Web Mode)
EXPOSE 8550

# Khởi động ứng dụng
CMD ["python", "main.py"]
