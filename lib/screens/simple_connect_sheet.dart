import 'package:flutter/material.dart';

class SimpleConnectSheet extends StatefulWidget {
  final String platformKey;
  final String platformName;
  final IconData icon;
  final Color brandColor;
  final bool isConnected;
  final Function(bool isConnected) onConnectionChanged;

  const SimpleConnectSheet({
    super.key,
    required this.platformKey,
    required this.platformName,
    required this.icon,
    required this.brandColor,
    required this.isConnected,
    required this.onConnectionChanged,
  });

  @override
  State<SimpleConnectSheet> createState() => _SimpleConnectSheetState();
}

class _SimpleConnectSheetState extends State<SimpleConnectSheet> {
  bool _isLoading = false;

  Future<void> _handleConnect() async {
    setState(() => _isLoading = true);
    // Simulate real OAuth flow response
    await Future.delayed(const Duration(milliseconds: 700));
    if (!mounted) return;
    setState(() => _isLoading = false);
    widget.onConnectionChanged(true);
    Navigator.pop(context);
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('تم ربط حساب ${widget.platformName} بنجاح ✓'),
        backgroundColor: const Color(0xFF10B981),
      ),
    );
  }

  void _handleDisconnect() {
    widget.onConnectionChanged(false);
    Navigator.pop(context);
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('تم إلغاء ربط حساب ${widget.platformName}'),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
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
          // Drag Handle
          Container(
            width: 36,
            height: 4,
            decoration: BoxDecoration(
              color: Colors.white24,
              borderRadius: BorderRadius.circular(2),
            ),
          ),
          const SizedBox(height: 16),

          // Header
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'ربط حساب ${widget.platformName}',
                style: const TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                  fontSize: 18,
                ),
              ),
              IconButton(
                icon: const Icon(Icons.close, color: Colors.white70),
                onPressed: () => Navigator.pop(context),
              ),
            ],
          ),
          const SizedBox(height: 20),

          // Platform Icon Avatar
          Container(
            width: 72,
            height: 72,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: widget.isConnected
                  ? const Color(0xFF10B981).withValues(alpha: 0.15)
                  : widget.brandColor.withValues(alpha: 0.15),
              border: Border.all(
                color: widget.isConnected
                    ? const Color(0xFF10B981)
                    : widget.brandColor,
                width: 2,
              ),
            ),
            child: Icon(
              widget.isConnected ? Icons.check_circle : widget.icon,
              size: 38,
              color: widget.isConnected
                  ? const Color(0xFF10B981)
                  : widget.brandColor,
            ),
          ),
          const SizedBox(height: 16),

          // Status and Info
          if (widget.isConnected) ...[
            const Text(
              'الحساب متصل وجاهز للبث',
              style: TextStyle(
                color: Color(0xFF10B981),
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 6),
            const Text(
              'التطبيق يقوم بإعداد البث المباشر ومفتاح السيرفر تلقائياً.',
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.white60, fontSize: 13),
            ),
          ] else ...[
            const Text(
              'تسجيل الدخول السريع',
              style: TextStyle(
                color: Colors.white,
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              'اضغط على زر تسجيل الدخول للموافقة على الربط وسيقوم التطبيق بتهيئة البث تلقائياً.',
              textAlign: TextAlign.center,
              style: const TextStyle(color: Colors.white60, fontSize: 13),
            ),
          ],

          const SizedBox(height: 28),

          // Main Action Button
          if (widget.isConnected)
            SizedBox(
              width: double.infinity,
              height: 50,
              child: OutlinedButton.icon(
                onPressed: _handleDisconnect,
                style: OutlinedButton.styleFrom(
                  foregroundColor: const Color(0xFFFF5252),
                  side: const BorderSide(color: Color(0xFFFF5252)),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14),
                  ),
                ),
                icon: const Icon(Icons.link_off, size: 20),
                label: const Text(
                  'إلغاء الربط',
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
                  backgroundColor: widget.brandColor,
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14),
                  ),
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
                  _isLoading ? 'جاري الاتصال...' : 'تسجيل الدخول والمتابعة',
                  style: const TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ),

          const SizedBox(height: 14),

          // Secure Note
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: const [
              Icon(Icons.lock, size: 13, color: Colors.white38),
              SizedBox(width: 5),
              Text(
                'اتصال آمن ومشفر عبر OAuth الرسمي بدون وسيط',
                style: TextStyle(color: Colors.white38, fontSize: 11),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
