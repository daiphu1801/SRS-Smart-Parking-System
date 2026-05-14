package com.smartparking.identity.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Value("${app.twilio.account-sid}")
    private String accountSid;

    @Value("${app.twilio.auth-token}")
    private String authToken;

    @Value("${app.twilio.messaging-service-sid}")
    private String messagingServiceSid;
    @Value("${app.twilio.from-number}")
    private String fromNumber;

    // Hàm này chạy ngay khi Spring Boot khởi động để kết nối với Twilio
    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
        log.info("Đã khởi tạo kết nối Twilio thành công!");
    }

    // Hàm gửi tin nhắn
    public void sendSms(String toPhoneNumber, String messageContent) {
//        try {
//            String formattedPhone = toPhoneNumber;
//            if (!formattedPhone.startsWith("+")) {
//                formattedPhone = "+" + formattedPhone;
//            }
//            Message message = Message.creator(
//                    new PhoneNumber(formattedPhone), // Số người nhận
//                    new PhoneNumber(fromNumber),            // Dùng Messaging Service SID thay vì số gửi
//                    messageContent                  // Nội dung tin nhắn
//            ).create();
//
//            log.info("Twilio đã gửi tin nhắn thành công. Message SID: {}", message.getSid());
//        } catch (Exception e) {
//            log.error("Lỗi khi gửi SMS qua Twilio: {}", e.getMessage(), e);
//        }

        log.info("============== MOCK SMS ==============");
        log.info("Gửi tới SĐT: {}", toPhoneNumber);
        log.info("Nội dung tin nhắn: {}", messageContent);
        log.info("======================================");
    }
}