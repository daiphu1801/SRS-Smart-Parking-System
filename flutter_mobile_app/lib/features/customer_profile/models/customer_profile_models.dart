class CustomerProfile {
  final int id;
  final int? groupId;
  final int? accountId;
  final String fullName;
  final String phone;
  final bool isOnline;
  final String address;
  final String? groupName;

  const CustomerProfile({
    required this.id,
    this.groupId,
    this.accountId,
    required this.fullName,
    required this.phone,
    required this.isOnline,
    required this.address,
    this.groupName,
  });

  factory CustomerProfile.fromJson(Map<String, dynamic> json) {
    final data = json['data'] ?? json;
    return CustomerProfile(
      id: data['id'] ?? 0,
      groupId: data['groupId'],
      accountId: data['accountId'],
      fullName: data['fullName'] ?? '',
      phone: data['phone'] ?? '',
      isOnline: data['isOnline'] ?? false,
      address: data['address'] ?? '',
      groupName: data['groupName'],
    );
  }

  CustomerProfile copyWith({
    String? fullName,
    String? address,
    String? phone,
  }) {
    return CustomerProfile(
      id: id,
      groupId: groupId,
      accountId: accountId,
      fullName: fullName ?? this.fullName,
      phone: phone ?? this.phone,
      isOnline: isOnline,
      address: address ?? this.address,
      groupName: groupName,
    );
  }
}

class UpdateProfileRequest {
  final String fullName;
  final String address;
  final String phone;

  const UpdateProfileRequest({
    required this.fullName,
    required this.address,
    required this.phone,
  });

  Map<String, dynamic> toJson() => {
    'fullName': fullName,
    'address': address,
    'phone': phone,
  };
}
