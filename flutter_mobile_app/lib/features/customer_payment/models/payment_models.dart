import 'package:equatable/equatable.dart';

enum PaymentStatus { pending, success, failed, refunded }
enum PaymentMethod { cash, payos, vnpay, vietqr }

class Payment extends Equatable {
  final int id;
  final String payCode;
  final double amount;
  final PaymentMethod method;
  final PaymentStatus status;
  final DateTime createdAt;

  const Payment({
    required this.id,
    required this.payCode,
    required this.amount,
    required this.method,
    required this.status,
    required this.createdAt,
  });

  @override
  List<Object?> get props => [id, payCode, amount, method, status, createdAt];

  Payment copyWith({
    int? id,
    String? payCode,
    double? amount,
    PaymentMethod? method,
    PaymentStatus? status,
    DateTime? createdAt,
  }) {
    return Payment(
      id: id ?? this.id,
      payCode: payCode ?? this.payCode,
      amount: amount ?? this.amount,
      method: method ?? this.method,
      status: status ?? this.status,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}

class VietQRData extends Equatable {
  final String bankId;
  final String accountNo;
  final String template;
  final double amount;
  final String description;
  final String accountName;

  const VietQRData({
    required this.bankId,
    required this.accountNo,
    required this.template,
    required this.amount,
    required this.description,
    required this.accountName,
  });

  String get qrUrl {
    // Sử dụng API VietQR (https://api.vietqr.io/) để generate hình ảnh QR Code
    return 'https://img.vietqr.io/image/$bankId-$accountNo-$template.png?amount=${amount.toInt()}&addInfo=$description&accountName=$accountName';
  }

  @override
  List<Object?> get props => [bankId, accountNo, template, amount, description, accountName];
}
