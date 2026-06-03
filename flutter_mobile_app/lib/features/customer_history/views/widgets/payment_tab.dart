import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_payment/models/payment_models.dart';
import 'package:smart_parking_mobile/features/customer_history/viewmodels/history_viewmodel.dart';
import 'package:smart_parking_mobile/features/customer_history/views/widgets/payment_card.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';

class PaymentTab extends StatelessWidget {
  const PaymentTab({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<HistoryViewModel>(
      builder: (context, vm, _) {
        final state = vm.paymentState;

        if (state is Loading) {
          return const Center(child: CircularProgressIndicator());
        }

        if (state is Failure) {
          return AppEmptyState(
            icon: Icons.error_outline,
            title: AppLocalizations.of(context)!.failedToLoadPaymentHistory,
            subtitle: (state as Failure).message,
            action: TextButton(
              onPressed: () => vm.fetchPayments(),
              child: Text(AppLocalizations.of(context)!.retry),
            ),
          );
        }

        if (state is Success<List<PaymentResponse>>) {
          final payments = state.data;

          if (payments.isEmpty) {
            return RefreshIndicator(
              color: AppTheme.primary,
              onRefresh: () => vm.fetchPayments(),
              child: ListView(
                children: [
                  const SizedBox(height: 120),
                  AppEmptyState(
                    icon: Icons.receipt_long_outlined,
                    title: AppLocalizations.of(context)!.noBillsYet,
                    subtitle: AppLocalizations.of(context)!.paymentHistorySubtitle,
                  ),
                ],
              ),
            );
          }

          // Tách pending vs completed
          final pending = vm.pendingPayments;
          final completed = vm.completedPayments;

          return RefreshIndicator(
            color: AppTheme.primary,
            onRefresh: () => vm.fetchPayments(),
            child: ListView(
              padding: const EdgeInsets.all(AppTheme.pagePadding),
              children: [
                // Đang chờ thanh toán
                if (pending.isNotEmpty) ...[
                  _SectionHeader(
                    title: AppLocalizations.of(context)!.pendingPaymentHeader,
                    count: pending.length,
                    color: Colors.orange,
                  ),
                  const SizedBox(height: 8),
                  ...pending.map((p) => Padding(
                        padding: const EdgeInsets.only(bottom: 12),
                        child: PaymentCard(payment: p, vm: vm),
                      )),
                  const SizedBox(height: 16),
                ],
                // Đã xử lý
                if (completed.isNotEmpty) ...[
                  _SectionHeader(
                    title: AppLocalizations.of(context)!.processedHeader,
                    count: completed.length,
                    color: AppTheme.subtle,
                  ),
                  const SizedBox(height: 8),
                  ...completed.map((p) => Padding(
                        padding: const EdgeInsets.only(bottom: 12),
                        child: PaymentCard(payment: p, vm: vm),
                      )),
                ],
              ],
            ),
          );
        }

        return const SizedBox.shrink();
      },
    );
  }
}

class _SectionHeader extends StatelessWidget {
  final String title;
  final int count;
  final Color color;
  const _SectionHeader({required this.title, required this.count, required this.color});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Container(
          width: 4,
          height: 16,
          decoration: BoxDecoration(color: color, borderRadius: BorderRadius.circular(2)),
        ),
        const SizedBox(width: 8),
        Text(title, style: AppTheme.heading3),
        const SizedBox(width: 8),
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
          decoration: BoxDecoration(
            color: color.withValues(alpha: 0.12),
            borderRadius: BorderRadius.circular(10),
          ),
          child: Text(
            count.toString(),
            style: AppTheme.caption.copyWith(color: color, fontWeight: FontWeight.w700),
          ),
        ),
      ],
    );
  }
}
