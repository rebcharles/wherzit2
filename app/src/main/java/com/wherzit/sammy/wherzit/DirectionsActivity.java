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
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

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
    private Location currentLocation;
    private JSONObject jsonResponse;
    private String destinationId;
    private String destinationChanged;
    private String waypoints;
    private String originString;
    private String[] originLatLng;
    private double originLongitude;
    private double originLatitude;
    private Location origin = new Location("initialize");
    double dCLng;
    double dCLat;
    TextView textViewToChange;
    String instructions;
    List<Route> routes;
    Double locationsAreEqual = 10.668;
    float distanceInMetersOne;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        Log.i(TAG, "======== onCreate =========");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_directions);

        //creating map background
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapDirectionsActivity);
        mapFragment.getMapAsync(this);

        buildClient();

        //fetching json response and origin
        Bundle extras = getIntent().getExtras();
        String jsonString = extras.getString("jsonResponse");
        originString = extras.getString("originChanged");



        if (originString != null) {
            Log.i("origin", originString);

            //splitting string to get origin coordinates
            String[] originLatLng = originString.split("\\(");
            String[] originLatLng1 = originLatLng[1].split(",");
            String[] originLatLng2 = originLatLng1[1].split("\\)");

            double originLatitude = Double.parseDouble(originLatLng1[0]);
            double originLongitude = Double.parseDouble(originLatLng2[0]);

            origin.setLongitude(originLongitude);
            origin.setLatitude(originLatitude);

        }


        textViewToChange = (TextView) findViewById(R.id.directions);

        try {
            jsonResponse = new JSONObject(jsonString);

            //getting origin and destination
            String startAddress = jsonResponse.getJSONArray("routes").getJSONObject(0)
                    .getJSONArray("legs").getJSONObject(0).getString("start_address");

            String endAddress = jsonResponse.getJSONArray("routes").getJSONObject(0)
                    .getJSONArray("legs").getJSONObject(0).getString("end_address");


        } catch (JSONException e) {
            e.printStackTrace();
        }


        //parsing data
        try {
            routes = parseJsonResponse(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    protected void setText(int i) throws JSONException {

        Log.i("valueOfI", Integer.toString(i));
        Log.i("size", Integer.toString(routes.size()));

        //getting ith step
        JSONObject jsonOBJ = jsonResponse.getJSONArray("routes").getJSONObject(0)
                .getJSONArray("legs").getJSONObject(0).getJSONArray("steps").getJSONObject(i);

        JSONArray jsonArray = jsonResponse.getJSONArray("routes").getJSONObject(0)
                .getJSONArray("legs").getJSONObject(0).getJSONArray("steps");

        //for testing
        for (int j = 0; j < jsonArray.length(); j++) {

            Log.i("endLocation",  j + " " + jsonArray.getJSONObject(j).getString("end_location"));
            Log.i("StartLocation",  j + " " + jsonArray.getJSONObject(j).getString("start_location"));
            Log.i("HTML", j + " " +  jsonArray.getJSONObject(j).getString("html_instructions"));
            if (jsonArray.getJSONObject(j).has("maneuver")) {
                Log.i("Maneuver", j + " " +  jsonArray.getJSONObject(j).getString("maneuver"));
            }
            Log.i("Distance", j + " " + jsonArray.getJSONObject(j).getJSONObject("distance").getString("text"));

        }


        if (jsonOBJ.has("maneuver")) {

            String Maneuver = jsonOBJ.getString("maneuver");

            textViewToChange.setText(Maneuver + " in " + jsonOBJ.getJSONObject("distance").getString("text"));

        } else {

            //getting instructions
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

                Log.i("jsonObj", jsonOBJ.getString("html_instructions"));

                instructions = Html.fromHtml(jsonOBJ.getString("html_instructions"),
                        Html.FROM_HTML_MODE_LEGACY).toString();

                Log.i("instructions", instructions);


            } else {

                instructions = Html.fromHtml(jsonOBJ.getString("html_instructions")).toString();


            }
            textViewToChange.setText(instructions);

        }
    }

    protected void changeStep() throws JSONException {

        Log.i(TAG, " =========== changeStep ========= ");

        int i = 0;
        while (i < routes.size()) {

            //coordinates for end location
            Double latEndLoc = routes.get(i).endLocation.lat;
            Double lngEndLoc = routes.get(i).endLocation.lng;
            Location endLocation = new Location("");
            endLocation.setLatitude(latEndLoc);
            endLocation.setLongitude(lngEndLoc);

            //if user does not change origin from current location
            if (originString == null) {

                //calculating distance between end location and current location
                distanceInMetersOne = endLocation.distanceTo(currentLocation);

            }

            //if they change origin
            else {

                distanceInMetersOne = endLocation.distanceTo(origin);
            }

            // if distance < 35 feet, locations are considered equal.
            if (distanceInMetersOne < locationsAreEqual) {
                //user has reached last step
                if (i == (routes.size() - 1)) {

                    Log.i(TAG, " =========== lastIndex  ========= ");

                    textViewToChange.setText("You have reached your destination");

                }

                Log.i(TAG, " =========== enteredIfBranch  ========= ");

                setText(i + 1);
                return;

            }
            i++;
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, " =========== onMapReady ========= ");
        mMap = googleMap;

    }

    private void buildClient() {
        Log.i(TAG, " =========== buildClient ========= ");

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //setting interval for updating location service
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
    public void onConnected(@Nullable Bundle bundle) {

        Log.i(TAG, " =========== onConnected ========= ");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.i(TAG, " =========== 1st========= ");

            //asking users permission to access location
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                Log.i(TAG, " =========== 2nd ========= ");

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            }

            return;
        }

        Log.i(TAG, " =========== 3rd ========= ");

        //setting location to last known location
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        requestLocationUpdates();

        myLatitude = currentLocation.getLatitude();
        myLongitude = currentLocation.getLongitude();

       currentLocation.setLatitude(myLatitude);
       currentLocation.setLongitude(myLongitude);

        try {
            setText(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("Location", "Latitude: " + String.valueOf(myLatitude)
                + "\n" + "Longitude: " + String.valueOf(myLongitude));

        Log.i("currentLocation", currentLocation.toString());
    }


    private void requestLocationUpdates() {


        Log.i(TAG, " =========== requestLocationUpdates ========= ");

        //checking permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //asking users permission to access location
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            }

            return;
        }
        Log.i(TAG, "======== Before requestLocationUpdates =========");
        //requesting updates
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        Log.i(TAG, "======== After requestLocationUpdates =========");
        // current location button
        mMap.setMyLocationEnabled(true);
        Log.i(TAG, "======== setLocationEnabled =========");
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

        currentLocation.setLatitude(myLatitude);
        currentLocation.setLongitude(myLongitude);

        Log.i("Location", "Latitude: " + String.valueOf(location.getLatitude())
                + "\n" + "Longitude: " + String.valueOf(location.getLongitude()));

        try {
            changeStep();
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
        checkPlayServices();

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
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
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

    private List<Route> parseJsonResponse(String data) throws JSONException {

        if (data == null) {
            return null;
        }


        List<Route> routes = new ArrayList<Route>();


        //organizing json response into variables and objects
        JSONObject directions = new JSONObject(data);
        JSONArray jsonRoutes = directions.getJSONArray("routes");
        JSONObject routesOBJ = jsonRoutes.getJSONObject(0);
        JSONArray jsonLegs = routesOBJ.getJSONArray("legs");
        JSONObject legsOBJ = jsonLegs.getJSONObject(0);
        JSONArray jsonSteps = legsOBJ.getJSONArray("steps");

        for (int i = 0; i < jsonSteps.length(); i++) {

            Route route = new Route();

            //adding details to each route
            JSONObject stepsOBJ = jsonSteps.getJSONObject(i);
            JSONObject jsonDistance = stepsOBJ.getJSONObject("distance");
            JSONObject jsonDuration = stepsOBJ.getJSONObject("duration");
            JSONObject jsonEnd = stepsOBJ.getJSONObject("end_location");
            JSONObject jsonStart = stepsOBJ.getJSONObject("start_location");

            route.startAddress = legsOBJ.getString("start_address");
            route.endAddress = legsOBJ.getString("end_address");
            route.distance = new Distance(jsonDistance);
            route.duration = new Duration(jsonDuration);
            route.startLocation = new com.google.maps.model.LatLng(jsonStart.getDouble("lat"),
                    jsonStart.getDouble("lng"));
            route.endLocation = new com.google.maps.model.LatLng(jsonEnd.getDouble("lat"),
                    jsonEnd.getDouble("lng"));

            routes.add(route);
        }
        return routes;
    }

}

