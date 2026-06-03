import 'package:flutter/material.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/features/customer_packages/models/booking_models.dart';
import 'package:smart_parking_mobile/features/customer_packages/models/metadata_models.dart';
import 'package:smart_parking_mobile/features/customer_packages/repositories/booking_repository.dart';

class AddVehicleViewModel extends ChangeNotifier {
  final BookingRepository _repository;

  ViewState<List<AllowedVehicleType>> vehicleTypesState = const Idle();
  ViewState<List<AvailablePackagePrice>> packagesState = const Idle();
  ViewState<BookingDetailDto> submitState = const Idle();

  AllowedVehicleType? selectedVehicleType;
  AvailablePackagePrice? selectedPackage;

  AddVehicleViewModel(this._repository);

  Future<void> fetchVehicleTypes() async {
    vehicleTypesState = const Loading();
    notifyListeners();

    try {
      final types = await _repository.getAllowedVehicleTypes();
      vehicleTypesState = Success(types);
    } catch (e) {
      vehicleTypesState = Failure(e.toString());
    }
    notifyListeners();
  }

  Future<void> selectVehicleType(AllowedVehicleType type) async {
    if (type.isFull) return; // Cannot select full quota

    selectedVehicleType = type;
    selectedPackage = null;
    packagesState = const Loading();
    notifyListeners();

    try {
      final packages = await _repository.getAvailablePackages(
        type.vehicleTypeId,
      );
      packagesState = Success(packages);
    } catch (e) {
      packagesState = Failure(e.toString());
    }
    notifyListeners();
  }

  void selectPackage(AvailablePackagePrice package) {
    selectedPackage = package;
    notifyListeners();
  }

  Future<bool> submitDraft({
    required int bookingId,
    required int customerId,
    required String vehicleNo,
    required DateTime startDate,
  }) async {
    if (selectedVehicleType == null || selectedPackage == null) {
      submitState = const Failure("Vui lòng chọn loại xe và gói cước");
      notifyListeners();
      return false;
    }

    submitState = const Loading();
    notifyListeners();

    try {
      final endDate = DateTime(
        startDate.year,
        startDate.month + selectedPackage!.durationMonths,
        startDate.day,
        23,
        59,
        59,
      );

      final request = BookingDetailCreateRequest(
        bookingId: bookingId,
        customerId: customerId,
        packagePriceId: selectedPackage!.packagePriceId,
        packagePriceName: selectedPackage!.packagePriceName,
        vehicleNo: vehicleNo,
        startDate: DateTime(
          startDate.year,
          startDate.month,
          startDate.day,
          0,
          0,
          0,
        ),
        endDate: endDate,
        status: BookingStatus.pendingPayment,
        vehicleTypeId: selectedVehicleType!.vehicleTypeId,
      );

      final draft = await _repository.createDraft(request);
      submitState = Success(draft);
      notifyListeners();
      return true;
    } catch (e) {
      submitState = Failure(e.toString());
      notifyListeners();
      return false;
    }
  }

  void reset() {
    selectedVehicleType = null;
    selectedPackage = null;
    packagesState = const Idle();
    submitState = const Idle();
    notifyListeners();
  }
}
