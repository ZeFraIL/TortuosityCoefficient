# 🧩 Диаграмма классов UML и подробное руководство — Коэффициент извилистости

Данный документ разработан для учащихся и начинающих разработчиков для понимания структуры классов, их взаимосвязей и правил чтения диаграмм UML в проекте **«Коэффициент извилистости» (Tortuosity Coefficient)**.

---

## 1. Диаграмма классов (Mermaid classDiagram)

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

    SplashActivity --> LanguageActivity : Авто-переход по задержке (4с)
    LanguageActivity --> MainActivity : Переход по таймеру или клику
    MainActivity --> TrackRecordingService : Запуск и привязка службы
    MainActivity o-- TrackCanvasView : Содержит холст отрисовки
    MainActivity --> AnalysisActivity : Передает записанный трек
    AnalysisActivity o-- TrackCanvasView : Содержит холст отрисовки
    AnalysisActivity ..> TrackStorage : Сохраняет трек в архив
    ArchiveActivity ..> TrackStorage : Загружает и удаляет треки
    ArchiveActivity o-- TrackAdapter : Отображает список треков
    TrackRecordingService o-- Track : Управляет активным треком
    Track *-- TrackPoint : Содержит список GPS-точек
    MainActivity ..> GpsCheckUtils : Проверяет статус GPS
    LanguageActivity ..> LocaleHelper : Сохраняет язык приложения
```

---

## 2. Как читать диаграмму

| Обозначение | Значение |
| :--- | :--- |
| **Прямоугольник (Class)** | Означает один класс в проекте. Имя вверху. |
| **Знак `+` (Plus)** | Публичный элемент (`public`) — доступен из других классов. |
| **Знак `-` (Minus)** | Приватный элемент (`private`) — доступен только внутри этого класса. |
| **Знак `$` (Dollar)** | Статический элемент (`static`) — вызывается напрямую через имя класса. |
| **`List~TrackPoint~`** | Дженерики (Generics) — список объектов исключительно типа `TrackPoint`. |
| **Сплошная стрелка `-->`** | Ассоциация / Переход между экранами или прямой вызов. |
| **Пунктирная стрелка `..>`** | Зависимость — класс использует статические функции утилиты. |
| **Пустой ромб `o--`** | Агрегация — класс содержит объект, который может существовать отдельно. |
| **Закрашенный ромб `*--`** | Композиция — жесткое владение (например, `Track` состоит из `TrackPoint`). |

---

## 3. Объяснение простыми словами

### 1. Главная идея
Каждый класс отвечает только за одну свою задачу (Single Responsibility Principle). В ресторане шеф-повар не разносит еду, а официант не готовит: так и в приложении экран показывает UI, служба собирает GPS, а модель считает геометрию.

### 2. Жизненная аналогия: Общественная транспортная станция
- **`SplashActivity` & `LanguageActivity`:** Входные ворота и бюро информации, где выбирают язык.
- **`MainActivity`:** Диспетчер станции, показывающий табло рейсов в реальном времени.
- **`TrackRecordingService`:** Водитель автобуса, работающий в фоновом режиме и собирающий остановки (GPS-точки).
- **`TrackCanvasView`:** Художник карт, чертящий линию маршрута.
- **`TrackStorage`:** Сейф билетов, хранящий историю поездок в JSON.

---

## 4. Вопросы для самопроверки

1. **Вопрос:** В каком классе вы измените код, если потребуется поменять формулу расчета коэффициента извилистости $K$?
   - *Ответ:* В классе `Track` (в методе `calculateTortuosityK`).
2. **Вопрос:** Что произойдет, если убрать стрелку между `MainActivity` и `TrackRecordingService`?
   - *Ответ:* Приложение не сможет запустить фоновую запись GPS и не получит обновления координат.
3. **Вопрос:** Почему стрелка между `LanguageActivity` и `LocaleHelper` пунктирная (`..>`), а не сплошная (`-->`)?
   - *Ответ:* Потому что `LanguageActivity` вызываeт статические методы `LocaleHelper` и не хранит его постоянное поле.
4. **Вопрос:** Какой класс отвечает за сохранение маршрутов в память устройства?
   - *Ответ:* `TrackStorage` (сохраняет в файл `saved_tracks.json`).
