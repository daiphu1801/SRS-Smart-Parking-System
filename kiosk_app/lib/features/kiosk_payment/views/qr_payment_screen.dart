import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/features/kiosk_payment/viewmodels/kiosk_payment_viewmodel.dart';
import 'package:intl/intl.dart';

class QrPaymentScreen extends StatefulWidget {
  const QrPaymentScreen({super.key});

  @override
  State<QrPaymentScreen> createState() => _QrPaymentScreenState();
}

class _QrPaymentScreenState extends State<QrPaymentScreen> {
  @override
  void initState() {
    super.initState();
    // Start the 5-minute timeout to return to the input screen
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<KioskPaymentViewModel>().startTimeoutTimer(() {
        if (mounted) {
          context.read<KioskPaymentViewModel>().reset();
          context.go('/');
        }
      });
    });
  }

  @override
  void dispose() {
    super.dispose();
  }

  void _close() {
    context.read<KioskPaymentViewModel>().reset();
    context.go('/');
  }

  @override
  Widget build(BuildContext context) {
    final viewModel = context.watch<KioskPaymentViewModel>();
    final response = viewModel.paymentResponse;

    if (response == null) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    final currencyFormatter = NumberFormat.currency(
      locale: 'vi_VN',
      symbol: 'đ',
    );

    return Scaffold(
      backgroundColor: AppTheme.background,
      body: SafeArea(
        child: Stack(
          children: [
            Center(
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 600),
                child: SingleChildScrollView(
                  child: Padding(
                    padding: const EdgeInsets.all(24.0),
                    child: Container(
                      padding: const EdgeInsets.all(32),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(24),
                        boxShadow: [
                          BoxShadow(
                            color: Colors.black.withValues(alpha: 0.1),
                            blurRadius: 20,
                            offset: const Offset(0, 10),
                          ),
                        ],
                      ),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const Text(
                            'Thanh Toán Kiosk',
                            style: TextStyle(
                              fontSize: 28,
                              fontWeight: FontWeight.bold,
                              color: AppTheme.primary,
                            ),
                          ),
                          const SizedBox(height: 12),
                          Text(
                            response.message.isNotEmpty
                                ? response.message
                                : 'Quét mã QR bên dưới để thanh toán',
                            textAlign: TextAlign.center,
                            style: TextStyle(
                              fontSize: 16,
                              color: AppTheme.subtle,
                            ),
                          ),
                          const SizedBox(height: 32),
                          if (response.checkoutUrl.isNotEmpty)
                            ClipRRect(
                              borderRadius: BorderRadius.circular(16),
                              child: Image.network(
                                response.checkoutUrl,
                                width: 300,
                                height: 300,
                                fit: BoxFit.contain,
                                errorBuilder: (context, error, stackTrace) =>
                                    Container(
                                      width: 300,
                                      height: 300,
                                      color: Colors.grey[200],
                                      child: const Icon(
                                        Icons.qr_code_scanner,
                                        size: 100,
                                        color: Colors.grey,
                                      ),
                                    ),
                              ),
                            )
                          else
                            Container(
                              width: 300,
                              height: 300,
                              color: Colors.grey[200],
                              child: const Center(
                                child: Text('Không tải được mã QR'),
                              ),
                            ),
                          const SizedBox(height: 32),
                          Container(
                            padding: const EdgeInsets.all(16),
                            decoration: BoxDecoration(
                              color: AppTheme.surface,
                              borderRadius: BorderRadius.circular(12),
                            ),
                            child: Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                const Text(
                                  'Số tiền:',
                                  style: TextStyle(
                                    fontSize: 18,
                                    fontWeight: FontWeight.w500,
                                  ),
                                ),
                                Text(
                                  currencyFormatter.format(response.amount),
                                  style: TextStyle(
                                    fontSize: 24,
                                    fontWeight: FontWeight.bold,
                                    color: AppTheme.error,
                                  ),
                                ),
                              ],
                            ),
                          ),
                          const SizedBox(height: 12),
                          Container(
                            padding: const EdgeInsets.all(16),
                            decoration: BoxDecoration(
                              color: AppTheme.surface,
                              borderRadius: BorderRadius.circular(12),
                            ),
                            child: Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                const Text(
                                  'Mã giao dịch:',
                                  style: TextStyle(
                                    fontSize: 18,
                                    fontWeight: FontWeight.w500,
                                  ),
                                ),
                                Text(
                                  response.payCode,
                                  style: TextStyle(
                                    fontSize: 18,
                                    fontWeight: FontWeight.bold,
                                    color: AppTheme.primary,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ),
            ),
            Positioned(
              top: 24,
              right: 24,
              child: IconButton(
                onPressed: _close,
                icon: const Icon(
                  Icons.close,
                  size: 36,
                  color: AppTheme.primary,
                ),
                style: IconButton.styleFrom(
                  backgroundColor: Colors.white,
                  shadowColor: Colors.black.withValues(alpha: 0.2),
                  elevation: 5,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
