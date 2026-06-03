import 'package:smart_parking_mobile/core/network/api_client.dart';
import 'package:smart_parking_mobile/core/network/api_exception.dart';
import 'package:smart_parking_mobile/features/auth/models/auth_models.dart';

class AuthApiService {
  AuthApiService(this._apiClient);

  final ApiClient _apiClient;

  Future<CheckPhoneResponse> checkPhone(String phone) async {
    final encodedPhone = Uri.encodeComponent(phone.trim());
    final response = await _apiClient.get(
      '/api/v1/auth/check-phone/$encodedPhone',
    );
    return CheckPhoneResponse.fromJson(_asMap(response.data));
  }

  Future<LoginResponse> login(String identifier, String password) async {
    final response = await _apiClient.post(
      '/api/v1/auth/login',
      body: {'username': identifier.trim(), 'password': password},
    );
    return _loginResponse(
      response.data,
      code: response.code,
      message: response.message,
    );
  }

  Future<LoginResponse> register({
    required String phone,
    required String otpCode,
    required String password,
  }) async {
    final response = await _apiClient.post(
      '/api/v1/auth/register',
      body: {
        'phone': phone.trim(),
        'otp_code': otpCode.trim(),
        'password': password,
      },
    );
    return _loginResponse(
      response.data,
      code: response.code,
      message: response.message,
    );
  }

  Future<void> sendActivationOtp(String phone) async {
    await _apiClient.post(
      '/api/v1/auth/send-otp',
      body: {'phone': phone.trim(), 'type': 'ACTIVATION'},
    );
  }

  Future<void> forgotPassword(String phone) async {
    final encodedPhone = Uri.encodeComponent(phone.trim());
    await _apiClient.post('/api/v1/auth/forgot-password/$encodedPhone');
  }

  Future<LoginResponse> resetPassword(
    String phone,
    String otp,
    String newPassword,
  ) async {
    final response = await _apiClient.post(
      '/api/v1/auth/reset-password',
      body: {
        'phone': phone.trim(),
        'otpCode': otp.trim(),
        'newPassword': newPassword,
      },
    );
    return _loginResponse(
      response.data,
      code: response.code,
      message: response.message,
    );
  }

  Future<LoginResponse> changePassword(
    String oldPassword,
    String newPassword,
  ) async {
    final response = await _apiClient.post(
      '/api/v1/auth/change-password',
      authenticated: true,
      body: {'oldPassword': oldPassword, 'newPassword': newPassword},
    );
    return _loginResponse(
      response.data,
      code: response.code,
      message: response.message,
    );
  }

  Future<UserProfile> getMe() async {
    final response = await _apiClient.get(
      '/api/v1/auth/me',
      authenticated: true,
    );
    return UserProfile.fromData(_asMap(response.data));
  }

  Future<void> logout() async {
    await _apiClient.post('/api/v1/auth/logout', authenticated: true);
  }

  LoginResponse _loginResponse(
    Object? data, {
    required int code,
    required String message,
  }) {
    final dataMap = _asMap(data);
    if ((dataMap['access_token'] ?? '').toString().isEmpty) {
      throw const ApiException('May chu khong tra ve access_token.');
    }
    return LoginResponse.fromData(dataMap, code: code, message: message);
  }

  Map<String, dynamic> _asMap(Object? value) {
    if (value is Map<String, dynamic>) return value;
    if (value is Map) return Map<String, dynamic>.from(value);
    return <String, dynamic>{};
  }
}
