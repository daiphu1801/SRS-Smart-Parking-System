import 'package:flutter/foundation.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/features/customer_home/models/home_models.dart';

/// ViewModel for CustomerHomeScreen.
/// Manages vehicle list and active session state.
/// Swap mock data with a real HomeRepository later without touching the view.
class HomeViewModel extends ChangeNotifier {
  ViewState<List<VehicleInfo>> vehicleState = const Idle();
  ViewState<ActiveSession?> sessionState = const Idle();

  HomeViewModel() {
    loadData();
  }

  Future<void> loadData() async {
    vehicleState = const Loading();
    sessionState = const Loading();
    notifyListeners();

    // MOCK — replace with repository calls when API is ready
    await Future.delayed(const Duration(milliseconds: 600));

    vehicleState = const Success([
      VehicleInfo(plate: '51A-123.45', packageName: 'Gói tháng', daysLeft: 12),
      VehicleInfo(plate: '59B-678.90', packageName: 'Gói tháng', daysLeft: 3, isExpired: true),
    ]);

    sessionState = const Success(
      ActiveSession(
        plate: '51A-123.45',
        enteredAt: '09:32',
        duration: '1h 23m',
        estimatedFee: '15.000 đ',
      ),
    );

    notifyListeners();
  }
}
