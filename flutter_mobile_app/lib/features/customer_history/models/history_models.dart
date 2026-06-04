/// Model cho lịch sử thanh toán của khách hàng.
/// Sau này sẽ ánh xạ từ response JSON của API backend.
class PaymentTransaction {
  final String id;
  final double amount;
  final PaymentMethod method;
  final DateTime createdAt;
  final PaymentStatus status;

  /// ID đối tượng được thanh toán (Parking Session ID hoặc Booking Detail ID)
  final String targetId;
  final String targetLabel;

  const PaymentTransaction({
    required this.id,
    required this.amount,
    required this.method,
    required this.createdAt,
    required this.status,
    required this.targetId,
    required this.targetLabel,
  });
}

enum PaymentMethod { qr, cash, other }

enum PaymentStatus { success, failed, pending }

extension PaymentStatusLabel on PaymentStatus {
  bool get isSuccess => this == PaymentStatus.success;
}
