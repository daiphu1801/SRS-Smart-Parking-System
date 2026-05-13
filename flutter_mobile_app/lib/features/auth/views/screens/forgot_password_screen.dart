import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/features/auth/viewmodels/auth_viewmodel.dart';

/// View: Forgot Password (2-step: phone → OTP + new password)
/// ViewModel: AuthViewModel
class ForgotPasswordScreen extends StatefulWidget {
  const ForgotPasswordScreen({super.key});

  @override
  State<ForgotPasswordScreen> createState() => _ForgotPasswordScreenState();
}

class _ForgotPasswordScreenState extends State<ForgotPasswordScreen> {
  int _step = 1;
  final _phoneCtrl = TextEditingController();
  final _otpCtrl = TextEditingController();
  final _passwordCtrl = TextEditingController();
  bool _obscure = true;

  @override
  void dispose() {
    _phoneCtrl.dispose();
    _otpCtrl.dispose();
    _passwordCtrl.dispose();
    super.dispose();
  }

  Future<void> _onNext() async {
    final vm = context.read<AuthViewModel>();

    if (_step == 1) {
      final ok = await vm.forgotPassword(_phoneCtrl.text.trim());
      if (!mounted) return;
      if (ok) {
        setState(() => _step = 2);
      } else {
        _showError(vm.errorMessage);
      }
    } else {
      final ok = await vm.resetPassword(
        _phoneCtrl.text.trim(),
        _otpCtrl.text.trim(),
        _passwordCtrl.text.trim(),
      );
      if (!mounted) return;
      if (ok) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Đặt lại mật khẩu thành công!')),
        );
        Navigator.pop(context);
      } else {
        _showError(vm.errorMessage);
      }
    }
  }

  void _showError(String? msg) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(msg ?? 'Đã có lỗi xảy ra')),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Quên mật khẩu'),
        iconTheme: const IconThemeData(color: AppTheme.primary),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(AppTheme.pagePadding),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              _StepIndicator(current: _step, total: 2),
              const SizedBox(height: 28),
              if (_step == 1) ...[
                Text('Nhập số điện thoại', style: AppTheme.heading3),
                const SizedBox(height: 8),
                Text('Chúng tôi sẽ gửi mã xác nhận để đặt lại mật khẩu.',
                    style: AppTheme.body.copyWith(color: AppTheme.subtle)),
                const SizedBox(height: 20),
                AppTextField(
                  label: 'Số điện thoại',
                  placeholder: 'Nhập số điện thoại',
                  controller: _phoneCtrl,
                  keyboardType: TextInputType.phone,
                ),
              ] else ...[
                Text('Đặt lại mật khẩu', style: AppTheme.heading3),
                const SizedBox(height: 8),
                Text(
                  'Nhập OTP đã gửi đến ${_phoneCtrl.text}.\n(Mock: nhập 123456)',
                  style: AppTheme.body.copyWith(color: AppTheme.subtle),
                ),
                const SizedBox(height: 20),
                AppTextField(
                  label: 'Mã OTP',
                  placeholder: 'Nhập 6 chữ số',
                  controller: _otpCtrl,
                  keyboardType: TextInputType.number,
                ),
                const SizedBox(height: 16),
                AppTextField(
                  label: 'Mật khẩu mới',
                  placeholder: 'Nhập mật khẩu mới',
                  controller: _passwordCtrl,
                  obscureText: _obscure,
                  suffixIcon: IconButton(
                    icon: Icon(_obscure ? Icons.visibility_off : Icons.visibility),
                    onPressed: () => setState(() => _obscure = !_obscure),
                  ),
                ),
              ],
              const SizedBox(height: 32),
              Consumer<AuthViewModel>(
                builder: (context, vm, _) {
                  final isLoading = vm.actionState is Loading;
                  return AppFilledButton(
                    label: _step == 1 ? 'Tiếp tục' : 'Đặt lại mật khẩu',
                    isLoading: isLoading,
                    onPressed: _onNext,
                  );
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _StepIndicator extends StatelessWidget {
  final int current;
  final int total;
  const _StepIndicator({required this.current, required this.total});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: List.generate(total, (i) {
        final active = i + 1 == current;
        final done = i + 1 < current;
        return Expanded(
          child: Container(
            margin: EdgeInsets.only(right: i < total - 1 ? 6 : 0),
            height: 4,
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(2),
              color: (active || done) ? AppTheme.primary : AppTheme.border,
            ),
          ),
        );
      }),
    );
  }
}
