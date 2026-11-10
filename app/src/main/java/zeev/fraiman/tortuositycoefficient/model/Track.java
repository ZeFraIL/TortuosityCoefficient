package zeev.fraiman.tortuositycoefficient.model;

import android.location.Location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Track implements Serializable {
    private String id;
    private String title;
    private long startTime;
    private long endTime;
    private List<TrackPoint> points;

    public Track() {
        this.id = UUID.randomUUID().toString();
        this.title = "Маршрут";
        this.points = new ArrayList<>();
        this.startTime = System.currentTimeMillis();
        this.endTime = System.currentTimeMillis();
    }

    public Track(String title) {
        this();
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public List<TrackPoint> getPoints() {
        return points;
    }

    public void setPoints(List<TrackPoint> points) {
        this.points = points;
    }

    public void addPoint(TrackPoint point) {
        if (this.points == null) {
            this.points = new ArrayList<>();
        }
        this.points.add(point);
        this.endTime = point.getTimestamp();
    }

    /**
     * Вычисляет полную длину пройденного пути (L_path) в метрах.
     */
    public double calculatePathLength() {
        if (points == null || points.size() < 2) {
            return 0.0;
        }
        double totalDistance = 0.0;
        float[] result = new float[1];
        for (int i = 0; i < points.size() - 1; i++) {
            TrackPoint p1 = points.get(i);
            TrackPoint p2 = points.get(i + 1);
            Location.distanceBetween(
                    p1.getLatitude(), p1.getLongitude(),
                    p2.getLatitude(), p2.getLongitude(),
                    result
            );
            totalDistance += result[0];
        }
        return totalDistance;
    }

    /**
     * Вычисляет прямое расстояние от старта до финиша (L_direct) в метрах.
     */
    public double calculateDirectDistance() {
        if (points == null || points.size() < 2) {
            return 0.0;
        }
        TrackPoint start = points.get(0);
        TrackPoint end = points.get(points.size() - 1);
        float[] result = new float[1];
        Location.distanceBetween(
                start.getLatitude(), start.getLongitude(),
                end.getLatitude(), end.getLongitude(),
                result
        );
        return result[0];
    }

    /**
     * Вычисляет коэффициент извилистости K = L_path / L_direct.
     */
    public double calculateTortuosityK() {
        double pathLength = calculatePathLength();
        double directDistance = calculateDirectDistance();
        if (directDistance <= 0.001) {
            return 1.0;
        }
        return pathLength / directDistance;
    }

    /**
     * Вычисляет оценку погрешности коэффициента извилистости Delta K,
     * вызванную шумом GPS при суммировании отрезков.
     */
    public double calculateNoiseUncertainty() {
        if (points == null || points.size() < 2) {
            return 0.0;
        }
        double directDistance = calculateDirectDistance();
        if (directDistance <= 0.001) {
            return 0.0;
        }

        double totalAccuracy = 0.0;
        for (TrackPoint p : points) {
            totalAccuracy += (p.getAccuracy() > 0 ? p.getAccuracy() : 5.0);
        }
        double avgAccuracy = totalAccuracy / points.size();

        // Оценка фиктивного удлинения из-за шума GPS N точек
        double estimatedNoiseLength = avgAccuracy * Math.sqrt(points.size());
        return estimatedNoiseLength / directDistance;
    }

    public long getDurationSeconds() {
        if (endTime >= startTime) {
            return (endTime - startTime) / 1000;
        }
        return 0;
    }
}
