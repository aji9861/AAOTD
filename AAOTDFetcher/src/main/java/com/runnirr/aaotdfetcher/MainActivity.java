package com.runnirr.aaotdfetcher;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("AAOTD", "started");
        promptIfNeeded();
    }

    void promptIfNeeded(){
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        String username = prefs.getString("username", null);
        String password = prefs.getString("password", null);
        if(username == null || username.isEmpty() || password == null || password.isEmpty()) {
            promptUsername();
        }else{
            runFetcher();
        }
    }

    void promptUsername(){
        promptUsername(null);
    }

    void promptUsername(String message){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Login to Amazon.com");

        if(message != null && !message.isEmpty()){
            alert.setMessage(message);
        }

        final LayoutInflater li = LayoutInflater.from(this);
        final View inputPanel = li.inflate(R.layout.username_prompt, null);

        alert.setView(inputPanel);

        final EditText emailField = (EditText) inputPanel.findViewById(R.id.emailAddressField);
        final EditText passwordField = (EditText) inputPanel.findViewById(R.id.passwordField);

        final SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        String storedUser = sharedPreferences.getString("username", null);
        if(storedUser != null){
            emailField.setText(storedUser, TextView.BufferType.EDITABLE);
            passwordField.requestFocus();
        }

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String username = "", password = "";

                try{
                    if(emailField == null){
                        Log.e("AAOTD", "email is null");
                    }
                    if(passwordField == null){
                        Log.e("AAOTD", "password is null");
                    }
                    username = emailField.getText().toString();
                    password = passwordField.getText().toString();
                    Log.d("AAOTD", "username: " + username);
                }catch(Exception e){
                    Log.e("AAOTD", e.getMessage(), e);
                    return;
                }
                SharedPreferences.Editor preferenceEditor = sharedPreferences.edit();
                preferenceEditor.putString("username", username);
                preferenceEditor.putString("password", password);
                preferenceEditor.commit();

                runFetcher();

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                promptUsername("You must sign in");
            }
        });

        alert.show();
    }

    final void runFetcher(){
        new WebLoader(this).execute();
    }
}
