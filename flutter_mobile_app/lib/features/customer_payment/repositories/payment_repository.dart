import 'package:smart_parking_mobile/core/network/api_exception.dart';
import 'package:smart_parking_mobile/features/customer_payment/models/payment_models.dart';
import 'package:smart_parking_mobile/features/customer_payment/services/payment_api_service.dart';

class PaymentRepository {
  final PaymentApiService _apiService;

  PaymentRepository(this._apiService);

  Future<PaymentCheckoutResponse> checkout(List<int> bookingDetailIds) async {
    try {
      final request = CheckoutRequest(bookingDetailIds: bookingDetailIds);
      return await _apiService.checkout(request);
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException('Unknown error when creating payment: $e');
    }
  }

  Future<PaymentInitiateResponse> initiateBookingPayment(PaymentBookingRequest request) async {
    try {
      return await _apiService.initiateBookingPayment(request);
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException('Unknown error when renewing package: $e');
    }
  }

  Future<PaymentTreeResponse> getPaymentDetails(int paymentId) async {
    try {
      return await _apiService.getPaymentDetails(paymentId);
    } catch (e) {
      if (e is ApiException) rethrow;
      throw ApiException('Unknown error when fetching payment details: $e');
    }
  }
}
