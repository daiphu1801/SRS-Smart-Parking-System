class LoginResponse {
  final String status;
  final String message;
  final String accessToken;
  final String accountType;
  final int accountId;

  LoginResponse({
    required this.status,
    required this.message,
    required this.accessToken,
    required this.accountType,
    required this.accountId,
  });

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    final data = json['data'] ?? {};
    return LoginResponse(
      status: json['status'] ?? '',
      message: json['message'] ?? '',
      accessToken: data['access_token'] ?? '',
      accountType: data['account_type'] ?? '',
      accountId: data['account_id'] ?? 0,
    );
  }
}

class UserProfile {
  final int accountId;
  final String username;
  final String accountType;
  final String fullName;
  final String phone;
  final String email;

  UserProfile({
    required this.accountId,
    required this.username,
    required this.accountType,
    required this.fullName,
    required this.phone,
    required this.email,
  });

  factory UserProfile.fromJson(Map<String, dynamic> json) {
    final data = json['data'] ?? {};
    return UserProfile(
      accountId: data['account_id'] ?? 0,
      username: data['username'] ?? '',
      accountType: data['account_type'] ?? '',
      fullName: data['full_name'] ?? '',
      phone: data['phone'] ?? '',
      email: data['email'] ?? '',
    );
  }

  UserProfile copyWith({
    String? fullName,
    String? phone,
    String? email,
  }) {
    return UserProfile(
      accountId: accountId,
      username: username,
      accountType: accountType,
      fullName: fullName ?? this.fullName,
      phone: phone ?? this.phone,
      email: email ?? this.email,
    );
  }
}
