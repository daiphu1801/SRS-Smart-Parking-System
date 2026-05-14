package com.smartparking.identity.service;

import com.smartparking.identity.dto.response.AuthLoginResponse;
import com.smartparking.identity.dto.response.CheckPhoneResponse;
import com.smartparking.identity.dto.response.ForgotPasswordResponse;
import com.smartparking.identity.dto.response.ProfileResponse;
import com.smartparking.identity.entity.*;
import com.smartparking.identity.repository.*;
import com.smartparking.shared.integration.SupabaseAuthClient;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final SupabaseAuthClient supabaseClient;
    private final RoleFunctionActionRepository roleFunctionActionRepo;
    private final OtpOneTimeRepository otpOneTimeRepository;
    private final GroupsCustomersRepository groupsCustomerRepo;

    private final SmsService smsService;

    @Value("${app.demo.admin-phone}")
    private String adminPhone;

    @Value("${app.demo.otp-expiration-minutes:5}") // Mặc định 5 phút nếu quên config
    private int otpExpirationMinutes;
    @Value("${app.demo.otp-max-try:5}")
    private  int MAX_OTP_TRY; // Giới hạn nhập sai 5 lần

    public AuthLoginResponse login(String username, String password) {
        Map<String, Object> response = supabaseClient.loginWithPassword(username, password);
        String accessToken = (String) response.get("access_token");
        Map<String, Object> user = (Map<String, Object>) response.get("user");
        String supabaseId_String = (String) user.get("id");
        UUID supabaseId = UUID.fromString(supabaseId_String);

        Account localAccount = accountRepository.findBySupabaseId(supabaseId)
                .orElseGet(() -> {
                    log.info("Tài khoản có trên Supabase nhưng chưa có ở Local. Tiến hành Auto-sync...");
                    Account newAcc = new Account();
                    newAcc.setUsername(username);
                    newAcc.setAccountType(AccountType.EMPLOYEE);
                    // Set thêm các default values khác nếu cần
                    return accountRepository.save(newAcc);
                });
        List<String> permissions = roleFunctionActionRepo.findPermissionCodesByRoleId(localAccount.getRoleId());

        if (localAccount.getStatus() == GeneralStatus.LOCKED) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa hoặc vô hiệu hóa. Vui lòng liên hệ Admin!");
        }

        return AuthLoginResponse.builder()
                .accessToken(accessToken)
                .accountType(localAccount.getAccountType().name())
                .accountId(localAccount.getId())
                .permissions(permissions)
                .build();
    }

    public void logout(String token) {
        try {
            supabaseClient.logout(token);
            log.info("Token invalidated on Supabase");
        } catch (Exception e) {
            log.warn("Supabase logout warning (Token may already be expired): {}", e.getMessage());
        }
    }


    // 6. Get Profile
    public ProfileResponse getProfile(Integer accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        ProfileResponse.ProfileResponseBuilder builder = ProfileResponse.builder()
                .accountId(account.getId())
                .username(account.getUsername())
                .accountType(account.getAccountType().name());

        if (account.getAccountType() == AccountType.CUSTOMER) {
            customerRepository.findByAccountId(account.getId()).ifPresent(c -> {
                builder.fullName(c.getFullName());
                builder.phone(account.getUsername());
            });
        } else {
            employeeRepository.findByAccountId(account.getId()).ifPresent(e -> {
                builder.fullName(e.getFullName());
                builder.phone(e.getPhone());
            });
        }
        return builder.build();
    }



    private String formatPhoneToE164(String phone) {
        if (phone.startsWith("0")) {
            return "84" + phone.substring(1);
        }
        return phone.startsWith("+") ? phone : "+" + phone;
    }

    @Transactional
    public CheckPhoneResponse checkPhone(String phone) {
        // 1. Tìm cái "vỏ" Account dưới Local DB
        Account account = accountRepository.findByUsername(phone)
                .orElseThrow(() -> {
                    log.warn("Nỗ lực đăng nhập bằng SĐT không tồn tại: {}", phone);
                    return new IllegalArgumentException("Số điện thoại chưa được đăng ký trong hệ thống. Vui lòng liên hệ Quản lý!");
                });

        String action;
        if (account.getSupabaseId() != null && !account.getSupabaseId().toString().trim().isEmpty()) {
            // Đã có Supabase ID -> Đây là người cũ -> Yêu cầu nhập Mật khẩu
            action = "REQUIRE_LOGIN_PASSWORD";
        } else {
            // Chưa có Supabase ID -> Đây là người mới -> Yêu cầu Kích hoạt
            action = "REQUIRE_OTP_ACTIVATION";

            // 2. TÁI SỬ DỤNG HÀM sendOtp LÕI
            // Gọi 1 dòng này là nó tự động: Hủy mã cũ, sinh mã mới, set TryTime = 0, lưu DB và bắn SMS.
            this.sendOtp(phone, OtpType.ACTIVATION);
        }

        // 3. Trả kết quả về cho Controller
        return CheckPhoneResponse.builder()
                .action(action)
                .build();
    }


    @Transactional
    public AuthLoginResponse createSupabaseAccount(String rawPhone, String otpCode, String password) {

        this.verifyOtp(rawPhone, otpCode);
        // 1. Kiểm tra "vỏ" Account ở Local DB
        String phone = rawPhone;
        if (phone.startsWith("0")) {
            phone = "84" + phone.substring(1);
        }
        Account account = accountRepository.findByUsername(phone)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại trong hệ thống."));
        if (account.getSupabaseId() != null) {
            throw new IllegalArgumentException("Tài khoản này đã được kích hoạt. Vui lòng đăng nhập.");
        }

        // 2. Gọi hàm helper để lấy cục Metadata mới nhất
        Map<String, Object> appMetadata = buildUserMetadata(account.getId());

        // 3. Tạo thẳng User trên Supabase kèm luôn Metadata
        String newSupabaseId = supabaseClient.createAdminUser(phone, password, appMetadata);

        // 4. Cập nhật Local Database
        account.setSupabaseId(UUID.fromString(newSupabaseId));
        account.setStatus(GeneralStatus.ACTIVE);
        accountRepository.save(account);

        // 5. Tự động Đăng nhập trả token về
        Map<String, Object> loginResponse = supabaseClient.loginWithPassword(phone, password);
        String accessToken = (String) loginResponse.get("access_token");

        return AuthLoginResponse.builder()
                .accessToken(accessToken)
                .accountType(account.getAccountType().name())
                .accountId(account.getId())
                .build();
    }

    public void syncMetadataToSupabase(Account account) {
        if (account == null || account.getSupabaseId() == null) return;

        // 1. Gọi hàm helper để lấy cục Metadata mới nhất
        Map<String, Object> metadata = buildUserMetadata(account.getId());

        // 2. Bắn API Update lên Supabase
        supabaseClient.updateUserMetadata(account.getSupabaseId().toString(), metadata);
    }

    private Map<String, Object> buildUserMetadata(Integer accountId) {
        List<Integer> masterIds = groupsCustomerRepo.findGroupIdsByMasterAccountId(accountId);
        List<Integer> memberIds = customerRepository.findMemberGroupIdsByAccountId(accountId);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("master_group_ids", masterIds);
        metadata.put("member_group_ids", memberIds);
        return metadata;
    }
    @Transactional
    public ForgotPasswordResponse sendOtp(String phone, OtpType type) {
        // 1. Map String type từ Client gửi lên thành Enum
            if((type == null )){
            type =  OtpType.ACTIVATION;
            }

        Integer accountId = null;
        if (type == OtpType.FORGOT_PASSWORD) {
            // Quên mật khẩu thì BẮT BUỘC số điện thoại đã phải có trong Account
            Account account = accountRepository.findByUsername(phone)
                    .orElseThrow(() -> new IllegalArgumentException("Số điện thoại chưa được đăng ký trong hệ thống!"));
            accountId = account.getId();
        } else if (type == OtpType.ACTIVATION) {
            // Đăng ký/Kích hoạt thì có thể chưa có account, nên tìm thấy thì gán, không thì để null
            accountId = accountRepository.findByUsername(phone).map(Account::getId).orElse(null);
        }

        // 3. Vô hiệu hóa tất cả các mã OTP cũ đang chờ của SĐT này
        otpOneTimeRepository.invalidateOldOtps(phone);

        // 4. Sinh mã OTP mới (6 số)
        String otpCode = generateRandomOtp();

        // 5. Lưu vào Database
        OtpOneTime otpEntity = new OtpOneTime();
        otpEntity.setPhone(phone);
        otpEntity.setAccountId(accountId);
        otpEntity.setOtpCode(otpCode);
        otpEntity.setExpiredAt(LocalDateTime.now().plusMinutes(otpExpirationMinutes));
        otpEntity.setUsed(false);
        otpEntity.setType(type);
        otpEntity.setTryTime(0); // Vừa tạo ra thì số lần thử = 0
        otpOneTimeRepository.save(otpEntity);

        // 6. Gửi SMS qua Twilio
        String smsMessage = String.format("Ma xac nhan cua ban la: %s. Ma co hieu luc trong %d phut. Khong chia se cho bat ky ai.",
                otpCode, otpExpirationMinutes);
        smsService.sendSms(adminPhone, smsMessage); // Nhớ đổi SĐT test nếu Twilio đang Trial

        log.info("Đã gửi OTP [{}] cho SĐT: {} (Type: {})", otpCode, phone, type);

       return   ForgotPasswordResponse.builder()
                .phone(phone.trim())
                .message("Mã xác thực đã được gửi đến số điện thoại của bạn.")
                .expireInMinutes(otpExpirationMinutes) // Fix cứng theo logic service
                .build();
    }


    @Transactional
    public void verifyOtp(String phone, String inputOtpCode) {

        // 1. Móc mã OTP mới nhất lên

        OtpOneTime latestOtp = otpOneTimeRepository.findTopByPhoneAndIsUsedFalseOrderByIdDesc(phone)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mã OTP nào đang chờ xác thực cho SĐT này!"));

        // 2. Kiểm tra Hết hạn
        if (LocalDateTime.now().isAfter(latestOtp.getExpiredAt())) {
            latestOtp.setUsed(true); // Quá hạn -> Hủy luôn mã này
            otpOneTimeRepository.save(latestOtp);
            throw new IllegalArgumentException("Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại mã mới!");
        }

        // 3. Kiểm tra số lần đã nhập sai trước đó (Bảo mật chống Brute-force)
        if (latestOtp.getTryTime() >= MAX_OTP_TRY) {
            latestOtp.setUsed(true); // Khóa luôn
            otpOneTimeRepository.save(latestOtp);
            throw new IllegalArgumentException("Mã OTP đã bị khóa do nhập sai quá 5 lần. Vui lòng gửi lại OTP!");
        }

        // 4. So khớp Mã OTP do khách nhập
        if (!latestOtp.getOtpCode().equals(inputOtpCode)) {
            // NẾU SAI: Tăng biến tryTime lên 1
            latestOtp.setTryTime(latestOtp.getTryTime() + 1);

            int remainingTries = MAX_OTP_TRY - latestOtp.getTryTime();

            // Nếu tăng xong mà chạm mốc 5 lần -> Hủy luôn mã
            if (remainingTries <= 0) {
                latestOtp.setUsed(true);
                otpOneTimeRepository.save(latestOtp);
                throw new IllegalArgumentException("Bạn đã nhập sai quá 5 lần. Mã OTP đã bị hủy!");
            }

            // Chưa chạm mốc thì lưu lại số lần sai và báo lỗi
            otpOneTimeRepository.save(latestOtp);
            throw new IllegalArgumentException("Mã OTP không chính xác. Bạn còn " + remainingTries + " lần thử.");
        }

        // 5. MỌI THỨ OK -> Xác thực thành công
        latestOtp.setUsed(true);
        otpOneTimeRepository.save(latestOtp);

        log.info("SĐT {} đã xác thực OTP thành công!", phone);
    }



    // Hàm Helper sinh 6 số ngẫu nhiên chuẩn Security
    private String generateRandomOtp() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    @Transactional
    public AuthLoginResponse resetPassword(String rawPhone, String otpCode, String newPassword) {

        this.verifyOtp(rawPhone, otpCode);

        String phone = rawPhone;
        if (phone.startsWith("0")) {
            phone = "84" + phone.substring(1);
        }

        Account account = accountRepository.findByUsername(phone)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại trong hệ thống."));

        if (account.getSupabaseId() == null) {
            throw new IllegalArgumentException("Tài khoản này chưa được kích hoạt. Vui lòng liên hệ Admin hoặc Đăng ký.");
        }

        try {
            supabaseClient.updateUserPassword(account.getSupabaseId().toString(), newPassword);
            log.info("SĐT {} đã reset mật khẩu thành công qua OTP!", phone);
        } catch (Exception e) {
            log.error("Lỗi khi reset mật khẩu trên Supabase cho SĐT: {}", phone, e);
            throw new RuntimeException("Có lỗi xảy ra khi đổi mật khẩu trên máy chủ. Vui lòng thử lại!");
        }

        return this.login(phone, newPassword);
    }

    @Transactional
    public AuthLoginResponse changePassword(Integer accountId, String oldPassword, String newPassword) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tài khoản!"));

        if (account.getSupabaseId() == null) {
            throw new IllegalArgumentException("Tài khoản này chưa được đồng bộ với máy chủ bảo mật.");
        }

        String phone = account.getUsername();

        try {
            supabaseClient.loginWithPassword(phone, oldPassword);
        } catch (Exception e) {
            log.warn("Đổi mật khẩu thất bại: Mật khẩu cũ không đúng cho SĐT {}", phone);
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác!");
        }

        try {
            supabaseClient.updateUserPassword(account.getSupabaseId().toString(), newPassword);
            log.info("SĐT {} đã chủ động thay đổi mật khẩu thành công!", phone);
        } catch (Exception e) {
            log.error("Lỗi Supabase khi change password cho SĐT: {}", phone, e);
            throw new RuntimeException("Có lỗi xảy ra khi đổi mật khẩu. Vui lòng thử lại!");
        }

        return this.login(phone, newPassword);
    }


}
