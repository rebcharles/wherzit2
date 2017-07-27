package com.wherzit.sammy.wherzit;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * Created by saritm on 7/25/17.
 */

public class DirectionsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    String instructions;
    public static final int FROM_HTML_MODE_LEGACY = 0;
    JSONObject jsonResponse;
    TextView textViewToChange;
    JSONArray steps;
    double myLatitude, myLongitude;
    private FusedLocationProviderApi locationProvider = LocationServices.FusedLocationApi;
    private GoogleApiClient googleApiClient;
    Location currentLocation;
    LatLng userLocation;
    List<Route> routes;
    boolean permissionIsGranted = false;
    private LocationRequest locationRequest;
    private static Location lastLocation;
    private String TAG = "coordinates";
    private static Context context;
    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        googleApiClient.connect();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(60 * 1000);
        locationRequest.setFastestInterval(15 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        //fetching json response and creating object
        Intent intent = getIntent();
        String jsonString = intent.getStringExtra("jsonResponse");

        try {
            jsonResponse = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (jsonResponse == null) {
            Log.i("jsonResponse", "fucking null");
        }

        textViewToChange = (TextView) findViewById(R.id.stepDirections);
        textViewToChange.setText("hiya");


        try {
            setText(0);
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


    protected void setText(int i) throws JSONException {

        JSONObject jsonOBJ = jsonResponse.getJSONArray("routes").getJSONObject(0)
                .getJSONArray("legs").getJSONObject(0).getJSONArray("steps").getJSONObject(i);


        if (jsonOBJ.has("maneuver")) {

            String Maneuver = jsonOBJ.getString("maneuver");

            textViewToChange.setText(Maneuver + " in " + jsonOBJ.getJSONObject("distance").getString("text"));

        } else {


            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

                Log.i("jsonObj", jsonOBJ.getString("html_instructions"));

                instructions = Html.fromHtml(jsonOBJ.getString("html_instructions"),
                        Html.FROM_HTML_MODE_LEGACY).toString();

                Log.i("instructions", instructions);

                textViewToChange.setText(instructions);

            } else {

                instructions = Html.fromHtml(jsonOBJ.getString("html_instructions")).toString();

                textViewToChange.setText(instructions);
            }

        }
    }


    protected void changeStep() throws JSONException {

        int i = 0;
        while (i < routes.size()) {

            //coordinates for end location
            Double latE = routes.get(i).endLocation.lat;
            Double lngE = routes.get(i).endLocation.lng;

            //coordinates for current location
            Double latC = myLatitude;
            Double lngC = myLongitude;


            Log.i("coordinates_change_step", latE + " " + latC + " " + lngE + " " + lngC);


            if ((latE - latC) <= 0.000001) {
                if ((lngE - lngC) <= 0.000001) {

                    Log.i("enteredIfStatement", "finally");
                    setText(i + 1);
                    return;

                }

            }
            i++;
        }
        return;

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

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


        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {

                            myLatitude = location.getLatitude();
                            myLongitude = location.getLongitude();

                            Log.i("coordinates", String.valueOf(myLatitude + " " + myLongitude));

                        }
                    }
                });

    }


    @Override
    public void onLocationChanged(Location location) {

        //fetching current location
        myLatitude = location.getLatitude();
        myLongitude = location.getLongitude();


        try {
            changeStep();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
//
    @Override
    public void onConnectionSuspended(int i) {

        Log.i("Connection", "Connection suspended");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        if (!connectionResult.isSuccess()) {
            Toast.makeText(this, "Unable to connect to Google", Toast.LENGTH_SHORT).show();
        }

        Log.i("Connection", "Connection failed");
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (permissionIsGranted) {

            //pausing the location service while application is not in use
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (permissionIsGranted) {
            googleApiClient.disconnect();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
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
}

