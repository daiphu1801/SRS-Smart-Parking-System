import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/auth/viewmodels/auth_viewmodel.dart';
import 'package:smart_parking_mobile/features/auth/views/widgets/auth_widgets.dart';

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
    final l10n = AppLocalizations.of(context)!;
    final vm = context.read<AuthViewModel>();
    final success = await vm.verifyAndProceedPhone(_phoneCtrl.text);
    if (!mounted) return;

    if (!success) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(vm.errorMessage ?? l10n.phoneVerifyError),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        leading: context.canPop()
            ? IconButton(
                icon: const Icon(Icons.arrow_back),
                onPressed: () => context.pop(),
              )
            : null,
        title: Text(l10n.login),
        centerTitle: true,
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: AppTheme.pagePadding),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const Spacer(flex: 2),
              AuthHeader(
                title: l10n.enterPhoneNumber,
                subtitle: l10n.phoneVerifyMessage,
              ),
              const SizedBox(height: 32),
              AppTextField(
                label: l10n.phoneLabel2,
                placeholder: l10n.phoneExample,
                controller: _phoneCtrl,
                keyboardType: TextInputType.phone,
              ),
              const SizedBox(height: 20),
              Consumer<AuthViewModel>(
                builder: (context, vm, _) => AppFilledButton(
                  label: l10n.continueBtn,
                  isLoading: vm.isLoading,
                  onPressed: _phoneCtrl.text.trim().isEmpty
                      ? null
                      : _onContinue,
                ),
              ),
              const SizedBox(height: 16),
              Center(
                child: Text(
                  l10n.noAccountMessage,
                  style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
                  textAlign: TextAlign.center,
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
