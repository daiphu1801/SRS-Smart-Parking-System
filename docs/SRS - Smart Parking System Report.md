# **1\. Problem Definition (Phiên bản Tiếng Việt)**

## **1.1. Problem Abstraction** 

### **Giới thiệu chung về dự án Quản lý Bãi đỗ xe thông minh**

Trong bối cảnh đô thị hóa mạnh mẽ tại các thành phố lớn của Việt Nam, nhu cầu đỗ xe ngày càng tăng cao trong khi quỹ đất dành cho giao thông tĩnh ngày càng hạn chế. Việc quản lý bãi đỗ xe theo phương thức truyền thống (sử dụng bảo vệ thủ công, ghi chép sổ sách, thanh toán tiền mặt) đang bộc lộ nhiều hạn chế nghiêm trọng: mất thời gian kiểm tra vé, tính toán phí thủ công dễ sai sót, tình trạng ùn tắc tại cổng ra vào giờ cao điểm, thiếu minh bạch trong quản lý doanh thu, cũng như khó khăn trong việc giám sát an ninh và vị trí đỗ xe.

Dự án “Hệ thống Quản lý Bãi đỗ xe Thông minh” được xây dựng nhằm mang đến một giải pháp công nghệ toàn diện, tự động hóa quy trình nhận diện phương tiện, tính phí, thanh toán và giám sát bãi đỗ bằng trí tuệ nhân tạo (AI) và IoT. Hệ thống không chỉ giúp tối ưu hóa năng lực đỗ xe mà còn nâng cao trải nghiệm người dùng cho cả khách vãng lai và khách thuê bao dài hạn, đồng thời mang lại hiệu quả quản lý vượt trội cho Ban quản lý.

### **Xuất xứ của Dự án**

Dự án xuất phát từ thực tế nhu cầu cấp thiết của các tòa nhà văn phòng, chung cư, trung tâm thương mại và khu đô thị tại Việt Nam. Hiện nay, hầu hết các bãi đỗ xe vẫn đang áp dụng mô hình quản lý thủ công hoặc bán tự động, dẫn đến nhiều vấn đề như: thời gian chờ đợi lâu tại cổng, sai sót trong tính toán phí đỗ xe, khó khăn trong việc quản lý khách thuê bao dài hạn (đặc biệt khi một hộ gia đình hoặc doanh nghiệp sở hữu nhiều phương tiện), và tình trạng mất an ninh do thiếu hệ thống giám sát thông minh.

Nhận thấy những bất cập đó, nhóm chúng tôi quyết định phát triển Hệ thống Quản lý Bãi đỗ xe Thông minh – một giải pháp tích hợp công nghệ nhận diện biển số xe bằng AI, thanh toán không tiền mặt, giám sát thời gian thực và quản trị tập trung, nhằm giải quyết triệt để các vấn đề tồn tại trong quản lý bãi đỗ xe truyền thống.

### **Lý do cần thiết xây dựng Dự án**

1. **Tự động hóa quy trình vận hành, giảm phụ thuộc vào con người** Phương thức quản lý thủ công đòi hỏi nhiều nhân sự tại cổng, dễ dẫn đến sai sót và chậm trễ, đặc biệt vào giờ cao điểm. Hệ thống tự động nhận diện biển số và tính phí giúp giảm đáng kể lực lượng bảo vệ, đồng thời tăng tốc độ ra/vào xe.  
2. **Giải quyết bài toán quản lý khách thuê bao phức tạp** Một hộ gia đình hoặc doanh nghiệp thường sở hữu nhiều phương tiện. Hệ thống cho phép tạo tài khoản nhóm (Household/Corporate Account) để quản lý nhiều biển số xe chung một hợp đồng, thanh toán gộp và theo dõi hạn mức sử dụng.  
3. **Tăng tính minh bạch và chính xác trong tính phí & doanh thu** Hệ thống tự động tính phí theo biểu giá động (theo loại xe, khung giờ, ngày lễ), ghi nhận toàn bộ giao dịch, giúp Ban quản lý kiểm soát doanh thu chính xác và giảm thiểu thất thoát.  
4. **Nâng cao trải nghiệm người dùng** Khách hàng có thể thanh toán nhanh chóng qua QR Code tại Kiosk hoặc trên ứng dụng di động, nhận thông báo nhắc nợ tự động, theo dõi lịch sử đỗ xe và nhận cảnh báo an ninh thời gian thực.  
5. **Tăng cường an ninh và giám sát thông minh** Hệ thống sử dụng AI để phát hiện xe đỗ sai vị trí, chắn lối đi hoặc có hành vi bất thường, đồng thời gửi cảnh báo ngay lập tức đến bảo vệ qua ứng dụng Guard App.  
6. **Hỗ trợ quản lý linh hoạt và dự báo thông minh** Ban quản lý có thể cấu hình động các thông số vận hành, dự báo tỷ lệ lấp đầy bãi đỗ và tối ưu hóa việc bán chỗ cho khách vãng lai mà vẫn đảm bảo chỗ đỗ cho khách thuê bao.

### **Thực trạng**

Hiện nay, hầu hết các bãi đỗ xe tại Việt Nam vẫn đang sử dụng phương thức quản lý thủ công hoặc hệ thống barrier cơ bản không tích hợp trí tuệ nhân tạo. Những giải pháp này thường gặp phải các hạn chế lớn về tốc độ xử lý, tính chính xác, khả năng mở rộng và trải nghiệm người dùng. Đặc biệt trong bối cảnh số lượng phương tiện cá nhân tăng nhanh, nhu cầu về một hệ thống quản lý bãi đỗ xe thông minh, tự động và dễ sử dụng là rất cấp thiết.

### **Tầm quan trọng của Dự án**

Hệ thống Quản lý Bãi đỗ xe Thông minh không chỉ là công cụ hỗ trợ vận hành mà còn là nền tảng giúp các chủ đầu tư và Ban quản lý nâng tầm chất lượng dịch vụ, tối ưu hóa nguồn lực, tăng doanh thu bền vững và mang lại trải nghiệm hiện đại, tiện lợi cho người sử dụng. Việc áp dụng công nghệ AI và IoT trong quản lý bãi đỗ xe chính là xu hướng tất yếu, góp phần xây dựng đô thị thông minh tại Việt Nam.

 

## **1.2. The Current System**

Hiện nay, hầu hết các bãi đỗ xe tại Việt Nam vẫn chủ yếu áp dụng phương thức quản lý truyền thống hoặc sử dụng các phần mềm quản lý bãi đỗ xe cơ bản. Qua khảo sát thực tế, chúng tôi nhận thấy rằng các giải pháp hiện tại vẫn tồn tại nhiều hạn chế lớn, chưa đáp ứng được nhu cầu vận hành chuyên nghiệp và hiệu quả trong bối cảnh đô thị hóa mạnh mẽ hiện nay.

### **Thực trạng và những bất cập của các phần mềm quản lý bãi đỗ xe hiện nay**

Phần lớn các bãi đỗ xe vẫn đang sử dụng cách quản lý thủ công kết hợp với một số hệ thống điều khiển barrier thủ công. Các phần mềm quản lý bãi đỗ xe có sẵn trên thị trường thường chỉ tập trung vào các chức năng cơ bản như thu phí thủ công, quản lý vé tháng và báo cáo doanh thu sơ bộ. Những bất cập nổi bật có thể kể đến như:

* Quy trình nhận diện và kiểm soát phương tiện ra vào còn chậm, chủ yếu phụ thuộc vào bảo vệ nhập liệu thủ công, dễ gây ùn tắc giao thông vào giờ cao điểm.  
* Hệ thống tính phí thiếu linh hoạt, chưa hỗ trợ tốt biểu giá động theo khung giờ, loại phương tiện hay ngày lễ, Tết.  
* Khó khăn trong việc quản lý khách thuê bao dài hạn, đặc biệt khi một tài khoản cần quản lý nhiều phương tiện (hộ gia đình hoặc doanh nghiệp).  
* Thanh toán chủ yếu bằng tiền mặt, thiếu các giải pháp thanh toán không tiền mặt tiện lợi và hiện đại.  
* Hệ thống giám sát an ninh và phát hiện vi phạm còn yếu, chủ yếu dựa vào con người nên hiệu quả thấp và dễ bỏ sót.  
* Báo cáo doanh thu thiếu chính xác, khó tra cứu lịch sử đỗ xe chi tiết của từng phương tiện.  
* Giao diện phần mềm thường phức tạp, không thân thiện với người dùng, gây khó khăn cho cả bảo vệ lẫn Ban quản lý.

Những hạn chế trên dẫn đến hiệu quả vận hành thấp, mất nhiều thời gian, dễ xảy ra sai sót, thất thoát doanh thu và làm giảm trải nghiệm của khách hàng.

### **Lợi ích khi áp dụng mô hình Scrum Agile**

Trong thời đại công nghệ số bùng nổ và thị trường biến đổi không ngừng, các dự án phát triển phần mềm đang chịu áp lực phải thích ứng nhanh chóng, tối ưu hiệu suất và liên tục tạo ra giá trị thực tế cho khách hàng. Đứng trước những thách thức ấy, việc lựa chọn một phương pháp quản lý dự án vừa linh hoạt, vừa hiệu quả là yếu tố then chốt quyết định sự thành bại của dự án.

Scrum Agile không chỉ đáp ứng hoàn hảo những yêu cầu trên mà còn trở thành phương pháp được hàng nghìn doanh nghiệp và nhóm phát triển trên thế giới tin tưởng áp dụng. Việc áp dụng Scrum Agile trong dự án “Hệ thống Quản lý Bãi đỗ xe Thông minh” mang lại nhiều lợi ích thiết thực và phù hợp với đặc thù của dự án.

**1\. Tăng khả năng thích ứng và linh hoạt trong mọi tình huống** 

Một trong những đặc điểm nổi bật của Scrum là khả năng thích ứng nhanh chóng với sự thay đổi. Dự án Quản lý Bãi đỗ xe Thông minh có tính phức tạp cao khi phải tích hợp nhiều công nghệ (AI Camera, IoT, Mobile App, Web Portal, Kiosk…). Trong quá trình phát triển, Ban quản lý có thể thay đổi yêu cầu về biểu giá động, thời gian ân hạn, quy trình xử lý ngoại lệ hoặc cách thức cảnh báo an ninh. Scrum cho phép đội ngũ dễ dàng điều chỉnh ưu tiên tính năng trong từng Sprint mà không làm gián đoạn toàn bộ dự án.

**2\. Tăng tính minh bạch và cải thiện giao tiếp trong đội ngũ** 

Scrum thúc đẩy môi trường làm việc minh bạch và cộng tác cao thông qua các cuộc họp định kỳ:

* **Daily Standup**: Cuộc họp ngắn 15 phút mỗi ngày để cập nhật tiến độ công việc, đặc biệt là tiến độ tích hợp AI Camera và xử lý ngoại lệ.  
* **Sprint Review**: Đánh giá sản phẩm đã hoàn thành và nhận phản hồi trực tiếp từ Ban quản lý bãi đỗ xe.  
* **Sprint Retrospective**: Nhìn lại những gì đã làm tốt, chưa tốt để cải tiến quy trình làm việc.

Các buổi họp này giúp đội ngũ luôn nắm rõ tiến độ và kịp thời hỗ trợ lẫn nhau khi gặp khó khăn kỹ thuật.

**3\. Giao sản phẩm sớm và liên tục, mang lại giá trị thực tế ngay từ đầu** 

Thay vì phải chờ đến cuối dự án mới có sản phẩm hoàn chỉnh, Scrum cho phép đội ngũ giao từng phần sản phẩm có giá trị sau mỗi Sprint. Ví dụ: Sau 2–3 Sprint đầu, Ban quản lý đã có thể thử nghiệm tính năng nhận diện biển số tự động và thanh toán QR Code tại Kiosk. Việc này giúp nhận được phản hồi thực tế sớm từ bảo vệ và khách hàng, từ đó điều chỉnh kịp thời.

**4\. Cải thiện chất lượng sản phẩm thông qua phản hồi liên tục** 

Chất lượng là yếu tố then chốt của hệ thống quản lý bãi đỗ xe. Thông qua Sprint Review, đội ngũ liên tục nhận phản hồi từ người dùng thực tế (bảo vệ, Ban quản lý, khách vãng lai) để phát hiện và khắc phục lỗi sớm, đặc biệt trong các quy trình phức tạp như xử lý ngoại lệ tại cổng và cảnh báo đỗ xe sai quy định.

**5\. Tối ưu năng suất và hiệu quả công việc** 

Scrum tập trung vào việc hoàn thành những công việc có giá trị cao nhất thông qua Product Backlog được sắp xếp ưu tiên rõ ràng. Trong dự án này, đội ngũ sẽ ưu tiên hoàn thành các tính năng cốt lõi trước như nhận diện biển số AI, thanh toán hybrid và quản lý khách thuê bao, giúp tối ưu nguồn lực và đẩy nhanh tiến độ dự án.

**6\. Tăng sự hài lòng và sự tham gia của khách hàng** 

Ban quản lý bãi đỗ xe được khuyến khích tham gia sâu vào dự án thông qua các buổi Sprint Review. Họ có cơ hội xem trực tiếp sản phẩm đang phát triển, đưa ra ý kiến thực tế về quy trình vận hành, từ đó đảm bảo hệ thống cuối cùng thực sự phù hợp với nhu cầu quản lý và vận hành thực tế.

**7\. Quản lý rủi ro hiệu quả hơn** 

Dự án Quản lý Bãi đỗ xe Thông minh có nhiều rủi ro kỹ thuật (tích hợp AI Camera, xử lý ngoại lệ, hiệu năng hệ thống thời gian thực…). Scrum giúp phát hiện và giải quyết các rủi ro này sớm thông qua việc theo dõi tiến độ hàng ngày và đánh giá định kỳ, đảm bảo dự án luôn đi đúng hướng và đúng chất lượng.

### **Quy trình làm việc của Scrum Agile**

Scrum Agile hoạt động theo chu kỳ lặp lại (Iterative), cho phép đội ngũ phát triển sản phẩm một cách linh hoạt, minh bạch và tập trung vào giá trị thực tế. Quy trình Scrum gồm các bước chính sau và được áp dụng xuyên suốt dự án “Hệ thống Quản lý Bãi đỗ xe Thông minh”.

1. #### **Product Backlog – Tạo lập và quản lý danh sách công việc**

Product Backlog là “trái tim” của quy trình Scrum, chứa tất cả các yêu cầu, tính năng, cải tiến và công việc cần thiết để phát triển Hệ thống Quản lý Bãi đỗ xe Thông minh. Đây là một danh sách sống (living document), có thể thay đổi liên tục dựa trên phản hồi từ Ban quản lý bãi đỗ xe và nhu cầu thực tế vận hành.

* Product Owner chịu trách nhiệm chính trong việc xây dựng và quản lý Product Backlog. Họ sẽ làm việc chặt chẽ với Ban quản lý để xác định các tính năng quan trọng như: nhận diện biển số xe bằng AI, thanh toán hybrid (QR Code & tiền mặt), quản lý khách thuê bao dài hạn, cảnh báo an ninh thời gian thực, dashboard quản trị…  
* Các mục trong Product Backlog được gọi là Product Backlog Items (PBIs) và có thể ở dạng: tính năng mới, yêu cầu cải tiến, sửa lỗi, hoặc tối ưu hiệu năng hệ thống.

**Mục tiêu**: Xác định rõ ràng tất cả những gì cần làm để đội ngũ phát triển có thể bắt tay vào công việc một cách có hệ thống và ưu tiên đúng đắn.

#### **2\. Sprint Planning – Lập kế hoạch Sprint**

Trước khi mỗi Sprint bắt đầu, toàn đội ngũ sẽ tham gia buổi Sprint Planning để lập kế hoạch cụ thể cho Sprint sắp tới. Đây là bước khởi đầu quan trọng, giúp đội ngũ xác định rõ những gì cần hoàn thành và cách thức thực hiện trong khoảng thời gian Sprint (thường từ 2 đến 4 tuần).

* Từ Product Backlog, đội ngũ chọn ra các công việc ưu tiên để tạo thành **Sprint Backlog**.  
* Xác định **Sprint Goal** – mục tiêu quan trọng nhất của Sprint (ví dụ: “Hoàn thành module nhận diện biển số xe tự động và tích hợp mở barie”).  
* Các thành viên cùng thảo luận, phân tích và chia nhỏ công việc để dễ thực hiện.

**Kết quả**: Đội ngũ bước vào Sprint với một kế hoạch rõ ràng, cụ thể và cam kết hoàn thành.

#### **3\. Development Work – Thực hiện công việc trong Sprint**

Đây là giai đoạn đội ngũ thực thi các công việc đã cam kết trong Sprint Backlog. Thời gian Sprint thường kéo dài từ 1 đến 4 tuần.

**Daily Scrum – Họp hàng ngày** Mỗi ngày, đội ngũ tổ chức một cuộc họp ngắn chỉ kéo dài khoảng 15 phút. Mục đích là để mỗi thành viên báo cáo tiến độ công việc, chia sẻ khó khăn đang gặp phải (ví dụ: khó khăn trong việc huấn luyện mô hình AI nhận diện biển số dưới điều kiện ánh sáng yếu) và tìm cách hỗ trợ lẫn nhau. Scrum Master đóng vai trò điều phối để cuộc họp diễn ra đúng mục đích, ngắn gọn và hiệu quả.

**Tự tổ chức và phối hợp** Đội ngũ Scrum có tính tự tổ chức cao. Các thành viên sẽ tự phân công công việc, phối hợp chặt chẽ giữa các mảng (AI, Backend, Frontend, Mobile App, DevOps) để hoàn thành mục tiêu Sprint. Toàn bộ tiến độ công việc được cập nhật minh bạch trên công cụ quản lý dự án (Jira hoặc Azure DevOps).

**Kết quả**: Công việc được thực hiện mượt mà, có sự hỗ trợ lẫn nhau và không ngừng tiến gần hơn đến Sprint Goal.

#### **4\. Sprint Review – Trình bày kết quả và nhận phản hồi**

Vào cuối mỗi Sprint, đội ngũ tổ chức buổi Sprint Review để trình bày những gì đã hoàn thành cho Product Owner và Ban quản lý bãi đỗ xe.

* **Demo sản phẩm**: Đội ngũ trình diễn các tính năng đã hoàn thành (ví dụ: demo hệ thống nhận diện biển số tự động và thanh toán QR Code tại Kiosk).  
* **Nhận phản hồi**: Ban quản lý và các bên liên quan đưa ra ý kiến, góp ý thực tế về trải nghiệm người dùng, tốc độ xử lý, giao diện…  
* **Cập nhật Product Backlog**: Những yêu cầu mới hoặc điều chỉnh sẽ được bổ sung và sắp xếp lại ưu tiên trong Product Backlog.

**Mục tiêu**: Đảm bảo sản phẩm đang phát triển đúng hướng và mang lại giá trị thực sự cho việc quản lý bãi đỗ xe.

#### **5\. Sprint Retrospective – Nhìn lại và cải tiến**

Sau Sprint Review, đội ngũ tổ chức buổi Sprint Retrospective để đánh giá quy trình làm việc của Sprint vừa qua. Đây là khoảng thời gian để đội ngũ tự phản ánh một cách thẳng thắn:

* Những gì đã làm tốt?  
* Những gì chưa hiệu quả?  
* Cần thay đổi hoặc cải tiến điều gì trong Sprint tiếp theo?

Buổi Retrospective giúp đội ngũ ngày càng hoàn thiện hơn, giảm thiểu sai sót và nâng cao hiệu suất làm việc, đặc biệt trong việc xử lý các vấn đề kỹ thuật phức tạp như tích hợp AI và hệ thống thời gian thực.

#### **6\. Increment – Sản phẩm tăng trưởng**

Sau mỗi Sprint, đội ngũ bàn giao một phần sản phẩm đã hoàn thiện gọi là **Increment**. Phần Increment này phải đáp ứng được tiêu chuẩn “Definition of Done” đã thống nhất từ đầu (ví dụ: đã kiểm thử, tích hợp, có tài liệu và sẵn sàng demo cho Ban quản lý).

Increment có thể được sử dụng ngay (ví dụ: module thanh toán QR Code có thể triển khai thử nghiệm tại một khu vực của bãi đỗ), mang lại giá trị rõ ràng và tiếp tục được phát triển trong các Sprint sau.

**7\. Chu kỳ lặp lại – Scrum là vòng lặp liên tục**

Quy trình Scrum không dừng lại sau một Sprint mà tiếp tục lặp lại theo chu kỳ:

* Cập nhật và sắp xếp lại Product Backlog  
* Sprint Planning cho Sprint mới  
* Thực hiện công việc và họp Daily Scrum  
* Sprint Review và Sprint Retrospective

Qua mỗi Sprint, hệ thống Quản lý Bãi đỗ xe Thông minh sẽ ngày càng hoàn thiện, ổn định và gần hơn với nhu cầu thực tế của Ban quản lý và người sử dụng.

 

## **1.3. The Proposed System**

Hệ thống Quản lý Bãi đỗ xe Thông minh là giải pháp tích hợp đa nền tảng, sử dụng công nghệ AI và IoT nhằm tự động hóa toàn bộ quy trình ra vào, thanh toán, giám sát an ninh và quản trị bãi đỗ xe. Hệ thống gồm sáu phân hệ chính: Customer App, Guard App, Admin Portal, AI Camera Subsystem, Virtual LED Dashboard và Pay-on-foot Kiosk.

Hệ thống phục vụ ba nhóm người dùng chính: **Khách hàng**, **Bảo vệ** và **Ban Quản Lý**. Dưới đây là các chức năng chi tiết được thiết kế dành riêng cho từng đối tượng.

### **Chức năng của Khách hàng (Customer)**

Đối với khách hàng (bao gồm khách vãng lai và khách lâu dài), hệ thống cung cấp các chức năng sau:

1. #### **Đăng nhập vào hệ thống**

   ○ Phương thức đăng nhập linh hoạt: Khách hàng có thể đăng nhập dễ dàng qua ứng dụng di động bằng số điện thoại hoặc email đã đăng ký.  
   ○ Xác thực sinh trắc học: Giao diện đăng nhập được thiết kế tối giản, thân thiện, hỗ trợ đăng nhập nhanh bằng vân tay hoặc khuôn mặt trên các thiết bị di động.  
   ○ Bảo mật hai lớp (2FA): Hệ thống áp dụng xác thực hai lớp (2FA) bằng mật khẩu kết hợp mã OTP gửi qua SMS hoặc email.  
   ➔ Tính năng này đảm bảo khách hàng có thể truy cập an toàn, nhanh chóng vào hệ thống để quản lý thông tin cá nhân và hoạt động đỗ xe của mình mọi lúc, mọi nơi.

2. #### **Quản lý thông tin cá nhân và nhóm khách hàng**

   ○ Cập nhật hồ sơ cá nhân: Khách hàng có thể xem, chỉnh sửa thông tin cá nhân (họ tên, số điện thoại, email, CCCD).  
   ○ Mô hình Nhóm khách hàng (Group Customer): Khách lâu dài có thể tạo và quản lý **Nhóm khách hàng (Group Customer)**, cho phép một tài khoản đại diện quản lý nhiều biển số xe (hộ gia đình hoặc doanh nghiệp).  
   ○ Quản lý danh sách phương tiện: Hỗ trợ thêm/xóa biển số xe vào nhóm và thiết lập vai trò chủ sở hữu (Owner) hoặc thành viên.  
   ➔ Tính năng này mang lại sự linh hoạt cao, giúp một tài khoản có thể quản lý nhiều phương tiện một cách tiện lợi và tập trung.

3. #### **Quản lý Booking và Booking Details**

   ○ Danh sách gói đăng ký: Xem danh sách tất cả các gói đăng ký (Booking) đang hoạt động và đã hết hạn.  
   ○ Xem chi tiết Booking Details: biển số xe thuộc gói nào, thời hạn hiệu lực, loại gói cước, số tiền đã thanh toán, ngày gia hạn.  
   ○ Gia hạn trực tuyến: Hỗ trợ gia hạn gói đăng ký trực tiếp trên ứng dụng.  
   ➔ Khách hàng có thể chủ động theo dõi và quản lý tình trạng các gói thuê chỗ đỗ xe dài hạn một cách dễ dàng và minh bạch.

4. #### **Xem lịch sử đỗ xe (Parking Session)**

   ○ Lịch sử chi tiết: Khách hàng tra cứu toàn bộ lịch sử ra vào của từng phương tiện thuộc tài khoản.  
   ○ Bằng chứng hình ảnh: Mỗi phiên đỗ xe hiển thị đầy đủ thông tin: thời gian vào/ra, thời lượng đỗ, chi phí phát sinh (nếu có), hình ảnh log từ camera và trạng thái thanh toán.  
   ○ Bộ lọc thông minh: Hỗ trợ lọc theo khoảng thời gian hoặc theo từng biển số xe.  
   ➔ Tính năng này giúp khách hàng dễ dàng kiểm tra, đối chiếu và lưu trữ lịch sử đỗ xe một cách chính xác và tiện lợi.

5. #### **Quản lý thanh toán và lịch sử giao dịch**

   ○ Chi tiết hóa đơn: Xem chi tiết các khoản thanh toán theo từng phiên đỗ xe (vãng lai) hoặc theo gói đăng ký (lâu dài).  
   ○ Thanh toán QR động (VietQR): Hỗ trợ thanh toán nhanh qua mã QR động (VietQR).  
   ○ Trạng thái thanh toán minh bạch: Lưu trữ đầy đủ lịch sử thanh toán với trạng thái (Đã thanh toán / Chưa thanh toán / Quá hạn).  
   ➔ Khách hàng có thể quản lý tài chính cá nhân liên quan đến đỗ xe một cách khoa học, minh bạch và chủ động.

6. #### **Gửi khiếu nại / phản hồi**

   ○ Khởi tạo khiếu nại: Khách hàng có thể tạo khiếu nại trực tiếp trên ứng dụng kèm theo mô tả vấn đề, hình ảnh minh họa và vị trí xe.  
   ○ Theo dõi tiến độ: Theo dõi trạng thái xử lý khiếu nại (Đã tiếp nhận, Đang xử lý, Đã hoàn tất).  
   ➔ Tạo kênh phản hồi nhanh chóng, giúp khách hàng dễ dàng gửi góp ý hoặc khiếu nại đến Ban quản lý một cách chuyên nghiệp.  
   

### **Chức năng của Bảo vệ (Security / Guard)**

Đối với bảo vệ hiện trường, hệ thống cung cấp ứng dụng Guard App với các chức năng hỗ trợ công việc tại chỗ như sau:

1. #### **Đăng nhập vào Guard App**

   ○ Bảo vệ đăng nhập bằng tài khoản được cấp để truy cập các chức năng vận hành thực địa.

2. #### **Quản lý và xử lý Parking Session**

   ○ Giám sát thời gian thực: Xem danh sách các phiên đỗ xe đang hoạt động thời gian thực.  
   ○ Xử lý ngoại lệ (Manual Override): Hỗ trợ cập nhật thủ công khi AI không nhận diện được biển số (Manual Override), kèm theo chụp ảnh hiện trường.  
   ○ Ghi log can thiệp: Ghi log đầy đủ mọi thao tác can thiệp thủ công.

3. #### **Xử lý thanh toán tiền mặt**

   ○ Ghi nhận thu phí: Bảo vệ ghi nhận thanh toán tiền mặt từ khách vãng lai.  
   ○ Điều khiển Barie: Bấm nút “Đã thu tiền” trên ứng dụng để mở barie tự động.  
   ○ Đối soát cuối ca: Hệ thống tự động ghi nhận và lưu log giao dịch để đối soát cuối ca.

4. #### **Tra cứu thông tin Booking và Booking detail**

   ○ Tra cứu bookings: Tra cứu nhanh danh sách và chi tiết Booking của khách lâu dài.  
   ○ Tra cứu booking details: Xem Booking Details để kiểm tra thông tin gói đăng ký, biển số xe và thời hạn sử dụng khi hỗ trợ tại cổng.

5. #### **Tra cứu thông tin thiết bị phần cứng**

   ○ Quản lý danh sách thiết bị: Xem danh sách và tình trạng hoạt động của các thiết bị IoT (Camera, Barrier, LED Dashboard) tại bãi đỗ.  
   ○ Xử lý sự cố: Hỗ trợ báo cáo sự cố thiết bị khi phát hiện thiết bị bị lỗi hoặc mất kết nối.

6. #### **Nhận và xử lý khiếu nại thời gian thực**

   ○ Hệ thống đẩy thông báo (Push Notification): Nhận thông báo tức thì khi có phát sinh khiếu nại.  
   ○ Xác thực vị trí vi phạm: Xem hình ảnh và thông tin vị trí xe vi phạm trực tiếp trên ứng dụng.  
   ○ Đánh dấu trạng thái xử lý (Đã giải quyết).  
   ➔ Tăng cường khả năng phản ứng nhanh với các sự cố phát sinh, đảm bảo trật tự và an toàn cho bãi đỗ xe.

➔ Guard App giúp bảo vệ làm việc chuyên nghiệp hơn, giảm thời gian xử lý thủ công, tăng tốc độ phản ứng với sự cố và đảm bảo lưu thông thông suốt tại bãi đỗ.

### **Chức năng của Ban Quản Lý (Admin)**

Ban Quản Lý sử dụng **Admin Portal** (Web) để thực hiện công việc quản trị tổng thể với các chức năng chi tiết sau:

1. #### **Quản lý Khách hàng và Nhóm Khách hàng (Group Customer & Group Profile)**

   ○ Quản trị danh sách: Quản lý toàn bộ danh sách khách hàng cá nhân và nhóm khách hang, tạo mới, chỉnh sửa, xóa khách hàng và nhóm khách hàng.  
   ○ Quản lý Group Profile: Quản lý chi tiết Group Profile (thông tin nhóm, chủ sở hữu, hạn mức, trạng thái).  
   ○ Phân quyền chủ nhóm: Thêm/xóa phương tiện vào nhóm và phân quyền chủ sở hữu.

2. #### **Quản lý Booking và Booking Details**

   ○ Quản lý danh sách: Xem danh sách, tìm kiếm Booking và Booking Details.  
   ○ Quản lý chi tiết Booking: Tạo, chỉnh sửa, gia hạn và hủy các gói đăng ký thuê chỗ đỗ xe.  
   ○ Quản lý chi tiết Booking Details: biển số xe, thời hạn gói, loại phương tiện, trạng thái thanh toán

3. #### **Quản lý Gói cước (Package) và Biểu giá**

   ○ Thiết lập gói dịch vụ: Thiết lập và quản lý các gói cước theo tháng/quý/năm.  
   ○ Cấu hình biểu giá động: Tạo mới, chỉnh sửa và xóa gói cước.    
   ○ Quản lý loại xe: Cấu hình biểu giá động cho khách vãng lai theo loại xe, khung giờ, ngày lễ, giờ cao điểm.

4. #### **Quản lý Loại xe (Vehicle Type)**

   ○ Quản lý danh sách: Quản lý danh sách các loại xe (xe máy, ô tô con, ô tô tải…).  
   ○ Quản lý chi tiết loại xe: Thêm, sửa, xóa loại xe và cấu hình hệ số tính phí tương ứng.

5. #### **Quản lý Parking Session**

   ○ Quản lý danh sách Parking session: Xem danh sách, tìm kiếm và theo dõi các phiên đỗ xe (Parking Session).  
   ○ Cập nhật thông tin: Hỗ trợ cập nhật thủ công một số thông tin khi cần thiết (Ban quản lý không được phép tạo mới Parking Session – hệ thống tự động tạo qua AI và Backend).  

6. #### **Quản lý Không gian và Zone**

   ○ Số hóa bãi đỗ: Phân chia bãi đỗ thành các khu vực (Zone) theo tầng, vị trí và loại phương tiện.  
   ○ Giám sát tỷ lệ lấp đầy: Giám sát số chỗ trống theo thời gian thực tại từng Zone.  
   ○ Điều hướng thông minh: Cấu hình thông tin hiển thị trên các bảng LED dựa trên số chỗ trống thực tế để phân luồng xe hiệu quả.

7. #### **Quản lý Thiết bị IoT và Phần cứng**

   ○ Quản lý danh sách thiết bị: Giám sát tình trạng hoạt động (Online/Offline) của toàn bộ Camera AI, Barrier, LED Dashboard.  
   ○ Cấu hình thiết bị: Cấu hình và gán thiết bị vào các Zone cụ thể.

8. #### **Quản lý Thanh toán và Doanh thu**

   ○ Báo cáo tổng hợp: Xuất báo cáo doanh thu chi tiết theo nhiều tiêu chí (theo ngày, theo loại khách hàng, theo phương thức thanh toán).  
   ○ Theo dõi nợ đọng: Quản lý danh sách các khách hàng chưa thanh toán hoặc các gói cước sắp hết hạn để hệ thống tự động nhắc nợ.  
   ○ Đối soát tài chính: Cung cấp dữ liệu chính xác để đối chiếu giữa tiền mặt bảo vệ thu và các giao dịch chuyển khoản qua hệ thống.

9. #### **Quản lý Khiếu nại**

   ○ Xử lý khiếu nại: Xem, phân công, cập nhật và theo dõi tình trạng xử lý toàn bộ khiếu nại từ khách hàng.

10. #### **Quản lý Nhân viên và Phân quyền**

    ○ Quản lý tài khoản nhân sự: Quản lý tài khoản nhân viên bảo vệ.  
    ○ Phân quyền chi tiết (RBAC): Thiết lập vai trò (Role) và phân quyền chi tiết cho từng chức năng trong hệ thống.

11. #### **Cấu hình Tham số Hệ thống Động**

    ○ Thời gian ân hạn (Grace Period): Thiết lập khoảng thời gian khách được phép ở lại bãi sau khi thanh toán mà không bị tính thêm phí.  
    ○ Hệ số an toàn (Safety Buffer): Cấu hình tỷ lệ chỗ trống dự phòng dành riêng cho khách thuê bao để đảm bảo luôn có chỗ cho khách ký hợp đồng dài hạn.  
    ○ Quy tắc vận hành: Cập nhật các thông số hệ thống mà không cần tắt máy chủ, giúp bãi xe vận hành liên tục 24/7.

12. #### **Quản lý Thiết lập AI và Buffer Dự phòng**

    ○ Thiết lập AI: Cấu hình ngưỡng dự báo lấp đầy và quản lý vùng đệm an toàn.  
    ○ Hiệu suất AI: Giám sát hiệu suất mô hình AI nhận diện biển số.

➔ Thông qua Admin Portal, Ban Quản Lý có thể kiểm soát toàn diện hoạt động của bãi đỗ xe, tối ưu hóa nguồn lực, tăng doanh thu bền vững và nâng cao chất lượng dịch vụ một cách chuyên nghiệp và hiện đại.

 

### **Chức năng tự động của AI và hệ thống**

1. Tạo mới parking session  
2.  **thông báo và nhắc nợ tự động**  
   ○ Nhắc hạn gói cước: Nhận thông báo đẩy khi gói đăng ký sắp hết hạn (5 ngày, 3 ngày, 1 ngày trước hạn).  
   ○ Cảnh báo an ninh: Nhận cảnh báo an ninh khi xe bị phát hiện đỗ sai vị trí hoặc có hành vi bất thường.

➔ Tính năng này giúp khách hàng chủ động nắm bắt thông tin, tránh phát sinh phí phạt và gián đoạn dịch vụ.

 

**Automatic System Functions**

1. #### **Tạo phiên đỗ xe tự động**

   Hệ thống tự động tạo Phiên đỗ xe mới khi có xe vào bãi đỗ bằng AI Camera để nhận dạng biển số và phân loại xe. Quá trình này yêu cầu sự can thiệp tối thiểu của con người, đảm bảo ghi lại phương tiện ra vào nhanh chóng và chính xác.

2. #### **Hệ thống thông báo và nhắc nợ tự động**

   Hệ thống tự động gửi thông báo đẩy và lời nhắc tới khách hàng về việc hết hạn đăng ký, hóa đơn chưa thanh toán và các sự kiện quan trọng khác. Thông báo được gửi qua Ứng dụng khách hàng (thông báo đẩy) và có thể được định cấu hình với nhiều cấp độ cảnh báo (5 ngày, 3 ngày và 1 ngày trước khi hết hạn).

### **1.3.1. Boundaries of the System**

Hệ thống Quản lý Bãi đỗ xe Thông minh là giải pháp công nghệ toàn diện, được xây dựng nhằm giải quyết các hạn chế của mô hình quản lý bãi đỗ xe truyền thống. Hệ thống sử dụng sự kết hợp giữa trí tuệ nhân tạo (AI), Internet of Things (IoT), nhận diện hình ảnh và các công nghệ thanh toán hiện đại để tự động hóa quy trình ra vào, tính phí, thanh toán, giám sát an ninh và quản trị vận hành.

Phạm vi của hệ thống tập trung vào việc quản lý một hoặc nhiều bãi đỗ xe thuộc cùng một đơn vị quản lý (chung cư, tòa nhà văn phòng, trung tâm thương mại, khu đô thị, bệnh viện…). Hệ thống không phải là giải pháp quản lý giao thông thông minh toàn khu vực hay thành phố, mà chỉ giới hạn trong không gian của bãi đỗ xe cố định.

Hệ thống hỗ trợ hai hình thức sử dụng chính: 

**Khách vãng lai (pay-per-use):** Thanh toán theo thời gian đỗ xe thực tế.

**Khách thuê bao dài hạn (monthly/quarterly package):** Đăng ký gói cước theo tháng hoặc quý, cho phép quản lý nhiều phương tiện trong cùng một tài khoản nhóm.

**Công nghệ cốt lõi** được sử dụng trong hệ thống bao gồm: 

* Nhận diện biển số xe tự động bằng AI Camera  
* Xử lý hình ảnh và phân loại phương tiện (xe máy, ô tô con, ô tô tải…)  
* Hệ thống Barrier tự động tích hợp IoT  
* Thanh toán không tiền mặt qua QR Code (VietQR)  
* Bảng LED hiển thị thông tin chỗ trống thời gian thực  
* Kiến trúc Microservices kết hợp với cơ sở dữ liệu thời gian thực


 **Giới hạn rõ ràng của hệ thống:** 

Hệ thống **không** quản lý việc tìm kiếm chỗ đỗ xe bên ngoài bãi đỗ (không phải hệ thống đỗ xe thông minh toàn thành phố).

Hệ thống **không** tự động thực thi các hình thức phạt vật lý như khóa bánh xe hay chặn xe. Tất cả các trường hợp vi phạm chỉ dừng ở mức cảnh báo và hỗ trợ bảo vệ xử lý thủ công.

Hệ thống vẫn yêu cầu sự hiện diện của bảo vệ tại hiện trường để xử lý ngoại lệ, thu tiền mặt và giải quyết các tình huống thực tế phát sinh.

Phạm vi hiện tại tập trung vào bãi đỗ xe cố định có lắp đặt hạ tầng Camera AI và Barrier, chưa hỗ trợ mô hình đỗ xe lưu động hoặc đỗ xe đường phố. 

**Lợi ích tổng quát của việc triển khai hệ thống:**

Việc áp dụng Hệ thống Quản lý Bãi đỗ xe Thông minh mang lại nhiều lợi ích thiết thực cho các bên liên quan. Đối với Ban quản lý, hệ thống giúp tối ưu hóa công suất đỗ xe, tăng doanh thu, giảm chi phí nhân sự và nâng cao khả năng quản trị dữ liệu. Đối với khách hàng, hệ thống mang đến trải nghiệm đỗ xe nhanh chóng, tiện lợi, minh bạch và an toàn hơn. Đối với bảo vệ, hệ thống giảm tải công việc thủ công, hỗ trợ ra quyết định nhanh chóng thông qua cảnh báo thời gian thực. Tổng thể, hệ thống góp phần xây dựng mô hình bãi đỗ xe thông minh, chuyên nghiệp, hướng tới mục tiêu phát triển đô thị thông minh tại Việt Nam.

**Các đối tượng sử dụng chính trong hệ thống:** 

**Khách hàng**: Sử dụng Customer App để quản lý phương tiện, thanh toán, theo dõi lịch sử đỗ xe và nhận thông báo.

**Bảo vệ (Guard):** Sử dụng Guard App để hỗ trợ xử lý ngoại lệ, thu tiền mặt, nhận cảnh báo và ghi log sự cố.

**Ban Quản Lý (Admin):** Sử dụng Admin Portal để quản trị tổng thể hệ thống, cấu hình giá cả, quản lý khách hàng, theo dõi doanh thu và giám sát thiết bị.

Hệ thống được xây dựng với nguyên tắc cân bằng giữa mức độ tự động hóa cao và sự linh hoạt cần thiết trong thực tế vận hành của các bãi đỗ xe tại Việt Nam hiện nay.

 

### **1.3.2. Hardware and Software Requirements**

**Client-Side Requirements**

**Mobile Devices:**

* CPU: Any dual-core 1.0 GHz or higher  
* RAM: 1 GB  
* Storage: 8 GB (just for OS and browser cache)  
* Display: 480×800 or higher  
* Operating System: Android 6.0+ or iOS 10+  
* Browser: Any modern browser (Chrome, Firefox, Safari, Edge)

**Desktop / Laptop:**

* CPU: Any dual-core 1.5 GHz or higher  
* RAM: 2 GB  
* Storage: 20 GB (for OS, browser, and temporary files)  
* GPU: Integrated graphics  
* Display: 1024×768 or higher  
* Operating System: Windows 7+, macOS 10.12+, or Linux (any modern distribution)  
* Browser: Modern browser with HTML5/CSS3/JS support

\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_

**Server-Side Requirements**

* Minimum CPU: Intel Xeon E-2236 / AMD EPYC 7302  
* Recommended CPU: Intel Xeon Gold 6248R / AMD EPYC 7742  
* Minimum RAM: 16 GB  
* Recommended RAM: 64 GB  
* Minimum Storage: SSD 100 GB  
* Recommended Storage: SSD 1 TB  
* Network (Minimum): 1 Gbps Ethernet  
* Network (Recommended): 10 Gbps Ethernet

 

# 

# **2\. Customer Requirements Specification**

## **2.1. Users of the System**

Hệ thống Quản lý Bãi đỗ xe Thông minh được thiết kế để phục vụ đa dạng đối tượng người dùng với các nhu cầu và mức độ tương tác khác nhau. Việc phân tích rõ ràng các đối tượng sử dụng là cơ sở quan trọng để xây dựng chức năng phù hợp, giao diện thân thiện và quyền hạn hợp lý cho từng nhóm.

Hệ thống xác định ba nhóm người dùng chính sau:

1. **Khách hàng (Customer)** Đây là nhóm người dùng cuối cùng và cũng là nhóm đông đảo nhất của hệ thống, bao gồm hai loại hình chính:  
   * **Khách vãng lai**: Những người sử dụng dịch vụ đỗ xe theo lượt, thanh toán dựa trên thời gian đỗ xe thực tế. Họ thường chỉ sử dụng hệ thống trong thời gian ngắn, tập trung vào việc thanh toán nhanh chóng và tiện lợi.  
   * **Khách lâu dài (Long-term Customer)**: Những cá nhân, hộ gia đình hoặc doanh nghiệp đăng ký gói thuê chỗ đỗ xe theo tháng hoặc quý. Một tài khoản có thể quản lý nhiều phương tiện khác nhau. Nhóm này có nhu cầu cao về việc theo dõi tình trạng gói đăng ký, lịch sử đỗ xe, thanh toán định kỳ và nhận thông báo nhắc nợ.

   Khách hàng tương tác chủ yếu qua **Customer App** trên điện thoại di động. Họ cần giao diện đơn giản, dễ sử dụng, tính năng thanh toán nhanh và khả năng tra cứu thông tin bất kỳ lúc nào.

2. **Bảo vệ (Guard)** Bảo vệ là lực lượng vận hành trực tiếp tại hiện trường. Họ chịu trách nhiệm hỗ trợ xử lý các tình huống ngoại lệ, thu tiền mặt (khi khách không thanh toán qua QR Code), tiếp nhận và xử lý cảnh báo từ hệ thống AI, cũng như duy trì trật tự chung của bãi đỗ xe.  
   Bảo vệ sử dụng **Guard App** trên điện thoại hoặc máy tính bảng. Họ cần một giao diện đơn giản, tốc độ xử lý nhanh, khả năng nhận thông báo thời gian thực và công cụ hỗ trợ cập nhật thủ công khi AI gặp lỗi. Công việc của bảo vệ đòi hỏi tính thực tiễn cao, vì họ phải làm việc ngoài trời trong nhiều điều kiện thời tiết khác nhau.  
3. **Ban Quản Lý (Admin)** Đây là nhóm người dùng có quyền hạn cao nhất trong hệ thống. Ban Quản Lý bao gồm các cá nhân hoặc bộ phận chịu trách nhiệm vận hành tổng thể bãi đỗ xe, bao gồm: quản lý khách hàng, thiết lập chính sách giá cả, theo dõi doanh thu, quản lý thiết bị phần cứng, cấu hình hệ thống và xử lý khiếu nại.  
   Ban Quản Lý sử dụng **Admin Portal** qua giao diện web trên máy tính. Họ cần công cụ quản trị mạnh mẽ, khả năng xem báo cáo chi tiết, cấu hình tham số động và quản lý phân quyền cho nhân viên. Nhóm này tập trung vào việc tối ưu hóa hiệu quả kinh doanh, kiểm soát rủi ro và nâng cao chất lượng dịch vụ.

**Mối quan hệ giữa các nhóm người dùng:**

* Khách hàng tương tác trực tiếp với hệ thống qua ứng dụng di động và gián tiếp qua bảo vệ khi cần hỗ trợ tại chỗ.  
* Bảo vệ đóng vai trò cầu nối giữa khách hàng và hệ thống, xử lý các tình huống thực tế mà AI chưa thể tự xử lý hoàn toàn.  
* Ban Quản Lý đứng ở vị trí trung tâm, quản trị toàn bộ hệ thống, giám sát hoạt động của bảo vệ và đảm bảo trải nghiệm tốt nhất cho khách hàng.

Việc phân loại rõ ràng ba nhóm người dùng này giúp hệ thống được thiết kế với giao diện và chức năng phù hợp, đảm bảo tính dễ sử dụng, an toàn và hiệu quả vận hành tổng thể.

 

## **2.2. System functions**

### **2.2.1. Chức năng của khách hang**

1\. Authentication & Authorization

* Login

| Thông tin đầu vào | Tên đăng nhập (username, phone, email), password |
| :---- | :---- |
| Cách thức xử lý | Xác thực thông tin và cấp session/token |

* Logout

| Thông tin đầu vào | Session hiện tại |
| :---- | :---- |
| Cách thức xử lý | Hủy session hiện tại, đăng xuất tài khoản ra khỏi hệ thống |

* Forget password

| Thông tin đầu vào | Email/phone |
| :---- | :---- |
| Thông tin đầu ra | Password mới đã được cập nhật |
| Cách thức xử lý | ·  Nhập email/phone ·  Xác thực email/phone có tồn tại và hợp lệ ·  Gửi mã OTP qua email/SMS ·  Nhập mã OTP ·  Đặt mật khẩu mới |
| Dữ liệu cần lưu trữ | Password mới |


* Change password

| Thông tin đầu vào | Password cũ |
| :---- | :---- |
| Thông tin đầu ra | Password mới đã được cập nhật |
| Cách thức xử lý | ·  Nhập password cũ và password mới ·  Xác thực tính hợp lệ của password cũ, password mới ·  Đổi mật khẩu mới |
| Dữ liệu cần lưu trữ | Password mới |

 

2\. Quản lý Customers, Groups Customers

* View Detail Customer by ID

| Thông tin đầu vào | Customer ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của khách hàng bao gồm Username, Fullname, Identity Number, Phone, Address, Email, Nhóm khách hàng |
| Cách thức xử lý | ·  Hệ thống truy vấn dữ liệu khách hang, lọc bằng Customer ID ·  Hiển thị đầy đủ thông tin customer |

* Update Customer

| Thông tin đầu vào | Customer ID, thông tin cần chỉnh sửa |
| :---- | :---- |
| Thông tin đầu ra | Customer đã được cập nhật, thông báo thành công |
| Cách thức xử lý | ·  Hệ thống truy vấn dữ liệu khách hang, lọc bằng Customer ID ·  Khách hang chỉnh sửa các trường cho phép ·  Hệ thống kiểm tra điều kiện trước khi lưu. ·  Cập nhật dữ liệu vào cơ sở dữ liệu và ghi log thay đổi. |
| Dữ liệu cần lưu trữ | Lịch sử chỉnh sửa thông tin customer, thông tin các trường đã thay đổi |

* View Detail Groups Customer by Customer ID

| Thông tin đầu vào | Customer ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của nhóm khách hàng bao gồm Tên nhóm, chủ nhóm |
| Cách thức xử lý | ·  Hệ thống truy vấn dữ liệu, lọc bằng Customer ID ·  Hiển thị đầy đủ thông tin group customers |

* Update Group Customer, áp dụng đối với customer là owner của group

| Thông tin đầu vào | Customer ID, danh sách customer trong group cần chỉnh sửa |
| :---- | :---- |
| Thông tin đầu ra | Danh sách Group Customer đã được cập nhật, thông báo thành công |
| Cách thức xử lý | ·  Hệ thống truy vấn dữ liệu, lọc bằng Customer ID ·  Owner Groups chỉnh sửa danh sách Customer trong groups ·  Hệ thống kiểm tra điều kiện trước khi lưu. ·  Cập nhật dữ liệu vào cơ sở dữ liệu và ghi log thay đổi. |
| Dữ liệu cần lưu trữ | Lịch sử chỉnh sửa group customer, danh sách customers đã thay đổi |

 

3\. Quản lý Bookings và Booking Details

* View List Bookings

| Thông tin đầu vào | Customer ID |
| :---- | :---- |
| Thông tin đầu ra | Danh sách các booking, bao gồm các thông tin: khách hàng, số lượng xe, thời hạn, trạng thái, ngày tạo |
| Cách thức xử lý | ·  Hệ thống truy vấn cơ sở dữ liệu, lọc bằng Customer ID và trả về kết quả ·  Hỗ trợ sắp xếp theo ngày tạo hoặc trạng thái. |

* View Detail Booking by ID

| Thông tin đầu vào | Booking ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của booking bao gồm danh sách Booking Details, tổng quan gói cước, trạng thái thanh toán |
| Cách thức xử lý | ·  Customer chọn một Booking từ danh sách hoặc nhập trực tiếp Booking ID. ·  Hiển thị đầy đủ thông tin booking cùng danh sách các xe thuộc booking. |

* View List Booking Details

| Thông tin đầu vào | Customer ID |
| :---- | :---- |
| Thông tin đầu ra | Danh sách các booking details, bao gồm các thông tin: thông tin xe, thời hạn, trạng thái, ngày tạo |
| Cách thức xử lý | ·  Hệ thống truy vấn cơ sở dữ liệu, lọc bằng Customer ID và trả về kết quả ·  Hỗ trợ sắp xếp theo ngày tạo hoặc trạng thái. |

* View Detail Booking Details by ID

| Thông tin đầu vào | Booking detail ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của booking detail bao gồm khách hàng, thông tin xe, thời hạn, trạng thái, ngày tạo |
| Cách thức xử lý | ·  Customer chọn một booking detail từ danh sách hoặc nhập trực tiếp booking detail ID. ·  Hệ thống truy vấn và hiển thị đầy đủ thông tin booking detail |

* Create Booking detail, áp dụng khi khách hang gia hạn đăng ký gói

| Thông tin đầu vào | Booking ID, Customer ID, loại xe, biển số xe, giá cước, thời hạn |
| :---- | :---- |
| Thông tin đầu ra | Booking detail ID mới được tạo, thông báo thành công |
| Cách thức xử lý | ·  Customer chọn Booking detail cần gia hạn ·  Hệ thống truy vấn dữ liệu, tự động điền thông tin cho booking detail mới theo booking detail cũ ·  Khách hang chỉnh sửa lại các thông tin cần thiết ·  Hệ thống kiểm tra tính hợp lệ (khách hàng tồn tại, không vượt quá số lượng gói cước cho phép…). ·  Tạo booking detail. |
| Dữ liệu cần lưu trữ | Mã booking, ID khách hàng, loại xe, biển số xe, giá cước, thời hạn, thời gian bắt đầu, thời gian kết thúc, tổng tiền, trạng thái |

 

4\. Quản lý Parking Sessions

* View List Parking Sessions

| Thông tin đầu vào | Customer ID |
| :---- | :---- |
| Thông tin đầu ra | Danh sách các Parking Sessions dưới dạng bảng, bao gồm các cột: Parking Session ID, biển số xe, loại xe, Booking Detail ID, thời gian đỗ xe, tiền vé |
| Cách thức xử lý | ·  Hệ thống truy vấn cơ sở dữ liệu, lọc bằng Customer ID và trả về kết quả theo phân trang. ·  Hỗ trợ sắp xếp theo ngày tạo hoặc trạng thái. |


* View Detail Parking Session by ID

| Thông tin đầu vào | Parking Session ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của Parking Session bao gồm Parking Session ID, biển số xe, loại xe, Booking Detail ID, thời gian đỗ xe, tiền vé |
| Cách thức xử lý | ·  Customer chọn một Parking Session từ danh sách hoặc nhập trực tiếp Parking Session ID. ·  Hệ thống truy vấn và hiển thị đầy đủ thông tin Parking Session |

 

5\. Quản lý thanh toán (Payment)

*   **View Payment Details**

| Thông tin đầu vào | Payment ID hoặc target ID (Booking Detail ID / Parking Session ID) |
| :---- | :---- |
| Thông tin đầu ra | Chi tiết thông tin giao dịch, trạng thái thanh toán, mã QR code để quét thanh toán |
| Cách thức xử lý | · Hệ thống truy vấn thông tin giao dịch cần thanh toán.<br>· Tạo mã QR thanh toán qua VietQR.<br>· Hiển thị màn hình quét QR cho khách hàng. |

*   **Process Online Payment (VietQR & SePay)**

| Thông tin đầu vào | Lựa chọn thanh toán trực tuyến từ khách hàng |
| :---- | :---- |
| Thông tin đầu ra | Cập nhật trạng thái `is_paid = true` cho Parking Session hoặc `Status = Active` cho Booking Detail gia hạn |
| Cách thức xử lý | · **Khởi tạo:** Hệ thống sinh mã VietQR với số tiền tương ứng và nội dung chuyển khoản duy nhất (PayCode).<br>· **Thanh toán:** Khách hàng sử dụng ứng dụng Ngân hàng quét mã QR để chuyển khoản.<br>· **Xác nhận (Webhook):** Dịch vụ trung gian (SePay) ghi nhận biến động số dư tài khoản của Ban quản lý, gửi Webhook về Backend.<br>· **Cập nhật:** Backend đối chiếu PayCode, xác nhận giao dịch thành công và cập nhật trạng thái đơn hàng (Gia hạn gói cước hoặc thanh toán phí đỗ xe).<br>· **Hiển thị:** Frontend Customer tự động nhận tín hiệu thành công và hiển thị thông báo. |
| Dữ liệu cần lưu trữ | Bản ghi trong bảng `Payment` và `Payment_Details`, mã giao dịch, số tiền, ngày giờ, trạng thái. |

6\. Quản lý Complaints

* View List Complaints

| Thông tin đầu vào | Customer ID |
| :---- | :---- |
| Thông tin đầu ra | Danh sách các Complaints dưới dạng bảng |
| Cách thức xử lý | ·  Hệ thống truy vấn dữ liệu, lọc bằng Customer ID·  Hiển thị danh sách Complaints với phân trang và lọc theo trạng thái |

* View Detail Complaint by ID

| Thông tin đầu vào | Complaint ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của Complaint bao gồm nội dung khiếu nại, hình ảnh/video, lịch sử xử lý |
| Cách thức xử lý | ·  Customer chọn một Complaint từ danh sách ·  Hệ thống truy vấn và hiển thị đầy đủ thông tin Complaint. |

* Create Complaint

| Thông tin đầu vào | Nội dung Complaint, hình ảnh/video |
| :---- | :---- |
| Thông tin đầu ra | Complaint ID mới được tạo, thông báo thành công |
| Cách thức xử lý | ·  Guard nhập thông tin complaint ·  Hệ thống kiểm tra tính hợp lệ ·  Tạo complaint. |
| Dữ liệu cần lưu trữ | Complaint ID, thông tin Complaint |

* Update Complaint

| Thông tin đầu vào | Complaint ID, thông tin cần chỉnh sửa |
| :---- | :---- |
| Thông tin đầu ra | Complaint đã được cập nhật, thông báo thành công |
| Cách thức xử lý | ·  Guard chọn Complaint cần chỉnh sửa. ·  Chỉnh sửa các trường cho phép ·  Hệ thống kiểm tra điều kiện trước khi lưu. ·  Cập nhật dữ liệu vào cơ sở dữ liệu và ghi log thay đổi. |
| Dữ liệu cần lưu trữ | Lịch sử chỉnh sửa Complaint, thông tin các trường đã thay đổi |

 

### **2.2.2. Chức năng của bảo vệ**

1\. Authentication & Authorization

* Login

| Thông tin đầu vào | Tên đăng nhập (username, phone, email), password |
| :---- | :---- |
| Cách thức xử lý | Xác thực thông tin và cấp session/token |

* Logout

| Thông tin đầu vào | Session hiện tại |
| :---- | :---- |
| Cách thức xử lý | Hủy session hiện tại, đăng xuất tài khoản ra khỏi hệ thống |

* Forget password

| Thông tin đầu vào | Email/phone |
| :---- | :---- |
| Thông tin đầu ra | Password mới đã được cập nhật |
| Cách thức xử lý | ·  Nhập email/phone ·  Xác thực email/phone có tồn tại và hợp lệ ·  Gửi mã OTP qua email/SMS ·  Nhập mã OTP ·  Đặt mật khẩu mới |
| Dữ liệu cần lưu trữ | Password mới |

* Change password

| Thông tin đầu vào | Password cũ |
| :---- | :---- |
| Thông tin đầu ra | Password mới đã được cập nhật |
| Cách thức xử lý | ·  Nhập password cũ và password mới ·  Xác thực tính hợp lệ của password cũ, password mới ·  Đổi mật khẩu mới |
| Dữ liệu cần lưu trữ | Password mới |

 

2\. Quản lý Parking Sessions

* View List Parking Sessions

| Thông tin đầu vào | Tiêu chí tìm kiếm |
| :---- | :---- |
| Thông tin đầu ra | Danh sách các Parking Sessions dưới dạng bảng, bao gồm các cột: Parking Session ID, biển số xe, loại xe, Booking Detail ID, thời gian đỗ xe, tiền vé |
| Cách thức xử lý | ·  Guard nhập tiêu chí tìm kiếm hoặc để trống để xem tất cả. ·  Hệ thống truy vấn cơ sở dữ liệu và trả về kết quả theo phân trang. ·  Hỗ trợ sắp xếp theo ngày tạo hoặc trạng thái. |

* View Detail Parking Session by ID

| Thông tin đầu vào | Parking Session ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của Parking Session bao gồm Parking Session ID, biển số xe, loại xe, Booking Detail ID, thời gian đỗ xe, tiền vé |
| Cách thức xử lý | ·  Guard chọn một Parking Session từ danh sách hoặc nhập trực tiếp Parking Session ID. ·  Hệ thống truy vấn và hiển thị đầy đủ thông tin Parking Session |

* Update Parking Session

| Thông tin đầu vào | Parking Session ID, thông tin cần chỉnh sửa |
| :---- | :---- |
| Thông tin đầu ra | Parking Session đã được cập nhật, thông báo thành công |
| Cách thức xử lý | ·  Guard chọn Parking Session cần chỉnh sửa. ·  Chỉnh sửa các trường cho phép ·  Hệ thống kiểm tra điều kiện trước khi lưu. ·  Cập nhật dữ liệu vào cơ sở dữ liệu và ghi log thay đổi. |
| Dữ liệu cần lưu trữ | Lịch sử chỉnh sửa Parking Session, thông tin các trường đã thay đổi |

 

3\. Quản lý Thanh toán

 

4\. Quản lý Bookings và Booking Details

* View List Bookings

| Thông tin đầu vào | Tiêu chí tìm kiếm (Booking ID, tên nhóm khách hàng, trạng thái booking) |
| :---- | :---- |
| Thông tin đầu ra | Danh sách các booking dưới dạng bảng, bao gồm các cột: Booking ID, khách hàng, số lượng xe, trạng thái, ngày tạo |
| Cách thức xử lý | ·  Admin nhập tiêu chí tìm kiếm hoặc để trống để xem tất cả. ·  Hệ thống truy vấn cơ sở dữ liệu và trả về kết quả theo phân trang. ·  Hỗ trợ sắp xếp theo ngày tạo hoặc trạng thái. |

* View Detail Booking by ID

| Thông tin đầu vào | Booking ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của booking bao gồm danh sách Booking Details, tổng quan gói cước, trạng thái thanh toán |
| Cách thức xử lý | ·  Admin chọn một booking từ danh sách hoặc nhập trực tiếp booking ID. ·  Hệ thống truy vấn và hiển thị đầy đủ thông tin booking cùng danh sách các xe thuộc booking. |

* View List Booking Details

| Thông tin đầu vào | Tiêu chí tìm kiếm (Booking ID, tên khách hàng, trạng thái booking, khoảng thời gian tạo) |
| :---- | :---- |
| Thông tin đầu ra | Danh sách các booking details dưới dạng bảng, bao gồm các cột: Booking ID, khách hàng, thông tin xe, thời hạn, trạng thái, ngày tạo |
| Cách thức xử lý | ·  Admin nhập tiêu chí tìm kiếm hoặc để trống để xem tất cả. ·  Hệ thống truy vấn cơ sở dữ liệu và trả về kết quả theo phân trang. ·  Hỗ trợ sắp xếp theo ngày tạo hoặc trạng thái. |


  

* View Detail Booking Details by ID

| Thông tin đầu vào | Booking detail ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của booking detail bao gồm khách hàng, thông tin xe, thời hạn, trạng thái, ngày tạo |
| Cách thức xử lý | ·  Admin chọn một booking detail từ danh sách hoặc nhập trực tiếp booking detail ID. ·  Hệ thống truy vấn và hiển thị đầy đủ thông tin booking detail |

 

5\. Quản lý thiết bị IoT và phần cứng (Devices IO)

* View List Devices

| Thông tin đầu vào | Tiêu chí tìm kiếm |
| :---- | :---- |
| Thông tin đầu ra | Danh sách các Devices dưới dạng bảng, bao gồm: tên thiết bị, thông tin thiết bị, tình trạng hoạt động |
| Cách thức xử lý | ·  Admin nhập tiêu chí tìm kiếm hoặc để trống để xem tất cả. ·  Hệ thống truy vấn cơ sở dữ liệu và trả về kết quả theo phân trang. ·  Hỗ trợ sắp xếp theo tên, loại thiết bị, tình trạng hoạt động |

* View Detail Device by ID

| Thông tin đầu vào | Device ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của Device bao gồm tên thiết bị, thông tin thiết bị, tình trạng hoạt động |
| Cách thức xử lý | ·  Admin chọn một Device từ danh sách hoặc nhập trực tiếp Device ID. ·  Hệ thống truy vấn và hiển thị đầy đủ thông tin Device. |

 

6\. Quản lý Complaints

* View List Complaints

| Thông tin đầu vào | Tiêu chí tìm kiếm |
| :---- | :---- |
| Thông tin đầu ra | Danh sách các Complaints dưới dạng bảng |
| Cách thức xử lý | Hiển thị danh sách Complaints với phân trang và lọc theo trạng thái |

* View Detail Complaint by ID

| Thông tin đầu vào | Complaint ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của Complaint bao gồm nội dung khiếu nại, hình ảnh/video, lịch sử xử lý |
| Cách thức xử lý | ·  Guard chọn một Complaint từ danh sách ·  Hệ thống truy vấn và hiển thị đầy đủ thông tin Complaint. |

* Create Complaint

| Thông tin đầu vào | Nội dung Complaint, hình ảnh/video |
| :---- | :---- |
| Thông tin đầu ra | Complaint ID mới được tạo, thông báo thành công |
| Cách thức xử lý | ·  Guard nhập thông tin complaint ·  Hệ thống kiểm tra tính hợp lệ ·  Tạo complaint. |
| Dữ liệu cần lưu trữ | Complaint ID, thông tin Complaint |

* Update Complaint

| Thông tin đầu vào | Complaint ID, thông tin cần chỉnh sửa |
| :---- | :---- |
| Thông tin đầu ra | Complaint đã được cập nhật, thông báo thành công |
| Cách thức xử lý | ·  Guard chọn Complaint cần chỉnh sửa. ·  Chỉnh sửa các trường cho phép ·  Hệ thống kiểm tra điều kiện trước khi lưu. ·  Cập nhật dữ liệu vào cơ sở dữ liệu và ghi log thay đổi. |
| Dữ liệu cần lưu trữ | Lịch sử chỉnh sửa Complaint, thông tin các trường đã thay đổi |

### **2.2.3. Chức năng của ban quản lý**

1\. Quản lý Customers, Groups Customers và Groups Customers Profiles

* View List Customers

| Thông tin đầu vào | Tiêu chí tìm kiếm |
| :---- | :---- |
| Thông tin đầu ra | Danh sách khách hang dưới dạng bảng, bao gồm các cột: Username, Fullname, Identity Number, Phone, Address, Email, Nhóm khách hàng |
| Cách thức xử lý | ·  Admin nhập tiêu chí tìm kiếm hoặc để trống để xem tất cả. ·  Hệ thống truy vấn cơ sở dữ liệu và trả về kết quả theo phân trang. ·  Hỗ trợ sắp xếp theo ngày tạo hoặc trạng thái. |

* View Detail Customer by ID

| Thông tin đầu vào | Customer ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của khách hàng bao gồm Username, Fullname, Identity Number, Phone, Address, Email, Nhóm khách hàng |
| Cách thức xử lý | ·  Admin chọn một khách hàng từ danh sách hoặc nhập trực tiếp customer ID. ·  Hệ thống truy vấn và hiển thị đầy đủ thông tin customer |

* Create Customer

| Thông tin đầu vào | Username, Fullname, Identity Number, Phone, Address, Email, Nhóm khách hang, Role ID |
| :---- | :---- |
| Thông tin đầu ra | Customer ID mới được tạo, thông báo thành công |
| Cách thức xử lý | ·  Admin nhập thông tin khách hàng ·  Hệ thống kiểm tra tính hợp lệ ·  Tạo customer. |
| Dữ liệu cần lưu trữ | Customer ID, thông tin khách hàng |

* Update Customer

| Thông tin đầu vào | Customer ID, thông tin cần chỉnh sửa |
| :---- | :---- |
| Thông tin đầu ra | Customer đã được cập nhật, thông báo thành công |
| Cách thức xử lý | ·  Admin chọn customer cần chỉnh sửa. ·  Chỉnh sửa các trường cho phép ·  Hệ thống kiểm tra điều kiện trước khi lưu. ·  Cập nhật dữ liệu vào cơ sở dữ liệu và ghi log thay đổi. |
| Dữ liệu cần lưu trữ | Lịch sử chỉnh sửa customer, thông tin các trường đã thay đổi |

* Delete Customer

| Thông tin đầu vào | Customer ID |
| :---- | :---- |
| Thông tin đầu ra | Thông báo xóa thành công hoặc thất bại |
| Cách thức xử lý | ·  Admin chọn customer cần xóa. ·  Hệ thống kiểm tra điều kiện ·  Nếu hợp lệ, thực hiện xóa logic (soft delete) và ghi log. |
| Dữ liệu cần lưu trữ | Lý do xóa, thời gian xóa (log) |

* View List Groups Customers

| Thông tin đầu vào | Tiêu chí tìm kiếm |
| :---- | :---- |
| Thông tin đầu ra | Danh sách nhóm khách hang dưới dạng bảng, bao gồm các cột: Tên nhóm, chủ nhóm (owner) |
| Cách thức xử lý | ·  Admin nhập tiêu chí tìm kiếm hoặc để trống để xem tất cả. ·  Hệ thống truy vấn cơ sở dữ liệu và trả về kết quả theo phân trang. |

* View Detail Groups Customer by ID

| Thông tin đầu vào | Groups Customer ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của nhóm khách hàng bao gồm Tên nhóm, chủ nhóm |
| Cách thức xử lý | ·  Admin chọn một nhóm khách hàng từ danh sách hoặc nhập trực tiếp group customers ID. ·  Hệ thống truy vấn và hiển thị đầy đủ thông tin group customers |

* Create Group Customers

| Thông tin đầu vào | Tên nhóm, Customer ID |
| :---- | :---- |
| Thông tin đầu ra | Group Customers ID mới được tạo, thông báo thành công |
| Cách thức xử lý | ·  Admin nhập thông tin nhóm khách hàng ·  Hệ thống kiểm tra tính hợp lệ ·  Tạo group customers. |
| Dữ liệu cần lưu trữ | Group Customers ID, tên nhóm |

* Update Group Customer

| Thông tin đầu vào | Group Customer ID, thông tin cần chỉnh sửa |
| :---- | :---- |
| Thông tin đầu ra | Group Customer đã được cập nhật, thông báo thành công |
| Cách thức xử lý | ·  Admin chọn group customer cần chỉnh sửa. ·  Chỉnh sửa các trường cho phép ·  Hệ thống kiểm tra điều kiện trước khi lưu. ·  Cập nhật dữ liệu vào cơ sở dữ liệu và ghi log thay đổi. |
| Dữ liệu cần lưu trữ | Lịch sử chỉnh sửa group customer, thông tin các trường đã thay đổi |

* Delete Group Customer

| Thông tin đầu vào | Group Customer ID |
| :---- | :---- |
| Thông tin đầu ra | Thông báo xóa thành công hoặc thất bại |
| Cách thức xử lý | ·  Admin chọn group customer cần xóa. ·  Hệ thống kiểm tra điều kiện ·  Nếu hợp lệ, thực hiện xóa logic (soft delete) và ghi log. |
| Dữ liệu cần lưu trữ | Lý do xóa, thời gian xóa (log) |

* View List Groups Profiles

| Thông tin đầu vào | Tiêu chí tìm kiếm |
| :---- | :---- |
| Thông tin đầu ra | Danh sách profile khách hang dưới dạng bảng, bao gồm các cột: Tên profile, tên gói, loại xe, số lượng xe tối đa cho phép |
| Cách thức xử lý | ·  Admin nhập tiêu chí tìm kiếm hoặc để trống để xem tất cả. ·  Hệ thống truy vấn cơ sở dữ liệu và trả về kết quả theo phân trang. |

* View Detail Groups Profile by ID

| Thông tin đầu vào | Groups Customer Profile ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của profile nhóm khách hàng bao gồm Tên nhóm, gói cước, loại xe, số lượng xe tối đa cho phép |
| Cách thức xử lý | ·  Admin chọn một profile nhóm khách hàng từ danh sách hoặc nhập trực tiếp group customers profile ID. ·  Hệ thống truy vấn và hiển thị đầy đủ thông tin group customers profile |

* Create Group Customers Profile

| Thông tin đầu vào | Tên profile |
| :---- | :---- |
| Thông tin đầu ra | Group Customers Profile ID mới được tạo, thông báo thành công |
| Cách thức xử lý | ·  Admin nhập thông tin profile nhóm khách hàng ·  Hệ thống kiểm tra tính hợp lệ ·  Tạo group customers profile. |
| Dữ liệu cần lưu trữ | Group Customers Profile ID, tên profile |

* Update Group Customer Profile

| Thông tin đầu vào | Group Customer Profile ID, thông tin cần chỉnh sửa |
| :---- | :---- |
| Thông tin đầu ra | Group Customer Profile đã được cập nhật, thông báo thành công |
| Cách thức xử lý | ·  Admin chọn group customer profile cần chỉnh sửa. ·  Chỉnh sửa các trường cho phép ·  Hệ thống kiểm tra điều kiện trước khi lưu. ·  Cập nhật dữ liệu vào cơ sở dữ liệu và ghi log thay đổi. |
| Dữ liệu cần lưu trữ | Lịch sử chỉnh sửa group customer profile, thông tin các trường đã thay đổi |

* Delete Group Customer profile

| Thông tin đầu vào | Group Customer profile ID |
| :---- | :---- |
| Thông tin đầu ra | Thông báo xóa thành công hoặc thất bại |
| Cách thức xử lý | ·  Admin chọn group customer profile cần xóa. ·  Hệ thống kiểm tra điều kiện ·  Nếu hợp lệ, thực hiện xóa logic (soft delete) và ghi log. |
| Dữ liệu cần lưu trữ | Lý do xóa, thời gian xóa (log) |

 

2\. Quản lý Bookings và Booking Details

* View List Bookings

| Thông tin đầu vào | Tiêu chí tìm kiếm (Booking ID, tên nhóm khách hàng, trạng thái booking) |
| :---- | :---- |
| Thông tin đầu ra | Danh sách các booking dưới dạng bảng, bao gồm các cột: Booking ID, khách hàng, số lượng xe, trạng thái, ngày tạo |
| Cách thức xử lý | ·  Admin nhập tiêu chí tìm kiếm hoặc để trống để xem tất cả. ·  Hệ thống truy vấn cơ sở dữ liệu và trả về kết quả theo phân trang. ·  Hỗ trợ sắp xếp theo ngày tạo hoặc trạng thái. |

* View Detail Booking by ID

| Thông tin đầu vào | Booking ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của booking bao gồm danh sách Booking Details, tổng quan gói cước, trạng thái thanh toán |
| Cách thức xử lý | ·  Admin chọn một booking từ danh sách hoặc nhập trực tiếp booking ID. ·  Hệ thống truy vấn và hiển thị đầy đủ thông tin booking cùng danh sách các xe thuộc booking. |

* Create Booking

| Thông tin đầu vào | Group Customer ID, loại gói cước |
| :---- | :---- |
| Thông tin đầu ra | Booking ID mới được tạo, thông báo thành công |
| Cách thức xử lý | ·  Admin chọn nhóm khách hàng. ·  Chọn gói cước. ·  Hệ thống kiểm tra tính hợp lệ (khách hàng tồn tại, không vượt quá số lượng gói cước cho phép…). ·  Tạo booking. |
| Dữ liệu cần lưu trữ | Booking ID, ID nhóm khách hàng, loại gói cước, status |

* Update Booking

| Thông tin đầu vào | Booking ID, thông tin cần chỉnh sửa |
| :---- | :---- |
| Thông tin đầu ra | Booking đã được cập nhật, thông báo thành công |
| Cách thức xử lý | ·  Admin chọn booking cần chỉnh sửa. ·  Chỉnh sửa các trường cho phép ·  Hệ thống kiểm tra điều kiện trước khi lưu. ·  Cập nhật dữ liệu vào cơ sở dữ liệu và ghi log thay đổi. |
| Dữ liệu cần lưu trữ | Lịch sử chỉnh sửa booking, thông tin các trường đã thay đổi |

* Delete Booking

| Thông tin đầu vào | Booking ID |
| :---- | :---- |
| Thông tin đầu ra | Thông báo xóa thành công hoặc thất bại |
| Cách thức xử lý | ·  Admin chọn booking cần xóa. ·  Hệ thống kiểm tra điều kiện (booking chưa có thanh toán hoặc đang hoạt động). ·  Nếu hợp lệ, thực hiện xóa logic (soft delete) và ghi log. |
| Dữ liệu cần lưu trữ | Lý do xóa, thời gian xóa (log) |

* View List Booking Details

| Thông tin đầu vào | Tiêu chí tìm kiếm (Booking ID, tên khách hàng, trạng thái booking, khoảng thời gian tạo) |
| :---- | :---- |
| Thông tin đầu ra | Danh sách các booking details dưới dạng bảng, bao gồm các cột: Booking ID, khách hàng, thông tin xe, thời hạn, trạng thái, ngày tạo |
| Cách thức xử lý | ·  Admin nhập tiêu chí tìm kiếm hoặc để trống để xem tất cả. ·  Hệ thống truy vấn cơ sở dữ liệu và trả về kết quả theo phân trang. ·  Hỗ trợ sắp xếp theo ngày tạo hoặc trạng thái. |

* View Detail Booking Details by ID

| Thông tin đầu vào | Booking detail ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của booking detail bao gồm khách hàng, thông tin xe, thời hạn, trạng thái, ngày tạo |
| Cách thức xử lý | ·  Admin chọn một booking detail từ danh sách hoặc nhập trực tiếp booking detail ID. ·  Hệ thống truy vấn và hiển thị đầy đủ thông tin booking detail |

* Create Booking detail

| Thông tin đầu vào | Booking ID, Customer ID, loại xe, biển số xe, giá cước, thời hạn |
| :---- | :---- |
| Thông tin đầu ra | Booking detail ID mới được tạo, thông báo thành công |
| Cách thức xử lý | ·  Admin chọn Booking. ·  Nhập các trường thông tin cần thiết ·  Hệ thống kiểm tra tính hợp lệ (khách hàng tồn tại, không vượt quá số lượng gói cước cho phép…). ·  Tạo booking detail. |
| Dữ liệu cần lưu trữ | Booking ID, ID khách hàng, loại xe, biển số xe, giá cước, thời hạn, thời gian bắt đầu, thời gian kết thúc, tổng tiền, trạng thái |

* Update Booking detail

| Thông tin đầu vào | Booking detail ID, thông tin cần chỉnh sửa |
| :---- | :---- |
| Thông tin đầu ra | Booking detail đã được cập nhật, thông báo thành công |
| Cách thức xử lý | ·  Admin chọn booking detail cần chỉnh sửa. ·  Chỉnh sửa các trường cho phép ·  Hệ thống kiểm tra điều kiện trước khi lưu. ·  Cập nhật dữ liệu vào cơ sở dữ liệu và ghi log thay đổi. |
| Dữ liệu cần lưu trữ | Lịch sử chỉnh sửa booking detail, thông tin các trường đã thay đổi |

* Delete Booking detail

| Thông tin đầu vào | Booking detail ID |
| :---- | :---- |
| Thông tin đầu ra | Thông báo xóa thành công hoặc thất bại |
| Cách thức xử lý | ·  Admin chọn booking detail cần xóa. ·  Hệ thống kiểm tra điều kiện (booking detail chưa có thanh toán hoặc đang hoạt động). ·  Nếu hợp lệ, thực hiện xóa logic (soft delete) và ghi log. |
| Dữ liệu cần lưu trữ | Lý do xóa, thời gian xóa (log) |

 

3\. Quản lý Packages và biểu giá

* View List Packages

| Thông tin đầu vào | Tiêu chí tìm kiếm |
| :---- | :---- |
| Thông tin đầu ra | Danh sách các gói cước dưới dạng bảng |
| Cách thức xử lý | ·  Admin nhập hoặc để trống tiêu chí tìm kiếm. ·  Hệ thống truy vấn và trả về danh sách gói cước theo phân trang. |

* Create Package

| Thông tin đầu vào | Tên gói |
| :---- | :---- |
| Thông tin đầu ra | Package ID mới, thông báo tạo thành công |
| Cách thức xử lý | ·  Admin nhập đầy đủ thông tin gói cước. ·  Hệ thống kiểm tra tính hợp lệ ·  Lưu gói cước mới vào cơ sở dữ liệu. |
| Dữ liệu cần lưu trữ | Package ID, tên gói cước |

* Update Package

| Thông tin đầu vào | Package ID, thông tin cần chỉnh sửa |
| :---- | :---- |
| Thông tin đầu ra | Package đã được cập nhật, thông báo thành công |
| Cách thức xử lý | ·  Admin chọn package cần chỉnh sửa. ·  Chỉnh sửa các trường cho phép ·  Hệ thống kiểm tra điều kiện trước khi lưu. ·  Cập nhật dữ liệu vào cơ sở dữ liệu và ghi log thay đổi. |
| Dữ liệu cần lưu trữ | Lịch sử chỉnh sửa package, thông tin các trường đã thay đổi |

* Delete Package

| Thông tin đầu vào | Package ID |
| :---- | :---- |
| Thông tin đầu ra | Thông báo xóa thành công hoặc thất bại |
| Cách thức xử lý | ·  Admin chọn package cần xóa. ·  Hệ thống kiểm tra điều ·  Nếu hợp lệ, thực hiện xóa logic (soft delete) và ghi log. |
| Dữ liệu cần lưu trữ | Lý do xóa, thời gian xóa (log) |

* View List Biểu giá

| Thông tin đầu vào | Tiêu chí tìm kiếm |
| :---- | :---- |
| Thông tin đầu ra | Danh sách bảng giá dưới dạng bảng |
| Cách thức xử lý | ·  Admin nhập hoặc để trống tiêu chí tìm kiếm. ·  Hệ thống truy vấn và trả về danh sách bảng giá theo bảng |

* Update biểu giá

| Thông tin đầu vào | Gói cước, Loại xe, thời hạn, ngày, khung giờ, mức giá mới |
| :---- | :---- |
| Thông tin đầu ra | Biểu giá đã được cập nhật, thông báo thành công |
| Cách thức xử lý | ·  Admin chọn mức giá và chỉnh sửa. ·  Hệ thống kiểm tra điều kiện trước khi lưu. ·  Cập nhật dữ liệu vào cơ sở dữ liệu và ghi log thay đổi. |
| Dữ liệu cần lưu trữ | Lịch sử chỉnh sửa biểu giá, thông tin các trường đã thay đổi |

   

  4\.      Quản lý Loại xe

* View List Vehicle Types

| Thông tin đầu vào | Tiêu chí tìm kiếm |
| :---- | :---- |
| Thông tin đầu ra | Danh sách các loại xe dưới dạng bảng |
| Cách thức xử lý | ·  Admin nhập hoặc để trống tiêu chí tìm kiếm. ·  Hệ thống truy vấn và trả về danh sách loại xe theo phân trang. |

* Create Vehicle Type

| Thông tin đầu vào | Tên Vehicle Type |
| :---- | :---- |
| Thông tin đầu ra | Vehicle Type ID, thông báo tạo thành công |
| Cách thức xử lý | ·  Admin nhập thông tin loại xe. ·  Hệ thống kiểm tra tính hợp ·  Lưu gói cước mới vào cơ sở dữ liệu. |
| Dữ liệu cần lưu trữ | Vehicle Type ID, tên loại xe |

* Update Vehicle Type

| Thông tin đầu vào | Vehicle Type ID, thông tin cần chỉnh sửa |
| :---- | :---- |
| Thông tin đầu ra | Vehicle Type đã được cập nhật, thông báo thành công |
| Cách thức xử lý | ·  Admin chọn Vehicle Type cần chỉnh sửa. ·  Chỉnh sửa các trường cho phép ·  Hệ thống kiểm tra điều kiện trước khi lưu. ·  Cập nhật dữ liệu vào cơ sở dữ liệu và ghi log thay đổi. |
| Dữ liệu cần lưu trữ | Lịch sử chỉnh sửa Vehicle Type, thông tin các trường đã thay đổi |

* Delete Vehicle Type

| Thông tin đầu vào | Vehicle Type ID |
| :---- | :---- |
| Thông tin đầu ra | Thông báo xóa thành công hoặc thất bại |
| Cách thức xử lý | ·  Admin chọn Vehicle Type cần xóa. ·  Hệ thống kiểm tra điều ·  Nếu hợp lệ, thực hiện xóa logic (soft delete) và ghi log. |
| Dữ liệu cần lưu trữ | Lý do xóa, thời gian xóa (log) |

 

5\. Quản lý Parking Sessions

* View List Parking Sessions

| Thông tin đầu vào | Tiêu chí tìm kiếm |
| :---- | :---- |
| Thông tin đầu ra | Danh sách các Parking Sessions dưới dạng bảng, bao gồm các cột: Parking Session ID, biển số xe, loại xe, Booking Detail ID, thời gian đỗ xe, tiền vé |
| Cách thức xử lý | ·  Admin nhập tiêu chí tìm kiếm hoặc để trống để xem tất cả. ·  Hệ thống truy vấn cơ sở dữ liệu và trả về kết quả theo phân trang. ·  Hỗ trợ sắp xếp theo ngày tạo hoặc trạng thái. |

* View Detail Parking Session by ID

| Thông tin đầu vào | Parking Session ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của Parking Session bao gồm Parking Session ID, biển số xe, loại xe, Booking Detail ID, thời gian đỗ xe, tiền vé |
| Cách thức xử lý | ·  Admin chọn một Parking Session từ danh sách hoặc nhập trực tiếp Parking Session ID. ·  Hệ thống truy vấn và hiển thị đầy đủ thông tin Parking Session |

* Update Parking Session

| Thông tin đầu vào | Parking Session ID, thông tin cần chỉnh sửa |
| :---- | :---- |
| Thông tin đầu ra | Parking Session đã được cập nhật, thông báo thành công |
| Cách thức xử lý | ·  Admin chọn Parking Session cần chỉnh sửa. ·  Chỉnh sửa các trường cho phép ·  Hệ thống kiểm tra điều kiện trước khi lưu. ·  Cập nhật dữ liệu vào cơ sở dữ liệu và ghi log thay đổi. |
| Dữ liệu cần lưu trữ | Lịch sử chỉnh sửa Parking Session, thông tin các trường đã thay đổi |

 

6\. Quản lý Zone

* View List Zone

| Thông tin đầu vào | Tiêu chí tìm kiếm |
| :---- | :---- |
| Thông tin đầu ra | Danh sách các Zone dưới dạng bảng, bao gồm: tên zone, sức chứa, số chỗ trống hiện tại |
| Cách thức xử lý | ·  Admin nhập tiêu chí tìm kiếm hoặc để trống để xem tất cả. ·  Hệ thống truy vấn cơ sở dữ liệu và trả về kết quả theo phân trang. ·  Hỗ trợ sắp xếp theo tên |


* View Detail Zone by ID

| Thông tin đầu vào | Zone ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của Zone bao gồm tên zone, sức chứa, số chỗ trống hiện tại, danh sách thiết bị |
| Cách thức xử lý | ·  Admin chọn một Zone từ danh sách hoặc nhập trực tiếp Zone ID. ·  Hệ thống truy vấn và hiển thị đầy đủ thông tin Zone. |

* Create Zone

| Thông tin đầu vào | Thông tin Zone |
| :---- | :---- |
| Thông tin đầu ra | Zone ID mới được tạo, thông báo thành công |
| Cách thức xử lý | ·  Admin nhập thông tin Zone ·  Hệ thống kiểm tra tính hợp lệ ·  Tạo Zone. |
| Dữ liệu cần lưu trữ | Zone ID, thông tin Zone |

* Update Zone

| Thông tin đầu vào | Zone ID, thông tin cần chỉnh sửa |
| :---- | :---- |
| Thông tin đầu ra | Zone đã được cập nhật, thông báo thành công |
| Cách thức xử lý | ·  Admin chọn Zone cần chỉnh sửa. ·  Chỉnh sửa các trường cho phép ·  Hệ thống kiểm tra điều kiện trước khi lưu. ·  Cập nhật dữ liệu vào cơ sở dữ liệu và ghi log thay đổi. |
| Dữ liệu cần lưu trữ | Lịch sử chỉnh sửa Zone, thông tin các trường đã thay đổi |

* Delete Device

| Thông tin đầu vào | Zone ID |
| :---- | :---- |
| Thông tin đầu ra | Thông báo xóa thành công hoặc thất bại |
| Cách thức xử lý | ·  Admin chọn Zone cần xóa. ·  Hệ thống kiểm tra điều kiện ·  Nếu hợp lệ, thực hiện xóa logic (soft delete) và ghi log. |

 

7\. Quản lý thiết bị IoT và phần cứng (Devices IO)

* View List Devices

| Thông tin đầu vào | Tiêu chí tìm kiếm |
| :---- | :---- |
| Thông tin đầu ra | Danh sách các Devices dưới dạng bảng, bao gồm: tên thiết bị, thông tin thiết bị, tình trạng hoạt động |
| Cách thức xử lý | ·  Admin nhập tiêu chí tìm kiếm hoặc để trống để xem tất cả. ·  Hệ thống truy vấn cơ sở dữ liệu và trả về kết quả theo phân trang. ·  Hỗ trợ sắp xếp theo tên, loại thiết bị, tình trạng hoạt động |

* View Detail Device by ID

| Thông tin đầu vào | Device ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của Device bao gồm tên thiết bị, thông tin thiết bị, tình trạng hoạt động |
| Cách thức xử lý | ·  Admin chọn một Device từ danh sách hoặc nhập trực tiếp Device ID. ·  Hệ thống truy vấn và hiển thị đầy đủ thông tin Device. |

* Create Device

| Thông tin đầu vào | Thông tin Device |
| :---- | :---- |
| Thông tin đầu ra | Device ID mới được tạo, thông báo thành công |
| Cách thức xử lý | ·  Admin nhập thông tin Device ·  Hệ thống kiểm tra tính hợp lệ ·  Tạo Device. |
| Dữ liệu cần lưu trữ | Device ID, thông tin Device |

* Update Device

| Thông tin đầu vào | Device ID, thông tin cần chỉnh sửa |
| :---- | :---- |
| Thông tin đầu ra | Device đã được cập nhật, thông báo thành công |
| Cách thức xử lý | ·  Admin chọn Device cần chỉnh sửa. ·  Chỉnh sửa các trường cho phép ·  Hệ thống kiểm tra điều kiện trước khi lưu. ·  Cập nhật dữ liệu vào cơ sở dữ liệu và ghi log thay đổi. |
| Dữ liệu cần lưu trữ | Lịch sử chỉnh sửa Device, thông tin các trường đã thay đổi |

* Delete Device

| Thông tin đầu vào | Device ID |
| :---- | :---- |
| Thông tin đầu ra | Thông báo xóa thành công hoặc thất bại |
| Cách thức xử lý | ·  Admin chọn Device cần xóa. ·  Hệ thống kiểm tra điều kiện ·  Nếu hợp lệ, thực hiện xóa logic (soft delete) và ghi log. |

 

8\. Quản lý thanh toán và doanh thu

 

9\. Quản lý Complaints

* View List Complaints

| Thông tin đầu vào | Tiêu chí tìm kiếm |
| :---- | :---- |
| Thông tin đầu ra | Danh sách các Complaints dưới dạng bảng |
| Cách thức xử lý | Hiển thị danh sách Complaints với phân trang và lọc theo trạng thái |

* View Detail Complaint by ID

| Thông tin đầu vào | Complaint ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của Complaint bao gồm nội dung khiếu nại, hình ảnh/video, lịch sử xử lý |
| Cách thức xử lý | ·  Admin chọn một Complaint từ danh sách ·  Hệ thống truy vấn và hiển thị đầy đủ thông tin Complaint. |

* Create Complaint

| Thông tin đầu vào | Nội dung Complaint, hình ảnh/video |
| :---- | :---- |
| Thông tin đầu ra | Complaint ID mới được tạo, thông báo thành công |
| Cách thức xử lý | ·  Admin nhập thông tin complaint ·  Hệ thống kiểm tra tính hợp lệ ·  Tạo complaint. |
| Dữ liệu cần lưu trữ | Complaint ID, thông tin Complaint |

* Update Complaint

| Thông tin đầu vào | Complaint ID, thông tin cần chỉnh sửa |
| :---- | :---- |
| Thông tin đầu ra | Complaint đã được cập nhật, thông báo thành công |
| Cách thức xử lý | ·  Admin chọn Complaint cần chỉnh sửa. ·  Chỉnh sửa các trường cho phép ·  Hệ thống kiểm tra điều kiện trước khi lưu. ·  Cập nhật dữ liệu vào cơ sở dữ liệu và ghi log thay đổi. |
| Dữ liệu cần lưu trữ | Lịch sử chỉnh sửa Complaint, thông tin các trường đã thay đổi |

* Delete Complaint

| Thông tin đầu vào | Complaint ID |
| :---- | :---- |
| Thông tin đầu ra | Thông báo xóa thành công hoặc thất bại |
| Cách thức xử lý | ·  Admin chọn Complaint cần xóa. ·  Hệ thống kiểm tra điều kiện ·  Nếu hợp lệ, thực hiện xóa logic (soft delete) và ghi log. |

 

10\. Quản lý Employees, Roles, Permissions

* View List Employees

| Thông tin đầu vào | Tiêu chí tìm kiếm |
| :---- | :---- |
| Thông tin đầu ra | Danh sách các Employees dưới dạng bảng, bao gồm các cột: Employee ID, Username, Fullname, Phone, Email, Role, Status |
| Cách thức xử lý | ·  Admin nhập tiêu chí tìm kiếm hoặc để trống để xem tất cả. ·  Hệ thống truy vấn cơ sở dữ liệu và trả về kết quả theo phân trang. ·  Hỗ trợ sắp xếp theo tên, vai trò, ngày tạo hoặc trạng thái. |

* View Detail Employee by ID

| Thông tin đầu vào | Employee ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của Employee bao gồm Employee ID, Username, Fullname, Phone, Email, Role, Status |
| Cách thức xử lý | ·  Admin chọn một Employee từ danh sách hoặc nhập trực tiếp Employee ID. ·  Hệ thống truy vấn và hiển thị đầy đủ thông tin Employee. |

* Create Employee

| Thông tin đầu vào | Thông tin nhân viên, Role ID |
| :---- | :---- |
| Thông tin đầu ra | Employee ID mới được tạo, thông báo thành công |
| Cách thức xử lý | ·  Admin nhập thông tin khách hang, chọn vai trò ·  Hệ thống kiểm tra tính hợp lệ ·  Tạo Employee. |
| Dữ liệu cần lưu trữ | Employee ID, thông tin nhân viên, Role ID |

* Update Employee

| Thông tin đầu vào | Employee ID, thông tin cần chỉnh sửa |
| :---- | :---- |
| Thông tin đầu ra | Employee đã được cập nhật, thông báo thành công |
| Cách thức xử lý | ·  Admin chọn Employee cần chỉnh sửa. ·  Chỉnh sửa các trường cho phép ·  Hệ thống kiểm tra điều kiện trước khi lưu. ·  Cập nhật dữ liệu vào cơ sở dữ liệu và ghi log thay đổi. |
| Dữ liệu cần lưu trữ | Lịch sử chỉnh sửa Employee, thông tin các trường đã thay đổi |

* Delete Employee

| Thông tin đầu vào | Employee ID |
| :---- | :---- |
| Thông tin đầu ra | Thông báo xóa thành công hoặc thất bại |
| Cách thức xử lý | ·  Admin chọn Employee cần xóa. ·  Hệ thống kiểm tra điều kiện ·  Nếu hợp lệ, thực hiện xóa logic (soft delete) và ghi log. |

* View List Roles

| Thông tin đầu vào | Tiêu chí tìm kiếm |
| :---- | :---- |
| Thông tin đầu ra | Danh sách các Roles dưới dạng bảng |
| Cách thức xử lý | Hiển thị danh sách vai trò và quyền được cấp |

* View Detail Role by ID

| Thông tin đầu vào | Role ID |
| :---- | :---- |
| Thông tin đầu ra | Thông tin chi tiết của Role bao gồm tên vai trò, danh sách quyền hạn |
| Cách thức xử lý | ·  Admin chọn một role từ danh sách ·  Hệ thống truy vấn và hiển thị đầy đủ thông tin Role. |

* Create Role

| Thông tin đầu vào | Tên role |
| :---- | :---- |
| Thông tin đầu ra | Role ID mới được tạo, thông báo thành công |
| Cách thức xử lý | ·  Admin nhập thông tin role ·  Hệ thống kiểm tra tính hợp lệ ·  Tạo Role. |
| Dữ liệu cần lưu trữ | Role ID, thông tin Role |

* Update Role

| Thông tin đầu vào | Role ID, thông tin cần chỉnh sửa |
| :---- | :---- |
| Thông tin đầu ra | Role đã được cập nhật, thông báo thành công |
| Cách thức xử lý | ·  Admin chọn Role cần chỉnh sửa. ·  Chỉnh sửa các trường cho phép ·  Hệ thống kiểm tra điều kiện trước khi lưu. ·  Cập nhật dữ liệu vào cơ sở dữ liệu và ghi log thay đổi. |
| Dữ liệu cần lưu trữ | Lịch sử chỉnh sửa Role, thông tin các trường đã thay đổi |

* Delete Role

| Thông tin đầu vào | Role ID |
| :---- | :---- |
| Thông tin đầu ra | Thông báo xóa thành công hoặc thất bại |
| Cách thức xử lý | ·  Admin chọn Role cần xóa. ·  Hệ thống kiểm tra điều kiện ·  Nếu hợp lệ, thực hiện xóa logic (soft delete) và ghi log. |

* View List Permissions

| Thông tin đầu vào | Không có |
| :---- | :---- |
| Thông tin đầu ra | Danh sách tất cả permissions trong hệ thống |
| Cách thức xử lý | Hiển thị danh sách quyền theo nhóm chức năng |

* Update Permission

| Thông tin đầu vào | Role ID, danh sách permission cần thay đổi |
| :---- | :---- |
| Thông tin đầu ra | Permission của role đã được cập nhật |
| Cách thức xử lý | ·  Admin chọn Role cần chỉnh sửa. ·  Chỉnh sửa các permission ·  Hệ thống kiểm tra điều kiện trước khi lưu. ·  Cập nhật dữ liệu vào cơ sở dữ liệu và ghi log thay đổi. |
| Dữ liệu cần lưu trữ | Lịch sử chỉnh sửa Permission, thông tin các permission đã thay đổi |

 

11\. Cấu hình tham số hệ thống động

* Update System Parameters

| Thông tin đầu vào | Thời gian ân hạn, quy tắc tính phí, thời gian giữ chỗ sau thanh toán |
| :---- | :---- |
| Thông tin đầu ra | Thời gian ân hạn, quy tắc tính phí, thời gian giữ chỗ sau thanh toán mới |
| Cách thức xử lý | ·  Admin xem và cập nhật các thông số ·  Hệ thống kiểm tra tính hợp lệ ·  Lưu lại các tham số đã cập nhật vào file cầu hình và áp dụng ngay mà không cần khởi động lại hệ thống. |
| Dữ liệu cần lưu trữ | Thời gian ân hạn, quy tắc tính phí, thời gian giữ chỗ sau thanh toán đã được cập nhật |

 

12\. Quản lý Thiết lập AI và Buffer Dự phòng

* View & Update AI Settings

| Thông tin đầu vào | Ngưỡng nhận diện, độ chính xác tối thiểu |
| :---- | :---- |
| Thông tin đầu ra | Ngưỡng nhận diện, độ chính xác tối thiểu mới |
| Cách thức xử lý | ·  Admin xem và cập nhật các tham số AI ·  Hệ thống kiểm tra tính hợp lệ ·  Lưu lại các tham số đã cập nhật. |
| Dữ liệu cần lưu trữ | Ngưỡng nhận diện, độ chính xác tối thiểu đã được cập nhật |

* View & Update Safety Buffer

| Thông tin đầu vào | Tỷ lệ buffer dự phòng |
| :---- | :---- |
| Thông tin đầu ra | Tỷ lệ buffer dự phòng mới |
| Cách thức xử lý | ·  Admin xem và cập nhật các hệ số an toàn và vùng đệm chỗ đỗ ·  Hệ thống kiểm tra tính hợp lệ ·  Lưu lại các thông tin đã cập nhật. |
| Dữ liệu cần lưu trữ | Tỷ lệ buffer dự phòng đã được cập nhật |

 

13\. Authentication & Authorization

* Login

| Thông tin đầu vào | Tên đăng nhập (username, phone, email), password |
| :---- | :---- |
| Cách thức xử lý | Xác thực thông tin và cấp session/token |

* Logout

| Thông tin đầu vào | Session hiện tại |
| :---- | :---- |
| Cách thức xử lý | Hủy session hiện tại, đăng xuất tài khoản ra khỏi hệ thống |

* Forget password

| Thông tin đầu vào | Email/phone |
| :---- | :---- |
| Thông tin đầu ra | Password mới đã được cập nhật |
| Cách thức xử lý | ·  Nhập email/phone ·  Xác thực email/phone có tồn tại và hợp lệ ·  Gửi mã OTP qua email/SMS ·  Nhập mã OTP ·  Đặt mật khẩu mới |
| Dữ liệu cần lưu trữ | Password mới |

* Change password

| Thông tin đầu vào | Password cũ |
| :---- | :---- |
| Thông tin đầu ra | Password mới đã được cập nhật |
| Cách thức xử lý | ·  Nhập password cũ và password mới ·  Xác thực tính hợp lệ của password cũ, password mới ·  Đổi mật khẩu mới |
| Dữ liệu cần lưu trữ | Password mới |

 

### **2.2.4. Chức năng tự động của AI và hệ thống**

1\. Tạo mới Parking Session

* Create Parking Session

| Thông tin đầu vào | Biển số xe, loại xe, Booking Detail ID, thời điểm gửi, hình ảnh checkin |
| :---- | :---- |
| Thông tin đầu ra | Parking session mới được tạo, thông báo thành công |
| Cách thức xử lý | ·  AI nhận diện biển số xe và loại xe ·  Hệ thống nhập biển số xe, loại xe, thời điểm gửi, hình ảnh checkin ·  Hệ thống kiểm tra tính hợp lệ (khách hàng tồn tại, không vượt quá số lượng gói cước cho phép…). ·  Tạo parking session. |
| Dữ liệu cần lưu trữ | Parking session ID, Biển số xe, loại xe, Booking Detail ID, thời điểm gửi, hình ảnh checkin |

 

2\. Thông báo và Nhắc nợ Tự động

* Automatic Notification and Debt Reminder

| Thông tin đầu vào | Booking Detail ID / Customer ID Loại sự kiện (sắp hết hạn gói cước, quá hạn thanh toán, nhắc nợ) Thời gian nhắc (5 ngày, 3 ngày, 1 ngày trước hạn) |
| :---- | :---- |
| Thông tin đầu ra | Thông báo đẩy (Push Notification) gửi đến Customer App Lịch sử thông báo được ghi nhận |
| Cách thức xử lý | Hệ thống chạy job nền định kỳ để kiểm tra các gói cước sắp hết hạn và các khoản thanh toán quá hạn. Tự động tạo thông báo theo cấu hình (5 ngày, 3 ngày, 1 ngày trước hạn). Gửi Push Notification đến ứng dụng di động của khách hàng. Ghi log lịch sử thông báo và trạng thái (Đã gửi / Đã xem). Hỗ trợ cấu hình nội dung thông báo động bởi Admin. |
| Dữ liệu cần lưu trữ | Notification ID, Customer ID, Booking Detail ID, Loại thông báo, Nội dung thông báo, Thời gian gửi, Trạng thái thông báo |

   

