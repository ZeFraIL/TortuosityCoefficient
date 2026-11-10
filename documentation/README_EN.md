# 📱 Android Application Documentation — Tortuosity Coefficient

---

### 🧾 General Information
- **Project Name:** Tortuosity Coefficient (מקדם פתלתלות / Коэффициент извилистости)
- **Author(s):** Zeev Fraiman
- **Date:** 2024
- **Language:** Java
- **Development Environment:** Android Studio
- **Android Version (minSdk / targetSdk):** minSdk 28 / targetSdk 37 / compileSdk 37

---

### 🎯 Project Purpose
- **Problem Solved:** When walking, driving, or navigating urban streets, the actual distance traveled ($L_{\text{path}}$) is usually significantly longer than the direct straight-line distance ($L_{\text{direct}}$). The application records real-time GPS tracks, renders them on a custom Canvas, calculates total distance, direct distance, and computes the **Tortuosity Coefficient** ($K = \frac{L_{\text{path}}}{L_{\text{direct}}}$) alongside a GPS noise uncertainty estimate ($\Delta K$).
- **Importance:** Allows quantitative analysis of walking routes, school paths, park trails, and road networks to understand route efficiency and curvature scientifically.
- **Target Audience:** Students, researchers, hikers, urban planners, and anyone interested in analyzing spatial movement precisely.

---

### 📌 Application Requirements

**Functional Requirements:**
1. **Splash Screen (`SplashActivity`):** Displays the app logo (`main_icon`), title, and automatically navigates after 4 seconds.
2. **Language Selection (`LanguageActivity`):** Supports English (default), Hebrew, and Russian with a 5-second countdown timer and preference persistence in `SharedPreferences`.
3. **Real-time Tracking (`MainActivity`):** Launches a Foreground Service for background GPS tracking, displays timer, point count, accumulated distance, live $K$ coefficient, and renders live track line on Canvas.
4. **Analysis Screen (`AnalysisActivity`):** Shows summary card with $K \pm \Delta K$, renders actual track (blue) overlaid with direct distance vector (dashed red), allows naming the track, and saving to archive.
5. **Archive Screen (`ArchiveActivity`):** Displays list of saved tracks in `RecyclerView`, supports detail re-inspection and long-click deletion.
6. **System & GPS Checks (`GpsCheckUtils`):** Verifies GPS status, fine location permissions (`ACCESS_FINE_LOCATION`), notification permissions (`POST_NOTIFICATIONS`), and Battery Saver warnings.

**Non-Functional Requirements:**
- **Performance:** Smooth rendering of thousands of GPS coordinates on Canvas without UI lag.
- **Usability:** Clean Material Design 3 UI, intuitive control buttons, and multi-language support.
- **Reliability:** Uninterrupted background GPS recording via `ForegroundService` even when screen is locked or app is minimized.

---

### 🧠 System Architecture
- **Chosen Approach:** Modular Event-Driven Architecture utilizing Android components: `ForegroundService` for background location updates, a custom `TrackCanvasView` for scaling and rendering, and `TrackStorage` for JSON persistence.
- **Rationale:** Ensures clean separation between background location gathering, mathematical/geometrical calculations (`Track`), UI rendering (`TrackCanvasView`), and storage.
- **Core Components:**
  - **SplashActivity & LanguageActivity:** Onboarding and localization flow.
  - **MainActivity:** Live control panel and track rendering.
  - **TrackRecordingService:** Foreground service handling GPS updates.
  - **AnalysisActivity & ArchiveActivity:** Track inspection, metrics calculation, and archive storage.
  - **TrackCanvasView:** Custom View scaling latitude/longitude to screen pixels preserving aspect ratio based on latitude cosine ($111,000 \cdot \cos(\text{lat})$).
  - **TrackStorage:** JSON file storage layer.

---

### 🧩 UML Class Diagram

```
[SplashActivity] ──> [LanguageActivity] ──> [MainActivity]
                                                 │
                     ┌───────────────────────────┼───────────────────────────┐
                     ▼                           ▼                           ▼
          [TrackRecordingService]       [TrackCanvasView]           [AnalysisActivity]
                     │                                                       │
                     ▼                                                       ▼
            [LocationManager]                                         [ArchiveActivity]
                     │                                                       │
                     └───────────────────> [Track] <─────────────────────────┘
                                           │  │
                                           │  └─> [TrackPoint]
                                           ▼
                                    [TrackStorage] (JSON File)
```

**Package Breakdown:**
- `zeev.fraiman.tortuositycoefficient.model`: Pure data objects (`Track`, `TrackPoint`).
- `zeev.fraiman.tortuositycoefficient.service`: Background GPS recording service (`TrackRecordingService`).
- `zeev.fraiman.tortuositycoefficient.storage`: JSON persistence layer (`TrackStorage`).
- `zeev.fraiman.tortuositycoefficient.ui`: Custom view components (`TrackCanvasView`).
- `zeev.fraiman.tortuositycoefficient.util`: Utility helpers (`LocaleHelper`, `GpsCheckUtils`).
- `zeev.fraiman.tortuositycoefficient.adapter`: `RecyclerView` adapter for archive list (`TrackAdapter`).

---

### 🧩 Detailed Class Descriptions

**📌 Class: SplashActivity**
- **Role:** Initial landing screen.
- **Responsibility:** Shows logo and app title for 4 seconds or skips on touch.
- **Key Methods:** `onCreate()`, `navigateToLanguageActivity()`.

**📌 Class: LanguageActivity**
- **Role:** Language selection screen (English, Hebrew, Russian).
- **Responsibility:** 5-second countdown timer. Stores selection in `SharedPreferences` and applies locale via `LocaleHelper`.
- **Key Methods:** `startCountdownTimer()`, `selectLanguageAndProceed()`, `proceedToMainActivity()`.

**📌 Class: MainActivity**
- **Role:** Primary recording dashboard.
- **Responsibility:** Checks permissions and GPS status, binds `TrackRecordingService`, updates live metrics and timer every second, renders live track.
- **Key Methods:** `checkPermissionsAndStart()`, `startTrackRecording()`, `togglePause()`, `stopRecordingAndAnalyze()`, `updateUIFromService()`.

**📌 Class: TrackRecordingService**
- **Role:** Foreground Service for background location tracking.
- **Responsibility:** Requests location updates from `LocationManager`, creates `TrackPoint` objects, updates notification, sends local broadcasts.
- **Key Methods:** `startRecordingTrack()`, `onLocationChanged()`, `stopRecordingTrack()`, `broadcastUpdate()`.

**📌 Class: AnalysisActivity**
- **Role:** Track inspection and analysis dashboard.
- **Responsibility:** Displays $K \pm \Delta K$, draws actual track and direct vector, handles title input, and saves track to archive.
- **Key Methods:** `displayAnalysisData()`, `saveTrackAndFinish()`.

**📌 Class: ArchiveActivity**
- **Role:** Saved tracks archive dashboard.
- **Responsibility:** Loads and displays saved tracks in a `RecyclerView`, opens detail view on tap, deletes on long-click.
- **Key Methods:** `loadArchiveData()`, `onTrackClick()`, `onTrackLongClick()`.

**📌 Class: TrackCanvasView**
- **Role:** Custom View for track rendering.
- **Responsibility:** Converts lat/lon coordinates into screen pixels with latitude cosine aspect ratio adjustment ($111,000 \cdot \cos(\text{lat})$), draws polyline, direct vector, and start/finish markers.
- **Key Methods:** `setTrack()`, `setShowDirectLine()`, `onDraw()`.

**📌 Class: Track**
- **Role:** Track data model and geometry math engine.
- **Responsibility:** Calculates total path length ($L_{\text{path}}$), direct distance ($L_{\text{direct}}$), tortuosity ($K = L_{\text{path}} / L_{\text{direct}}$), and GPS noise uncertainty ($\Delta K = \frac{\bar{\sigma} \sqrt{N}}{L_{\text{direct}}}$).

**📌 Class: TrackPoint**
- **Role:** Represents a single GPS point (`latitude`, `longitude`, `timestamp`, `accuracy`).

**📌 Class: TrackStorage**
- **Role:** Data layer for saving/loading tracks to `saved_tracks.json` in internal storage.

**📌 Class: GpsCheckUtils & LocaleHelper**
- **Role:** Utilities for GPS status, Battery Saver checks, permission validation, and locale management.

---

### 🔄 Workflow Diagram
1. **Startup:** `SplashActivity` displays logo for 4 seconds ──> proceeds to `LanguageActivity`.
2. **Language Selection:** 5-second timer or manual tap ──> saves preference and proceeds to `MainActivity`.
3. **Validation:** Click "Start" on `MainActivity` ──> check permissions ──> check GPS enabled ──> check Battery Saver mode.
4. **Recording:** `TrackRecordingService` runs as Foreground Service ──> receives GPS fixes ──> updates UI & Canvas live.
5. **Analysis:** Click "Analyze" ──> stops service ──> opens `AnalysisActivity` ──> computes $K \pm \Delta K$ and direct line ──> saves to JSON archive.
6. **Archive:** Open `ArchiveActivity` ──> inspect or delete saved tracks.

---

### 🎨 UI/UX Analysis
- **Design:** Uses Material Design 3, `MaterialCardView` cards with rounded corners, distinct color-coded buttons (Green for Start, Orange for Pause, Red for Stop/Analyze).
- **Usability:** High contrast Toolbar themed with dark action bar to highlight white overflow menu (3 dots) and navigation buttons.
- **User Experience:** Immediate visual feedback during recording, live counter updates, and clear visual comparison between curved path and straight vector.

---

### ⚙️ Threading & Background Processing
- **Foreground Service:** `TrackRecordingService` guarantees continuous GPS tracking even when screen is locked.
- **Handler & Looper:** Main UI thread timer updates stats and Canvas every second in `MainActivity`.
- **ANR & Leak Prevention:** Location listeners are removed in `onDestroy()`, timers cancelled, and BroadcastReceivers unregistered properly.

---

### 💾 Data Management
- **Storage Location:** Internal JSON file `saved_tracks.json` inside `context.getFilesDir()`.
- **Rationale:** Fast serialization of complex structures (lists of GPS points) without overhead of heavy database setup.
- **Data Integrity:** Safe JSON serialization with `JSONException` handling.

---

### 🔐 Security & Permissions
- **Location Permissions:** Explicit runtime request for `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION`.
- **Android 13+/14+ Compliance:** Declares `FOREGROUND_SERVICE_LOCATION` in manifest and requests `POST_NOTIFICATIONS`.
- **Privacy:** All location data remains stored locally on device without cloud transmission.

---

### 🧪 Testing
- **Build Verification:** `./gradlew assembleDebug` verifies 100% compilation success.
- **Math Verification:** Verified distance and $K$ calculations for straight lines ($K = 1.0$) and curved paths.
- **Field Testing:** Live outdoor walking tracks, stationary GPS noise tests, and UI transition verification.

---

### 🐞 Error Handling
- **Disabled GPS:** Modal dialog prompting user to open system location settings.
- **Insufficient Points:** Alert if recording stopped with <2 points, preventing empty analysis.
- **I/O Errors:** Graceful error logging for JSON file reading/writing.

---

### ⚡ Performance
- **Canvas Optimization:** Track bounding box and scaling factors computed per frame without allocating new objects inside `onDraw()`.
- **Memory Efficiency:** Direct `Path` and `Canvas` drawing ensures minimal memory footprint even for tracks with thousands of points.

---

### 🚀 Future Extensions
1. **Enclosed Polygon Area:** Gauss shoelace formula to measure park/field areas.
2. **Speed Spike Filtering:** Filter out unrealistically fast GPS jumps.
3. **GPX / KML Export:** Export tracks for viewing in Google Earth.
