class PaymentSessionRequest {
  final String vehicleNo;
  final String gateway;
  final String method;

  PaymentSessionRequest({
    required this.vehicleNo,
    this.gateway = 'SEPAY',
    this.method = 'BANK_TRANSFER',
  });

  Map<String, dynamic> toJson() {
    return {
      'vehicleNo': vehicleNo,
      'gateway': gateway,
      'method': method,
    };
  }
}
