import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/router/app_router.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/auth/viewmodels/auth_viewmodel.dart';

import 'package:smart_parking_mobile/features/auth/views/widgets/auth_widgets.dart';

/// Step 3: Password & Login
/// User enters password and logs in after OTP verification.
class LoginPasswordScreen extends StatefulWidget {
  const LoginPasswordScreen({super.key});

  @override
  State<LoginPasswordScreen> createState() => _LoginPasswordScreenState();
}

class _LoginPasswordScreenState extends State<LoginPasswordScreen> {
  final _passwordCtrl = TextEditingController();
  bool _obscure = true;

  @override
  void initState() {
    super.initState();
    _passwordCtrl.addListener(() => setState(() {}));
  }

  @override
  void dispose() {
    _passwordCtrl.dispose();
    super.dispose();
  }

  Future<void> _onLogin() async {
    final vm = context.read<AuthViewModel>();
    final success = await vm.loginWithPassword(_passwordCtrl.text);
    if (!mounted) return;

    if (success) {
      context.go(AppRoutes.customerHome);
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(vm.errorMessage ?? 'Đăng nhập thất bại')),
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
                  title: 'Nhập mật khẩu',
                  subtitle: 'Hoàn tất đăng nhập với số ${vm.phone}',
                ),
              ),
              const Spacer(),
              const AuthStepIndicator(currentStep: 3),
              const SizedBox(height: 32),
              AppTextField(
                label: 'Mật khẩu',
                placeholder: 'Nhập mật khẩu',
                controller: _passwordCtrl,
                obscureText: _obscure,
                suffixIcon: IconButton(
                  icon: Icon(_obscure ? Icons.visibility_off : Icons.visibility,
                      color: AppTheme.subtle),
                  onPressed: () => setState(() => _obscure = !_obscure),
                ),
              ),
              const SizedBox(height: 20),
              // Login button
              Consumer<AuthViewModel>(
                builder: (context, vm, _) => AppFilledButton(
                  label: 'Đăng nhập',
                  isLoading: vm.isLoading,
                  onPressed: _passwordCtrl.text.isEmpty ? null : _onLogin,
                ),
              ),
              const SizedBox(height: 16),
              // Help text
              Center(
                child: TextButton(
                  onPressed: () => context.push(AppRoutes.forgotPassword),
                  child: const Text('Quên mật khẩu?'),
                ),
              ),
              const Spacer(flex: 3),
              // Mock password hint (for demo)
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.amber.shade50,
                  border: Border.all(color: Colors.amber.shade200),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Text(
                  '💡 Mock: Dùng số điện thoại 0987654321 với mật khẩu 123456',
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
