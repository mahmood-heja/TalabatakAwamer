package com.talabatakawamer.talabatakawamer.Notification;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import com.talabatakawamer.talabatakawamer.R;
import com.talabatakawamer.talabatakawamer.VegetablesSection.VegetablesActivity;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class MyReceiver  extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(context);

        MyNotificationManager mNotificationManager = new MyNotificationManager(context);

        String overData=intent.getStringExtra("overData");
        String channel_ID=intent.getStringExtra("channel_ID");
        String title=intent.getStringExtra("title");
        String message=intent.getStringExtra("message");

        Intent notificationIntent = new Intent(context, VegetablesActivity.class);
        mNotificationManager.showSmallNotification(title, message, notificationIntent, channel_ID);

        try {
            JSONObject data = new JSONObject(overData);
            int orderId = data.getInt("id");
            String phoneNumber = data.getString("phoneNumber");

            SharedPreferences.Editor editor=getPreferences(context).edit();
            editor.putBoolean(context.getString(R.string.showRating),true);
            editor.putInt(context.getString(R.string.orderId), orderId );
            editor.putString(context.getString(R.string.phoneNumber), phoneNumber).apply();

            Intent intent_broadcaster = new Intent("ShowRating");
            broadcaster.sendBroadcast(intent_broadcaster);




        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(context.getString(R.string.notificationPreference), MODE_PRIVATE);
    }

}
