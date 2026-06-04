import 'package:smart_parking_mobile/core/network/api_exception.dart';
import 'package:smart_parking_mobile/features/customer_history/services/history_api_service.dart';

class HistoryRepository {
  final HistoryApiService _service;
  HistoryRepository(this._service);

  Future<PagedPaymentResponse> getMyPayments({int page = 0, int size = 50}) async {
    try {
      final raw = await _service.getMyPayments(page: page, size: size);
      // Backend trả ApiResponse<Page<PaymentResponse>> → raw đã là .data (Page)
      return PagedPaymentResponse.fromJson(raw);
    } on ApiException {
      rethrow;
    } catch (e) {
      throw ApiException('Failed to load transaction history: $e');
    }
  }

  Future<void> cancelPayment(int paymentId) async {
    try {
      await _service.cancelPayment(paymentId);
    } on ApiException {
      rethrow;
    } catch (e) {
      throw ApiException('Failed to cancel transaction: $e');
    }
  }
}
