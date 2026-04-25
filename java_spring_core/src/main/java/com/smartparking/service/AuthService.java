package com.smartparking.service;

import com.smartparking.entity.Account;
import com.smartparking.integration.ZaloZNSService;
import com.smartparking.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AccountRepository accountRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final ZaloZNSService znsService;

    private final ConcurrentHashMap<String, String> otpStore = new ConcurrentHashMap<>();

    public void sendOtp(String phone) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStore.put(phone, otp);
        znsService.sendOtp(phone, otp);
        log.info("OTP generated for {} (remove this log in production)", phone);
    }

    public AuthResponse verifyOtpAndLogin(String phone, String otp) {
        String stored = otpStore.get(phone);
        if (!otp.equals(stored)) throw new IllegalArgumentException("Invalid or expired OTP");
        otpStore.remove(phone);

        Account account = accountRepository.findByUsername(phone)
            .orElseGet(() -> accountRepository.save(Account.createCustomer(phone)));

        String token = jwtService.generateToken(account.getId(), account.getRoleName(), account.getAccountType().name());
        return AuthResponse.builder()
            .token(token).role(account.getRoleName())
            .accountType(account.getAccountType().name()).accountId(account.getId()).build();
    }

    public AuthResponse employeeLogin(String username, String password) {
        Account account = accountRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (!passwordEncoder.matches(password, account.getPasswordHash()))
            throw new IllegalArgumentException("Invalid credentials");

        String token = jwtService.generateToken(account.getId(), account.getRoleName(), account.getAccountType().name());
        return AuthResponse.builder()
            .token(token).role(account.getRoleName())
            .accountType(account.getAccountType().name()).accountId(account.getId()).build();
    }
}
