enum SessionStatus {
  ongoing,
  completed;

  String get label {
    switch (this) {
      case SessionStatus.ongoing:
        return 'Đang đỗ';
      case SessionStatus.completed:
        return 'Hoàn thành';
    }
  }
}

class ParkingSession {
  final String id;
  final String customerId;
  final String plateNumber;
  final String vehicleType;
  final String? bookingDetailId;
  final DateTime entryTime;
  final DateTime? exitTime;
  final String? imageInUrl;
  final String? imageOutUrl;
  final String? zoneInName;
  final String? zoneOutName;
  final double amountDue;
  final double amountPaid;
  final double amountLeft;
  final bool isPaid;
  final DateTime? gracePeriodEnd;
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
    this.amountPaid = 0,
    this.amountLeft = 0,
    this.isPaid = false,
    this.gracePeriodEnd,
    this.flagManual = false,
    required this.status,
  });

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

  bool get isInGracePeriod =>
      bookingDetailId == null &&
      isPaid &&
      exitTime == null &&
      gracePeriodEnd != null;

  Duration? get gracePeriodRemaining {
    if (gracePeriodEnd == null) return null;
    final remaining = gracePeriodEnd!.difference(DateTime.now());
    return remaining.isNegative ? Duration.zero : remaining;
  }

  factory ParkingSession.fromJson(Map<String, dynamic> json) {
    final exitTime = _asDateTime(_value(json, 'exitTime', 'exit_time'));
    final amountDue = _asDouble(_value(json, 'amountDue', 'amount_due'));
    final amountPaid = _asDouble(_value(json, 'amountPaid', 'amount_paid'));
    final amountLeftValue = _value(json, 'amountLeft', 'amount_left');
    final amountLeft = amountLeftValue == null
        ? amountDue - amountPaid
        : _asDouble(amountLeftValue);
    final isPaid = amountDue <= 0 || amountLeft <= 0 || amountPaid >= amountDue;

    return ParkingSession(
      id: _value(json, 'id')?.toString() ?? '',
      customerId: _value(json, 'customerId', 'customer_id')?.toString() ?? '',
      plateNumber:
          _value(json, 'vehicleNo', 'vehicle_no', 'plateNumber')?.toString() ??
          '',
      vehicleType: _vehicleTypeFromJson(json),
      bookingDetailId: _value(
        json,
        'bookingDetailId',
        'booking_detail_id',
      )?.toString(),
      entryTime:
          _asDateTime(_value(json, 'entryTime', 'entry_time')) ??
          DateTime.now(),
      exitTime: exitTime,
      imageInUrl: _value(json, 'imageInUrl', 'image_in_url')?.toString(),
      imageOutUrl: _value(json, 'imageOutUrl', 'image_out_url')?.toString(),
      zoneInName: _value(json, 'zoneInName', 'zone_in_name')?.toString(),
      zoneOutName: _value(json, 'zoneOutName', 'zone_out_name')?.toString(),
      amountDue: amountDue,
      amountPaid: amountPaid,
      amountLeft: amountLeft,
      isPaid: isPaid,
      gracePeriodEnd: _asDateTime(
        _value(json, 'gracePeriodEnd', 'grace_period_end'),
      ),
      flagManual:
          _value(json, 'flagManual', 'flag_manual') == true ||
          _value(json, 'flagManual', 'flag_manual')?.toString() == 'true',
      status: exitTime == null
          ? SessionStatus.ongoing
          : SessionStatus.completed,
    );
  }

  static Object? _value(
    Map<String, dynamic> json,
    String key1, [
    String? key2,
    String? key3,
  ]) {
    if (json.containsKey(key1)) return json[key1];
    if (key2 != null && json.containsKey(key2)) return json[key2];
    if (key3 != null && json.containsKey(key3)) return json[key3];
    return null;
  }

  static double _asDouble(Object? value) {
    if (value is num) return value.toDouble();
    return double.tryParse(value?.toString() ?? '') ?? 0;
  }

  static DateTime? _asDateTime(Object? value) {
    if (value == null) return null;
    return DateTime.tryParse(value.toString());
  }

  static String _vehicleTypeFromJson(Map<String, dynamic> json) {
    final explicit = _value(json, 'vehicleType', 'vehicle_type')?.toString();
    if (explicit != null && explicit.trim().isNotEmpty) {
      return explicit.toUpperCase();
    }

    final name = _value(json, 'vehicleName', 'vehicle_name')?.toString() ?? '';
    final normalized = name.toLowerCase();
    if (normalized.contains('bike') ||
        normalized.contains('motor') ||
        normalized.contains('máy') ||
        normalized.contains('may')) {
      return 'BIKE';
    }
    return 'CAR';
  }
}
