package com.example.applock;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class MyApplicationNotification extends Application {

    public static final String CHAINNEL_ID = "chainnel service";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotification();

    }

    private void createNotification() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(
                    CHAINNEL_ID ,
                    "applock",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager  = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

        }

    }
}
