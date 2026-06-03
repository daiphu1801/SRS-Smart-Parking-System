import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:smart_parking_mobile/core/theme/app_theme.dart';
import 'package:smart_parking_mobile/core/widgets/app_widgets.dart';
import 'package:smart_parking_mobile/features/customer_payment/models/payment_models.dart';
import 'package:smart_parking_mobile/features/customer_history/viewmodels/history_viewmodel.dart';
import 'package:smart_parking_mobile/core/l10n/app_localizations.dart';
import 'package:smart_parking_mobile/core/di/service_locator.dart';
import 'package:smart_parking_mobile/features/customer_payment/repositories/payment_repository.dart';

class PaymentCard extends StatelessWidget {
  final PaymentResponse payment;
  final HistoryViewModel vm;
  const PaymentCard({super.key, required this.payment, required this.vm});

  Color get _statusColor {
    switch (payment.status) {
      case PaymentStatus.success:
        return Colors.green;
      case PaymentStatus.pending:
        return Colors.orange;
      case PaymentStatus.canceled:
        return Colors.grey;
      case PaymentStatus.failed:
        return Colors.red;
    }
  }

  @override
  Widget build(BuildContext context) {
    final dateFormatter = DateFormat('dd/MM/yyyy HH:mm');
    final currencyFormatter = NumberFormat.currency(locale: 'vi_VN', symbol: 'đ');

    return GestureDetector(
      onTap: () => _showPaymentDetails(context),
      child: AppCard(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header: Mã hóa đơn + Badge trạng thái
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Expanded(
                  child: Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(8),
                        decoration: BoxDecoration(
                          color: AppTheme.primary.withValues(alpha: 0.1),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Icon(
                          Icons.qr_code_rounded,
                          color: AppTheme.primary,
                          size: 18,
                        ),
                      ),
                      const SizedBox(width: 10),
                      Flexible(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              payment.payCode,
                              style: AppTheme.heading3.copyWith(fontSize: 15),
                              overflow: TextOverflow.ellipsis,
                            ),
                            Text(
                              '#${payment.id}',
                              style: AppTheme.caption.copyWith(color: AppTheme.subtle),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(
                    color: _statusColor.withValues(alpha: 0.12),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    payment.status.label,
                    style: AppTheme.caption.copyWith(
                      color: _statusColor,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ],
            ),
            const Divider(height: 20),

            // Thời gian tạo
            Row(
              children: [
                Icon(Icons.access_time, size: 14, color: AppTheme.subtle),
                const SizedBox(width: 6),
                Text(
                  dateFormatter.format(payment.createdAt),
                  style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle),
                ),
              ],
            ),
            const SizedBox(height: 12),

            // Footer: Số tiền + Actions
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  currencyFormatter.format(payment.amount),
                  style: AppTheme.heading3.copyWith(
                    color: AppTheme.primary,
                    fontSize: 16,
                  ),
                ),
                if (payment.isPending)
                  Row(
                    children: [
                      // Nút Hủy
                      InkWell(
                        onTap: () => _confirmCancel(context),
                        borderRadius: BorderRadius.circular(8),
                        child: Container(
                          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                          decoration: BoxDecoration(
                            border: Border.all(color: Colors.red.shade300),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Text(
                            AppLocalizations.of(context)!.cancel,
                            style: AppTheme.bodySmall.copyWith(color: Colors.red),
                          ),
                        ),
                      ),
                      const SizedBox(width: 8),
                      // Nút Thanh toán tiếp
                      if (payment.checkoutUrl != null)
                        InkWell(
                          onTap: () => _resumePayment(context),
                          borderRadius: BorderRadius.circular(8),
                          child: Container(
                            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                            decoration: BoxDecoration(
                              color: AppTheme.primary,
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Text(
                              AppLocalizations.of(context)!.continuePaymentButton,
                              style: AppTheme.bodySmall.copyWith(color: Colors.white),
                            ),
                          ),
                        ),
                    ],
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  void _showPaymentDetails(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (ctx) => _PaymentDetailsSheet(paymentId: payment.id),
    );
  }

  void _resumePayment(BuildContext context) {
    // Điều hướng sang QRPaymentScreen với mode Resume
    context.push('/payment/resume', extra: payment);
  }

  Future<void> _confirmCancel(BuildContext context) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(AppLocalizations.of(context)!.cancelInvoiceDialogTitle),
        content: Text(
          AppLocalizations.of(context)!.cancelInvoiceDialogBody(payment.payCode),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: Text(AppLocalizations.of(context)!.no),
          ),
          TextButton(
            onPressed: () => Navigator.pop(ctx, true),
            style: TextButton.styleFrom(foregroundColor: Colors.red),
            child: Text(AppLocalizations.of(context)!.cancelInvoiceDialogTitle),
          ),
        ],
      ),
    );

    if (confirmed == true && context.mounted) {
      final success = await vm.cancelPayment(payment.id);
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(success
                ? AppLocalizations.of(context)!.invoiceCancelSuccess
                : AppLocalizations.of(context)!.invoiceCancelFailed),
            backgroundColor: success ? Colors.green : Colors.red,
          ),
        );
      }
    }
  }
}

class _PaymentDetailsSheet extends StatefulWidget {
  final int paymentId;
  const _PaymentDetailsSheet({required this.paymentId});

  @override
  State<_PaymentDetailsSheet> createState() => _PaymentDetailsSheetState();
}

class _PaymentDetailsSheetState extends State<_PaymentDetailsSheet> {
  PaymentTreeResponse? _details;
  String? _error;
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _fetchDetails();
  }

  Future<void> _fetchDetails() async {
    try {
      final repository = sl<PaymentRepository>();
      final data = await repository.getPaymentDetails(widget.paymentId);
      setState(() {
        _details = data;
        _loading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _loading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final currencyFormatter = NumberFormat.currency(locale: 'vi_VN', symbol: 'đ');
    final dateFormatter = DateFormat('dd/MM/yyyy HH:mm');

    return Container(
      decoration: const BoxDecoration(
        color: AppTheme.background,
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      padding: const EdgeInsets.all(AppTheme.pagePadding),
      child: SafeArea(
        top: false,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('Chi tiết thanh toán', style: AppTheme.heading2),
                IconButton(
                  icon: const Icon(Icons.close),
                  onPressed: () => Navigator.of(context).pop(),
                ),
              ],
            ),
            const Divider(),
            if (_loading)
              const Padding(
                padding: EdgeInsets.all(32.0),
                child: Center(child: CircularProgressIndicator()),
              )
            else if (_error != null)
              Padding(
                padding: const EdgeInsets.all(16.0),
                child: Text('Lỗi: $_error', style: const TextStyle(color: Colors.red)),
              )
            else if (_details != null) ...[
              const SizedBox(height: 8),
              _buildInfoRow('Mã GD', _details!.paymentInfo.payCode),
              const SizedBox(height: 8),
              _buildInfoRow('Số tiền', currencyFormatter.format(_details!.paymentInfo.amount)),
              const SizedBox(height: 8),
              _buildInfoRow('Trạng thái', _details!.paymentInfo.status.label),
              const SizedBox(height: 8),
              _buildInfoRow('Thời gian tạo', dateFormatter.format(_details!.paymentInfo.createdAt)),
              if (_details!.paymentInfo.isPending && _details!.paymentInfo.checkoutUrl != null) ...[
                const SizedBox(height: 16),
                Center(
                  child: Container(
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Image.network(
                      _details!.paymentInfo.checkoutUrl!,
                      width: 200,
                      height: 200,
                      fit: BoxFit.contain,
                      loadingBuilder: (context, child, loadingProgress) {
                        if (loadingProgress == null) return child;
                        return const SizedBox(
                          width: 200,
                          height: 200,
                          child: Center(child: CircularProgressIndicator()),
                        );
                      },
                      errorBuilder: (context, error, stackTrace) => Container(
                        width: 200,
                        height: 200,
                        color: AppTheme.surface,
                        child: const Icon(Icons.qr_code, size: 60, color: Colors.grey),
                      ),
                    ),
                  ),
                ),
                const SizedBox(height: 8),
                Center(
                  child: Text(
                    'Mã GD: ${_details!.paymentInfo.payCode}',
                    style: AppTheme.heading3.copyWith(color: AppTheme.primary),
                  ),
                ),
              ],
              const SizedBox(height: 16),
              Text('Các mục thanh toán (${_details!.details.length})', style: AppTheme.heading3),
              const SizedBox(height: 8),
              Flexible(
                child: ListView.builder(
                  shrinkWrap: true,
                  itemCount: _details!.details.length,
                  itemBuilder: (context, index) {
                    final d = _details!.details[index];
                    return ListTile(
                      contentPadding: EdgeInsets.zero,
                      leading: const Icon(Icons.receipt_long_outlined),
                      title: Text('Booking Detail #${d.bookingDetailId}', style: AppTheme.body),
                      subtitle: Text(
                        'Từ ${dateFormatter.format(d.appliedStartDate)}\nđến ${dateFormatter.format(d.appliedEndDate)}',
                        style: AppTheme.caption,
                      ),
                      trailing: Text(
                        currencyFormatter.format(d.itemAmount),
                        style: AppTheme.body.copyWith(color: AppTheme.primary, fontWeight: FontWeight.bold),
                      ),
                    );
                  },
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildInfoRow(String label, String value) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(label, style: AppTheme.bodySmall.copyWith(color: AppTheme.subtle)),
        Text(value, style: AppTheme.body),
      ],
    );
  }
}
