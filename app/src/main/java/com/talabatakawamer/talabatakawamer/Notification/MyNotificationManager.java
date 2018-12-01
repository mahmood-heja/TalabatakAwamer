package com.talabatakawamer.talabatakawamer.Notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;


import com.talabatakawamer.talabatakawamer.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyNotificationManager {

    private static final int ID_BIG_NOTIFICATION = 234;
    private static final int ID_SMALL_NOTIFICATION = 235;



    private Context mCtx;

    private SharedPreferences preferences;

    public MyNotificationManager(Context mCtx) {
        this.mCtx = mCtx;

        preferences = mCtx.getSharedPreferences("notification", Context.MODE_PRIVATE);
    }

    //the method will show a big notification with an image
    //parameters are title for message title, message for message text, url of the big image and an intent that will open
    //when you will tap on the notification

    public void showBigNotification(String title, String message, String url, Intent intent, String channel_ID) {

        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("des", message);
        bundle.putString("url image", url);
        intent.putExtra("bundle", bundle);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mCtx,
                        ID_BIG_NOTIFICATION,
                        intent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );


        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(title);
        bigPictureStyle.setSummaryText(Html.fromHtml(message).toString());
        bigPictureStyle.bigPicture(getBitmapFromURL(url, true));
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mCtx, channel_ID);
        Notification notification;


        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notification = mBuilder.setTicker(title).setWhen(0)
                .setSound(alarmSound)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setStyle(bigPictureStyle)
                .setLargeIcon(BitmapFactory.decodeResource(mCtx.getResources(), R.mipmap.ic_launcher))
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(ID_BIG_NOTIFICATION, notification);
    }

    //the method will show a small notification
    //parameters are title for message title, message for message text and an intent that will open
    //when you will tap on the notification
    public void showSmallNotification(String title, String message, Intent intent, String channel_ID) {

        intent.putExtra("ShowFinalBillDialog", true);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mCtx,
                        ID_SMALL_NOTIFICATION,
                        intent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );



        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mCtx, channel_ID);
        Notification notification;


        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        notification = mBuilder.setTicker(title).setWhen(0)
                .setSound(alarmSound)
                .setAutoCancel(true)
                .setShowWhen(true)
                .setContentIntent(resultPendingIntent)
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(mCtx.getResources(), R.drawable.ic_final_bill))
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mCtx);
        notificationManager.notify(ID_SMALL_NOTIFICATION, notification);


    }

    //The method will return Bitmap from an image URL
    private Bitmap getBitmapFromURL(String strURL, boolean firstCall) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();

            Log.e("ttt", input.toString());
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();

            if (firstCall) {
                String url_s = "http" + new StringBuffer().append(strURL).substring(5);
                return getBitmapFromURL(url_s, false);
            }

            return null;
        }
    }


}