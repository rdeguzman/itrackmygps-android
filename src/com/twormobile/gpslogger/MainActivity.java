package com.twormobile.gpslogger;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.res.Configuration;
import android.location.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    public static final int ENTRY_SETTINGS = 10;

    private GpsManager gpsManager;
    private Button btnStart;
    private Button btnStop;

    private int ctrUpdate = 0;

    private TableLayout viewTableLayout;
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

    private int mapLayer;
    private float currentZoom; //tracks the current zoom of the map
    private boolean isZoomBasedOnSpeed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpsManager = GpsManager.get(getApplicationContext());
        if(!gpsManager.isLocationAccessEnabled()){
            displayLocationAccessDialog();
        }

        //find the view layouts
        viewTableLayout = (TableLayout)findViewById(R.id.table_layout);

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

        updateFromPreferences();
    }

    private void updateFromPreferences() {
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // we add 1 since GoogleMap.MAP_TYPE_NORMAL starts at 1
        mapLayer = prefs.getInt(SettingsActivity.PREF_MAP_LAYER_INDEX, 0) + 1;
        isZoomBasedOnSpeed = prefs.getBoolean(SettingsActivity.PREF_ZOOM_BASED_ON_SPEED, true);

        if(gmap != null){
            gmap.setMapType(mapLayer);
        }
    }

    public void buttonStartPressed(View view){
        Log.i(TAG, "buttonStartPressed");
        gpsManager.startLocationUpdates();
        updateButtons();
    }

    public void buttonStopPressed(View view){
        Log.i(TAG, "buttonStopPressed");
        gpsManager.stopLocationUpdates();
        updateButtons();
    }

    private void updateButtons() {
        boolean started = gpsManager.isTrackingRun();

        btnStart.setEnabled(!started);
        btnStop.setEnabled(started);
    }

    private BroadcastReceiver mLocationReceiver = new LocationReceiver() {

        @Override
        protected void onLocationReceived(Context context, Location loc, int ctr) {
            displayGPSDetails(loc, ctr);

            if(gmap != null){
                LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());
                float maxZoom = gmap.getMaxZoomLevel();

                if(marker == null){
                    MarkerOptions markerOptions = new MarkerOptions().position(pos);
                    marker = gmap.addMarker(markerOptions);
                }

                marker.setPosition(pos);

                // If this is the first fix, zoom to the new position
                if(firstFix){
                    firstFix = false;
                    currentZoom = maxZoom/2.0f;
                    gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, currentZoom));
                }
                else {
                    // Dynamic zoom based on speed
                    if(isZoomBasedOnSpeed){
                        int speed = (int)loc.getSpeed();

                        boolean isMoving = speed > 1;
                        boolean isSpeedSlow = isBetween(speed, 10, 40);
                        boolean isSpeedModerate = isBetween(speed, 41, 60);
                        boolean isSpeedQuiteFast = isBetween(speed, 61, 80);
                        boolean isSpeedFast = speed > 81;

                        if(isSpeedSlow){
                            currentZoom = maxZoom - 3.0f; //zoom = 18
                        }
                        else if(isSpeedModerate){
                            currentZoom = maxZoom - 4.0f; //zoom = 17
                        }
                        else if(isSpeedQuiteFast){
                            currentZoom = maxZoom - 5.0f; //zoom = 16
                        }
                        else if(isSpeedFast){
                            currentZoom = maxZoom - 6.0f; //zoom = 15
                        }
                        else{ //crawling
                            currentZoom = maxZoom - 2.0f; //zoom = 19
                        }

                        if(isMoving){
                            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, currentZoom));
                        }
                        else{
                            if(MapUtils.isLatLngNotVisible(pos, gmap)){
                                gmap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                                return;
                            }
                        }
                    }

                    if(MapUtils.isLatLngNotVisible(pos, gmap)){
                        gmap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                        return;
                    }


                }
            }
        }

    };

    @Override
    public void onStart() {
        super.onStart();
        this.registerReceiver(mLocationReceiver, new IntentFilter(GpsManager.ACTION_LOCATION));
    }

    @Override
    public void onStop() {
        this.unregisterReceiver(mLocationReceiver);
        super.onStop();
    }

    private void displayGPSDetails(Location location, int ctr) {
        ctrUpdate++;
        String tmpZoom = " Z:" + Float.toString(gmap.getCameraPosition().zoom);

        tvGPSCounter.setText(Integer.toString(ctr) + tmpZoom);
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
        updateFromPreferences();
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
                displaySettingsActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displaySettingsActivity(){
        Log.d(TAG, "displaySettingsActivity");
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, ENTRY_SETTINGS);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "Switched to Landscape", Toast.LENGTH_SHORT).show();
            viewTableLayout.setVisibility(View.INVISIBLE);

            TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 0);
            viewTableLayout.setLayoutParams(params);

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){

            Toast.makeText(this, "Switched to Portrait", Toast.LENGTH_SHORT).show();

            viewTableLayout.setVisibility(View.VISIBLE);

            TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            viewTableLayout.setLayoutParams(params);
        }
    }

    private void displayLocationAccessDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setMessage(R.string.gps_network_not_enabled);
        dialog.setPositiveButton(R.string.enable, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
            }
        });
        dialog.show();
    }

    private boolean isBetween(int x, int lower, int upper) {
        return lower <= x && x <= upper;
    }

}
