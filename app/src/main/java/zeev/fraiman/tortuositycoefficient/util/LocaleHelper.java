package zeev.fraiman.tortuositycoefficient.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

public class LocaleHelper {
    private static final String PREF_NAME = "app_language_prefs";
    private static final String KEY_LANGUAGE = "key_language";
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_HEBREW = "he";
    public static final String LANGUAGE_RUSSIAN = "ru";

    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, LANGUAGE_ENGLISH);
    }

    public static void setLanguage(Context context, String language) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, language).apply();
        applyLanguage(context, language);
    }

    public static void applyLanguage(Context context) {
        String lang = getLanguage(context);
        applyLanguage(context, lang);
    }

    public static void applyLanguage(Context context, String language) {
        if (language == null || language.isEmpty()) {
            language = LANGUAGE_ENGLISH;
        }

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        LocaleListCompat currentAppLocales = AppCompatDelegate.getApplicationLocales();
        if (!currentAppLocales.isEmpty()) {
            Locale currentLocale = currentAppLocales.get(0);
            if (currentLocale != null && language.equalsIgnoreCase(currentLocale.getLanguage())) {
                // Язык уже применен в AppCompatDelegate, избегаем повторного вызова и мерцания экрана
                return;
            }
        }

        // Устанавливаем локаль через стандартный AppCompatDelegate без конфликтов с updateConfiguration
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language));
    }
}
