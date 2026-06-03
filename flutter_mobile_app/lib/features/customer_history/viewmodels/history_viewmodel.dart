import 'package:flutter/material.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/features/customer_payment/models/payment_models.dart';
import 'package:smart_parking_mobile/features/customer_parking/models/parking_session_models.dart';
import 'package:smart_parking_mobile/features/customer_parking/repositories/parking_session_repository.dart';
import 'package:smart_parking_mobile/features/customer_history/repositories/history_repository.dart';

class HistoryViewModel extends ChangeNotifier {
  HistoryViewModel(this._parkingRepository, this._historyRepository);

  final ParkingSessionRepository _parkingRepository;
  final HistoryRepository _historyRepository;

  // ─── Parking Session State ───────────────────────────────────────────
  ViewState<List<ParkingSession>> historyState = const Idle();

  DateTime? startDate;
  DateTime? endDate;
  String? plateNumber;

  // ─── Payment History State ───────────────────────────────────────────
  ViewState<List<PaymentResponse>> paymentState = const Idle();

  List<PaymentResponse> _allPayments = [];

  List<PaymentResponse> get pendingPayments =>
      _allPayments.where((p) => p.isPending).toList();

  List<PaymentResponse> get completedPayments =>
      _allPayments.where((p) => !p.isPending).toList();

  // ─── Parking Session Methods ─────────────────────────────────────────

  Future<void> fetchHistory({String? customerId}) async {
    historyState = const Loading();
    notifyListeners();

    try {
      final sessions = await _parkingRepository.getSessions(
        startDate: startDate,
        endDate: endDate == null
            ? null
            : DateTime(endDate!.year, endDate!.month, endDate!.day, 23, 59, 59),
        plateNumber: plateNumber,
      );
      final completed = sessions
          .where((session) => session.status == SessionStatus.completed)
          .toList()
        ..sort((a, b) => b.entryTime.compareTo(a.entryTime));
      historyState = Success(completed);
    } catch (e) {
      historyState = Failure(e.toString());
    }
    notifyListeners();
  }

  Future<void> applyFilter({
    String? customerId,
    DateTime? newStartDate,
    DateTime? newEndDate,
    String? newPlateNumber,
  }) async {
    startDate = newStartDate;
    endDate = newEndDate;
    plateNumber = newPlateNumber;
    await fetchHistory();
  }

  Future<void> clearFilter({String? customerId}) async {
    startDate = null;
    endDate = null;
    plateNumber = null;
    await fetchHistory();
  }

  bool get hasActiveFilter =>
      startDate != null ||
      endDate != null ||
      (plateNumber != null && plateNumber!.isNotEmpty);

  // ─── Payment History Methods ─────────────────────────────────────────

  Future<void> fetchPayments() async {
    paymentState = const Loading();
    notifyListeners();
    try {
      final paged = await _historyRepository.getMyPayments(page: 0, size: 50);
      _allPayments = paged.content;
      paymentState = Success(_allPayments);
    } catch (e) {
      paymentState = Failure(e.toString());
    }
    notifyListeners();
  }

  Future<bool> cancelPayment(int paymentId) async {
    try {
      await _historyRepository.cancelPayment(paymentId);
      // Remove from list locally
      _allPayments = _allPayments.where((p) => p.id != paymentId).toList();
      paymentState = Success(_allPayments);
      notifyListeners();
      return true;
    } catch (e) {
      return false;
    }
  }
}
