package com.example.gpslogger;

import android.app.Activity;
import android.content.Context;
import android.location.*;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends Activity {
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
    private TextView tvGPSDateTime;
    private TextView tvGPSAccuracy;
    private TextView tvGPSProvider;
    private TextView tvGPSTotalSatellites;

    private LocationManager locationManager;
    private MyLocationListener gpslocationListener;

    private ArrayList gpsSatelliteList; // loop through satellites to get status
    private ListView lvSatellites;
    private SatteliteListAdapter satelliteAdapter;

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
        tvGPSDateTime = (TextView)findViewById(R.id.tvGPSTimestamp);
        tvGPSAccuracy = (TextView)findViewById(R.id.tvGPSAccuracy);
        tvGPSProvider = (TextView)findViewById(R.id.tvGPSProvider);
        tvGPSTotalSatellites = (TextView)findViewById(R.id.tvGPSTotalSatellites);

        lvSatellites = (ListView)findViewById(R.id.lv_satellites);
        gpsSatelliteList = new ArrayList<GpsSatellite>();
        satelliteAdapter = new SatteliteListAdapter(this, gpsSatelliteList);
        lvSatellites.setAdapter(satelliteAdapter);

        // get handle for LocationManager
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        gpsProvider = LocationManager.GPS_PROVIDER;
        networkProvider = LocationManager.NETWORK_PROVIDER;

        MyGpsStatusListener gpsStatusListener = new MyGpsStatusListener();
        locationManager.addGpsStatusListener(gpsStatusListener);

        // connect to the network location service
        Location loc = locationManager.getLastKnownLocation(networkProvider);

        if(loc != null){
            displayGPSDetails(loc);
        }

        gpslocationListener = new MyLocationListener();
        locationManager.requestLocationUpdates(gpsProvider, 0, 0, gpslocationListener);
    }

    private void displayGPSDetails(Location location) {
        ctrUpdate++;

        tvGPSCounter.setText(Integer.toString(ctrUpdate));
        tvGPSLatitude.setText(Double.toString(location.getLatitude()));
        tvGPSLongitude.setText(Double.toString(location.getLongitude()));
        tvGPSAltitude.setText(Double.toString(location.getAltitude()));
        tvGPSBearing.setText(Float.toString(location.getBearing()));
        tvGPSSpeed.setText(Float.toString(location.getSpeed()));
        tvGPSAccuracy.setText(Float.toString(location.getAccuracy()));
        tvGPSProvider.setText(location.getProvider());

        String gpsDateTime = CustomDateUtils.formatDateTimestamp(location.getTime());
        tvGPSDateTime.setText(gpsDateTime);

        int totalSatellites = location.getExtras().getInt("satellites");
        tvGPSTotalSatellites.setText(Integer.toString(totalSatellites));
    }

    public void onPause() {
        super.onPause(); // Always call the superclass method first
        Log.i(TAG, "paused");

        locationManager.removeUpdates(gpslocationListener);
    }

    public void onResume() {
        super.onResume();  // Always call the superclass method first

        Log.i(TAG, "resume");
        locationManager.requestLocationUpdates(gpsProvider, 0, 0, gpslocationListener);
    }

    // Methods in this class are called when the location providers give an update
    private class MyLocationListener implements LocationListener{
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

    // Methods in this class are called when the status of the GPS changes
    private class MyGpsStatusListener implements GpsStatus.Listener
    {
        // called to handle an event updating the satellite status
        private void satelliteStatusUpdate() {
            // use the location manager to get a gps status object
            // this method should only be called inside GpsStatus.Listener
            GpsStatus gpsStatus = locationManager.getGpsStatus(null);

            // create an iterator to loop through list of satellites
            Iterable<GpsSatellite> iSatellites = gpsStatus.getSatellites();
            Iterator<GpsSatellite> gpsSatelliteIterator = iSatellites.iterator();

            // find the satellite with the best (greatest signal to noise ratio to update display
            // and save list of satellites in an ArrayList
            gpsSatelliteList.clear();
            
            while (gpsSatelliteIterator.hasNext()){
                // get next satellite from iterator
                GpsSatellite s = (GpsSatellite) gpsSatelliteIterator.next();
                // and add to ArrayList
                gpsSatelliteList.add(s);
            }

            satelliteAdapter.notifyDataSetChanged();
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
