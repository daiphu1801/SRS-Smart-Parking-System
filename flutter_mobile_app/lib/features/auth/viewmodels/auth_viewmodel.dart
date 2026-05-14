import 'package:flutter/material.dart';
import 'package:smart_parking_mobile/core/di/service_locator.dart';
import 'package:smart_parking_mobile/core/utils/local_storage.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/features/auth/models/auth_models.dart';
import 'package:smart_parking_mobile/features/auth/services/mock_auth_service.dart';

/// ViewModel for all authentication flows.
/// View layer only reads [state], [profile], and calls methods here.
class AuthViewModel extends ChangeNotifier {
  AuthViewModel({
    MockAuthService? authService,
    LocalStorage? storage,
  })  : _authService = authService ?? sl<MockAuthService>(),
        _storage = storage ?? sl<LocalStorage>() {
    _initSession();
  }

  final MockAuthService _authService;
  final LocalStorage _storage;

  // ── Auth State ─────────────────────────────────────────────────────────────
  ViewState<UserProfile> _profileState = const Idle();
  bool _isAuthenticated = false;

  ViewState<UserProfile> get profileState => _profileState;
  bool get isAuthenticated => _isAuthenticated;
  UserProfile? get profile =>
      _profileState is Success<UserProfile> ? (_profileState as Success<UserProfile>).data : null;

  // ── Step-based Login State ─────────────────────────────────────────────────
  int _currentStep = 1; // 1: Phone, 2: OTP, 3: Password
  String _phone = '';
  String _otp = '';
  String _password = '';

  int get currentStep => _currentStep;
  String get phone => _phone;

  // ── Operation states ───────────────────────────────────────────────────────
  ViewState<bool> _loginState = const Idle();
  ViewState<void> _actionState = const Idle(); // for logout, register, etc.
  
  // Specific states for step-based login
  ViewState<void> _phoneVerifyState = const Idle();
  ViewState<void> _otpSendState = const Idle();
  ViewState<void> _otpVerifyState = const Idle();

  ViewState<bool> get loginState => _loginState;
  ViewState<void> get actionState => _actionState;
  ViewState<void> get phoneVerifyState => _phoneVerifyState;
  ViewState<void> get otpSendState => _otpSendState;
  ViewState<void> get otpVerifyState => _otpVerifyState;

  bool get isLoading =>
      _loginState is Loading ||
      _actionState is Loading ||
      _phoneVerifyState is Loading ||
      _otpSendState is Loading ||
      _otpVerifyState is Loading;

  String? get errorMessage {
    if (_loginState is Failure) return (_loginState as Failure).message;
    if (_actionState is Failure) return (_actionState as Failure).message;
    if (_phoneVerifyState is Failure) return (_phoneVerifyState as Failure).message;
    if (_otpSendState is Failure) return (_otpSendState as Failure).message;
    if (_otpVerifyState is Failure) return (_otpVerifyState as Failure).message;
    return null;
  }

  // ── Initializer ────────────────────────────────────────────────────────────
  Future<void> _initSession() async {
    _isAuthenticated = await _storage.isLoggedIn();
    if (_isAuthenticated) {
      notifyListeners();
      await _loadProfile();
    }
  }

  // ── Multi-step Login (Modern Flow) ─────────────────────────────────────────
  
  Future<bool> verifyAndProceedPhone(String phone) async {
    _phone = phone.trim();
    _phoneVerifyState = const Loading();
    notifyListeners();

    try {
      await _authService.verifyPhone(_phone);
      _phoneVerifyState = const Success(null);
      notifyListeners();

      // Auto-send OTP to next step
      await sendOtp();
      return true;
    } catch (e) {
      _phoneVerifyState = Failure(_clean(e));
      notifyListeners();
      return false;
    }
  }

  Future<void> sendOtp() async {
    _otpSendState = const Loading();
    notifyListeners();

    try {
      await _authService.sendLoginOtp(_phone);
      _currentStep = 2;
      _otpSendState = const Success(null);
      notifyListeners();
    } catch (e) {
      _otpSendState = Failure(_clean(e));
      notifyListeners();
    }
  }

  Future<bool> verifyAndProceedOtp(String otp) async {
    _otp = otp.trim();
    _otpVerifyState = const Loading();
    notifyListeners();

    try {
      await _authService.verifyLoginOtp(_phone, _otp);
      _otpVerifyState = const Success(null);
      _currentStep = 3;
      notifyListeners();
      return true;
    } catch (e) {
      _otpVerifyState = Failure(_clean(e));
      notifyListeners();
      return false;
    }
  }

  Future<bool> loginWithPassword(String password) async {
    _password = password.trim();
    _loginState = const Loading();
    notifyListeners();

    try {
      final response = await _authService.loginWithPhoneAndPassword(_phone, _password);
      if (response.accountType != 'CUSTOMER') {
        throw Exception('Chỉ khách hàng mới được đăng nhập vào ứng dụng này.');
      }
      
      await _storage.saveToken(response.accessToken);
      await _storage.saveAuthMeta(
        accountType: response.accountType,
        accountId: response.accountId,
      );
      
      _isAuthenticated = true;
      _loginState = const Success(true);
      notifyListeners();
      await _loadProfile();
      return true;
    } catch (e) {
      _loginState = Failure(_clean(e));
      notifyListeners();
      return false;
    }
  }

  // ── Reset Flow ─────────────────────────────────────────────────────────────
  void resetLoginFlow() {
    _currentStep = 1;
    _phone = '';
    _otp = '';
    _password = '';
    _phoneVerifyState = const Idle();
    _otpSendState = const Idle();
    _otpVerifyState = const Idle();
    _loginState = const Idle();
    notifyListeners();
  }

  // ── Legacy Login (Direct) ──────────────────────────────────────────────────
  Future<bool> login(String identifier, String password) async {
    _loginState = const Loading();
    notifyListeners();

    try {
      final response = await _authService.login(identifier, password);
      if (response.accountType != 'CUSTOMER') {
        throw Exception('Chỉ khách hàng mới được đăng nhập vào ứng dụng này.');
      }
      await _storage.saveToken(response.accessToken);
      await _storage.saveAuthMeta(
        accountType: response.accountType,
        accountId: response.accountId,
      );
      _isAuthenticated = true;
      _loginState = const Success(true);
      notifyListeners();
      await _loadProfile();
      return true;
    } catch (e) {
      _loginState = Failure(_clean(e));
      notifyListeners();
      return false;
    }
  }

  // ── Logout ─────────────────────────────────────────────────────────────────
  Future<void> logout() async {
    _actionState = const Loading();
    notifyListeners();
    await _authService.logout();
    await _storage.clearAuth();
    _isAuthenticated = false;
    _profileState = const Idle();
    _loginState = const Idle();
    _actionState = const Idle();
    resetLoginFlow();
    notifyListeners();
  }

  // ── Profile ────────────────────────────────────────────────────────────────
  Future<void> _loadProfile() async {
    _profileState = const Loading();
    notifyListeners();
    try {
      final profile = await _authService.getMe();
      _profileState = Success(profile);
    } catch (e) {
      _profileState = Failure(_clean(e));
    }
    notifyListeners();
  }

  // ── Register ───────────────────────────────────────────────────────────────
  Future<bool> sendRegisterOtp(String phone) async {
    _actionState = const Loading();
    notifyListeners();
    try {
      await _authService.sendRegisterOtp(phone);
      _actionState = const Success(null);
      notifyListeners();
      return true;
    } catch (e) {
      _actionState = Failure(_clean(e));
      notifyListeners();
      return false;
    }
  }

  Future<bool> verifyRegisterOtp(String phone, String otp, String password) async {
    _actionState = const Loading();
    notifyListeners();
    try {
      await _authService.verifyRegisterOtp(phone, otp, password);
      _actionState = const Success(null);
      notifyListeners();
      return true;
    } catch (e) {
      _actionState = Failure(_clean(e));
      notifyListeners();
      return false;
    }
  }

  // ── Forgot / Reset Password ────────────────────────────────────────────────
  Future<bool> forgotPassword(String phone) async {
    _actionState = const Loading();
    notifyListeners();
    try {
      await _authService.forgotPassword(phone);
      _actionState = const Success(null);
      notifyListeners();
      return true;
    } catch (e) {
      _actionState = Failure(_clean(e));
      notifyListeners();
      return false;
    }
  }

  Future<bool> resetPassword(String phone, String otp, String newPassword) async {
    _actionState = const Loading();
    notifyListeners();
    try {
      await _authService.resetPassword(phone, otp, newPassword);
      _actionState = const Success(null);
      notifyListeners();
      return true;
    } catch (e) {
      _actionState = Failure(_clean(e));
      notifyListeners();
      return false;
    }
  }

  Future<bool> changePassword(String oldPassword, String newPassword) async {
    _actionState = const Loading();
    notifyListeners();
    try {
      await _authService.changePassword(oldPassword, newPassword);
      _actionState = const Success(null);
      notifyListeners();
      return true;
    } catch (e) {
      _actionState = Failure(_clean(e));
      notifyListeners();
      return false;
    }
  }

  // ── Helpers ────────────────────────────────────────────────────────────────
  String _clean(Object e) => e.toString().replaceAll('Exception: ', '');

  void resetActionState() {
    _actionState = const Idle();
    notifyListeners();
  }
}
