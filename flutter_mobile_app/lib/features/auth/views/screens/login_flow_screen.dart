import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/features/auth/viewmodels/auth_viewmodel.dart';
import 'package:smart_parking_mobile/features/auth/views/screens/login_phone_screen.dart';
import 'package:smart_parking_mobile/features/auth/views/screens/login_otp_screen.dart';
import 'package:smart_parking_mobile/features/auth/views/screens/login_password_screen.dart';

/// Login Flow Wrapper
/// Manages the 3-step login flow using the global AuthViewModel.
/// Displays phone screen, OTP screen, or password screen based on current step.
class LoginFlowScreen extends StatelessWidget {
  const LoginFlowScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<AuthViewModel>(
      builder: (context, vm, _) {
        switch (vm.currentStep) {
          case 1:
            return const LoginPhoneScreen();
          case 2:
            return const LoginOtpScreen();
          case 3:
            return const LoginPasswordScreen();
          default:
            return const LoginPhoneScreen();
        }
      },
    );
  }
}
