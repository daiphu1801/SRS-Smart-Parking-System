-- 1. Tạo một cái Sequence mới (Nhảy mỗi lần 50 số để khớp với jdbc.batch_size)
CREATE SEQUENCE parking_sessions_seq INCREMENT BY 50 START 1;

-- 2. Chỉnh cái Sequence này đếm tiếp từ cái ID lớn nhất hiện tại trong bảng (để không bị trùng ID cũ)
SELECT setval('parking_sessions_seq', (SELECT MAX(id) FROM parking_sessions));

-- 3. Gỡ bỏ AUTO_INCREMENT (hoặc IDENTITY) cũ và trỏ Khóa chính vào Sequence mới
ALTER TABLE parking_sessions ALTER COLUMN id SET DEFAULT nextval('parking_sessions_seq');