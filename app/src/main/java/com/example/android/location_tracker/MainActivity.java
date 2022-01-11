package com.example.android.location_tracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.android.location_tracker.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_FINE_LOCATION = 99;

    // We are tracking user's location periodically every 30 seconds
    private static final long DEFAULT_UPDATE_INTERVAL = 30;

    final String googleMapsDefaultURL = "https://www.google.com/maps/search/?api=1&query=";

    private ActivityMainBinding binding;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;

    LocationCallback locationCallback;

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
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        binding.map.getSettings().setJavaScriptEnabled(true);
        binding.map.getSettings().setDomStorageEnabled(true);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        initializeLocationCallback();
        startLocationUpdates();
    }

    private void initializeLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                binding.map.setVisibility(View.INVISIBLE);
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.progressText.setVisibility(View.VISIBLE);

                // save the location
                updateUi(locationResult.getLastLocation());

            }
        };
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    /**
     * This method performs 2 tasks
     * 1. Get permissions from the user to track GPS
     * 2. Get the current location from the fused client
     */
    private void updateGPS() {

        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // user provided the permission

            Task<Location> currentLocation = fusedLocationProviderClient.getCurrentLocation(
                    LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
                    new CancellationToken() {
                @NonNull
                @Override
                public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                    return null;
                }

                @Override
                public boolean isCancellationRequested() {
                    return false;
                }
            });

            currentLocation.addOnSuccessListener(this, new OnSuccessListener<Location>() {
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

        String coordinates = latitude + "%20" + longitude;
        String locationURL = googleMapsDefaultURL + coordinates;
        Log.v("MainActivity", locationURL);

        binding.map.loadUrl(locationURL);

        // sleep for 5 seconds, so that when the map is visible, it does not load in parts
        new Handler().postDelayed(new Runnable() {
            public void run() {
                binding.progressBar.setVisibility(View.INVISIBLE);
                binding.progressText.setVisibility(View.INVISIBLE);
                binding.latitude.setText(latitude);
                binding.longitude.setText(longitude);
                binding.map.setVisibility(View.VISIBLE);
            }
        }, 5000);

    }


}