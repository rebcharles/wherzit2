package com.wherzit.sammy.wherzit;

import android.Manifest;
import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wherzit.sammy.wherzit.MapActivity;


/**
 * Created by sammy on 7/13/17.
 */

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener, OnMapReadyCallback {

    TextView latitudeText;
    TextView longitudeText;

    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "MainActivity";
    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private FusedLocationProviderApi locationProvider = LocationServices.FusedLocationApi;
    LocationListener locationListener;
    LocationManager locationManager;
    private double myLatitude;
    private double myLongitude;
    private boolean permissionIsGranted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        latitudeText = (TextView) findViewById(R.id.latitude);
        longitudeText = (TextView) findViewById(R.id.longitude);


        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .enableAutoManage(this, this)
                .build();

        //taking information about location from other providers
        locationRequest = new LocationRequest();
        locationRequest.setInterval(60 * 1000);
        locationRequest.setFastestInterval(15 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        //search bar for PlacePicker API
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.placeAutocomplete);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {

            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.

                if (mMap == null) {
                    Log.i(TAG, "MAP is null");
                } else {
                    mMap.addMarker(new MarkerOptions().position(place.getLatLng()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));
//                    FragmentManager fragmentManager = getSupportFragmentManager();
//                    fragmentManager.beginTransaction()
//                                    .show(getSupportFragmentManager()
//                                    .findFragmentById(R.id.autocompleteResult))
//                                    .commit();
                    findViewById(R.id.autocompleteResult).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.resultName)).setText(place.getName());
                    if(place.getRating() > 0.0){
                        ((RatingBar) findViewById(R.id.resultRating)).setRating(place.getRating());
                        ((TextView) findViewById(R.id.ratingsNumber)).setText(Float.toString(place.getRating()));
                        findViewById(R.id.ratingsResult).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.ratingsResult).setVisibility(View.INVISIBLE);
                    }

                    Log.i(TAG, "Place: " + place.getName());
                }


            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }

        });


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        requestLocationUpdates();

    }

    private void requestLocationUpdates() {

        //checking permissions
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //asking users permission to access location
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                requestPermissions(new String[] {permission.ACCESS_FINE_LOCATION}, 1);

            }

            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        mMap.setMyLocationEnabled(true);

    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.i("Connection", "Connection suspended");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        if(!connectionResult.isSuccess()) {
            Toast.makeText(this, "Unable to connect to Google", Toast.LENGTH_SHORT).show();
        }

        Log.i("Connection", "Connection failed");
    }

    @Override
    public void onLocationChanged(Location location) {

        myLatitude = location.getLatitude();
        myLongitude = location.getLongitude();


        latitudeText.setText(String.valueOf(myLatitude));
        longitudeText.setText(String.valueOf(myLongitude));
        Log.i("Location", "Latitude: " + String.valueOf(myLatitude)
                + "\n" + "Longitude: "+ String.valueOf(myLongitude));

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(permissionIsGranted) {

            if (mGoogleApiClient.isConnected()) {

                requestLocationUpdates();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(permissionIsGranted) {

            //pausing the location service while application is not in use
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(permissionIsGranted) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

            //marker for current location
            LatLng currentLocation = new LatLng(myLatitude, myLongitude);
            mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location").draggable(true));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));




//                // Add a marker in Sydney and move the camera
//        LatLng newYork = new LatLng(40.7128, -74.0059);
////        mMap.addMarker(new MarkerOptions().position().title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newYork, 14.0f));

    }
}

