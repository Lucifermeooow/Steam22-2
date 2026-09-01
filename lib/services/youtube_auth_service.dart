import 'dart:convert';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:http/http.dart' as http;
import '../models/platform_account.dart';
import 'token_storage_service.dart';

class YouTubeAuthResult {
  final bool isSuccess;
  final PlatformAccount? account;
  final String? errorMessage;

  const YouTubeAuthResult.success(this.account)
      : isSuccess = true,
        errorMessage = null;

  const YouTubeAuthResult.failure(this.errorMessage)
      : isSuccess = false,
        account = null;
}

class YouTubeAuthService {
  static const String youtubeScope = 'https://www.googleapis.com/auth/youtube.force-ssl';
  static const String userinfoScope = 'https://www.googleapis.com/auth/userinfo.profile';
  static const String emailScope = 'https://www.googleapis.com/auth/userinfo.email';

  static const String serverClientId = '18650771866-pj87a85tfl7vkrs0ij926n6q2ubqtdd3.apps.googleusercontent.com';

  final GoogleSignIn _googleSignIn;
  final TokenStorageService _tokenStorage;
  final http.Client _httpClient;

  YouTubeAuthService({
    GoogleSignIn? googleSignIn,
    TokenStorageService? tokenStorage,
    http.Client? httpClient,
  })  : _tokenStorage = tokenStorage ?? TokenStorageService(),
        _httpClient = httpClient ?? http.Client(),
        _googleSignIn = googleSignIn ??
            GoogleSignIn(
              serverClientId: serverClientId,
              scopes: <String>[
                emailScope,
                userinfoScope,
                youtubeScope,
              ],
            );

  Future<YouTubeAuthResult> signIn() async {
    try {
      // 1. Launch real native Google Play Services OAuth prompt
      final GoogleSignInAccount? googleUser = await _googleSignIn.signIn();
      if (googleUser == null) {
        return const YouTubeAuthResult.failure('تم إلغاء تسجيل الدخول بواسطة المستخدم');
      }

      // 2. Obtain real Google OAuth tokens
      final GoogleSignInAuthentication googleAuth = await googleUser.authentication;
      final String? accessToken = googleAuth.accessToken;

      if (accessToken == null || accessToken.isEmpty) {
        return const YouTubeAuthResult.failure('فشل استلام Access Token من Google');
      }

      // 3. Save Access Token securely using Android KeyStore
      await _tokenStorage.saveToken(
        platformId: 'youtube',
        accessToken: accessToken,
      );

      // 4. Fetch Real YouTube Channel Info from official YouTube Data API v3
      final channelInfo = await fetchChannelDetails(accessToken);
      if (channelInfo == null) {
        final fallbackAccount = PlatformAccount(
          platformId: 'youtube',
          accountId: googleUser.id,
          displayName: googleUser.displayName ?? 'Google User',
          avatarUrl: googleUser.photoUrl,
          email: googleUser.email,
          isConnected: true,
          lastValidatedAt: DateTime.now(),
        );
        return YouTubeAuthResult.success(fallbackAccount);
      }

      final account = PlatformAccount(
        platformId: 'youtube',
        accountId: channelInfo['id'] ?? googleUser.id,
        displayName: channelInfo['title'] ?? googleUser.displayName ?? 'YouTube Channel',
        avatarUrl: channelInfo['thumbnail'] ?? googleUser.photoUrl,
        email: googleUser.email,
        isConnected: true,
        lastValidatedAt: DateTime.now(),
      );

      return YouTubeAuthResult.success(account);
    } catch (e) {
      return YouTubeAuthResult.failure('خطأ أثناء تسجيل الدخول: $e');
    }
  }

  Future<Map<String, String>?> fetchChannelDetails(String accessToken) async {
    try {
      final response = await _httpClient.get(
        Uri.parse('https://www.googleapis.com/youtube/v3/channels?part=snippet&mine=true'),
        headers: {
          'Authorization': 'Bearer $accessToken',
          'Accept': 'application/json',
        },
      );

      if (response.statusCode == 200) {
        final data = json.decode(response.body) as Map<String, dynamic>;
        final items = data['items'] as List<dynamic>?;
        if (items != null && items.isNotEmpty) {
          final firstItem = items[0] as Map<String, dynamic>;
          final snippet = firstItem['snippet'] as Map<String, dynamic>?;
          final channelId = firstItem['id'] as String?;
          final title = snippet?['title'] as String?;
          final thumbnails = snippet?['thumbnails'] as Map<String, dynamic>?;
          final defaultThumb = thumbnails?['default'] as Map<String, dynamic>?;
          final thumbnailUrl = defaultThumb?['url'] as String?;

          return {
            'id': channelId ?? '',
            'title': title ?? '',
            'thumbnail': thumbnailUrl ?? '',
          };
        }
      }
      return null;
    } catch (_) {
      return null;
    }
  }

  Future<void> signOut() async {
    try {
      await _googleSignIn.signOut();
    } catch (_) {}
    await _tokenStorage.clearTokens('youtube');
  }

  Future<PlatformAccount?> getSavedAccount() async {
    final token = await _tokenStorage.getAccessToken('youtube');
    if (token == null || token.isEmpty) return null;

    final channel = await fetchChannelDetails(token);
    if (channel != null) {
      return PlatformAccount(
        platformId: 'youtube',
        accountId: channel['id'] ?? '',
        displayName: channel['title'] ?? 'YouTube Channel',
        avatarUrl: channel['thumbnail'],
        isConnected: true,
        lastValidatedAt: DateTime.now(),
      );
    }
    return null;
  }
}
