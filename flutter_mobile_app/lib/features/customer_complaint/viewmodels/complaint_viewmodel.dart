import 'package:flutter/material.dart';
import 'package:smart_parking_mobile/core/network/api_exception.dart';
import 'package:smart_parking_mobile/core/utils/view_state.dart';
import 'package:smart_parking_mobile/features/customer_complaint/models/complaint_models.dart';
import 'package:smart_parking_mobile/features/customer_complaint/repositories/complaint_repository.dart';

class ComplaintViewModel extends ChangeNotifier {
  final ComplaintRepository _repository;

  ComplaintViewModel(this._repository);

  ViewState<List<Complaint>> complaintsState = const Idle();
  ViewState<Complaint> currentComplaintState = const Idle();

  // ── LOCAL LIST (mock + kết quả API) ────────────────────────────────────────
  // Danh sách khiếu nại hiện tại lưu local. Phục vụ 2 mục đích:
  // 1. Mock dữ liệu mẫu khi hiển thị danh sách (backend customer chưa có GET API)
  // 2. Tự cập nhật khi người dùng vừa tạo thành công từ API thật
  final List<Complaint> _localComplaints = [
    Complaint(
      id: 'CP-001',
      customerId: '',
      title: 'Hệ thống nhận diện biển số chậm',
      description:
          'Hôm qua lúc 18h tối tôi đi làm về, camera ở cổng chính mất hơn 10 giây mới nhận diện được biển số để mở barie, gây tắc nghẽn ở phía sau.',
      status: ComplaintStatus.resolved,
      resolutionNote:
          'Đã vệ sinh lại camera và tinh chỉnh lại góc chụp. Cảm ơn cư dân đã phản ánh.',
      createdAt: DateTime.now().subtract(const Duration(days: 2)),
      updatedAt: DateTime.now().subtract(const Duration(hours: 10)),
    ),
    Complaint(
      id: 'CP-002',
      customerId: '',
      title: 'Đèn hầm khu vực B1 bị mờ',
      description:
          'Khu vực đỗ xe máy ở B1 có vài bóng đèn bị cháy, buổi tối rất tối và khó lùi xe.',
      imageUrl:
          'https://upload.wikimedia.org/wikipedia/commons/thumb/1/1e/Underground_parking_lot.jpg/800px-Underground_parking_lot.jpg',
      status: ComplaintStatus.processing,
      createdAt: DateTime.now().subtract(const Duration(hours: 5)),
    ),
    Complaint(
      id: 'CP-003',
      customerId: '',
      title: 'Ứng dụng trừ tiền 2 lần',
      description: 'Tôi quẹt thẻ ra cổng lúc 8h sáng nay, ứng dụng trừ 5000đ hai lần.',
      status: ComplaintStatus.pending,
      createdAt: DateTime.now().subtract(const Duration(hours: 1)),
    ),
  ];

  // ── FETCH (MOCK) ────────────────────────────────────────────────────────────
  // Backend customer chưa có GET /api/v1/customer/complaints
  // → Tiếp tục dùng local list, bao gồm mock + item vừa tạo qua API
  Future<void> fetchComplaints([String? customerId]) async {
    complaintsState = const Loading();
    notifyListeners();

    try {
      await Future.delayed(const Duration(milliseconds: 300));
      final sorted = List<Complaint>.from(_localComplaints)
        ..sort((a, b) => b.createdAt.compareTo(a.createdAt));
      complaintsState = Success(sorted);
    } catch (e) {
      complaintsState = Failure(e.toString());
    }
    notifyListeners();
  }

  // ── FETCH BY ID (MOCK) ──────────────────────────────────────────────────────
  Future<void> fetchComplaintById(String complaintId) async {
    currentComplaintState = const Loading();
    notifyListeners();

    try {
      await Future.delayed(const Duration(milliseconds: 200));
      final complaint = _localComplaints.firstWhere(
        (c) => c.id == complaintId,
        orElse: () => throw Exception('Không tìm thấy khiếu nại'),
      );
      currentComplaintState = Success(complaint);
    } catch (e) {
      currentComplaintState = Failure(e.toString());
    }
    notifyListeners();
  }

  // ── CREATE (API THẬT) ───────────────────────────────────────────────────────
  /// Gửi khiếu nại mới tới backend qua API thật.
  /// [title] và [description] được ghép thành [content] = "$title\n\n$description"
  /// Backend lấy customerId từ JWT token, Flutter không cần truyền lên.
  /// Sau khi tạo thành công, item mới được thêm vào đầu danh sách local để UI tự cập nhật.
  Future<bool> createComplaint({
    required String title,
    required String description,
    String? imageUrl,
  }) async {
    try {
      // Ghép title + description thành content gửi lên backend
      final content = '$title\n\n$description';

      final created = await _repository.createComplaint(
        content: content,
        imgUrl: imageUrl,
      );

      // Thêm item mới (được trả về từ backend) vào đầu local list
      _localComplaints.insert(0, created);

      // Refresh danh sách để UI cập nhật
      await fetchComplaints();
      return true;
    } on ApiException catch (e) {
      // Lỗi từ server (401, 400, 503, ...)
      _lastError = e.message;
      notifyListeners();
      return false;
    } catch (e) {
      _lastError = e.toString();
      notifyListeners();
      return false;
    }
  }

  /// Thông báo lỗi gần nhất từ lần tạo khiếu nại thất bại.
  String? _lastError;
  String? get lastError => _lastError;
  void clearError() {
    _lastError = null;
    notifyListeners();
  }
}
