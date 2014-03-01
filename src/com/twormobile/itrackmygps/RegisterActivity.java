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

public class RegisterActivity extends Activity {
    private static final String TAG = "RegisterActivity";

    private GpsLoggerApplication gpsApp;

    private EditText etxtUsername;
    private EditText etxtPassword;
    private EditText etxtPasswordConfirmation;
    private EditText etxtEmail;
    private EditText etxtPin;

    private ProgressDialog pd;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // make this activity full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_register);

        this.gpsApp = (GpsLoggerApplication)getApplication();

        etxtUsername = (EditText)findViewById(R.id.etxt_username);
        etxtPassword = (EditText)findViewById(R.id.etxt_password);
        etxtPasswordConfirmation  = (EditText)findViewById(R.id.etxt_password_confirmation);
        etxtEmail = (EditText)findViewById(R.id.etxt_email);
        etxtPin = (EditText)findViewById(R.id.etxt_pin);
    }

    public void buttonCancelPressed(View view){
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        super.finish();
    }

    public void buttonSubmitPressed(View view){
        if( gpsApp.isUsernameValid(etxtUsername, this)
                && isPasswordMatch()
                && gpsApp.isEmailValid(etxtEmail, this)){
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

    private String getCleanString(EditText etxt) {
        return String.valueOf(etxt.getText()).trim();
    }

    private void register(){
        pd = ProgressDialog.show(this, "Please Wait...", "Trying to Register");

        final String url = gpsApp.REGISTER_URL;

        //curl -i -H "Content-Type applicationjson" -X POST --data
        // 'user[username]=rupert
        // &user[email]=rupert@2rmobile.com
        // &user[password]=junjunmalupet
        // &user[password_confirmation]=junjunmalupet'
        // http://127.0.0.1:3000/api/register

        final String username = getCleanString(etxtUsername);
        final String email = getCleanString(etxtEmail);
        final String password = getCleanString(etxtPassword);
        final String passwordConfirmation = getCleanString(etxtPasswordConfirmation);
        final String pin = getCleanString(etxtPin);
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
            user.put("pin", pin);

            jsonObject.put("user", user);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response){

                        Log.v(TAG, "REGISTER Response: " + response.toString());
                        pd.dismiss();

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

    public void save(){
        final String username = getCleanString(etxtUsername);
        final String email = getCleanString(etxtEmail);
        final String pin = getCleanString(etxtPin);

        gpsApp.saveLogin(username, email, pin);
        gpsApp.setLoggedIn(username);
    }

    private void showDialog(String message){
        Log.e(TAG, message);
        gpsApp.showDialog("Error", message, RegisterActivity.this);
    }

}