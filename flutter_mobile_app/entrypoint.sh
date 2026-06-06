#!/bin/sh
echo "Đang tiêm biến môi trường API_BASE_URL vào mã nguồn tĩnh..."

# Quét tất cả các file Javascript và thay thế chữ Placeholder thành biến môi trường thật lấy từ K8s
find /usr/share/nginx/html -type f -name "*.js" -exec sed -i "s|__API_BASE_URL_PLACEHOLDER__|${API_BASE_URL}|g" {} +

# Ghi SUPABASE credentials vào file .env để Flutter dotenv đọc được
echo "SUPABASE_URL=${SUPABASE_URL}" > /usr/share/nginx/html/assets/.env
echo "SUPABASE_ANON_KEY=${SUPABASE_ANON_KEY}" >> /usr/share/nginx/html/assets/.env

echo "Tiêm cấu hình thành công! Đang khởi động Nginx..."
# Bật Web Server
nginx -g "daemon off;"
