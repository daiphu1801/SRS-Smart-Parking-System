/// === BOOKING MODELS (Aligned with DB ERD) ===
/// DB Tables: Bookings, BookingDetails, Payments, Payment_Details

// ─── Enums (mirrors DB enums) ────────────────────────────────────────────────

/// booking_status enum
enum BookingStatus {
  active,
  expired,
  pendingPayment,
  canceled;

  String get label {
    switch (this) {
      case BookingStatus.active:       return 'Đang hoạt động';
      case BookingStatus.expired:      return 'Hết hạn';
      case BookingStatus.pendingPayment: return 'Chờ thanh toán';
      case BookingStatus.canceled:     return 'Đã huỷ';
    }
  }

  static BookingStatus fromString(String value) {
    switch (value.toUpperCase()) {
      case 'ACTIVE':          return BookingStatus.active;
      case 'EXPIRED':         return BookingStatus.expired;
      case 'PENDING_PAYMENT': return BookingStatus.pendingPayment;
      case 'CANCELED':        return BookingStatus.canceled;
      default:                return BookingStatus.pendingPayment;
    }
  }
}

/// payment_status enum
enum PaymentStatus {
  pending,
  success,
  failed,
  refunded;

  String get label {
    switch (this) {
      case PaymentStatus.pending:   return 'Chờ thanh toán';
      case PaymentStatus.success:   return 'Đã thanh toán';
      case PaymentStatus.failed:    return 'Thất bại';
      case PaymentStatus.refunded:  return 'Đã hoàn tiền';
    }
  }

  static PaymentStatus fromString(String value) {
    switch (value.toUpperCase()) {
      case 'SUCCESS':  return PaymentStatus.success;
      case 'FAILED':   return PaymentStatus.failed;
      case 'REFUNDED': return PaymentStatus.refunded;
      default:         return PaymentStatus.pending;
    }
  }
}

/// payment_method enum
enum PaymentMethod {
  cash,
  payos,
  vnpay;

  String get label {
    switch (this) {
      case PaymentMethod.cash:   return 'Tiền mặt';
      case PaymentMethod.payos:  return 'PayOS';
      case PaymentMethod.vnpay:  return 'VNPay';
    }
  }

  static PaymentMethod fromString(String value) {
    switch (value.toUpperCase()) {
      case 'PAYOS': return PaymentMethod.payos;
      case 'VNPAY': return PaymentMethod.vnpay;
      default:      return PaymentMethod.cash;
    }
  }
}

// ─── Models ──────────────────────────────────────────────────────────────────

/// Maps to DB: `Bookings`
/// Hợp đồng tổng gắn với GroupsCustomers (chủ hộ / đại diện công ty)
class Booking {
  final String id;
  // Changed from customerId/customerName → groupId/groupName per ERD
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

  Booking copyWith({
    PaymentStatus? paymentStatus,
    int? totalVehicles,
    double? totalAmount,
    List<BookingDetail>? details,
  }) {
    return Booking(
      id: id,
      groupId: groupId,
      groupName: groupName,
      totalVehicles: totalVehicles ?? this.totalVehicles,
      duration: duration,
      paymentStatus: paymentStatus ?? this.paymentStatus,
      paymentMethod: paymentMethod,
      createdAt: createdAt,
      totalAmount: totalAmount ?? this.totalAmount,
      details: details ?? this.details,
    );
  }

  factory Booking.fromJson(Map<String, dynamic> json) {
    return Booking(
      id:            json['id']?.toString() ?? '',
      groupId:       json['group_id']?.toString() ?? '',
      groupName:     json['group_name'] ?? '',
      totalVehicles: json['total_vehicles'] ?? 0,
      duration:      json['duration'] ?? '',
      paymentStatus: PaymentStatus.fromString(json['payment_status'] ?? ''),
      paymentMethod: json['payment_method'] != null
          ? PaymentMethod.fromString(json['payment_method'])
          : null,
      createdAt:     DateTime.tryParse(json['created_at'] ?? '') ?? DateTime.now(),
      totalAmount:   (json['total_amount'] ?? 0).toDouble(),
      details: (json['details'] as List<dynamic>?)
              ?.map((d) => BookingDetail.fromJson(d))
              .toList() ??
          [],
    );
  }
}

/// Maps to DB: `BookingDetails`
/// Chi tiết từng xe trong hợp đồng: liên kết Customer + xe + PackagePrice
class BookingDetail {
  final String id;
  final String bookingId;
  final String customerId;        // BookingDetails.customer_id → Customers.id
  final String plateNumber;       // vehicle_no
  final String vehicleType;       // từ VehicleTypes.type_code (CAR/BIKE)
  final String packageType;       // tên gói, ghép từ Packages.package_name
  final String duration;          // tính từ PackagePrice.duration_days
  final double price;             // PackagePrice.price
  final DateTime startDate;
  final DateTime endDate;
  final BookingStatus status;
  final String? packagePriceId;   // BookingDetails.package_price_id

  BookingDetail({
    required this.id,
    required this.bookingId,
    required this.customerId,
    required this.plateNumber,
    required this.vehicleType,
    required this.packageType,
    required this.duration,
    required this.price,
    required this.startDate,
    required this.endDate,
    required this.status,
    this.packagePriceId,
  });

  BookingDetail copyWith({
    BookingStatus? status,
    DateTime? endDate,
  }) {
    return BookingDetail(
      id: id,
      bookingId: bookingId,
      customerId: customerId,
      plateNumber: plateNumber,
      vehicleType: vehicleType,
      packageType: packageType,
      duration: duration,
      price: price,
      startDate: startDate,
      endDate: endDate ?? this.endDate,
      status: status ?? this.status,
      packagePriceId: packagePriceId,
    );
  }

  factory BookingDetail.fromJson(Map<String, dynamic> json) {
    return BookingDetail(
      id:             json['id']?.toString() ?? '',
      bookingId:      json['booking_id']?.toString() ?? '',
      customerId:     json['customer_id']?.toString() ?? '',
      plateNumber:    json['vehicle_no'] ?? '',
      vehicleType:    json['vehicle_type'] ?? '',
      packageType:    json['package_type'] ?? '',
      duration:       json['duration'] ?? '',
      price:          (json['price'] ?? 0).toDouble(),
      startDate:      DateTime.tryParse(json['start_date'] ?? '') ?? DateTime.now(),
      endDate:        DateTime.tryParse(json['end_date'] ?? '') ?? DateTime.now(),
      status:         BookingStatus.fromString(json['status'] ?? ''),
      packagePriceId: json['package_price_id']?.toString(),
    );
  }

  bool get isActive => status == BookingStatus.active;

  int get remainingDays => endDate.difference(DateTime.now()).inDays;
}

/// Maps to DB: `PackagePrices`
class PackagePrice {
  final String id;
  final String packageId;
  final String packageName;
  final String vehicleType;
  final int durationDays;
  final double price;

  PackagePrice({
    required this.id,
    required this.packageId,
    required this.packageName,
    required this.vehicleType,
    required this.durationDays,
    required this.price,
  });

  factory PackagePrice.fromJson(Map<String, dynamic> json) {
    return PackagePrice(
      id: json['id']?.toString() ?? '',
      packageId: json['package_id']?.toString() ?? '',
      packageName: json['package_name'] ?? '',
      vehicleType: json['vehicle_type'] ?? '',
      durationDays: json['duration_days'] ?? 0,
      price: (json['price'] ?? 0).toDouble(),
    );
  }
}
