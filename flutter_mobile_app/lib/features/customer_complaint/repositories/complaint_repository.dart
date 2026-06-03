import 'package:smart_parking_mobile/features/customer_complaint/models/complaint_models.dart';
import 'package:smart_parking_mobile/features/customer_complaint/services/complaint_api_service.dart';

class ComplaintRepository {
  final ComplaintApiService _apiService;

  ComplaintRepository(this._apiService);

  /// Tạo khiếu nại mới qua API thật.
  /// Throws [Exception] nếu API thất bại.
  Future<Complaint> createComplaint({
    required String content,
    String? imgUrl,
  }) async {
    return _apiService.createComplaint(
      content: content,
      imgUrl: imgUrl,
    );
  }
}
