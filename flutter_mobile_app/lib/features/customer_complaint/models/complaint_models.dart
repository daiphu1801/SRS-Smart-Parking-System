import 'package:flutter/material.dart';

enum ComplaintStatus {
  pending,
  processing,
  resolved,
  rejected;

  String get label {
    switch (this) {
      case ComplaintStatus.pending:
        return 'Chờ xử lý';
      case ComplaintStatus.processing:
        return 'Đang xử lý';
      case ComplaintStatus.resolved:
        return 'Đã giải quyết';
      case ComplaintStatus.rejected:
        return 'Từ chối';
    }
  }

  Color get color {
    switch (this) {
      case ComplaintStatus.pending:
        return Colors.orange;
      case ComplaintStatus.processing:
        return Colors.blue;
      case ComplaintStatus.resolved:
        return Colors.green;
      case ComplaintStatus.rejected:
        return Colors.red;
    }
  }

  static ComplaintStatus fromString(String value) {
    switch (value.toUpperCase()) {
      case 'PROCESSING':
        return ComplaintStatus.processing;
      case 'RESOLVED':
        return ComplaintStatus.resolved;
      case 'REJECTED':
        return ComplaintStatus.rejected;
      default:
        return ComplaintStatus.pending;
    }
  }
}

class Complaint {
  final String id;
  final String customerId;
  final String title;
  final String description;
  final String? imageUrl;
  final ComplaintStatus status;
  final String? resolutionNote;
  final DateTime createdAt;
  final DateTime? updatedAt;

  Complaint({
    required this.id,
    required this.customerId,
    required this.title,
    required this.description,
    this.imageUrl,
    required this.status,
    this.resolutionNote,
    required this.createdAt,
    this.updatedAt,
  });

  /// Parse từ backend [ComplaintDetailResponse] (camelCase).
  /// Backend không có trường title, nên lấy dòng đầu tiên của [content] làm tiêu đề hiển thị.
  factory Complaint.fromJson(Map<String, dynamic> json) {
    // content = toàn bộ nội dung từ backend (ghép title+desc từ phía Flutter khi tạo)
    final fullContent = json['content']?.toString() ?? '';
    // Tách dòng đầu tiên làm title để hiển thị, phần còn lại là description
    final lines = fullContent.split('\n\n');
    final title = lines.isNotEmpty ? lines.first.trim() : fullContent.trim();
    final description = lines.length > 1 ? lines.sublist(1).join('\n\n').trim() : '';

    // isSolved: true → resolved, null/false → pending
    final isSolved = json['isSolved'] as bool? ?? false;
    final status = isSolved ? ComplaintStatus.resolved : ComplaintStatus.pending;

    return Complaint(
      id: json['id']?.toString() ?? '',
      customerId: json['createdBy']?.toString() ?? '',
      title: title,
      description: description.isNotEmpty ? description : fullContent,
      imageUrl: json['imgUrl'],
      status: status,
      resolutionNote: null, // Backend ComplaintDetailResponse không có resolutionNote
      createdAt: _parseDateTime(json['createdAt']) ?? DateTime.now(),
      updatedAt: _parseDateTime(json['solvedAt']),
    );
  }

  static DateTime? _parseDateTime(dynamic value) {
    if (value == null) return null;
    if (value is String) return DateTime.tryParse(value);
    // Spring Boot có thể trả về dạng array [year, month, day, ...]
    if (value is List && value.length >= 6) {
      return DateTime(
        (value[0] as num).toInt(),
        (value[1] as num).toInt(),
        (value[2] as num).toInt(),
        (value[3] as num).toInt(),
        (value[4] as num).toInt(),
        (value[5] as num).toInt(),
      );
    }
    return null;
  }

  Map<String, dynamic> toJson() {
    return {
      'customer_id': customerId,
      'title': title,
      'description': description,
      'image_url': imageUrl,
    };
  }
}
