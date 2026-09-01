import 'package:flutter/material.dart';
import '../models/platform_account.dart';
import '../services/youtube_auth_service.dart';
import 'youtube_connect_dialog.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final YouTubeAuthService _youtubeAuth = YouTubeAuthService();

  bool _isCheckingAuth = true;
  PlatformAccount? _youtubeAccount;

  @override
  void initState() {
    super.initState();
    _loadYouTubeAccount();
  }

  Future<void> _loadYouTubeAccount() async {
    setState(() => _isCheckingAuth = true);
    final account = await _youtubeAuth.getSavedAccount();
    if (!mounted) return;
    setState(() {
      _youtubeAccount = account;
      _isCheckingAuth = false;
    });
  }

  void _openYouTubeDialog() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => YouTubeConnectDialog(
        currentAccount: _youtubeAccount,
        onAccountChanged: (account) {
          setState(() {
            _youtubeAccount = account;
          });
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final isYouTubeConnected = _youtubeAccount != null && _youtubeAccount!.isConnected;

    return Scaffold(
      backgroundColor: const Color(0xFF090B0E),
      appBar: AppBar(
        backgroundColor: const Color(0xFF090B0E),
        elevation: 0,
        title: const Text(
          'STREAM 22 PRO',
          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18, letterSpacing: 1.0),
        ),
        centerTitle: true,
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Connect your platforms',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 6),
              const Text(
                'ربط الحسابات الرسمية عبر بروتوكولات OAuth لتجهيز البث المباشر.',
                style: TextStyle(color: Colors.white54, fontSize: 13),
              ),
              const SizedBox(height: 24),

              if (_isCheckingAuth)
                const Center(
                  child: Padding(
                    padding: EdgeInsets.all(20),
                    child: CircularProgressIndicator(color: Colors.white38),
                  ),
                )
              else
                // 1. YouTube Connection Card
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(18),
                  decoration: BoxDecoration(
                    color: const Color(0xFF13171D),
                    borderRadius: BorderRadius.circular(18),
                    border: Border.all(
                      color: isYouTubeConnected
                          ? const Color(0xFF10B981)
                          : const Color(0xFF262F3C),
                      width: isYouTubeConnected ? 1.5 : 1.0,
                    ),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.all(10),
                            decoration: const BoxDecoration(
                              color: Color(0xFFFF0000),
                              shape: BoxShape.circle,
                            ),
                            child: const Icon(Icons.play_circle_fill, color: Colors.white, size: 24),
                          ),
                          const SizedBox(width: 14),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                const Text(
                                  'YouTube',
                                  style: TextStyle(
                                    fontSize: 17,
                                    fontWeight: FontWeight.bold,
                                    color: Colors.white,
                                  ),
                                ),
                                const SizedBox(height: 3),
                                if (isYouTubeConnected)
                                  Row(
                                    children: [
                                      Container(
                                        width: 8,
                                        height: 8,
                                        decoration: const BoxDecoration(
                                          color: Color(0xFF10B981),
                                          shape: BoxShape.circle,
                                        ),
                                      ),
                                      const SizedBox(width: 6),
                                      Expanded(
                                        child: Text(
                                          '${_youtubeAccount!.displayName} • Connected ✓',
                                          style: const TextStyle(
                                            color: Color(0xFF10B981),
                                            fontSize: 13,
                                            fontWeight: FontWeight.w600,
                                          ),
                                          overflow: TextOverflow.ellipsis,
                                        ),
                                      ),
                                    ],
                                  )
                                else
                                  const Text(
                                    'Not connected',
                                    style: TextStyle(color: Colors.white54, fontSize: 13),
                                  ),
                              ],
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 18),
                      if (isYouTubeConnected)
                        SizedBox(
                          width: double.infinity,
                          height: 44,
                          child: OutlinedButton.icon(
                            onPressed: _openYouTubeDialog,
                            icon: const Icon(Icons.link_off, size: 18),
                            label: const Text('DISCONNECT / إدارة الحساب'),
                            style: OutlinedButton.styleFrom(
                              foregroundColor: const Color(0xFFFF5252),
                              side: const BorderSide(color: Color(0xFFFF5252)),
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                            ),
                          ),
                        )
                      else
                        SizedBox(
                          width: double.infinity,
                          height: 44,
                          child: FilledButton.icon(
                            onPressed: _openYouTubeDialog,
                            icon: const Icon(Icons.login, size: 18),
                            label: const Text(
                              'CONNECT',
                              style: TextStyle(fontWeight: FontWeight.bold, letterSpacing: 0.5),
                            ),
                            style: FilledButton.styleFrom(
                              backgroundColor: const Color(0xFFFF0000),
                              foregroundColor: Colors.white,
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                            ),
                          ),
                        ),
                    ],
                  ),
                ),

              const SizedBox(height: 14),

              // Placeholder for Facebook (Waiting for YouTube Done)
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: const Color(0xFF0F1318),
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: const Color(0xFF1C232E)),
                ),
                child: Row(
                  children: const [
                    Icon(Icons.facebook, color: Colors.white30, size: 24),
                    SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Facebook', style: TextStyle(color: Colors.white38, fontWeight: FontWeight.bold)),
                          Text('Not connected (Phase 2)', style: TextStyle(color: Colors.white24, fontSize: 12)),
                        ],
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 10),

              // Placeholder for Twitch (Waiting for YouTube Done)
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: const Color(0xFF0F1318),
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: const Color(0xFF1C232E)),
                ),
                child: Row(
                  children: const [
                    Icon(Icons.tv, color: Colors.white30, size: 24),
                    SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Twitch', style: TextStyle(color: Colors.white38, fontWeight: FontWeight.bold)),
                          Text('Not connected (Phase 3)', style: TextStyle(color: Colors.white24, fontSize: 12)),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
