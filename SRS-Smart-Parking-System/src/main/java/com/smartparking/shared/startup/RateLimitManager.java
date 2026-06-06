package com.smartparking.shared.startup;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitManager {

    private final RedissonClient redissonClient;

    // RAM nội bộ: Chỉ dùng khi con Upstash Redis bị sập
    private final Map<String, Bucket> localFallbackCache = new ConcurrentHashMap<>();

    // Record Data (Java 16+) để truyền kết quả về cho Filter
    public record RateLimitResult(boolean isAllowed, long remainingTokens, boolean isFallback) {}

    /**
     * Dùng Redisson chính chủ để check Rate Limit
     */
    public RateLimitResult tryConsume(String key, long capacity, long seconds) {
        try {
            RRateLimiter rateLimiter = redissonClient.getRateLimiter("rate_limit:ip:" + key);

            boolean isNew = rateLimiter.trySetRate(RateType.OVERALL, capacity, seconds, RateIntervalUnit.SECONDS);

            // Chỉ set TTL lần đầu tạo key → IP bị block sẽ tự unblock sau đúng 'seconds' giây
            // (key tự xóa → lần request tiếp theo tạo lại với full token)
            if (isNew) {
                rateLimiter.expire(Duration.ofSeconds(seconds));
            }

            // Xin 1 lượt đi qua
            boolean isAllowed = rateLimiter.tryAcquire(1);
            long remaining = rateLimiter.availablePermits();

            return new RateLimitResult(isAllowed, remaining, false);

        } catch (Exception e) {
            log.warn("🔥 [RATE LIMIT FALLBACK] Mất kết nối Upstash! Cấp Xô tạm thời trên RAM cho IP: {}", key);

            Bucket fallbackBucket = getLocalFallbackBucket(key, capacity, seconds);
            io.github.bucket4j.ConsumptionProbe probe = fallbackBucket.tryConsumeAndReturnRemaining(1);

            return new RateLimitResult(probe.isConsumed(), probe.getRemainingTokens(), true);
        }
    }

    private Bucket getLocalFallbackBucket(String key, long capacity, long seconds) {
        return localFallbackCache.computeIfAbsent(key, k -> {
            Bandwidth limit = Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofSeconds(seconds)));
            return Bucket.builder().addLimit(limit).build();
        });
    }
}