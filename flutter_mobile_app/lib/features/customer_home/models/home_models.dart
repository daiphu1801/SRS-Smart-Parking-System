/// Data model for a customer's vehicle with package info.
class VehicleInfo {
  final String plate;
  final String packageName;
  final int daysLeft;
  final bool isExpired;

  const VehicleInfo({
    required this.plate,
    required this.packageName,
    required this.daysLeft,
    this.isExpired = false,
  });
}

/// Data model for an active parking session.
class ActiveSession {
  final String plate;
  final String enteredAt;
  final String duration;
  final String estimatedFee;

  const ActiveSession({
    required this.plate,
    required this.enteredAt,
    required this.duration,
    required this.estimatedFee,
  });
}
