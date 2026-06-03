import 'package:smart_parking_mobile/core/network/api_client.dart';
import 'package:smart_parking_mobile/features/customer_home/models/home_models.dart';

class HomeApiService {
  HomeApiService(this._apiClient);

  final ApiClient _apiClient;

  Future<HomeDashboard> getDashboard() async {
    final response = await _apiClient.get(
      '/api/v1/customer/home',
      authenticated: true,
    );
    return HomeDashboard.fromJson(_asMap(response.data));
  }

  Map<String, dynamic> _asMap(Object? value) {
    if (value is Map<String, dynamic>) return value;
    if (value is Map) return Map<String, dynamic>.from(value);
    return <String, dynamic>{};
  }
}
