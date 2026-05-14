/// Thông tin chi tiết đầy đủ của một Customer.
/// Mở rộng từ UserProfile (auth) với các trường: identityNumber, address, group.
class CustomerProfile {
  final int customerId;
  final String username;
  final String fullName;
  final String identityNumber; // CMND / CCCD
  final String phone;
  final String address;
  final String email;
  final CustomerGroupSummary? group; // null nếu chưa thuộc nhóm nào

  const CustomerProfile({
    required this.customerId,
    required this.username,
    required this.fullName,
    required this.identityNumber,
    required this.phone,
    required this.address,
    required this.email,
    this.group,
  });

  factory CustomerProfile.fromJson(Map<String, dynamic> json) {
    final data = json['data'] ?? json;
    return CustomerProfile(
      customerId: data['customer_id'] ?? 0,
      username: data['username'] ?? '',
      fullName: data['full_name'] ?? '',
      identityNumber: data['identity_number'] ?? '',
      phone: data['phone'] ?? '',
      address: data['address'] ?? '',
      email: data['email'] ?? '',
      group: data['group'] != null
          ? CustomerGroupSummary.fromJson(data['group'])
          : null,
    );
  }

  CustomerProfile copyWith({
    String? fullName,
    String? identityNumber,
    String? address,
    String? email,
  }) {
    return CustomerProfile(
      customerId: customerId,
      username: username,
      fullName: fullName ?? this.fullName,
      identityNumber: identityNumber ?? this.identityNumber,
      phone: phone,
      address: address ?? this.address,
      email: email ?? this.email,
      group: group,
    );
  }
}

/// Tóm tắt nhóm — dùng trong CustomerProfile để hiện nhanh tên nhóm.
class CustomerGroupSummary {
  final int groupId;
  final String groupName;
  final bool isOwner;

  const CustomerGroupSummary({
    required this.groupId,
    required this.groupName,
    required this.isOwner,
  });

  factory CustomerGroupSummary.fromJson(Map<String, dynamic> json) {
    return CustomerGroupSummary(
      groupId: json['group_id'] ?? 0,
      groupName: json['group_name'] ?? '',
      isOwner: json['is_owner'] ?? false,
    );
  }
}

/// Chi tiết đầy đủ của một Customer Group (dùng trong CustomerGroupScreen).
class CustomerGroupDetail {
  final int groupId;
  final String groupName;
  final CustomerMember owner;
  final List<CustomerMember> members;
  final bool isOwner; // true nếu user hiện tại là chủ nhóm

  const CustomerGroupDetail({
    required this.groupId,
    required this.groupName,
    required this.owner,
    required this.members,
    required this.isOwner,
  });
}

/// Thành viên trong nhóm — hiển thị danh sách.
class CustomerMember {
  final int customerId;
  final String fullName;
  final String phone;

  const CustomerMember({
    required this.customerId,
    required this.fullName,
    required this.phone,
  });
}

/// Request body khi cập nhật profile.
class UpdateProfileRequest {
  final String fullName;
  final String identityNumber;
  final String address;
  final String email;

  const UpdateProfileRequest({
    required this.fullName,
    required this.identityNumber,
    required this.address,
    required this.email,
  });

  Map<String, dynamic> toJson() => {
    'full_name': fullName,
    'identity_number': identityNumber,
    'address': address,
    'email': email,
  };
}
