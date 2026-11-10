package zeev.fraiman.tortuositycoefficient;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;


public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 4000L; // 4 секунды
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isNavigated = false;

    private final Runnable navigateRunnable = this::navigateToLanguageActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_splash);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Нажатие на экран пропускает ожидание 4 секунд
        findViewById(android.R.id.content).setOnClickListener(v -> navigateToLanguageActivity());

        // Запуск таймера на 4 секунды
        handler.postDelayed(navigateRunnable, SPLASH_DELAY_MS);
    }

    private synchronized void navigateToLanguageActivity() {
        if (isNavigated) return;
        isNavigated = true;
        handler.removeCallbacks(navigateRunnable);

        Intent intent = new Intent(SplashActivity.this, LanguageActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(navigateRunnable);
    }
}
