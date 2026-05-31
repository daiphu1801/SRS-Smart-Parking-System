package com.smartparking.shared.security.filter;
import com.smartparking.shared.startup.RateLimitManager;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    // Bộ nhớ đệm lưu "Cái Xô" của từng địa chỉ IP
    private final RateLimitManager rateLimitManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // Chỉ quét Rate Limit cho luồng Auth (OTP, Login)
        if (uri.startsWith("/api/v1/auth/send-otp") || uri.startsWith("/api/v1/auth/login")) {

            String clientIp = getClientIp(request);

            // Tạo Key định danh rõ ràng để không lẫn với Rate Limit của các API khác
            String rateLimitKey = "auth_spam:" + clientIp;

            // Truyền luật chơi: 3 lượt / 60 giây (Redisson sẽ tự xử lý)
            RateLimitManager.RateLimitResult result = rateLimitManager.tryConsume(rateLimitKey, 3, 60);

            if (result.isAllowed()) {
                // Hợp lệ -> Đi tiếp
                response.setHeader("X-Rate-Limit-Remaining", String.valueOf(result.remainingTokens()));
                filterChain.doFilter(request, response);
            } else {
                // Vi phạm -> Trả JSON 429
                log.warn("🚨 RATE LIMIT: IP {} đang bị block vì spam API {}!", clientIp, uri);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\": 429, \"message\": \"Người dùng thao tác quá nhiều, vui lòng đợi 1 phút!\"}");
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}