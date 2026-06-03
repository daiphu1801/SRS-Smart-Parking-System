import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/auth/viewmodels/auth_viewmodel.dart';
import 'package:smart_parking_mobile/features/auth/views/widgets/auth_widgets.dart';

class LoginOtpScreen extends StatefulWidget {
  const LoginOtpScreen({super.key});

  @override
  State<LoginOtpScreen> createState() => _LoginOtpScreenState();
}

class _LoginOtpScreenState extends State<LoginOtpScreen> {
  final _otpCtrl = TextEditingController();
  int _secondsRemaining = 60;
  bool _canResend = false;

  @override
  void initState() {
    super.initState();
    _otpCtrl.addListener(() => setState(() {}));
    _startResendTimer();
  }

  @override
  void dispose() {
    _otpCtrl.dispose();
    super.dispose();
  }

  void _startResendTimer() {
    _secondsRemaining = 60;
    _canResend = false;
    _tick();
  }

  void _tick() {
    Future.delayed(const Duration(seconds: 1), () {
      if (!mounted) return;
      setState(() => _secondsRemaining--);
      if (_secondsRemaining > 0) {
        _tick();
      } else {
        setState(() => _canResend = true);
      }
    });
  }

  Future<void> _onVerifyOtp() async {
    final l10n = AppLocalizations.of(context)!;
    final vm = context.read<AuthViewModel>();
    final success = await vm.verifyAndProceedOtp(_otpCtrl.text);
    if (!mounted) return;

    if (!success) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(vm.errorMessage ?? l10n.otpVerifyError)),
      );
    }
  }

  Future<void> _onResend() async {
    final l10n = AppLocalizations.of(context)!;
    final vm = context.read<AuthViewModel>();
    _otpCtrl.clear();
    final success = await vm.sendOtp();
    if (!mounted) return;

    if (success) {
      _startResendTimer();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(l10n.otpResent)),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(vm.errorMessage!)),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.read<AuthViewModel>().resetLoginFlow(),
        ),
        title: Text(l10n.activateAccount),
        centerTitle: true,
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: AppTheme.pagePadding),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const Spacer(flex: 2),
              Consumer<AuthViewModel>(
                builder: (context, vm, _) => AuthHeader(
                  title: l10n.enterOtp,
                  subtitle: l10n.otpSentTo(vm.phone),
                ),
              ),
              const SizedBox(height: 32),
              AppTextField(
                label: l10n.otpCode,
                placeholder: l10n.enter6Digits,
                controller: _otpCtrl,
                keyboardType: TextInputType.number,
              ),
              const SizedBox(height: 20),
              Consumer<AuthViewModel>(
                builder: (context, vm, _) => AppFilledButton(
                  label: l10n.continueBtn,
                  isLoading: vm.isLoading,
                  onPressed: _otpCtrl.text.trim().isEmpty ? null : _onVerifyOtp,
                ),
              ),
              const SizedBox(height: 16),
              Center(
                child: _canResend
                    ? TextButton(
                        onPressed: _onResend,
                        child: Text(l10n.resendOtp),
                      )
                    : Text(
                        l10n.resendOtpIn(_secondsRemaining),
                        style: AppTheme.bodySmall.copyWith(
                          color: AppTheme.subtle,
                        ),
                      ),
              ),
              const Spacer(flex: 3),
            ],
          ),
        ),
      ),
    );
  }
}
