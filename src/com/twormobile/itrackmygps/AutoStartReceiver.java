package com.twormobile.itrackmygps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.twormobile.itrackmygps.android.Log;

public class AutoStartReceiver extends BroadcastReceiver {

    private static final String TAG = "AutoStartReceiver";

    @Override
    public void onReceive(Context context, Intent arg1)
    {
        Intent intent = new Intent(context, GpsLoggerService.class);
        context.startService(intent);
        Log.i(TAG, "started");
    }

}