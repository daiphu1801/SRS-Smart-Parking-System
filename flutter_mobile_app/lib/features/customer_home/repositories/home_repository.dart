import 'package:smart_parking_mobile/features/customer_home/models/home_models.dart';
import 'package:smart_parking_mobile/features/customer_home/services/home_api_service.dart';

class HomeRepository {
  HomeRepository({required HomeApiService apiService})
      : _apiService = apiService;

  final HomeApiService _apiService;

  Future<HomeDashboard> getHomeDashboard() {
    return _apiService.getDashboard();
  }
}
