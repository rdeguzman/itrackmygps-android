package com.twormobile.gpslogger;

import android.app.Application;
import android.util.Log;

public class GpsLoggerApplication extends Application {
    private static final String TAG = GpsLoggerApplication.class.getSimpleName();
    private GpsManager gpsManager;
    private boolean mServiceRun;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreated");
        gpsManager = GpsManager.get(getApplicationContext());
        mServiceRun = false;
    }

    @Override
    public void onTerminate(){
        Log.i(TAG, "onTerminated");
    }

    public GpsManager getGpsManager() {
        return gpsManager;
    }

    public boolean isServiceRunning(){
        return mServiceRun;
    }

    public void setServiceRunning(boolean b){
        mServiceRun = b;
    }
}
