# 🏗️ מבנה וארכיטקטורה של פרויקט "מקדם פתלתלות" (Tortuosity Coefficient)

מכשיר מורה: מדריך ארכיטקטורה מובנה לתלמידי תיכון ולמפתחים מתחילים.

---

## 1. התמונה הגדולה

אפליקציית **"מקדם פתלתלות"** משמשת ככלי מדידה גיאוגרפי חכם: היא מקליטה את מסלול ה-GPS של המשתמש בזמן אמת, משרטטת את קו ההליכה על גבי קנבס מותאם, ומחשבת את היחס בין אורך הדרך בפועל ($L_{\text{path}}$) לבין המרחק הישיר בקו אוויר ($L_{\text{direct}}$) לקבלת מקדם הפתלתלות $K = \frac{L_{\text{path}}}{L_{\text{direct}}}$. האפליקציה מורכבת מ-4 חלקים מרכזיים: מסכי הממשק (Activities), שירות הקלטה ברקע (Foreground Service), מודל חישובים וקנבס (Math & Canvas), ושכבת שמירת נתונים בקובץ JSON.

### 🏢 אנלוגיה מהחיים האמיתיים: שדה תעופה ומגדל פיקוח
כדי להבין מדוע הקוד מחולק לחבילות (Packages) נפרדות, דמיינו **שדה תעופה בינלאומי**:
- **האולם הראשי (Activities):** המקום שבו הנוסעים (המשתמשים) רואים את הלוחות, לוחצים על כפתורים ורואים את הנתונים.
- **טייס המטוס ברקע (Foreground Service):** טס באוויר וממשיך לרשום את נתוני הטיסה במכשירים גם כשהנוסעים באולם ישנים (כשהמסך כבוי).
- **מסך המכ"ם (TrackCanvasView):** מסך מיוחד הממיר את קואורדינטות הלוויין לנקודות אור יפות ומצייר את מסלול הטיסה.
- **ארכיון הטיסות (TrackStorage & JSON):** כספת שבה נשמרים כל יומני הטיסה שעברו, כך שניתן יהיה לפתוח אותם מחדש בכל עת.

---

## 2. סקירת תיקיות וקבצים

הקוד מחולק לחבילות (Packages) לפי אחריות מקצועית:

### 📁 `zeev.fraiman.tortuositycoefficient` (מסכים - Activities)
- **`SplashActivity`:** מסך הפתיחה הראשוני המציג את הלוגו והשם ל-4 שניות.
- **`LanguageActivity`:** מסך לבחירת שפת האפליקציה (אנגלית, עברית, רוסית) עם טיימר של 5 שניות.
- **`MainActivity`:** לוח הבקרה הראשי להקלטת ה-GPS בזמן אמת.
- **`AnalysisActivity`:** מסך ניתוח התוצאות, הצגת $K \pm \Delta K$ ושמירה.
- **`ArchiveActivity`:** מסך הארכיון המציג את כל המסלולים השמורים.

### 📁 `service` (שירותי רקע)
- **`TrackRecordingService`:** שירות חזית (Foreground Service) האוסף נקודות GPS ברקע ללא הפסקה דרך `LocationManager`.

### 📁 `ui` (רכיבי תצוגה מותאמים)
- **`TrackCanvasView`:** רכיב קנבס מותאם אישית (Custom View) הממיר קואורדינטות גיאוגרפיות (קו רוחב/אורך) לפיקסלים ומצייר את המסלול.

### 📁 `model` (מודלי נתונים)
- **`TrackPoint`:** מייצג נקודת GPS בודדת (`latitude`, `longitude`, `timestamp`, `accuracy`).
- **`Track`:** המחלקה הגיאומטרית המרכזית המכילה את רשימת הנקודות ואת נוסחאות חישוב $K$ ושגיאת GPS.

### 📁 `storage` (אחסון נתונים)
- **`TrackStorage`:** מנהל השמירה והטעינה של המסלולים מתוך קובץ טקסט מובנה `saved_tracks.json`.

### 📁 `util` & `adapter` (כלי עזר ומתאמים)
- **`LocaleHelper`:** מנהל שמירת והחלפת שפת האפליקציה.
- **`GpsCheckUtils`:** בודק תקינות GPS, הרשאות ומצב חיסכון בסוללה.
- **`TrackAdapter`:** מתאם המקשר בין רשימת המסלולים לכרטיסיות ה-`RecyclerView`.

---

## 3. יחסים בין החלקים (זרימת הנתונים)

להלן תרשים הזרימה של הנתונים במערכת בעת הקלטת מסלול:

```
[לוויין GPS] 
    │ (אות מיקום)
    ▼
[LocationManager]
    │ (onLocationChanged)
    ▼
[TrackRecordingService] ──> יוצר ──> [TrackPoint] ──> מוסיף ל- ──> [Track]
    │
    │ (עדכון שניות / Broadcast)
    ▼
[MainActivity] ──> שולף נתונים ──> מעדכן טקסטים ──> מעביר ל- ──> [TrackCanvasView]
    │
    │ (לחיצה על "Анализ")
    ▼
[AnalysisActivity] ──> מציג K ± ΔK ──> לחיצה על שמירה ──> [TrackStorage] ──> [saved_tracks.json]
```

### שלבי האינטראקציה:
1. המשתמש לוחץ על **"Старт"** ב-`MainActivity`.
2. `MainActivity` מפעילה את `TrackRecordingService`.
3. ה-GPS שולח נקודה ──> ה-Service יוצר `TrackPoint` ומוסיף ל-`Track`.
4. בכל שניה, `MainActivity` קוראת מ-`TrackRecordingService` את ה-`Track` העדכני ומעבירה אותו ל-`TrackCanvasView.setTrack()`.
5. בעצירת ההקלטה, ה-`Track` מועבר ב-Intent ל-`AnalysisActivity` ונשמר ב-`TrackStorage` בקובץ JSON.

---

## 4. דוגמה מפרויקט זה: מסלול הקלטת נקודה ושמירתה

נتبع את המסלול שעוברת נקודת GPS בודדת בקוד מתחילה ועד סוף:

### שלב 1: קבלת הנקודה בשירות הרקע (`TrackRecordingService.java`)
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

### שלב 2: עדכון המסך הראשי והקנבס (`MainActivity.java`)
```java
private void updateUIFromService() {
    if (!isBound || recordingService == null) return;
    Track track = recordingService.getCurrentTrack();
    if (track != null) {
        tvDistance.setText(String.format(Locale.getDefault(), "%.1f м", track.calculatePathLength()));
        tvK.setText(String.format(Locale.getDefault(), "%.2f", track.calculateTortuosityK()));
        canvasView.setTrack(track); // מצייר מחדש את הקנבס
    }
}
```

### שלב 3: שמירת המסלול בפורמט JSON (`TrackStorage.java`)
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

## 5. מילון מונחים קצר

1. **Activity (מסך):** רכיב ממשק משתמש המציג חלון בודד על מסך הטלפון.
2. **Foreground Service (שירות חזית):** רכיב הפועל ברקע עם התראה קבועה בסרגל העליון, המבטיח שמערכת אנדרואיד לא תסגור את הקלטת ה-GPS.
3. **Custom View / Canvas (קנבס מותאם):** משטח ציור המאפשר למפתח לצייר צורות, קווים ונקודות לפי חישובים מתמטיים.
4. **JSON:** פורמט טקסטואלי קל וסטנדרטי לארגון ושמירת נתונים במבנה של מפתח וערך.
5. **Adapter (מתאם):** רכיב המקשר בין רשימת נתונים בזיכרון לבין הצגתם הוויזואלית ב רשימה נגללת (`RecyclerView`).
6. **SharedPreferences:** מנגנון שמירה מקומי של ערכים קטנים (כמו שפת האפליקציה הנבחרת).
7. **LocationListener:** מאזין של מערכת אנדרואיד המקבל התראה בכל مرة שהלוויין מזהה שינוי במיקום המכשיר.
