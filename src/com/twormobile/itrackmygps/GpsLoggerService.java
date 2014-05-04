package com.twormobile.itrackmygps;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import com.twormobile.itrackmygps.android.Log;

public class GpsLoggerService extends Service {
    private static final String TAG = GpsLoggerService.class.getSimpleName();

    private GpsLoggerApplication gpsApp;
    private GpsManager gpsManager;
    private WifiStatusReceiver mWifiStatusReceiver;

    /**
     * When WIFI is disconnected, starts the location service.
     * When WIFI is connected and the service is set to ON, poll every 5 minutes
     */
    public class WifiStatusReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if(gpsApp.isWiFiConnected()) {
                stop();

                if(gpsApp.isON()) {
                    gpsManager.startNetworkPolling(300, 10);
                }
            }
            else {
                start();
                gpsManager.adjustLocationUpdateInterval(30, 10);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.gpsApp = (GpsLoggerApplication)getApplication();
        this.gpsManager = gpsApp.getGpsManager();
        this.mWifiStatusReceiver = new WifiStatusReceiver();

        final IntentFilter wifiFilters = new IntentFilter();
        wifiFilters.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        wifiFilters.addAction("android.net.wifi.STATE_CHANGE");
        this.registerReceiver(mWifiStatusReceiver, wifiFilters);

        Log.d(TAG, "onCreated");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStarted");

        start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroyed");

        stop();
    }

    private void start(){
        if(!gpsApp.isON() && !gpsManager.isGPSRunning()){
            gpsApp.setON(true);
            gpsManager.startLocationUpdates();
        }
    }

    private void stop() {
        if(gpsApp.isON() && gpsManager.isGPSRunning()){
            gpsApp.setON(false);
            gpsManager.stopLocationProviders();
        }

        this.unregisterReceiver(mWifiStatusReceiver);
    }

}
