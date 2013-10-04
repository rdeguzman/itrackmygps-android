package com.twormobile.gpslogger;

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

    private MyLocationListener networkLocationListener;
    private MyLocationListener gpsLocationListener;
    private MyGpsStatusListener gpsStatusListener;

    private String networkProvider;
    private String gpsProvider;

    private ArrayList gpsSatelliteList;     // loop through satellites to get status
    private ArrayList<MyLocationListener> locationListeners = new ArrayList();    // list of location listenres ("network", "gps", etc)
    private int counter = 0;

    private LocationDatabaseHelper mDatabaseHelper;

    // The private constructor forces users to use RunManager.get(Context)
    private RunManager(Context appContext) {
        mAppContext = appContext;
        mLocationManager = (LocationManager)mAppContext.getSystemService(Context.LOCATION_SERVICE);
        mDatabaseHelper = new LocationDatabaseHelper(mAppContext);

        networkLocationListener = new MyLocationListener();
        gpsLocationListener = new MyLocationListener();
        gpsStatusListener = new MyGpsStatusListener();
    }

    public static RunManager get(Context c) {
        if (sRunManager == null) {
            // Use the application context to avoid leaking activities
            sRunManager = new RunManager(c.getApplicationContext());
        }
        return sRunManager;
    }

    public void startLocationUpdates() {
        gpsProvider = LocationManager.GPS_PROVIDER;
        networkProvider = LocationManager.NETWORK_PROVIDER;

        // Get the last known gps location and broadcast it if you have one
        // If you can't find one then broadcast a network location
        Location lastKnownGPSLocation = mLocationManager.getLastKnownLocation(gpsProvider);
        if (lastKnownGPSLocation != null) {
            broadcastLocation(lastKnownGPSLocation);
        }
        else{
            Location lastKnownNetworkLocation = mLocationManager.getLastKnownLocation(networkProvider);
            if(lastKnownNetworkLocation != null)
                broadcastLocation(lastKnownNetworkLocation);
        }

        // Here we check for "network", "gps" in providers and start them if they are available
        // Note that "network" is not available in the emulator
        startLocationListener(networkLocationListener, networkProvider);
        startLocationListener(gpsLocationListener, gpsProvider);
        mLocationManager.addGpsStatusListener(gpsStatusListener);

        mRunning = true;
    }

    private boolean isProviderAllowed(String s){
        boolean flag = false;
        for(String provider : mLocationManager.getAllProviders()){
            if(provider.contains(s)){
                flag = true;
                break;
            }
        }

        return flag;
    }

    private void startLocationListener(MyLocationListener listener, String provider){
        if(isProviderAllowed(provider) && mLocationManager.isProviderEnabled(provider)){
            mLocationManager.requestLocationUpdates(provider, 0, 0, listener);
            locationListeners.add(listener);
        }
    }

    public void stopLocationUpdates() {
        // Synchronized because there may be multiple listeners running and
        // we don't want them to both try to alter the listeners collection
        // at the same time.
        synchronized (locationListeners) {
            for (MyLocationListener listener : locationListeners) {
                mLocationManager.removeUpdates(listener);
                locationListeners.remove(listener);
            }
        }

        mLocationManager.removeGpsStatusListener(gpsStatusListener);
        mRunning = false;
    }

    public boolean isTrackingRun() {
        return mRunning;
    }

    private void broadcastLocation(Location location) {
        insertLocation(location);

        counter++;
        Intent broadcast = new Intent(ACTION_LOCATION);
        broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
        broadcast.putExtra("counter", counter);
        mAppContext.sendBroadcast(broadcast);
    }

    public void insertLocation(Location location){
        mDatabaseHelper.insertLocation(location);
    }

    // Methods in this class are called when the location providers give an update
    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            mRunning = true;
            broadcastLocation(location);
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
    private class MyGpsStatusListener implements GpsStatus.Listener {
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
                    Log.d(TAG, "GPS_EVENT_SATELLITE_STATUS");
                    satelliteStatusUpdate();
                    break;
                case GpsStatus.GPS_EVENT_STARTED:
                    Log.d(TAG, "GPS_EVENT_STARTED");
                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.d(TAG, "GPS_EVENT_FIRST_FIX");
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.d(TAG, "GPS_EVENT_STOPPED");
                    break;
            }
        }
    };

}