import 'package:flutter/material.dart';
import '../models/platform_account.dart';
import '../services/youtube_auth_service.dart';

class YouTubeConnectDialog extends StatefulWidget {
  final PlatformAccount? currentAccount;
  final Function(PlatformAccount? account) onAccountChanged;

  const YouTubeConnectDialog({
    super.key,
    required this.currentAccount,
    required this.onAccountChanged,
  });

  @override
  State<YouTubeConnectDialog> createState() => _YouTubeConnectDialogState();
}

class _YouTubeConnectDialogState extends State<YouTubeConnectDialog> {
  final YouTubeAuthService _authService = YouTubeAuthService();
  bool _isLoading = false;
  String? _errorMessage;

  Future<void> _handleConnect() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    final result = await _authService.signIn();

    if (!mounted) return;

    setState(() {
      _isLoading = false;
    });

    if (result.isSuccess && result.account != null) {
      widget.onAccountChanged(result.account);
      Navigator.pop(context);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('تم تسجيل الدخول بحساب: ${result.account!.displayName} ✓'),
          backgroundColor: const Color(0xFF10B981),
        ),
      );
    } else {
      setState(() {
        _errorMessage = result.errorMessage ?? 'حدث خطأ أثناء تسجيل الدخول';
      });
    }
  }

  Future<void> _handleDisconnect() async {
    setState(() => _isLoading = true);
    await _authService.signOut();
    if (!mounted) return;
    setState(() => _isLoading = false);
    widget.onAccountChanged(null);
    Navigator.pop(context);
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('تم تسجيل الخروج وإلغاء الربط')),
    );
  }

  @override
  Widget build(BuildContext context) {
    final account = widget.currentAccount;
    final isConnected = account != null && account.isConnected;

    return Container(
      decoration: const BoxDecoration(
        color: Color(0xFF12161E),
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      padding: EdgeInsets.only(
        bottom: MediaQuery.of(context).viewInsets.bottom + 24,
        top: 16,
        left: 20,
        right: 20,
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 36,
            height: 4,
            decoration: BoxDecoration(
              color: Colors.white24,
              borderRadius: BorderRadius.circular(2),
            ),
          ),
          const SizedBox(height: 16),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'ربط حساب YouTube Live',
                style: TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                  fontSize: 18,
                ),
              ),
              IconButton(
                icon: const Icon(Icons.close, color: Colors.white70),
                onPressed: _isLoading ? null : () => Navigator.pop(context),
              ),
            ],
          ),
          const SizedBox(height: 20),
          Container(
            width: 72,
            height: 72,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: isConnected
                  ? const Color(0xFF10B981).withValues(alpha: 0.15)
                  : const Color(0xFFFF0000).withValues(alpha: 0.15),
              border: Border.all(
                color: isConnected ? const Color(0xFF10B981) : const Color(0xFFFF0000),
                width: 2,
              ),
            ),
            child: ClipRRect(
              borderRadius: BorderRadius.circular(36),
              child: isConnected && account.avatarUrl != null && account.avatarUrl!.isNotEmpty
                  ? Image.network(
                      account.avatarUrl!,
                      fit: BoxFit.cover,
                      errorBuilder: (_, __, ___) => const Icon(
                        Icons.check_circle,
                        color: Color(0xFF10B981),
                        size: 38,
                      ),
                    )
                  : Icon(
                      isConnected ? Icons.check_circle : Icons.play_circle_fill,
                      size: 38,
                      color: isConnected ? const Color(0xFF10B981) : const Color(0xFFFF0000),
                    ),
            ),
          ),
          const SizedBox(height: 16),
          if (isConnected) ...[
            Text(
              account.displayName,
              style: const TextStyle(
                color: Colors.white,
                fontSize: 17,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 4),
            if (account.email != null)
              Text(
                account.email!,
                style: const TextStyle(color: Colors.white54, fontSize: 13),
              ),
            const SizedBox(height: 8),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
              decoration: BoxDecoration(
                color: const Color(0xFF113824),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: const Color(0xFF10B981).withValues(alpha: 0.5)),
              ),
              child: const Text(
                'متصل ومفوض للبث ✓',
                style: TextStyle(color: Color(0xFF10B981), fontSize: 12, fontWeight: FontWeight.bold),
              ),
            ),
          ] else ...[
            const Text(
              'تسجيل الدخول بحساب Google',
              style: TextStyle(
                color: Colors.white,
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 6),
            const Text(
              'يتطلب البث المباشر الحصول على إذن YouTube Live Streaming لإنشاء وجلب مفاتيح البث تلقائياً.',
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.white60, fontSize: 13),
            ),
          ],
          if (_errorMessage != null) ...[
            const SizedBox(height: 14),
            Container(
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: const Color(0xFF331414),
                borderRadius: BorderRadius.circular(10),
                border: Border.all(color: Colors.redAccent.withValues(alpha: 0.5)),
              ),
              child: Text(
                _errorMessage!,
                textAlign: TextAlign.center,
                style: const TextStyle(color: Colors.redAccent, fontSize: 12),
              ),
            ),
          ],
          const SizedBox(height: 24),
          if (isConnected)
            SizedBox(
              width: double.infinity,
              height: 50,
              child: OutlinedButton.icon(
                onPressed: _isLoading ? null : _handleDisconnect,
                style: OutlinedButton.styleFrom(
                  foregroundColor: const Color(0xFFFF5252),
                  side: const BorderSide(color: Color(0xFFFF5252)),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                ),
                icon: const Icon(Icons.link_off, size: 20),
                label: const Text(
                  'تسجيل الخروج وإلغاء الربط',
                  style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
                ),
              ),
            )
          else
            SizedBox(
              width: double.infinity,
              height: 50,
              child: FilledButton.icon(
                onPressed: _isLoading ? null : _handleConnect,
                style: FilledButton.styleFrom(
                  backgroundColor: const Color(0xFFFF0000),
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                ),
                icon: _isLoading
                    ? const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(
                          color: Colors.white,
                          strokeWidth: 2,
                        ),
                      )
                    : const Icon(Icons.login, size: 20),
                label: Text(
                  _isLoading ? 'جاري الاتصال بـ Google...' : 'تسجيل الدخول بـ Google',
                  style: const TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
                ),
              ),
            ),
          const SizedBox(height: 12),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: const [
              Icon(Icons.security, size: 13, color: Colors.white38),
              SizedBox(width: 5),
              Text(
                'OAuth 2.0 رسمي عبر Google Play Services بدون وسيط',
                style: TextStyle(color: Colors.white38, fontSize: 11),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
