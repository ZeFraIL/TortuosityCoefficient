package zeev.fraiman.tortuositycoefficient.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

public class GpsCheckUtils {

    public static boolean isGpsEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (lm != null) {
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        return false;
    }

    public static boolean hasFineLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isPowerSaveMode(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            return pm.isPowerSaveMode();
        }
        return false;
    }

    public static void showGpsDisabledDialog(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("Геолокация (GPS) выключена")
                .setMessage("Для точной записи трека движения и вычисления коэффициента извилистости необходимо включить модуль GPS на устройстве.")
                .setPositiveButton("Включить GPS", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    activity.startActivity(intent);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    public static void showPowerSaveWarningDialog(Activity activity, Runnable onContinue) {
        new AlertDialog.Builder(activity)
                .setTitle("Включен режим энергосбережения")
                .setMessage("Внимание: на вашем устройстве активирована экономия батареи. Система Android может глушить работу GPS в фоновом режиме, из-за чего трек может быть прерван.\n\nРекомендуется отключить энергосбережение на время прогулки.")
                .setPositiveButton("Все равно начать", (dialog, which) -> {
                    if (onContinue != null) {
                        onContinue.run();
                    }
                })
                .setNeutralButton("Настройки батареи", (dialog, which) -> {
                    try {
                        Intent intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
                        activity.startActivity(intent);
                    } catch (Exception e) {
                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        activity.startActivity(intent);
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}
