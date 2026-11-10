# 📄 Detailed Documentation of Data Models & Utilities — English

---

## 📌 Class: Track

### 1. General Information
- **Class Name:** `Track`
- **Type:** Model Class
- **Role:** Core data object encapsulating GPS point list and mathematical algorithms for total path length ($L_{\text{path}}$), direct distance ($L_{\text{direct}}$), tortuosity ($K = L_{\text{path}} / L_{\text{direct}}$), and GPS noise uncertainty ($\Delta K$).

### 2. Key Mathematical Methods
- `calculatePathLength()`: Sums distances between consecutive points ($L_{\text{path}}$) via `Location.distanceBetween`.
- `calculateDirectDistance()`: Calculates straight-line distance between start and finish points ($L_{\text{direct}}$).
- `calculateTortuosityK()`: Computes ratio $K = L_{\text{path}} / L_{\text{direct}}$.
- `calculateNoiseUncertainty()`: Estimates GPS noise uncertainty: $\Delta K = \frac{\bar{\sigma} \sqrt{N}}{L_{\text{direct}}}$.

### 3. 💡 Explanation in Simple Words
Like a **geometrical route calculator**: it holds the full chain of GPS coordinates and calculates total distance, direct distance, and route curvature.

---

## 📌 Class: TrackPoint

### 1. General Information
- **Class Name:** `TrackPoint`
- **Type:** Model Class
- **Role:** Data model representing a single GPS point (`latitude`, `longitude`, `timestamp`, `accuracy`).

### 2. 💡 Explanation in Simple Words
Like a **pushpin on a map**: marks exactly where you were, at what time, and satellite accuracy at that moment.

---

## 📌 Class: TrackStorage

### 1. General Information
- **Class Name:** `TrackStorage`
- **Type:** Storage Manager
- **Role:** Handles saving and loading tracks to/from internal JSON file (`saved_tracks.json`).

### 2. 💡 Explanation in Simple Words
Like a **track safe box**: stores all your saved routes in a structured text file (JSON) so they remain safe even if you restart your phone.

---

## 📌 Class: TrackAdapter

### 1. General Information
- **Class Name:** `TrackAdapter`
- **Type:** Adapter (`RecyclerView.Adapter`)
- **Role:** Binds `Track` list items to `item_track.xml` views in `ArchiveActivity`.

### 2. 💡 Explanation in Simple Words
Like an **exhibition organizer**: takes recorded tracks from storage and neatly arranges them into card items on screen.

---

## 📌 Class: GpsCheckUtils & LocaleHelper

### 1. General Information
- **`GpsCheckUtils`:** Validates GPS status, fine location permissions, and Battery Saver mode, displaying warning dialogs when issues are found.
- **`LocaleHelper`:** Manages language preference persistence in `SharedPreferences` and applies locales via `AppCompatDelegate`.

### 2. 💡 Explanation in Simple Words
Like **pre-flight safety inspectors**: ensuring GPS is turned on, battery saver won't interrupt background tracking, and app labels match your chosen language.
