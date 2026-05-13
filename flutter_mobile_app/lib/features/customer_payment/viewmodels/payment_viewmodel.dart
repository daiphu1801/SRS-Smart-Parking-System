import 'dart:async';
import 'package:flutter/foundation.dart';
import '../../../core/utils/view_state.dart';
import '../models/payment_models.dart';

class PaymentViewModel extends ChangeNotifier {
  ViewState<Payment> _paymentState = const Idle();
  VietQRData? _currentQRData;
  bool _isPaymentSuccess = false;

  ViewState<Payment> get paymentState => _paymentState;
  VietQRData? get currentQRData => _currentQRData;
  bool get isPaymentSuccess => _isPaymentSuccess;

  Payment? get currentPayment => _paymentState is Success<Payment> ? (_paymentState as Success<Payment>).data : null;

  /// Tạo giao dịch mới
  Future<void> createPaymentTransaction({
    required double amount,
    required String targetId, // Có thể là sessionId hoặc bookingDetailId
    required bool isSession, // true: Parking Session, false: Renewal Booking
  }) async {
    _paymentState = const Loading();
    _isPaymentSuccess = false;
    notifyListeners();

    try {
      // Giả lập API call tạo Payment
      await Future.delayed(const Duration(milliseconds: 800));

      // Tạo payCode duy nhất
      final payCode = 'PAY${DateTime.now().millisecondsSinceEpoch.toString().substring(5)}';

      final payment = Payment(
        id: DateTime.now().millisecondsSinceEpoch,
        payCode: payCode,
        amount: amount,
        method: PaymentMethod.vietqr,
        status: PaymentStatus.pending,
        createdAt: DateTime.now(),
      );

      // Cấu hình ngân hàng đích (Giả lập thông tin Ban quản lý)
      _currentQRData = VietQRData(
        bankId: 'MB', // VD: MB Bank
        accountNo: '0123456789', // Số tài khoản BQL
        template: 'compact',
        amount: amount,
        description: payCode,
        accountName: 'BQL BAI XE THONG MINH',
      );
      
      _paymentState = Success(payment);
    } catch (e) {
      _paymentState = Failure(e.toString());
    } finally {
      notifyListeners();
    }
  }

  /// Giả lập việc nhận Webhook từ Sepay báo thành công
  Future<void> simulatePaymentSuccess() async {
    final payment = currentPayment;
    if (payment == null) return;
    
    // Giả lập độ trễ mạng
    await Future.delayed(const Duration(seconds: 1));

    final updatedPayment = payment.copyWith(status: PaymentStatus.success);
    _paymentState = Success(updatedPayment);
    _isPaymentSuccess = true;
    notifyListeners();
  }

  void resetState() {
    _paymentState = const Idle();
    _currentQRData = null;
    _isPaymentSuccess = false;
    notifyListeners();
  }
}
