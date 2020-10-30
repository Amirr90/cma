package com.jccode.mycorrespondence;

import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.jccode.mycorrespondence.utility.Util;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    NotificationManagerCompat managerCompat;
    private static final int PENDING_INTENT_REQ_CODE = 101;//any random number
    private static final CharSequence SUMMERY = "summary";
    private static final CharSequence BIG_CONTENT_TITLE = "big_content_title";
    Util utils;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getNotification() != null) {
            String body = remoteMessage.getNotification().getBody();
            String title = remoteMessage.getNotification().getTitle();
            String click_action = remoteMessage.getNotification().getClickAction();
            String notificationId = remoteMessage.getData().get( "notification_id" );
            utils = new Util( this );
            if (utils != null) {
                utils.showNotification( title, body, click_action, notificationId );
            }

        }

    }


}