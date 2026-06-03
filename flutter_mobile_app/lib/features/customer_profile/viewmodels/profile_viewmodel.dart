import 'package:flutter/foundation.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/features/customer_profile/models/customer_profile_models.dart';
import 'package:smart_parking_mobile/features/customer_profile/repositories/profile_repository.dart';

class ProfileViewModel extends ChangeNotifier {
  final ProfileRepository _repository;
  
  ViewState<CustomerProfile> profileState = const Idle();

  ProfileViewModel(this._repository);

  Future<void> fetchProfile() async {
    profileState = const Loading();
    notifyListeners();

    try {
      final profile = await _repository.getMyProfile();
      profileState = Success(profile);
    } catch (e) {
      profileState = Failure(e.toString());
    }
    notifyListeners();
  }

  Future<bool> updateProfile(UpdateProfileRequest request) async {
    profileState = const Loading();
    notifyListeners();

    try {
      final updatedProfile = await _repository.updateMyProfile(request);
      profileState = Success(updatedProfile);
      notifyListeners();
      return true;
    } catch (e) {
      profileState = Failure(e.toString());
      notifyListeners();
      return false;
    }
  }
}
