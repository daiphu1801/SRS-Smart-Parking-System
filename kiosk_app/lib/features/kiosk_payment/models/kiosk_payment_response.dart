class KioskPaymentResponse {
  final String payCode;
  final double amount;
  final int paymentId;
  final String paymentCode;
  final String checkoutUrl;
  final String message;

  KioskPaymentResponse({
    required this.payCode,
    required this.amount,
    required this.paymentId,
    required this.paymentCode,
    required this.checkoutUrl,
    required this.message,
  });

  factory KioskPaymentResponse.fromJson(Map<String, dynamic> json) {
    return KioskPaymentResponse(
      payCode: json['payCode'] ?? '',
      amount: (json['amount'] as num?)?.toDouble() ?? 0.0,
      paymentId: json['paymentId'] as int? ?? 0,
      paymentCode: json['paymentCode'] ?? '',
      checkoutUrl: json['checkoutUrl'] ?? '',
      message: json['message'] ?? '',
    );
  }
}
