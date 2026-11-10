package zeev.fraiman.tortuositycoefficient;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

import zeev.fraiman.tortuositycoefficient.model.Track;
import zeev.fraiman.tortuositycoefficient.storage.TrackStorage;
import zeev.fraiman.tortuositycoefficient.ui.TrackCanvasView;

public class AnalysisActivity extends AppCompatActivity {
    public static final String EXTRA_TRACK = "EXTRA_TRACK";

    private Track track;
    private TrackStorage storage;

    private TextView tvK;
    private TextView tvPathLength;
    private TextView tvDirectDistance;
    private TextView tvDuration;
    private TextView tvPoints;
    private TrackCanvasView canvasView;
    private EditText etTrackTitle;
    private Button btnSaveTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_analysis);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbarAnalysis), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbarAnalysis);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Анализ трека");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        storage = new TrackStorage(this);

        tvK = findViewById(R.id.tvAnalysisK);
        tvPathLength = findViewById(R.id.tvAnalysisPathLength);
        tvDirectDistance = findViewById(R.id.tvAnalysisDirectDistance);
        tvDuration = findViewById(R.id.tvAnalysisDuration);
        tvPoints = findViewById(R.id.tvAnalysisPoints);
        canvasView = findViewById(R.id.analysisCanvasView);
        etTrackTitle = findViewById(R.id.etTrackTitle);
        btnSaveTrack = findViewById(R.id.btnSaveTrack);

        if (getIntent() != null && getIntent().hasExtra(EXTRA_TRACK)) {
            track = (Track) getIntent().getSerializableExtra(EXTRA_TRACK);
        }

        if (track != null) {
            displayAnalysisData();
        } else {
            Toast.makeText(this, "Не удалось загрузить данные трека", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnSaveTrack.setOnClickListener(v -> saveTrackAndFinish());
    }

    private void displayAnalysisData() {
        double k = track.calculateTortuosityK();
        double deltaK = track.calculateNoiseUncertainty();

        tvK.setText(String.format(Locale.getDefault(), "K = %.2f ± %.2f", k, deltaK));

        double pathLen = track.calculatePathLength();
        tvPathLength.setText(String.format(Locale.getDefault(), "Пройденный путь (L_path): %.1f м", pathLen));

        double directLen = track.calculateDirectDistance();
        tvDirectDistance.setText(String.format(Locale.getDefault(), "Прямое расстояние (L_direct): %.1f м", directLen));

        long durationSec = track.getDurationSeconds();
        long mins = durationSec / 60;
        long secs = durationSec % 60;
        tvDuration.setText(String.format(Locale.getDefault(), "Время в пути: %d мин %d сек", mins, secs));

        int ptsCount = track.getPoints() != null ? track.getPoints().size() : 0;
        tvPoints.setText(String.format(Locale.getDefault(), "Записано GPS-точек: %d", ptsCount));

        if (track.getTitle() != null) {
            etTrackTitle.setText(track.getTitle());
        }

        canvasView.setTrack(track);
        canvasView.setShowDirectLine(true);
    }

    private void saveTrackAndFinish() {
        if (track != null) {
            String newTitle = etTrackTitle.getText().toString().trim();
            if (!newTitle.isEmpty()) {
                track.setTitle(newTitle);
            }
            boolean saved = storage.addTrack(track);
            if (saved) {
                Toast.makeText(this, "Трек сохранен в архив", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, ArchiveActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Ошибка при сохранении трека", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_common, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_previous) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else if (itemId == R.id.action_exit) {
            finishAffinity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
