import 'package:smart_parking_mobile/core/network/api_client.dart';
import 'package:smart_parking_mobile/features/customer_complaint/models/complaint_models.dart';

class ComplaintApiService {
  final ApiClient _apiClient;

  ComplaintApiService(this._apiClient);

  /// Gọi POST /api/v1/customer/complaints
  /// Backend tự lấy customerId từ JWT token — không cần truyền lên.
  /// [content] = title + "\n\n" + description (gộp ở tầng UI/ViewModel)
  /// [imgUrl]  = null nếu không đính kèm ảnh (chưa hỗ trợ upload)
  Future<Complaint> createComplaint({
    required String content,
    String? imgUrl,
  }) async {
    final body = <String, dynamic>{'content': content};
    if (imgUrl != null) body['imgUrl'] = imgUrl;

    final response = await _apiClient.post(
      '/api/v1/customer/complaints',
      body: body,
      authenticated: true,
    );

    if (response.data == null) {
      throw Exception('Did not receive data from server');
    }

    return Complaint.fromJson(response.data as Map<String, dynamic>);
  }
}
