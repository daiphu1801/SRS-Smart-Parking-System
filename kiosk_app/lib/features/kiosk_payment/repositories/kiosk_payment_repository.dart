
import 'package:smart_parking_mobile/core/network/api_client.dart';
import 'package:smart_parking_mobile/core/network/api_exception.dart';
import 'package:smart_parking_mobile/features/kiosk_payment/models/kiosk_payment_request.dart';
import 'package:smart_parking_mobile/features/kiosk_payment/models/kiosk_payment_response.dart';

class KioskPaymentRepository {
  final ApiClient _apiClient;

  KioskPaymentRepository(this._apiClient);

  Future<KioskPaymentResponse> initiatePaymentSession(PaymentSessionRequest request) async {
    try {
      final response = await _apiClient.post(
        '/api/v1/system/payments/session',
        data: request.toJson(),
      );
      
      final data = response.data;
      if (data != null) {
        return KioskPaymentResponse.fromJson(data);
      } else {
        throw Exception('Phản hồi từ server không hợp lệ');
      }
    } on ApiException catch (e) {
      throw Exception(e.message);
    } catch (e) {
      throw Exception('Đã xảy ra lỗi: $e');
    }
  }
}
