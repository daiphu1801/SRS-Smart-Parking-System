class ApiResponse<T> {
  final int code;
  final String message;
  final T? data;

  const ApiResponse({required this.code, required this.message, this.data});

  bool get success => code >= 200 && code < 300 || code == 0;

  factory ApiResponse.fromJson(
    Map<String, dynamic> json, {
    T Function(Object? value)? dataParser,
  }) {
    return ApiResponse<T>(
      code: _asInt(json['code']),
      message: (json['message'] ?? '').toString(),
      data: dataParser == null ? json['data'] as T? : dataParser(json['data']),
    );
  }

  static int _asInt(Object? value) {
    if (value is int) return value;
    if (value is num) return value.toInt();
    return int.tryParse(value?.toString() ?? '') ?? 0;
  }
}
