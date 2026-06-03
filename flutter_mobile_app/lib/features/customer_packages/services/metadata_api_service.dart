import '../../../core/network/api_client.dart';
import '../models/metadata_models.dart';

class MetadataApiService {
  final ApiClient _apiClient;

  MetadataApiService(this._apiClient);

  Future<List<AllowedVehicleType>> getAllowedVehicleTypes() async {
    final response = await _apiClient.get(
      '/api/v1/customer/subscription/metadata/allowed-vehicle-types',
      authenticated: true,
    );

    final data = response.data as List<dynamic>? ?? [];
    return data.map((e) => AllowedVehicleType.fromJson(e)).toList();
  }

  Future<List<AvailablePackagePrice>> getAvailablePackages(
    int vehicleTypeId,
  ) async {
    final response = await _apiClient.get(
      '/api/v1/customer/subscription/metadata/available-packages',
      queryParameters: {'vehicleTypeId': vehicleTypeId.toString()},
      authenticated: true,
    );

    final data = response.data as List<dynamic>? ?? [];
    return data.map((e) => AvailablePackagePrice.fromJson(e)).toList();
  }
}
