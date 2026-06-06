#!/bin/sh
echo "Dang tiem bien moi truong API_BASE_URL vao ma nguon tinh..."

# Quet tat ca cac file Javascript va thay the chu Placeholder thanh bien moi truong that lay tu K8s
find /usr/share/nginx/html -type f -name "*.js" -exec sed -i "s|__API_BASE_URL_PLACEHOLDER__|${API_BASE_URL}|g" {} +

# Ghi SUPABASE credentials vao file .env de Flutter dotenv doc duoc
echo "SUPABASE_URL=${SUPABASE_URL}" > /usr/share/nginx/html/assets/.env
echo "SUPABASE_ANON_KEY=${SUPABASE_ANON_KEY}" >> /usr/share/nginx/html/assets/.env

echo "Tiem cau hinh thanh cong! Dang khoi dong Nginx..."
# Bat Web Server
nginx -g "daemon off;"
