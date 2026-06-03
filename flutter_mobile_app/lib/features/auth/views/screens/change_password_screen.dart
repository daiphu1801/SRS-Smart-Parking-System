import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/auth/viewmodels/auth_viewmodel.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';

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

    final l10n = AppLocalizations.of(context)!;
    final vm = context.read<AuthViewModel>();
    final success = await vm.changePassword(
      _oldPassCtrl.text.trim(),
      _newPassCtrl.text.trim(),
    );

    if (!mounted) return;

    if (success) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(l10n.changePasswordSuccess),
          backgroundColor: Colors.green,
        ),
      );
      context.pop();
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(vm.errorMessage ?? l10n.changePasswordFailed),
          backgroundColor: Colors.red,
        ),
      );
      vm.resetActionState();
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.changePasswordTitle, style: AppTheme.heading1),
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
                              Text(l10n.accountSecurity, style: AppTheme.heading3),
                              const SizedBox(height: 4),
                              Text(
                                l10n.passwordMinLength,
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
                  Text(l10n.currentPassword, style: AppTheme.heading3),
                  const SizedBox(height: 12),
                  _PasswordField(
                    controller: _oldPassCtrl,
                    hintText: l10n.enterCurrentPassword,
                    isVisible: _oldPassVisible,
                    onToggleVisibility: () => setState(() => _oldPassVisible = !_oldPassVisible),
                    validator: (v) {
                      if (v == null || v.isEmpty) return l10n.pleaseEnterOldPassword;
                      return null;
                    },
                  ),
                  const SizedBox(height: AppTheme.sectionGap),

                  // New password
                  Text(l10n.newPassword, style: AppTheme.heading3),
                  const SizedBox(height: 12),
                  _PasswordField(
                    controller: _newPassCtrl,
                    hintText: l10n.enterNewPassword,
                    isVisible: _newPassVisible,
                    onToggleVisibility: () => setState(() => _newPassVisible = !_newPassVisible),
                    validator: (v) {
                      if (v == null || v.isEmpty) return l10n.pleaseEnterNewPassword;
                      if (v.length < 6) return l10n.passwordTooShort;
                      if (v == _oldPassCtrl.text) {
                        return l10n.passwordMustDiffer;
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: AppTheme.sectionGap),

                  // Confirm password
                  Text(l10n.confirmNewPassword, style: AppTheme.heading3),
                  const SizedBox(height: 12),
                  _PasswordField(
                    controller: _confirmCtrl,
                    hintText: l10n.reenterNewPassword,
                    isVisible: _confirmVisible,
                    onToggleVisibility: () => setState(() => _confirmVisible = !_confirmVisible),
                    validator: (v) {
                      if (v == null || v.isEmpty) return l10n.pleaseConfirmPassword;
                      if (v != _newPassCtrl.text) return l10n.passwordMismatch;
                      return null;
                    },
                  ),
                  const SizedBox(height: 32),

                  AppFilledButton(
                    label: isLoading ? l10n.processing : l10n.confirmChangePassword,
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
