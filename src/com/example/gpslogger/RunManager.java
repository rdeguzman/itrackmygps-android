package com.example.gpslogger;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.*;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

public class RunManager {
    private static final String TAG = "RunManager";

    public static final String ACTION_LOCATION = "com.example.gpslogger.ACTION_LOCATION";

    private static RunManager sRunManager;
    private Context mAppContext;
    private LocationManager mLocationManager;
    private boolean mRunning;

    private MyLocationListener gpsLocationListener;
    private MyGpsStatusListener gpsStatusListener;

    private String gpsProvider;
    private ArrayList gpsSatelliteList; // loop through satellites to get status

    // The private constructor forces users to use RunManager.get(Context)
    private RunManager(Context appContext) {
        mAppContext = appContext;
        mLocationManager = (LocationManager)mAppContext.getSystemService(Context.LOCATION_SERVICE);
    }

    public static RunManager get(Context c) {
        if (sRunManager == null) {
            // Use the application context to avoid leaking activities
            sRunManager = new RunManager(c.getApplicationContext());
        }
        return sRunManager;
    }

//    private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
//        Log.d(TAG, "getLocationPendingIntent");
//        Intent broadcast = new Intent(ACTION_LOCATION);
//        int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
//        return PendingIntent.getBroadcast(mAppContext, 0, broadcast, flags);
//    }

    public void startLocationUpdates() {
        gpsProvider = LocationManager.GPS_PROVIDER;

        // Get the last known location and broadcast it if you have one
        Location lastKnown = mLocationManager.getLastKnownLocation(gpsProvider);
        if (lastKnown != null) {
            broadcastLocation(lastKnown);
        }

        // Start updates from the location manager
//        PendingIntent pi = getLocationPendingIntent(true);

        gpsLocationListener = new MyLocationListener();
        mLocationManager.requestLocationUpdates(gpsProvider, 0, 0, gpsLocationListener);

        MyGpsStatusListener gpsStatusListener = new MyGpsStatusListener();
        mLocationManager.addGpsStatusListener(gpsStatusListener);

        mRunning = true;
    }

    public void stopLocationUpdates() {
//        PendingIntent pi = getLocationPendingIntent(false);
//        if (pi != null) {
//            mLocationManager.removeUpdates(pi);
//            pi.cancel();
//        }

        mLocationManager.removeUpdates(gpsLocationListener);
        mLocationManager.removeGpsStatusListener(gpsStatusListener);
        mRunning = false;
    }

    public boolean isTrackingRun() {
        return mRunning;
    }

    private void broadcastLocation(Location location) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
        mAppContext.sendBroadcast(broadcast);
    }

    // Methods in this class are called when the location providers give an update
    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            broadcastLocation(location);
            mRunning = true;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    // Methods in this class are called when the status of the GPS changes
    private class MyGpsStatusListener implements GpsStatus.Listener
    {
        // called to handle an event updating the satellite status
        private void satelliteStatusUpdate() {
            // use the location manager to get a gps status object
            // this method should only be called inside GpsStatus.Listener
            GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);

            // create an iterator to loop through list of satellites
            Iterable<GpsSatellite> iSatellites = gpsStatus.getSatellites();
            Iterator<GpsSatellite> gpsSatelliteIterator = iSatellites.iterator();

            // find the satellite with the best (greatest signal to noise ratio to update display
            // and save list of satellites in an ArrayList
            gpsSatelliteList = new ArrayList<GpsSatellite>();

            while (gpsSatelliteIterator.hasNext()){
                // get next satellite from iterator
                GpsSatellite s = (GpsSatellite) gpsSatelliteIterator.next();
                // and add to ArrayList
                gpsSatelliteList.add(s);
            }

        }

        // the status of the GPS has changed
        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    Log.i(TAG, "GPS_EVENT_SATELLITE_STATUS");
                    satelliteStatusUpdate();
                    break;
                case GpsStatus.GPS_EVENT_STARTED:
                    Log.i(TAG, "GPS_EVENT_STARTED");
                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.i(TAG, "GPS_EVENT_FIRST_FIX");
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.i(TAG, "GPS_EVENT_STOPPED");
                    break;
            }
        }
    };

}