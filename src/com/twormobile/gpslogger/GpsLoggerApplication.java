package com.twormobile.gpslogger;

import android.app.Application;
import android.util.Log;

public class GpsLoggerApplication extends Application {
    private static final String TAG = GpsLoggerApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreated");
    }

    @Override
    public void onTerminate(){
        Log.i(TAG, "onTerminated");
    }

}
