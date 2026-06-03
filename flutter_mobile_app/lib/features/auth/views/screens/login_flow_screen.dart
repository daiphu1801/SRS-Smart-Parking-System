import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/features/auth/models/auth_flow_state.dart';
import 'package:smart_parking_mobile/features/auth/viewmodels/auth_viewmodel.dart';
import 'package:smart_parking_mobile/features/auth/views/screens/login_phone_screen.dart';
import 'package:smart_parking_mobile/features/auth/views/screens/login_otp_screen.dart';
import 'package:smart_parking_mobile/features/auth/views/screens/login_password_screen.dart';

/// Login Flow Wrapper
/// Displays the correct auth screen based on the typed AuthStep state.
class LoginFlowScreen extends StatelessWidget {
  const LoginFlowScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<AuthViewModel>(
      builder: (context, vm, _) {
        switch (vm.authStep) {
          case AuthStep.phone:
            return const LoginPhoneScreen();
          case AuthStep.otpActivation:
            return const LoginOtpScreen();
          case AuthStep.passwordLogin:
          case AuthStep.createPassword:
            return const LoginPasswordScreen();
        }
      },
    );
  }
}
