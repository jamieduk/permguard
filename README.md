# PermGuard - Permission Activity Monitor

Monitor which apps access sensitive permissions on your device in real-time.

## Features
- **Permission Scanning**: Scan all installed apps and their declared permissions
- **Privacy Scores**: Each app gets a privacy score (0-100) based on risky permissions
- **Access Timeline**: Historical timeline of permission access events
- **Background Monitoring**: Continuous tracking of app permission usage
- **Privacy Alerts**: Get notified about high-risk apps
- **Audit Reports**: Generate and export detailed privacy reports

## Requirements
- Android 10 (API 29) or higher
- ARM64 device

## Installation
Download the latest APK from the [Releases](https://github.com/jnetaol/permguard/releases) page.

## Building
```bash
./gradlew assembleDebug    # Debug APK
./gradlew assembleRelease  # Release APK (requires keystore)
```

## Tech Stack
- Kotlin 1.9.22
- Jetpack Compose (BOM 2024.01.00)
- Room Database 2.6.1
- Material Design 3 (Dark Theme)
- Min SDK 29, Target SDK 34

## Made By
[jnetaol.com](https://jnetaol.com)
