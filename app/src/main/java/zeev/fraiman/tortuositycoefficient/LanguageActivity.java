package zeev.fraiman.tortuositycoefficient;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

import zeev.fraiman.tortuositycoefficient.util.LocaleHelper;

public class LanguageActivity extends AppCompatActivity {

    private static final long TIMER_DURATION_MS = 5000L; // 5 секунд
    private static final long TIMER_INTERVAL_MS = 1000L;

    private TextView tvCountdown;
    private Button btnEnglish;
    private Button btnHebrew;
    private Button btnRussian;

    private CountDownTimer countDownTimer;
    private boolean isNavigated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_language);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutLanguageRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbarLanguage);
        setSupportActionBar(toolbar);

        tvCountdown = findViewById(R.id.tvTimerCountdown);
        btnEnglish = findViewById(R.id.btnEnglish);
        btnHebrew = findViewById(R.id.btnHebrew);
        btnRussian = findViewById(R.id.btnRussian);

        btnEnglish.setOnClickListener(v -> selectLanguageAndProceed(LocaleHelper.LANGUAGE_ENGLISH));
        btnHebrew.setOnClickListener(v -> selectLanguageAndProceed(LocaleHelper.LANGUAGE_HEBREW));
        btnRussian.setOnClickListener(v -> selectLanguageAndProceed(LocaleHelper.LANGUAGE_RUSSIAN));

        startCountdownTimer();
    }

    private void startCountdownTimer() {
        countDownTimer = new CountDownTimer(TIMER_DURATION_MS, TIMER_INTERVAL_MS) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsLeft = Math.round((double) millisUntilFinished / 1000.0);
                if (secondsLeft < 1) secondsLeft = 1;
                tvCountdown.setText(String.format(Locale.getDefault(), "Переход через %d сек...", secondsLeft));
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("Переход...");
                proceedToMainActivity();
            }
        }.start();
    }

    private synchronized void selectLanguageAndProceed(String languageCode) {
        if (isNavigated) return;
        isNavigated = true;
        cancelTimer();

        // Сохраняем и применяем выбранный язык
        LocaleHelper.setLanguage(this, languageCode);

        Intent intent = new Intent(LanguageActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private synchronized void proceedToMainActivity() {
        if (isNavigated) return;
        isNavigated = true;
        cancelTimer();

        // Если пользователь не выбрал язык за 5 секунд, применяем ранее сохраненный (или "en" по умолчанию)
        String currentLang = LocaleHelper.getLanguage(this);
        LocaleHelper.applyLanguage(this, currentLang);

        Intent intent = new Intent(LanguageActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_common, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
    }
}
