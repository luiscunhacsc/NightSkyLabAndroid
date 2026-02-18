# NightSkyLab Android

**A real-time interactive planetarium for Android.**

> **Author:** Luís Simões da Cunha  
> **Date:** February 2026

## 🔭 Overview

**NightSkyLab** is a native Android astronomy application that displays the night sky in real-time. See exactly what stars, planets, and constellations are above you.

Explore the sky from **any location on Earth**, at **any point in time**, with intuitive touch controls.

## ✨ Features

### Sky Display
- **Real-time star positions** — Thousands of stars with accurate brightness rendering
- **Planets** — Mercury, Venus, Mars, Jupiter, and Saturn with real-time orbital positions
- **Sun & Moon** — Accurate positions with dynamic rendering
- **Constellations** — 88 official constellations with connecting lines and labels
- **3D globe projection** — Fish-eye view with curved horizon

### Time Controls
- **Real-time mode** — Sky updates continuously
- **Time travel** — Fast-forward or rewind to see how the sky changes
- **Pause** — Freeze time to study a specific moment
- **Speed control** — 1×, 10×, 100×, 1000× forward or backward

### Location
- **GPS location** — Use your current position
- **World cities** — Preset locations across all continents
- **Manual coordinates** — Enter any latitude/longitude
- **Automatic timezone** — Correct local time display with DST support

### Display Options
- **Constellation modes** — Off / Lines / Lines + Names
- **Star names** — Off / Bright stars only / All named stars
- **Coordinate grid** — Toggle altitude/azimuth grid
- **Fullscreen mode** — Hide UI for immersive viewing

---

## 🎮 Touch Controls

### Basic Gestures

| Gesture | Action |
|:--------|:-------|
| **Drag (one finger)** | Pan the view (change azimuth and altitude) |
| **Pinch** | Zoom in / out (field of view) |
| **Double-tap (on globe)** | Toggle UI overlay |
| **Double-tap (outside globe)** | Toggle UI overlay (restore hidden UI) |

### Advanced Gestures

| Gesture | Action |
|:--------|:-------|
| **Two-finger tap** | Open location selector |
| **Two-finger double-tap** | Pause / resume time |
| **Three-finger swipe up** | Speed up time |
| **Three-finger swipe down** | Slow down / reverse time |

### On-Screen Buttons

| Button | Location | Action |
|:-------|:---------|:-------|
| ⚙️ Settings | Top-right | Open options menu |
| 📍 Location | Top-right | Open location selector |
| ⏪ Rewind | Bottom | Slow down or reverse time |
| ⏸ Reset | Bottom | Reset to current time |
| ⏩ Forward | Bottom | Speed up time |

---

## ⚙️ Options Menu

Tap the **⚙️ Settings** button to access:

| Option | Description |
|:-------|:------------|
| **Constellations** | Cycle: Off → Lines → Lines + Names |
| **Star Names** | Cycle: Off → Bright → All |
| **Grid** | Toggle coordinate grid on/off |
| **Toggle UI** | Hide/show the UI overlay |
| **N / E / S / W** | Snap view to cardinal direction |
| **Select Location** | Open location dialog |
| **Exit** | Close the application |

---

## 📍 Location Selector

Access via the **📍 Location** button or two-finger tap:

- **Use GPS** — Get coordinates from device (requires permission)
- **World Cities** — Choose from major cities worldwide:
  - Europe: Lisbon, London, Paris, Berlin, Rome, Moscow
  - Americas: New York, Los Angeles, Toronto, Mexico City, São Paulo, Buenos Aires
  - Asia: Dubai, Mumbai, Beijing, Tokyo, Seoul, Singapore
  - Africa: Cairo, Cape Town, Nairobi
  - Oceania: Sydney, Auckland
  - Polar: Tromsø, Reykjavik, McMurdo Station
- **Manual Entry** — Enter custom latitude and longitude

---

## 📱 Information Display

The UI overlay shows:

| Field | Description |
|:------|:------------|
| **Date** | Current simulation date (UTC) |
| **UTC Time** | Time in UTC |
| **Local Time** | Time in location's timezone (with DST) |
| **Location** | Current location name |
| **Direction** | View direction (N/E/S/W + degrees) |
| **Time Speed** | Current simulation speed |

---

## 🚀 Building from Source

### Requirements
- Android Studio (latest)
- Android SDK 24+
- Gradle 8.2+

### Steps

```bash
# Clone repository
git clone https://github.com/luiscunhacsc/NightSkyLabAndroid.git

# Open in Android Studio and sync Gradle

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

---

## 📋 Technical Details

| Specification | Value |
|:--------------|:------|
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 34 (Android 14) |
| **Language** | Java |
| **Rendering** | Android Canvas API |
| **Location** | Google Play Services |
| **Data Format** | JSON |

### Permissions
- `ACCESS_FINE_LOCATION` — For GPS coordinates (optional)

---

## 📄 License

MIT License — Copyright (c) 2026 Luís Simões da Cunha

See [LICENSE](LICENSE) for details.

## 🔒 Privacy

This app collects **no personal data**. Location is used only locally to calculate star positions. See [PRIVACY_POLICY.md](PRIVACY_POLICY.md) for details.
