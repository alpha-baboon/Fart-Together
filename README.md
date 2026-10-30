# Raft Rumble — Android prototype

This is an early playable prototype scaffold for a Bluetooth raft party game.

## What is included
- Landscape Android game screen with a simple raft, water, islands and touch propulsion.
- Bluetooth Classic RFCOMM host/join foundation.
- GitHub Actions workflow that builds a debug APK.

## Important prototype limitations
- This is an early prototype, not a finished commercial-quality game.
- Bluetooth pairing must be done first in Android Settings. The current join flow is a simple paired-device demo and needs a device-selection screen for general use.
- Host/client state synchronization and multi-player input need further testing and refinement; the current code is a starting point.
- The game is currently a simple 2D prototype, not the full 3D Fart Together-style experience.

## Build the APK using GitHub Actions
1. Create a GitHub repository and upload all files in this folder.
2. Push to the `main` branch (or open Actions and run **Build Android APK** manually).
3. Open the workflow run and download the `Raft-Rumble-debug-apk` artifact.
4. Extract `app-debug.apk` and install it on your Android phone. You may need to allow installation from that source.

## Local build
Open the folder in Android Studio, allow Gradle sync, then choose Build > Build Bundle(s) / APK(s) > Build APK(s).

Package: `com.example.raftrumble`
