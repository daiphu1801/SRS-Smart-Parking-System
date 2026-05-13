import 'dart:math';

import '../models/auth_models.dart';

class MockAuthService {
  // Mock data: existing phone numbers in the system
  static const _mockPhones = {
    '0987654321': '123456',    // phone: password
    '0912345678': '654321',
    '0901234567': 'password123',
  };

  // ── New Login Flow: Phone → OTP → Password ────────────────────────────────
  /// Step 1: Verify if phone exists in system
  Future<bool> verifyPhone(String phone) async {
    await Future.delayed(const Duration(milliseconds: 1000)); // Simulate network

    if (phone.isEmpty || !phone.startsWith('0') || phone.length != 10) {
      throw Exception('Số điện thoại không hợp lệ');
    }

    if (!_mockPhones.containsKey(phone)) {
      throw Exception('Số điện thoại không tồn tại trong hệ thống');
    }

    return true;
  }

  /// Step 2: Send OTP (mock data: always "123456")
  Future<void> sendLoginOtp(String phone) async {
    await Future.delayed(const Duration(milliseconds: 1500)); // Simulate network + SMS

    if (!_mockPhones.containsKey(phone)) {
      throw Exception('Số điện thoại không hợp lệ');
    }
    // In real app, server sends SMS. Here we just succeed.
  }

  /// Step 3: Verify OTP
  Future<void> verifyLoginOtp(String phone, String otp) async {
    await Future.delayed(const Duration(milliseconds: 800)); // Simulate network

    if (otp != '123456') {
      throw Exception('Mã OTP không chính xác');
    }
  }

  /// Step 4: Login with phone and password after OTP is verified
  Future<LoginResponse> loginWithPhoneAndPassword(String phone, String password) async {
    await Future.delayed(const Duration(milliseconds: 1200)); // Simulate network

    if (!_mockPhones.containsKey(phone)) {
      throw Exception('Số điện thoại không tồn tại');
    }

    if (_mockPhones[phone] != password) {
      throw Exception('Mật khẩu không chính xác');
    }

    return LoginResponse(
      status: 'success',
      message: 'Đăng nhập thành công',
      accessToken: 'mock_jwt_token_${Random().nextInt(10000)}',
      accountType: 'CUSTOMER',
      accountId: 1,
    );
  }

  // ── Old Login Flow (for backward compatibility) ────────────────────────────
  Future<LoginResponse> login(String identifier, String password) async {
    await Future.delayed(const Duration(milliseconds: 1500)); // Simulate network delay

    if (identifier == "0987654321" && password == "123456") {
      return LoginResponse(
        status: "success",
        message: "Thành công",
        accessToken: "mock_jwt_token_${Random().nextInt(10000)}",
        accountType: "CUSTOMER",
        accountId: 1,
      );
    } else {
      throw Exception("Tài khoản hoặc mật khẩu không chính xác.");
    }
  }

  Future<void> logout() async {
    await Future.delayed(const Duration(milliseconds: 500));
  }

  Future<void> forgotPassword(String phone) async {
    await Future.delayed(const Duration(milliseconds: 1000));
    if (phone.isEmpty) throw Exception("Số điện thoại không hợp lệ");
  }

  Future<void> resetPassword(String phone, String otp, String newPassword) async {
    await Future.delayed(const Duration(milliseconds: 1000));
    if (otp != "123456") throw Exception("Mã OTP không chính xác");
  }

  Future<void> changePassword(String oldPassword, String newPassword) async {
    await Future.delayed(const Duration(milliseconds: 1000));
    if (oldPassword != "123456") throw Exception("Mật khẩu cũ không đúng");
  }

  Future<UserProfile> getMe() async {
    await Future.delayed(const Duration(milliseconds: 1000));
    return UserProfile(
      accountId: 1,
      username: "0987654321",
      accountType: "CUSTOMER",
      fullName: "Nguyễn Văn Khách Hàng",
      phone: "0987654321",
      email: "khachhang@example.com",
    );
  }

  Future<void> sendRegisterOtp(String phone) async {
    await Future.delayed(const Duration(milliseconds: 1000));
  }

  Future<void> verifyRegisterOtp(String phone, String otp, String password) async {
    await Future.delayed(const Duration(milliseconds: 1000));
    if (otp != "123456") throw Exception("Mã OTP không chính xác");
  }
}
