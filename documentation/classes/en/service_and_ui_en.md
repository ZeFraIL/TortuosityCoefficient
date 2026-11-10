# 📄 Detailed Documentation of Foreground Service & Canvas View — English

---

## 📌 Class: TrackRecordingService

### 1. General Information
- **Class Name:** `TrackRecordingService`
- **Type:** Service (Foreground Service)
- **Role:** Background service continuously receiving GPS location fixes from `LocationManager` even when app is minimized or screen is locked.
- **Interaction:** Controlled by `MainActivity` (Start, Pause, Resume, Stop), posts ongoing notification, and broadcasts live updates.

### 2. Class Fields

| Name | Type | Purpose | Usage Location |
|------|------|---------|----------------|
| `locationManager` | `LocationManager` | Android system GPS location provider | `startRecordingTrack` |
| `currentTrack` | `Track` | Active track object gathering GPS points | All service logic |
| `isRecording` | `boolean` | Flag indicating recording status | Conditional checks |
| `isPaused` | `boolean` | Flag indicating pause status | Pause management |

### 3. Key Methods
- `startRecordingTrack()`: Starts foreground notification, requests location updates from `GPS_PROVIDER` and `NETWORK_PROVIDER` (1000ms, 0.0m).
- `onLocationChanged(Location location)`: Triggered on every new location fix, wraps location into `TrackPoint` and appends to `currentTrack`.
- `stopRecordingTrack()`: Stops location updates, removes foreground notification, and returns completed `Track`.

### 4. 💡 Explanation in Simple Words
Think of this service as a **pocket voice recorder**: even when you turn off the screen and put your phone in your pocket, it keeps recording every step and GPS point along your route without stopping.

---

## 📌 Class: TrackCanvasView

### 1. General Information
- **Class Name:** `TrackCanvasView`
- **Type:** Custom View
- **Role:** Renders the actual curved track path, direct start-to-end vector, and start/finish markers on screen, converting geographical coordinates into screen pixels with latitude cosine aspect ratio adjustment ($111,000 \cdot \cos(\text{lat})$).
- **Interaction:** Embedded in `activity_main.xml` and `activity_analysis.xml`, receives `Track` object via `setTrack()`.

### 2. Key Methods
- `onDraw(Canvas canvas)`: Called by Android framework for rendering. Computes coordinate bounding box, scales lat/lon to screen pixels, and draws track `Path`.

### 3. 💡 Explanation in Simple Words
Like a **professional cartographer**: it takes raw satellite GPS coordinates, scales them precisely to fit your screen size, and draws the path in vibrant colors.
