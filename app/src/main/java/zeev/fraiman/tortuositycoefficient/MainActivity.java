package zeev.fraiman.tortuositycoefficient;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import zeev.fraiman.tortuositycoefficient.model.Track;
import zeev.fraiman.tortuositycoefficient.service.TrackRecordingService;
import zeev.fraiman.tortuositycoefficient.ui.TrackCanvasView;
import zeev.fraiman.tortuositycoefficient.util.GpsCheckUtils;

public class MainActivity extends AppCompatActivity {

    private TrackRecordingService recordingService;
    private boolean isBound = false;

    private TextView tvTimer;
    private TextView tvPoints;
    private TextView tvDistance;
    private TextView tvK;
    private TrackCanvasView canvasView;

    private Button btnStart;
    private Button btnPause;
    private Button btnStop;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isBound && recordingService != null && recordingService.isRecording() && !recordingService.isPaused()) {
                updateUIFromService();
            }
            timerHandler.postDelayed(this, 1000);
        }
    };

    private final BroadcastReceiver trackUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUIFromService();
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TrackRecordingService.LocalBinder binder = (TrackRecordingService.LocalBinder) service;
            recordingService = binder.getService();
            isBound = true;
            updateUIFromService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            recordingService = null;
            isBound = false;
        }
    };

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean fineLocation = Boolean.TRUE.equals(permissions.get(Manifest.permission.ACCESS_FINE_LOCATION));
                boolean coarseLocation = Boolean.TRUE.equals(permissions.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                if (fineLocation || coarseLocation) {
                    startTrackRecording();
                } else {
                    Toast.makeText(this, "Для записи трека необходим доступ к геолокации", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);

        tvTimer = findViewById(R.id.tvLiveTimer);
        tvPoints = findViewById(R.id.tvLivePoints);
        tvDistance = findViewById(R.id.tvLiveDistance);
        tvK = findViewById(R.id.tvLiveK);
        canvasView = findViewById(R.id.mainCanvasView);
        canvasView.setShowDirectLine(false); // во время записи показываем только линию трека

        btnStart = findViewById(R.id.btnStart);
        btnPause = findViewById(R.id.btnPause);
        btnStop = findViewById(R.id.btnStop);

        btnStart.setOnClickListener(v -> checkPermissionsAndStart());
        btnPause.setOnClickListener(v -> togglePause());
        btnStop.setOnClickListener(v -> stopRecordingAndAnalyze());

        timerHandler.post(timerRunnable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, TrackRecordingService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        ContextCompat.registerReceiver(this, trackUpdateReceiver, 
                new IntentFilter(TrackRecordingService.BROADCAST_TRACK_UPDATE), 
                ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
        try {
            unregisterReceiver(trackUpdateReceiver);
        } catch (Exception ignored) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void checkPermissionsAndStart() {
        List<String> permissionsNeeded = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            requestPermissionsLauncher.launch(permissionsNeeded.toArray(new String[0]));
        } else {
            proceedWithGpsChecksAndStart();
        }
    }

    private void proceedWithGpsChecksAndStart() {
        if (!GpsCheckUtils.isGpsEnabled(this)) {
            GpsCheckUtils.showGpsDisabledDialog(this);
            return;
        }

        if (GpsCheckUtils.isPowerSaveMode(this)) {
            GpsCheckUtils.showPowerSaveWarningDialog(this, this::startTrackRecording);
        } else {
            startTrackRecording();
        }
    }

    private void startTrackRecording() {
        Intent serviceIntent = new Intent(this, TrackRecordingService.class);
        serviceIntent.setAction(TrackRecordingService.ACTION_START);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        updateControlButtons(true, false);
    }

    private void togglePause() {
        if (isBound && recordingService != null) {
            if (recordingService.isPaused()) {
                Intent serviceIntent = new Intent(this, TrackRecordingService.class);
                serviceIntent.setAction(TrackRecordingService.ACTION_RESUME);
                startService(serviceIntent);
                btnPause.setText("Пауза");
                updateControlButtons(true, false);
            } else {
                Intent serviceIntent = new Intent(this, TrackRecordingService.class);
                serviceIntent.setAction(TrackRecordingService.ACTION_PAUSE);
                startService(serviceIntent);
                btnPause.setText("Продолжить");
                updateControlButtons(true, true);
            }
        }
    }

    private void stopRecordingAndAnalyze() {
        Track trackToAnalyze = null;
        if (isBound && recordingService != null) {
            trackToAnalyze = recordingService.stopRecordingTrack();
        }

        updateControlButtons(false, false);
        tvTimer.setText("00:00:00");
        tvPoints.setText("0");
        tvDistance.setText("0 м");
        tvK.setText("1.00");
        canvasView.setTrack(null);

        if (trackToAnalyze != null && trackToAnalyze.getPoints() != null && trackToAnalyze.getPoints().size() >= 2) {
            Intent intent = new Intent(this, AnalysisActivity.class);
            intent.putExtra(AnalysisActivity.EXTRA_TRACK, trackToAnalyze);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Недостаточно GPS-точек для анализа (требуется минимум 2 точки)", Toast.LENGTH_LONG).show();
        }
    }

    private void updateUIFromService() {
        if (!isBound || recordingService == null) return;

        boolean recording = recordingService.isRecording();
        boolean paused = recordingService.isPaused();
        updateControlButtons(recording, paused);

        Track track = recordingService.getCurrentTrack();
        if (track != null) {
            long durationSec = track.getDurationSeconds();
            long hours = durationSec / 3600;
            long minutes = (durationSec % 3600) / 60;
            long seconds = durationSec % 60;
            tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));

            int ptsCount = track.getPoints() != null ? track.getPoints().size() : 0;
            tvPoints.setText(String.valueOf(ptsCount));

            double pathLen = track.calculatePathLength();
            tvDistance.setText(String.format(Locale.getDefault(), "%.1f м", pathLen));

            double k = track.calculateTortuosityK();
            tvK.setText(String.format(Locale.getDefault(), "%.2f", k));

            canvasView.setTrack(track);
        }
    }

    private void updateControlButtons(boolean recording, boolean paused) {
        if (recording) {
            btnStart.setEnabled(false);
            btnPause.setEnabled(true);
            btnStop.setEnabled(true);
            btnPause.setText(paused ? "Продолжить" : "Пауза");
        } else {
            btnStart.setEnabled(true);
            btnPause.setEnabled(false);
            btnStop.setEnabled(false);
            btnPause.setText("Пауза");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_archive) {
            Intent intent = new Intent(this, ArchiveActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_previous) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else if (itemId == R.id.action_exit) {
            finishAffinity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
