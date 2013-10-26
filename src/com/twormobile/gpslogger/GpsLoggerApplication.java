package com.twormobile.gpslogger;

import android.app.Application;
import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class GpsLoggerApplication extends Application {
    private static final String TAG = GpsLoggerApplication.class.getSimpleName();
    public static final String LOCATION_NEW_URL = "http://192.168.0.16:3000/locations/";

    private GpsManager gpsManager;
    private boolean mServiceRun;
    private RequestQueue queue;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreated");
        gpsManager = GpsManager.get(getApplicationContext());
        queue = Volley.newRequestQueue(this);
        mServiceRun = false;
    }

    @Override
    public void onTerminate(){
        Log.i(TAG, "onTerminated");
    }

    public GpsManager getGpsManager() {
        return gpsManager;
    }

    public RequestQueue getVolleyRequestQueue() {
        return queue;
    }

    public boolean isServiceRunning(){
        return mServiceRun;
    }

    public void setServiceRunning(boolean b){
        mServiceRun = b;
    }
}
