package zeev.fraiman.tortuositycoefficient.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.List;

import zeev.fraiman.tortuositycoefficient.model.Track;
import zeev.fraiman.tortuositycoefficient.model.TrackPoint;

public class TrackCanvasView extends View {
    private Track track;
    private boolean showDirectLine = true;

    private Paint trackPaint;
    private Paint directLinePaint;
    private Paint startMarkerPaint;
    private Paint endMarkerPaint;
    private Paint textPaint;
    private Paint gridPaint;
    private Paint hintPaint;

    public TrackCanvasView(Context context) {
        super(context);
        init();
    }

    public TrackCanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TrackCanvasView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setColor(Color.parseColor("#1976D2")); // Blue
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(8f);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setStrokeJoin(Paint.Join.ROUND);

        directLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        directLinePaint.setColor(Color.parseColor("#E53935")); // Red
        directLinePaint.setStyle(Paint.Style.STROKE);
        directLinePaint.setStrokeWidth(6f);
        directLinePaint.setPathEffect(new DashPathEffect(new float[]{16f, 12f}, 0));

        startMarkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        startMarkerPaint.setColor(Color.parseColor("#4CAF50")); // Green
        startMarkerPaint.setStyle(Paint.Style.FILL);

        endMarkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        endMarkerPaint.setColor(Color.parseColor("#E53935")); // Red
        endMarkerPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#E0E0E0"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2f);

        hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hintPaint.setColor(Color.GRAY);
        hintPaint.setTextSize(36f);
        hintPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setTrack(Track track) {
        this.track = track;
        invalidate();
    }

    public void setShowDirectLine(boolean showDirectLine) {
        this.showDirectLine = showDirectLine;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Отрисовка фона и мягкой сетки
        canvas.drawColor(Color.parseColor("#FAFAFA"));
        drawBackgroundGrid(canvas, width, height);

        if (track == null || track.getPoints() == null || track.getPoints().size() < 2) {
            canvas.drawText("Ожидание GPS точек трека...", width / 2f, height / 2f, hintPaint);
            return;
        }

        List<TrackPoint> points = track.getPoints();

        // Поиск границ координат
        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;

        for (TrackPoint p : points) {
            if (p.getLatitude() < minLat) minLat = p.getLatitude();
            if (p.getLatitude() > maxLat) maxLat = p.getLatitude();
            if (p.getLongitude() < minLon) minLon = p.getLongitude();
            if (p.getLongitude() > maxLon) maxLon = p.getLongitude();
        }

        double meanLatRad = Math.toRadians((minLat + maxLat) / 2.0);
        double cosLat = Math.cos(meanLatRad);

        // Перевод координат в м (относительно minLat, minLon)
        double minX = 0.0;
        double maxX = (maxLon - minLon) * 111000.0 * cosLat;
        double minY = 0.0;
        double maxY = (maxLat - minLat) * 111000.0;

        double deltaX = maxX - minX;
        double deltaY = maxY - minY;

        // Если все точки в одном месте (delta слишком мала)
        if (deltaX < 1.0 && deltaY < 1.0) {
            deltaX = 10.0;
            deltaY = 10.0;
        }

        float padding = 60f;
        float availWidth = width - 2 * padding;
        float availHeight = height - 2 * padding;

        double scaleX = availWidth / deltaX;
        double scaleY = availHeight / deltaY;
        double scale = Math.min(scaleX, scaleY); // сохраняем пропорции

        // Центрирование рисунка
        float offsetX = (float) (padding + (availWidth - deltaX * scale) / 2.0);
        float offsetY = (float) (padding + (availHeight - deltaY * scale) / 2.0);

        // Преобразование lat/lon -> экранные X/Y
        float[] screenX = new float[points.size()];
        float[] screenY = new float[points.size()];

        for (int i = 0; i < points.size(); i++) {
            TrackPoint p = points.get(i);
            double xMeters = (p.getLongitude() - minLon) * 111000.0 * cosLat;
            double yMeters = (p.getLatitude() - minLat) * 111000.0;

            screenX[i] = (float) (offsetX + xMeters * scale);
            // Ось Y на экране направлена вниз, а широта — вверх
            screenY[i] = (float) (offsetY + (deltaY - yMeters) * scale);
        }

        // 1. Отрисовка линии прямого расстояния (старт -> финиш)
        if (showDirectLine) {
            canvas.drawLine(screenX[0], screenY[0], screenX[points.size() - 1], screenY[points.size() - 1], directLinePaint);
        }

        // 2. Отрисовка фактического трека
        Path trackPath = new Path();
        trackPath.moveTo(screenX[0], screenY[0]);
        for (int i = 1; i < points.size(); i++) {
            trackPath.lineTo(screenX[i], screenY[i]);
        }
        canvas.drawPath(trackPath, trackPaint);

        // 3. Маркеры старта (зеленый) и финиша (красный)
        canvas.drawCircle(screenX[0], screenY[0], 20f, startMarkerPaint);
        canvas.drawText("S", screenX[0], screenY[0] + 8f, textPaint);

        canvas.drawCircle(screenX[points.size() - 1], screenY[points.size() - 1], 20f, endMarkerPaint);
        canvas.drawText("F", screenX[points.size() - 1], screenY[points.size() - 1] + 8f, textPaint);
    }

    private void drawBackgroundGrid(Canvas canvas, int width, int height) {
        int step = 80;
        for (int x = 0; x < width; x += step) {
            canvas.drawLine(x, 0, x, height, gridPaint);
        }
        for (int y = 0; y < height; y += step) {
            canvas.drawLine(0, y, width, y, gridPaint);
        }
    }
}
