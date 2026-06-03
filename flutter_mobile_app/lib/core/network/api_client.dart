import 'dart:async';
import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:smart_parking_mobile/core/network/api_exception.dart';
import 'package:smart_parking_mobile/core/network/api_response.dart';
import 'package:smart_parking_mobile/core/utils/local_storage.dart';

class ApiClient {
  ApiClient({http.Client? httpClient, LocalStorage? storage, String? baseUrl})
    : _httpClient = httpClient ?? http.Client(),
      _storage = storage ?? LocalStorage.instance,
      _baseUrlOverride = baseUrl;

  static const _envBaseUrl = String.fromEnvironment('API_BASE_URL', defaultValue: 'http://localhost:8080');
  static const _timeout = Duration(seconds: 20);

  final http.Client _httpClient;
  final LocalStorage _storage;
  final String? _baseUrlOverride;

  Future<ApiResponse<dynamic>> get(
    String path, {
    Map<String, String>? queryParameters,
    bool authenticated = false,
  }) async {
    final uri = _buildUri(path, queryParameters: queryParameters);
    final response = await _send(
      () async => _httpClient.get(
        uri,
        headers: await _headers(authenticated: authenticated),
      ),
    );
    return _parseResponse(response);
  }

  Future<ApiResponse<dynamic>> post(
    String path, {
    Map<String, dynamic>? body,
    Map<String, dynamic>? data,
    bool authenticated = false,
  }) async {
    final requestBody = body ?? data;
    final uri = _buildUri(path);
    final response = await _send(
      () async => _httpClient.post(
        uri,
        headers: await _headers(authenticated: authenticated),
        body: requestBody == null ? null : jsonEncode(requestBody),
      ),
    );
    return _parseResponse(response);
  }

  Future<ApiResponse<dynamic>> put(
    String path, {
    Map<String, dynamic>? body,
    bool authenticated = false,
  }) async {
    final uri = _buildUri(path);
    final response = await _send(
      () async => _httpClient.put(
        uri,
        headers: await _headers(authenticated: authenticated),
        body: body == null ? null : jsonEncode(body),
      ),
    );
    return _parseResponse(response);
  }

  Future<ApiResponse<dynamic>> delete(
    String path, {
    Map<String, dynamic>? body,
    bool authenticated = false,
  }) async {
    final uri = _buildUri(path);
    final response = await _send(
      () async => _httpClient.delete(
        uri,
        headers: await _headers(authenticated: authenticated),
        body: body == null ? null : jsonEncode(body),
      ),
    );
    return _parseResponse(response);
  }

  Uri _buildUri(String path, {Map<String, String>? queryParameters}) {
    final baseUrl = (_baseUrlOverride?.trim().isNotEmpty ?? false)
        ? _baseUrlOverride!.trim()
        : _envBaseUrl.trim();

    if (baseUrl.isEmpty) {
      throw const ApiException(
        'Chua cau hinh API_BASE_URL. Hay chay app voi --dart-define=API_BASE_URL=<server-url>.',
      );
    }

    final normalizedBase = baseUrl.endsWith('/')
        ? baseUrl.substring(0, baseUrl.length - 1)
        : baseUrl;
    final normalizedPath = path.startsWith('/') ? path : '/$path';
    final uri = Uri.parse('$normalizedBase$normalizedPath');
    if (queryParameters == null || queryParameters.isEmpty) return uri;
    return uri.replace(
      queryParameters: {...uri.queryParameters, ...queryParameters},
    );
  }

  Future<Map<String, String>> _headers({required bool authenticated}) async {
    final headers = <String, String>{
      'Accept': 'application/json',
      'Content-Type': 'application/json',
    };

    if (authenticated) {
      final token = await _storage.getToken();
      if (token == null || token.isEmpty) {
        throw const ApiException(
          'Phien dang nhap da het han. Vui long dang nhap lai.',
        );
      }
      headers['Authorization'] = 'Bearer $token';
    }

    return headers;
  }

  Future<http.Response> _send(Future<http.Response> Function() request) async {
    try {
      return await request().timeout(_timeout);
    } on TimeoutException {
      throw const ApiException('Ket noi may chu qua thoi gian cho phep.');
    } on ApiException {
      rethrow;
    } catch (_) {
      throw const ApiException(
        'Khong the ket noi may chu. Vui long kiem tra server.',
      );
    }
  }

  ApiResponse<dynamic> _parseResponse(http.Response response) {
    final body = utf8.decode(response.bodyBytes);
    Map<String, dynamic>? jsonBody;

    if (body.trim().isNotEmpty) {
      try {
        final decoded = jsonDecode(body);
        if (decoded is Map<String, dynamic>) {
          jsonBody = decoded;
        }
      } catch (_) {
        jsonBody = null;
      }
    }

    if (jsonBody == null) {
      if (response.statusCode >= 200 && response.statusCode < 300) {
        return ApiResponse<dynamic>(
          code: response.statusCode,
          message: 'Thanh cong',
        );
      }
      throw ApiException(
        'May chu tra ve loi ${response.statusCode}.',
        statusCode: response.statusCode,
      );
    }

    final apiResponse = ApiResponse<dynamic>.fromJson(jsonBody);
    final apiCode = apiResponse.code == 0
        ? response.statusCode
        : apiResponse.code;
    final isHttpSuccess =
        response.statusCode >= 200 && response.statusCode < 300;
    final isApiSuccess = apiCode >= 200 && apiCode < 300;

    if (!isHttpSuccess || !isApiSuccess) {
      throw ApiException(
        apiResponse.message.isEmpty
            ? 'May chu tra ve loi $apiCode.'
            : apiResponse.message,
        statusCode: apiCode,
      );
    }

    return ApiResponse<dynamic>(
      code: apiCode,
      message: apiResponse.message,
      data: apiResponse.data,
    );
  }
}
