import 'package:smart_parking_mobile/features/customer_profile/models/customer_profile_models.dart';
import 'package:smart_parking_mobile/features/customer_profile/services/profile_api_service.dart';

class ProfileRepository {
  final ProfileApiService _apiService;

  ProfileRepository(this._apiService);

  Future<CustomerProfile> getMyProfile() async {
    return await _apiService.getMyProfile();
  }

  Future<CustomerProfile> updateMyProfile(UpdateProfileRequest request) async {
    return await _apiService.updateMyProfile(request);
  }
}
