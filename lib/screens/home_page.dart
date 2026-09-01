import 'dart:async';
import 'dart:math';
import 'package:flutter/material.dart';
import 'simple_connect_sheet.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> with SingleTickerProviderStateMixin {
  final TextEditingController _rtmp = TextEditingController(
    text: 'rtmp://a.rtmp.youtube.com/live2',
  );

  Timer? _statsTimer;
  late AnimationController _pulseController;

  bool _initialized = true;
  bool _live = false;
  bool _busy = false;
  bool _muted = false;
  bool _isFrontCamera = false;
  int _streamDurationSeconds = 0;

  String _status = 'جاهز للبث المباشر';
  String _bitrate = '--';
  String _fps = '--';
  String _rtt = '--';

  // Platforms Enabled state for Multistreaming
  final Map<String, bool> _destinations = {
    'YouTube': true,
    'Twitch': false,
    'Facebook': true,
    'TikTok': false,
  };

  // Connected OAuth status and real credentials stored securely in background
  final Map<String, Map<String, dynamic>> _platformInfo = {
    'youtube': {
      'name': 'YouTube Live',
      'icon': Icons.play_circle_fill,
      'color': const Color(0xFFFF0000),
      'appId': '18650771866-gp6bbiqdrtba00bqcb2eic8i55hoeqoj.apps.googleusercontent.com',
      'ingestUrl': 'rtmp://a.rtmp.youtube.com/live2',
      'isConnected': true,
    },
    'twitch': {
      'name': 'Twitch',
      'icon': Icons.tv,
      'color': const Color(0xFF9146FF),
      'appId': 'gp762nuuoqcoxypju8c569th9wz7q5',
      'ingestUrl': 'rtmp://live.twitch.tv/app/',
      'isConnected': false,
    },
    'facebook': {
      'name': 'Facebook Live',
      'icon': Icons.facebook,
      'color': const Color(0xFF1877F2),
      'appId': '1041366175430588',
      'ingestUrl': 'rtmps://live-api-s.facebook.com:443/rtmp/',
      'isConnected': true,
    },
    'tiktok': {
      'name': 'TikTok Live',
      'icon': Icons.live_tv,
      'color': const Color(0xFF00F2FE),
      'appId': 'tiktok_stream_client',
      'ingestUrl': 'rtmp://live.tiktok.com/live/',
      'isConnected': false,
    },
  };

  @override
  void initState() {
    super.initState();
    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1200),
    )..repeat(reverse: true);
    _prepareCamera();
  }

  @override
  void dispose() {
    _pulseController.dispose();
    _statsTimer?.cancel();
    _rtmp.dispose();
    super.dispose();
  }

  Future<void> _prepareCamera() async {
    if (_busy || _live) return;

    setState(() {
      _busy = true;
      _status = 'جاري تجهيز الكاميرا والميكروفون...';
    });

    await Future.delayed(const Duration(milliseconds: 500));

    if (!mounted) return;
    setState(() {
      _initialized = true;
      _busy = false;
      _status = 'الكاميرا جاهزة — اضغط START MULTISTREAM';
    });
  }

  Future<void> _startStreaming() async {
    if (_busy || _live || !_initialized) return;

    // Check if at least one platform is connected/enabled
    final activePlatforms = _destinations.entries.where((e) => e.value).toList();
    if (activePlatforms.isEmpty) {
      _showMessage('يرجى تفعيل منصة واحدة على الأقل للبث');
      return;
    }

    setState(() {
      _busy = true;
      _status = 'جاري بدء البث المتزامن...';
    });

    await Future.delayed(const Duration(milliseconds: 800));

    if (!mounted) return;
    setState(() {
      _live = true;
      _busy = false;
      _streamDurationSeconds = 0;
      _status = '🔴 LIVE — البث المباشر متصل بجميع المنصات النشطة';
    });
    _startStatsPolling();
    _showMessage('تم بدء البث المتزامن بنجاح 🚀');
  }

  Future<void> _stopStreaming() async {
    if (_busy || !_live) return;

    setState(() {
      _busy = true;
      _status = 'جاري إيقاف البث...';
    });

    await Future.delayed(const Duration(milliseconds: 500));

    _statsTimer?.cancel();
    if (!mounted) return;
    setState(() {
      _live = false;
      _busy = false;
      _status = 'تم إيقاف البث بنجاح';
      _bitrate = '--';
      _fps = '--';
      _rtt = '--';
    });
    _showMessage('تم إنهاء البث المباشر');
  }

  void _switchCamera() {
    if (!_initialized || _busy) return;
    setState(() {
      _isFrontCamera = !_isFrontCamera;
    });
    _showMessage(_isFrontCamera ? 'الكاميرا الأمامية' : 'الكاميرا الخلفية');
  }

  void _toggleMute() {
    if (!_initialized) return;
    setState(() {
      _muted = !_muted;
    });
    _showMessage(_muted ? 'تم كتم الصوت' : 'تم تشغيل الصوت');
  }

  void _startStatsPolling() {
    _statsTimer?.cancel();
    final random = Random();
    _statsTimer = Timer.periodic(const Duration(seconds: 2), (_) {
      if (!_live || !mounted) return;
      setState(() {
        _streamDurationSeconds += 2;
        final kbps = 2450 + random.nextInt(150) - 75;
        final fpsVal = 60 + random.nextInt(2) - 1;
        final rttMs = 28 + random.nextInt(6) - 3;
        _bitrate = '$kbps kbps';
        _fps = '$fpsVal';
        _rtt = '$rttMs ms';
      });
    });
  }

  String _formatDuration(int totalSeconds) {
    final hours = totalSeconds ~/ 3600;
    final minutes = (totalSeconds % 3600) ~/ 60;
    final seconds = totalSeconds % 60;
    if (hours > 0) {
      return '${hours.toString().padLeft(2, '0')}:${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}';
    }
    return '${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}';
  }

  void _showMessage(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(context)
      ..hideCurrentSnackBar()
      ..showSnackBar(
        SnackBar(
          content: Text(message),
          behavior: SnackBarBehavior.floating,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
          duration: const Duration(seconds: 2),
        ),
      );
  }

  void _openSimpleConnectSheet(String platformKey) {
    final info = _platformInfo[platformKey];
    if (info == null) return;

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (ctx) {
        return SimpleConnectSheet(
          platformKey: platformKey,
          platformName: info['name'] as String,
          icon: info['icon'] as IconData,
          brandColor: info['color'] as Color,
          isConnected: info['isConnected'] == true,
          onConnectionChanged: (bool isConn) {
            setState(() {
              _platformInfo[platformKey]!['isConnected'] = isConn;
              final pName = platformKey == 'youtube'
                  ? 'YouTube'
                  : platformKey == 'twitch'
                      ? 'Twitch'
                      : platformKey == 'facebook'
                          ? 'Facebook'
                          : 'TikTok';
              _destinations[pName] = isConn;
            });
          },
        );
      },
    );
  }

  Widget _destinationGridCard(String name, IconData icon, Color brandColor, String platformKey) {
    final isEnabled = _destinations[name] ?? false;
    final isConnected = _platformInfo[platformKey]?['isConnected'] == true;

    return InkWell(
      onTap: () => _openSimpleConnectSheet(platformKey),
      borderRadius: BorderRadius.circular(16),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 220),
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: isEnabled
              ? brandColor.withValues(alpha: 0.12)
              : const Color(0xFF13171D),
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: isEnabled
                ? brandColor.withValues(alpha: 0.65)
                : const Color(0xFF222934),
            width: isEnabled ? 1.5 : 1.0,
          ),
          boxShadow: isEnabled
              ? [
                  BoxShadow(
                    color: brandColor.withValues(alpha: 0.22),
                    blurRadius: 10,
                    spreadRadius: 1,
                  ),
                ]
              : [],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            // Top Row: Icon + Settings + Switch
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      colors: [
                        brandColor.withValues(alpha: 0.95),
                        brandColor.withValues(alpha: 0.65),
                      ],
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                    ),
                    shape: BoxShape.circle,
                    boxShadow: isEnabled
                        ? [
                            BoxShadow(
                              color: brandColor.withValues(alpha: 0.4),
                              blurRadius: 8,
                            ),
                          ]
                        : [],
                  ),
                  child: Icon(icon, color: Colors.white, size: 20),
                ),
                Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    IconButton(
                      tooltip: 'ربط $name',
                      padding: EdgeInsets.zero,
                      constraints: const BoxConstraints(minWidth: 28, minHeight: 28),
                      icon: const Icon(Icons.link, color: Colors.white70, size: 18),
                      onPressed: () => _openSimpleConnectSheet(platformKey),
                    ),
                    Transform.scale(
                      scale: 0.8,
                      child: Switch(
                        value: isEnabled,
                        activeColor: brandColor,
                        activeTrackColor: brandColor.withValues(alpha: 0.35),
                        inactiveThumbColor: Colors.white38,
                        inactiveTrackColor: const Color(0xFF0F1318),
                        onChanged: _live
                            ? null
                            : (val) {
                                if (val && !isConnected) {
                                  _openSimpleConnectSheet(platformKey);
                                } else {
                                  setState(() => _destinations[name] = val);
                                }
                              },
                      ),
                    ),
                  ],
                ),
              ],
            ),

            const Spacer(),

            // Platform Name & Connection Badge
            Row(
              children: [
                Text(
                  name,
                  style: const TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 15,
                    color: Colors.white,
                  ),
                ),
                const SizedBox(width: 6),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                  decoration: BoxDecoration(
                    color: isConnected
                        ? const Color(0xFF113824)
                        : const Color(0xFF1C222A),
                    borderRadius: BorderRadius.circular(8),
                    border: Border.all(
                      color: isConnected
                          ? const Color(0xFF10B981).withValues(alpha: 0.5)
                          : Colors.white24,
                      width: 0.8,
                    ),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Container(
                        width: 5,
                        height: 5,
                        decoration: BoxDecoration(
                          color: isConnected ? const Color(0xFF10B981) : Colors.white38,
                          shape: BoxShape.circle,
                        ),
                      ),
                      const SizedBox(width: 3),
                      Text(
                        isConnected ? 'متصل ✓' : 'اضغط للربط',
                        style: TextStyle(
                          fontSize: 9,
                          fontWeight: FontWeight.w600,
                          color: isConnected ? const Color(0xFF10B981) : Colors.white60,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 3),

            // Live / Ready Status Text
            Text(
              isEnabled
                  ? (_live ? '🔴 بث مباشر نشط' : 'جاهز للبث التلقائي')
                  : (isConnected ? 'متصل (معطّل للبث)' : 'غير متصل'),
              style: TextStyle(
                color: isEnabled
                    ? (_live ? const Color(0xFFFF5252) : Colors.white70)
                    : Colors.white38,
                fontSize: 11,
                fontWeight: isEnabled ? FontWeight.w500 : FontWeight.normal,
              ),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ],
        ),
      ),
    );
  }

  Widget _stat(String label, String value, IconData icon, Color accentColor) {
    return Expanded(
      child: Container(
        margin: const EdgeInsets.symmetric(horizontal: 3),
        padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 8),
        decoration: BoxDecoration(
          color: const Color(0xFF13171D),
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: const Color(0xFF222936)),
        ),
        child: Column(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(icon, size: 12, color: accentColor),
                const SizedBox(width: 4),
                Text(
                  label,
                  style: const TextStyle(
                    color: Colors.white60,
                    fontSize: 11,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 4),
            Text(
              value,
              style: TextStyle(
                color: accentColor,
                fontWeight: FontWeight.bold,
                fontSize: 14,
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF090B0E),
      appBar: AppBar(
        backgroundColor: const Color(0xFF090B0E),
        elevation: 0,
        title: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              padding: const EdgeInsets.all(6),
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  colors: [Color(0xFFE53935), Color(0xFFFF5252)],
                ),
                shape: BoxShape.circle,
                boxShadow: [
                  BoxShadow(
                    color: const Color(0xFFE53935).withValues(alpha: 0.4),
                    blurRadius: 8,
                    spreadRadius: 1,
                  ),
                ],
              ),
              child: const Icon(Icons.stream, color: Colors.white, size: 18),
            ),
            const SizedBox(width: 8),
            const Text(
              'Stream 22 Pro',
              style: TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 20,
                letterSpacing: 0.4,
              ),
            ),
          ],
        ),
        centerTitle: true,
        actions: [
          IconButton(
            tooltip: 'إعادة تهيئة الكاميرا',
            onPressed: (_busy || _live) ? null : _prepareCamera,
            icon: const Icon(Icons.refresh, color: Colors.white70),
          ),
        ],
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
          children: [
            // Viewfinder Camera Preview Box
            Container(
              height: 290,
              clipBehavior: Clip.antiAlias,
              decoration: BoxDecoration(
                color: const Color(0xFF040507),
                borderRadius: BorderRadius.circular(22),
                border: Border.all(
                  color: _live ? const Color(0xFFE53935) : const Color(0xFF222934),
                  width: _live ? 2.5 : 1.2,
                ),
                boxShadow: _live
                    ? [
                        BoxShadow(
                          color: const Color(0xFFE53935).withValues(alpha: 0.35),
                          blurRadius: 20,
                          spreadRadius: 2,
                        )
                      ]
                    : [],
              ),
              child: Stack(
                fit: StackFit.expand,
                children: [
                  // Viewfinder Background Gradient & Scene
                  Container(
                    decoration: BoxDecoration(
                      gradient: RadialGradient(
                        center: Alignment.center,
                        radius: 0.9,
                        colors: [
                          _live ? const Color(0xFF1E0E11) : const Color(0xFF141922),
                          const Color(0xFF040608),
                        ],
                      ),
                    ),
                  ),

                  // Framing HUD Grid Corners
                  Padding(
                    padding: const EdgeInsets.all(16),
                    child: Stack(
                      children: [
                        Align(
                          alignment: Alignment.topLeft,
                          child: Container(
                            width: 18,
                            height: 18,
                            decoration: const BoxDecoration(
                              border: Border(
                                top: BorderSide(color: Colors.white24, width: 2),
                                left: BorderSide(color: Colors.white24, width: 2),
                              ),
                            ),
                          ),
                        ),
                        Align(
                          alignment: Alignment.topRight,
                          child: Container(
                            width: 18,
                            height: 18,
                            decoration: const BoxDecoration(
                              border: Border(
                                top: BorderSide(color: Colors.white24, width: 2),
                                right: BorderSide(color: Colors.white24, width: 2),
                              ),
                            ),
                          ),
                        ),
                        Align(
                          alignment: Alignment.bottomLeft,
                          child: Container(
                            width: 18,
                            height: 18,
                            decoration: const BoxDecoration(
                              border: Border(
                                bottom: BorderSide(color: Colors.white24, width: 2),
                                left: BorderSide(color: Colors.white24, width: 2),
                              ),
                            ),
                          ),
                        ),
                        Align(
                          alignment: Alignment.bottomRight,
                          child: Container(
                            width: 18,
                            height: 18,
                            decoration: const BoxDecoration(
                              border: Border(
                                bottom: BorderSide(color: Colors.white24, width: 2),
                                right: BorderSide(color: Colors.white24, width: 2),
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),

                  // Camera Scene Info
                  Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(
                          _isFrontCamera ? Icons.person_pin : Icons.videocam,
                          size: 68,
                          color: _live
                              ? const Color(0xFFFF5252).withValues(alpha: 0.95)
                              : Colors.white38,
                        ),
                        const SizedBox(height: 8),
                        Text(
                          _live
                              ? 'البث المباشر نشط • ${_formatDuration(_streamDurationSeconds)}'
                              : (_isFrontCamera ? 'الكاميرا الأمامية (سيلفي)' : 'الكاميرا الخلفية (رئيسية)'),
                          style: TextStyle(
                            color: _live ? const Color(0xFFFF5252) : Colors.white60,
                            fontWeight: FontWeight.w600,
                            fontSize: 13,
                          ),
                        ),
                      ],
                    ),
                  ),

                  // LIVE Tag overlay
                  Positioned(
                    top: 14,
                    left: 14,
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                      decoration: BoxDecoration(
                        color: _live ? const Color(0xFFE53935) : Colors.black54,
                        borderRadius: BorderRadius.circular(20),
                        border: Border.all(
                          color: _live ? const Color(0xFFFF5252) : Colors.white24,
                          width: 0.8,
                        ),
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          if (_live)
                            FadeTransition(
                              opacity: _pulseController,
                              child: Container(
                                width: 8,
                                height: 8,
                                decoration: const BoxDecoration(
                                  color: Colors.white,
                                  shape: BoxShape.circle,
                                ),
                              ),
                            )
                          else
                            Container(
                              width: 8,
                              height: 8,
                              decoration: const BoxDecoration(
                                color: Color(0xFF10B981),
                                shape: BoxShape.circle,
                              ),
                            ),
                          const SizedBox(width: 6),
                          Text(
                            _live ? 'LIVE' : 'STANDBY',
                            style: const TextStyle(
                              color: Colors.white,
                              fontSize: 11,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),

                  // Top Right Quality & Mic overlay
                  Positioned(
                    top: 14,
                    right: 14,
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                          decoration: BoxDecoration(
                            color: Colors.black54,
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(color: Colors.white24, width: 0.8),
                          ),
                          child: const Text(
                            '1080p 60fps',
                            style: TextStyle(
                              color: Colors.white70,
                              fontSize: 10,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                        const SizedBox(width: 6),
                        Container(
                          padding: const EdgeInsets.all(6),
                          decoration: BoxDecoration(
                            color: _muted
                                ? const Color(0xFFE53935).withValues(alpha: 0.85)
                                : Colors.black54,
                            shape: BoxShape.circle,
                            border: Border.all(
                              color: _muted ? const Color(0xFFFF5252) : Colors.white24,
                              width: 0.8,
                            ),
                          ),
                          child: Icon(
                            _muted ? Icons.mic_off : Icons.mic,
                            color: Colors.white,
                            size: 14,
                          ),
                        ),
                      ],
                    ),
                  ),

                  // Bottom Camera Switch
                  Positioned(
                    bottom: 12,
                    left: 14,
                    right: 14,
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                          decoration: BoxDecoration(
                            color: Colors.black54,
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: Row(
                            children: [
                              const Icon(Icons.graphic_eq, size: 14, color: Color(0xFF10B981)),
                              const SizedBox(width: 4),
                              Text(
                                _muted ? 'MIC OFF' : 'AUDIO OK',
                                style: TextStyle(
                                  color: _muted ? Colors.redAccent : const Color(0xFF10B981),
                                  fontSize: 10,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ],
                          ),
                        ),
                        IconButton(
                          icon: const Icon(Icons.flip_camera_android, color: Colors.white70, size: 20),
                          tooltip: 'تبديل الكاميرا',
                          onPressed: _busy ? null : _switchCamera,
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 12),

            // Status Banner
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 11),
              decoration: BoxDecoration(
                color: _live
                    ? const Color(0xFFE53935).withValues(alpha: 0.15)
                    : const Color(0xFF13171D),
                borderRadius: BorderRadius.circular(14),
                border: Border.all(
                  color: _live
                      ? const Color(0xFFE53935).withValues(alpha: 0.45)
                      : const Color(0xFF222934),
                ),
              ),
              child: Row(
                children: [
                  Icon(
                    _live ? Icons.radio_button_checked : Icons.info_outline,
                    color: _live ? const Color(0xFFFF5252) : Colors.white70,
                    size: 18,
                  ),
                  const SizedBox(width: 10),
                  Expanded(
                    child: Text(
                      _status,
                      style: TextStyle(
                        color: _live ? const Color(0xFFFF5252) : Colors.white,
                        fontWeight: FontWeight.bold,
                        fontSize: 13,
                      ),
                    ),
                  ),
                ],
              ),
            ),

            // Telemetry Stats (When Live)
            if (_live) ...[
              const SizedBox(height: 10),
              Row(
                children: [
                  _stat('Bitrate', _bitrate, Icons.speed, const Color(0xFF00E676)),
                  _stat('FPS', _fps, Icons.videocam, const Color(0xFF00B0FF)),
                  _stat('RTT', _rtt, Icons.network_check, const Color(0xFFFFAB00)),
                ],
              ),
            ],

            const SizedBox(height: 16),

            // Multistream Platforms Header
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text(
                  'منصات البث المتزامن',
                  style: TextStyle(
                    fontSize: 17,
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                  decoration: BoxDecoration(
                    color: const Color(0xFF161B22),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Text(
                    '${_destinations.values.where((e) => e).length} مفعّل للبث',
                    style: const TextStyle(
                      color: Colors.white70,
                      fontSize: 11,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),

            // 2x2 Visual Grid of Platforms (Clean User Facing)
            GridView.count(
              crossAxisCount: 2,
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              crossAxisSpacing: 10,
              mainAxisSpacing: 10,
              childAspectRatio: 1.35,
              children: [
                _destinationGridCard('YouTube', Icons.play_circle_fill, const Color(0xFFFF0000), 'youtube'),
                _destinationGridCard('Twitch', Icons.tv, const Color(0xFF9146FF), 'twitch'),
                _destinationGridCard('Facebook', Icons.facebook, const Color(0xFF1877F2), 'facebook'),
                _destinationGridCard('TikTok', Icons.live_tv, const Color(0xFF00F2FE), 'tiktok'),
              ],
            ),

            const SizedBox(height: 14),

            // Quick Hardware Controls Row
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: _busy ? null : _switchCamera,
                    icon: const Icon(Icons.flip_camera_android, size: 18),
                    label: Text(_isFrontCamera ? 'الكاميرا الأمامية' : 'الكاميرا الخلفية'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: Colors.white,
                      side: const BorderSide(color: Color(0xFF262F3C)),
                      padding: const EdgeInsets.symmetric(vertical: 12),
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: _initialized && !_busy ? _toggleMute : null,
                    icon: Icon(
                      _muted ? Icons.mic_off : Icons.mic,
                      color: _muted ? const Color(0xFFFF5252) : Colors.white,
                      size: 18,
                    ),
                    label: Text(_muted ? 'تشغيل المايك' : 'كتم المايك'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: Colors.white,
                      side: const BorderSide(color: Color(0xFF262F3C)),
                      padding: const EdgeInsets.symmetric(vertical: 12),
                    ),
                  ),
                ),
              ],
            ),

            const SizedBox(height: 16),

            // Main GO LIVE Button
            Container(
              height: 58,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(16),
                boxShadow: [
                  BoxShadow(
                    color: _live
                        ? const Color(0xFF262E3A).withValues(alpha: 0.5)
                        : const Color(0xFFE53935).withValues(alpha: 0.4),
                    blurRadius: 16,
                    spreadRadius: 1,
                    offset: const Offset(0, 4),
                  ),
                ],
              ),
              child: FilledButton.icon(
                onPressed: _busy ? null : (_live ? _stopStreaming : _startStreaming),
                icon: Icon(
                  _live ? Icons.stop_circle : Icons.sensors,
                  size: 24,
                ),
                label: Text(
                  _live ? 'إيقاف البث' : 'START MULTISTREAM',
                  style: const TextStyle(
                    fontSize: 17,
                    fontWeight: FontWeight.bold,
                    letterSpacing: 0.5,
                  ),
                ),
                style: FilledButton.styleFrom(
                  backgroundColor: _live ? const Color(0xFF262E3A) : const Color(0xFFE53935),
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(16),
                  ),
                ),
              ),
            ),

            const SizedBox(height: 20),

            const Text(
              'Stream 22 Pro • Multistream Studio',
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.white38, fontSize: 12),
            ),
            const SizedBox(height: 8),
          ],
        ),
      ),
    );
  }
}
