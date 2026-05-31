package com.smartparking.identity.service;

import com.smartparking.identity.dto.response.AuthLoginResponse;
import com.smartparking.identity.dto.response.CheckPhoneResponse;
import com.smartparking.identity.dto.response.ForgotPasswordResponse;
import com.smartparking.identity.dto.response.ProfileResponse;
import com.smartparking.identity.entity.*;
import com.smartparking.identity.repository.*;
import com.smartparking.shared.exception.BusinessException;
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

    @Value("${app.demo.otp-expiration-minutes:5}")
    private int otpExpirationMinutes;
    @Value("${app.demo.otp-max-try:5}")
    private int MAX_OTP_TRY;

    public AuthLoginResponse login(String username, String password) {
        username = formatPhoneToE164(username);

        Map<String, Object> response;
        try {
            response = supabaseClient.loginWithPassword(username, password);
        } catch (Exception e) {
            log.warn("CẢNH BÁO BẢO MẬT: Đăng nhập thất bại cho SĐT: {}. Lỗi từ Supabase: {}", username, e.getMessage());
            throw new IllegalArgumentException("Số điện thoại hoặc mật khẩu không chính xác!");
        }
        String accessToken = (String) response.get("access_token");
        Map<String, Object> user = (Map<String, Object>) response.get("user");
        String supabaseId_String = (String) user.get("id");
        UUID supabaseId = UUID.fromString(supabaseId_String);

        String finalUsername = username;
        Account localAccount = accountRepository.findBySupabaseId(supabaseId)
                .orElseGet(() -> {
                    log.info("Tài khoản có trên Supabase nhưng chưa có ở Local. Tiến hành Auto-sync...");
                    Account newAcc = new Account();
                    newAcc.setUsername(finalUsername);
                    newAcc.setAccountType(AccountType.EMPLOYEE);
                    return accountRepository.save(newAcc);
                });

        if (localAccount.getStatus() == GeneralStatus.LOCKED) {
            log.warn("CẢNH BÁO BẢO MẬT: Tài khoản SĐT {} ĐANG BỊ KHÓA nhưng cố gắng đăng nhập!", username);
            throw new BusinessException("Tài khoản của bạn đã bị khóa hoặc vô hiệu hóa. Vui lòng liên hệ Admin!");
        }
        List<String> permissions = roleFunctionActionRepo.findPermissionCodesByRoleId(localAccount.getRoleId());

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
        } catch (Exception e) {
            log.warn("Supabase logout warning (Token may already be expired or invalid): {}", e.getMessage());
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
        return phone;
    }

    @Transactional
    public CheckPhoneResponse checkPhone(String rawPhone) {
        final String phone = formatPhoneToE164(rawPhone);
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
    public AuthLoginResponse createSupabaseAccount(String phone, String otpCode, String password) {

        phone = formatPhoneToE164(phone);
        this.verifyOtp(phone, otpCode);

        Account account = accountRepository.findByUsername(phone)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại trong hệ thống."));

        if (account.getSupabaseId() != null) {
            throw new IllegalArgumentException("Tài khoản này đã được kích hoạt. Vui lòng đăng nhập.");
        }

        Map<String, Object> appMetadata = buildUserMetadata(account.getId());

        String newSupabaseId;
        try {
            newSupabaseId = supabaseClient.createAdminUser(phone, password, appMetadata);
            log.info("Tạo tài khoản Supabase thành công cho SĐT: {}. Supabase ID: {}", phone, newSupabaseId);
        } catch (Exception e) {
            log.error("CRITICAL: Lỗi gọi API tạo tài khoản Supabase cho SĐT: {}. Nguyên nhân: {}", phone, e.getMessage());
            throw e;
        }

        account.setSupabaseId(UUID.fromString(newSupabaseId));
        account.setStatus(GeneralStatus.ACTIVE);
        accountRepository.save(account);

        try {
            Map<String, Object> loginResponse = supabaseClient.loginWithPassword(phone, password);
            String accessToken = (String) loginResponse.get("access_token");

            return AuthLoginResponse.builder()
                    .accessToken(accessToken)
                    .accountType(account.getAccountType().name())
                    .accountId(account.getId())
                    .build();
        } catch (Exception e) {
            log.error("Tạo user thành công nhưng Auto-login thất bại cho SĐT: {}", phone, e);
            throw new RuntimeException("Tạo tài khoản thành công nhưng không thể tự động đăng nhập. Vui lòng đăng nhập thủ công.", e);
        }
    }

    public void syncMetadataToSupabase(Account account) {
        if (account == null || account.getSupabaseId() == null) {
            log.warn("Bỏ qua đồng bộ: Account null hoặc chưa có SupabaseID");
            return;
        }

        try {
            log.info("Bắt đầu đồng bộ Metadata lên Supabase cho Account ID: {}", account.getId());

            Map<String, Object> metadata = buildUserMetadata(account.getId());
            supabaseClient.updateUserMetadata(account.getSupabaseId().toString(), metadata);

            log.info("Đồng bộ thành công Metadata cho Account ID: {}", account.getId());
        } catch (Exception e) {
            log.error("CRITICAL: Đồng bộ Metadata thất bại cho account: {}. Supabase ID: {}. Lỗi: {}",
                    account.getId(), account.getSupabaseId(), e.getMessage());
            throw new RuntimeException("Lỗi đồng bộ dữ liệu với hệ thống Auth", e);
        }
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
        if ((type == null)) {
            type = OtpType.ACTIVATION;
        }

        Integer accountId = null;
        if (type == OtpType.FORGOT_PASSWORD) {
            Account account = accountRepository.findByUsername(phone)
                    .orElseThrow(() -> new IllegalArgumentException("Số điện thoại chưa được đăng ký trong hệ thống!"));
            accountId = account.getId();
        } else if (type == OtpType.ACTIVATION) {
            accountId = accountRepository.findByUsername(phone).map(Account::getId).orElse(null);
        }

        otpOneTimeRepository.invalidateOldOtps(phone);
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
        try {
            String smsMessage = String.format("Ma xac nhan cua ban la: %s. Ma co hieu luc trong %d phut. Khong chia se cho bat ky ai.",
                    otpCode, otpExpirationMinutes);
            smsService.sendSms(adminPhone, smsMessage);
            log.info("Gửi OTP thành công cho SĐT: {} (Loại: {})", phone, type);

        } catch (Exception e) {
            log.error("LỖI GỬI SMS: Không thể gửi OTP cho SĐT: {}. Lỗi: {}", phone, e.getMessage());
            throw new RuntimeException("Hệ thống tạm thời không thể gửi tin nhắn. Vui lòng thử lại sau.");
        }
        log.info("Đã gửi OTP [{}] cho SĐT: {} (Type: {})", otpCode, phone, type);

        return ForgotPasswordResponse.builder()
                .phone(phone.trim())
                .message("Mã xác thực đã được gửi đến số điện thoại của bạn.")
                .expireInMinutes(otpExpirationMinutes)
                .build();
    }


    @Transactional
    public void verifyOtp(String phone, String inputOtpCode) {


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


    public AuthLoginResponse resetPassword(String phone, String otpCode, String newPassword) {
        phone = formatPhoneToE164(phone);
        this.verifyOtp(phone, otpCode);

        Account account = accountRepository.findByUsername(phone)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại trong hệ thống."));

        if (account.getSupabaseId() == null) {
            throw new IllegalArgumentException("Tài khoản này chưa được kích hoạt. Vui lòng liên hệ Admin hoặc Đăng ký.");
        }
        executeResetPassword(phone, otpCode, newPassword, account);

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

    @Transactional
    public void executeResetPassword(String phone, String otpCode, String newPassword, Account account) {
        this.verifyOtp(phone, otpCode); // Đánh dấu dùng OTP (Local DB)

        try {
            supabaseClient.updateUserPassword(account.getSupabaseId().toString(), newPassword); // Gọi Supabase
            log.info("SĐT {} đã reset mật khẩu thành công qua OTP!", phone);
        } catch (Exception e) {
            log.error("Lỗi khi reset mật khẩu trên Supabase cho SĐT: {}", phone, e);
            throw new RuntimeException("Có lỗi xảy ra khi đổi mật khẩu trên máy chủ. Vui lòng thử lại!");
        }
    }

}
