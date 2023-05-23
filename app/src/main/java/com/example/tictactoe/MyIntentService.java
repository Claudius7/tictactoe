package com.example.tictactoe;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MyIntentService extends IntentService {
    private static final int NOTIF_ID = 777;

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        createNotification();
    }

    private void createNotification() {
        String channelId = this.getPackageName() + ".daily_reminder";

        Intent intent = new Intent(this, SplashScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, intent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notifification)
                .setContentTitle("Let's play!")
                .setContentText("We miss you♥♥♥. Come and play again soon!")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notifBuilder.setPriority(NotificationCompat.PRIORITY_MIN);
        }

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(NOTIF_ID, notifBuilder.build());
    }
}
