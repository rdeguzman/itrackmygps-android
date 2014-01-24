package com.twormobile.trackble;

import android.app.Application;
import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class GpsLoggerApplication extends Application {
    private static final String TAG = GpsLoggerApplication.class.getSimpleName();

    public static String LOCATION_NEW_URL;

    private DeviceUUIDFactory uuidFactory;
    private String deviceId;

    private GpsManager gpsManager;
    private boolean mServiceRun;
    private RequestQueue queue;

    @Override
    public void onCreate() {
        initHTTPRequests();

        Log.i(TAG, "onCreated");
        uuidFactory = new DeviceUUIDFactory(getApplicationContext());
        deviceId = uuidFactory.getDeviceUuid().toString();

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

    public String getUUID(){
        return deviceId;
    }

    public void initHTTPRequests(){
        LOCATION_NEW_URL = getResources().getString(R.string.NEW_LOCATION_URL);
    }
}
