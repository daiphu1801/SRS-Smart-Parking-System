import 'package:smart_parking_mobile/core/network/api_client.dart';
import 'package:smart_parking_mobile/features/customer_payment/models/payment_models.dart';

class HistoryApiService {
  final ApiClient _client;
  HistoryApiService(this._client);

  /// POST /api/v1/customer/payments
  /// Lấy danh sách lịch sử hóa đơn (có phân trang).
  /// Backend nhận page/size qua queryParameters trên URL.
  Future<Map<String, dynamic>> getMyPayments({
    int page = 0,
    int size = 50,
  }) async {
    final path = '/api/v1/customer/payments?page=$page&size=$size&sort=createdAt,desc';
    final response = await _client.get(
      path,
      authenticated: true,
    );
    // response.data là Map/ApiResponse<Page<PaymentResponse>>
    return response.data as Map<String, dynamic>;
  }

  /// POST /api/v1/customer/payments/{id}/cancel
  Future<void> cancelPayment(int paymentId) async {
    await _client.post(
      '/api/v1/customer/payments/$paymentId/cancel',
      authenticated: true,
    );
  }
}

/// Model wrapper cho phân trang từ Spring Boot Page
class PagedPaymentResponse {
  final List<PaymentResponse> content;
  final int totalElements;
  final int totalPages;
  final int currentPage;
  final bool isLast;

  const PagedPaymentResponse({
    required this.content,
    required this.totalElements,
    required this.totalPages,
    required this.currentPage,
    required this.isLast,
  });

  factory PagedPaymentResponse.fromJson(Map<String, dynamic> json) {
    final contentList = (json['content'] as List<dynamic>)
        .map((e) => PaymentResponse.fromJson(e as Map<String, dynamic>))
        .toList();
    return PagedPaymentResponse(
      content: contentList,
      totalElements: json['totalElements'] as int? ?? 0,
      totalPages: json['totalPages'] as int? ?? 1,
      currentPage: json['number'] as int? ?? 0,
      isLast: json['last'] as bool? ?? true,
    );
  }
}
