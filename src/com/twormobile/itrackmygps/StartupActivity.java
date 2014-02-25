package com.twormobile.itrackmygps;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class StartupActivity extends Activity {

    private GpsLoggerApplication gpsApp;

    private WebView wvAds;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // make this activity full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        this.gpsApp = (GpsLoggerApplication)getApplication();

        if(gpsApp.isLoggedIn()){
            setContentView(R.layout.activity_splash_ads);

            wvAds = (WebView) findViewById(R.id.wv_ads);
            initAds();

            String ADS_URL = getResources().getString(R.string.ADS_URL);
            wvAds.loadUrl(ADS_URL);
        }
        else {
            // Register or Login
            setContentView(R.layout.activity_splash_startup);
        }

    }

    private void initAds() {
        WebSettings settings = wvAds.getSettings();
        wvAds.setSaveEnabled(true);
        wvAds.setBackgroundColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            wvAds.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        }

        settings.setLoadsImagesAutomatically(true);
        settings.setBlockNetworkLoads(false);
        settings.setBlockNetworkImage(false);
        settings.setJavaScriptEnabled(true);        
    }

    public void buttonRegisterPressed(View view){
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
    }

    public void buttonLoginPressed(View view){
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    public void buttonEnterPressed(View view){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        //Ensures that we don't go back to previous activity
    }

}