enum CheckPhoneAction { requireLoginPassword, requireOtpActivation, unknown }

class CheckPhoneResponse {
  final CheckPhoneAction action;
  final String rawAction;

  const CheckPhoneResponse({required this.action, required this.rawAction});

  factory CheckPhoneResponse.fromJson(Map<String, dynamic> json) {
    final rawAction = (json['action'] ?? '').toString();
    return CheckPhoneResponse(
      action: _parseAction(rawAction),
      rawAction: rawAction,
    );
  }

  static CheckPhoneAction _parseAction(String value) {
    switch (value) {
      case 'REQUIRE_LOGIN_PASSWORD':
        return CheckPhoneAction.requireLoginPassword;
      case 'REQUIRE_OTP_ACTIVATION':
        return CheckPhoneAction.requireOtpActivation;
      default:
        return CheckPhoneAction.unknown;
    }
  }
}

class LoginResponse {
  final String status;
  final String message;
  final String accessToken;
  final String accountType;
  final int accountId;
  final List<String> permissions;

  LoginResponse({
    required this.status,
    required this.message,
    required this.accessToken,
    required this.accountType,
    required this.accountId,
    this.permissions = const [],
  });

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    final data = _asMap(json['data']);
    return LoginResponse(
      status: (json['code'] ?? json['status'] ?? '').toString(),
      message: (json['message'] ?? '').toString(),
      accessToken: (data['access_token'] ?? '').toString(),
      accountType: (data['account_type'] ?? '').toString(),
      accountId: _asInt(data['account_id']),
      permissions: _asStringList(data['permissions']),
    );
  }

  factory LoginResponse.fromData(
    Map<String, dynamic> data, {
    int code = 200,
    String message = '',
  }) {
    return LoginResponse(
      status: code.toString(),
      message: message,
      accessToken: (data['access_token'] ?? '').toString(),
      accountType: (data['account_type'] ?? '').toString(),
      accountId: _asInt(data['account_id']),
      permissions: _asStringList(data['permissions']),
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
    final data = _asMap(json['data']);
    return UserProfile.fromData(data);
  }

  factory UserProfile.fromData(Map<String, dynamic> data) {
    return UserProfile(
      accountId: _asInt(data['account_id']),
      username: (data['username'] ?? '').toString(),
      accountType: (data['account_type'] ?? '').toString(),
      fullName: (data['full_name'] ?? '').toString(),
      phone: (data['phone'] ?? '').toString(),
      email: (data['email'] ?? '').toString(),
    );
  }

  UserProfile copyWith({String? fullName, String? phone, String? email}) {
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

Map<String, dynamic> _asMap(Object? value) {
  if (value is Map<String, dynamic>) return value;
  if (value is Map) return Map<String, dynamic>.from(value);
  return <String, dynamic>{};
}

int _asInt(Object? value) {
  if (value is int) return value;
  if (value is num) return value.toInt();
  return int.tryParse(value?.toString() ?? '') ?? 0;
}

List<String> _asStringList(Object? value) {
  if (value is List) {
    return value.map((item) => item.toString()).toList(growable: false);
  }
  return const [];
}
