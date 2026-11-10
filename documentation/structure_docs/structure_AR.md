# 🏗️ الهيكل والهندسة المعمارية للمشروع — معامل التعرج (Tortuosity Coefficient)

دليل المعلم: توثيق بكتبي موجه لطلاب المدارس الثانوية والمطورين المبتدئين.

---

## 1. الصورة الكبيرة

يعمل تطبيق **"معامل التعرج"** كأداة قياس جغرافية ذكية: يسجل مسار الـ GPS للمستخدم في الوقت الفعلي، ويرسم مسار المشي المنحني على قماش مخصص (Canvas)، ويحسب النسبة بين مسافة الطريق الفعلية ($L_{\text{path}}$) والمسافة المباشرة بخط مستقيم ($L_{\text{direct}}$) للحصول على معامل التعرج $K = \frac{L_{\text{path}}}{L_{\text{direct}}}$. يتكون التطبيق من 4 أركان رئيسية: الشاشات (Activities)، وخدمة التسجيل في الخلفية (`ForegroundService`)، ومحرك الحسابات والرسم (Math & Canvas)، وطبقة حفظ البيانات في ملف JSON.

### 🏢 تشبيه من الحياة الواقعية: المطار وبرج المراقبة
لفهم سبب تقسيم الكود إلى حزم (Packages) منفصلة، تخيل **مطارات دولياً**:
- **الصالة الرئيسية (Activities):** حيث يرى المسافرون (المستخدمون) الشاشات، ويضغطون الأزرار، ويرون البيانات الحية.
- **الطيار في الجو (Foreground Service):** يطير في الجو ويستمر في تسجيل إحداثيات الرحلة في الخلفية حتى لو نام المسافرون في الصالة (عند قفل الشاشة).
- **شاشة الرادار (TrackCanvasView):** شاشة مخصصة تحول إحداثيات القمر الصناعي إلى نقاط مضيئة وترسم مسار الطيران.
- **أرشيف الرحلات (TrackStorage & JSON):** خزنة تُحفظ فيها سجلات الرحلات السابقة ليتم فتحها في أي وقت.

---

## 2. نظرة عامة على المجلدات والملفات

الكود مقسم إلى حزم (Packages) حسب المسؤولية:

### 📁 `zeev.fraiman.tortuositycoefficient` (الشاشات - Activities)
- **`SplashActivity`:** شاشة الانطلاق الأولى تعرض الشعار والاسم لمدة 4 ثوانٍ.
- **`LanguageActivity`:** شاشة اختيار اللغة (الإنجليزية، العبرية، الروسية) مع مؤقت 5 ثوانٍ.
- **`MainActivity`:** لوحة التحكم الرئيسية لتسجيل الـ GPS والعدادات الحية.
- **`AnalysisActivity`:** شاشة تحليل النتائج وعرض المعامل $K \pm \Delta K$ والخط المستقيم.
- **`ArchiveActivity`:** شاشة الأرشيف للمسارات المحفوظة.

### 📁 `service` (التنفيذ في الخلفية)
- **`TrackRecordingService`:** خدمة خلفية تستقبل إحداثيات الـ GPS باستمرار عبر `LocationManager`.

### 📁 `ui` (الرسم المخصص)
- **`TrackCanvasView`:** مكون رسم مخصص يحول إحداثيات خط العرض/الطول إلى بكسلات الشاشة مع ضبط الأبعاد.

### 📁 `model` (نماذج البيانات)
- **`TrackPoint`:** تمثيل نقطة GPS واحدة (`latitude`, `longitude`, `timestamp`, `accuracy`).
- **`Track`:** النموذج الهندسي الرئيسي يحتوي على قائمة النقاط والمعادلات الرياضية.

### 📁 `storage` (التخزين)
- **`TrackStorage`:** مسؤول عن حفظ وقراءة المسارات من ملف `saved_tracks.json`.

### 📁 `util` & `adapter` (الأدوات والمحولات)
- **`LocaleHelper`:** يدير إعدادات لغات التطبيق.
- **`GpsCheckUtils`:** يفحص حالة الـ GPS وإذونات الموقع ووضع توفير الطاقة.
- **`TrackAdapter`:** محول `RecyclerView` لبطاقات الأرشيف.

---

## 3. العلاقات بين المكونات (تدفق البيانات)

فيما يلي مخطط تدفق البيانات أثناء تسجيل المسار:

```
[قمر GPS صناعي] 
    │ (تحديث الموقع)
    ▼
[LocationManager]
    │ (onLocationChanged)
    ▼
[TrackRecordingService] ──> ينشئ ──> [TrackPoint] ──> يضيف إلى ──> [Track]
    │
    │ (مؤقت كل ثانية / Broadcast)
    ▼
[MainActivity] ──> يجلب Track ──> يحدد الواجهة ──> يمرر إلى ──> [TrackCanvasView]
    │
    │ (النقر على "تحليل")
    ▼
[AnalysisActivity] ──> يعرض K ± ΔK ──> النقر على "حفظ" ──> [TrackStorage] ──> [saved_tracks.json]
```

---

## 4. مثال عملي من المشروع: خط سير نقطة GPS حتى الحفظ

لنتابع مسار نقطة GPS واحدة في الكود من البداية حتى النهاية:

### الخطوة 1: استقبال الإحداثيات في خدمة الخلفية (`TrackRecordingService.java`)
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

### الخطوة 2: تحديث الواجهة ورسم القماش (`MainActivity.java`)
```java
private void updateUIFromService() {
    if (!isBound || recordingService == null) return;
    Track track = recordingService.getCurrentTrack();
    if (track != null) {
        tvDistance.setText(String.format(Locale.getDefault(), "%.1f м", track.calculatePathLength()));
        tvK.setText(String.format(Locale.getDefault(), "%.2f", track.calculateTortuosityK()));
        canvasView.setTrack(track); // إعادة رسم القماش
    }
}
```

### الخطوة 3: حفظ المسار في الأرشيف (`TrackStorage.java`)
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

## 5. قاموس المصطلحات التقنية

1. **Activity:** مكون واجهة المستخدم ينشئ شاشة واحدة.
2. **Foreground Service:** خدمة خلفية تعمل بإشعار دائم تضمن عدم إغلاق التسجيل من أندرويد.
3. **Custom View / Canvas:** مساحة رسم مخصصة تتيח للمطور رسم الأشكال والخطوط بالمعادلات.
4. **JSON:** صيغة نصية خفيفة ومنظمة لحفظ البيانات.
5. **Adapter:** مكون وسيط يربط قائمة البيانات بالتعديل البصري في القائمة (`RecyclerView`).
6. **SharedPreferences:** ذاكرة محلية خفيفة للإعدادات البسيطة (مثل اللغة).
7. **LocationListener:** مستمع يستقبل الإحداثيات فور تحديثها من الأقمار الصناعية.
