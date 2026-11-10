# 🏗️ Project Structure & Architecture — Tortuosity Coefficient

Teacher's Guide: Structured Architecture Documentation for High School Students and Beginner Developers.

---

## 1. The Big Picture

The **Tortuosity Coefficient** application functions as an intelligent geographical measurement tool: it records a user's GPS movement track in real time, renders the curved walking path on a custom Canvas, and computes the ratio between actual traveled path distance ($L_{\text{path}}$) and direct straight-line distance ($L_{\text{direct}}$) to derive the Tortuosity Coefficient $K = \frac{L_{\text{path}}}{L_{\text{direct}}}$. The app consists of 4 main architectural pillars: User Interface screens (Activities), Background Recording Service (`ForegroundService`), Geometry & Canvas Rendering Engine (Math & Canvas), and Local JSON File Persistence.

### 🏢 Real-Life Analogy: Airport & Control Tower
To understand why the code is divided into separate packages, imagine an **International Airport**:
- **Main Terminal (Activities):** Where passengers (users) see display boards, tap buttons, and view real-time flight stats.
- **In-Flight Pilot (Foreground Service):** Flies the plane and continuously records flight coordinates in the background even when passengers are sleeping (when the screen is locked).
- **Radar Screen (TrackCanvasView):** A specialized screen converting raw satellite coordinates into illuminated points and rendering the flight path.
- **Flight Log Archive (TrackStorage & JSON):** A safe vault storing historical flight logs so they can be reopened at any time.

---

## 2. Package & File Overview

The code is organized into packages by professional responsibility:

### 📁 `zeev.fraiman.tortuositycoefficient` (Activities)
- **`SplashActivity`:** Initial landing screen displaying branding logo and app title for 4 seconds.
- **`LanguageActivity`:** Language selection screen (English, Hebrew, Russian) with a 5-second countdown timer.
- **`MainActivity`:** Primary control panel for live GPS recording and stats monitoring.
- **`AnalysisActivity`:** Results analysis screen displaying $K \pm \Delta K$ and direct line vector overlay.
- **`ArchiveActivity`:** Saved tracks archive dashboard.

### 📁 `service` (Background Execution)
- **`TrackRecordingService`:** Foreground Service continuously gathering GPS location updates in the background via `LocationManager`.

### 📁 `ui` (Custom View Rendering)
- **`TrackCanvasView`:** Custom View scaling latitude/longitude coordinates into screen pixels adjusting for latitude cosine.

### 📁 `model` (Data Models)
- **`TrackPoint`:** Represents a single GPS point (`latitude`, `longitude`, `timestamp`, `accuracy`).
- **`Track`:** Core geometry model encapsulating the GPS point list and mathematical formulas for $K$ and GPS noise uncertainty.

### 📁 `storage` (Persistence)
- **`TrackStorage`:** Handles saving and loading tracks to/from `saved_tracks.json` in internal storage.

### 📁 `util` & `adapter` (Utilities & Adapters)
- **`LocaleHelper`:** Manages app language preferences and locale switching.
- **`GpsCheckUtils`:** Validates GPS hardware status, permissions, and Battery Saver mode.
- **`TrackAdapter`:** RecyclerView adapter binding track items to card layouts.

---

## 3. Component Relationships (Data Flow)

Here is the data flow diagram during track recording:

```
[GPS Satellite] 
    │ (Location fix)
    ▼
[LocationManager]
    │ (onLocationChanged)
    ▼
[TrackRecordingService] ──> Creates ──> [TrackPoint] ──> Adds to ──> [Track]
    │
    │ (Timer ticks / Broadcasts)
    ▼
[MainActivity] ──> Fetches Track ──> Updates UI ──> Passes to ──> [TrackCanvasView]
    │
    │ (Click "Analyze")
    ▼
[AnalysisActivity] ──> Displays K ± ΔK ──> Click "Save" ──> [TrackStorage] ──> [saved_tracks.json]
```

### Execution Steps:
1. User clicks **"Start"** in `MainActivity`.
2. `MainActivity` launches `TrackRecordingService`.
3. GPS satellite delivers a location fix ──> `TrackRecordingService` wraps it into `TrackPoint` and appends to `Track`.
4. Every second, `MainActivity` fetches updated `Track` from `TrackRecordingService` and passes it to `TrackCanvasView.setTrack()`.
5. Upon stopping, `Track` is passed to `AnalysisActivity` and saved to `TrackStorage` as a JSON entry.

---

## 4. Real Scenario Code Example: GPS Point Recording to Storage

Let's trace the path of a single GPS point through the codebase from start to finish:

### Step 1: Receiving GPS Fix in Service (`TrackRecordingService.java`)
```java
@Override
public void onLocationChanged(Location location) {
    if (!isRecording || isPaused || location == null) return;

    TrackPoint point = new TrackPoint(
            location.getLatitude(),
            location.getLongitude(),
            location.getTime(),
            location.getAccuracy()
    );
    currentTrack.addPoint(point);
    broadcastUpdate();
}
```

### Step 2: Updating Dashboard and Canvas (`MainActivity.java`)
```java
private void updateUIFromService() {
    if (!isBound || recordingService == null) return;
    Track track = recordingService.getCurrentTrack();
    if (track != null) {
        tvDistance.setText(String.format(Locale.getDefault(), "%.1f м", track.calculatePathLength()));
        tvK.setText(String.format(Locale.getDefault(), "%.2f", track.calculateTortuosityK()));
        canvasView.setTrack(track); // Triggers canvas redraw
    }
}
```

### Step 3: Saving Track to JSON Archive (`TrackStorage.java`)
```java
public synchronized boolean addTrack(Track track) {
    List<Track> list = loadTracks();
    list.add(0, track);
    JSONArray jsonArray = new JSONArray();
    for (Track t : list) {
        jsonArray.put(trackToJson(t));
    }
    File file = new File(context.getFilesDir(), "saved_tracks.json");
    try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file))) {
        writer.write(jsonArray.toString(2));
    }
    return true;
}
```

---

## 5. Short Technical Glossary

1. **Activity:** Android UI component presenting a single user screen window.
2. **Foreground Service:** A background component operating with a persistent notification bar item, preventing Android from killing GPS recording.
3. **Custom View / Canvas:** A custom rendering surface allowing developers to draw lines, markers, and geometry using mathematical formulas.
4. **JSON:** A lightweight, structured text format for storing data in key-value pairs.
5. **Adapter:** A bridge component connecting a data collection in memory to scrollable UI cards (`RecyclerView`).
6. **SharedPreferences:** Local lightweight key-value storage for app settings (e.g. selected language).
7. **LocationListener:** Android framework listener receiving callbacks whenever GPS satellites fix a position update.
