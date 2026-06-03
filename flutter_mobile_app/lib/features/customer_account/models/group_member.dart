class GroupMember {
  final int id;
  final String fullName;
  final String phone;
  final String address;
  final String? groupName;
  final String? createdAt;

  GroupMember({
    required this.id,
    required this.fullName,
    required this.phone,
    required this.address,
    this.groupName,
    this.createdAt,
  });

  factory GroupMember.fromJson(Map<String, dynamic> json) {
    return GroupMember(
      id: json['id'] as int,
      fullName: json['fullName'] as String? ?? '',
      phone: json['phone'] as String? ?? '',
      address: json['address'] as String? ?? '',
      groupName: json['groupName'] as String?,
      createdAt: json['createdAt'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'fullName': fullName,
      'phone': phone,
      'address': address,
      if (groupName != null) 'groupName': groupName,
      if (createdAt != null) 'createdAt': createdAt,
    };
  }

  GroupMember copyWith({
    int? id,
    String? fullName,
    String? phone,
    String? address,
    String? groupName,
    String? createdAt,
  }) {
    return GroupMember(
      id: id ?? this.id,
      fullName: fullName ?? this.fullName,
      phone: phone ?? this.phone,
      address: address ?? this.address,
      groupName: groupName ?? this.groupName,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}
