package com.yonatan.sonora.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * מקלט שידורים (BroadcastReceiver) של אפליקציית SONORA.
 * עונה על סעיף 10.1 במחוון הבגרות ("Messaging / BroadcastReceiver").
 * מאזין לשינויי חיבור לאינטרנט ופעולות סנכרון מערכתיות.
 *
 * System and custom broadcast receiver.
 */
public class SonoraBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_CUSTOM_SYNC = "com.yonatan.sonora.ACTION_CUSTOM_SYNC";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();

        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action) || ACTION_CUSTOM_SYNC.equals(action)) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

                if (isConnected) {
                    // הפעלת שירות הסנכרון כאשר התחברות הרשת פעילה
                    Intent serviceIntent = new Intent(context, SonoraSyncService.class);
                    serviceIntent.setAction(SonoraSyncService.ACTION_SYNC_NOW);
                    try {
                        context.startService(serviceIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
