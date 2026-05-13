import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/auth/viewmodels/auth_viewmodel.dart';

/// Màn hình Đổi mật khẩu — theo chuẩn MVVM.
/// Gọi [AuthViewModel.changePassword] và hiển thị kết quả.
class ChangePasswordScreen extends StatefulWidget {
  const ChangePasswordScreen({super.key});

  @override
  State<ChangePasswordScreen> createState() => _ChangePasswordScreenState();
}

class _ChangePasswordScreenState extends State<ChangePasswordScreen> {
  final _formKey = GlobalKey<FormState>();
  final _oldPassCtrl = TextEditingController();
  final _newPassCtrl = TextEditingController();
  final _confirmCtrl = TextEditingController();

  bool _oldPassVisible = false;
  bool _newPassVisible = false;
  bool _confirmVisible = false;

  @override
  void dispose() {
    _oldPassCtrl.dispose();
    _newPassCtrl.dispose();
    _confirmCtrl.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;

    final vm = context.read<AuthViewModel>();
    final success = await vm.changePassword(
      _oldPassCtrl.text.trim(),
      _newPassCtrl.text.trim(),
    );

    if (!mounted) return;

    if (success) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Đổi mật khẩu thành công!'),
          backgroundColor: Colors.green,
        ),
      );
      context.pop();
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(vm.errorMessage ?? 'Đổi mật khẩu thất bại.'),
          backgroundColor: Colors.red,
        ),
      );
      vm.resetActionState();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Đổi mật khẩu', style: AppTheme.heading1),
      ),
      body: Consumer<AuthViewModel>(
        builder: (context, vm, _) {
          final isLoading = vm.actionState is Loading;

          return SingleChildScrollView(
            padding: const EdgeInsets.all(AppTheme.pagePadding),
            child: Form(
              key: _formKey,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  // Header info card
                  AppCard(
                    padding: const EdgeInsets.all(16),
                    child: Row(
                      children: [
                        Container(
                          padding: const EdgeInsets.all(12),
                          decoration: BoxDecoration(
                            color: AppTheme.primary.withValues(alpha: 0.1),
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: const Icon(
                            Icons.lock_outline_rounded,
                            color: AppTheme.primary,
                            size: 24,
                          ),
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text('Bảo mật tài khoản', style: AppTheme.heading3),
                              const SizedBox(height: 4),
                              Text(
                                'Mật khẩu mới phải có ít nhất 6 ký tự.',
                                style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: AppTheme.sectionGap),

                  // Old password
                  Text('Mật khẩu hiện tại', style: AppTheme.heading3),
                  const SizedBox(height: 12),
                  _PasswordField(
                    controller: _oldPassCtrl,
                    hintText: 'Nhập mật khẩu hiện tại',
                    isVisible: _oldPassVisible,
                    onToggleVisibility: () => setState(() => _oldPassVisible = !_oldPassVisible),
                    validator: (v) {
                      if (v == null || v.isEmpty) return 'Vui lòng nhập mật khẩu cũ';
                      return null;
                    },
                  ),
                  const SizedBox(height: AppTheme.sectionGap),

                  // New password
                  Text('Mật khẩu mới', style: AppTheme.heading3),
                  const SizedBox(height: 12),
                  _PasswordField(
                    controller: _newPassCtrl,
                    hintText: 'Nhập mật khẩu mới',
                    isVisible: _newPassVisible,
                    onToggleVisibility: () => setState(() => _newPassVisible = !_newPassVisible),
                    validator: (v) {
                      if (v == null || v.isEmpty) return 'Vui lòng nhập mật khẩu mới';
                      if (v.length < 6) return 'Mật khẩu phải có ít nhất 6 ký tự';
                      if (v == _oldPassCtrl.text) {
                        return 'Mật khẩu mới không được trùng mật khẩu cũ';
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: AppTheme.sectionGap),

                  // Confirm password
                  Text('Xác nhận mật khẩu mới', style: AppTheme.heading3),
                  const SizedBox(height: 12),
                  _PasswordField(
                    controller: _confirmCtrl,
                    hintText: 'Nhập lại mật khẩu mới',
                    isVisible: _confirmVisible,
                    onToggleVisibility: () => setState(() => _confirmVisible = !_confirmVisible),
                    validator: (v) {
                      if (v == null || v.isEmpty) return 'Vui lòng xác nhận mật khẩu mới';
                      if (v != _newPassCtrl.text) return 'Mật khẩu xác nhận không khớp';
                      return null;
                    },
                  ),
                  const SizedBox(height: 32),

                  AppFilledButton(
                    label: isLoading ? 'Đang xử lý...' : 'Xác nhận đổi mật khẩu',
                    onPressed: isLoading ? null : _submit,
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}

// ── Private widgets ──────────────────────────────────────────────────────────

class _PasswordField extends StatelessWidget {
  final TextEditingController controller;
  final String hintText;
  final bool isVisible;
  final VoidCallback onToggleVisibility;
  final FormFieldValidator<String>? validator;

  const _PasswordField({
    required this.controller,
    required this.hintText,
    required this.isVisible,
    required this.onToggleVisibility,
    this.validator,
  });

  @override
  Widget build(BuildContext context) {
    return TextFormField(
      controller: controller,
      obscureText: !isVisible,
      validator: validator,
      decoration: InputDecoration(
        hintText: hintText,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: AppTheme.border),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: AppTheme.border),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: AppTheme.primary, width: 2),
        ),
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        suffixIcon: IconButton(
          icon: Icon(
            isVisible ? Icons.visibility_off_outlined : Icons.visibility_outlined,
            color: AppTheme.subtle,
          ),
          onPressed: onToggleVisibility,
        ),
      ),
    );
  }
}
