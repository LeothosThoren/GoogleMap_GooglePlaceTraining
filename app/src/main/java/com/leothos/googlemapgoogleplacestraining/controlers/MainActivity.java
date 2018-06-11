package com.leothos.googlemapgoogleplacestraining.controlers;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.leothos.googlemapgoogleplacestraining.R;

public class MainActivity extends AppCompatActivity {

    public static final int ERROR_DIALOG_REQUEST = 9003;
    private static final String TAG = "MainActivity";
    Button btnMap;
    Button btnScdMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnMap = (Button) findViewById(R.id.btnMap);
        btnScdMap = (Button) findViewById(R.id.btnScdMap);

        if (isServiceOk()) {
            init();
        }

    }

    private void init() {
        //add something

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        btnScdMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SecondMapActivity.class);
                startActivity(i);
            }
        });

    }

    public boolean isServiceOk() {
        Log.d(TAG, "isServiceOk: checking google service version");

        int availability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (availability == ConnectionResult.SUCCESS) {
            //We check that the google services is fine and user can make request
            Log.d(TAG, "isServiceOk: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(availability)) ;
        //We have to handle the error status
        Log.d(TAG, "isServiceOk: an error occured but we can fix it");
        Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, availability, ERROR_DIALOG_REQUEST);
        dialog.show();
        return false;
    }


}
