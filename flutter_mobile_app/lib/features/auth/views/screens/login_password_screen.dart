import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';
import 'package:smart_parking_mobile/core/router/app_router.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/auth/viewmodels/auth_viewmodel.dart';
import 'package:smart_parking_mobile/features/auth/views/widgets/auth_widgets.dart';

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
    final l10n = AppLocalizations.of(context)!;
    final vm = context.read<AuthViewModel>();
    final success = await vm.loginWithPassword(_passwordCtrl.text);
    if (!mounted) return;

    if (success) {
      context.go(AppRoutes.customerHome);
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(vm.errorMessage ?? l10n.loginFailed)),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Consumer<AuthViewModel>(
      builder: (context, vm, _) {
        final isActivation = vm.isActivationFlow;
        final title = isActivation ? l10n.createPassword : l10n.enterPassword;
        final subtitle = isActivation
            ? l10n.createPasswordFor(vm.phone)
            : l10n.loginWith(vm.phone);
        final buttonLabel = isActivation ? l10n.activateAccount : l10n.login;

        return Scaffold(
          appBar: AppBar(
            leading: IconButton(
              icon: const Icon(Icons.arrow_back),
              onPressed: () => context.read<AuthViewModel>().resetLoginFlow(),
            ),
            title: Text(isActivation ? l10n.activateAccount : l10n.login),
            centerTitle: true,
          ),
          body: SafeArea(
            child: Padding(
              padding: const EdgeInsets.symmetric(
                horizontal: AppTheme.pagePadding,
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  const Spacer(flex: 2),
                  AuthHeader(title: title, subtitle: subtitle),
                  const SizedBox(height: 32),
                  AppTextField(
                    label: l10n.password,
                    placeholder: isActivation
                        ? l10n.createNewPassword
                        : l10n.enterPassword,
                    controller: _passwordCtrl,
                    obscureText: _obscure,
                    suffixIcon: IconButton(
                      icon: Icon(
                        _obscure ? Icons.visibility_off : Icons.visibility,
                        color: AppTheme.subtle,
                      ),
                      onPressed: () => setState(() => _obscure = !_obscure),
                    ),
                  ),
                  const SizedBox(height: 20),
                  AppFilledButton(
                    label: buttonLabel,
                    isLoading: vm.isLoading,
                    onPressed: _passwordCtrl.text.trim().isEmpty
                        ? null
                        : _onLogin,
                  ),
                  if (!isActivation) ...[
                    const SizedBox(height: 16),
                    Center(
                      child: TextButton(
                        onPressed: () => context.push(AppRoutes.forgotPassword),
                        child: Text(l10n.forgotPassword),
                      ),
                    ),
                  ],
                  const Spacer(flex: 3),
                ],
              ),
            ),
          ),
        );
      },
    );
  }
}
