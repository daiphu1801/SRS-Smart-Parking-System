import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/features/kiosk_payment/viewmodels/kiosk_payment_viewmodel.dart';

class InputPlateScreen extends StatefulWidget {
  const InputPlateScreen({super.key});

  @override
  State<InputPlateScreen> createState() => _InputPlateScreenState();
}

class _InputPlateScreenState extends State<InputPlateScreen> {
  final TextEditingController _plateController = TextEditingController();

  @override
  void dispose() {
    _plateController.dispose();
    super.dispose();
  }

  void _submit() async {
    final plate = _plateController.text.trim().toUpperCase();
    if (plate.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Vui lòng nhập biển số xe')),
      );
      return;
    }

    final viewModel = context.read<KioskPaymentViewModel>();
    final success = await viewModel.initiatePayment(plate);

    if (success && mounted) {
      context.go('/qr');
      _plateController.clear();
    } else if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(viewModel.errorMessage ?? 'Đã xảy ra lỗi'),
          backgroundColor: AppTheme.error,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppTheme.background,
      body: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 600),
          child: Padding(
            padding: const EdgeInsets.all(32.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const Icon(
                  Icons.directions_car,
                  size: 100,
                  color: AppTheme.primary,
                ),
                const SizedBox(height: 24),
                const Text(
                  'Thanh Toán Kiosk',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 32,
                    fontWeight: FontWeight.bold,
                    color: AppTheme.primary,
                  ),
                ),
                const SizedBox(height: 12),
                Text(
                  'Vui lòng nhập biển số xe của bạn để thanh toán',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 18,
                    color: AppTheme.subtle,
                  ),
                ),
                const SizedBox(height: 48),
                TextField(
                  controller: _plateController,
                  textAlign: TextAlign.center,
                  textCapitalization: TextCapitalization.characters,
                  style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
                  decoration: InputDecoration(
                    hintText: 'VD: 30A12345',
                    filled: true,
                    fillColor: Colors.white,
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(16),
                      borderSide: BorderSide.none,
                    ),
                    contentPadding: const EdgeInsets.symmetric(vertical: 24),
                  ),
                  onSubmitted: (_) => _submit(),
                ),
                const SizedBox(height: 24),
                Consumer<KioskPaymentViewModel>(
                  builder: (context, viewModel, child) {
                    return ElevatedButton(
                      onPressed: viewModel.isLoading ? null : _submit,
                      style: ElevatedButton.styleFrom(
                        padding: const EdgeInsets.symmetric(vertical: 20),
                        backgroundColor: AppTheme.primary,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(16),
                        ),
                      ),
                      child: viewModel.isLoading
                          ? const CircularProgressIndicator(color: Colors.white)
                          : const Text(
                              'Xác Nhận',
                              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.white),
                            ),
                    );
                  },
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
