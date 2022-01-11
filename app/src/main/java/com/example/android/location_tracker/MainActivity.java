package com.example.android.location_tracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.android.location_tracker.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private String googleMapsURL = "https://www.google.com/maps/search/?api=1&query=";
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    private static final long DEFAULT_UPDATE_INTERVAL = 30;
    private static final long FAST_UPDATE_INTERVAL = 5;

    private ActivityMainBinding binding;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;

    /**
     * This method is called when the user either gives/denies location permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateGPS();
            } else {
                Toast.makeText(this,
                        "This app requires permissions in order to work properly",
                        Toast.LENGTH_SHORT)
                        .show();
                finish();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        locationRequest = LocationRequest.create()
                .setInterval(1000 * DEFAULT_UPDATE_INTERVAL)         // how often does the default location check occur
                .setFastestInterval(1000 * FAST_UPDATE_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        updateGPS();
    }

    /**
     * This method performs 2 tasks
     * 1. Get permissions from the user to track GPS
     * 2. Get the current location from the fused client
     */
    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // user provided the permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // We got permissions. Use argument location
                    updateUi(location);
                }
            });
        } else {
            // Permissions not granted yet
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    /**
     * This method updates the UI
     * @param location The current location of the user
     */
    private void updateUi(Location location) {
        String latitude = String.valueOf(location.getLatitude());
        String longitude = String.valueOf(location.getLongitude());

        binding.latitude.setText(latitude);
        binding.longitude.setText(longitude);

        String coordinates = latitude + "%20" + longitude;
        Log.v("MainActivity", coordinates);
        googleMapsURL += coordinates;
        Log.v("MainActivity", googleMapsURL);
        binding.map.getSettings().setJavaScriptEnabled(true);
        binding.map.loadUrl(googleMapsURL);
    }


}