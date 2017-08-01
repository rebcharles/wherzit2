package com.wherzit.sammy.wherzit;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

//
///**
// * Created by saritm on 7/25/17.
// */
//
public class DirectionsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private static final String TAG = "DirectionsActivity";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest locationRequest;
    private Double myLatitude;
    private Double myLongitude;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private boolean permissionIsGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        Log.i(TAG, "======== onCreate =========");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_directions);

        //creating map background
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapDirectionsActivity);

        //fetching json response and creating object
        Intent intent = getIntent();
        String jsonString = intent.getStringExtra("jsonResponse");

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //taking information about location from other providers
        locationRequest = new LocationRequest();
        locationRequest.setInterval(20000);
        locationRequest.setFastestInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                requestLocationUpdates();
            }
        };

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, " =========== onMapReady ========= ");
        mMap = googleMap;

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.i(TAG, " =========== onConnected ========= ");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.i(TAG, " =========== 1st========= ");

            //asking users permission to access location
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                Log.i(TAG, " =========== 2nd ========= ");

                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            }

            return;
        }

        Log.i(TAG, " =========== 3rd ========= ");
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            Log.i(TAG, " =========== 4th ========= ");
            requestLocationUpdates();

        } else {
            myLatitude = location.getLatitude();
            myLongitude = location.getLongitude();

            Log.i("Location", "Latitude: " + String.valueOf(location.getLatitude())
                    + "\n" + "Longitude: "+ String.valueOf(location.getLongitude()));



        }
    }

    private void requestLocationUpdates() {


        Log.i(TAG, " =========== requestLocationUpdates ========= ");

        //checking permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //asking users permission to access location
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            }

            return;
        }
        Log.i(TAG, "======== Before requestLocationUpdates =========");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        Log.i(TAG, "======== After requestLocationUpdates =========");
//        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.i(TAG, " =========== onConnectionSuspended ========= ");

        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        if (!connectionResult.isSuccess()) {
            Toast.makeText(this, "Unable to connect to Google", Toast.LENGTH_SHORT).show();
        }

        Log.i("Connection", "Connection failed");
    }

    @Override
    public void onLocationChanged(Location location) {

        Log.i(TAG, " =========== onLocationChanged ========= ");

        myLatitude = location.getLatitude();
        myLongitude = location.getLongitude();

        Log.i("Location", "Latitude: " + String.valueOf(location.getLatitude())
                + "\n" + "Longitude: "+ String.valueOf(location.getLongitude()));

    }

    @Override
    protected void onStart() {

        Log.i(TAG, " =========== onStart ========= ");
        super.onStart();

        if (mGoogleApiClient != null) {

            mGoogleApiClient.connect();

        }

    }

    @Override
    protected void onResume() {

        Log.i(TAG, " =========== onResume ========= ");
        super.onResume();


        if(permissionIsGranted && checkPlayServices()) {

            if (mGoogleApiClient.isConnected()) {

                requestLocationUpdates();
            }
        }

    }

    private boolean checkPlayServices() {

        Log.i(TAG, " =========== checkPlayServices ========= ");

            int resultCode = GooglePlayServicesUtil
                    .isGooglePlayServicesAvailable(this);
            if (resultCode != ConnectionResult.SUCCESS) {
                if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                    GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                            PLAY_SERVICES_RESOLUTION_REQUEST).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "This device is not supported.", Toast.LENGTH_LONG)
                            .show();
                    finish();
                }
                return false;
            }
            return true;
    }

    @Override
    protected void onPause() {

        Log.i(TAG, " =========== onPause ========= ");
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
    }

    @Override
    protected void onStop() {

        Log.i(TAG, " =========== onStop ========= ");
        super.onStop();

        if (mGoogleApiClient != null) {

            mGoogleApiClient.disconnect();

        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        Log.i(TAG, "============ onRequestPermissionsResult ============");

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case 1:
                //permission granted

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    permissionIsGranted = true;

                }
                //permission denied
                else {

                    permissionIsGranted = false;
                    Toast.makeText(getApplicationContext(), "This app requires location " +
                            "permissions to be granted", Toast.LENGTH_SHORT).show();

                }

                break;

        }
    }

}

