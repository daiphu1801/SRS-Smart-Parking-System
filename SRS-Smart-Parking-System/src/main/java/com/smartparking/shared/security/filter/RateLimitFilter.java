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

    private final RateLimitManager rateLimitManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String clientIp = getClientIp(request);

        String globalKey = "global_limit:" + clientIp;
        RateLimitManager.RateLimitResult globalResult = rateLimitManager.tryConsume(globalKey, 50, 1);

        if (!globalResult.isAllowed()) {
            log.warn("🚨 GLOBAL LIMIT: IP {} đang spam hệ thống quá nhanh!", clientIp);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 429, \"message\": \"Thao tác quá nhanh, vui lòng thử lại sau!\"}");
            return;
        }

        if (uri.startsWith("/api/v1/auth/send-otp") || uri.startsWith("/api/v1/auth/login")) {


            String rateLimitKey = "auth_spam:" + clientIp;

            RateLimitManager.RateLimitResult result = rateLimitManager.tryConsume(rateLimitKey, 3, 60);

            if (result.isAllowed()) {
                response.setHeader("X-Rate-Limit-Remaining", String.valueOf(result.remainingTokens()));
                filterChain.doFilter(request, response);
            } else {
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