package com.twormobile.itrackmygps;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import com.twormobile.itrackmygps.android.Log;
import android.view.View;
import android.widget.EditText;

public class ShareActivity extends Activity {
    private static final String TAG = "ShareActivity";

    private GpsLoggerApplication gpsApp;
    private EditText etxtMessage;
    private EditText etxtPin;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        etxtMessage = (EditText)findViewById(R.id.etxt_message);
        etxtPin = (EditText)findViewById(R.id.etxt_pin);

        this.gpsApp = (GpsLoggerApplication)getApplication();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String pin = prefs.getString("pin", null);

        String trackURL = getResources().getString(R.string.TRACK_URL) + gpsApp.getUsername();
        String message = "Track me in realtime " + trackURL;

        etxtMessage.setText(message);
        etxtPin.setText(pin);
        etxtPin.setEnabled(false);
    }

    public void buttonSMSPressed(View view){
        Log.i(TAG, "buttonSMSPressed");

        if(isMessageValid()) {
            String message = String.valueOf(etxtMessage.getText()).trim();

            //At least KitKat
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                //Need to change the build to API 19
                String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(this);

                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, message);

                // Can be null in case that there is no default, then the user would be able
                // to choose any app that support this intent.
                if (defaultSmsPackageName != null) {
                    sendIntent.setPackage(defaultSmsPackageName);
                }

                startActivity(sendIntent);

            }
            else  {
                // For early versions we just use ACTION_VIEW
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.putExtra("sms_body", message);
                startActivity(sendIntent);
            }
        }
    }

    private boolean isMessageValid() {
        boolean valid = true;

        String message = String.valueOf(etxtMessage.getText()).trim();

        if(message.length() == 0){
            valid = false;
        }

        if(!valid) {
            gpsApp.showDialog("Warning", "Please specify a message with URL before proceeding", this);
        }

        return valid;
    }

    public void buttonCancelPressed(View view){
        super.finish();
    }

}
