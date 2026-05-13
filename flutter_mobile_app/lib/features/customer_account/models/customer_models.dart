class Customer {
  final String id;
  final String username;
  final String fullName;
  final String identityNumber;
  final String phone;
  final String address;
  final String email;
  final String groupName;

  Customer({
    required this.id,
    required this.username,
    required this.fullName,
    required this.identityNumber,
    required this.phone,
    required this.address,
    required this.email,
    required this.groupName,
  });

  Customer copyWith({
    String? username,
    String? fullName,
    String? identityNumber,
    String? phone,
    String? address,
    String? email,
    String? groupName,
  }) {
    return Customer(
      id: id,
      username: username ?? this.username,
      fullName: fullName ?? this.fullName,
      identityNumber: identityNumber ?? this.identityNumber,
      phone: phone ?? this.phone,
      address: address ?? this.address,
      email: email ?? this.email,
      groupName: groupName ?? this.groupName,
    );
  }
}

class GroupCustomer {
  final String id;
  final String name;
  final String ownerName;
  final String ownerId;
  final List<Customer> customers;

  GroupCustomer({
    required this.id,
    required this.name,
    required this.ownerName,
    required this.ownerId,
    required this.customers,
  });

  GroupCustomer copyWith({
    String? name,
    String? ownerName,
    String? ownerId,
    List<Customer>? customers,
  }) {
    return GroupCustomer(
      id: id,
      name: name ?? this.name,
      ownerName: ownerName ?? this.ownerName,
      ownerId: ownerId ?? this.ownerId,
      customers: customers ?? this.customers,
    );
  }
}
