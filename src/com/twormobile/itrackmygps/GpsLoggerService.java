package com.twormobile.itrackmygps;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import com.twormobile.itrackmygps.android.Log;

public class GpsLoggerService extends Service {
    private static final String TAG = GpsLoggerService.class.getSimpleName();

    private GpsLoggerApplication gpsApp;
    private GpsManager gpsManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.gpsApp = (GpsLoggerApplication)getApplication();
        this.gpsManager = gpsApp.getGpsManager();

        Log.d(TAG, "onCreated");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStarted");

        start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroyed");

        stop();
    }

    private void start(){
        if(!gpsApp.isON() && !gpsManager.isGPSActive()){
            gpsApp.setON(true);
            gpsManager.startLocationUpdates();
        }
    }

    private void stop() {
        if(gpsApp.isON() && gpsManager.isGPSActive()){
            gpsApp.setON(false);
            gpsManager.stopLocationProviders();
        }
    }

}
