import 'package:equatable/equatable.dart';

enum PaymentStatus {
  pending,
  success,
  canceled,
  failed;

  String get label {
    switch (this) {
      case PaymentStatus.pending:
        return 'Chờ thanh toán';
      case PaymentStatus.success:
        return 'Thành công';
      case PaymentStatus.canceled:
        return 'Đã hủy';
      case PaymentStatus.failed:
        return 'Thất bại';
    }
  }
}

enum PaymentMethod {
  cash,
  bankTransfer,
  vnpay,
  creditCard;

  String get label {
    switch (this) {
      case PaymentMethod.cash:
        return 'Tiền mặt';
      case PaymentMethod.bankTransfer:
        return 'Chuyển khoản';
      case PaymentMethod.vnpay:
        return 'VNPay';
      case PaymentMethod.creditCard:
        return 'Thẻ tín dụng';
    }
  }

  String get toBackendString {
    switch (this) {
      case PaymentMethod.cash:
        return 'CASH';
      case PaymentMethod.bankTransfer:
        return 'BANK_TRANSFER';
      case PaymentMethod.vnpay:
        return 'VNPAY';
      case PaymentMethod.creditCard:
        return 'CREDIT_CARD';
    }
  }
}

// ----------------------------------------------------------------------
// Responses
// ----------------------------------------------------------------------

class PaymentCheckoutResponse extends Equatable {
  final int paymentId;
  final double amount;
  final String checkoutUrl;
  final String paymentCode;
  final String? message;

  const PaymentCheckoutResponse({
    required this.paymentId,
    required this.amount,
    required this.checkoutUrl,
    required this.paymentCode,
    this.message,
  });

  factory PaymentCheckoutResponse.fromJson(Map<String, dynamic> json) {
    return PaymentCheckoutResponse(
      paymentId: json['paymentId'] as int,
      amount: (json['amount'] as num).toDouble(),
      checkoutUrl: json['checkoutUrl'] as String,
      paymentCode: json['paymentCode'] as String,
      message: json['message'] as String?,
    );
  }

  @override
  List<Object?> get props => [paymentId, amount, checkoutUrl, paymentCode, message];
}

class PaymentInitiateResponse extends Equatable {
  final int paymentId;
  final double amount;
  final String checkoutUrl;
  final String paymentCode;
  final String? message;

  const PaymentInitiateResponse({
    required this.paymentId,
    required this.amount,
    required this.checkoutUrl,
    required this.paymentCode,
    this.message,
  });

  factory PaymentInitiateResponse.fromJson(Map<String, dynamic> json) {
    return PaymentInitiateResponse(
      paymentId: json['paymentId'] as int,
      amount: (json['amount'] as num).toDouble(),
      checkoutUrl: json['checkoutUrl'] as String,
      paymentCode: json['paymentCode'] as String,
      message: json['message'] as String?,
    );
  }

  @override
  List<Object?> get props => [paymentId, amount, checkoutUrl, paymentCode, message];
}

class PaymentResponse extends Equatable {
  final int id;
  final String? payerId;
  final String? customerFullName;
  final String? customerPhone;
  final String? transactionId;
  final String payCode;
  final double amount;
  final PaymentMethod method;
  final String? gateway;
  final PaymentStatus status;
  final DateTime createdAt;
  final DateTime? updatedAt;
  /// Chỉ có giá trị khi status == PENDING (Backend sẽ sinh link QR VietQR).
  final String? checkoutUrl;

  const PaymentResponse({
    required this.id,
    this.payerId,
    this.customerFullName,
    this.customerPhone,
    this.transactionId,
    required this.payCode,
    required this.amount,
    required this.method,
    this.gateway,
    required this.status,
    required this.createdAt,
    this.updatedAt,
    this.checkoutUrl,
  });

  factory PaymentResponse.fromJson(Map<String, dynamic> json) {
    return PaymentResponse(
      id: json['id'] as int,
      payerId: json['payerId']?.toString(),
      customerFullName: json['customerFullName'] as String?,
      customerPhone: json['customerPhone'] as String?,
      transactionId: json['transactionId'] as String?,
      payCode: json['payCode'] as String,
      amount: (json['amount'] as num).toDouble(),
      method: PaymentMethod.values.firstWhere(
        (e) => e.toBackendString == json['method'],
        orElse: () => PaymentMethod.bankTransfer,
      ),
      gateway: json['gateway'] as String?,
      status: PaymentStatus.values.firstWhere(
        (e) => e.name.toUpperCase() == json['status'],
        orElse: () => PaymentStatus.pending,
      ),
      createdAt: DateTime.parse(json['createdAt'] as String),
      updatedAt: json['updatedAt'] != null ? DateTime.parse(json['updatedAt'] as String) : null,
      checkoutUrl: json['checkoutUrl'] as String?,
    );
  }

  bool get isPending => status == PaymentStatus.pending;

  @override
  List<Object?> get props => [
        id,
        payerId,
        customerFullName,
        customerPhone,
        transactionId,
        payCode,
        amount,
        method,
        gateway,
        status,
        createdAt,
        updatedAt,
        checkoutUrl,
      ];
}

class PaymentDetailResponse extends Equatable {
  final int id;
  final int paymentId;
  final int bookingDetailId;
  final double itemAmount;
  final DateTime appliedStartDate;
  final DateTime appliedEndDate;

  const PaymentDetailResponse({
    required this.id,
    required this.paymentId,
    required this.bookingDetailId,
    required this.itemAmount,
    required this.appliedStartDate,
    required this.appliedEndDate,
  });

  factory PaymentDetailResponse.fromJson(Map<String, dynamic> json) {
    return PaymentDetailResponse(
      id: json['id'] as int,
      paymentId: json['paymentId'] as int,
      bookingDetailId: json['bookingDetailId'] as int,
      itemAmount: (json['itemAmount'] as num).toDouble(),
      appliedStartDate: DateTime.parse(json['appliedStartDate'] as String),
      appliedEndDate: DateTime.parse(json['appliedEndDate'] as String),
    );
  }

  @override
  List<Object?> get props => [
        id,
        paymentId,
        bookingDetailId,
        itemAmount,
        appliedStartDate,
        appliedEndDate,
      ];
}

class PaymentTreeResponse extends Equatable {
  final PaymentResponse paymentInfo;
  final List<PaymentDetailResponse> details;

  const PaymentTreeResponse({
    required this.paymentInfo,
    required this.details,
  });

  factory PaymentTreeResponse.fromJson(Map<String, dynamic> json) {
    return PaymentTreeResponse(
      paymentInfo: PaymentResponse.fromJson(json['paymentInfo'] as Map<String, dynamic>),
      details: (json['details'] as List<dynamic>)
          .map((e) => PaymentDetailResponse.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
  }

  @override
  List<Object?> get props => [paymentInfo, details];
}

// ----------------------------------------------------------------------
// Requests
// ----------------------------------------------------------------------

class CheckoutRequest extends Equatable {
  final List<int> bookingDetailIds;

  const CheckoutRequest({required this.bookingDetailIds});

  Map<String, dynamic> toJson() {
    return {
      'bookingDetailIds': bookingDetailIds,
    };
  }

  @override
  List<Object?> get props => [bookingDetailIds];
}

class RenewItemRequest extends Equatable {
  final int oldBookingDetailId;
  final int newPackagePriceId;

  const RenewItemRequest({
    required this.oldBookingDetailId,
    required this.newPackagePriceId,
  });

  Map<String, dynamic> toJson() {
    return {
      'oldBookingDetailId': oldBookingDetailId,
      'newPackagePriceId': newPackagePriceId,
    };
  }

  @override
  List<Object?> get props => [oldBookingDetailId, newPackagePriceId];
}

class PaymentBookingRequest extends Equatable {
  final List<RenewItemRequest> items;
  final String gateway;
  final String returnUrl;

  const PaymentBookingRequest({
    required this.items,
    required this.gateway,
    required this.returnUrl,
  });

  Map<String, dynamic> toJson() {
    return {
      'items': items.map((e) => e.toJson()).toList(),
      'gateway': gateway,
      'returnUrl': returnUrl,
    };
  }

  @override
  List<Object?> get props => [items, gateway, returnUrl];
}
