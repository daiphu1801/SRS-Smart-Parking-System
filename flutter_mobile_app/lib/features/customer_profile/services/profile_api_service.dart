import 'package:smart_parking_mobile/core/network/api_client.dart';
import 'package:smart_parking_mobile/features/customer_profile/models/customer_profile_models.dart';

class ProfileApiService {
  final ApiClient _apiClient;

  ProfileApiService(this._apiClient);

  Future<CustomerProfile> getMyProfile() async {
    final response = await _apiClient.get(
      '/api/v1/customer/me',
      authenticated: true,
    );
    return CustomerProfile.fromJson(response.data as Map<String, dynamic>);
  }

  Future<CustomerProfile> updateMyProfile(UpdateProfileRequest request) async {
    final response = await _apiClient.put(
      '/api/v1/customer/me',
      body: request.toJson(),
      authenticated: true,
    );
    return CustomerProfile.fromJson(response.data as Map<String, dynamic>);
  }
}
