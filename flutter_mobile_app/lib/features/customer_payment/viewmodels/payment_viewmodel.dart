import 'package:flutter/foundation.dart';
import '../../../core/utils/view_state.dart';
import '../models/payment_models.dart';
import '../repositories/payment_repository.dart';

class PaymentViewModel extends ChangeNotifier {
  final PaymentRepository _repository;

  PaymentViewModel(this._repository);

  /// State cho bước khởi tạo thanh toán (gọi /checkout)
  ViewState<PaymentCheckoutResponse> _checkoutState = const Idle();

  /// Trạng thái đã thanh toán thành công (sau khi bấm "Tôi đã chuyển khoản")
  ViewState<PaymentTreeResponse> _verifyState = const Idle();

  /// Dùng cho luồng Resume Payment (tiếp tục thanh toán từ Lịch sử)
  int? _resumePaymentId;

  ViewState<PaymentCheckoutResponse> get checkoutState => _checkoutState;
  ViewState<PaymentTreeResponse> get verifyState => _verifyState;

  PaymentCheckoutResponse? get currentCheckout =>
      _checkoutState is Success<PaymentCheckoutResponse>
          ? (_checkoutState as Success<PaymentCheckoutResponse>).data
          : null;

  bool get isPaymentSuccess {
    if (_verifyState is! Success<PaymentTreeResponse>) return false;
    final data = (_verifyState as Success<PaymentTreeResponse>).data;
    return data.paymentInfo.status == PaymentStatus.success;
  }

  /// Gọi API POST /api/v1/customer/payments/checkout
  /// Dùng cho các booking dạng DRAFT (đăng ký lần đầu).
  Future<void> checkout(List<int> bookingDetailIds) async {
    _checkoutState = const Loading();
    _verifyState = const Idle();
    _resumePaymentId = null;
    notifyListeners();

    try {
      final response = await _repository.checkout(bookingDetailIds);
      _checkoutState = Success(response);
    } catch (e) {
      _checkoutState = Failure(e.toString());
    } finally {
      notifyListeners();
    }
  }

  /// Load lại thông tin hóa đơn đang PENDING từ màn hình Lịch sử
  /// (Không gọi /checkout mà chỉ lưu paymentId để verify sau)
  void loadExistingPayment(PaymentResponse payment) {
    _resumePaymentId = payment.id;
    _verifyState = const Idle();
    notifyListeners();
  }

  /// Kiểm tra trạng thái thanh toán sau khi người dùng quét QR xong.
  Future<void> checkPaymentStatus() async {
    // Lấy paymentId từ checkout mới hoặc từ Resume Payment
    final paymentId = currentCheckout?.paymentId ?? _resumePaymentId;
    if (paymentId == null) return;

    _verifyState = const Loading();
    notifyListeners();

    try {
      final detail = await _repository.getPaymentDetails(paymentId);
      if (detail.paymentInfo.status == PaymentStatus.pending) {
        _verifyState = const Failure('Hệ thống chưa ghi nhận thanh toán. Vui lòng đợi giây lát và thử lại.');
      } else if (detail.paymentInfo.status != PaymentStatus.success) {
        _verifyState = Failure('Hóa đơn có trạng thái: ${detail.paymentInfo.status.label}.');
      } else {
        _verifyState = Success(detail);
      }
    } catch (e) {
      _verifyState = Failure(e.toString());
    } finally {
      notifyListeners();
    }
  }

  void resetState() {
    _checkoutState = const Idle();
    _verifyState = const Idle();
    _resumePaymentId = null;
    notifyListeners();
  }
}
