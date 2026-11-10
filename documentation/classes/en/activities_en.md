# 📄 Detailed Documentation of Activity Classes — English

---

## 📌 Class: SplashActivity

### 1. General Information
- **Class Name:** `SplashActivity`
- **Type:** Activity
- **Role:** Initial landing screen displaying app branding and logo (`main_icon`) upon launch, then automatically navigating to `LanguageActivity` after 4 seconds.
- **Interaction:** Set as launcher activity in manifest (`LAUNCHER`), transfers control to `LanguageActivity`.

### 2. Class Fields

| Name | Type | Purpose | Usage Location |
|------|------|---------|----------------|
| `SPLASH_DELAY_MS` | `long` | Timer delay in milliseconds (4000ms = 4 seconds) | Handler delay call |
| `handler` | `Handler` | Schedules UI thread execution | Delay and cleanup |
| `isNavigated` | `boolean` | Flag preventing double navigation | Navigation logic |
| `navigateRunnable` | `Runnable` | Runnable block executed after delay | `handler.postDelayed` |

### 3. Key Methods
- `onCreate()`: Initializes edge-to-edge UI layout, sets up screen click listener for instant skip, posts 4-second handler delay.
- `navigateToLanguageActivity()`: Cancels handler, launches `LanguageActivity` via `Intent`, and finishes `SplashActivity`.

### 4. 💡 Explanation in Simple Words
Think of this screen as a **museum welcome poster**: it shows the logo and app title for 4 seconds to create a polished first impression, then opens the door to the entrance hall (language selection screen).

---

## 📌 Class: LanguageActivity

### 1. General Information
- **Class Name:** `LanguageActivity`
- **Type:** Activity
- **Role:** Language selection screen (English, Hebrew, Russian) with a 5-second countdown timer.
- **Interaction:** Receives control from `SplashActivity`, updates `LocaleHelper`, and launches `MainActivity`.

### 2. Key Methods
- `startCountdownTimer()`: Runs a 5-second `CountDownTimer` updating UI text every second.
- `selectLanguageAndProceed(String code)`: Stores chosen language code in `SharedPreferences` via `LocaleHelper` and launches `MainActivity`.
- `proceedToMainActivity()`: Called when the 5-second timer finishes without user tap, proceeding with saved/default language.

### 3. 💡 Explanation in Simple Words
Like a **flight attendant asking which language menu you prefer**: if you tap a language button immediately, the app switches to it. If you don't choose within 5 seconds, it hands you the default English menu.

---

## 📌 Class: MainActivity

### 1. General Information
- **Class Name:** `MainActivity`
- **Type:** Activity
- **Role:** Primary live GPS recording dashboard displaying real-time metrics (time, point count, path distance, live $K$) and live track canvas.
- **Interaction:** Binds to `TrackRecordingService`, updates `TrackCanvasView`, and launches `AnalysisActivity` and `ArchiveActivity`.

### 2. Key Methods
- `checkPermissionsAndStart()`: Verifies location and notification permissions.
- `proceedWithGpsChecksAndStart()`: Checks GPS hardware status and Battery Saver mode using `GpsCheckUtils`.
- `updateUIFromService()`: Updates UI stats and Canvas every second from `TrackRecordingService`.

### 3. 💡 Explanation in Simple Words
Like a **race car dashboard**: it displays live speedometer metrics, trip timer, point count, and a live map line as you walk.

---

## 📌 Class: AnalysisActivity

### 1. General Information
- **Class Name:** `AnalysisActivity`
- **Type:** Activity
- **Role:** Track inspection and analysis dashboard.
- **Interaction:** Receives `Track` from `MainActivity`, displays $K \pm \Delta K$, draws actual track and direct vector, and saves to `TrackStorage`.

### 2. 💡 Explanation in Simple Words
Like a **lab results report**: after you finish your walk, it shows all geometrical calculations, how curved your path was compared to a straight line, and lets you name and archive the result.

---

## 📌 Class: ArchiveActivity

### 1. General Information
- **Class Name:** `ArchiveActivity`
- **Type:** Activity
- **Role:** Archive dashboard displaying saved tracks.
- **Interaction:** Loads tracks from `TrackStorage` and presents them using `TrackAdapter` in a `RecyclerView`.

### 2. 💡 Explanation in Simple Words
Like a **walking trip photo album**: lets you review all your recorded walks, reopen details, or delete old tracks.
