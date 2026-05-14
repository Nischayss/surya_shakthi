package com.suryashakti.solar.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.suryashakti.solar.R;

public class NotificationHelper {

    private static final String CHANNEL_ID = "surya_shakti_channel";
    private static final String CHANNEL_NAME = "Surya-Shakti Alerts";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Solar energy alerts and tips");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    public static void sendPeakSunAlert(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_sun)
                .setContentTitle("☀️ High Sun Alert!")
                .setContentText("Ideal time to run heavy appliances. Maximize your solar savings!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(1001, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public static void sendOverGenerationAlert(Context context, float exportKwh) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_bolt)
                .setContentTitle("⚡ Over-Generation!")
                .setContentText(String.format("%.1f kWh being exported to grid. Great work!", exportKwh))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(1002, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
