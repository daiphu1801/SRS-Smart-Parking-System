import 'package:flutter/foundation.dart';
import 'package:smart_parking_mobile/core/network/api_exception.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/features/customer_home/models/home_models.dart';
import 'package:smart_parking_mobile/features/customer_home/repositories/home_repository.dart';

/// ViewModel for CustomerHomeScreen.
/// Manages the unified dashboard state.
class HomeViewModel extends ChangeNotifier {
  HomeViewModel({required HomeRepository repository}) : _repository = repository;

  final HomeRepository _repository;

  ViewState<HomeDashboard> state = const Idle();

  Future<void> loadData() async {
    state = const Loading();
    notifyListeners();

    try {
      final data = await _repository.getHomeDashboard();
      state = Success(data);
    } on ApiException catch (e) {
      state = Failure(e.message);
    } catch (_) {
      state = const Failure('Không thể tải dữ liệu trang chủ. Vui lòng thử lại sau.');
    }

    notifyListeners();
  }
}
