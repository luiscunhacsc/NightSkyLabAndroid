# NightSkyLab Android

**A high-precision, retro-styled interactive planetarium for Android.**

> **Author:** Luís Simões da Cunha  
> **Date:** January 2026

## 🔭 Overview

**NightSkyLab Android** is a native Android port of the browser-based NightSkyLab astronomy engine. It features a retro terminal aesthetic with modern, high-precision astronomy routines to compute real-time positions of the Sun, Moon, and planets, rendering a rich star field with constellation overlays.

Explore the sky from **any location on Earth**, at **any point in time**, and "time-travel" to see celestial motion—all optimized for touch-based mobile interaction.

## ✨ Key Features

- **Precision astronomy:** Real-time positions of the Sun, Moon, and planets using simplified astronomical algorithms
- **Rich star catalog:** Renders **50+ bright stars** with magnitude-based brightness and proper names
- **Star names overlay:** Toggle star labels for bright stars or all visible stars
- **3D spherical projection:** Fish-eye / globe projection with curved horizon and zoomable field of view
- **Constellation mode:** Toggle Lines / Names / Off with auto-centered constellation labels (15 major constellations)
- **Moon phases:** Dynamic phase rendering
- **Time travel engine:** Run time forward/backward with multi-speed steps, pause, and reset to "now"
- **Touch gestures:** Fully keyboard-free operation with intuitive gestures
- **Location services:** Use device GPS, preset locations, or manual coordinates
- **Retro aesthetic:** Dark "void" background with terminal-green UI for contrast

## 🎮 Touch Controls

NightSkyLab Android is optimized for touch interaction (no keyboard required):

| Gesture | Action |
| :--- | :--- |
| **Single-finger drag** | Pan (Azimuth) and Tilt (Altitude) |
| **Pinch zoom** | Zoom In / Zoom Out |
| **Double-tap** | Toggle Full Screen |
| **Two-finger tap** | Open Location Menu |
| **Three-finger swipe up** | Fast Forward Time |
| **Three-finger swipe down** | Rewind Time |
| **Two-finger double-tap** | Pause / Resume Time |
| **Long press** | Open Options Menu |

### Options Menu

Long press anywhere on the screen to access:
- Cycle Constellations (Off → Lines → Names)
- Cycle Star Names (Off → Bright → All)
- Reset Time to Now
- Select Location
- Snap View to Cardinal Directions (N/S/E/W)
- Toggle UI Overlay
- Exit

## 🚀 How to Build

**Prerequisites:**
- Android Studio (latest version)
- Android SDK 24 or higher
- Gradle 8.2+

**Steps:**

1. Clone or download the repository
2. Open the project in Android Studio
3. Wait for Gradle to sync
4. Connect an Android device or start an emulator (SDK 24+)
5. Click **Run** or use:
   ```bash
   ./gradlew installDebug
   ```

## 📱 System Requirements

- **Minimum SDK:** 24 (Android 7.0 Nougat)
- **Target SDK:** 34 (Android 14)
- **Permissions:** Location access (optional, for GPS features)

## 🛠️ Technical Stack

- **Language:** Java 8
- **Platform:** Android SDK 24-34
- **Rendering:** Android Canvas API (2D Context)
- **Astronomy:** Custom lightweight engine with simplified VSOP formulas
- **Data Format:** JSON (Gson parser)
- **Location:** Google Play Services Location API
- **UI:** Material Components with custom retro theming
- **Gestures:** ScaleGestureDetector + GestureDetector

## 📄 License

This project is released under the **MIT License**.

Copyright (c) 2026 Luís Simões da Cunha

## 🌟 Features Comparison

| Feature | Web Version | Android Version |
|---------|-------------|----------------|
| Stars | 5,000+ | 50+ bright stars |
| Planets | Sun, Moon, 5 planets | Sun, Moon, 5 planets |
| Constellations | 88 with lines/names | 15 major with lines/names |
| Controls | Keyboard | Touch gestures |
| Time Travel | ✓ | ✓ |
| Location | GPS + Presets | GPS + Presets |
| Offline | After initial load | Fully offline |

## 🎨 Design Philosophy

The Android version maintains the retro terminal aesthetic of the original web version:
- Void black background (#06060C)
- Terminal green text (#00AAAA)
- Semi-transparent dark panels
- Monospace fonts
- Minimalist, functional UI

## 🔮 Future Enhancements

- Expand star catalog to 1000+ stars
- Add all 88 constellations
- Nebulae and deep sky objects
- Telescope control integration
- Photo mode with celestial object identification
- Augmented reality mode
- Custom observation lists
- Night mode (red theme for dark adaptation)
