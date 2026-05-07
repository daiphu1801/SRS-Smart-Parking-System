package com.smartparking.identity.service;

import com.smartparking.identity.dto.response.AuthLoginResponse;
import com.smartparking.identity.dto.response.CheckPhoneResponse;
import com.smartparking.identity.dto.response.ProfileResponse;
import com.smartparking.identity.entity.Account;
import com.smartparking.identity.entity.AccountType;
import com.smartparking.identity.entity.Customer;
import com.smartparking.identity.entity.Employee;
import com.smartparking.identity.repository.AccountRepository;
import com.smartparking.identity.repository.CustomerRepository;
import com.smartparking.identity.repository.EmployeeRepository;
import com.smartparking.identity.entity.GeneralStatus;
import com.smartparking.shared.integration.SupabaseAuthClient;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final SupabaseAuthClient supabaseClient;

    @Value("${supabase.service-role-key}")
    private String supabaseServiceRoleKey;

    @Value("${supabase.url}")
    private String supabaseUrl;

    // 1. Login (Admin, Customer)
    public AuthLoginResponse login(String username, String password) {
        // Supabase login verifies the password and returns a JWT
        // Đăng nhập ở supabase trước, sau đó nhận token ở supabase
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

        return AuthLoginResponse.builder()
                .accessToken(accessToken)
                .accountType(localAccount.getAccountType().name())
                .accountId(localAccount.getId())
                .build();
    }

    // 2. Logout
    public void logout(String token) {
        try {
            supabaseClient.logout(token);
            log.info("Token invalidated on Supabase");
        } catch (Exception e) {
            // Bắt lỗi để app không crash. Lỗi này thường do token đã hết hạn sẵn.
            // Bất kể Supabase báo gì, ở phía Spring Boot ta vẫn cho qua vì mục đích cuối
            // cùng
            // vẫn là clear session ở client.
            log.warn("Supabase logout warning (Token may already be expired): {}", e.getMessage());
        }
    }

    // 3. Forgot Password
    public void forgotPassword(String phone) {
        accountRepository.findByUsername(phone).orElseThrow(() -> new IllegalArgumentException("Phone not registered"));
//        supabaseClient.sendOtp(formatPhoneToE164(phone));
        log.info("Requested Supabase OTP for Forgot Password: {}", phone);
    }

    // 4. Reset Password
//    public void resetPassword(String phone, String otp, String newPassword) {
//        // 1. Format lại SĐT cho chuẩn quốc tế (Bắt buộc với Supabase)
//        String formattedPhone = formatPhoneToE164(phone);
//
//        try {
//            // 2. Verify OTP với Supabase
//            Map<String, Object> verifyResponse = supabaseClient.verifyOtp(formattedPhone, otp);
//            String sessionToken = (String) verifyResponse.get("access_token");
//
//            // 3. MỞ COMMENT: Dùng session token để update password
//            // Lưu ý: Hàm updatePassword trong SupabaseAuthClient của bạn không trả về gì
//            // (void)
//            supabaseClient.updatePassword(sessionToken, newPassword);
//
//            log.info("Password reset via Supabase for phone: {}", formattedPhone);
//
//        } catch (Exception e) {
//            // 4. Cực kỳ quan trọng: Nếu OTP sai, RestTemplate sẽ ném lỗi 400 Bad Request.
//            // Phải catch lại để ném ra lỗi có ý nghĩa cho Frontend hiển thị.
//            log.error("Failed to reset password for {}. Error: {}", formattedPhone, e.getMessage());
//            throw new IllegalArgumentException("Mã OTP không hợp lệ hoặc đã hết hạn!");
//        }
//    }

    // 5. Change Password
    public void changePassword(String currentAccessToken, String newPassword) {
        try {

            supabaseClient.updatePassword(currentAccessToken, newPassword);
            log.info("Successfully changed password via Supabase");
        } catch (Exception e) {
            log.error("Lỗi khi đổi mật khẩu", e);
            throw new IllegalArgumentException("Lỗi hệ thống hoặc token đã hết hạn");
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

    // 7. Register Send OTP
//    public void registerSendOtp(String phone) {
//        // 1. Tìm xem SĐT này có nằm trong danh sách được cấp phép không
//        Optional<Employee> pendingEmployee = employeeRepository.findByPhone(phone);
//        Optional<Customer> pendingCustomer = customerRepository.findByPhone(phone);
//
//        // Nếu không nằm trong cả 2 bảng -> Đuổi về
//        if (pendingEmployee.isEmpty() && pendingCustomer.isEmpty()) {
//            throw new IllegalArgumentException("Số điện thoại chưa được Admin hoặc Master thêm vào hệ thống.");
//        }
//
//        // 2. Nếu có trong danh sách, kiểm tra xem đã tạo tài khoản (link account) chưa
//        if ((pendingEmployee.isPresent() && pendingEmployee.get().getAccountId() != null) ||
//                (pendingCustomer.isPresent() && pendingCustomer.get().getAccountId() != null)) {
//            throw new IllegalArgumentException("Tài khoản đã được đăng ký. Vui lòng đăng nhập.");
//        }
//
//        // 3. Vượt qua 2 vòng gửi xe -> Cho phép gửi OTP qua Supabase
//        supabaseClient.sendOtp(formatPhoneToE164(phone));
//        log.info("Requested Supabase OTP for Registration: {}", phone);
//    }

    // 8. Register Verify
//    @Transactional
//    public AuthLoginResponse registerVerify(String phone, String otp, String password) {
//        // 1. Xác minh OTP & Lấy UUID từ Supabase
//        Map<String, Object> verifyResponse = supabaseClient.verifyOtp(formatPhoneToE164(phone), otp);
//        String accessToken = (String) verifyResponse.get("access_token");
//        Map<String, Object> user = (Map<String, Object>) verifyResponse.get("user");
//        String supabaseId = (String) user.get("id");
//
//        // 2. Set Password người dùng vừa nhập lên Supabase
//        supabaseClient.updatePassword(accessToken, password);
//
//        // 3. Khởi tạo Account (Chưa save vội)
//        Account account;
//
//        // 4. PHÂN LUỒNG & LINK VÀO BẢN GHI CÓ SẴN
//        Optional<Employee> pendingEmployee = employeeRepository.findByPhone(phone);
//        Optional<Customer> pendingCustomer = customerRepository.findByPhone(phone);
//
//        if (pendingEmployee.isPresent() && pendingEmployee.get().getAccountId() == null) {
//            account = accountRepository.findByUsername(phone).get();
//            // Cập nhật Account là Employee và Save
//
//            account.setStatus(GeneralStatus.PENDING);
//            account = accountRepository.save(account);
//
//            // Móc Account ID vào vỏ Employee đã có sẵn
//            Employee emp = pendingEmployee.get();
//
//            emp.setAccountId(account.getId());
//            employeeRepository.save(emp);
//
//        } else if (pendingCustomer.isPresent() && pendingCustomer.get().getAccountId() == null) {
//
//            account = new Account();
//            account.setUsername(phone);
//            account.setSupabaseId(UUID.fromString(supabaseId));
//            account.setStatus(com.smartparking.identity.entity.GeneralStatus.ACTIVE);
//            // Cập nhật Account là Customer và Save
//            account.setAccountType(AccountType.CUSTOMER);
//            account.setRoleId(1);
//            account = accountRepository.save(account);
//
//            // Móc Account ID vào vỏ Customer (do Admin hoặc Master tạo)
//            Customer cus = pendingCustomer.get();
//            cus.setAccountId(account.getId());
//
//            customerRepository.save(cus);
//
//        } else {
//            // Cú double-check an toàn: Lỡ trong tích tắc database bị thay đổi
//            throw new IllegalArgumentException("Dữ liệu đăng ký không hợp lệ hoặc tài khoản đã tồn tại.");
//        }
//
//        return AuthLoginResponse.builder()
//                .accessToken(accessToken)
//                .accountType(account.getAccountType().name())
//                .accountId(account.getId())
//                .build();
//    }

    private String formatPhoneToE164(String phone) {
        if (phone.startsWith("0")) {
            return "+84" + phone.substring(1);
        }
        return phone.startsWith("+") ? phone : "+" + phone;
    }

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
            // Chưa có Supabase ID -> Đây là người mới do Admin tạo -> Yêu cầu Kích hoạt
            action = "REQUIRE_CREATE_PASSWORD";
        }

        // 3. Trả kết quả về cho Controller
        return CheckPhoneResponse.builder()
                .phone(phone)
                .action(action)
                .build();
    }

    @Transactional
    public AuthLoginResponse createSupabaseAccount(String phone, String otpCode, String password) {
        // 1. Kiểm tra "vỏ" Account ở Local DB
        Account account = accountRepository.findByUsername(phone)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại trong hệ thống."));

        if (account.getSupabaseId() != null) {
            throw new IllegalArgumentException("Tài khoản này đã được kích hoạt. Vui lòng đăng nhập.");
        }

        // 2. Gọi Client xử lý vụ Supabase (1 DÒNG DUY NHẤT!)
        String newSupabaseId = supabaseClient.createAdminUser(phone, password);

        // 3. Cập nhật Local Database
        account.setSupabaseId(UUID.fromString(newSupabaseId));
        account.setStatus(GeneralStatus.ACTIVE);
        accountRepository.save(account);

        // 4. Tự động Đăng nhập
        Map<String, Object> loginResponse = supabaseClient.loginWithPassword(phone, password);
        String accessToken = (String) loginResponse.get("access_token");

        // 5. Trả về kết quả
        return AuthLoginResponse.builder()
                .accessToken(accessToken)
                .accountType(account.getAccountType().name())
                .accountId(account.getId())
                .build();
    }
}
