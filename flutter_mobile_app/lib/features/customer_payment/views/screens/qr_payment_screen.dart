import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';
import '../../../../core/theme/app_theme.dart';
import '../../../../core/utils/view_state.dart';
import '../../../../core/widgets/app_widgets.dart';
import '../../viewmodels/payment_viewmodel.dart';
import '../../models/payment_models.dart';
import '../widgets/payment_widgets.dart';

class QRPaymentScreen extends StatefulWidget {
  /// Danh sách ID của các BookingDetail cần thanh toán.
  final List<int> bookingDetailIds;

  const QRPaymentScreen({super.key, required this.bookingDetailIds});

  @override
  State<QRPaymentScreen> createState() => _QRPaymentScreenState();
}

class _QRPaymentScreenState extends State<QRPaymentScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<PaymentViewModel>().checkout(widget.bookingDetailIds);
    });
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

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
      body: Consumer<PaymentViewModel>(
        builder: (context, viewModel, child) {
          return switch (viewModel.checkoutState) {
            Idle() ||
            Loading() => const Center(child: CircularProgressIndicator()),
            Failure(message: var msg) => AppEmptyState(
              icon: Icons.error_outline,
              title: l10n.paymentInitError,
              subtitle: msg,
            ),
            Success(data: var checkout) => _QRContent(
              checkout: checkout,
              viewModel: viewModel,
            ),
          };
        },
      ),
    );
  }
}

class _QRContent extends StatelessWidget {
  final PaymentCheckoutResponse checkout;
  final PaymentViewModel viewModel;

  const _QRContent({required this.checkout, required this.viewModel});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return SingleChildScrollView(
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

          // QR Code từ VietQR (URL thật từ BE)
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
              child: Image.network(
                checkout.checkoutUrl,
                width: 240,
                height: 240,
                fit: BoxFit.contain,
                loadingBuilder: (context, child, loadingProgress) {
                  if (loadingProgress == null) return child;
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
                value: '${checkout.amount.toInt()} VNĐ',
                valueStyle: AppTheme.heading3.copyWith(color: AppTheme.primary),
              ),
              PaymentRowItem(
                icon: Icons.description_outlined,
                label: l10n.transferContent,
                value: checkout.paymentCode,
                valueStyle: AppTheme.heading3.copyWith(color: AppTheme.primary),
              ),
            ],
          ),

          const SizedBox(height: 32.0),

          // Trạng thái sau khi kiểm tra
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

          // Nút kiểm tra trạng thái
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
