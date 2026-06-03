import 'package:flutter/material.dart';
import 'package:smart_parking_mobile/core/di/service_locator.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/features/auth/models/auth_flow_state.dart';
import 'package:smart_parking_mobile/features/auth/models/auth_models.dart';
import 'package:smart_parking_mobile/features/auth/repositories/auth_repository.dart';

class AuthViewModel extends ChangeNotifier {
  AuthViewModel({AuthRepository? authRepository})
    : _authRepository = authRepository ?? sl<AuthRepository>() {
    _initSession();
  }

  final AuthRepository _authRepository;

  ViewState<UserProfile> _profileState = const Idle();
  bool _isAuthenticated = false;

  ViewState<UserProfile> get profileState => _profileState;
  bool get isAuthenticated => _isAuthenticated;
  UserProfile? get profile => _profileState is Success<UserProfile>
      ? (_profileState as Success<UserProfile>).data
      : null;

  AuthStep _authStep = AuthStep.phone;
  String _phone = '';
  String _otp = '';
  String _password = '';

  AuthStep get authStep => _authStep;
  int get currentStep => _authStep.stepNumber;
  String get phone => _phone;
  bool get isActivationFlow => _authStep.isActivation;
  bool get isPasswordLoginFlow => _authStep == AuthStep.passwordLogin;

  ViewState<bool> _loginState = const Idle();
  ViewState<void> _actionState = const Idle();
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
    if (_phoneVerifyState is Failure) {
      return (_phoneVerifyState as Failure).message;
    }
    if (_otpSendState is Failure) return (_otpSendState as Failure).message;
    if (_otpVerifyState is Failure) return (_otpVerifyState as Failure).message;
    if (_profileState is Failure) return (_profileState as Failure).message;
    return null;
  }

  Future<void> _initSession() async {
    _isAuthenticated = await _authRepository.hasActiveSession();
    if (_isAuthenticated) {
      notifyListeners();
      await _loadProfile();
    }
  }

  Future<bool> verifyAndProceedPhone(String phone) async {
    _phone = phone.trim();
    _phoneVerifyState = const Loading();
    _otp = '';
    _password = '';
    _authStep = AuthStep.phone;
    notifyListeners();

    try {
      final response = await _authRepository.checkPhone(_phone);

      switch (response.action) {
        case CheckPhoneAction.requireLoginPassword:
          _authStep = AuthStep.passwordLogin;
          break;
        case CheckPhoneAction.requireOtpActivation:
          _authStep = AuthStep.otpActivation;
          break;
        case CheckPhoneAction.unknown:
          throw Exception(
            'May chu tra ve action khong ho tro: ${response.rawAction}',
          );
      }

      _phoneVerifyState = const Success(null);
      notifyListeners();
      return true;
    } catch (e) {
      _phoneVerifyState = Failure(_clean(e));
      notifyListeners();
      return false;
    }
  }

  Future<bool> sendOtp() async {
    _otpSendState = const Loading();
    notifyListeners();

    try {
      await _authRepository.sendActivationOtp(_phone);
      _authStep = AuthStep.otpActivation;
      _otpSendState = const Success(null);
      notifyListeners();
      return true;
    } catch (e) {
      _otpSendState = Failure(_clean(e));
      notifyListeners();
      return false;
    }
  }

  Future<bool> verifyAndProceedOtp(String otp) async {
    _otp = otp.trim();
    _otpVerifyState = const Loading();
    notifyListeners();

    try {
      if (_otp.isEmpty) {
        throw Exception('Vui long nhap ma OTP.');
      }
      _otpVerifyState = const Success(null);
      _authStep = AuthStep.createPassword;
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
      final profile = isActivationFlow
          ? await _authRepository.activateAccount(
              phone: _phone,
              otpCode: _otp,
              password: _password,
            )
          : await _authRepository.loginWithPassword(
              phone: _phone,
              password: _password,
            );

      _setAuthenticatedProfile(profile);
      _loginState = const Success(true);
      notifyListeners();
      return true;
    } catch (e) {
      _loginState = Failure(_clean(e));
      notifyListeners();
      return false;
    }
  }

  void resetLoginFlow() {
    _authStep = AuthStep.phone;
    _phone = '';
    _otp = '';
    _password = '';
    _phoneVerifyState = const Idle();
    _otpSendState = const Idle();
    _otpVerifyState = const Idle();
    _loginState = const Idle();
    notifyListeners();
  }

  Future<bool> login(String identifier, String password) async {
    _loginState = const Loading();
    notifyListeners();

    try {
      final profile = await _authRepository.loginWithPassword(
        phone: identifier.trim(),
        password: password.trim(),
      );
      _setAuthenticatedProfile(profile);
      _loginState = const Success(true);
      notifyListeners();
      return true;
    } catch (e) {
      _loginState = Failure(_clean(e));
      notifyListeners();
      return false;
    }
  }

  Future<void> logout() async {
    _actionState = const Loading();
    notifyListeners();

    await _authRepository.logout();
    _isAuthenticated = false;
    _profileState = const Idle();
    _loginState = const Idle();
    _actionState = const Idle();
    resetLoginFlow();
    notifyListeners();
  }

  Future<void> _loadProfile() async {
    _profileState = const Loading();
    notifyListeners();
    try {
      final profile = await _authRepository.getCurrentProfile();
      _profileState = Success(profile);
    } catch (e) {
      _profileState = Failure(_clean(e));
    }
    notifyListeners();
  }

  Future<bool> sendRegisterOtp(String phone) async {
    _actionState = const Loading();
    notifyListeners();
    try {
      await _authRepository.sendActivationOtp(phone.trim());
      _actionState = const Success(null);
      notifyListeners();
      return true;
    } catch (e) {
      _actionState = Failure(_clean(e));
      notifyListeners();
      return false;
    }
  }

  Future<bool> verifyRegisterOtp(
    String phone,
    String otp,
    String password,
  ) async {
    _actionState = const Loading();
    notifyListeners();
    try {
      final profile = await _authRepository.activateAccount(
        phone: phone.trim(),
        otpCode: otp.trim(),
        password: password.trim(),
      );
      _setAuthenticatedProfile(profile);
      _actionState = const Success(null);
      notifyListeners();
      return true;
    } catch (e) {
      _actionState = Failure(_clean(e));
      notifyListeners();
      return false;
    }
  }

  Future<bool> forgotPassword(String phone) async {
    _actionState = const Loading();
    notifyListeners();
    try {
      await _authRepository.forgotPassword(phone.trim());
      _actionState = const Success(null);
      notifyListeners();
      return true;
    } catch (e) {
      _actionState = Failure(_clean(e));
      notifyListeners();
      return false;
    }
  }

  Future<bool> resetPassword(
    String phone,
    String otp,
    String newPassword,
  ) async {
    _actionState = const Loading();
    notifyListeners();
    try {
      final profile = await _authRepository.resetPassword(
        phone: phone.trim(),
        otpCode: otp.trim(),
        newPassword: newPassword.trim(),
      );
      _setAuthenticatedProfile(profile);
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
      final profile = await _authRepository.changePassword(
        oldPassword: oldPassword.trim(),
        newPassword: newPassword.trim(),
      );
      _setAuthenticatedProfile(profile);
      _actionState = const Success(null);
      notifyListeners();
      return true;
    } catch (e) {
      _actionState = Failure(_clean(e));
      notifyListeners();
      return false;
    }
  }

  void _setAuthenticatedProfile(UserProfile profile) {
    _isAuthenticated = true;
    _profileState = Success(profile);
  }

  String _clean(Object e) => e.toString().replaceAll('Exception: ', '');

  void resetActionState() {
    _actionState = const Idle();
    notifyListeners();
  }
}
