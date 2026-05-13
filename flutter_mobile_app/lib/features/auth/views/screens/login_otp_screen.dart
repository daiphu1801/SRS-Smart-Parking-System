import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/auth/viewmodels/auth_viewmodel.dart';

import 'package:smart_parking_mobile/features/auth/views/widgets/auth_widgets.dart';

/// Step 2: OTP Verification
/// User enters the OTP sent to their phone.
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
    final vm = context.read<AuthViewModel>();
    final success = await vm.verifyAndProceedOtp(_otpCtrl.text);
    if (!mounted) return;

    if (!success) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(vm.errorMessage ?? 'Lỗi xác minh OTP')),
      );
    }
  }

  Future<void> _onResend() async {
    final vm = context.read<AuthViewModel>();
    _otpCtrl.clear();
    await vm.sendOtp();
    if (mounted) {
      _startResendTimer();
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Đã gửi lại mã OTP')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () {
            context.read<AuthViewModel>().resetLoginFlow();
          },
        ),
        title: const Text('Đăng nhập'),
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
                  title: 'Nhập mã OTP',
                  subtitle: 'Chúng tôi đã gửi mã OTP đến ${vm.phone}',
                ),
              ),
              const Spacer(),
              const AuthStepIndicator(currentStep: 2),
              const SizedBox(height: 32),
              AppTextField(
                label: 'Mã OTP',
                placeholder: 'Nhập 6 chữ số',
                controller: _otpCtrl,
                keyboardType: TextInputType.number,
              ),
              const SizedBox(height: 20),
              // Verify button
              Consumer<AuthViewModel>(
                builder: (context, vm, _) => AppFilledButton(
                  label: 'Xác minh',
                  isLoading: vm.isLoading,
                  onPressed: _otpCtrl.text.isEmpty ? null : _onVerifyOtp,
                ),
              ),
              const SizedBox(height: 16),
              // Resend
              Center(
                child: _canResend
                    ? TextButton(
                        onPressed: _onResend,
                        child: const Text('Gửi lại mã OTP'),
                      )
                    : Text(
                        'Gửi lại mã OTP trong $_secondsRemaining giây',
                        style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
                      ),
              ),
              const Spacer(flex: 3),
              // Mock OTP hint (for demo)
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.amber.shade50,
                  border: Border.all(color: Colors.amber.shade200),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Text(
                  '💡 Mock OTP: 123456',
                  style: TextStyle(color: Colors.amber, fontSize: 12),
                  textAlign: TextAlign.center,
                ),
              ),
              const SizedBox(height: 24),
            ],
          ),
        ),
      ),
    );
  }
}
