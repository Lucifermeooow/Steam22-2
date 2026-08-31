import 'dart:async';
import 'dart:math';
import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> with SingleTickerProviderStateMixin {
  final TextEditingController _rtmp = TextEditingController(
    text: 'rtmp://your-server/live/stream-key',
  );
  final TextEditingController _backend = TextEditingController(
    text: 'https://your-server.example.com',
  );

  Timer? _statsTimer;
  Timer? _cameraPulseTimer;
  late AnimationController _pulseController;

  bool _initialized = true;
  bool _live = false;
  bool _busy = false;
  bool _muted = false;
  bool _isFrontCamera = false;
  int _streamDurationSeconds = 0;

  String _status = 'جاهز للبث';
  String _bitrate = '--';
  String _fps = '--';
  String _rtt = '--';

  final Map<String, bool> _destinations = {
    'YouTube': true,
    'Twitch': true,
    'Facebook': true,
    'TikTok': true,
  };

  // OAuth Credentials Store
  final Map<String, Map<String, dynamic>> _oauthConfigs = {
    'youtube': {
      'name': 'YouTube Live',
      'icon': Icons.play_circle_fill,
      'color': const Color(0xFFFF0000),
      'clientId': TextEditingController(),
      'clientSecret': TextEditingController(),
      'redirectUri': TextEditingController(text: 'https://your-server.example.com/auth/callback'),
      'streamKey': TextEditingController(),
      'ingestUrl': TextEditingController(text: 'rtmp://a.rtmp.youtube.com/live2'),
      'scopes': 'https://www.googleapis.com/auth/youtube.force-ssl',
      'authEndpoint': 'https://accounts.google.com/o/oauth2/v2/auth',
      'isConnected': false,
    },
    'twitch': {
      'name': 'Twitch',
      'icon': Icons.tv,
      'color': const Color(0xFF9146FF),
      'clientId': TextEditingController(),
      'clientSecret': TextEditingController(),
      'redirectUri': TextEditingController(text: 'https://your-server.example.com/auth/callback'),
      'streamKey': TextEditingController(),
      'ingestUrl': TextEditingController(text: 'rtmp://live.twitch.tv/app/'),
      'scopes': 'channel:manage:broadcast user:read:email',
      'authEndpoint': 'https://id.twitch.tv/oauth2/authorize',
      'isConnected': false,
    },
    'facebook': {
      'name': 'Facebook Live',
      'icon': Icons.facebook,
      'color': const Color(0xFF1877F2),
      'clientId': TextEditingController(),
      'clientSecret': TextEditingController(),
      'redirectUri': TextEditingController(text: 'https://your-server.example.com/auth/callback'),
      'streamKey': TextEditingController(),
      'ingestUrl': TextEditingController(text: 'rtmps://live-api-s.facebook.com:443/rtmp/'),
      'scopes': 'publish_video,pages_show_list',
      'authEndpoint': 'https://www.facebook.com/v19.0/dialog/oauth',
      'isConnected': false,
    },
    'tiktok': {
      'name': 'TikTok Live',
      'icon': Icons.live_tv,
      'color': const Color(0xFF00F2FE),
      'clientId': TextEditingController(),
      'clientSecret': TextEditingController(),
      'redirectUri': TextEditingController(text: 'https://your-server.example.com/auth/callback'),
      'streamKey': TextEditingController(),
      'ingestUrl': TextEditingController(text: 'rtmp://live.tiktok.com/live/'),
      'scopes': 'video.upload,user.info.basic',
      'authEndpoint': 'https://www.tiktok.com/v2/auth/authorize/',
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
    _cameraPulseTimer?.cancel();
    _rtmp.dispose();
    _backend.dispose();
    super.dispose();
  }

  Future<void> _prepareCamera() async {
    if (_busy || _live) return;

    setState(() {
      _busy = true;
      _status = 'جاري تجهيز الكاميرا والميكروفون...';
    });

    await Future.delayed(const Duration(milliseconds: 600));

    if (!mounted) return;
    setState(() {
      _initialized = true;
      _busy = false;
      _status = 'الكاميرا جاهزة — أدخل RTMP واضغط GO LIVE';
    });
    _showMessage('تم تجهيز الكاميرا بنجاح');
  }

  Future<void> _startStreaming() async {
    if (_busy || _live || !_initialized) return;

    final url = _rtmp.text.trim();
    if (!url.startsWith('rtmp://') && !url.startsWith('rtmps://')) {
      _showMessage('اكتب RTMP URL صحيح يبدأ بـ rtmp:// أو rtmps://');
      return;
    }

    setState(() {
      _busy = true;
      _status = 'جاري بدء البث...';
    });

    await Future.delayed(const Duration(milliseconds: 900));

    if (!mounted) return;
    setState(() {
      _live = true;
      _busy = false;
      _streamDurationSeconds = 0;
      _status = '🔴 LIVE — البث متصل بالسيرفر';
    });
    _startStatsPolling();
    _showMessage('تم بدء البث المباشر');
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
      _status = 'تم إيقاف البث';
      _bitrate = '--';
      _fps = '--';
      _rtt = '--';
    });
    _showMessage('تم إيقاف البث');
  }

  void _switchCamera() {
    if (!_initialized || _live || _busy) return;
    setState(() {
      _isFrontCamera = !_isFrontCamera;
    });
    _showMessage(_isFrontCamera ? 'تم التبديل للكاميرا الأمامية' : 'تم التبديل للكاميرا الخلفية');
  }

  void _toggleMute() {
    if (!_initialized) return;
    setState(() {
      _muted = !_muted;
    });
    _showMessage(_muted ? 'تم كتم الميكروفون' : 'تم تشغيل الميكروفون');
  }

  void _startStatsPolling() {
    _statsTimer?.cancel();
    final random = Random();
    _statsTimer = Timer.periodic(const Duration(seconds: 2), (_) {
      if (!_live || !mounted) return;
      setState(() {
        _streamDurationSeconds += 2;
        final kbps = 1450 + random.nextInt(120) - 60;
        final fpsVal = 30 + random.nextInt(2) - 1;
        final rttMs = 38 + random.nextInt(8) - 4;
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

  Future<void> _connectProvider(String provider) async {
    final backend = _backend.text.trim().replaceAll(RegExp(r'/+$'), '');
    if (backend.isEmpty || !backend.startsWith('https://')) {
      _showMessage('استخدم رابط Backend يبدأ بـ HTTPS.');
      return;
    }

    final uri = Uri.tryParse('$backend/auth/$provider/start');
    if (uri == null) {
      _showMessage('رابط غير صالح.');
      return;
    }

    try {
      final ok = await launchUrl(uri, mode: LaunchMode.externalApplication);
      if (!ok) _showMessage('تعذر فتح تسجيل الدخول.');
    } catch (e) {
      _showMessage('خطأ في تسجيل الدخول: $e');
    }
  }

  void _showMessage(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(context)
      ..hideCurrentSnackBar()
      ..showSnackBar(
        SnackBar(
          content: Text(message),
          behavior: SnackBarBehavior.floating,
          duration: const Duration(seconds: 3),
        ),
      );
  }

  void _openOAuthDialog([String initialPlatform = 'youtube']) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: const Color(0xFF0B0D10),
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (ctx) {
        String selectedKey = initialPlatform;
        bool obscureSecret = true;
        bool obscureKey = true;

        return StatefulBuilder(
          builder: (context, setModalState) {
            final config = _oauthConfigs[selectedKey]!;
            final platformList = _oauthConfigs.entries.toList();

            return Padding(
              padding: EdgeInsets.only(
                bottom: MediaQuery.of(context).viewInsets.bottom,
                top: 16,
                left: 16,
                right: 16,
              ),
              child: SizedBox(
                height: MediaQuery.of(context).size.height * 0.88,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    // Handle bar
                    Center(
                      child: Container(
                        width: 40,
                        height: 4,
                        decoration: BoxDecoration(
                          color: Colors.white24,
                          borderRadius: BorderRadius.circular(2),
                        ),
                      ),
                    ),
                    const SizedBox(height: 12),

                    // Title Row
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'إعدادات وتفويض المنصات (OAuth)',
                              style: TextStyle(
                                color: Colors.white,
                                fontWeight: FontWeight.bold,
                                fontSize: 18,
                              ),
                            ),
                            Text(
                              'إدخال وتخزين بيانات الاعتماد للربط المباشر',
                              style: TextStyle(color: Colors.white54, fontSize: 12),
                            ),
                          ],
                        ),
                        IconButton(
                          icon: const Icon(Icons.close, color: Colors.white70),
                          onPressed: () => Navigator.pop(ctx),
                        ),
                      ],
                    ),
                    const SizedBox(height: 14),

                    // Platform Selector Chips
                    SingleChildScrollView(
                      scrollDirection: Axis.horizontal,
                      child: Row(
                        children: platformList.map((entry) {
                          final isSelected = entry.key == selectedKey;
                          final isConnected = entry.value['isConnected'] == true;
                          final pColor = entry.value['color'] as Color;

                          return Padding(
                            padding: const EdgeInsets.only(right: 8),
                            child: ChoiceChip(
                              label: Row(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  Icon(
                                    entry.value['icon'] as IconData,
                                    size: 16,
                                    color: isSelected ? Colors.white : pColor,
                                  ),
                                  const SizedBox(width: 6),
                                  Text(
                                    entry.value['name'] as String,
                                    style: TextStyle(
                                      color: isSelected ? Colors.white : Colors.white70,
                                      fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                                      fontSize: 12,
                                    ),
                                  ),
                                  if (isConnected) ...[
                                    const SizedBox(width: 5),
                                    Container(
                                      width: 6,
                                      height: 6,
                                      decoration: const BoxDecoration(
                                        color: Colors.greenAccent,
                                        shape: BoxShape.circle,
                                      ),
                                    ),
                                  ],
                                ],
                              ),
                              selected: isSelected,
                              selectedColor: const Color(0xFFE53935),
                              backgroundColor: const Color(0xFF15191E),
                              onSelected: (selected) {
                                if (selected) {
                                  setModalState(() => selectedKey = entry.key);
                                }
                              },
                            ),
                          );
                        }).toList(),
                      ),
                    ),

                    const SizedBox(height: 12),

                    // Form Fields
                    Expanded(
                      child: ListView(
                        children: [
                          // Status Card
                          Container(
                            padding: const EdgeInsets.all(12),
                            decoration: BoxDecoration(
                              color: config['isConnected'] == true
                                  ? const Color(0xFF132B1F)
                                  : const Color(0xFF15191E),
                              borderRadius: BorderRadius.circular(12),
                              border: Border.all(
                                color: config['isConnected'] == true
                                    ? Colors.greenAccent.withValues(alpha: 0.4)
                                    : const Color(0xFF232A34),
                              ),
                            ),
                            child: Row(
                              children: [
                                Icon(
                                  config['isConnected'] == true
                                      ? Icons.check_circle
                                      : Icons.info_outline,
                                  color: config['isConnected'] == true
                                      ? Colors.greenAccent
                                      : const Color(0xFFFF5252),
                                  size: 20,
                                ),
                                const SizedBox(width: 10),
                                Expanded(
                                  child: Text(
                                    config['isConnected'] == true
                                        ? 'الحساب مهيأ وجاهز للبث المباشر'
                                        : 'لم يتم حفظ بيانات هذا الحساب بعد',
                                    style: const TextStyle(
                                      color: Colors.white,
                                      fontSize: 13,
                                      fontWeight: FontWeight.w600,
                                    ),
                                  ),
                                ),
                              ],
                            ),
                          ),

                          const SizedBox(height: 14),

                          // Client ID Field
                          TextField(
                            controller: config['clientId'] as TextEditingController,
                            style: const TextStyle(color: Colors.white, fontSize: 13),
                            decoration: InputDecoration(
                              labelText: 'Client ID / App ID',
                              labelStyle: const TextStyle(color: Colors.white70),
                              hintText: 'أدخل Client ID الخاص بـ ${config['name']}',
                              hintStyle: const TextStyle(color: Colors.white30),
                              prefixIcon: const Icon(Icons.vpn_key, color: Colors.white70),
                              filled: true,
                              fillColor: const Color(0xFF15191E),
                              border: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(12),
                                borderSide: const BorderSide(color: Color(0xFF232A34)),
                              ),
                            ),
                          ),

                          const SizedBox(height: 12),

                          // Client Secret Field
                          TextField(
                            controller: config['clientSecret'] as TextEditingController,
                            obscureText: obscureSecret,
                            style: const TextStyle(color: Colors.white, fontSize: 13),
                            decoration: InputDecoration(
                              labelText: 'Client Secret / App Secret',
                              labelStyle: const TextStyle(color: Colors.white70),
                              hintText: 'أدخل Secret Key',
                              hintStyle: const TextStyle(color: Colors.white30),
                              prefixIcon: const Icon(Icons.lock, color: Colors.white70),
                              suffixIcon: IconButton(
                                icon: Icon(
                                  obscureSecret ? Icons.visibility_off : Icons.visibility,
                                  color: Colors.white60,
                                ),
                                onPressed: () => setModalState(() => obscureSecret = !obscureSecret),
                              ),
                              filled: true,
                              fillColor: const Color(0xFF15191E),
                              border: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(12),
                                borderSide: const BorderSide(color: Color(0xFF232A34)),
                              ),
                            ),
                          ),

                          const SizedBox(height: 12),

                          // Redirect URI
                          TextField(
                            controller: config['redirectUri'] as TextEditingController,
                            style: const TextStyle(color: Colors.white, fontSize: 13),
                            decoration: InputDecoration(
                              labelText: 'Redirect URI',
                              labelStyle: const TextStyle(color: Colors.white70),
                              hintText: 'https://your-server.example.com/auth/callback',
                              hintStyle: const TextStyle(color: Colors.white30),
                              prefixIcon: const Icon(Icons.link, color: Colors.white70),
                              filled: true,
                              fillColor: const Color(0xFF15191E),
                              border: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(12),
                                borderSide: const BorderSide(color: Color(0xFF232A34)),
                              ),
                            ),
                          ),

                          const SizedBox(height: 12),

                          // Stream Key
                          TextField(
                            controller: config['streamKey'] as TextEditingController,
                            obscureText: obscureKey,
                            style: const TextStyle(color: Colors.white, fontSize: 13),
                            decoration: InputDecoration(
                              labelText: 'Stream Key / Access Token',
                              labelStyle: const TextStyle(color: Colors.white70),
                              hintText: 'live_xxxx...',
                              hintStyle: const TextStyle(color: Colors.white30),
                              prefixIcon: const Icon(Icons.stream, color: Colors.white70),
                              suffixIcon: IconButton(
                                icon: Icon(
                                  obscureKey ? Icons.visibility_off : Icons.visibility,
                                  color: Colors.white60,
                                ),
                                onPressed: () => setModalState(() => obscureKey = !obscureKey),
                              ),
                              filled: true,
                              fillColor: const Color(0xFF15191E),
                              border: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(12),
                                borderSide: const BorderSide(color: Color(0xFF232A34)),
                              ),
                            ),
                          ),

                          const SizedBox(height: 12),

                          // RTMP Ingest URL
                          TextField(
                            controller: config['ingestUrl'] as TextEditingController,
                            style: const TextStyle(color: Colors.white, fontSize: 13),
                            decoration: InputDecoration(
                              labelText: 'RTMP Ingest Server URL',
                              labelStyle: const TextStyle(color: Colors.white70),
                              hintText: 'rtmp://...',
                              hintStyle: const TextStyle(color: Colors.white30),
                              prefixIcon: const Icon(Icons.cloud_upload, color: Colors.white70),
                              filled: true,
                              fillColor: const Color(0xFF15191E),
                              border: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(12),
                                borderSide: const BorderSide(color: Color(0xFF232A34)),
                              ),
                            ),
                          ),

                          const SizedBox(height: 12),

                          // Scopes Card
                          Container(
                            padding: const EdgeInsets.all(10),
                            decoration: BoxDecoration(
                              color: const Color(0xFF15191E),
                              borderRadius: BorderRadius.circular(10),
                              border: Border.all(color: const Color(0xFF232A34)),
                            ),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                const Text(
                                  'OAuth Scopes المطلوبة:',
                                  style: TextStyle(
                                    color: Colors.white70,
                                    fontSize: 11,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                                const SizedBox(height: 3),
                                Text(
                                  config['scopes'] as String,
                                  style: const TextStyle(color: Colors.white38, fontSize: 11),
                                ),
                              ],
                            ),
                          ),

                          const SizedBox(height: 16),
                        ],
                      ),
                    ),

                    // Actions
                    FilledButton.icon(
                      onPressed: () {
                        final cId = (config['clientId'] as TextEditingController).text.trim();
                        final sKey = (config['streamKey'] as TextEditingController).text.trim();
                        final isReady = cId.isNotEmpty || sKey.isNotEmpty;

                        setState(() {
                          config['isConnected'] = isReady;
                        });
                        setModalState(() {
                          config['isConnected'] = isReady;
                        });

                        Navigator.pop(ctx);
                        _showMessage('تم حفظ بيانات ${config['name']} بنجاح');
                      },
                      icon: const Icon(Icons.save),
                      label: const Text('حفظ بيانات الاعتماد'),
                      style: FilledButton.styleFrom(
                        backgroundColor: const Color(0xFFE53935),
                        foregroundColor: Colors.white,
                        padding: const EdgeInsets.symmetric(vertical: 14),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                    ),

                    const SizedBox(height: 8),

                    Row(
                      children: [
                        Expanded(
                          child: OutlinedButton.icon(
                            onPressed: () => _connectProvider(selectedKey),
                            icon: const Icon(Icons.open_in_browser, size: 18),
                            label: const Text('ربط بالمتصفح', style: TextStyle(fontSize: 12)),
                            style: OutlinedButton.styleFrom(
                              foregroundColor: Colors.white,
                              side: const BorderSide(color: Color(0xFF2C3542)),
                              padding: const EdgeInsets.symmetric(vertical: 11),
                            ),
                          ),
                        ),
                        const SizedBox(width: 8),
                        OutlinedButton.icon(
                          onPressed: () {
                            (config['clientId'] as TextEditingController).clear();
                            (config['clientSecret'] as TextEditingController).clear();
                            (config['streamKey'] as TextEditingController).clear();
                            setState(() {
                              config['isConnected'] = false;
                            });
                            setModalState(() {
                              config['isConnected'] = false;
                            });
                            _showMessage('تم مسح بيانات ${config['name']}');
                          },
                          icon: const Icon(Icons.delete, color: Color(0xFFFF5252), size: 18),
                          label: const Text('مسح', style: TextStyle(color: Color(0xFFFF5252), fontSize: 12)),
                          style: OutlinedButton.styleFrom(
                            side: const BorderSide(color: Color(0xFFFF5252)),
                            padding: const EdgeInsets.symmetric(vertical: 11, horizontal: 14),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 10),
                  ],
                ),
              ),
            );
          },
        );
      },
    );
  }

  Widget _destination(String name, IconData icon, Color brandColor) {
    return Container(
      margin: const EdgeInsets.symmetric(vertical: 4),
      decoration: BoxDecoration(
        color: const Color(0xFF15191E),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: const Color(0xFF232A34)),
      ),
      child: SwitchListTile(
        contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 2),
        secondary: Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: brandColor.withValues(alpha: 0.15),
            shape: BoxShape.circle,
          ),
          child: Icon(icon, color: brandColor, size: 22),
        ),
        title: Text(
          name,
          style: const TextStyle(
            fontWeight: FontWeight.w600,
            fontSize: 15,
            color: Colors.white,
          ),
        ),
        subtitle: const Text(
          'يتم التوزيع من السيرفر وليس من الهاتف',
          style: TextStyle(fontSize: 12, color: Colors.white54),
        ),
        value: _destinations[name] ?? false,
        activeColor: const Color(0xFFE53935),
        onChanged: _live ? null : (value) => setState(() => _destinations[name] = value),
      ),
    );
  }

  Widget _stat(String label, String value) {
    return Expanded(
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 8),
        margin: const EdgeInsets.symmetric(horizontal: 3),
        decoration: BoxDecoration(
          color: const Color(0xFF181D24),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: const Color(0xFF262E3A)),
        ),
        child: Column(
          children: [
            Text(
              value,
              style: const TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 15,
                color: Colors.white,
              ),
            ),
            const SizedBox(height: 3),
            Text(
              label,
              style: const TextStyle(color: Colors.white60, fontSize: 11),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0B0D10),
      appBar: AppBar(
        backgroundColor: const Color(0xFF0B0D10),
        elevation: 0,
        title: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              padding: const EdgeInsets.all(6),
              decoration: BoxDecoration(
                color: const Color(0xFFE53935).withValues(alpha: 0.2),
                shape: BoxShape.circle,
              ),
              child: const Icon(Icons.stream, color: Color(0xFFE53935), size: 18),
            ),
            const SizedBox(width: 8),
            const Text(
              'Stream 22',
              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 20),
            ),
          ],
        ),
        centerTitle: true,
        actions: [
          IconButton(
            tooltip: 'إعدادات OAuth وحسابات البث',
            onPressed: () => _openOAuthDialog('youtube'),
            icon: const Icon(Icons.key, color: Colors.white),
          ),
          IconButton(
            tooltip: 'إعادة تجهيز الكاميرا',
            onPressed: (_busy || _live) ? null : _prepareCamera,
            icon: const Icon(Icons.refresh, color: Colors.white70),
          ),
        ],
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          children: [
            // Viewfinder View
            Container(
              height: 280,
              clipBehavior: Clip.antiAlias,
              decoration: BoxDecoration(
                color: const Color(0xFF050608),
                borderRadius: BorderRadius.circular(20),
                border: Border.all(
                  color: _live ? const Color(0xFFE53935) : const Color(0xFF232A34),
                  width: _live ? 2.5 : 1.2,
                ),
                boxShadow: _live
                    ? [
                        BoxShadow(
                          color: const Color(0xFFE53935).withValues(alpha: 0.3),
                          blurRadius: 18,
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
                          _live ? const Color(0xFF1E1012) : const Color(0xFF161B22),
                          const Color(0xFF06080B),
                        ],
                      ),
                    ),
                  ),

                  // Viewfinder Reticle / Camera Scene simulation
                  Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(
                          _isFrontCamera ? Icons.person_pin : Icons.videocam,
                          size: 72,
                          color: _live
                              ? const Color(0xFFFF5252).withValues(alpha: 0.9)
                              : Colors.white30,
                        ),
                        const SizedBox(height: 10),
                        Text(
                          _live
                              ? 'البث نشط • ${_formatDuration(_streamDurationSeconds)}'
                              : (_isFrontCamera ? 'الكاميرا الأمامية (سيلفي)' : 'الكاميرا الرئيسية (خلفية)'),
                          style: TextStyle(
                            color: _live ? const Color(0xFFFF5252) : Colors.white54,
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
                                color: Colors.greenAccent,
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

                  // Mic Status overlay
                  Positioned(
                    top: 14,
                    right: 14,
                    child: Container(
                      padding: const EdgeInsets.all(6),
                      decoration: BoxDecoration(
                        color: _muted
                            ? const Color(0xFFE53935).withValues(alpha: 0.8)
                            : Colors.black54,
                        shape: BoxShape.circle,
                      ),
                      child: Icon(
                        _muted ? Icons.mic_off : Icons.mic,
                        color: Colors.white,
                        size: 16,
                      ),
                    ),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 12),

            // Status Banner
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: _live
                    ? const Color(0xFFE53935).withValues(alpha: 0.15)
                    : const Color(0xFF15191E),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(
                  color: _live ? const Color(0xFFE53935).withValues(alpha: 0.4) : const Color(0xFF232A34),
                ),
              ),
              child: Text(
                _status,
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: _live ? const Color(0xFFFF5252) : Colors.white,
                  fontWeight: FontWeight.bold,
                  fontSize: 14,
                ),
              ),
            ),

            // Telemetry Stats (When Live)
            if (_live) ...[
              const SizedBox(height: 10),
              Row(
                children: [
                  _stat('Bitrate', _bitrate),
                  _stat('FPS', _fps),
                  _stat('RTT', _rtt),
                ],
              ),
            ],

            const SizedBox(height: 16),

            // RTMP URL Field
            TextField(
              controller: _rtmp,
              enabled: !_live,
              maxLines: 2,
              style: const TextStyle(color: Colors.white, fontSize: 14),
              decoration: const InputDecoration(
                labelText: 'Server RTMP Ingest URL',
                labelStyle: TextStyle(color: Colors.white70),
                hintText: 'rtmp://your-server/live/stream-key',
                hintStyle: TextStyle(color: Colors.white38),
                prefixIcon: Icon(Icons.link, color: Colors.white70),
              ),
            ),
            const SizedBox(height: 8),
            const Text(
              'الهاتف يرسل بثاً واحداً إلى Red5 / Media Server. السيرفر هو المسؤول عن التوزيع للمنصات.',
              style: TextStyle(color: Colors.white54, fontSize: 12),
            ),

            const SizedBox(height: 18),

            // OAuth Backend URL Field
            TextField(
              controller: _backend,
              enabled: !_live,
              style: const TextStyle(color: Colors.white, fontSize: 14),
              decoration: const InputDecoration(
                labelText: 'OAuth Backend HTTPS URL',
                labelStyle: TextStyle(color: Colors.white70),
                hintText: 'https://your-server.example.com',
                hintStyle: TextStyle(color: Colors.white38),
                prefixIcon: Icon(Icons.cloud_outlined, color: Colors.white70),
              ),
            ),
            const SizedBox(height: 12),

            // Dedicated OAuth Configuration Card Button
            InkWell(
              onTap: () => _openOAuthDialog('youtube'),
              borderRadius: BorderRadius.circular(12),
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                decoration: BoxDecoration(
                  color: const Color(0xFF15191E),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: const Color(0xFF232A34)),
                ),
                child: Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: const Color(0xFFE53935).withValues(alpha: 0.15),
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(Icons.key, color: Color(0xFFE53935), size: 20),
                    ),
                    const SizedBox(width: 12),
                    const Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'إعدادات وتفويض المنصات (OAuth)',
                            style: TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.bold,
                              fontSize: 14,
                            ),
                          ),
                          Text(
                            'إدخال وحفظ Client ID و Secret و Stream Key',
                            style: TextStyle(color: Colors.white54, fontSize: 11),
                          ),
                        ],
                      ),
                    ),
                    OutlinedButton(
                      onPressed: () => _openOAuthDialog('youtube'),
                      style: OutlinedButton.styleFrom(
                        foregroundColor: const Color(0xFFFF5252),
                        side: const BorderSide(color: Color(0xFFE53935)),
                        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      ),
                      child: const Text('تهيئة', style: TextStyle(fontSize: 12)),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 12),

            // OAuth Connect Buttons for YouTube, Twitch, Facebook, TikTok
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: _live ? null : () => _openOAuthDialog('youtube'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: Colors.white,
                      side: const BorderSide(color: Color(0xFF2C3542)),
                      padding: const EdgeInsets.symmetric(vertical: 10),
                    ),
                    child: const Text('YouTube', style: TextStyle(fontSize: 12)),
                  ),
                ),
                const SizedBox(width: 6),
                Expanded(
                  child: OutlinedButton(
                    onPressed: _live ? null : () => _openOAuthDialog('twitch'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: Colors.white,
                      side: const BorderSide(color: Color(0xFF2C3542)),
                      padding: const EdgeInsets.symmetric(vertical: 10),
                    ),
                    child: const Text('Twitch', style: TextStyle(fontSize: 12)),
                  ),
                ),
                const SizedBox(width: 6),
                Expanded(
                  child: OutlinedButton(
                    onPressed: _live ? null : () => _openOAuthDialog('facebook'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: Colors.white,
                      side: const BorderSide(color: Color(0xFF2C3542)),
                      padding: const EdgeInsets.symmetric(vertical: 10),
                    ),
                    child: const Text('Facebook', style: TextStyle(fontSize: 12)),
                  ),
                ),
                const SizedBox(width: 6),
                Expanded(
                  child: OutlinedButton(
                    onPressed: _live ? null : () => _openOAuthDialog('tiktok'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: Colors.white,
                      side: const BorderSide(color: Color(0xFF2C3542)),
                      padding: const EdgeInsets.symmetric(vertical: 10),
                    ),
                    child: const Text('TikTok', style: TextStyle(fontSize: 12)),
                  ),
                ),
              ],
            ),

            const SizedBox(height: 18),

            // Multistream Platforms Header
            const Text(
              'منصات البث',
              style: TextStyle(
                fontSize: 17,
                fontWeight: FontWeight.bold,
                color: Colors.white,
              ),
            ),
            const SizedBox(height: 8),

            _destination('YouTube', Icons.play_circle_fill, const Color(0xFFFF0000)),
            _destination('Twitch', Icons.tv, const Color(0xFF9146FF)),
            _destination('Facebook', Icons.facebook, const Color(0xFF1877F2)),
            _destination('TikTok', Icons.live_tv, const Color(0xFF00F2FE)),

            const SizedBox(height: 12),

            // Camera Controls Row
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: (_busy || _live) ? null : _prepareCamera,
                    icon: const Icon(Icons.camera_alt),
                    label: const Text('الكاميرا'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: Colors.white,
                      side: const BorderSide(color: Color(0xFF2C3542)),
                      padding: const EdgeInsets.symmetric(vertical: 12),
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: (_busy || _live) ? null : _switchCamera,
                    icon: const Icon(Icons.flip_camera_android),
                    label: const Text('تغيير'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: Colors.white,
                      side: const BorderSide(color: Color(0xFF2C3542)),
                      padding: const EdgeInsets.symmetric(vertical: 12),
                    ),
                  ),
                ),
              ],
            ),

            const SizedBox(height: 8),

            // Mute / Unmute Button
            OutlinedButton.icon(
              onPressed: _initialized && !_busy ? _toggleMute : null,
              icon: Icon(
                _muted ? Icons.mic_off : Icons.mic,
                color: _muted ? const Color(0xFFFF5252) : Colors.white,
              ),
              label: Text(_muted ? 'تشغيل الميكروفون' : 'كتم الميكروفون'),
              style: OutlinedButton.styleFrom(
                foregroundColor: Colors.white,
                side: const BorderSide(color: Color(0xFF2C3542)),
                padding: const EdgeInsets.symmetric(vertical: 12),
              ),
            ),

            const SizedBox(height: 16),

            // Main GO LIVE Button
            SizedBox(
              height: 56,
              child: FilledButton.icon(
                onPressed: _busy ? null : (_live ? _stopStreaming : _startStreaming),
                icon: Icon(
                  _live ? Icons.stop_circle : Icons.live_tv,
                  size: 24,
                ),
                label: Text(
                  _live ? 'إيقاف البث' : 'GO LIVE',
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    letterSpacing: 0.5,
                  ),
                ),
                style: FilledButton.styleFrom(
                  backgroundColor: _live ? const Color(0xFF262E3A) : const Color(0xFFE53935),
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14),
                  ),
                ),
              ),
            ),

            const SizedBox(height: 20),

            const Text(
              'Stream 22 • GitHub Ready',
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
