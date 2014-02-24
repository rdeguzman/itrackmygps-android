package com.twormobile.itrackmygps;

import android.app.Activity;
import android.os.Bundle;

public class StartupActivity extends Activity {

    private GpsLoggerApplication gpsApp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.gpsApp = (GpsLoggerApplication)getApplication();

        if(gpsApp.isLoggedIn()){
            setContentView(R.layout.activity_splash_ads);
        }
        else {
            // Register or Login
            setContentView(R.layout.activity_splash_startup);
        }

    }
}