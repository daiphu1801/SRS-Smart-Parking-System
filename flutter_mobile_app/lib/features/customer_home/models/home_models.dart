
// ─────────────────────────────────────────────────────────────────────────────
// Root DTO – maps toàn bộ response từ GET /api/v1/customer/home
// ─────────────────────────────────────────────────────────────────────────────

class HomeDashboard {
  final ProfileSummary profile;
  final List<VehicleCard> vehicles;
  final ActiveSessionCard? activeSession; // null nếu không có xe trong bãi
  final PendingActionSummary pendingActions;

  const HomeDashboard({
    required this.profile,
    required this.vehicles,
    this.activeSession,
    required this.pendingActions,
  });

  factory HomeDashboard.fromJson(Map<String, dynamic> json) {
    final sessionJson = json['activeSession'] as Map<String, dynamic>?;
    final vehiclesRaw = json['vehicles'] as List<dynamic>? ?? [];

    return HomeDashboard(
      profile: ProfileSummary.fromJson(
          _asMap(json['profile'])),
      vehicles: vehiclesRaw
          .map((e) => VehicleCard.fromJson(_asMap(e)))
          .toList(),
      activeSession:
          sessionJson != null ? ActiveSessionCard.fromJson(sessionJson) : null,
      pendingActions: PendingActionSummary.fromJson(
          _asMap(json['pendingActions'])),
    );
  }

  static Map<String, dynamic> _asMap(Object? value) {
    if (value is Map<String, dynamic>) return value;
    if (value is Map) return Map<String, dynamic>.from(value);
    return {};
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Profile
// ─────────────────────────────────────────────────────────────────────────────

class ProfileSummary {
  final int customerId;
  final String fullName;
  final String phone;
  final String? groupName;

  const ProfileSummary({
    required this.customerId,
    required this.fullName,
    required this.phone,
    this.groupName,
  });

  factory ProfileSummary.fromJson(Map<String, dynamic> json) {
    return ProfileSummary(
      customerId: json['customerId'] as int? ?? 0,
      fullName: json['fullName'] as String? ?? '',
      phone: json['phone'] as String? ?? '',
      groupName: json['groupName'] as String?,
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Vehicle Card
// ─────────────────────────────────────────────────────────────────────────────

/// Status label được Backend tính sẵn:
/// "ACTIVE" | "EXPIRING_SOON" | "EXPIRED"
class VehicleCard {
  final int bookingDetailId;
  final String vehicleNo;
  final String? packageName;
  final int? durationMonths;
  final DateTime? endDate;
  final int daysLeft;
  final VehicleStatus status;

  const VehicleCard({
    required this.bookingDetailId,
    required this.vehicleNo,
    this.packageName,
    this.durationMonths,
    this.endDate,
    required this.daysLeft,
    required this.status,
  });

  factory VehicleCard.fromJson(Map<String, dynamic> json) {
    final rawStatus = json['status'] as String? ?? 'ACTIVE';
    return VehicleCard(
      bookingDetailId: json['bookingDetailId'] as int? ?? 0,
      vehicleNo: json['vehicleNo'] as String? ?? '',
      packageName: json['packageName'] as String?,
      durationMonths: json['durationMonths'] as int?,
      endDate: json['endDate'] != null
          ? DateTime.tryParse(json['endDate'] as String)
          : null,
      daysLeft: json['daysLeft'] as int? ?? 0,
      status: VehicleStatus.fromString(rawStatus),
    );
  }

  bool get isExpiringSoon => status == VehicleStatus.expiringSoon;
  bool get isExpired => status == VehicleStatus.expired;
}

enum VehicleStatus {
  active,
  expiringSoon,
  expired;

  static VehicleStatus fromString(String raw) {
    return switch (raw.toUpperCase()) {
      'EXPIRING_SOON' => VehicleStatus.expiringSoon,
      'EXPIRED' => VehicleStatus.expired,
      _ => VehicleStatus.active,
    };
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Active Parking Session
// ─────────────────────────────────────────────────────────────────────────────

class ActiveSessionCard {
  final int sessionId;
  final String vehicleNo;
  final DateTime entryTime;
  final String? zoneInName;
  final double estimatedFee;

  const ActiveSessionCard({
    required this.sessionId,
    required this.vehicleNo,
    required this.entryTime,
    this.zoneInName,
    required this.estimatedFee,
  });

  factory ActiveSessionCard.fromJson(Map<String, dynamic> json) {
    return ActiveSessionCard(
      sessionId: json['sessionId'] as int? ?? 0,
      vehicleNo: json['vehicleNo'] as String? ?? '',
      entryTime: DateTime.tryParse(json['entryTime'] as String? ?? '') ??
          DateTime.now(),
      zoneInName: json['zoneInName'] as String?,
      estimatedFee: (json['estimatedFee'] as num?)?.toDouble() ?? 0.0,
    );
  }

  /// Thời gian đã đỗ tính đến hiện tại
  Duration get parkingDuration => DateTime.now().difference(entryTime);

  String get formattedDuration {
    final h = parkingDuration.inHours;
    final m = parkingDuration.inMinutes.remainder(60);
    if (h > 0) return '${h}h ${m}m';
    return '${m}m';
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pending Action Summary
// ─────────────────────────────────────────────────────────────────────────────

class PendingActionSummary {
  final int draftCount;
  final int pendingPaymentCount;
  final int expiringSoonCount;

  const PendingActionSummary({
    required this.draftCount,
    required this.pendingPaymentCount,
    required this.expiringSoonCount,
  });

  factory PendingActionSummary.fromJson(Map<String, dynamic> json) {
    return PendingActionSummary(
      draftCount: json['draftCount'] as int? ?? 0,
      pendingPaymentCount: json['pendingPaymentCount'] as int? ?? 0,
      expiringSoonCount: json['expiringSoonCount'] as int? ?? 0,
    );
  }

  /// Tổng xe cần gia hạn: chỉ dùng expiringSoon cho banner gia hạn
  bool get hasRenewalAlert => expiringSoonCount > 0;

  /// Tổng pending checkout (giỏ hàng + chờ thanh toán)
  int get totalPending => draftCount + pendingPaymentCount;
  bool get hasPendingPayment => totalPending > 0;
}
