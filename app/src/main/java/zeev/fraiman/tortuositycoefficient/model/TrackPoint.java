package zeev.fraiman.tortuositycoefficient.model;

import java.io.Serializable;

public class TrackPoint implements Serializable {
    private double latitude;
    private double longitude;
    private long timestamp;
    private float accuracy;

    public TrackPoint() {
    }

    public TrackPoint(double latitude, double longitude, long timestamp, float accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.accuracy = accuracy;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }
}
