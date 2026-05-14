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

  factory Complaint.fromJson(Map<String, dynamic> json) {
    return Complaint(
      id: json['id']?.toString() ?? '',
      customerId: json['customer_id']?.toString() ?? '',
      title: json['title'] ?? '',
      description: json['description'] ?? '',
      imageUrl: json['image_url'],
      status: ComplaintStatus.fromString(json['status'] ?? 'PENDING'),
      resolutionNote: json['resolution_note'],
      createdAt: DateTime.tryParse(json['created_at'] ?? '') ?? DateTime.now(),
      updatedAt: json['updated_at'] != null ? DateTime.tryParse(json['updated_at']) : null,
    );
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
