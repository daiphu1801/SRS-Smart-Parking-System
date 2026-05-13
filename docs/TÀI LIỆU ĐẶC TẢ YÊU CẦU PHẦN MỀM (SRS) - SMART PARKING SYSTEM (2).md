---
title: "[]{#_w421n39c55sg .anchor}Phân tích hệ thống"
---

### **TÀI LIỆU ĐẶC TẢ YÊU CẦU PHẦN MỀM (SRS) - SMART PARKING SYSTEM**

**1. TỔNG QUAN DỰ ÁN (PROJECT OVERVIEW)**

**1.1. Giới thiệu** Hệ thống quản lý bãi đỗ xe thông minh là giải pháp tự động hóa quy trình kiểm soát phương tiện ra/vào và giám sát vị trí đỗ xe bằng công nghệ nhận diện hình ảnh (AI Camera). Hệ thống phục vụ song song hai tệp khách hàng: vãng lai và thuê bao dài hạn.

**1.2. Mục tiêu Cốt lõi**

- Tự động hóa hoàn toàn quy trình nhận diện, tính phí và mở cổng, giảm thiểu sự can thiệp của con người.

- Giải quyết bài toán linh hoạt trong việc quản lý theo cụm phương tiện (một hộ gia đình đăng ký nhiều xe, cấp hạn mức doanh nghiệp).

- Cung cấp trải nghiệm thanh toán liền mạch và hệ thống cảnh báo an ninh toàn diện.

**2. PHẠM VI & ĐỐI TƯỢNG (SCOPE & ACTORS)**

**2.1. Phân hệ Hệ thống (System Scope)**

- **Customer App (Mobile):** Ứng dụng dành cho khách hàng để xem thông tin, nhận cảnh báo, quản lý ví và thanh toán.

- **Guard App (Mobile/Tablet/PC):** Ứng dụng dành cho bảo vệ hiện trường để nhận luồng cảnh báo sự cố theo thời gian thực và xử lý ngoại lệ.

- **Admin Portal (Web):** Cổng quản trị dành cho Ban quản lý thiết lập bảng giá, gói cước, hạn mức và theo dõi doanh thu.

- **AI Camera Subsystem:** Cụm thiết bị IoT tại cổng (quét biển số, phân loại xe) và tại bãi đỗ (giám sát vị trí, an ninh).

- **Virtual LED Dashboard:** Giao diện hiển thị thông tin điều hướng và tính tiền giả lập màn hình LED.

- **Pay-on-foot Kiosk (Trạm thanh toán trung tâm):** Thiết bị Mobile/Tablet đặt tại sảnh, cho phép tài xế thao tác thanh toán trước thay vì chờ ở cổng.

**2.2. Các Tác nhân (Actors)**

- **Khách vãng lai:** Sử dụng dịch vụ theo lượt, thanh toán dựa trên thời gian thực tế.

- **Khách lâu dài:** Đăng ký sử dụng dịch vụ theo tháng/quý. Một tài khoản có thể đại diện quản lý nhiều phương tiện.

- **Bảo vệ (Guard):** Điều phối xe, thu tiền mặt, tiếp nhận và xử lý các thông báo vi phạm.

- **Ban Quản Lý (Admin):** Khởi tạo dữ liệu người dùng, thiết lập biểu giá động.

**3. YÊU CẦU CHỨC NĂNG (FUNCTIONAL REQUIREMENTS)**

**3.1. Phân hệ Xử lý Ra/Vào & Thanh toán (Check-in/Check-out)**

- **Check-in Tự động & Phân loại AI:** Khi xe tiến vào, AI Camera quét biển số xe và phân loại hình dáng phương tiện (Xe máy - Ô tô). Hệ thống ghi nhận dữ liệu và tự động mở barie.

- **Check-out & Tính cước:** Tại cổng ra, camera đối chiếu biển số để tính thời gian đỗ.

- **Check-out và thanh toán trước:** Thời gian ân hạn (Grace Period) tối đa x phút, quá thời gian ân hạn khách phải thanh toán thêm phụ phí phát sinh.

- **Biểu giá động (Dynamic Pricing):** Hệ thống tính tiền dựa trên các tham số cấu hình linh hoạt: loại phương tiện (xe to phí cao hơn), khung giờ (giờ cao điểm, qua đêm) và ngày (lễ, Tết). Bảng giá này được lưu trữ trong Database.

- **Thanh toán Hybrid (Đa phương thức):**
  - **Không tiền mặt:** Khách nhập biển số vào màn hình của Kiosk tại sảnh, hệ thống hiển thị thông tin, giờ vào, hóa đơn và QR code (VietQR/PayOS) để thanh toán. Cho phép tối đa x phút từ lúc thanh toán thành công đến lúc ra khỏi cổng, nếu không phải thanh toán thêm.

  - **Tiền mặt (Fallback):** Khách đưa tiền mặt cho bảo vệ tại cổng. Bảo vệ bấm nút \"Đã thu tiền\" trên Guard App để mở barie. Giao dịch được ghi log vào hệ thống. Cuối ca, bảo vệ tự kiểm đếm và nộp lại báo cáo doanh thu để kế toán đối soát.

**3.2. Phân hệ Khách Lâu Dài & Doanh Nghiệp (Long-term & Corporate)**

- **Quản lý Cụm phương tiện (Household):** Cho phép tạo \"Tài khoản Hộ gia đình/Doanh nghiệp\". Tài khoản có thể đăng ký mua vé tháng cho nhiều biển số xe khác nhau, thanh toán gộp chung một hóa đơn. Biển số xe được lưu cứng cùng thông tin Hợp đồng thuê bao (BookingDetails).

- **Chu kỳ Thanh toán & Nhắc nợ tự động:**
  - Hệ thống tự động quét dữ liệu thuê bao mỗi ngày. Trước khi hết hạn (VD: 5 ngày), hệ thống tự động gửi thông báo nhắc nợ.

  - Thông báo đính kèm đường dẫn (Link) dẫn tới trang thanh toán chứa mã QR động.

  - Khi khách thanh toán thành công, hệ thống tự động gia hạn ngày sử dụng (valid_until) và lưu thông báo xác nhận vào Database để khách tra cứu.

- **Phân hạng chỗ đỗ (Parking Tiers):** Hệ thống chia bãi đỗ thành các phân hạng không gian gắn với các khu vực đỗ riêng.

**3.3. Phân hệ Giám sát AI (AI Monitoring)**

- **Bảng LED Chỉ dẫn (Indoor Guidance):** Hệ thống liên tục đếm số xe ra/vào tại các khu vực (Zone) thông qua AI Camera để tính toán số chỗ trống realtime. Kết quả hiển thị lên các bảng LED chỉ đường (VD: Hầm B1: Còn 4 chỗ trống), giúp phân luồng các loại xe về đúng khu vực.

**3.4. Phân hệ An ninh & Xử lý Ngoại lệ (Security & Exceptions)**

- **Xử lý Ngoại lệ tại Cổng (Manual Override):** Nếu AI không đọc được biển số (do mờ, hỏng, khuất sáng), camera chụp ảnh toàn cảnh hiện trường gửi về app. Bảo vệ dùng Guard App để nhập biển số bằng tay. Mọi thao tác này được lưu Log kèm hình ảnh để Admin hậu kiểm.

- **Cảnh báo Đỗ sai quy định / Chắn lối đi:** AI nhận diện các xe đỗ sai làn, đỗ tại các khúc cua/lối đi quá thời gian quy định (VD: 4 phút) và lập tức báo động cho bảo vệ.

- **Quy trình Xử lý Vi phạm Tối giản:** Cảnh báo vi phạm được truyền (Broadcast) tức thì qua mạng tới các thiết bị của bảo vệ đang trong trạng thái Online. Bảo vệ có trách nhiệm ra hiện trường xử lý bằng mắt thường và nhắc nhở khách hàng. Hệ thống không áp dụng cơ chế khóa cứng barie (block) hay quản lý \"Danh sách đen\" để đảm bảo lưu thông thông suốt.

- **Chiến lược Xử lý Khủng hoảng Thiếu chỗ (Overbooking Backup Strategy):** Hệ thống tự động chuyển trạng thái bảng LED/Barie sang \"HẾT CHỖ\" và kiên quyết từ chối khách vãng lai mới khi tổng số chỗ trống toàn bãi tụt xuống một ngưỡng phần trăm nhất định (VD: 4%), giữ nguyên số chỗ đệm mềm chờ xe thuê bao quay về.

**3.5. Phân hệ Quản trị Trung tâm (Admin Portal)**

- **Quản lý Không gian (Digital Topology):** Giao diện cho phép Admin phân chia bãi xe thành các Khu vực (Zone) theo từng Tầng (Floor). Thiết bị IoT (Camera, Barie) được cấu hình gán trực tiếp vào các Zone với hướng luồng xe xác định (Chiều IN hoặc Chiều OUT).

- **Cấu hình Tham số Động (Dynamic Settings):** Cung cấp giao diện Web UI trực quan để Admin thiết lập mọi trị số điều hành (thời gian ân hạn, mốc tính phí, hệ số an toàn - Safety Buffer). Để tối ưu hiệu năng, Backend tiếp nhận dữ liệu từ UI và tiến hành ghi đè trực tiếp xuống cấu hình hệ thống (file settings.json) thay vì lưu vào Database. Hệ thống sẽ tự động tải lại cấu hình mà không cần khởi động lại.

- **Quản lý Thiết bị IoT (Device Management):** Dashboard giám sát tình trạng hoạt động mạng (Online/Offline/Error) của toàn bộ thiết bị phần cứng bao gồm: Barie, Camera AI, Màn hình LED.

- **Thuật toán Dự báo Lấp đầy & Quản lý Vùng đệm (Predictive Occupancy & Safety Buffer):**
  - **AI Dự báo (Time-series Forecasting):** Phân tích dữ liệu lịch sử để dự báo tỷ lệ xe thuê bao vắng mặt theo khung giờ, quyết định số chỗ được phép \"bán chéo\" cho khách vãng lai.

  - **Cấu hình Safety Buffer:** Admin điều chỉnh hệ số an toàn ban đầu (lưu qua file settings.json) để dự phòng biến động số chỗ, đảm bảo không thiếu chỗ cho xe thuê bao.

- **Báo cáo Thống kê & Lịch sử Đỗ xe (Reports & Parking History):** \* Dashboard trực quan hóa dữ liệu doanh thu (phân tách doanh thu vé vãng lai và vé tháng).
  - Tra cứu lịch sử ra/vào của biển số xe, kèm đầy đủ hình ảnh Log check-in/check-out để làm bằng chứng.

**4. KIẾN TRÚC & Ý TƯỞNG MỞ RỘNG (ARCHITECTURE & EXTENSIONS)**

- **Tích hợp Cổng thanh toán một chiều:** Hệ thống giao tiếp API qua các chuẩn mã mật bảo mật (Client ID, API Key) với các đơn vị Cổng thanh toán (như PayOS, VNPay) nhằm xử lý giao dịch. Đảm bảo an ninh dữ liệu, hệ thống **không** cung cấp dịch vụ Open API mở ngược ra ngoài cho các bên thứ ba khai thác.

- **Kiến trúc Cảnh báo Đa luồng (Multi-tier Notification):**
  - **Luồng Cache/Socket (Thời gian thực):** Chuyên dùng để Broadcast các tín hiệu AI, rủi ro an ninh thẳng tới các thiết bị di động của Bảo vệ (Guard App) đang trong trạng thái Online (Đã đăng nhập). Dữ liệu này không lưu cứng.

  - **Luồng Database (Lưu vết):** Chuyên dùng để lưu trữ các thông báo nghiệp vụ, xác nhận thanh toán, báo cáo nợ đọng cho từng tài khoản Khách hàng để họ có thể tra cứu lịch sử trên App.

- **Kiến trúc Microservices:** Tách biệt các core services (AI Nhận diện, Thanh toán, Quản lý User) để dễ dàng nâng cấp, bảo trì và triển khai linh hoạt trên Cloud.

Use Case List

### **DANH SÁCH USE CASE**

**NHÓM 1: KHÁCH HÀNG - TƯƠNG TÁC TẠI HIỆN TRƯỜNG (PHYSICAL TOUCHPOINTS)**

_(Tác nhân: Mọi loại khách hàng (Vãng lai, Thuê bao). Tương tác qua thiết bị IoT.)_

- **UC1.1: Check-in tự động qua AI:** Xe tiến vào cổng, Camera LPR nhận diện biển số, truyền sự kiện qua hệ thống. Hệ thống kiểm tra hợp lệ (Thuê bao còn hạn hoặc Khách vãng lai) và tự động bật rơ-le mở barie.

- **UC1.2: Xem thông tin điều hướng nội bộ:** Khách hàng quan sát Bảng LED để biết số chỗ trống tại từng Tầng/Khu vực (Zone) được hệ thống cập nhật realtime.

- **UC1.3: Thanh toán trước qua Kiosk (Pay-on-foot):** Khách vãng lai nhập biển số trên thiết bị Mobile/Tablet tại sảnh, quét mã QR thanh toán để nhận \"Thời gian ân hạn\" (Grace Period) trước khi ra lấy xe.

- **UC1.4: Check-out tự động qua AI:** Xe tiến ra cổng, Camera quét biển số. Hệ thống tự động mở barie nếu đáp ứng điều kiện: Xe Thuê bao còn hạn, hoặc xe vãng lai đã thanh toán Kiosk và chưa vượt quá Thời gian ân hạn.

**NHÓM 2: KHÁCH HÀNG THUÊ BAO - TƯƠNG TÁC QUA APP (CUSTOMER APP)**

_(Tác nhân: Khách hàng thuộc Hộ gia đình/Doanh nghiệp đã có tài khoản định danh.)_

- **UC2.1: Đăng nhập & Quản lý Thông tin:** Khách hàng đăng nhập ứng dụng bằng OTP. Xem thông tin cá nhân và thông báo từ hệ thống (Nhắc nợ, biên lai thanh toán).

- **UC2.2: Mua & Gia hạn Gói cước (Subscriptions):** Khách hàng chọn Gói cước phù hợp \$\rightarrow\$ Nhập trực tiếp Biển số xe cần đăng ký \$\rightarrow\$ Thanh toán online qua Gateway (PayOS). Hệ thống ghi nhận biển số vào hợp đồng (BookingDetails).

- **UC2.3: Gửi phản hồi, khiếu nại:** Khách hàng báo cáo các sự cố xảy ra trong bãi đỗ (barie không mở, nhầm biển số) trực tiếp qua App cho Ban quản lý.

- **UC2.4: Tra cứu lịch sử & Giao dịch:** Xem lại lịch sử xe ra/vào cổng (kèm ảnh chụp) và lịch sử thanh toán hóa đơn.

**NHÓM 3: BẢO VỆ (SECURITY GUARD)**

_(Tác nhân: Nhân viên an ninh. Tương tác qua Guard App.)_

- **UC3.1: Đăng nhập & Kích hoạt Online:** Bảo vệ đăng nhập vào Guard App bằng tài khoản do Admin cấp. Hệ thống tự động chuyển trạng thái nhân viên sang \"Online\" và mở kết nối WebSocket để sẵn sàng nhận cảnh báo từ AI.

- **UC3.2: Xác nhận thu tiền mặt (Fallback):** Khi khách vãng lai không thanh toán Kiosk, bảo vệ thu tiền mặt tại cổng và bấm \"Đã thu tiền\" trên Guard App để ghi nhận doanh thu và mở barie.

- **UC3.3: Xử lý ngoại lệ Ra/Vào (Manual Override):** Nhập tay biển số trên App để đối chiếu và mở barie khi AI Camera không đọc được (do mờ, lóa). Hệ thống lưu log thao tác tay kèm ảnh để hậu kiểm.

- **UC3.4: Tra cứu nhanh phương tiện:** Nhập biển số trên Guard App để tra cứu trạng thái xe: Thuộc căn hộ nào, vé tháng còn hạn hay không.

- **UC3.5: Xử lý cảnh báo đỗ sai quy định:** Nhận thông báo Broadcast từ hệ thống khi AI phát hiện xe đỗ chắn lối đi. Bảo vệ ra hiện trường nhắc nhở thủ công, bấm xác nhận hoàn tất trên App.

**NHÓM 4: BAN QUẢN LÝ (ADMIN)** _(Tác nhân: Quản trị viên hệ thống. Tương tác qua Admin Web Portal.)_

- **UC4.1: Quản lý Định danh (Identity Management):** Khởi tạo Customer_Groups (Căn hộ/Doanh nghiệp) và quản lý tài khoản cư dân.

- **UC4.2: Khởi tạo Tài khoản Nội bộ (Internal Provisioning):** Tạo tài khoản đăng nhập (Employees) cấp quyền cho Kế toán và Bảo vệ.

- **UC4.3: Quản lý Không gian & IoT (Topology & Devices):** Tạo các Khu vực chức năng (Zone) phân cấp theo Tầng (Floor). Khai báo IP và gán trực tiếp thiết bị IoT (Camera, Barie) vào Zone (chỉ định chiều IN/OUT).

- **UC4.4: Cấu hình Bảng giá & Gói cước (Pricing & Packages):** Thiết lập các Gói cước vé tháng (Package) và Bảng giá vãng lai (Block thời gian) lưu vào Database.

- **UC4.5: Cấu hình Tham số Hệ thống (System Settings):** Admin điều chỉnh các tham số kỹ thuật (Thời gian ân hạn, Hệ số dự phòng - Buffer). Backend ghi đè trực tiếp xuống file settings.json.

- **UC4.6: Cấu hình Cổng thanh toán (Payment Gateway):** Cấu hình khóa bảo mật (Client ID, API Key) của PayOS/VNPay để xử lý giao dịch.

- **UC4.7: Tra cứu Log & Hậu kiểm (Audit):** Tra soát lịch sử ra/vào, giao dịch, và lịch sử mở barie bằng tay của Bảo vệ kèm ảnh chụp.

- **UC4.8: Báo cáo Thống kê Doanh thu (Analytics):** Xem Dashboard phân tích doanh thu (tách biệt Khách vãng lai và Khách mua vé tháng).

- **UC4.9: Quản lý Khiếu nại (Complaint Resolution):** Tiếp nhận và xử lý khiếu nại (Claims) từ khách hàng.

- **UC4.10: Hiệu chỉnh AI (AI Calibration):** Vẽ vùng nhận diện (ROI) cho camera và thiết lập ngưỡng tin cậy (Confidence Score).

Use Case

# **USE CASE CHI TIẾT** {#use-case-chi-tiết}

### **NHÓM 1: KHÁCH HÀNG - TƯƠNG TÁC TẠI HIỆN TRƯỜNG (PHYSICAL TOUCHPOINTS)**

**Tác nhân (Actor):** Khách hàng chung (Mọi loại khách hàng: Vãng lai, Cư dân, Khách sạc).

#### **UC1.1: Check-in tự động** {#uc1.1-check-in-tự-động}

**1. Mô tả:** Khách hàng lái xe qua cổng kiểm soát đầu vào, hệ thống tự động nhận diện biển số, phân loại đối tượng ngầm và mở barie mà không cần sự can thiệp của con người.

**2. Tiền điều kiện:** - Hệ thống Camera LPR và Barie đang hoạt động kết nối với Server.

- Bãi đỗ xe chưa đạt ngưỡng \"Cut-off\" (Ngưỡng khóa bãi đối với xe vãng lai).

- Thiết bị IoT (Camera, Barie) đang Online.

**3. Luồng sự kiện chính (Basic Flow):**

1.  Khách hàng điều khiển xe tiến vào vùng nhận diện của Camera LPR tại cổng vào.

2.  Camera LPR tại Edge Node chụp ảnh biển số và phân loại phương tiện.

3.  Edge Server xử lý nhận diện, đóng gói JSON và gửi Event lên Kafka Topic vehicle-entry..

4.  Cloud Backend nhận Event, thực hiện kiểm tra:
    - a\. Nếu biển số thuộc **Subscriptions** (còn hạn): Áp dụng vé tháng.

    - b\. Nếu không: Tạo **Parking_Session** mới cho khách vãng lai.

5.  Backend gửi lệnh mở Barie xuống Edge Server.

6.  Barie mở, tài xế đi vào. Hệ thống cập nhật số chỗ trống của Zone hạ nguồn.

**4. Luồng ngoại lệ (Exception Flows):**

- **E1 - Bãi xe hết chỗ vãng lai:** Hệ thống phát hiện ngưỡng trống dành cho vãng lai bằng 0 (chỉ còn Buffer dự phòng cho Cư dân). Bảng LED cổng vào báo \"HẾT CHỖ\", Barie không mở đối với xe vãng lai (Nhánh B). Xe Cư dân (Nhánh A) vẫn được mở.

- **E2 - Lỗi nhận diện LPR:** Camera bị chói sáng hoặc bùn đất che biển số. Khách hàng bị kẹt tại cổng. Chuyển sang sự can thiệp của Bảo vệ (UC của Guard: Nhập tay biển số).

**5. Hậu điều kiện:** Trạng thái của xe trên hệ thống được cập nhật thành \"In-Parking\" (Đang trong bãi). Số lượng xe trong Zone tăng lên 1.

#### **UC1.2: Xem thông tin điều hướng** {#uc1.2-xem-thông-tin-điều-hướng}

**1. Mô tả:** Khách hàng lái xe theo chỉ dẫn của bảng LED để xem khu nào trống.

**2. Tiền điều kiện:** Xe vừa đi qua một Cổng (Gate) hoặc đang đứng trước Ngã rẽ có bảng LED.

**3. Luồng sự kiện chính (Basic Flow):**

1.  Khi xe đi qua một Gate, Kafka bắn sự kiện cập nhật số lượng xe của Zone tương ứng.

2.  Thuật toán **Roll-up** tính toán lại số chỗ trống của các Zone con và Zone cha.

3.  Hệ thống xác định các Bảng LED có liên quan đến sự biến động này.

4.  Dựa trên cấu trúc cây (Tree Hierarchy), hệ thống gửi dữ liệu hiển thị mới xuống LED:
    - Nếu Zone đích có con: Hiển thị trạng thái các Zone con trực thuộc.

    - Nếu là Zone lá: Hiển thị trạng thái trống/đầy của chính nó.

5.  Tài xế quan sát bảng LED để quyết định hướng di chuyển tiếp theo.

**4. Luồng ngoại lệ (Exception Flows):**

- **E1 \-- Mất kết nối LED:** Bảng LED hiển thị trạng thái mặc định (hoặc tắt) để tránh chỉ dẫn sai.

**5. Hậu điều kiện:** Khách hàng tìm được ô đỗ thường.

#### **UC1.3: Thanh toán trước qua Kiosk (Pay-on-foot)** {#uc1.3-thanh-toán-trước-qua-kiosk-pay-on-foot}

**1. Mô tả:** Trước khi xuống hầm lấy xe, khách hàng tới máy Kiosk ở sảnh, tra cứu biển số và thanh toán mọi khoản phí để hệ thống cấp \"thời gian ân hạn\", giúp qua cổng ra mà không cần dừng lại trả tiền.

**2. Tiền điều kiện:** Xe đang ở trạng thái đỗ trong bãi (có Parking_Session đang mở).

**3. Luồng sự kiện chính (Basic Flow):**

1.  Khách hàng thao tác trên màn hình Kiosk, chọn \"Thanh toán\".

2.  Khách hàng nhập Biển số xe.

3.  Hệ thống truy vấn Parking_Sessions, tính toán tiền phí dựa trên **Tariff_Rules** (Biểu giá động)..

4.  Hệ thống gọi API bên thứ 3 (PayOS/Casso) tạo mã **VietQR động** kèm số tiền và nội dung định danh.

5.  Khách hàng quét mã bằng App Ngân hàng và thực hiện chuyển khoản.

6.  Backend nhận **Webhook** xác nhận thành công, cập nhật trạng thái Session thành PAID.

7.  Hệ thống tính toán và hiển thị \"Thời gian ân hạn\" (Grace Period) trên màn hình Kiosk.

**4. Luồng ngoại lệ (Exception Flows):**

- **E1 - Không tìm thấy biển số:** LPR nhận diện sai ngay từ lúc vào. Kiosk báo lỗi \"Không tìm thấy xe\". Khách hàng phải xuống bốt bảo vệ cổng ra để xử lý thủ công.

- **E2 -** **Lỗi thanh toán:** Khách hàng có thể chọn thanh toán lại hoặc trả tiền mặt tại cổng (UC3.1).

**5. Hậu điều kiện:** Trạng thái hóa đơn của xe chuyển thành \"Đã thanh toán (Paid)\". Bộ đếm thời gian ân hạn Check-out bắt đầu chạy ngầm.

#### **UC1.4: Check-out tự động & Thanh toán thông minh** {#uc1.4-check-out-tự-động-thanh-toán-thông-minh}

**1. Mô tả:** Khách hàng lái xe ra khỏi bãi, hệ thống quét biển số, tự động tính toán các khoản phí phát sinh (nếu có), thực hiện lệnh trừ ví tự động và đối chiếu điều kiện để mở cổng.

**2. Tiền điều kiện:** Xe đang tiến ra cổng Check-out.

**3. Luồng sự kiện chính (Basic Flow):**

1.  Xe tiến vào vùng quét của Camera LPR tại cổng ra.

2.  **Camera LPR chụp biển số, Edge Server đẩy dữ liệu lên Kafka Topic vehicle-exit.**

3.  **Cloud Backend thực hiện đối chiếu điều kiện xuất bãi:**
    - **Đối với Thuê bao:** Kiểm tra Subscriptions còn hiệu lực hay không.

    - **Đối với Vãng lai:** Kiểm tra trạng thái PAID và thời gian ân hạn (Grace Period).

4.  **Nếu hợp lệ:** Backend gửi lệnh mở Barie và kết thúc (Close) Parking_Session.

5.  **Nếu không hợp lệ (Chưa trả tiền/Quá giờ ân hạn):** Barie không mở, LED hiển thị yêu cầu thanh toán hoặc báo bảo vệ.

6.  Xe đi qua cổng, số lượng xe trong Zone giảm xuống 1.

**4. Luồng ngoại lệ (Exception Flows):**

- **E1 - Quá giờ ân hạn: Hệ thống yêu cầu thanh toán bổ sung phần thời gian vượt mức.**

**5. Hậu điều kiện:** Xe rời bãi thành công. Dữ liệu lấp đầy của bãi xe được cập nhật lại trên toàn hệ thống.

#### **UC 1.5: Đăng ký Thuê bao mới (Initial Subscription Registration)** {#uc-1.5-đăng-ký-thuê-bao-mới-initial-subscription-registration}

- **Tác nhân:** Chủ tài khoản (Master Account).

- **Mô tả:** Người dùng đăng ký một xe mới vào hệ thống lần đầu tiên hoặc sau một thời gian dài ngưng sử dụng. Hệ thống sẽ tính toán để đưa xe này về đúng \"nhịp\" thanh toán mùng 1 của Group.

- **Tiền điều kiện:** Xe đã được khai báo thông tin cơ bản; Master Account đã đăng nhập.

- **Luồng nghiệp vụ chính:**
  1.  Người dùng chọn biển số xe và chọn Gói cước muốn áp dụng (VD: Gói 1 tháng).

  2.  Hệ thống xác định ngày hiện tại và tính số ngày còn lại đến ngày mùng 1 tháng sau.

  3.  Hệ thống tính **Tiền lẻ (Pro-rated)**: (Giá gói / 30) \* Số ngày lẻ.

  4.  Hệ thống tạo lệnh thanh toán cho: Tiền lẻ + Tiền gói chu kỳ đầu.

  5.  Sau khi thanh toán thành công, hệ thống đặt end_date của xe là ngày mùng 1 của tháng kết thúc chu kỳ.

- **Hậu điều kiện:** Xe chuyển sang trạng thái ACTIVE, barie sẽ tự mở khi xe này đến cổng.

#### **UC 1.6: Gia hạn Thuê bao đồng bộ (Synchronized Renewal)** {#uc-1.6-gia-hạn-thuê-bao-đồng-bộ-synchronized-renewal}

- **Tác nhân:** Chủ tài khoản (Master Account), Hệ thống (Cron Job).

- **Mô tả:** Đến kỳ chốt sổ hàng tháng, người dùng thực hiện gia hạn cho các xe trong danh sách để tiếp tục sử dụng dịch vụ trong chu kỳ tiếp theo.

- **Tiền điều kiện:** Các xe đang ở trạng thái sắp hết hạn (thường là cuối tháng).

- **Luồng nghiệp vụ chính:**
  1.  Hệ thống tự động liệt kê tất cả các xe trong Group có ngày hết hạn vào mùng 1 tháng tới.

  2.  Người dùng truy cập vào \"Giỏ hàng thanh toán\", xem danh sách và **tích chọn** những xe muốn gia hạn.

  3.  Người dùng có thể thay đổi gói cước cho từng xe (VD: Xe A gia hạn 1 tháng, xe B gia hạn 6 tháng).

  4.  Hệ thống tổng hợp tiền và tạo 1 mã QR thanh toán duy nhất.

  5.  Sau khi thành công, hệ thống cộng thêm thời gian vào end_date hiện tại (luôn đảm bảo đích đến là ngày mùng 1).

- **Hậu điều kiện:** end_date của các xe được cập nhật, lịch sử thanh toán được lưu vào Payment_Items.

### **NHÓM 2: KHÁCH HÀNG - TƯƠNG TÁC QUA APP (CUSTOMER APP TOUCHPOINTS)**

**Tác nhân (Actor):** Khách hàng đã tải ứng dụng

#### **UC2.1: Đăng ký / Đăng nhập ứng dụng** {#uc2.1-đăng-ký-đăng-nhập-ứng-dụng}

**1. Mô tả:** Khách hàng tạo tài khoản và đăng nhập vào ứng dụng di động để sử dụng các dịch vụ liên quan đến bãi xe. Lưu ý: Thao tác này chỉ cấp quyền sử dụng App; các quyền đỗ xe dài hạn (Quota) phải do Admin cấp tại Ban quản lý.

**2. Tiền điều kiện:** Khách hàng đã tải App và thiết bị có kết nối Internet.

**3. Luồng sự kiện chính (Basic Flow):**

1.  Khách hàng mở App, chọn Đăng nhập/Đăng ký.

2.  Khách hàng nhập Số điện thoại và yêu cầu gửi mã OTP.

3.  Hệ thống gửi mã OTP qua SMS/Zalo ZNS.

4.  Khách hàng nhập mã OTP để xác thực.

5.  Hệ thống kiểm tra số điện thoại:
    - **Nhánh A (Khách mới/Khách sạc vãng lai):** Hệ thống tạo tài khoản mới (Guest Account) với các quyền hạn cơ bản (Sạc xe, trả phí tự động).

    - **Nhánh B (Khách Cư dân/Doanh nghiệp):** Số điện thoại đã được Admin khai báo trước đó trên hệ thống cấp quyền. App tự động Link (liên kết) tài khoản này với \"Mã định danh Hộ gia đình/Doanh nghiệp\" tương ứng, mở khóa toàn bộ tính năng (Quota miễn phí, Thêm khách đến chơi\...).

6.  App chuyển hướng người dùng vào Màn hình chính (Dashboard).

**4. Luồng ngoại lệ (Exception Flows):**

- **E1 - Không nhận được OTP:** Quá thời gian timeout (VD: 60s), khách hàng bấm \"Gửi lại mã\".

**5. Hậu điều kiện:** Khách hàng đăng nhập thành công, phiên làm việc (Token/Session) được lưu trên thiết bị.

#### **UC2.3: Mua & Gia Hạn Gói Cước (Subscription Renewal)** {#uc2.3-mua-gia-hạn-gói-cước-subscription-renewal}

**1. Mô tả:** Khách hàng thiết lập danh sách biển số xe thuộc quyền sở hữu của mình để hệ thống LPR tại cổng nhận diện tự động.

**2. Tiền điều kiện:** Đã khai báo biển số xe (UC2.2).

**3. Luồng sự kiện chính (Basic Flow):**

1.  Khách hàng chọn biển số xe cần mua/gia hạn.

2.  App hiển thị các **Subscription_Plans** phù hợp với Tier (VIP/Regular) của xe đó.

3.  Khách hàng chọn gói (VD: Gói 1 tháng) ,nhập Biển số xe vào form đăng ký gói\". Dữ liệu biển số sẽ được lưu thẳng vào BookingDetails.và bấm \"Thanh toán\".

4.  Hệ thống tạo mã **VietQR động** (tích hợp PayOS/Casso) và hiển thị trên màn hình App.

5.  Sau khi khách quét mã chuyển khoản thành công, Webhook báo về Backend.

6.  Hệ thống cập nhật ngày hết hạn (valid_until) và gửi thông báo Zalo/Telegram xác nhận thành công.

**4. Luồng ngoại lệ (Exception Flows):**

- **E1 - -Hết thời gian thanh toán:** Mã QR vô hiệu lực, yêu cầu tạo lại lệnh mới.

**5. Hậu điều kiện:**Hiệu lực vé tháng của xe được cập nhật. Doanh thu được ghi nhận.

#### **UC2.4: Gửi phản hồi / Khiếu nại** {#uc2.4-gửi-phản-hồi-khiếu-nại}

**1. Mô tả:** Khách hàng báo cáo các sự cố xảy ra trong bãi đỗ ( xe lạ chiếm chỗ đỗ định danh, barie không mở) trực tiếp qua App.

**2. Tiền điều kiện:** Đã đăng nhập App.

**3. Luồng sự kiện chính (Basic Flow):**

1.  Khách hàng vào mục \"Hỗ trợ / Phản hồi\".

2.  Khách hàng chọn Chủ đề phản hồi (VD: \"Bị chiếm ô đỗ VIP\", "Có xe đỗ sai quy định").

3.  Khách hàng nhập nội dung chi tiết và đính kèm hình ảnh (VD: Chụp ảnh chiếc xe đang chiếm chỗ).

4.  Khách hàng bấm gửi. Hệ thống tạo một Ticket (Phiếu hỗ trợ) gửi đến Admin Portal (UC5.9).

5.  Khi Admin xử lý xong (VD: Điều bảo vệ xuống phạt xe kia), trạng thái Ticket trên App chuyển thành \"Đã giải quyết\" kèm phản hồi từ BQL.

**4. Hậu điều kiện:** Dữ liệu khiếu nại được lưu trữ để Admin tra soát.

#### **UC2.5: Tra Cứu Lịch Sử & Giao Dịch (History & Transactions)** {#uc2.5-tra-cứu-lịch-sử-giao-dịch-history-transactions}

**1. Mô tả:** Khách hàng theo dõi lại mọi hoạt động của mình trên hệ thống để minh bạch tài chính và kiểm soát phương tiện.

**2. Tiền điều kiện:** Đã đăng nhập App.

**3. Luồng sự kiện chính (Basic Flow):**

1.  Khách hàng vào mục \"Lịch sử\".

2.  App cung cấp các Tab phân loại:
    - **Lịch sử Ra/Vào:** Liệt kê các mốc thời gian Check-in/Check-out của từng biển số (có kèm hình ảnh chụp LPR tại cổng để làm bằng chứng).

    - **Lịch sử Giao dịch:** Khách hàng có thể lọc xem lịch sử Giao dịch (Payments) để kiểm tra các khoản phí đã đóng cho vé tháng hoặc phí phạt.

    - Khách hàng sử dụng bộ lọc (Filter) theo tuần/tháng hoặc theo biển số xe cụ thể để tìm kiếm.

**4. Hậu điều kiện:** Khách hàng nắm bắt được toàn bộ lịch sử sử dụng dịch vụ và biến động tài chính.

### **NHÓM 3: NHÂN VIÊN BẢO VỆ (SECURITY GUARD)**

**Tác nhân (Actor):** Nhân viên bảo vệ, thao tác thông qua Guard App trên điện thoại/máy tính bảng hoặc PC tại bốt kiểm soát.

#### **UC3.1: Nhận ca trực (On-Duty Activation)** {#uc3.1-nhận-ca-trực-on-duty-activation}

**1. Tóm tắt:** Nhân viên bảo vệ thực hiện xác thực mã PIN tại máy tính trạm (Kiosk) để hệ thống ghi nhận ca trực. Sau khi xác nhận thành công, Guard App trên điện thoại của bảo vệ đó sẽ được mở khóa các tính năng điều khiển.

**2. Tác nhân:** Nhân viên Bảo vệ.

**3. Tiền điều kiện:** Nhân viên đã được Admin cấp tài khoản và đã thiết lập mã PIN cá nhân (UC4.13).

- Guard App trên điện thoại đã đăng nhập ở trạng thái chờ (Off-Duty).

**4. Luồng sự kiện chính (Basic Flow):**

- Bảo vệ đến quầy an ninh, truy cập vào giao diện Guard Kiosk trên màn hình Desktop.

- Bảo vệ chọn tên/ID của mình từ danh sách nhân sự trên màn hình và nhập mã PIN 4 số.

- Hệ thống Backend xác thực mã PIN khớp với cơ sở dữ liệu.

- Hệ thống cập nhật trạng thái is_on_duty = true cho bảo vệ này, đồng thời thu hồi quyền (is_on_duty = false) của bảo vệ ca trước.

- Guard Kiosk hiển thị thông báo \"Nhận ca thành công\".

- Backend tự động gửi tín hiệu Real-time (WebSocket/FCM) xuống điện thoại của bảo vệ.

- Guard App nhận tín hiệu, lập tức mở khóa giao diện (hiển thị nút bấm Barie, kích hoạt luồng nhận thông báo).

**5. Luồng ngoại lệ (Exception Flows):**

- **E1 - Sai mã PIN:** Hệ thống Kiosk thông báo lỗi, yêu cầu nhập lại. Guard App trên điện thoại vẫn giữ nguyên trạng thái bị khóa.

- **6. Hậu điều kiện:** Mọi thao tác thủ công (mở cổng, thu tiền mặt) từ thời điểm này trở đi sẽ được gắn liền với ID của nhân viên bảo vệ vừa nhận ca.

#### **UC3.2: Xác nhận thu tiền mặt (Xử lý ngoại lệ Check-out)** {#uc3.2-xác-nhận-thu-tiền-mặt-xử-lý-ngoại-lệ-check-out}

**1. Mô tả:** Bảo vệ trực tiếp thu tiền mặt từ khách vãng lai hoặc khách nợ phí tại cổng ra trong các trường hợp: khách chưa thanh toán Kiosk, quá thời gian ân hạn, hoặc khách không có app ngân hàng. **2. Tiền điều kiện:** Xe đang dừng tại cổng Check-out, Barie đang đóng. Hệ thống hiển thị tổng số tiền cần thanh toán trên màn hình PC/Guard App.

**3. Luồng sự kiện chính (Basic Flow):**

1.  Bảo vệ tiếp nhận yêu cầu trả tiền mặt từ khách hàng.

2.  Bảo vệ nhìn vào hệ thống để báo số tiền cần thu.

3.  Nhận tiền mặt từ khách, kiểm đếm và trả lại tiền thừa (nếu có).

4.  Bảo vệ chọn giao dịch của biển số đó trên Guard App/PC và bấm **\"Xác nhận đã thu tiền mặt\"**.

5.  Hệ thống ghi nhận trạng thái hóa đơn thành \"Paid\", lưu doanh thu vào ca trực của tài khoản bảo vệ đang đăng nhập.

6.  Hệ thống tự động phát lệnh mở Barie. Xe khách hàng di chuyển ra ngoài.

**4. Luồng ngoại lệ (Exception Flows):**

- **E1 - Khách không đủ tiền:** Khách hàng không có đủ tiền mặt. Bảo vệ yêu cầu khách tấp xe gọn vào vùng xử lý sự cố để gọi người nhà hoặc chuyển khoản thủ công cho bảo vệ, tiến hành mở Barie cho xe phía sau đi trước.

**5. Hậu điều kiện:** Xe Check-out thành công. Doanh thu tiền mặt được cộng dồn để đối soát (Chốt ca) cuối ngày.

#### **UC3.3: Xử lý ngoại lệ Ra/Vào (Nhập biển thủ công)** {#uc3.3-xử-lý-ngoại-lệ-ravào-nhập-biển-thủ-công}

**1. Mô tả:** Khi hệ thống Camera LPR không thể đọc được biển số do thời tiết (mưa lóa), bùn đất che khuất hoặc biển số bị hỏng/mất, bảo vệ phải can thiệp nhập thông tin bằng tay để cho xe qua cổng.

**2. Tiền điều kiện:** AI tại Edge Camera không nhận diện được biển số (biển số quá bẩn, bị che khuất) và gửi cảnh báo về Guard App.

**3. Luồng sự kiện chính (Basic Flow):**

1.  Bảo vệ dùng Guard App (hoặc PC) chọn tính năng \"Xử lý LPR lỗi\".

2.  Bảo vệ quan sát ảnh và nhập biển số xe bằng tay vào hệ thống.

3.  Backend kiểm tra logic (Check Quota cho thuê bao hoặc khởi tạo Session cho vãng lai).

4.  Nếu hợp lệ, bảo vệ bấm nút **\"Mở cổng thủ công\"**.

**4. Luồng ngoại lệ (Exception Flows):**

- **E1 - Nhập sai biển số lúc Check-out:** Bảo vệ gõ sai biển số, hệ thống báo \"Không tìm thấy xe trong bãi\". Bảo vệ phải gõ lại đúng biển số hoặc tìm kiếm theo hình ảnh xe.

**5. Hậu điều kiện:** Phiên gửi xe/Hóa đơn được tạo với cờ đánh dấu (Flag) là \"Xử lý thủ công\", đính kèm ảnh chụp hiện trường để Admin kiểm tra chống gian lận.

#### **UC3.4: Tra cứu nhanh phương tiện** {#uc3.4-tra-cứu-nhanh-phương-tiện}

**1. Mô tả:** Trong quá trình đi tuần tra hầm bãi xe, bảo vệ có thể tra cứu thông tin của một chiếc xe bất kỳ để biết xe đó có đỗ hợp lệ hay không.

**2. Tiền điều kiện:** Đã đăng nhập vào Guard App.

**3. Luồng sự kiện chính (Basic Flow):**

1.  Bảo vệ mở tính năng \"Tra cứu phương tiện\".

2.  Bảo vệ nhập biển số xe (hoặc mở camera App chụp biển số để App tự bóc tách text).

3.  Hệ thống trả về thông tin chi tiết:
    - Trạng thái: Đang trong bãi.

    - Phân loại: Xe Vãng lai / Xe Cư dân (Thuộc căn hộ/công ty nào) / Hạng VIP.

    - Thời gian Check-in.

    - Trạng thái nợ phí.

**4. Luồng ngoại lệ (Exception Flows):**

- **E1 - Xe không có dữ liệu:** Hệ thống báo \"Không tìm thấy phiên Check-in\". Điều này cảnh báo xe có thể lọt vào bãi trái phép (đi ké đuôi xe khác), bảo vệ tiến hành khóa bánh và lập biên bản.

#### **UC3.5: Xử lý vi phạm tại chỗ** {#uc3.5-xử-lý-vi-phạm-tại-chỗ}

**1. Mô tả:** Bảo vệ tiếp nhận thông báo vi phạm từ hệ thống AI hoặc tự phát hiện khi tuần tra (chiếm ô đỗ định danh, nhận được khiếu nại từ khách hàng). Bảo vệ tiến hành xử lý và áp dụng án phạt.

**2. Tiền điều kiện:** Camera ROI (Region of Interest) phát hiện xe đỗ tại lối đi \> 4 phút

**3. Luồng sự kiện chính (Basic Flow):**

1.  Hệ thống gửi thông báo kèm vị trí chính xác của xe vi phạm lên bản đồ Guard App.

2.  Bảo vệ ra hiện trường xử lý (yêu cầu di dời hoặc khóa bánh tùy quy định).

3.  Bảo vệ cập nhật trạng thái xử lý vào hệ thống để Admin theo dõi.

**4. Hậu điều kiện:** Trật tự và an ninh bãi xe được duy trì. Các đối tượng xấu bị ngăn chặn kịp thời.

### **NHÓM 4: BAN QUẢN LÝ (ADMIN PORTAL)**

**Tác nhân (Actor):** Quản trị viên (Admin), Ban quản lý tòa nhà/bãi xe. **Giao diện tương tác:** Web-based Admin Dashboard (Truy cập qua trình duyệt PC/Laptop).

#### **UC4.1: Quản lý Định danh (Identity Management)** {#uc4.1-quản-lý-định-danh-identity-management}

1.  **Mô tả:** Admin tạo lập nhóm khách hàng (Căn hộ, Doanh nghiệp) để cấp quyền quản lý mua vé tháng.

2.  **Tiền điều kiện:** Admin đã đăng nhập Admin Portal.

3.  **Luồng sự kiện chính:** Admin chọn tạo mới Nhóm khách hàng (GroupsCustomers) \$\rightarrow\$ Nhập tên nhóm, mã định danh và Profile \$\rightarrow\$ Hệ thống tự động khởi tạo và lưu vào Database.

#### **UC4.2: Khởi tạo Tài khoản Nội bộ (Internal Account Provisioning)** {#uc4.2-khởi-tạo-tài-khoản-nội-bộ-internal-account-provisioning}

1.  **Mô tả:** Admin tạo tài khoản cho nhân viên (Employees).

2.  **Tiền điều kiện:** Đăng nhập Admin Portal.

3.  **Luồng sự kiện chính:** Admin chọn \"Thêm tài khoản\" \$\rightarrow\$ Nhập Tên, Số điện thoại và phân quyền Role (Bảo vệ/Kế toán) \$\rightarrow\$ Hệ thống lưu vào Database. (Nhân viên dùng SĐT này để đăng nhập App/Web và xác thực).

4.  **Luồng ngoại lệ:** E1 - Trùng SĐT: Báo lỗi số điện thoại đã tồn tại.

#### **UC4.3: Quản lý Không gian & IoT (Topology & Devices)** {#uc4.3-quản-lý-không-gian-iot-topology-devices}

1.  **Mô tả:** Admin định nghĩa các khu vực đỗ (Zones) theo Tầng (Floor) và gán thiết bị phần cứng.

2.  **Tiền điều kiện:** Có bản vẽ mặt bằng và thiết bị đã nối mạng.

3.  **Luồng sự kiện chính:** Admin tạo Zone, gán Tầng (Floor) \$\rightarrow\$ Khai báo IP Thiết bị (Camera, Barie) \$\rightarrow\$ Gán Thiết bị vào Zone và chọn chiều nhận diện (IN/OUT) \$\rightarrow\$ Kiểm tra Ping kết nối \$\rightarrow\$ Kích hoạt cấu hình.

4.  **Luồng ngoại lệ:** E1 - Thiết lập thiết bị sai logic: Gán camera IN nhưng không có OUT cho 1 Zone. Hệ thống cảnh báo.

#### **UC4.4: Cấu hình Bảng giá & Gói cước (Pricing & Packages)** {#uc4.4-cấu-hình-bảng-giá-gói-cước-pricing-packages}

1.  **Mô tả:** Admin thiết lập cả Gói dịch vụ vé tháng và Quy tắc tính phí vãng lai.

2.  **Tiền điều kiện:** Danh mục loại xe (Ô tô/Xe máy) đã thiết lập.

3.  **Luồng sự kiện chính:** Admin tạo Gói cước mới (Tên gói, Loại xe, Chu kỳ) và nhập Giá \$\rightarrow\$ Admin thiết lập Giá vãng lai theo block thời gian \$\rightarrow\$ Hệ thống lưu toàn bộ biểu giá vào Database (Packages, TemporaryPrice).

#### **UC4.5: Cấu hình Tham số Hệ thống (System Settings)** {#uc4.5-cấu-hình-tham-số-hệ-thống-system-settings}

1.  **Mô tả:** Thiết lập các tham số vận hành (Safety Buffer, Thời gian ân hạn) không cần lưu Database.

2.  **Tiền điều kiện:** Admin có quyền hệ thống.

3.  **Luồng sự kiện chính:** Admin nhập số phút ân hạn (Grace Period), tỷ lệ % Buffer cảnh báo Hết chỗ trên Web UI \$\rightarrow\$ Bấm Lưu \$\rightarrow\$ **Backend validate và ghi đè trực tiếp xuống file settings.json** \$\rightarrow\$ Hệ thống reload cấu hình vào RAM.

#### **UC4.6: Cấu hình Cổng thanh toán (Payment Gateway)** {#uc4.6-cấu-hình-cổng-thanh-toán-payment-gateway}

1.  **Mô tả:** Thiết lập API kết nối với đối tác thanh toán.

2.  **Tiền điều kiện:** Có API Key của PayOS/VNPay.

3.  **Luồng sự kiện chính:** Admin dán khóa API Key, Client ID, Checksum Key vào giao diện \$\rightarrow\$ Hệ thống mã hóa và lưu trữ \$\rightarrow\$ Ping kiểm tra thử nghiệm tạo luồng Webhook.

#### **UC4.7: Tra cứu Log & Hậu kiểm (Audit)** {#uc4.7-tra-cứu-log-hậu-kiểm-audit}

1.  **Mô tả:** Đối soát dữ liệu lịch sử phục vụ thanh tra chống gian lận.

2.  **Tiền điều kiện:** Có dữ liệu vận hành.

3.  **Luồng sự kiện chính:** Admin tìm kiếm theo Biển số xe, Thời gian, hoặc ID Bảo vệ \$\rightarrow\$ Hệ thống hiển thị danh sách giao dịch, ảnh chụp LPR tại cổng, và lịch sử mở barie thủ công \$\rightarrow\$ Xuất báo cáo (Excel/PDF).

#### **UC4.8: Báo cáo Thống kê Doanh thu (Analytics)** {#uc4.8-báo-cáo-thống-kê-doanh-thu-analytics}

1.  **Mô tả:** Theo dõi hiệu quả kinh doanh.

2.  **Tiền điều kiện:** Có phát sinh giao dịch.

3.  **Luồng sự kiện chính:** Truy cập Dashboard, chọn mốc thời gian \$\rightarrow\$ Hệ thống tổng hợp doanh thu theo kênh: Vé tháng trên App, Vãng lai qua Kiosk, Tiền mặt của Bảo vệ \$\rightarrow\$ Hiển thị biểu đồ và xuất file báo cáo.

#### **UC4.9: Quản lý Khiếu nại (Complaint Resolution)** {#uc4.9-quản-lý-khiếu-nại-complaint-resolution}

1.  **Mô tả:** Tiếp nhận và xử lý khiếu nại (Claims).

2.  **Tiền điều kiện:** Khách hàng gửi khiếu nại từ App.

3.  **Luồng sự kiện chính:** Admin xem danh sách ticket \$\rightarrow\$ Đối chiếu với hệ thống Log/Hình ảnh để xác minh \$\rightarrow\$ Xử lý ngoại tuyến hoặc điều phối bảo vệ \$\rightarrow\$ Cập nhật trạng thái \"Đã giải quyết\" trên hệ thống.

#### **UC4.10: Hiệu chỉnh AI (AI Calibration)** {#uc4.10-hiệu-chỉnh-ai-ai-calibration}

1.  **Mô tả:** Chỉnh vùng nhận diện Camera cho chính xác.

2.  **Tiền điều kiện:** Camera đang livestream RTSP.

3.  **Luồng sự kiện chính:** Admin mở luồng video \$\rightarrow\$ Vẽ vùng nhận diện (ROI - Region of Interest) trên màn hình live \$\rightarrow\$ Cấu hình ngưỡng tin cậy (Confidence Score) \$\rightarrow\$ Lưu lại cho Edge Server xử lý.

Work Flow List

**DANH SÁCH SƠ ĐỒ HOẠT ĐỘNG (WORKFLOWS)**

**NHÓM 1: CÁC LUỒNG VẬN HÀNH HIỆN TRƯỜNG (CORE PHYSICAL OPERATIONS)**

_(Ghi chú: Các luồng này có sự rẽ nhánh logic để phục vụ đồng thời cả Khách vãng lai và Khách thuê bao)._

- **WF1.1: Luồng Check-in Tự động (Auto Check-in Flow):** Bao gồm UC1.1. Mô tả quá trình xe tiến vào, AI đọc biển số. Rẽ nhánh: Nếu là Khách thuê bao \$\rightarrow\$ Kiểm tra hiệu lực Hợp đồng (BookingDetails). Nếu là Khách vãng lai \$\rightarrow\$ Tạo Phiên đỗ xe mới (Parking_Sessions). Gửi lệnh mở Barie.

- **WF1.2: Cập nhật & Điều hướng Bảng LED (LED Navigation Flow):** Bao gồm UC1.2. Logic thuật toán đếm xe ra/vào tại các luồng camera IN/OUT của từng Zone để tính toán số chỗ trống realtime và hiển thị lên Bảng LED phân cấp theo Tầng (Floor).

- **WF1.3: Thanh toán trước tại Kiosk (Pay-on-foot Flow):** Bao gồm UC1.3. Chỉ áp dụng cho Khách vãng lai. Mô tả luồng khách nhập biển số, truy vấn cước, quét VietQR thanh toán và hệ thống kích hoạt Thời gian ân hạn (Grace Period) để xe ra cổng.

- **WF1.4: Luồng Check-out Tự động (Auto Check-out Flow):** Bao gồm UC1.4. Rẽ nhánh: Nếu là Khách thuê bao (hợp đồng còn hạn) \$\rightarrow\$ Mở cổng. Nếu là Khách vãng lai \$\rightarrow\$ Kiểm tra trạng thái hóa đơn. Nếu đã thanh toán Kiosk và còn trong thời gian ân hạn \$\rightarrow\$ Mở cổng.

**NHÓM 2: CÁC LUỒNG TƯƠNG TÁC QUA ỨNG DỤNG (APP JOURNEYS)**

- **WF2.1: Luồng Khởi tạo Nhóm Người Dùng (User Provisioning):** Bao gồm UC4.1 và UC2.1. Admin tạo nhóm người dùng (Customer_Groups) và chỉ định tài khoản đại diện. Tài khoản đại diện đăng nhập ứng dụng lần đầu bằng mã OTP, sau đó hệ thống tự động liên kết tài khoản này với mã định danh nhóm để mở khóa các quyền lợi.

- **WF2.2: Luồng Mua vé tháng (Subscription Flow):** Bao gồm UC2.3. Khách hàng thao tác trên Customer App: Chọn Gói cước (Package) \$\rightarrow\$ Nhập trực tiếp Biển số xe muốn đăng ký \$\rightarrow\$ Thanh toán online \$\rightarrow\$ Hệ thống lưu hợp đồng (BookingDetails) chứa thông tin gói cước và biển số xe \$\rightarrow\$ Đồng bộ quyền truy cập xuống Edge Server.

**NHÓM 3: LUỒNG VẬN HÀNH CỦA BẢO VỆ & XỬ LÝ SỰ CỐ (GUARD OPERATIONS & FALLBACKS)**

- **WF3.1: Luồng Khởi tạo & Đăng nhập Bảo vệ (Guard Provisioning & Login):** Bao gồm UC4.13 và UC3.1. Admin cấp tài khoản cho bảo vệ. Bảo vệ đăng nhập Guard App, hệ thống cập nhật trạng thái Status = \'Online\' và mở kết nối WebSocket để sẵn sàng nhận thông báo Broadcast từ hệ thống AI.

- **WF3.2: Luồng Xử lý Ngoại lệ tại Cổng (Gate Fallback Resolution):** Bao gồm UC3.2 và UC3.3. Xử lý 2 biến cố khi Check-in/Check-out: (1) Camera không đọc được biển số \$\rightarrow\$ Bảo vệ nhập tay mở cổng; (2) Khách vãng lai chưa thanh toán \$\rightarrow\$ Bảo vệ thu tiền mặt, bấm xác nhận trên App để mở Barie.

- **WF3.3: Luồng Xử lý Cảnh báo Đỗ sai quy định (Violation Alert Flow):** Bao gồm UC3.5. AI Camera gửi tín hiệu có xe đỗ chắn lối đi vượt quá thời gian cho phép. Hệ thống Broadcast (đẩy Push Notification) đồng thời cho các Bảo vệ đang Online. Bảo vệ ra hiện trường nhắc nhở thủ công và bấm nút \"Đã xử lý\" trên App để đóng cảnh báo.

**NHÓM 4: CÁC LUỒNG QUẢN TRỊ & THIẾT LẬP (ADMINISTRATIVE FLOWS)**

- **WF4.1: Luồng Hiệu chỉnh Không gian & IoT (Topology & IoT Setup Flow):** Bao gồm UC4.2, UC4.3 và UC4.11. Admin tạo Zone, gán Tầng (Floor) \$\rightarrow\$ Khai báo IP Camera/Barie \$\rightarrow\$ Gán thiết bị trực tiếp vào Zone (chỉ định chiều IN/OUT) \$\rightarrow\$ Vẽ Vùng nhận diện (ROI) và cấu hình Độ nhạy (Confidence Score) truyền xuống các node Edge Server.

- **WF4.2: Luồng Cấu hình Tham số & Giá vé (Settings & Pricing Setup Flow):** Bao gồm UC4.4, UC4.5. Admin thiết lập Giá vé tháng/vãng lai \$\rightarrow\$ Lưu vào Database. Admin cấu hình các tham số kỹ thuật (Grace Period, Safety Buffer) \$\rightarrow\$ Backend ghi đè xuống file cấu hình (settings.json).

- **WF4.3: Luồng Đối soát Doanh thu & Hậu kiểm (Audit & Reconciliation Flow):** Bao gồm UC4.7, UC4.8 và UC4.9. Cuối ngày, Bảo vệ tự kiểm đếm tiền mặt thực tế và nhập báo cáo. Kế toán/Admin tra soát chéo giữa tiền thực tế và log hệ thống (gồm ảnh chụp LPR, số lần bấm mở barie bằng tay của từng ID bảo vệ) để chốt sổ thu chi.

<!-- -->

-

Thẻ 5

| **id** | **pay_code** | **transaction_id** | **amount**    | **status** | **gateway_response (Cục JSON nặng)**                                |
| ------ | ------------ | ------------------ | ------------- | ---------- | ------------------------------------------------------------------- |
| **50** | PAY_01       | **PAYOS_999**      | **3,000,000** | SUCCESS    | {\"code\":\"00\", \"msg\":\"OK\", \"data\": \"DÀI 1000 KÝ TỰ\...\"} |

| **id** | **payment_id** | **booking_detail_id** | **item_amount** |
| ------ | -------------- | --------------------- | --------------- |
| **1**  | **50**         | **Xe_A**              | **1,000,000**   |
| **2**  | **50**         | **Xe_B**              | **1,000,000**   |
| **3**  | **50**         | **Xe_C**              | **1,000,000**   |
