package com.example.gpslogger;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity implements LocationListener {
    private String gpsProvider;
    private String networkProvider;

    private static final String TAG = "MainActivity";

    private int ctrUpdate = 0;

    private TextView tvGPSCounter;
    private TextView tvGPSLatitude;
    private TextView tvGPSLongitude;
    private TextView tvGPSAltitude;
    private TextView tvGPSBearing;
    private TextView tvGPSSpeed;
    private TextView tvGPSTimestampUTC;
    private TextView tvGPSDateTime;
    private TextView tvGPSAccuracy;
    private TextView tvGPSProvider;

    LocationManager locationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find the textviews
        tvGPSCounter = (TextView)findViewById(R.id.tvGPSCounter);
        tvGPSLatitude = (TextView)findViewById(R.id.tvGPSLatitude);
        tvGPSLongitude = (TextView)findViewById(R.id.tvGPSLongitude);
        tvGPSAltitude = (TextView)findViewById(R.id.tvGPSAltitude);
        tvGPSBearing = (TextView)findViewById(R.id.tvGPSBearing);
        tvGPSSpeed = (TextView)findViewById(R.id.tvGPSSpeed);
        tvGPSTimestampUTC = (TextView)findViewById(R.id.tvGPSTimestampUTC);
        tvGPSDateTime = (TextView)findViewById(R.id.tvGPSTimestamp);
        tvGPSAccuracy = (TextView)findViewById(R.id.tvGPSAccuracy);
        tvGPSProvider = (TextView)findViewById(R.id.tvGPSProvider);

        // get handle for LocationManager
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        gpsProvider = LocationManager.GPS_PROVIDER;
        networkProvider = LocationManager.NETWORK_PROVIDER;

        // connect to the network location service
        Location loc = locationManager.getLastKnownLocation(networkProvider);

        // connect to the GPS location service
        //Location loc = lm.getLastKnownLocation(gpsProvider);

        // fill in the TextViews
        if(loc != null){
            displayGPSDetails(loc);
        }

        locationManager.requestLocationUpdates(gpsProvider, 0, 0, this);
    }

    private void displayGPSDetails(Location location){
        ctrUpdate++;

        tvGPSCounter.setText(Integer.toString(ctrUpdate));
        tvGPSLatitude.setText(Double.toString(location.getLatitude()));
        tvGPSLongitude.setText(Double.toString(location.getLongitude()));
        tvGPSAltitude.setText(Double.toString(location.getAltitude()));
        tvGPSBearing.setText(Float.toString(location.getBearing()));
        tvGPSSpeed.setText(Float.toString(location.getSpeed()));
        tvGPSTimestampUTC.setText(Long.toString(location.getTime()));
        tvGPSAccuracy.setText(Float.toString(location.getAccuracy()));
        tvGPSProvider.setText(location.getProvider());

        String gpsDateTime = CustomDateUtils.timeAgoInWords(this,location.getTime());
        tvGPSDateTime.setText(gpsDateTime);
    }

    @Override
    public void onLocationChanged(Location location) {
        displayGPSDetails(location);
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

    public void onPause(){
        super.onPause(); // Always call the superclass method first
        Log.i(TAG, "paused");

        locationManager.removeUpdates(this);
    }

    public void onResume(){
        super.onResume();  // Always call the superclass method first

        Log.i(TAG, "resume");
        locationManager.requestLocationUpdates(gpsProvider, 0, 0, this);
    }

}
