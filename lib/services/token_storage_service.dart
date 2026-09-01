import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class TokenStorageService {
  final FlutterSecureStorage _storage;

  TokenStorageService({FlutterSecureStorage? storage})
      : _storage = storage ?? const FlutterSecureStorage(
          aOptions: AndroidOptions(encryptedSharedPreferences: true),
        );

  Future<void> saveToken({
    required String platformId,
    required String accessToken,
    String? refreshToken,
    DateTime? expiresAt,
  }) async {
    await _storage.write(key: '${platformId}_access_token', value: accessToken);
    if (refreshToken != null) {
      await _storage.write(key: '${platformId}_refresh_token', value: refreshToken);
    }
    if (expiresAt != null) {
      await _storage.write(
        key: '${platformId}_expires_at',
        value: expiresAt.toIso8601String(),
      );
    }
  }

  Future<String?> getAccessToken(String platformId) async {
    return await _storage.read(key: '${platformId}_access_token');
  }

  Future<String?> getRefreshToken(String platformId) async {
    return await _storage.read(key: '${platformId}_refresh_token');
  }

  Future<void> clearTokens(String platformId) async {
    await _storage.delete(key: '${platformId}_access_token');
    await _storage.delete(key: '${platformId}_refresh_token');
    await _storage.delete(key: '${platformId}_expires_at');
  }
}
