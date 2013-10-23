package com.twormobile.gpslogger;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class GpsLoggerService extends Service {
    private static final String TAG = GpsLoggerService.class.getSimpleName();

    private GpsLoggerApplication gpssapp;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.gpssapp = (GpsLoggerApplication)getApplication();

        Log.d(TAG, "onCreated");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStarted");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroyed");
    }
}
