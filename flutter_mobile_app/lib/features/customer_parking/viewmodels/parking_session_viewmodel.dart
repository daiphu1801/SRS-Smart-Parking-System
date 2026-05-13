import 'package:flutter/material.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/features/customer_parking/models/parking_session_models.dart';

class ParkingSessionViewModel extends ChangeNotifier {
  ViewState<List<ParkingSession>> sessionsState = const Idle();
  ViewState<List<ParkingSession>> historySessionsState = const Idle();
  ViewState<ParkingSession> currentSessionState = const Idle();

  // ── MOCK DATA (aligned with DB ERD: ParkingSessions) ─────────────────────
  final List<ParkingSession> _mockSessions = [
    // PS-2001: Hoàn thành, dùng gói cước (fee=0)
    ParkingSession(
      id: 'PS-2001',
      customerId: 'CUST-001',
      plateNumber: '30A-123.45',
      vehicleType: 'CAR',
      bookingDetailId: 'BD-1001',
      entryTime: DateTime.now().subtract(const Duration(hours: 2, minutes: 15)),
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
    // PS-2002: Hoàn thành, xe vãng lai, chưa thanh toán (để test QR)
    ParkingSession(
      id: 'PS-2002',
      customerId: 'CUST-001',
      plateNumber: '29B-987.65',
      vehicleType: 'CAR',
      bookingDetailId: null, // xe vãng lai
      entryTime: DateTime.now().subtract(const Duration(days: 1, hours: 3)),
      exitTime: DateTime.now().subtract(const Duration(days: 1)),
      imageInUrl:
          'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a7/Camponotus_flavomarginatus_ant.jpg/400px-Camponotus_flavomarginatus_ant.jpg',
      imageOutUrl:
          'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a7/Camponotus_flavomarginatus_ant.jpg/400px-Camponotus_flavomarginatus_ant.jpg',
      zoneInName: 'Cổng phụ - Tòa B',
      zoneOutName: 'Cổng phụ - Tòa B',
      amountDue: 25000,
      isPaid: false,
      status: SessionStatus.completed,
    ),
    // PS-2003: Đang đỗ, dùng gói cước, xe đang trong bãi
    ParkingSession(
      id: 'PS-2003',
      customerId: 'CUST-001',
      plateNumber: '30A-123.45',
      vehicleType: 'CAR',
      bookingDetailId: 'BD-1001',
      entryTime: DateTime.now().subtract(const Duration(minutes: 45)),
      exitTime: null,
      imageInUrl:
          'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a7/Camponotus_flavomarginatus_ant.jpg/400px-Camponotus_flavomarginatus_ant.jpg',
      imageOutUrl: null, // xe chưa ra
      zoneInName: 'Cổng chính - Tòa A',
      zoneOutName: null, // chưa ra
      amountDue: 0,
      isPaid: false,
      status: SessionStatus.ongoing,
    ),
    // PS-2004: Hoàn thành, xe máy
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
    // PS-2005: Đã thanh toán, trong thời gian ân hạn (15 phút để lấy xe)
    ParkingSession(
      id: 'PS-2005',
      customerId: 'CUST-001',
      plateNumber: '43C-888.99',
      vehicleType: 'CAR',
      bookingDetailId: null,
      entryTime: DateTime.now().subtract(const Duration(hours: 3)),
      exitTime: null,
      imageInUrl:
          'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a7/Camponotus_flavomarginatus_ant.jpg/400px-Camponotus_flavomarginatus_ant.jpg',
      imageOutUrl: null,
      zoneInName: 'Cổng chính - Tòa A',
      zoneOutName: null,
      amountDue: 30000,
      isPaid: true,
      gracePeriodEnd: DateTime.now().add(const Duration(minutes: 12)),
      status: SessionStatus.ongoing,
    ),
  ];

  Future<void> fetchSessions(String customerId) async {
    sessionsState = const Loading();
    notifyListeners();

    try {
      await Future.delayed(const Duration(milliseconds: 600));
      final filtered = _mockSessions
          .where((s) => s.customerId == customerId)
          .toList()
        ..sort((a, b) => b.entryTime.compareTo(a.entryTime));
      sessionsState = Success(filtered);
    } catch (e) {
      sessionsState = Failure(e.toString());
    }
    notifyListeners();
  }

  Future<void> fetchSessionById(String sessionId) async {
    currentSessionState = const Loading();
    notifyListeners();

    try {
      await Future.delayed(const Duration(milliseconds: 400));
      final session = _mockSessions.firstWhere(
        (s) => s.id == sessionId,
        orElse: () => throw Exception('Không tìm thấy phiên đỗ xe'),
      );
      currentSessionState = Success(session);
    } catch (e) {
      currentSessionState = Failure(e.toString());
    }
    notifyListeners();
  }

  // ── LẤY DANH SÁCH LỊCH SỬ ĐỖ XE (CÓ LỌC) ──────────────────────────────────
  Future<void> fetchSessionHistory({
    required String customerId,
    DateTime? startDate,
    DateTime? endDate,
    String? plateNumber,
  }) async {
    historySessionsState = const Loading();
    notifyListeners();

    try {
      await Future.delayed(const Duration(milliseconds: 600));
      
      // Chỉ lấy các phiên đã hoàn thành (xe đã ra)
      var filtered = _mockSessions
          .where((s) => s.customerId == customerId && s.status == SessionStatus.completed)
          .toList();

      // Lọc theo khoảng thời gian
      if (startDate != null) {
        filtered = filtered.where((s) => s.entryTime.isAfter(startDate) || s.entryTime.isAtSameMomentAs(startDate)).toList();
      }
      if (endDate != null) {
        // Cần tính đến cuối ngày của endDate
        final endOfDay = DateTime(endDate.year, endDate.month, endDate.day, 23, 59, 59);
        filtered = filtered.where((s) => s.entryTime.isBefore(endOfDay) || s.entryTime.isAtSameMomentAs(endOfDay)).toList();
      }

      // Lọc theo biển số xe
      if (plateNumber != null && plateNumber.trim().isNotEmpty) {
        filtered = filtered.where((s) => s.plateNumber.toLowerCase().contains(plateNumber.trim().toLowerCase())).toList();
      }

      // Sắp xếp mới nhất lên đầu
      filtered.sort((a, b) => b.entryTime.compareTo(a.entryTime));

      historySessionsState = Success(filtered);
    } catch (e) {
      historySessionsState = Failure(e.toString());
    }
    notifyListeners();
  }
}
