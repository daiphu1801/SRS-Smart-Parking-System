# ==========================================
# BƯỚC 1: XÂY DỰNG MÃ NGUỒN (BUILD STAGE)
# ==========================================
# Sử dụng Ubuntu làm nền tảng để tải Flutter
FROM ubuntu:20.04 AS build-env

# Tránh các câu hỏi tương tác trong quá trình cài đặt (ví dụ: chọn múi giờ)
ENV DEBIAN_FRONTEND=noninteractive

# Cài đặt các công cụ lõi của Linux cần thiết cho Flutter
RUN apt-get update && \
    apt-get install -y curl git unzip xz-utils zip libglu1-mesa && \
    rm -rf /var/lib/apt/lists/*

# Tải Flutter SDK (Bản ổn định - Stable) trực tiếp từ Google
RUN git clone https://github.com/flutter/flutter.git /usr/local/flutter
ENV PATH="/usr/local/flutter/bin:/usr/local/flutter/bin/cache/dart-sdk/bin:${PATH}"
RUN flutter channel stable && flutter upgrade

# Tạo thư mục làm việc
WORKDIR /app

# Sao chép mã nguồn của sếp vào thùng
COPY . .

# Tải các thư viện (package) và tiến hành biên dịch (Build) ra Web
RUN flutter pub get
# Tạo file .env rỗng để tránh lỗi missing asset khi build
RUN touch .env
# Biên dịch sang bản Web với biến môi trường giả (Placeholder)
RUN flutter build web --release --dart-define=API_BASE_URL=__API_BASE_URL_PLACEHOLDER__

# ==========================================
# BƯỚC 2: ĐÓNG GÓI CHẠY THẬT (DEPLOY STAGE)
# ==========================================
# Sang bước này, ta ném bỏ cái đống Ubuntu và Flutter nặng nề ở trên đi.
# Chỉ dùng Nginx (Web Server) bản Alpine siêu nhẹ (chỉ khoảng 5MB).
FROM nginx:alpine

# Xóa sạch cái trang web chào mừng mặc định của Nginx
RUN rm -rf /usr/share/nginx/html/*

# Sao chép thành quả (bản Web đã build xong) từ Bước 1 sang thư mục của Nginx
COPY --from=build-env /app/build/web /usr/share/nginx/html

# Mở cổng 80 (cổng Web mặc định)
EXPOSE 80

# Copy file kịch bản tiêm biến môi trường vào Nginx
COPY entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/bin/sh", "/entrypoint.sh"]
