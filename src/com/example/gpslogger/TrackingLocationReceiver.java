package com.example.gpslogger;

import android.content.Context;
import android.location.Location;
import android.util.Log;

public class TrackingLocationReceiver extends LocationReceiver {
    private static final String TAG = "TrackingLocationReceiver";


    protected void onLocationReceived(Context c, Location loc){
        RunManager.get(c).insertLocation(loc);
        Log.d(TAG, "onLocationReceived: lat:" + loc.getLatitude() + " lon:" + loc.getLongitude());
    }
}
