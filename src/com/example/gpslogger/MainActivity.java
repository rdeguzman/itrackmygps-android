package com.example.gpslogger;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity implements LocationListener {
    private String gpsProvider;
    private String networkProvider;

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private LocationListener locListenD;

    private TextView tvGPSLatitude;
    private TextView tvGPSLongitude;
    private TextView tvGPSAltitude;
    private TextView tvGPSBearing;
    private TextView tvGPSSpeed;
    private TextView tvGPSTimestamp;
    private TextView tvGPSAccuracy;
    private TextView tvGPSProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find the textviews
        tvGPSLatitude = (TextView)findViewById(R.id.tvGPSLatitude);
        tvGPSLongitude = (TextView)findViewById(R.id.tvGPSLongitude);
        tvGPSAltitude = (TextView)findViewById(R.id.tvGPSAltitude);
        tvGPSBearing = (TextView)findViewById(R.id.tvGPSBearing);
        tvGPSSpeed = (TextView)findViewById(R.id.tvGPSSpeed);
        tvGPSTimestamp = (TextView)findViewById(R.id.tvGPSTimestamp);
        tvGPSAccuracy = (TextView)findViewById(R.id.tvGPSAccuracy);
        tvGPSProvider = (TextView)findViewById(R.id.tvGPSProvider);

        // get handle for LocationManager
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        gpsProvider = LocationManager.GPS_PROVIDER;
        networkProvider = LocationManager.NETWORK_PROVIDER;

        // connect to the network location service
        Location loc = lm.getLastKnownLocation(networkProvider);

        // connect to the GPS location service
        //Location loc = lm.getLastKnownLocation(gpsProvider);

        // fill in the TextViews
        if(loc != null){
            displayGPSDetails(loc);
        }

        lm.requestLocationUpdates(gpsProvider, 10000L, 10.0f, this);
    }

    private void displayGPSDetails(Location location){
        tvGPSLatitude.setText(Double.toString(location.getLatitude()));
        tvGPSLongitude.setText(Double.toString(location.getLongitude()));
        tvGPSAltitude.setText(Double.toString(location.getAltitude()));
        tvGPSBearing.setText(Float.toString(location.getBearing()));
        tvGPSSpeed.setText(Float.toString(location.getSpeed()));
        tvGPSTimestamp.setText(Long.toString(location.getTime()));
        tvGPSAccuracy.setText(Float.toString(location.getAccuracy()));
        tvGPSProvider.setText(location.getProvider());
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


}
