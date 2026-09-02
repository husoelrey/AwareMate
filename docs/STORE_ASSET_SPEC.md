# AwareMate — App Icon, Splash Screen & Store Asset Specification

This document provides exact technical specifications, dimensions, file formats, and directory locations for all branding and store assets required for the Google Play Store (Internal Test Track & Production).

---

## 1. App Icon (Adaptive Icon — Android 8.0 API 26+)

### 1.1 In-App Structure (Implemented in `androidApp/src/main/res/`)
Android requires Adaptive Icons composed of two separate layers:
- **Foreground Layer:** `androidApp/src/main/res/drawable/ic_launcher_foreground.xml`
- **Background Layer:** `androidApp/src/main/res/drawable/ic_launcher_background.xml`
- **Launcher Config:** `androidApp/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml`

### 1.2 Dimension Rules
- **Canvas Size:** 108dp x 108dp
- **Safe Zone (Visible Area):** Inner 72dp diameter circle/squircle. (The outer 18dp on each side may be cropped or masked by OEM launchers).
- **Format:** Android Vector Drawable (`.xml`) or raster PNG across standard density buckets:
  - `mipmap-mdpi`: 48 x 48 px (108px canvas)
  - `mipmap-hdpi`: 72 x 72 px (162px canvas)
  - `mipmap-xhdpi`: 96 x 96 px (216px canvas)
  - `mipmap-xxhdpi`: 144 x 144 px (324px canvas)
  - `mipmap-xxxhdpi`: 192 x 192 px (432px canvas)

---

## 2. Android 12+ Splash Screen (`androidx.core:core-splashscreen`)

### 2.1 Technical Specs
- **Background:** `#F8FBF8` (Light) / `#111A13` (Dark)
- **Splash Icon:** 
  - Circular icon: 160dp diameter within a 240dp container.
  - Non-circular icon: 160dp width/height centered within a 288dp container.
  - Duration: Maximum 1000ms animation or static exit.

---

## 3. Google Play Store Listing Assets

When preparing the Google Play Console store listing (required before releasing to Closed/Internal Test Track), prepare the following assets:

| Asset | Dimensions | Format | Max Size | Notes |
|---|---|---|---|---|
| **High-Res App Icon** | 512 x 512 px | 32-bit PNG | 1024 KB | Square icon with no rounded corners or drop shadows (Google Play automatically adds mask and shadow). Transparent background allowed if icon has clear boundaries. |
| **Feature Graphic** | 1024 x 500 px | JPEG or 24-bit PNG | 15 MB | Landscape banner showcased at top of store listing. Keep central 800x400 safe from edges. Highlight the sprout companion and tagline ("Compassionate Awareness Companion"). |
| **Phone Screenshots** | Min 2, Max 8 per device type | PNG or JPEG (16:9 or 9:16) | 8 MB each | Min dimension 320px, max 3840px. Recommended: **1080 x 2400 px**.<br>Recommended screen flow:<br>1. Home Dashboard with Companion & Momentum<br>2. Mindful Focus Session Timer<br>3. Screen Time Analytics & Weekly Rhythm Chart<br>4. Mood Journal & Animated Radial Breathing<br>5. Curiosity-Driven Self-Discovery Prompts |
| **Tablet Screenshots** (Optional for v1.0) | 7-inch & 10-inch | PNG or JPEG (16:9) | 8 MB each | 1920 x 1200 px or 2560 x 1600 px |

---

## 4. Text Metadata for Play Console

- **App Title:** `AwareMate` (9 characters / 30 max)
- **Short Description:** `Compassionate awareness companion helping youth build healthy digital habits.` (77 characters / 80 max)
- **Full Description:** (Up to 4000 characters — see `docs/STORE_LISTING_DESCRIPTION.md` template).
- **Category:** Health & Fitness or Tools
- **Content Rating:** Target age 13+ (Everyone / PEGI 3) — No violence, no profanity, no ads, no user-to-user chat.
- **Contact Email:** `support@awaremate.org`
- **Privacy Policy URL:** `https://github.com/husoelrey/AwareMate/blob/main/docs/PRIVACY_POLICY.md`
