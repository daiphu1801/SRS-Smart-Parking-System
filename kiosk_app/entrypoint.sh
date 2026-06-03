#!/bin/sh
echo "Đang tiêm biến môi trường API_BASE_URL vào mã nguồn tĩnh..."

find /usr/share/nginx/html -type f -name "*.js" -exec sed -i "s|__API_BASE_URL_PLACEHOLDER__|${API_BASE_URL}|g" {} +

echo "Tiêm cấu hình thành công! Đang khởi động Nginx..."
# Bật Web Server
nginx -g "daemon off;"
