package com.twormobile.trackble;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class GpsLoggerApplication extends Application {
    private static final String TAG = GpsLoggerApplication.class.getSimpleName();

    public static String LOCATION_NEW_URL;

    private DeviceUUIDFactory uuidFactory;
    private String deviceId;

    private GpsManager gpsManager;
    private boolean bServiceRun;
    private RequestQueue queue;

    private SharedPreferences prefs;

    private boolean bLoggedIn = false;
    private String username;

    @Override
    public void onCreate() {
        setURLs();

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        checkUser();

        Log.i(TAG, "onCreated");
        uuidFactory = new DeviceUUIDFactory(getApplicationContext());
        deviceId = uuidFactory.getDeviceUuid().toString();

        gpsManager = GpsManager.get(getApplicationContext());
        queue = Volley.newRequestQueue(this);
        bServiceRun = false;
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
        return bServiceRun;
    }

    public void setServiceRunning(boolean b){
        bServiceRun = b;
    }

    public String getUUID(){
        return deviceId;
    }

    public void setURLs(){
        LOCATION_NEW_URL = getResources().getString(R.string.NEW_LOCATION_URL);
    }

    public boolean isLoggedIn() {
        return bLoggedIn;
    }

    public String getUsername() { return username; }

    private void checkUser() {
        username = prefs.getString("username", null);
        if(username == null) {
            bLoggedIn = false;
        }
        else {
            bLoggedIn = true;
        }
    }
}
