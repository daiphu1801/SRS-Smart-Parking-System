import 'package:smart_parking_mobile/core/utils/local_storage.dart';
import 'package:smart_parking_mobile/features/auth/models/auth_models.dart';
import 'package:smart_parking_mobile/features/auth/services/auth_api_service.dart';

class AuthRepository {
  AuthRepository({
    required AuthApiService apiService,
    required LocalStorage storage,
  }) : _apiService = apiService,
       _storage = storage;

  final AuthApiService _apiService;
  final LocalStorage _storage;

  Future<bool> hasActiveSession() => _storage.isLoggedIn();

  Future<CheckPhoneResponse> checkPhone(String phone) {
    return _apiService.checkPhone(phone.trim());
  }

  Future<void> sendActivationOtp(String phone) {
    return _apiService.sendActivationOtp(phone.trim());
  }

  Future<void> forgotPassword(String phone) {
    return _apiService.forgotPassword(phone.trim());
  }

  Future<UserProfile> loginWithPassword({
    required String phone,
    required String password,
  }) async {
    final response = await _apiService.login(phone.trim(), password);
    await _saveCustomerSession(response);
    return _apiService.getMe();
  }

  Future<UserProfile> activateAccount({
    required String phone,
    required String otpCode,
    required String password,
  }) async {
    final response = await _apiService.register(
      phone: phone.trim(),
      otpCode: otpCode.trim(),
      password: password,
    );
    await _saveCustomerSession(response);
    return _apiService.getMe();
  }

  Future<UserProfile> resetPassword({
    required String phone,
    required String otpCode,
    required String newPassword,
  }) async {
    final response = await _apiService.resetPassword(
      phone.trim(),
      otpCode.trim(),
      newPassword,
    );
    await _saveCustomerSession(response);
    return _apiService.getMe();
  }

  Future<UserProfile> changePassword({
    required String oldPassword,
    required String newPassword,
  }) async {
    final response = await _apiService.changePassword(oldPassword, newPassword);
    await _saveCustomerSession(response);
    return _apiService.getMe();
  }

  Future<UserProfile> getCurrentProfile() {
    return _apiService.getMe();
  }

  Future<void> logout() async {
    try {
      await _apiService.logout();
    } catch (_) {
      // Local logout should still complete if the server token is already invalid.
    } finally {
      await _storage.clearAuth();
    }
  }

  Future<void> _saveCustomerSession(LoginResponse response) async {
    if (response.accountType != 'CUSTOMER') {
      throw Exception('Chi khach hang moi duoc dang nhap vao ung dung nay.');
    }

    await _storage.saveToken(response.accessToken);
    await _storage.saveAuthMeta(
      accountType: response.accountType,
      accountId: response.accountId,
    );
  }
}
