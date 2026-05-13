import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/auth/viewmodels/auth_viewmodel.dart';

import 'package:smart_parking_mobile/features/auth/views/widgets/auth_widgets.dart';

/// Step 1: Phone Verification
/// User enters phone number, system checks if it exists.
class LoginPhoneScreen extends StatefulWidget {
  const LoginPhoneScreen({super.key});

  @override
  State<LoginPhoneScreen> createState() => _LoginPhoneScreenState();
}

class _LoginPhoneScreenState extends State<LoginPhoneScreen> {
  final _phoneCtrl = TextEditingController();

  @override
  void initState() {
    super.initState();
    _phoneCtrl.addListener(() => setState(() {}));
  }

  @override
  void dispose() {
    _phoneCtrl.dispose();
    super.dispose();
  }

  Future<void> _onContinue() async {
    final vm = context.read<AuthViewModel>();
    final success = await vm.verifyAndProceedPhone(_phoneCtrl.text);
    if (!mounted) return;

    if (!success) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(vm.errorMessage ?? 'Lỗi xác minh')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        leading: context.canPop()
            ? IconButton(
                icon: const Icon(Icons.arrow_back),
                onPressed: () => context.pop(),
              )
            : null,
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
              const AuthHeader(
                title: 'Nhập số điện thoại',
                subtitle: 'Chúng tôi sẽ gửi mã OTP để xác thực tài khoản của bạn',
              ),
              const Spacer(),
              const AuthStepIndicator(currentStep: 1),
              const SizedBox(height: 32),
              AppTextField(
                label: 'Số điện thoại',
                placeholder: 'Ví dụ: 0987654321',
                controller: _phoneCtrl,
                keyboardType: TextInputType.phone,
              ),
              const SizedBox(height: 20),
              // Continue button
              Consumer<AuthViewModel>(
                builder: (context, vm, _) => AppFilledButton(
                  label: 'Tiếp tục',
                  isLoading: vm.isLoading,
                  onPressed: _phoneCtrl.text.isEmpty ? null : _onContinue,
                ),
              ),
              const SizedBox(height: 16),
              // Help text
              Center(
                child: Text(
                  'Không có tài khoản? Tạo tài khoản mới',
                  style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
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
