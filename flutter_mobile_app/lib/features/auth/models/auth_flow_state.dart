enum AuthStep { phone, otpActivation, passwordLogin, createPassword }

extension AuthStepUi on AuthStep {
  int get stepNumber {
    switch (this) {
      case AuthStep.phone:
        return 1;
      case AuthStep.otpActivation:
        return 2;
      case AuthStep.passwordLogin:
        return 2;
      case AuthStep.createPassword:
        return 3;
    }
  }

  bool get isActivation {
    return this == AuthStep.otpActivation || this == AuthStep.createPassword;
  }
}
