package com.twormobile.trackble;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
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

public class MainActivity extends Activity{
    private static final String TAG = "MainActivity";
    public static final int ENTRY_SETTINGS = 10;

    private int ctrUpdate = 0;

    private ToggleButton toggleBtnService;
    private ImageView ivGpsFixStatus;
    private TextView tvGpsFixStatus;

    private TableLayout viewGPSLayout;
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

    private GoogleMap gmap;
    private boolean firstFix = true;
    private Marker marker;

    private int mapLayer;
    private float currentZoom; //tracks the current zoom of the map
    private boolean isZoomBasedOnSpeed;

    private GpsLoggerApplication gpsApp;
    private GpsManager gpsManager;
    private GpsConnectionStatusReceiver mGpsNetworkStatusReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.gpsApp = (GpsLoggerApplication)getApplication();
        this.gpsManager = gpsApp.getGpsManager();
        if(!gpsManager.isLocationAccessEnabled()){
            displayLocationAccessDialog();
        }

        toggleBtnService = (ToggleButton)findViewById(R.id.btn_toggle_tracker_status);
        ivGpsFixStatus = (ImageView)findViewById(R.id.iv_gps_status);
        tvGpsFixStatus = (TextView)findViewById(R.id.tv_gps_fix_status);

        //find the view layouts
        viewGPSLayout = (TableLayout)findViewById(R.id.gps_status_layout);

        //find the textviews
        tvGPSCounter = (TextView)findViewById(R.id.tv_gps_counter);
        tvGPSLatitude = (TextView)findViewById(R.id.tv_gps_latitude);
        tvGPSLongitude = (TextView)findViewById(R.id.tv_gps_longitude);
        tvGPSAltitude = (TextView)findViewById(R.id.tv_gps_altitude);
        tvGPSBearing = (TextView)findViewById(R.id.tv_gps_bearing);
        tvGPSSpeed = (TextView)findViewById(R.id.tv_gps_speed);
        tvGPSDateTime = (TextView)findViewById(R.id.tv_gps_timestamp);
        tvGPSAccuracy = (TextView)findViewById(R.id.tv_gps_accuracy);
        tvGPSProvider = (TextView)findViewById(R.id.tv_gps_provider);
        tvGPSTotalSatellites = (TextView)findViewById(R.id.tv_gps_fix_total_satellites);

        ActionBar actionBar = getActionBar();
        actionBar.show();

        gmap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapview)).getMap();

        mGpsNetworkStatusReceiver = new GpsConnectionStatusReceiver();

        showGPSStatus(false);
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

        int minTimeInSeconds = prefs.getInt(SettingsActivity.PREF_TIME_INTERVAL_IN_SECONDS,
                SettingsActivity.DEFAULT_TIME_INTERVAL_IN_SECONDS);
        int minDistanceInMeters = prefs.getInt(SettingsActivity.PREF_TIME_INTERVAL_IN_METERS,
                SettingsActivity.DEFAULT_DISTANCE_INTERVAL_IN_METERS);
        gpsManager.updateLocationUpdateSettings(minTimeInSeconds, minDistanceInMeters);
    }

    public void updateToggleButtonService(){
        toggleBtnService.setChecked(gpsApp.isServiceRunning());
    }

    public void onToggleClicked(View view) {
        // Is the toggle on?
        boolean on = ((ToggleButton) view).isChecked();

        if (on) {
            Log.i(TAG, "buttonStartPressed");
            showGPSStatus(true);

            startService(new Intent(this, GpsLoggerService.class));
        } else {
            Log.i(TAG, "buttonStopPressed");
            showGPSStatus(false);

            stopService(new Intent(this, GpsLoggerService.class));
        }
    }

    private void showGPSStatus(boolean f){
        if(f) {
            viewGPSLayout.setVisibility(View.VISIBLE);
            ivGpsFixStatus.setVisibility(View.VISIBLE);
        }
        else {
            viewGPSLayout.setVisibility(View.INVISIBLE);
            ivGpsFixStatus.setVisibility(View.INVISIBLE);
        }
    }

    public class GpsConnectionStatusReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            Log.d("GpsConnectionStatusReceiver", "onReceived");
            updateGpsFixConnectionStatus();
        }
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

    private void updateGpsFixConnectionStatus() {
        GpsFix status = gpsManager.connectionStatus();
        ivGpsFixStatus.setImageResource(status.icon());
        tvGpsFixStatus.setText(status.toString());
    }

    @Override
    public void onStart() {
        super.onStart();
        this.registerReceiver(mLocationReceiver, new IntentFilter(GpsManager.ACTION_LOCATION));
        this.registerReceiver(mGpsNetworkStatusReceiver, new IntentFilter(GpsManager.ACTION_GPS_NETWORK_STATUS));
    }

    @Override
    public void onStop() {
        this.unregisterReceiver(mLocationReceiver);
        this.unregisterReceiver(mGpsNetworkStatusReceiver);
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
        int satellitesTotal = gpsManager.getTotalSatellites();
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
        updateToggleButtonService();
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
