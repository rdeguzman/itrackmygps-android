package com.twormobile.gpslogger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import com.google.android.gms.maps.GoogleMap;

public class SettingsActivity extends Activity {
    private static final String TAG = "MainActivity";
    public static final String PREF_MAP_LAYER_INDEX = "PREF_MAP_LAYER_INDEX";
    public static final String PREF_ZOOM_BASED_ON_SPEED = "PREF_ZOOM_BASED_ON_SPEED";

    private Spinner mapLayerSpinner;
    private CheckBox chkDynamicZoom;

    SharedPreferences prefs;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mapLayerSpinner = (Spinner)findViewById(R.id.spinner_map_layers);
        chkDynamicZoom = (CheckBox)findViewById(R.id.chk_dynamic_zoom_on_speed);
        populate();

        Context context = getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        updateUIFromPreferences();
    }

    private void updateUIFromPreferences() {
        int mapLayerIndex = prefs.getInt(PREF_MAP_LAYER_INDEX, GoogleMap.MAP_TYPE_NORMAL);
        boolean isZoomBasedOnSpeed = prefs.getBoolean(PREF_ZOOM_BASED_ON_SPEED, true);
        mapLayerSpinner.setSelection(mapLayerIndex);
        chkDynamicZoom.setChecked(isZoomBasedOnSpeed);
    }

    private void populate(){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.map_layers, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapLayerSpinner.setAdapter(adapter);

    }

    public void buttonSavePressed(View view){
        Log.d(TAG, "buttonSavePressed");
        savePreferences();
        SettingsActivity.this.setResult(RESULT_OK);
        finish();
    }

    private void savePreferences() {
        int mapLayerIndex = mapLayerSpinner.getSelectedItemPosition();
        boolean isZoomBasedOnSpeed = chkDynamicZoom.isChecked();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREF_MAP_LAYER_INDEX, mapLayerIndex);
        editor.putBoolean(PREF_ZOOM_BASED_ON_SPEED, isZoomBasedOnSpeed);
        editor.commit();
    }

    public void buttonCancelPressed(View view){
        Log.d(TAG, "buttonCancelPressed");
        SettingsActivity.this.setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onBackPressed() {
        savePreferences();
        SettingsActivity.this.setResult(RESULT_OK);
        finish();
    }

}