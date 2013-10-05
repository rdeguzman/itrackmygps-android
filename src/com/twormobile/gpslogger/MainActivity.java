package com.twormobile.gpslogger;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.location.*;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private RunManager runManager;
    private Button btnStart;
    private Button btnStop;

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

    private ArrayList gpsSatelliteList; // loop through satellites to get status

    private GoogleMap gmap;
    private boolean firstFix = true;
    private Marker marker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        runManager = RunManager.get(getApplicationContext());

        //find the buttons
        btnStart = (Button)findViewById(R.id.btn_start);
        btnStop = (Button)findViewById(R.id.btn_stop);

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
        tvGPSTotalSatellites = (TextView)findViewById(R.id.tvGPSFixTotalSatellites);

        ActionBar actionBar = getActionBar();
        actionBar.show();

        gpsSatelliteList = new ArrayList<GpsSatellite>();

        gmap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapview)).getMap();
        if(gmap != null){
            gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    public void buttonStartPressed(View view){
        Log.i(TAG, "buttonStartPressed");
        runManager.startLocationUpdates();
        updateButtons();
    }

    public void buttonStopPressed(View view){
        Log.i(TAG, "buttonStopPressed");
        runManager.stopLocationUpdates();
        updateButtons();
    }

    private void updateButtons() {
        boolean started = runManager.isTrackingRun();

        btnStart.setEnabled(!started);
        btnStop.setEnabled(started);
    }

    private BroadcastReceiver mLocationReceiver = new LocationReceiver() {

        @Override
        protected void onLocationReceived(Context context, Location loc, int ctr) {
            displayGPSDetails(loc, ctr);

            if(gmap != null){
                LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());

                if(marker == null){
                    MarkerOptions markerOptions = new MarkerOptions().position(pos);
                    marker = gmap.addMarker(markerOptions);
                }

                marker.setPosition(pos);

                // If this is the first fix, zoom to the new position
                if(firstFix){
                    firstFix = false;
                    float maxZoom = gmap.getMaxZoomLevel()/2.0f;
                    gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, maxZoom));
                }
                else {
                    // If the new location is not within the map move to the new position.
                    if(MapUtils.isLatLngVisible(gmap, pos) == false){
                        gmap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                    }

                }
            }
        }

    };

    @Override
    public void onStart() {
        super.onStart();
        this.registerReceiver(mLocationReceiver,new IntentFilter(RunManager.ACTION_LOCATION));
    }

    @Override
    public void onStop() {
        this.unregisterReceiver(mLocationReceiver);
        super.onStop();
    }

    private void displayGPSDetails(Location location, int ctr) {
        ctrUpdate++;

        tvGPSCounter.setText(Integer.toString(ctr));
        tvGPSLatitude.setText(Double.toString(location.getLatitude()));
        tvGPSLongitude.setText(Double.toString(location.getLongitude()));
        tvGPSAltitude.setText(Double.toString(location.getAltitude()));
        tvGPSBearing.setText(Float.toString(location.getBearing()));
        tvGPSSpeed.setText(Float.toString(location.getSpeed()));
        tvGPSAccuracy.setText(Float.toString(location.getAccuracy()));
        tvGPSProvider.setText(location.getProvider());

        String gpsDateTime = CustomDateUtils.formatDateTimestamp(location.getTime());
        tvGPSDateTime.setText(gpsDateTime);

        int satellitesWithFix = location.getExtras().getInt("satellites");
        int satellitesTotal = gpsSatelliteList.size();
        String s = Integer.toString(satellitesWithFix) + "/" + satellitesTotal;
        tvGPSTotalSatellites.setText(s);
    }

    public void onPause() {
        super.onPause(); // Always call the superclass method first
        Log.d(TAG, "paused");
    }

    public void onResume() {
        super.onResume();  // Always call the superclass method first

        Log.d(TAG, "resume");
        updateButtons();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
                startSettingsActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startSettingsActivity(){
        Log.d(TAG, "startSettingsActivity");
    }

}
