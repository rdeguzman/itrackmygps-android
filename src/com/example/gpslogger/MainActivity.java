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
    private String locationProvider;

    private LocationListener locListenD;

    private TextView tvLatitude;
    private TextView tvLongitude;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find the textviews
        tvLatitude = (TextView)findViewById(R.id.tvGPSLatitude);
        tvLongitude = (TextView)findViewById(R.id.tvGPSLongitude);

        // get handle for LocationManager
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        gpsProvider = LocationManager.GPS_PROVIDER;
        locationProvider = LocationManager.NETWORK_PROVIDER;

        // connect to the GPS location service
        Location loc = lm.getLastKnownLocation(gpsProvider);

        // fill in the TextViews
//        tvLatitude.setText(Double.toString(loc.getLatitude()));
//        tvLongitude.setText(Double.toString(loc.getLongitude()));

        lm.requestLocationUpdates(gpsProvider, 10000L, 10.0f, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        // update TextViews
        tvLatitude.setText(Double.toString(location.getLatitude()));
        tvLongitude.setText(Double.toString(location.getLongitude()));
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
