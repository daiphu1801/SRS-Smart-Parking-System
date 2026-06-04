// === BOOKING MODELS (Aligned with DB ERD) ===
// DB Tables: Bookings, BookingDetails, Payments, Payment_Details

// ─── Enums (mirrors DB enums) ────────────────────────────────────────────────

/// booking_status enum
enum BookingStatus {
  active,
  expired,
  pendingPayment,
  canceled,
  pendingActivation,
  needsAttention,
  partialPayment,
  draft,
  complete;

  String get toBackendString {
    switch (this) {
      case BookingStatus.active:
        return 'ACTIVE';
      case BookingStatus.expired:
        return 'EXPIRED';
      case BookingStatus.pendingPayment:
        return 'PENDING_PAYMENT';
      case BookingStatus.canceled:
        return 'CANCELED';
      case BookingStatus.pendingActivation:
        return 'PENDING_ACTIVATION';
      case BookingStatus.needsAttention:
        return 'NEEDS_ATTENTION';
      case BookingStatus.partialPayment:
        return 'PARTIAL_PAYMENT';
      case BookingStatus.draft:
        return 'DRAFT';
      case BookingStatus.complete:
        return 'COMPLETE';
    }
  }

  static BookingStatus fromString(String value) {
    switch (value.toUpperCase()) {
      case 'ACTIVE':
        return BookingStatus.active;
      case 'EXPIRED':
        return BookingStatus.expired;
      case 'PENDING_PAYMENT':
        return BookingStatus.pendingPayment;
      case 'CANCELED':
        return BookingStatus.canceled;
      case 'PENDING_ACTIVATION':
        return BookingStatus.pendingActivation;
      case 'NEEDS_ATTENTION':
        return BookingStatus.needsAttention;
      case 'PARTIAL_PAYMENT':
        return BookingStatus.partialPayment;
      case 'DRAFT':
        return BookingStatus.draft;
      case 'COMPLETE':
        return BookingStatus.complete;
      default:
        return BookingStatus.draft;
    }
  }
}

/// payment_status enum
enum PaymentStatus {
  pending,
  success,
  failed,
  refunded;

  static PaymentStatus fromString(String value) {
    switch (value.toUpperCase()) {
      case 'SUCCESS':
        return PaymentStatus.success;
      case 'FAILED':
        return PaymentStatus.failed;
      case 'REFUNDED':
        return PaymentStatus.refunded;
      default:
        return PaymentStatus.pending;
    }
  }
}

/// payment_method enum
enum PaymentMethod {
  cash,
  payos,
  vnpay;

  static PaymentMethod fromString(String value) {
    switch (value.toUpperCase()) {
      case 'PAYOS':
        return PaymentMethod.payos;
      case 'VNPAY':
        return PaymentMethod.vnpay;
      default:
        return PaymentMethod.cash;
    }
  }
}

// ─── Models ──────────────────────────────────────────────────────────────────

class BookingAndDetailResponse {
  final BookingResponse bookingInfo;
  final List<BookingDetailDto> details;

  BookingAndDetailResponse({required this.bookingInfo, required this.details});

  factory BookingAndDetailResponse.fromJson(Map<String, dynamic> json) {
    return BookingAndDetailResponse(
      bookingInfo: BookingResponse.fromJson(json['bookingInfo'] ?? {}),
      details:
          (json['details'] as List<dynamic>?)
              ?.map((e) => BookingDetailDto.fromJson(e))
              .toList() ??
          [],
    );
  }
}

class Booking {
  final String id;
  final String groupId;
  final String groupName;
  final int totalVehicles;
  final String duration;
  final PaymentStatus paymentStatus;
  final PaymentMethod? paymentMethod;
  final DateTime createdAt;
  final double totalAmount;
  final List<BookingDetail> details;

  Booking({
    required this.id,
    required this.groupId,
    required this.groupName,
    required this.totalVehicles,
    required this.duration,
    required this.paymentStatus,
    this.paymentMethod,
    required this.createdAt,
    required this.totalAmount,
    this.details = const [],
  });

  factory Booking.fromBackend(BookingAndDetailResponse response) {
    final bookingInfo = response.bookingInfo;
    final details = response.details.map(BookingDetail.fromDto).toList();
    final totalAmount = details.fold<double>(
      0,
      (sum, detail) => sum + detail.price,
    );
    final hasPending = details.any(
      (d) => d.status == BookingStatus.pendingPayment,
    );

    return Booking(
      id: bookingInfo.id.toString(),
      groupId: bookingInfo.groupId.toString(),
      groupName: bookingInfo.groupName,
      totalVehicles: details.length,
      duration: '',
      paymentStatus: hasPending ? PaymentStatus.pending : PaymentStatus.success,
      createdAt: bookingInfo.createdAt ?? DateTime.now(),
      totalAmount: totalAmount,
      details: details,
    );
  }
}

class BookingDetail {
  final String id;
  final String bookingId;
  final String customerId;
  final String plateNumber;
  final String vehicleType;
  final String packageType;
  final String duration;
  final double price;
  final DateTime startDate;
  final DateTime endDate;
  final BookingStatus status;
  final String? packagePriceId;
  final int? vehicleTypeId;
  final String customerName;
  final String customerPhone;

  BookingDetail({
    required this.id,
    required this.bookingId,
    required this.customerId,
    required this.customerName,
    required this.customerPhone,
    required this.plateNumber,
    required this.vehicleType,
    required this.packageType,
    required this.duration,
    required this.price,
    required this.startDate,
    required this.endDate,
    required this.status,
    this.packagePriceId,
    this.vehicleTypeId,
  });

  factory BookingDetail.fromDto(BookingDetailDto dto) {
    return BookingDetail(
      id: dto.id.toString(),
      bookingId: dto.bookingId.toString(),
      customerId: dto.customerId.toString(),
      customerName: dto.customerName,
      customerPhone: dto.customerPhone,
      plateNumber: dto.vehicleNo,
      vehicleType: dto.typeName ?? '',
      packageType: dto.packagePriceName,
      duration: '${dto.durationMonths} tháng',
      price: dto.price,
      startDate: dto.startDate,
      endDate: dto.endDate,
      status: dto.status,
      packagePriceId: dto.packagePriceId.toString(),
      vehicleTypeId: dto.vehicleTypeId,
    );
  }

  bool get isActive => status == BookingStatus.active;
  bool get isDraft => status == BookingStatus.draft;
  bool get isPendingPayment => status == BookingStatus.pendingPayment;

  int get remainingDays => endDate.difference(DateTime.now()).inDays;
}

class BookingResponse {
  final int id;
  final DateTime? createdAt;
  final int groupId;
  final String groupName;
  final String groupCode;
  final int packageId;
  final String packageName;
  final int? createdBy;
  final String? creatorName;

  BookingResponse({
    required this.id,
    this.createdAt,
    required this.groupId,
    required this.groupName,
    required this.groupCode,
    required this.packageId,
    required this.packageName,
    this.createdBy,
    this.creatorName,
  });

  factory BookingResponse.fromJson(Map<String, dynamic> json) {
    return BookingResponse(
      id: json['id'] ?? 0,
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'])
          : null,
      groupId: json['groupId'] ?? 0,
      groupName: json['groupName'] ?? '',
      groupCode: json['groupCode'] ?? '',
      packageId: json['packageId'] ?? 0,
      packageName: json['packageName'] ?? '',
      createdBy: json['createdBy'],
      creatorName: json['creatorName'],
    );
  }
}

class BookingDetailDto {
  final int id;
  final int bookingId;
  final int customerId;
  final String customerPhone;
  final String customerName;
  final int packagePriceId;
  final double price;
  final String packagePriceName;
  final int durationMonths;
  final String vehicleNo;
  final DateTime startDate;
  final DateTime endDate;
  final BookingStatus status;
  final DateTime? createdAt;
  final int? vehicleTypeId;
  final String? typeName;

  BookingDetailDto({
    required this.id,
    required this.bookingId,
    required this.customerId,
    required this.customerPhone,
    required this.customerName,
    required this.packagePriceId,
    required this.price,
    required this.packagePriceName,
    required this.durationMonths,
    required this.vehicleNo,
    required this.startDate,
    required this.endDate,
    required this.status,
    this.createdAt,
    this.vehicleTypeId,
    this.typeName,
  });

  factory BookingDetailDto.fromJson(Map<String, dynamic> json) {
    return BookingDetailDto(
      id: json['id'] ?? 0,
      bookingId: json['bookingId'] ?? 0,
      customerId: json['customerId'] ?? 0,
      customerPhone: json['customerPhone'] ?? '',
      customerName: json['customerName'] ?? '',
      packagePriceId: json['packagePriceId'] ?? 0,
      price: (json['price'] ?? 0).toDouble(),
      packagePriceName: json['packagePriceName'] ?? '',
      durationMonths: json['durationMonths'] ?? 0,
      vehicleNo: json['vehicleNo'] ?? '',
      startDate: DateTime.tryParse(json['startDate'] ?? '') ?? DateTime.now(),
      endDate: DateTime.tryParse(json['endDate'] ?? '') ?? DateTime.now(),
      status: BookingStatus.fromString(json['status'] ?? ''),
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'])
          : null,
      vehicleTypeId: json['vehicleTypeId'] as int?,
      typeName: json['typeName'] as String?,
    );
  }

  bool get isActive => status == BookingStatus.active;
  bool get isDraft => status == BookingStatus.draft;
  bool get isPendingPayment => status == BookingStatus.pendingPayment;
  int get remainingDays => endDate.difference(DateTime.now()).inDays;
}

class BookingDetailCreateRequest {
  final int bookingId;
  final int customerId;
  final int packagePriceId;
  final String packagePriceName;
  final String vehicleNo;
  final DateTime startDate;
  final DateTime endDate;
  final BookingStatus status;
  final int vehicleTypeId;

  BookingDetailCreateRequest({
    required this.bookingId,
    required this.customerId,
    required this.packagePriceId,
    required this.packagePriceName,
    required this.vehicleNo,
    required this.startDate,
    required this.endDate,
    required this.status,
    required this.vehicleTypeId,
  });

  Map<String, dynamic> toJson() {
    return {
      'bookingId': bookingId,
      'customerId': customerId,
      'packagePriceId': packagePriceId,
      'packagePriceName': packagePriceName,
      'vehicleNo': vehicleNo,
      // startDate needs to be 00h00m00s, handled externally
      'startDate': startDate.toIso8601String(),
      // endDate needs to be 23h59m59s, handled externally
      'endDate': endDate.toIso8601String(),
      'status': status.toBackendString,
      'vehicleTypeId': vehicleTypeId,
    };
  }
}
