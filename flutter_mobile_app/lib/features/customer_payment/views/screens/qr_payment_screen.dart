import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import '../../../../core/theme/app_theme.dart';
import '../../../../core/utils/view_state.dart';
import '../../../../core/widgets/app_widgets.dart';
import '../../viewmodels/payment_viewmodel.dart';
import '../widgets/payment_widgets.dart';

class QRPaymentScreen extends StatefulWidget {
  final double amount;
  final String targetId;
  final bool isSession; // true nếu thanh toán lượt, false nếu gia hạn gói cước

  const QRPaymentScreen({
    super.key,
    required this.amount,
    required this.targetId,
    required this.isSession,
  });

  @override
  State<QRPaymentScreen> createState() => _QRPaymentScreenState();
}

class _QRPaymentScreenState extends State<QRPaymentScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<PaymentViewModel>().createPaymentTransaction(
            amount: widget.amount,
            targetId: widget.targetId,
            isSession: widget.isSession,
          );
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Thanh Toán QR'),
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
          return switch (viewModel.paymentState) {
            Idle() || Loading() => const Center(child: CircularProgressIndicator()),
            Failure(message: var msg) => AppEmptyState(
                icon: Icons.error_outline,
                title: 'Lỗi khởi tạo thanh toán',
                subtitle: msg,
              ),
            Success(data: _) => Builder(builder: (context) {
                final qrData = viewModel.currentQRData;
                if (qrData == null) {
                  return const AppEmptyState(
                    icon: Icons.qr_code_scanner_outlined,
                    title: 'Không có dữ liệu QR',
                    subtitle: 'Không thể tạo mã QR thanh toán vào lúc này.',
                  );
                }

                return SingleChildScrollView(
                  padding: const EdgeInsets.all(24.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      const SizedBox(height: 16.0),
                      Text('Quét mã để thanh toán', style: AppTheme.heading2),
                      const SizedBox(height: 8.0),
                      Text(
                        'Vui lòng sử dụng Ứng dụng ngân hàng để quét mã.',
                        style: AppTheme.body.copyWith(color: AppTheme.subtle),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 32.0),

                      // QR Code Container
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
                            qrData.qrUrl,
                            width: 220,
                            height: 220,
                            fit: BoxFit.cover,
                            loadingBuilder: (context, child, loadingProgress) {
                              if (loadingProgress == null) return child;
                              return const SizedBox(
                                width: 220,
                                height: 220,
                                child: Center(child: CircularProgressIndicator()),
                              );
                            },
                            errorBuilder: (context, error, stackTrace) => Container(
                              width: 220,
                              height: 220,
                              color: AppTheme.surface,
                              child: const Icon(Icons.qr_code, size: 80, color: Colors.grey),
                            ),
                          ),
                        ),
                      ),

                      const SizedBox(height: 32.0),

                      // Thông tin chuyển khoản
                      PaymentInfoCard(
                        title: 'Thông tin chuyển khoản',
                        items: [
                          PaymentRowItem(
                            icon: Icons.account_balance_outlined,
                            label: 'Ngân hàng',
                            value: qrData.bankId,
                          ),
                          PaymentRowItem(
                            icon: Icons.person_outline,
                            label: 'Chủ tài khoản',
                            value: qrData.accountName,
                          ),
                          PaymentRowItem(
                            icon: Icons.numbers_outlined,
                            label: 'Số tài khoản',
                            value: qrData.accountNo,
                          ),
                          PaymentRowItem(
                            icon: Icons.payments_outlined,
                            label: 'Số tiền',
                            value: '${qrData.amount.toInt()} VNĐ',
                            valueStyle: AppTheme.heading3.copyWith(color: AppTheme.primary),
                          ),
                          PaymentRowItem(
                            icon: Icons.description_outlined,
                            label: 'Nội dung',
                            value: qrData.description,
                            valueStyle: AppTheme.heading3.copyWith(color: AppTheme.primary),
                          ),
                        ],
                      ),

                      const SizedBox(height: 32.0),

                      // Trạng thái thanh toán
                      if (viewModel.isPaymentSuccess)
                        _SuccessBanner()
                      else
                        _WaitingState(),

                      const SizedBox(height: 32.0),

                      // Mock Webhook Button
                      if (!viewModel.isPaymentSuccess)
                        AppFilledButton(
                          label: 'Mô phỏng Webhook Thành Công (Test)',
                          onPressed: () async {
                            await viewModel.simulatePaymentSuccess();
                            if (context.mounted) {
                              Future.delayed(const Duration(seconds: 2), () {
                                if (context.mounted) context.pop(true);
                              });
                            }
                          },
                        ),
                    ],
                  ),
                );
              }),
            _ => const SizedBox.shrink(),
          };
        },
      ),
    );
  }
}

class _SuccessBanner extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Container(
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
            'Thanh toán thành công!',
            style: AppTheme.heading3.copyWith(color: Colors.green),
          ),
        ],
      ),
    );
  }
}

class _WaitingState extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        const CircularProgressIndicator(strokeWidth: 2),
        const SizedBox(height: 16.0),
        Text(
          'Đang chờ xác nhận thanh toán...',
          style: AppTheme.body.copyWith(color: AppTheme.subtle),
        ),
      ],
    );
  }
}
