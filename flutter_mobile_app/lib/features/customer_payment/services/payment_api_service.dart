import 'package:smart_parking_mobile/core/network/api_client.dart';
import 'package:smart_parking_mobile/features/customer_payment/models/payment_models.dart';

class PaymentApiService {
  final ApiClient _apiClient;

  PaymentApiService(this._apiClient);

  Future<PaymentCheckoutResponse> checkout(CheckoutRequest request) async {
    final response = await _apiClient.post(
      '/api/v1/customer/payments/checkout',
      data: request.toJson(),
      authenticated: true, // ← Bắt buộc: gửi Bearer Token
    );
    return PaymentCheckoutResponse.fromJson(response.data);
  }

  Future<PaymentInitiateResponse> initiateBookingPayment(
    PaymentBookingRequest request,
  ) async {
    final response = await _apiClient.post(
      '/api/v1/customer/payments/booking',
      data: request.toJson(),
      authenticated: true, // ← Bắt buộc: gửi Bearer Token
    );
    return PaymentInitiateResponse.fromJson(response.data);
  }

  Future<PaymentTreeResponse> getPaymentDetails(int paymentId) async {
    final response = await _apiClient.get(
      '/api/v1/customer/payments/$paymentId/details',
      authenticated: true, // ← Bắt buộc: gửi Bearer Token
    );
    return PaymentTreeResponse.fromJson(response.data);
  }
}
