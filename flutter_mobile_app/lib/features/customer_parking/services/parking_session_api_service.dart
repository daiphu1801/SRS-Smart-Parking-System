import 'package:smart_parking_mobile/core/network/api_client.dart';
import 'package:smart_parking_mobile/features/customer_parking/models/parking_session_models.dart';

class ParkingSessionApiService {
  ParkingSessionApiService(this._apiClient);

  final ApiClient _apiClient;

  Future<List<ParkingSession>> getSessions({
    DateTime? entryTimeFrom,
    DateTime? entryTimeTo,
    String? vehicleNo,
    int page = 0,
    int size = 100,
  }) async {
    final query = <String, String>{
      'page': page.toString(),
      'size': size.toString(),
      'sort': 'entryTime,desc',
      if (vehicleNo != null && vehicleNo.trim().isNotEmpty)
        'vehicleNo': vehicleNo.trim(),
      if (entryTimeFrom != null)
        'entryTimeFrom': entryTimeFrom.toIso8601String(),
      if (entryTimeTo != null) 'entryTimeTo': entryTimeTo.toIso8601String(),
    };

    final response = await _apiClient.get(
      '/api/v1/customer/parking-sessions',
      queryParameters: query,
      authenticated: true,
    );

    final pageData = response.data as Map<String, dynamic>? ?? {};
    final content = pageData['content'] as List<dynamic>? ?? [];
    return content
        .map((e) => ParkingSession.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<ParkingSession> getSessionDetail(String id) async {
    final response = await _apiClient.get(
      '/api/v1/customer/parking-sessions/$id',
      authenticated: true,
    );

    return ParkingSession.fromJson(response.data as Map<String, dynamic>);
  }
}
