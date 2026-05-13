/// === PARKING SESSION MODELS (Aligned with DB ERD) ===
/// DB Table: ParkingSessions

// ─── Enums ────────────────────────────────────────────────────────────────────

/// Session status — derived from exit_time & is_paid
enum SessionStatus {
  ongoing,   // xe vẫn trong bãi
  completed; // xe đã ra và đã thanh toán

  String get label {
    switch (this) {
      case SessionStatus.ongoing:   return 'Đang đỗ';
      case SessionStatus.completed: return 'Hoàn thành';
    }
  }

  static SessionStatus fromString(String value) {
    switch (value.toUpperCase()) {
      case 'ONGOING': return SessionStatus.ongoing;
      default:        return SessionStatus.completed;
    }
  }
}

// ─── Model ────────────────────────────────────────────────────────────────────

/// Maps to DB: `ParkingSessions`
class ParkingSession {
  final String id;                         // ParkingSessions.id (bigint)
  final String customerId;                 // Trường phụ trợ để filter (không có trực tiếp trong DB)
  final String plateNumber;                // vehicle_no
  final String vehicleType;               // từ VehicleTypes.type_code (CAR / BIKE)
  final String? bookingDetailId;          // booking_detail_id (null = xe vãng lai)

  // Thời gian (DB: entry_time, exit_time)
  final DateTime entryTime;
  final DateTime? exitTime;

  // Ảnh Camera LPR (DB: image_in_url, image_out_url)
  final String? imageInUrl;
  final String? imageOutUrl;

  // Thông tin cổng (DB: zone_in_id, zone_out_id → join → Zones.zone_name)
  final String? zoneInName;               // Tên cổng vào (VD: "Cổng chính Tòa A")
  final String? zoneOutName;             // Tên cổng ra

  // Thanh toán (DB: amount_due, is_paid)
  final double amountDue;                // amount_due (thay vì int fee)
  final bool isPaid;                     // is_paid boolean

  // Thời gian ân hạn (DB: grace_period_end)
  final DateTime? gracePeriodEnd;

  // Can thiệp thủ công (DB: flag_manual)
  final bool flagManual;

  final SessionStatus status;

  ParkingSession({
    required this.id,
    required this.customerId,
    required this.plateNumber,
    required this.vehicleType,
    this.bookingDetailId,
    required this.entryTime,
    this.exitTime,
    this.imageInUrl,
    this.imageOutUrl,
    this.zoneInName,
    this.zoneOutName,
    this.amountDue = 0,
    this.isPaid = false,
    this.gracePeriodEnd,
    this.flagManual = false,
    required this.status,
  });

  // ─── Computed properties ───────────────────────────────────────────────────

  Duration get parkingDuration {
    final end = exitTime ?? DateTime.now();
    return end.difference(entryTime);
  }

  String get durationFormatted {
    final d = parkingDuration;
    final hours = d.inHours;
    final minutes = d.inMinutes.remainder(60);
    if (hours == 0) return '${minutes}ph';
    return '${hours}g ${minutes}ph';
  }

  bool get isFreeParking => bookingDetailId != null && amountDue == 0;

  bool get isOngoing => status == SessionStatus.ongoing;

  /// Xe đã thanh toán nhưng chưa rời bãi (trong thời gian ân hạn)
  bool get isInGracePeriod =>
      bookingDetailId == null && isPaid && exitTime == null && gracePeriodEnd != null;

  Duration? get gracePeriodRemaining {
    if (gracePeriodEnd == null) return null;
    final remaining = gracePeriodEnd!.difference(DateTime.now());
    return remaining.isNegative ? Duration.zero : remaining;
  }

  // ─── Factory ───────────────────────────────────────────────────────────────

  factory ParkingSession.fromJson(Map<String, dynamic> json) {
    return ParkingSession(
      id:               json['id']?.toString() ?? '',
      customerId:       json['customer_id']?.toString() ?? '',
      plateNumber:      json['vehicle_no'] ?? '',
      vehicleType:      json['vehicle_type'] ?? '',
      bookingDetailId:  json['booking_detail_id']?.toString(),
      entryTime:        DateTime.tryParse(json['entry_time'] ?? '') ?? DateTime.now(),
      exitTime:         json['exit_time'] != null ? DateTime.tryParse(json['exit_time']) : null,
      imageInUrl:       json['image_in_url'],
      imageOutUrl:      json['image_out_url'],
      zoneInName:       json['zone_in_name'],
      zoneOutName:      json['zone_out_name'],
      amountDue:        (json['amount_due'] ?? 0).toDouble(),
      isPaid:           json['is_paid'] ?? false,
      gracePeriodEnd:   json['grace_period_end'] != null
                          ? DateTime.tryParse(json['grace_period_end'])
                          : null,
      flagManual:       json['flag_manual'] ?? false,
      status:           SessionStatus.fromString(json['status'] ?? 'COMPLETED'),
    );
  }
}
