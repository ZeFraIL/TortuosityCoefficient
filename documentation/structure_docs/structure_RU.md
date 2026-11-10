# 🏗️ Структура и архитектура проекта «Коэффициент извилистости»

Руководство для преподавателя: понятное описание архитектуры проекта для школьников и начинающих разработчиков.

---

## 1. Большая картина (The Big Picture)

Приложение **«Коэффициент извилистости»** работает как умный географический измерительный инструмент: оно записывает GPS-трек движения пользователя в реальном времени, отрисовывает траекторию на кастомном холсте (Canvas) и рассчитывает отношение пройденного пути ($L_{\text{path}}$) к прямому расстоянию по воздуху ($L_{\text{direct}}$) для получения коэффициента извилистости $K = \frac{L_{\text{path}}}{L_{\text{direct}}}$. Приложение состоит из 4 главных архитектурных столпов: экранного интерфейса (Activities), фоновой службы записи (`ForegroundService`), математическо-графического движка (Math & Canvas) и слоя сохранения данных в JSON-файл.

### 🏢 Жизненная аналогия: Международный аэропорт и диспетчерская вышка
Чтобы понять, почему код разделен на отдельные пакеты (Packages), представьте **аэропорт**:
- **Главный терминал (Activities):** Место, где пассажиры (пользователи) видят информационные табло, нажимают кнопки и смотрят результаты.
- **Пилот самолета в воздухе (Foreground Service):** Летит в небе и непрерывно записывает координаты полета даже когда пассажиры в зале спали (когда экран телефона заблокирован).
- **Экран локатора (TrackCanvasView):** Специальный дисплей, который преобразует сырые координаты спутника в светящиеся точки и красиво чертит траекторию полета.
- **Архив полетных журналов (TrackStorage & JSON):** Сейф, в котором надежно хранятся прошлые полеты, чтобы их можно было открыть в любой момент.

---

## 2. Обзор папок и файлов проекта

Код распределен по пакетам в соответствии с зонами ответственности:

### 📁 `zeev.fraiman.tortuositycoefficient` (Экраны - Activities)
- **`SplashActivity`:** Стартовый экран с логотипом и названием на 4 секунды.
- **`LanguageActivity`:** Экран выбора языка (английский, иврит, русский) с таймером на 5 секунд.
- **`MainActivity`:** Главный экран управления записью GPS и живого мониторинга.
- **`AnalysisActivity`:** Экран анализа результатов с карточкой $K \pm \Delta K$ и отрисовкой прямой.
- **`ArchiveActivity`:** Экран архива сохраненных маршрутов.

### 📁 `service` (Фоновая работа)
- **`TrackRecordingService`:** Фоновая служба (`ForegroundService`), непрерывно принимающая GPS-координаты через `LocationManager`.

### 📁 `ui` (Кастомная отрисовка)
- **`TrackCanvasView`:** Кастомный элемент интерфейса, пересчитывающий координаты широты/долготы в пиксели экрана с учетом косинуса широты.

### 📁 `model` (Модели данных)
- **`TrackPoint`:** Модель одиночной GPS-точки (`latitude`, `longitude`, `timestamp`, `accuracy`).
- **`Track`:** Главная геометрическая модель, содержащая список точек и математические формулы расчета $K$ и погрешности GPS.

### 📁 `storage` (Хранение данных)
- **`TrackStorage`:** Менеджер сохранения и загрузки маршрутов из файла `saved_tracks.json`.

### 📁 `util` & `adapter` (Утилиты и адаптеры)
- **`LocaleHelper`:** Управляет языковыми настройками приложения.
- **`GpsCheckUtils`:** Проверяет статус GPS, разрешения и режим энергосбережения.
- **`TrackAdapter`:** Адаптер `RecyclerView` для отображения карточек архива.

---

## 3. Взаимосвязь компонентов (Движение данных)

Ниже представлена схема движения данных при записи маршрута:

```
[Спутник GPS] 
    │ (Засечка координат)
    ▼
[LocationManager]
    │ (onLocationChanged)
    ▼
[TrackRecordingService] ──> Создает ──> [TrackPoint] ──> Добавляет в ──> [Track]
    │
    │ (Секундный таймер / Broadcast)
    ▼
[MainActivity] ──> Запрашивает Track ──> Обновляет UI ──> Передает в ──> [TrackCanvasView]
    │
    │ (Нажатие "Анализ")
    ▼
[AnalysisActivity] ──> Показывает K ± ΔK ──> Нажатие "Сохранить" ──> [TrackStorage] ──> [saved_tracks.json]
```

### Пошаговый сценарий работы:
1. Пользователь нажимает **«Старт»** в `MainActivity`.
2. `MainActivity` запускает службу `TrackRecordingService`.
3. GPS-модуль фиксирует координаты ──> `TrackRecordingService` оборачивает их в `TrackPoint` и добавляет в `Track`.
4. Каждую секунду `MainActivity` запрашивает актуальный `Track` из службы и передает его в `TrackCanvasView.setTrack()`.
5. При остановке записи `Track` передается в `AnalysisActivity` и сохраняется через `TrackStorage` в JSON-файл.

---

## 4. Практический пример: путь GPS-точки от приема до сохранения

Проследим путь одной GPS-точки по коду приложения от начала до конца:

### Шаг 1: Прием координат в фоновой службе (`TrackRecordingService.java`)
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

### Шаг 2: Обновление экрана и перерисовка холста (`MainActivity.java`)
```java
private void updateUIFromService() {
    if (!isBound || recordingService == null) return;
    Track track = recordingService.getCurrentTrack();
    if (track != null) {
        tvDistance.setText(String.format(Locale.getDefault(), "%.1f м", track.calculatePathLength()));
        tvK.setText(String.format(Locale.getDefault(), "%.2f", track.calculateTortuosityK()));
        canvasView.setTrack(track); // Запускает перерисовку Canvas
    }
}
```

### Шаг 3: Сохранение маршрута в JSON-архив (`TrackStorage.java`)
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

## 5. Краткий словарь технических терминов

1. **Activity (Активность):** Элемент пользовательского интерфейса Android, представляющий отдельный экран приложения.
2. **Foreground Service (Фоновая служба):** Компонент, работающий в фоновом режиме с постоянным уведомлением в шторке, гарантирующий, что система Android не закроет запись GPS.
3. **Custom View / Canvas (Холст):** Пользовательская поверхность отрисовки, позволяющая программисту чертить линии и фигуры по математическим формулам.
4. **JSON:** Легкий структурированный текстовый формат для хранения и передачи данных в виде «ключ-значение».
5. **Adapter (Адаптер):** Компонент-посредник, связывающий список данных из памяти с их наглядным отображением в прокручиваемом списке (`RecyclerView`).
6. **SharedPreferences:** Локальное хранилище небольших настроек приложения (например, выбранного языка).
7. **LocationListener:** Системный слушатель Android, получающий координаты каждый раз, когда GPS-спутник фиксирует изменение позиции устройства.
