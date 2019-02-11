package com.epochconsulting.motoinventory.vehicletracker.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.epochconsulting.motoinventory.vehicletracker.R;

public class Configuration extends AppCompatActivity {

    boolean serveraddressfound=false;
    String MY_PREFS_NAME;
    EditText serverAddress ;
    Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        serveraddressfound = true;
        serverAddress = (EditText)findViewById(R.id.server_address);
        nextButton = (Button)findViewById(R.id.nextbutton);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putBoolean("IsServeraddressFound",serveraddressfound);
                String adress = serverAddress.getText().toString();
                editor.putString("serverAddress",adress);
                editor.commit();
                editor.apply();
                Intent intent = new Intent(Configuration.this, Login.class);

                startActivity(intent);
            }
        });
    }
}
