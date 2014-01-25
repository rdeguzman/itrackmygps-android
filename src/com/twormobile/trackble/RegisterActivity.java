package com.twormobile.trackble;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class RegisterActivity extends Activity {

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
        boolean valid = true;
        if( isUsernameValid() && isPasswordMatch() && isEmailValid()){
            valid = true;
        }
        else {
            valid = false;
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

}