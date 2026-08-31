# Stream 22

GitHub-ready Flutter Android live-streaming client.

## What is included

- Camera + microphone permissions.
- Camera preview.
- RTMP publishing from the phone.
- 1080p-class `ResolutionPreset.high` capture with 30 FPS target and 1500 kbps video / 128 kbps audio defaults.
- Start / stop live controls.
- Camera switching.
- Microphone mute/unmute.
- Screen wakelock while live.
- Basic live statistics: bitrate, FPS and RTT when the RTMP plugin exposes them.
- YouTube / Facebook / TikTok destination switches as server-routing configuration.
- HTTPS OAuth backend buttons for `/auth/youtube/start`, `/auth/facebook/start`, `/auth/tiktok/start`.
- GitHub Actions workflow that installs Java 17, pins Flutter 3.47.2, runs format/analyze/tests, and builds a release APK.

## Important architecture

The Android app publishes **one** RTMP stream to your media server. The server is responsible for fan-out to YouTube, Facebook and TikTok. The destination switches in the app are configuration/UI only; they do not fake provider APIs.

OAuth client secrets, access tokens, refresh tokens and stream keys must stay on the backend. Do not commit them to GitHub.

## Local build (Optional)

```bash
flutter pub get
flutter analyze
flutter test
flutter build apk --release
```

## GitHub Actions Cloud Build ($0 / Free)

1. Create an empty GitHub repository.
2. Copy the contents of this folder into it.
3. Push to the `main` branch.
4. Open **Actions** → **Build Stream 22 Release APK**.
5. Download the `Stream22-release-apk` artifact after a successful build.
