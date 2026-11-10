package zeev.fraiman.tortuositycoefficient.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import zeev.fraiman.tortuositycoefficient.MainActivity;
import zeev.fraiman.tortuositycoefficient.R;
import zeev.fraiman.tortuositycoefficient.model.Track;
import zeev.fraiman.tortuositycoefficient.model.TrackPoint;

public class TrackRecordingService extends Service implements LocationListener {
    private static final String TAG = "TrackRecordingService";

    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_RESUME = "ACTION_RESUME";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String BROADCAST_TRACK_UPDATE = "zeev.fraiman.tortuositycoefficient.TRACK_UPDATE";

    private static final String CHANNEL_ID = "TrackRecordingChannel";
    private static final int NOTIFICATION_ID = 101;

    private LocationManager locationManager;
    private Track currentTrack;
    private boolean isRecording = false;
    private boolean isPaused = false;

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public TrackRecordingService getService() {
            return TrackRecordingService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_START:
                    startRecordingTrack();
                    break;
                case ACTION_PAUSE:
                    pauseRecording();
                    break;
                case ACTION_RESUME:
                    resumeRecording();
                    break;
                case ACTION_STOP:
                    stopRecordingTrack();
                    break;
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @SuppressLint("MissingPermission")
    private void startRecordingTrack() {
        if (isRecording) return;

        currentTrack = new Track("Маршрут " + new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(new java.util.Date()));
        isRecording = true;
        isPaused = false;

        Notification notification = createNotification("Запись трека...", "Начато отслеживание движения");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
            } else {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
            }
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0.0f, this);
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 0.0f, this);
            }

            Location lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location lastNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location initialLoc = (lastGps != null) ? lastGps : lastNet;
            if (initialLoc != null) {
                onLocationChanged(initialLoc);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error requesting location updates", e);
        }

        broadcastUpdate();
    }

    private void pauseRecording() {
        if (!isRecording || isPaused) return;
        isPaused = true;
        try {
            locationManager.removeUpdates(this);
        } catch (Exception e) {
            Log.e(TAG, "Error removing location updates", e);
        }
        updateNotification("Запись приостановлена", "Точки не записываются");
        broadcastUpdate();
    }

    @SuppressLint("MissingPermission")
    private void resumeRecording() {
        if (!isRecording || !isPaused) return;
        isPaused = false;
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0.0f, this);
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 0.0f, this);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error resuming location updates", e);
        }
        updateNotification("Запись трека...", "Продолжение записи");
        broadcastUpdate();
    }

    public Track stopRecordingTrack() {
        isRecording = false;
        isPaused = false;
        try {
            locationManager.removeUpdates(this);
        } catch (Exception e) {
            Log.e(TAG, "Error removing location updates", e);
        }
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
        return currentTrack;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!isRecording || isPaused || location == null) return;

        TrackPoint point = new TrackPoint(
                location.getLatitude(),
                location.getLongitude(),
                location.getTime() > 0 ? location.getTime() : System.currentTimeMillis(),
                location.hasAccuracy() ? location.getAccuracy() : 5.0f
        );

        if (currentTrack != null) {
            currentTrack.addPoint(point);
            updateNotification("Запись трека", String.format(java.util.Locale.getDefault(),
                    "Точек: %d | Путь: %.1f м", currentTrack.getPoints().size(), currentTrack.calculatePathLength()));
            broadcastUpdate();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

    public Track getCurrentTrack() {
        return currentTrack;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public boolean isPaused() {
        return isPaused;
    }

    private void broadcastUpdate() {
        Intent intent = new Intent(BROADCAST_TRACK_UPDATE);
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Запись GPS трека",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Уведомление службы записи GPS-трека");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification(String title, String contentText) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void updateNotification(String title, String contentText) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, createNotification(title, contentText));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            locationManager.removeUpdates(this);
        } catch (Exception e) {
            Log.e(TAG, "Error removing location updates in onDestroy", e);
        }
    }
}
