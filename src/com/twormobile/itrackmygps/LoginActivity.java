package com.twormobile.itrackmygps;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends Activity {
    private static final String TAG = "LoginActivity";

    private GpsLoggerApplication gpsApp;

    private EditText etxtUsername;
    private EditText etxtPassword;

    private ProgressDialog pd;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // make this activity full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);

        this.gpsApp = (GpsLoggerApplication)getApplication();

        etxtUsername = (EditText)findViewById(R.id.etxt_username);
        etxtPassword = (EditText)findViewById(R.id.etxt_password);
    }

    public void buttonCancelPressed(View view){
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        super.finish();
    }

    public void buttonSubmitPressed(View view){
        if( gpsApp.isUsernameValid(etxtUsername, this)
                && gpsApp.isPasswordValid(etxtPassword, this)){
            login();
        }
    }

    private String getCleanString(EditText etxt) {
        return String.valueOf(etxt.getText()).trim();
    }

    private void login(){
        pd = ProgressDialog.show(this,"Please Wait...","Trying to Login");

        final String url = gpsApp.LOGIN_URL;

        final String username = getCleanString(etxtUsername);
        final String password = getCleanString(etxtPassword);
        final String uuid = gpsApp.getUUID();

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject();
            jsonObject.put("uuid", uuid);
            jsonObject.put("username", username);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST,
                url,
                jsonObject,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response){

                        Log.v(TAG, "LOGIN Response: " + response.toString());
                        pd.dismiss();

                        try {
                            boolean valid = response.getBoolean("valid");

                            if(valid) {
                                String message = "Login successful!";
                                Log.e(TAG, message);

                                final String email = response.getString("email");

                                AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
                                dialog.setTitle("Info");
                                dialog.setMessage(message);
                                dialog.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                        save(email);
                                        done();
                                    }
                                });

                                dialog.show();
                            }
                            else {
                                String message = response.getString("errors");
                                showDialog(message);
                            }

                        } catch (JSONException e) {
                            String message = "Cannot parse response from " + url + "(" + response.toString() + ")";
                            showDialog(message);
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        pd.dismiss();
                        String message = "A network error has occurred on " + url + "(" + error.toString() + ")";
                        showDialog(message);
                    }
                });

        gpsApp.getVolleyRequestQueue().add(postRequest);
    }

    public void done(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void save(String email){
        final String username = getCleanString(etxtUsername);

        gpsApp.saveLogin(username, email);
        gpsApp.setLoggedIn(username);
    }

    private void showDialog(String message){
        Log.e(TAG, message);
        gpsApp.showDialog("Error", message, LoginActivity.this);
    }

}