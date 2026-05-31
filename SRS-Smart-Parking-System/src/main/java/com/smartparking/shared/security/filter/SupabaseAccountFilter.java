package com.smartparking.shared.security.filter;

import com.smartparking.identity.entity.*;
import com.smartparking.shared.dto.CustomAccountPrincipal;
import com.smartparking.identity.repository.CustomerRepository;
import com.smartparking.identity.repository.EmployeeRepository;
import com.smartparking.identity.repository.RoleFunctionActionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.smartparking.identity.repository.AccountRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupabaseAccountFilter extends OncePerRequestFilter {


    private final AccountRepository accountRepository;
    private final RoleFunctionActionRepository roleFunctionActionRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        // 1. Kiểm tra xem request có chứa JWT Token hợp lệ từ Supabase không
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {

            String supabaseId = jwtAuth.getToken().getSubject();

            try {
                Account account = accountRepository.findBySupabaseId(UUID.fromString(supabaseId)).orElse(null);

                if (account == null) {
                    // 🚨 Log cảnh báo: Có token hợp lệ nhưng lại không có account dưới DB? (Data bị lỗi đồng bộ)
                    log.warn("Bảo mật: Token JWT chứa supabaseId {} hợp lệ nhưng KHÔNG TỒN TẠI trong Database!", supabaseId);
                    sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Tài khoản không tồn tại trong hệ thống.");
                    return; // Ngắt request ngay tại đây
                }

                if (account.getStatus() == GeneralStatus.LOCKED) {
                    // 🚨 Còi báo động hú lên
                    log.warn("Bảo mật: Tài khoản {} (ID: {}) đang bị khóa nhưng cố gắng gọi API {}",
                            account.getUsername(), account.getId(), request.getRequestURI());

                    // Trả về JSON lỗi 403 và CHẶN ĐỨNG request
                    sendErrorResponse(response, HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin.");
                    return; // Ngắt request, không cho đi tới filterChain.doFilter
                }
                // Set accountId vào request attribute để các Controller bên dưới xài nếu cần
                request.setAttribute("accountId", account.getId());

                List<Integer> masterGroupIds = new ArrayList<>();
                List<Integer> memberGroupIds = new ArrayList<>();
                Integer customerId = null;
                Integer employeeId = null;

                if (account.getAccountType() == AccountType.EMPLOYEE) {
                    employeeId = employeeRepository.findByAccountId(account.getId())
                            .map(Employee::getId) // Hoặc Employee::getId
                            .orElse(null);
                } else if (account.getAccountType() == AccountType.CUSTOMER) {
                    customerId = customerRepository.findByAccountId(account.getId())
                            .map(Customer::getId)
                            .orElse(null);
                }

                // 2. Bóc tách an toàn app_metadata từ Token Payload
                Map<String, Object> appMetadata = jwtAuth.getToken().getClaim("app_metadata");

                if (appMetadata != null) {
                    // 2.1 Xử lý Master Group (List an toàn)
                    if (appMetadata.get("master_group_ids") instanceof List<?> rawMasterList) {
                        masterGroupIds = rawMasterList.stream()
                                .filter(java.util.Objects::nonNull)
                                .map(this::convertToInteger)
                                .toList();
                    }

                    // 2.2 Xử lý Member Group (List an toàn)
                    if (appMetadata.get("member_group_ids") instanceof List<?> rawMemberList) {
                        memberGroupIds = rawMemberList.stream()
                                .filter(java.util.Objects::nonNull)
                                .map(this::convertToInteger)
                                .toList();
                    }

                }

                // 3. KHỞI TẠO PRINCIPAL (Chứng minh thư chứa thông tin định danh cá nhân)
                CustomAccountPrincipal principal = new CustomAccountPrincipal(
                        account.getId(),
                        account.getRoleName(),
                        masterGroupIds,
                        memberGroupIds,
                        customerId,
                        employeeId
                );

                // =======================================================
                // 4. XỬ LÝ QUYỀN HẠN (AUTHORITIES - Thẻ ra vào API)
                // =======================================================
                List<GrantedAuthority> authorities = new ArrayList<>();

                // 4.1. Nhét quyền cấp bậc (Role) - Bắt buộc có tiền tố ROLE_
                authorities.add(new SimpleGrantedAuthority("ROLE_" + account.getRoleName()));

                // 4.2. Chọc DB lấy danh sách các hành động (Actions) và nhét thẳng vào làm quyền
                List<String> permissions = roleFunctionActionRepository.findPermissionCodesByRoleId(account.getRoleId());
                if (permissions != null && !permissions.isEmpty()) {
                    permissions.forEach(action -> authorities.add(new SimpleGrantedAuthority(action)));
                }

                // 5. Khởi tạo Token bảo mật của Spring và nhét vào Context
                var customAuth = new UsernamePasswordAuthenticationToken(
                        principal,   // Thông tin cá nhân nằm ở đây
                        null,        // Credentials (đã check JWT nên để null)
                        authorities  // FULL băng đạn Quyền hạn nằm ở đây
                );
                SecurityContextHolder.getContext().setAuthentication(customAuth);
            } catch(Exception e) {
                log.error("CRITICAL: Lỗi trích xuất quyền hạn từ DB cho supabaseId {}: {}", supabaseId, e.getMessage(), e);
                sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi xác thực hệ thống. Vui lòng thử lại sau!");
                return; // Ngắt request
            }

        }

        filterChain.doFilter(request, response);
    }

    private Integer convertToInteger(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue(); // Cân hết Long, Double, Short...
        } else if (obj instanceof String) {
            return Integer.valueOf((String) obj); // Lỡ Supabase ném về chuỗi "1"
        }
        throw new IllegalArgumentException("Không thể ép kiểu dữ liệu này sang Integer: " + obj);
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format("{\"code\": %d, \"message\": \"%s\"}", status.value(), message));
    }

}
