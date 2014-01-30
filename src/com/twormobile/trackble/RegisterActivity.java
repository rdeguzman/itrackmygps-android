package com.twormobile.trackble;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends Activity {
    private static final String TAG = "RegisterActivity";

    private GpsLoggerApplication gpsApp;

    private EditText etxtUsername;
    private EditText etxtPassword;
    private EditText etxtPasswordConfirmation;
    private EditText etxtEmail;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        this.gpsApp = (GpsLoggerApplication)getApplication();

        etxtUsername = (EditText)findViewById(R.id.etxt_username);
        etxtPassword = (EditText)findViewById(R.id.etxt_password);
        etxtPasswordConfirmation  = (EditText)findViewById(R.id.etxt_password_confirmation);
        etxtEmail = (EditText)findViewById(R.id.etxt_email);
    }

    public void buttonCancelPressed(View view){
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        super.finish();
    }

    public void buttonSubmitPressed(View view){
        if( isUsernameValid() && isPasswordMatch() && isEmailValid()){
            register();
        }
    }

    private boolean isPasswordMatch(){
        boolean valid = true;
        String password = String.valueOf(etxtPassword.getText());
        String passwordConfirm = String.valueOf(etxtPasswordConfirmation.getText());

        if(password.isEmpty() || passwordConfirm.isEmpty() || password.equals(passwordConfirm) == false){
            valid = false;
        }

        if(!valid){
            String message = getResources().getString(R.string.password_mismatch);
            gpsApp.showDialog("Error", message, this);
        }

        return valid;
    }

    private boolean isUsernameValid(){
        boolean valid = false;
        String value = String.valueOf(etxtUsername.getText());
        if(value != null && value.trim().length() > 0) {
            int length = value.trim().length();
            if(length > 0 && length <= 16) {
                valid = true;
            }
        }

        if(!valid){
            String message = getResources().getString(R.string.invalid_username);
            gpsApp.showDialog("Error", message, this);
        }

        return valid;
    }

    private boolean isEmailValid(){
        boolean valid = false;
        String value = String.valueOf(etxtEmail.getText());
        if(value != null && value.trim().length() > 0) {
            valid = true;
        }

        if(!valid){
            String message = getResources().getString(R.string.invalid_email);
            gpsApp.showDialog("Error", message, this);
        }

        return valid;
    }

    private String getCleanString(EditText etxt) {
        return String.valueOf(etxt.getText()).trim();
    }

    private void register(){
        final String url = gpsApp.REGISTER_URL;

        //curl -i -H "Content-Type applicationjson" -X POST --data
        // 'user[username]=rupert
        // &user[email]=rupert@2rmobile.com
        // &user[password]=junjunmalupet
        // &user[password_confirmation]=junjunmalupet'
        // http://127.0.0.1:3000/api/users.json

        final String username = getCleanString(etxtUsername);
        final String email = getCleanString(etxtEmail);
        final String password = getCleanString(etxtPassword);
        final String passwordConfirmation = getCleanString(etxtPasswordConfirmation);
        final String uuid = gpsApp.getUUID();

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject();
            jsonObject.put("uuid", uuid);

            JSONObject user = new JSONObject();
            user.put("username", username);
            user.put("email", email);
            user.put("password", password);
            user.put("password_confirmation", passwordConfirmation);

            jsonObject.put("user", user);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response){

                        Log.v(TAG, "REGISTER Response: " + response.toString());

                        try {
                            boolean valid = response.getBoolean("valid");

                            if(valid) {
                                String message = "Registration successful!";
                                Log.e(TAG, message);

                                AlertDialog.Builder dialog = new AlertDialog.Builder(RegisterActivity.this);
                                dialog.setTitle("Info");
                                dialog.setMessage(message);
                                dialog.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                        save();
                                        done();
                                    }
                                });

                                dialog.show();
                            }
                            else {
                                String message = response.getString("errors");
                                Log.e(TAG, message);
                                gpsApp.showDialog("Error", message, RegisterActivity.this);
                            }

                        } catch (JSONException e) {
                            String message = "Cannot parse response from " + url + "(" + response.toString() + ")";
                            Log.e(TAG, message);
                            gpsApp.showDialog("Error", message, RegisterActivity.this);
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        String message = "A network error has occurred on " + url + "(" + error.toString() + ")";
                        Log.e(TAG, message);
                        gpsApp.showDialog("Error", message, RegisterActivity.this);
                    }
                });

        gpsApp.getVolleyRequestQueue().add(postRequest);

    }

    public void done(){
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        super.finish();
    }

    public void save(){
        final String username = getCleanString(etxtUsername);
        final String email = getCleanString(etxtEmail);
        final String uuid = gpsApp.getUUID();

        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.putString("email", email);
        editor.putString("uuid", uuid);
        editor.commit();

        gpsApp.setLoggedIn(username);
    }

}