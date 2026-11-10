# 🧩 مخطط فئات UML ودليل تفصيلي — معامل التعرج

يوفر هذا المستند دليلاً سهلاً للطلاب والمطورين المبتدئين لفهم مخطط الفئات والعلاقات بين المكونات لمشروع **معامل التعرج (Tortuosity Coefficient)**.

---

## 1. مخطط الفئات (Mermaid classDiagram)

```mermaid
classDiagram
    class SplashActivity {
        -Handler handler
        -boolean isNavigated
        +onCreate(Bundle) void
        -navigateToLanguageActivity() void
    }

    class LanguageActivity {
        -CountDownTimer countDownTimer
        -boolean isNavigated
        +onCreate(Bundle) void
        -startCountdownTimer() void
        -selectLanguageAndProceed(String) void
        -proceedToMainActivity() void
    }

    class MainActivity {
        -TrackRecordingService recordingService
        -boolean isBound
        -TrackCanvasView canvasView
        +onCreate(Bundle) void
        -checkPermissionsAndStart() void
        -startTrackRecording() void
        -stopRecordingAndAnalyze() void
        -updateUIFromService() void
    }

    class AnalysisActivity {
        -Track track
        -TrackStorage storage
        -TrackCanvasView canvasView
        +onCreate(Bundle) void
        -displayAnalysisData() void
        -saveTrackAndFinish() void
    }

    class ArchiveActivity {
        -TrackStorage storage
        -TrackAdapter adapter
        +onCreate(Bundle) void
        -loadArchiveData() void
        +onTrackClick(Track) void
        +onTrackLongClick(Track) void
    }

    class TrackRecordingService {
        -LocationManager locationManager
        -Track currentTrack
        -boolean isRecording
        +onStartCommand(Intent, int, int) int
        -startRecordingTrack() void
        +onLocationChanged(Location) void
        +stopRecordingTrack() Track
    }

    class TrackCanvasView {
        -Track track
        -boolean showDirectLine
        -Paint trackPaint
        +setTrack(Track) void
        +setShowDirectLine(boolean) void
        #onDraw(Canvas) void
    }

    class Track {
        -String id
        -String title
        -List~TrackPoint~ points
        +addPoint(TrackPoint) void
        +calculatePathLength() double
        +calculateDirectDistance() double
        +calculateTortuosityK() double
        +calculateNoiseUncertainty() double
    }

    class TrackPoint {
        -double latitude
        -double longitude
        -long timestamp
        -float accuracy
        +getLatitude() double
        +getLongitude() double
    }

    class TrackStorage {
        -Context context
        +loadTracks() List~Track~
        +saveTracks(List~Track~) boolean
        +addTrack(Track) boolean
        +deleteTrack(String) boolean
    }

    class TrackAdapter {
        -List~Track~ tracks
        -OnTrackClickListener listener
        +onCreateViewHolder(ViewGroup, int) TrackViewHolder
        +onBindViewHolder(TrackViewHolder, int) void
    }

    class GpsCheckUtils {
        +isGpsEnabled(Context)$ boolean
        +isPowerSaveMode(Context)$ boolean
        +showGpsDisabledDialog(Activity)$ void
        +showPowerSaveWarningDialog(Activity, Runnable)$ void
    }

    class LocaleHelper {
        +getLanguage(Context)$ String
        +setLanguage(Context, String)$ void
        +applyLanguage(Context, String)$ void
    }

    SplashActivity --> LanguageActivity : انتقال تلقائي (4 ثوانٍ)
    LanguageActivity --> MainActivity : انتقال تلقائي أو اختيار
    MainActivity --> TrackRecordingService : تشغيل وربط الخدمة
    MainActivity o-- TrackCanvasView : يحتوي على مكون الرسم
    MainActivity --> AnalysisActivity : يمرر المسار المكتمل
    AnalysisActivity o-- TrackCanvasView : يحتوي على مكون الرسم
    AnalysisActivity ..> TrackStorage : يحفظ المسار بالأرشيف
    ArchiveActivity ..> TrackStorage : يقرأ ويحذف المسارات
    ArchiveActivity o-- TrackAdapter : يعرض قائمة المسارات
    TrackRecordingService o-- Track : يدير المسار النشط
    Track *-- TrackPoint : يحتوي قائمة نقاط الـ GPS
    MainActivity ..> GpsCheckUtils : يفحص حالة الـ GPS
    LanguageActivity ..> LocaleHelper : يحفظ لغة التطبيق
```

---

## 2. كيف تقرأ المخطط

| الرمز | المعنى |
| :--- | :--- |
| **مستطيل (Class)** | يمثل فئة واحدة. الاسم في الأعلى. |
| **`+` (Plus)** | عنصر عام (`public`) — متاح للفئات الأخرى. |
| **`-` (Minus)** | عنصر خاص (`private`) — متاح فقط داخل هذه الفئة. |
| **`$` (Dollar)** | عنصر ساكن (`static`) — يتم استدعاؤه مباشرة عبر اسم الفئة. |
| **`List~TrackPoint~`** | قائمة تحتوي حصريًا كائنات `TrackPoint`. |
| **سهم متصل `-->`** | انتقال بين الشاشات أو استدعاء مباشر. |
| **سهم منقط `..>`** | تبعية — الفئة تستخدم فئة أخرى بدون الاحتفاظ بها. |
| **معين فارغ `o--`** | تجميع — الفئة تحتوي كائنًا آخر يمكنه الوجود بشكل مستقل. |
| **معين ممتلئ `*--`** | تركيبة قوية — ملكية كاملة (مثل `Track` المحتوي على `TrackPoint`). |

---

## 3. أسئلة للاختبار الذاتي

1. **سؤال:** أي فئة ستعدلها إذا أردت تغيير معادلة حساب معامل التعرج $K$؟
   - *الجواب:* فئة `Track` (في الدالة `calculateTortuosityK`).
2. **سؤال:** ماذا يحدث إذا أزلنا السهم بين `MainActivity` و `TrackRecordingService`؟
   - *الجواب:* لن يتمكن التطبيق من بدء تسجيل الـ GPS في الخلفية ولن يتلقى التحديثات المباشرة.
3. **سؤال:** ما هي الفئة المسؤولة عن حفظ المسارات في ذاكرة الجهاز المحلية؟
   - *الجواب:* `TrackStorage` (تحفظ في ملف `saved_tracks.json`).
