import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_payment/viewmodels/payment_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_payment/models/payment_models.dart';
import 'package:smart_parking_mobile/features/customer_payment/views/widgets/payment_widgets.dart';

/// Màn hình hiển thị lại QR của một hóa đơn đang PENDING
/// (Tiếp tục thanh toán từ màn hình Lịch sử)
class ResumePaymentScreen extends StatefulWidget {
  final PaymentResponse payment;

  const ResumePaymentScreen({super.key, required this.payment});

  @override
  State<ResumePaymentScreen> createState() => _ResumePaymentScreenState();
}

class _ResumePaymentScreenState extends State<ResumePaymentScreen> {
  @override
  void initState() {
    super.initState();
    // Nạp paymentId vào ViewModel để nút "Tôi đã chuyển khoản" biết gọi API nào
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<PaymentViewModel>().loadExistingPayment(widget.payment);
    });
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final payment = widget.payment;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.qrPayment),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () {
            context.read<PaymentViewModel>().resetState();
            context.pop();
          },
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            const SizedBox(height: 16.0),
            Text(l10n.scanToPayTitle, style: AppTheme.heading2),
            const SizedBox(height: 8.0),
            Text(
              l10n.scanToPaySubtitle,
              style: AppTheme.body.copyWith(color: AppTheme.subtle),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 32.0),

            // QR Code từ VietQR (URL từ Backend)
            Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(AppTheme.radiusCard),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withValues(alpha: 0.05),
                    blurRadius: 10,
                    offset: const Offset(0, 4),
                  ),
                ],
              ),
              child: ClipRRect(
                borderRadius: BorderRadius.circular(8.0),
                child: payment.checkoutUrl != null
                    ? Image.network(
                        payment.checkoutUrl!,
                        width: 240,
                        height: 240,
                        fit: BoxFit.contain,
                        loadingBuilder: (context, child, progress) {
                          if (progress == null) return child;
                          return const SizedBox(
                            width: 240,
                            height: 240,
                            child: Center(child: CircularProgressIndicator()),
                          );
                        },
                        errorBuilder: (context, error, stackTrace) => Container(
                          width: 240,
                          height: 240,
                          color: AppTheme.surface,
                          child: const Icon(
                            Icons.qr_code,
                            size: 80,
                            color: Colors.grey,
                          ),
                        ),
                      )
                    : Container(
                        width: 240,
                        height: 240,
                        color: AppTheme.surface,
                        child: Center(
                          child: Text(
                            l10n.noQrAvailable,
                            style: const TextStyle(color: Colors.grey),
                          ),
                        ),
                      ),
              ),
            ),

            const SizedBox(height: 32.0),

            // Thông tin chuyển khoản
            PaymentInfoCard(
              title: l10n.transferInfo,
              items: [
                PaymentRowItem(
                  icon: Icons.payments_outlined,
                  label: l10n.amount,
                  value: '${payment.amount.toInt()} VNĐ',
                  valueStyle: AppTheme.heading3.copyWith(
                    color: AppTheme.primary,
                  ),
                ),
                PaymentRowItem(
                  icon: Icons.description_outlined,
                  label: l10n.transferContent,
                  value: payment.payCode,
                  valueStyle: AppTheme.heading3.copyWith(
                    color: AppTheme.primary,
                  ),
                ),
              ],
            ),

            const SizedBox(height: 32.0),

            // Trạng thái verify
            Consumer<PaymentViewModel>(
              builder: (context, vm, _) {
                if (vm.isPaymentSuccess) {
                  return _SuccessBanner(onDone: () => context.pop(true));
                }

                return switch (vm.verifyState) {
                  Loading() => Column(
                    children: [
                      const CircularProgressIndicator(strokeWidth: 2),
                      const SizedBox(height: 12),
                      Text(
                        l10n.checkingPaymentStatus,
                        style: AppTheme.body.copyWith(color: AppTheme.subtle),
                      ),
                    ],
                  ),
                  Failure(message: var msg) => Column(
                    children: [
                      _WaitingHint(),
                      const SizedBox(height: 12),
                      Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: Colors.orange.withValues(alpha: 0.1),
                          borderRadius: BorderRadius.circular(
                            AppTheme.radiusCard,
                          ),
                          border: Border.all(
                            color: Colors.orange.withValues(alpha: 0.3),
                          ),
                        ),
                        child: Text(
                          msg,
                          style: AppTheme.bodySmall.copyWith(
                            color: Colors.orange.shade800,
                          ),
                          textAlign: TextAlign.center,
                        ),
                      ),
                    ],
                  ),
                  _ => _WaitingHint(),
                };
              },
            ),

            const SizedBox(height: 24.0),

            Consumer<PaymentViewModel>(
              builder: (context, vm, _) {
                if (vm.isPaymentSuccess) return const SizedBox.shrink();
                return AppFilledButton(
                  label: l10n.iHaveTransferred,
                  onPressed: vm.verifyState is Loading
                      ? null
                      : () => vm.checkPaymentStatus(),
                );
              },
            ),

            const SizedBox(height: 16.0),
          ],
        ),
      ),
    );
  }
}

class _SuccessBanner extends StatelessWidget {
  final VoidCallback onDone;
  const _SuccessBanner({required this.onDone});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Column(
      children: [
        Container(
          padding: const EdgeInsets.all(16.0),
          decoration: BoxDecoration(
            color: Colors.green.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(AppTheme.radiusCard),
            border: Border.all(color: Colors.green.withValues(alpha: 0.3)),
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.check_circle, color: Colors.green, size: 24),
              const SizedBox(width: 12.0),
              Text(
                l10n.paymentSuccess,
                style: AppTheme.heading3.copyWith(color: Colors.green),
              ),
            ],
          ),
        ),
        const SizedBox(height: 16),
        AppFilledButton(label: l10n.done, onPressed: onDone),
      ],
    );
  }
}

class _WaitingHint extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Column(
      children: [
        const SizedBox(height: 8),
        Text(
          l10n.afterTransferHint,
          style: AppTheme.body.copyWith(color: AppTheme.subtle),
          textAlign: TextAlign.center,
        ),
        const SizedBox(height: 8),
      ],
    );
  }
}
