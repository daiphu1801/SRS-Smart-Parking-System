package com.smartparking.shared.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // Bắt toàn bộ các hàm nằm trong package controller
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void controllerPointcut() {}

    // @Around sẽ bao bọc lấy cái hàm đang được gọi
    @Around("controllerPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();

        // 1. LOG TỰ ĐỘNG TRƯỚC KHI VÀO HÀM
        log.info("Enter: {} with arguments = {}", methodName, Arrays.toString(joinPoint.getArgs()));

        try {
            // Cho phép hàm gốc chạy
            Object result = joinPoint.proceed();

            // 2. LOG TỰ ĐỘNG SAU KHI HÀM CHẠY XONG
            long elapsedTime = System.currentTimeMillis() - start;
            log.info("Exit: {} completed in {} ms", methodName, elapsedTime);

            return result;
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument in {} : {}", methodName, Arrays.toString(joinPoint.getArgs()));
            throw e;
        }
    }
}