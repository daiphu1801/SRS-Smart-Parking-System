import 'package:flutter/material.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/features/customer_history/models/history_models.dart';
import 'package:smart_parking_mobile/features/customer_parking/models/parking_session_models.dart';

/// HistoryViewModel: quản lý toàn bộ logic & state cho màn hình Lịch sử.
/// - historyState : danh sách phiên đỗ xe đã hoàn thành (có lọc).
/// - Dữ liệu lấy từ mock list (sau này thay bằng repository gọi API).
class HistoryViewModel extends ChangeNotifier {
  ViewState<List<ParkingSession>> historyState = const Idle();
  ViewState<List<PaymentTransaction>> paymentState = const Idle();

  // Trạng thái bộ lọc hiện hành
  DateTime? startDate;
  DateTime? endDate;
  String? plateNumber;

  // ── MOCK DATA: Lịch sử thanh toán ──────────────────────────────────────────
  final List<PaymentTransaction> _allPayments = [
    PaymentTransaction(
      id: 'PAY-001',
      amount: 200000,
      method: PaymentMethod.qr,
      createdAt: DateTime.now().subtract(const Duration(days: 1, hours: 2)),
      status: PaymentStatus.success,
      targetId: 'BD-1001',
      targetLabel: 'Gia hạn gói cước · 30A-123.45',
    ),
    PaymentTransaction(
      id: 'PAY-002',
      amount: 25000,
      method: PaymentMethod.cash,
      createdAt: DateTime.now().subtract(const Duration(days: 2, hours: 4)),
      status: PaymentStatus.success,
      targetId: 'PS-2002',
      targetLabel: 'Phí đỗ xe · 29B-987.65',
    ),
    PaymentTransaction(
      id: 'PAY-003',
      amount: 10000,
      method: PaymentMethod.qr,
      createdAt: DateTime.now().subtract(const Duration(days: 4)),
      status: PaymentStatus.success,
      targetId: 'PS-2004',
      targetLabel: 'Phí đỗ xe · 51G-456.78',
    ),
    PaymentTransaction(
      id: 'PAY-004',
      amount: 200000,
      method: PaymentMethod.qr,
      createdAt: DateTime.now().subtract(const Duration(days: 7)),
      status: PaymentStatus.failed,
      targetId: 'BD-1002',
      targetLabel: 'Gia hạn gói cước · 30A-123.45',
    ),
  ];


  // ── MOCK DATA (dùng chung với ParkingSessionViewModel, sau sẽ thay bằng repository) ──
  final List<ParkingSession> _allSessions = [
    ParkingSession(
      id: 'PS-2001',
      customerId: 'CUST-001',
      plateNumber: '30A-123.45',
      vehicleType: 'CAR',
      bookingDetailId: 'BD-1001',
      entryTime: DateTime.now().subtract(const Duration(days: 0, hours: 2, minutes: 15)),
      exitTime: DateTime.now().subtract(const Duration(minutes: 5)),
      imageInUrl:
          'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a7/Camponotus_flavomarginatus_ant.jpg/400px-Camponotus_flavomarginatus_ant.jpg',
      imageOutUrl:
          'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a7/Camponotus_flavomarginatus_ant.jpg/400px-Camponotus_flavomarginatus_ant.jpg',
      zoneInName: 'Cổng chính - Tòa A',
      zoneOutName: 'Cổng chính - Tòa A',
      amountDue: 0,
      isPaid: true,
      status: SessionStatus.completed,
    ),
    ParkingSession(
      id: 'PS-2002',
      customerId: 'CUST-001',
      plateNumber: '29B-987.65',
      vehicleType: 'CAR',
      bookingDetailId: null,
      entryTime: DateTime.now().subtract(const Duration(days: 1, hours: 3)),
      exitTime: DateTime.now().subtract(const Duration(days: 1)),
      imageInUrl:
          'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a7/Camponotus_flavomarginatus_ant.jpg/400px-Camponotus_flavomarginatus_ant.jpg',
      imageOutUrl:
          'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a7/Camponotus_flavomarginatus_ant.jpg/400px-Camponotus_flavomarginatus_ant.jpg',
      zoneInName: 'Cổng phụ - Tòa B',
      zoneOutName: 'Cổng phụ - Tòa B',
      amountDue: 25000,
      isPaid: true,
      status: SessionStatus.completed,
    ),
    ParkingSession(
      id: 'PS-2004',
      customerId: 'CUST-001',
      plateNumber: '51G-456.78',
      vehicleType: 'BIKE',
      bookingDetailId: null,
      entryTime: DateTime.now().subtract(const Duration(days: 2, hours: 1)),
      exitTime: DateTime.now().subtract(const Duration(days: 2)),
      imageInUrl:
          'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a7/Camponotus_flavomarginatus_ant.jpg/400px-Camponotus_flavomarginatus_ant.jpg',
      imageOutUrl:
          'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a7/Camponotus_flavomarginatus_ant.jpg/400px-Camponotus_flavomarginatus_ant.jpg',
      zoneInName: 'Cổng xe máy - Tầng B1',
      zoneOutName: 'Cổng xe máy - Tầng B1',
      amountDue: 10000,
      isPaid: true,
      status: SessionStatus.completed,
    ),
    ParkingSession(
      id: 'PS-2006',
      customerId: 'CUST-001',
      plateNumber: '30A-123.45',
      vehicleType: 'CAR',
      bookingDetailId: 'BD-1001',
      entryTime: DateTime.now().subtract(const Duration(days: 5, hours: 1)),
      exitTime: DateTime.now().subtract(const Duration(days: 5)),
      imageInUrl:
          'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a7/Camponotus_flavomarginatus_ant.jpg/400px-Camponotus_flavomarginatus_ant.jpg',
      imageOutUrl:
          'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a7/Camponotus_flavomarginatus_ant.jpg/400px-Camponotus_flavomarginatus_ant.jpg',
      zoneInName: 'Cổng chính - Tòa A',
      zoneOutName: 'Cổng chính - Tòa A',
      amountDue: 0,
      isPaid: true,
      status: SessionStatus.completed,
    ),
  ];

  /// Tải danh sách lịch sử, áp dụng các điều kiện lọc hiện hành.
  Future<void> fetchHistory({required String customerId}) async {
    historyState = const Loading();
    notifyListeners();

    try {
      await Future.delayed(const Duration(milliseconds: 600));

      var filtered = _allSessions
          .where((s) =>
              s.customerId == customerId &&
              s.status == SessionStatus.completed)
          .toList();

      if (startDate != null) {
        filtered = filtered
            .where((s) =>
                s.entryTime.isAfter(startDate!) ||
                s.entryTime.isAtSameMomentAs(startDate!))
            .toList();
      }
      if (endDate != null) {
        final endOfDay =
            DateTime(endDate!.year, endDate!.month, endDate!.day, 23, 59, 59);
        filtered = filtered
            .where((s) =>
                s.entryTime.isBefore(endOfDay) ||
                s.entryTime.isAtSameMomentAs(endOfDay))
            .toList();
      }
      if (plateNumber != null && plateNumber!.trim().isNotEmpty) {
        filtered = filtered
            .where((s) => s.plateNumber
                .toLowerCase()
                .contains(plateNumber!.trim().toLowerCase()))
            .toList();
      }

      filtered.sort((a, b) => b.entryTime.compareTo(a.entryTime));
      historyState = Success(filtered);
    } catch (e) {
      historyState = Failure(e.toString());
    }
    notifyListeners();
  }

  /// Áp dụng bộ lọc mới và tải lại dữ liệu.
  Future<void> applyFilter({
    required String customerId,
    DateTime? newStartDate,
    DateTime? newEndDate,
    String? newPlateNumber,
  }) async {
    startDate = newStartDate;
    endDate = newEndDate;
    plateNumber = newPlateNumber;
    await fetchHistory(customerId: customerId);
  }

  /// Xóa bộ lọc và tải lại.
  Future<void> clearFilter({required String customerId}) async {
    startDate = null;
    endDate = null;
    plateNumber = null;
    await fetchHistory(customerId: customerId);
  }

  bool get hasActiveFilter =>
      startDate != null ||
      endDate != null ||
      (plateNumber != null && plateNumber!.isNotEmpty);

  // ── Payment History ──────────────────────────────────────────────────────

  /// Tải danh sách lịch sử thanh toán theo Customer ID.
  Future<void> fetchPayments({required String customerId}) async {
    paymentState = const Loading();
    notifyListeners();
    try {
      await Future.delayed(const Duration(milliseconds: 600));
      final sorted = [..._allPayments]
        ..sort((a, b) => b.createdAt.compareTo(a.createdAt));
      paymentState = Success(sorted);
    } catch (e) {
      paymentState = Failure(e.toString());
    }
    notifyListeners();
  }
}
