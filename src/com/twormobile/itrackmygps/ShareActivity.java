package com.twormobile.itrackmygps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class ShareActivity extends Activity {
    private static final String TAG = "ShareActivity";

    private GpsLoggerApplication gpsApp;
    private EditText etxtMessage;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        etxtMessage = (EditText)findViewById(R.id.etxt_message);

        this.gpsApp = (GpsLoggerApplication)getApplication();

        String trackURL = getResources().getString(R.string.TRACK_URL) + gpsApp.getUsername();
        String message = "Track me in realtime " + trackURL;

        etxtMessage.setText(message);
    }

    public void buttonSMSPressed(View view){
        Log.i(TAG, "buttonSMSPressed");

        if(isMessageValid()) {
            String message = String.valueOf(etxtMessage.getText()).trim();

            Intent sendIntent = new Intent(Intent.ACTION_VIEW);
            sendIntent.putExtra("sms_body", message);
            sendIntent.setType("vnd.android-dir/mms-sms");
            startActivity(sendIntent);
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
