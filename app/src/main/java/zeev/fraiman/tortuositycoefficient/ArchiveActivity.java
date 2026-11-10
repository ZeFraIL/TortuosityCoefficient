package zeev.fraiman.tortuositycoefficient;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import zeev.fraiman.tortuositycoefficient.adapter.TrackAdapter;
import zeev.fraiman.tortuositycoefficient.model.Track;
import zeev.fraiman.tortuositycoefficient.storage.TrackStorage;

public class ArchiveActivity extends AppCompatActivity implements TrackAdapter.OnTrackClickListener {
    private TrackStorage storage;
    private RecyclerView rvTracks;
    private TextView tvEmptyArchive;
    private TrackAdapter adapter;
    private List<Track> tracks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_archive);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbarArchive), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbarArchive);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Архив треков");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        storage = new TrackStorage(this);
        rvTracks = findViewById(R.id.rvTracks);
        tvEmptyArchive = findViewById(R.id.tvEmptyArchive);

        rvTracks.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadArchiveData();
    }

    private void loadArchiveData() {
        tracks = storage.loadTracks();
        if (tracks.isEmpty()) {
            tvEmptyArchive.setVisibility(View.VISIBLE);
            rvTracks.setVisibility(View.GONE);
        } else {
            tvEmptyArchive.setVisibility(View.GONE);
            rvTracks.setVisibility(View.VISIBLE);
            if (adapter == null) {
                adapter = new TrackAdapter(tracks, this);
                rvTracks.setAdapter(adapter);
            } else {
                adapter.setTracks(tracks);
            }
        }
    }

    @Override
    public void onTrackClick(Track track) {
        Intent intent = new Intent(this, AnalysisActivity.class);
        intent.putExtra(AnalysisActivity.EXTRA_TRACK, track);
        startActivity(intent);
    }

    @Override
    public void onTrackLongClick(Track track) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить трек?")
                .setMessage("Вы действительно хотите удалить трек \"" + track.getTitle() + "\"?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    boolean deleted = storage.deleteTrack(track.getId());
                    if (deleted) {
                        Toast.makeText(this, "Трек удален", Toast.LENGTH_SHORT).show();
                        loadArchiveData();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
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
