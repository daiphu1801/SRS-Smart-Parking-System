import 'package:smart_parking_mobile/features/customer_parking/models/parking_session_models.dart';
import 'package:smart_parking_mobile/features/customer_parking/services/parking_session_api_service.dart';

class ParkingSessionRepository {
  ParkingSessionRepository(this._apiService);

  final ParkingSessionApiService _apiService;

  Future<List<ParkingSession>> getSessions({
    DateTime? startDate,
    DateTime? endDate,
    String? plateNumber,
  }) {
    return _apiService.getSessions(
      entryTimeFrom: startDate,
      entryTimeTo: endDate,
      vehicleNo: plateNumber,
    );
  }

  Future<ParkingSession> getSessionDetail(String id) {
    return _apiService.getSessionDetail(id);
  }
}
