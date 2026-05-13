import 'package:flutter/material.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/features/customer_complaint/models/complaint_models.dart';

class ComplaintViewModel extends ChangeNotifier {
  ViewState<List<Complaint>> complaintsState = const Idle();
  ViewState<Complaint> currentComplaintState = const Idle();

  // ── MOCK DATA ─────────────────────────────────────────────────────────────
  final List<Complaint> _mockComplaints = [
    Complaint(
      id: 'CP-001',
      customerId: 'CUST-001',
      title: 'Hệ thống nhận diện biển số chậm',
      description: 'Hôm qua lúc 18h tối tôi đi làm về, camera ở cổng chính mất hơn 10 giây mới nhận diện được biển số để mở barie, gây tắc nghẽn ở phía sau.',
      status: ComplaintStatus.resolved,
      resolutionNote: 'Đã vệ sinh lại camera và tinh chỉnh lại góc chụp. Cảm ơn cư dân đã phản ánh.',
      createdAt: DateTime.now().subtract(const Duration(days: 2)),
      updatedAt: DateTime.now().subtract(const Duration(hours: 10)),
    ),
    Complaint(
      id: 'CP-002',
      customerId: 'CUST-001',
      title: 'Đèn hầm khu vực B1 bị mờ',
      description: 'Khu vực đỗ xe máy ở B1 có vài bóng đèn bị cháy, buổi tối rất tối và khó lùi xe.',
      imageUrl: 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/1e/Underground_parking_lot.jpg/800px-Underground_parking_lot.jpg',
      status: ComplaintStatus.processing,
      createdAt: DateTime.now().subtract(const Duration(hours: 5)),
    ),
    Complaint(
      id: 'CP-003',
      customerId: 'CUST-001',
      title: 'Ứng dụng trừ tiền 2 lần',
      description: 'Tôi quẹt thẻ ra cổng lúc 8h sáng nay, ứng dụng trừ 5000đ hai lần.',
      status: ComplaintStatus.pending,
      createdAt: DateTime.now().subtract(const Duration(hours: 1)),
    ),
  ];

  Future<void> fetchComplaints(String customerId) async {
    complaintsState = const Loading();
    notifyListeners();

    try {
      await Future.delayed(const Duration(milliseconds: 600));
      final filtered = _mockComplaints
          .where((c) => c.customerId == customerId)
          .toList()
        ..sort((a, b) => b.createdAt.compareTo(a.createdAt));
      complaintsState = Success(filtered);
    } catch (e) {
      complaintsState = Failure(e.toString());
    }
    notifyListeners();
  }

  Future<void> fetchComplaintById(String complaintId) async {
    currentComplaintState = const Loading();
    notifyListeners();

    try {
      await Future.delayed(const Duration(milliseconds: 400));
      final complaint = _mockComplaints.firstWhere(
        (c) => c.id == complaintId,
        orElse: () => throw Exception('Không tìm thấy khiếu nại'),
      );
      currentComplaintState = Success(complaint);
    } catch (e) {
      currentComplaintState = Failure(e.toString());
    }
    notifyListeners();
  }

  Future<bool> createComplaint({
    required String customerId,
    required String title,
    required String description,
    String? imageUrl,
  }) async {
    try {
      await Future.delayed(const Duration(milliseconds: 800)); // Simulate API call
      final newComplaint = Complaint(
        id: 'CP-00${_mockComplaints.length + 1}',
        customerId: customerId,
        title: title,
        description: description,
        imageUrl: imageUrl,
        status: ComplaintStatus.pending,
        createdAt: DateTime.now(),
      );
      _mockComplaints.insert(0, newComplaint);
      await fetchComplaints(customerId); // Refresh the list
      return true;
    } catch (e) {
      return false;
    }
  }
}
