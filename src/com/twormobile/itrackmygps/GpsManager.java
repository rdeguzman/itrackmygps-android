package com.twormobile.itrackmygps;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.twormobile.itrackmygps.android.Log;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GpsManager {
    private static final String TAG = "GpsManager";

    public static float KPH = 3.6f;

    // Time Interval in seconds
    private static final int WALKING_TIME_INTERVAL = 10;
    private static final int SLOW_DRIVING_TIME_INTERVAL = 30;
    private static final int MODERATE_DRIVING_TIME_INTERVAL = 60;
    private static final int FAST_DRIVING_TIME_INTERVAL = 120;
    private static final int FIVE_MINUTES = 300;

    // Distance Interval in seconds
    private static final int ZERO_DISTANCE = 0;

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private static GpsManager sGpsManager;
    private static GpsLoggerApplication gpsApp;
    private Context mAppContext;
    private LocationManager mLocationManager;
    private boolean mActive;
    private boolean mGpsFixed;
    private boolean mGpsStatusListenerActive = false;

    private MyLocationListener networkLocationListener;
    private MyLocationListener gpsLocationListener;
    private MyGpsStatusListener gpsStatusListener;

    private String networkProvider;
    private String gpsProvider;

    private ArrayList gpsSatelliteList;     // loop through satellites to get status
    private ArrayList<MyLocationListener> locationListeners = new ArrayList();    // list of location listenres ("network", "gps", etc)
    private int counter = 0;

    private LocationDatabaseHelper mDatabaseHelper;
    private Location currentBestLocation;

    //used for location updates
    private long minTimeInMilliseconds;
    private float minDistanceInMeters;

    // The private constructor forces users to use GpsManager.get(Context)
    private GpsManager(Context appContext) {
        mAppContext = appContext;
        mLocationManager = (LocationManager)mAppContext.getSystemService(Context.LOCATION_SERVICE);
        mDatabaseHelper = new LocationDatabaseHelper(mAppContext);

        networkLocationListener = new MyLocationListener();
        gpsLocationListener = new MyLocationListener();
        gpsStatusListener = new MyGpsStatusListener();

        gpsProvider = LocationManager.GPS_PROVIDER;
        networkProvider = LocationManager.NETWORK_PROVIDER;

        // get time and distance interval from preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mAppContext);
        int minTimeInSeconds = prefs.getInt(SettingsActivity.PREF_TIME_INTERVAL_IN_SECONDS,
                SettingsActivity.DEFAULT_TIME_INTERVAL_IN_SECONDS);
        minTimeInMilliseconds = minTimeInSeconds * 1000L;
        minDistanceInMeters = prefs.getInt(SettingsActivity.PREF_TIME_INTERVAL_IN_METERS,
                SettingsActivity.DEFAULT_DISTANCE_INTERVAL_IN_METERS) * 1.0f;
    }

    public static GpsManager get(Context c) {
        if (sGpsManager == null) {
            // Use the application context to avoid leaking activities
            Log.i(TAG, "onCreated");
            gpsApp = (GpsLoggerApplication)c.getApplicationContext();
            sGpsManager = new GpsManager(gpsApp);
        }
        return sGpsManager;
    }

    public void startLocationUpdates() {
        // Get the last known gps location and broadcast it if you have one
        // If you can't find one then broadcast a network location
        Location lastKnownGPSLocation = mLocationManager.getLastKnownLocation(gpsProvider);
        if (lastKnownGPSLocation != null) {
            currentBestLocation = lastKnownGPSLocation;
            broadcastLocation(lastKnownGPSLocation);
        }
        else{
            Location lastKnownNetworkLocation = mLocationManager.getLastKnownLocation(networkProvider);
            if(lastKnownNetworkLocation != null){
                currentBestLocation = lastKnownNetworkLocation;
                broadcastLocation(lastKnownNetworkLocation);
            }
        }

        startLocationProviders();
        broadcastGpsNetworkStatus();
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

    public void startLocationProviders(){
        // If we have WIFI then it means we are at home or indoors
        if(gpsApp.isWiFiConnected()) {
            startPollingAfterFiveMinutes();
        }
        else {
            startActivePolling();
        }

        mActive = true;
    }

    private void startListenerForProvider(MyLocationListener listener, String provider){
        if(isProviderAllowed(provider) && mLocationManager.isProviderEnabled(provider)){
            if(locationListeners.contains(listener) == false) {
                // http://developer.android.com/reference/android/location/LocationManager.html
                // If it is greater than 0 then the location provider will only send your application an update when the
                // location has changed by at least minDistance meters, AND at least minTime milliseconds have passed.
                mLocationManager.requestLocationUpdates(provider, minTimeInMilliseconds, minDistanceInMeters, listener);
                locationListeners.add(listener);

                if(provider == gpsProvider) {
                    gpsApp.showToast("Interval every " + minTimeInMilliseconds/1000L + " secs and " + minDistanceInMeters + " m");
                }

            }
        }
    }

    public void stopLocationProviders() {
        stopListenerForProvider(networkLocationListener);
        stopListenerForProvider(gpsLocationListener);

        if(mGpsStatusListenerActive) {
            mLocationManager.removeGpsStatusListener(gpsStatusListener);
            mGpsStatusListenerActive = false;
        }

        mActive = false;
        mGpsFixed = false;

        broadcastGpsNetworkStatus();
    }

    private void stopListenerForProvider(LocationListener listener) {
        // Safely removeUpdates for the LocationListener
        if(locationListeners.contains(listener)){
            mLocationManager.removeUpdates(listener);
            locationListeners.remove(listener);
        }
    }

    public void startPollingAfterFiveMinutes() {
        startPolling(FIVE_MINUTES, ZERO_DISTANCE);
    }

    public void startActivePolling() {
        startPolling(WALKING_TIME_INTERVAL, ZERO_DISTANCE);
    }

    private void startPolling(int time_interval, int distance) {
        minTimeInMilliseconds = time_interval * 1000L;
        minDistanceInMeters = distance;

        startListenerForProvider(networkLocationListener, networkProvider);
        startListenerForProvider(gpsLocationListener, gpsProvider);

        if(!mGpsStatusListenerActive) {
            mLocationManager.addGpsStatusListener(gpsStatusListener);
            mGpsStatusListenerActive = true;
        }
    }

    /**
     * Adjust time and distance interval for requestLocationUpdate
     *
     * @param seconds Seconds for time delay.
     * @param meters Meters for distance delay.
     */
    public void adjustGpsUpdateInterval(int seconds, int meters){
        if(isGPSActive()){
            minTimeInMilliseconds = seconds * 1000L;
            minDistanceInMeters = meters * 1.0f;

            stopListenerForProvider(gpsLocationListener);
            startListenerForProvider(gpsLocationListener, gpsProvider);
        }
    }

    /**
     * Returns true if the location listeners are running and getting active updates from onLocationChanged
     *
     */
    public boolean isGPSActive() {
        return mActive;
    }

    /**
     * Broadcast the location to receivers. Afterwards, insert the location to the database and post the location via HTTP.
     *
     * @param location The accepted location from onLocationChanged.
     */
    private void broadcastLocation(Location location) {
        if(location != null){
            counter++;
            Intent broadcast = new Intent(IntentCodes.ACTION_LOCATION);
            broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
            broadcast.putExtra("counter", counter);
            mAppContext.sendBroadcast(broadcast);

            if(connectionStatus() == GpsFix.CONNECTED){
                insertLocation(location);
            }

            postLocation(location);
        }
    }

    private void broadcastGpsNetworkStatus() {
        Intent broadcast = new Intent(IntentCodes.ACTION_GPS_NETWORK_STATUS);
        int index = connectionStatus().ordinal();
        broadcast.putExtra("GPS_NETWORK_STATUS", index);
        mAppContext.sendBroadcast(broadcast);
    }


    public void insertLocation(Location location){
        mDatabaseHelper.insertLocation(location);
    }

    public void postLocation(final Location location){
        if(location == null)
            return;

        final String url = gpsApp.LOCATION_NEW_URL;

        final String timestamp = Long.toString(location.getTime());
        final String latitude = Double.toString(location.getLatitude());
        final String longitude = Double.toString(location.getLongitude());
        final String speedInKPH = Float.toString(location.getSpeed()*KPH);
        final String heading = Float.toString(location.getBearing());
        final String provider = location.getProvider();
        final String timeInterval = Integer.toString((int) minTimeInMilliseconds);
        final String distanceInterval = Integer.toString((int) minDistanceInMeters);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response){
                        VolleyLog.v("Response:%n %s", response);
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Log.d(TAG, "Error on " + url);
                        VolleyLog.e("Error: ", error.getMessage());
                    }
                })
        {

            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("uuid", gpsApp.getUUID());
                params.put("gps_timestamp", timestamp);
                params.put("gps_latitude", latitude);
                params.put("gps_longitude", longitude);
                params.put("gps_speed", speedInKPH);
                params.put("gps_heading", heading);
                params.put("provider", provider);
                params.put("time_interval", timeInterval);
                params.put("distance_interval", distanceInterval);

                return params;
            }
        };

        gpsApp.getVolleyRequestQueue().add(postRequest);
    }

    // Methods in this class are called when the location providers give an update
    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            mActive = true;

            if(isBetterLocation(location, currentBestLocation)){
                currentBestLocation = location;
                broadcastLocation(location);
            }

            // Adjust minTime and minDistance based on Speed
            if(location.getProvider() == LocationManager.GPS_PROVIDER) {

                int seconds = (int) (minTimeInMilliseconds / 1000L);

                boolean mChange = false;

                float speedInKPH = location.getSpeed()*KPH;

                // Speed is slow
                if(speedInKPH >= 20 && speedInKPH < 60) {
                    seconds = SLOW_DRIVING_TIME_INTERVAL;
                    mChange = true;
                }
                else if(speedInKPH >= 60 && speedInKPH < 100) {
                    seconds = MODERATE_DRIVING_TIME_INTERVAL;
                    mChange = true;
                }
                // Speed is high
                else if(speedInKPH > 100) {
                    seconds = FAST_DRIVING_TIME_INTERVAL;
                    mChange = true;
                }

                if(mChange) {
                    adjustGpsUpdateInterval(seconds, (int) minDistanceInMeters);
                }
            }
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
                    gpsApp.showToast("Receive First Fix");
                    stopListenerForProvider(networkLocationListener);
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.d(TAG, "GPS_EVENT_STOPPED");
                    break;
            }
        }
    };

    public boolean isLocationAccessEnabled(){
        return mLocationManager.isProviderEnabled(gpsProvider);
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param bestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location bestLocation) {
        if (bestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - bestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - bestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                bestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            setGpsFixStatusIf(location);
            return true;
        } else if (isNewer && !isLessAccurate) {
            setGpsFixStatusIf(location);
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            setGpsFixStatusIf(location);
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public void updateLocationUpdateSettings(int secs, int meters){
        minTimeInMilliseconds = secs * 1000L;
        minDistanceInMeters = meters * 1.0f;
        if(isGPSActive()){
            stopLocationProviders();
            startLocationProviders();
        }
    }

    public void setGpsFixStatusIf(Location location){
        if(!mGpsFixed && location.getProvider().equals("gps")){
            mGpsFixed = true;
            broadcastGpsNetworkStatus();
        }
    }

    public GpsFix connectionStatus(){
        if(mActive){
            if(mGpsFixed){
                return GpsFix.CONNECTED;
            }
            else{
                return GpsFix.ACQUIRING_FIX;
            }
        }
        else{
            return GpsFix.IDLE;
        }

    }

    public int getTotalSatellites(){
        if(gpsSatelliteList != null) {
            return gpsSatelliteList.size();
        }
        else
            return 0;

    }

}