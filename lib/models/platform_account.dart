class PlatformAccount {
  final String platformId; // "youtube"
  final String accountId;
  final String displayName;
  final String? avatarUrl;
  final String? email;
  final bool isConnected;
  final DateTime? lastValidatedAt;

  const PlatformAccount({
    required this.platformId,
    required this.accountId,
    required this.displayName,
    this.avatarUrl,
    this.email,
    this.isConnected = false,
    this.lastValidatedAt,
  });

  Map<String, dynamic> toJson() => {
    'platformId': platformId,
    'accountId': accountId,
    'displayName': displayName,
    'avatarUrl': avatarUrl,
    'email': email,
    'isConnected': isConnected,
    'lastValidatedAt': lastValidatedAt?.toIso8601String(),
  };

  factory PlatformAccount.fromJson(Map<String, dynamic> json) => PlatformAccount(
    platformId: json['platformId'] as String,
    accountId: json['accountId'] as String,
    displayName: json['displayName'] as String,
    avatarUrl: json['avatarUrl'] as String?,
    email: json['email'] as String?,
    isConnected: json['isConnected'] as bool? ?? false,
    lastValidatedAt: json['lastValidatedAt'] != null
        ? DateTime.tryParse(json['lastValidatedAt'] as String)
        : null,
  );
}
