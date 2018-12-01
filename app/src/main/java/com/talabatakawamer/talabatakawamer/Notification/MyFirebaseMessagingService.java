package com.talabatakawamer.talabatakawamer.Notification;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.util.Log;

import com.talabatakawamer.talabatakawamer.CartActivity.CartActivity;
import com.talabatakawamer.talabatakawamer.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    private String TAG = "MyFirebaseMessagingService";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                sendPushNotification(json);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }

        }

        if (remoteMessage.getNotification() != null)
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());


    }

    private void sendPushNotification(JSONObject json) throws JSONException {

        //getting the json data
        JSONObject data = json.getJSONObject("data");

        //parsing json data
        String title = data.getString("title");
        String message = data.getString("message");
        String imageUrl = data.getString("image");
        String channel_ID = data.getString("channel_id");
        // final Bill as order ID or JsonObject for data of Rating
        String overData = data.getString("content");


        if (channel_ID.equals("final Bill"))
            // save last final bill
            getPreferences().edit().putString(getApplicationContext().getString(R.string.lastFinalBill), overData).apply();
        else {
            // mean channel_ID = Rating so show notify after delay
            showNotifyAfterDelay(overData, title, message, channel_ID);
            // stopMethod
            return;
        }


        //creating MyNotificationManager object
        MyNotificationManager mNotificationManager = new MyNotificationManager(getApplicationContext());

        //creating an intent for the notification
        Intent intent = new Intent(getApplicationContext(), CartActivity.class);

        //if there is no image
        if (imageUrl.equals("null")) {
            //displaying small notification
            mNotificationManager.showSmallNotification(title, message, intent, channel_ID);
        } else {
            //if there is an image
            //displaying a big notification
            mNotificationManager.showBigNotification(title, message, imageUrl, intent, channel_ID);
        }

    }


    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // save token
        SharedPreferences preferences=getPreferences();
        preferences.edit().putString(getApplicationContext().getString(R.string.token), token).apply();
    }


    private SharedPreferences getPreferences() {
        Context context = getApplicationContext();
        return context.getSharedPreferences(context.getString(R.string.notificationPreference), MODE_PRIVATE);
    }


    private void showNotifyAfterDelay(String overData, String title, String message, String channel_ID) {

        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent serviceIntent = new Intent(getApplicationContext(), MyReceiver.class);

        serviceIntent.putExtra("overData",overData);
        serviceIntent.putExtra("title",title);
        serviceIntent.putExtra("message",message);
        serviceIntent.putExtra("channel_ID",channel_ID);

        PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0,     serviceIntent , PendingIntent.FLAG_UPDATE_CURRENT);

        // call receiver after 13 min
        assert am!=null;
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (1000*60*13), pi);


    }

}
