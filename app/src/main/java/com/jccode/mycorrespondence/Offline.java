package com.jccode.mycorrespondence;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;

import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

public class Offline extends Application {

    public static final String DEMO_CHANNEL_ID = "demo_notification_id";


    @Override
    public void onCreate() {
        super.onCreate();


        crateNotificationChannel();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();


        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
    }

    private void crateNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel demoNotificationChannel = new NotificationChannel(
                    DEMO_CHANNEL_ID,
                    "demo_notification",
                    NotificationManager.IMPORTANCE_HIGH);
            demoNotificationChannel.setDescription("DemoNotification");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(demoNotificationChannel);
        }
    }
}
