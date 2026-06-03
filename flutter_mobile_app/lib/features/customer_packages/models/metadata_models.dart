class AllowedVehicleType {
  final int vehicleTypeId;
  final String vehicleTypeName;
  final int currentQuantity;
  final int maxQuantity;

  AllowedVehicleType({
    required this.vehicleTypeId,
    required this.vehicleTypeName,
    required this.currentQuantity,
    required this.maxQuantity,
  });

  factory AllowedVehicleType.fromJson(Map<String, dynamic> json) {
    return AllowedVehicleType(
      vehicleTypeId: json['vehicleTypeId'] as int,
      vehicleTypeName: json['vehicleTypeName'] as String,
      currentQuantity: json['currentQuantity'] as int,
      maxQuantity: json['maxQuantity'] as int,
    );
  }

  bool get isFull => currentQuantity >= maxQuantity;
}

class AvailablePackagePrice {
  final int packagePriceId;
  final String packagePriceName;
  final double price;
  final int durationMonths;

  AvailablePackagePrice({
    required this.packagePriceId,
    required this.packagePriceName,
    required this.price,
    required this.durationMonths,
  });

  factory AvailablePackagePrice.fromJson(Map<String, dynamic> json) {
    return AvailablePackagePrice(
      packagePriceId: json['packagePriceId'] as int,
      packagePriceName: json['packagePriceName'] as String,
      price: (json['price'] as num).toDouble(),
      durationMonths: json['durationMonths'] as int,
    );
  }
}
