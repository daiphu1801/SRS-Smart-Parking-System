class ChildAccount {
  final String id;
  final String bookingId;
  final String fullName;
  final String address;
  final String phone;

  ChildAccount({
    required this.id,
    required this.bookingId,
    required this.fullName,
    this.address = '',
    required this.phone,
  });

  ChildAccount copyWith({
    String? id,
    String? bookingId,
    String? fullName,
    String? address,
    String? phone,
  }) {
    return ChildAccount(
      id: id ?? this.id,
      bookingId: bookingId ?? this.bookingId,
      fullName: fullName ?? this.fullName,
      address: address ?? this.address,
      phone: phone ?? this.phone,
    );
  }
}
