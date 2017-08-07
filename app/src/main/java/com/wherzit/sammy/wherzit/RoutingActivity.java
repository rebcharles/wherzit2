package com.wherzit.sammy.wherzit;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.DataBuffer;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


import javax.net.ssl.HttpsURLConnection;


public class RoutingActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener, OnMapReadyCallback {

    private Place destination;
    private static final String TAG = "RoutingActivity";
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private FusedLocationProviderApi locationProvider = LocationServices.FusedLocationApi;
    LocationListener locationListener;
    LocationManager locationManager;
    private double myLatitude;
    private double myLongitude;
    private boolean permissionIsGranted = false;
    private Place origin = null;
    private Place destinationChanged = null;
    private String destinationId;
    List<Route> routes;
    String encodedPolyline;
    private JSONObject json_response;
    private HashMap<CharSequence, String> waypoints;
    LatLng currentLocation;
    private Polyline currentRoute;
    public PendingResult<AutocompletePredictionBuffer> results;
    public ArrayList<AutocompletePrediction> predictions;
    public Type predictionType = new TypeToken<ArrayList<AutocompletePrediction>>() {}.getType();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "============ onCreate ============");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routing);
        waypoints = new HashMap<CharSequence, String>();

        //making background transparent
        LinearLayout background = (LinearLayout) findViewById(R.id.background);
        background.setAlpha((float)0.7);
        TextView stopsView = (TextView) findViewById(R.id.currentStopsText);
        stopsView.setMovementMethod(new ScrollingMovementMethod());

        //creating map background
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.routingMap);
        mapFragment.getMapAsync(this);

        Log.i(TAG, "======== After getMapAsync =========");




        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .enableAutoManage(this, this)
                .build();
        locationRequest = new LocationRequest();
        locationRequest.setInterval(60 * 1000);
        locationRequest.setFastestInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        destinationId = getIntent().getExtras().getString("destinationId");

        final Button navigationButton = (Button) findViewById(R.id.navigateButton);
        navigationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RoutingActivity.this, DirectionsActivity.class);

                if(json_response == null) {

                    Log.i("null","json is NULL");
                }

                if (json_response != null) {
                    Bundle extras = new Bundle();
                    extras.putString("jsonResponse", json_response.toString());

                    if (origin != null) {
                        extras.putString("originChanged",origin.getLatLng().toString());

                    }


                    if(destinationChanged != null) {
                        extras.putString("destinationChanged",destinationChanged.getLatLng().toString());
                    }

                    else {
                        extras.putString("destinationID",destinationId);
                    }

                    intent.putExtras(extras);
                }

                startActivity(intent);
            }
        });

        Places.GeoDataApi.getPlaceById(mGoogleApiClient, destinationId)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                            destination = places.get(0);
                            Log.i(TAG, "Place found: " + destination.getName());
                        } else {
                            Log.e(TAG, "Place not found");
                        }
                        //Destination
                        PlaceAutocompleteFragment destFragment = (PlaceAutocompleteFragment)
                                getFragmentManager().findFragmentById(R.id.autocompleteDest);
                        destFragment.setHint(destination != null ?
                                destination.getName() :"Choose a destination!");
                        if (mMap != null){
                            mMap.addMarker(new MarkerOptions().position(destination.getLatLng()).title(destination.getName().toString()));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination.getLatLng(), 12.0f));
                        }
                        places.release();
                    }
                });

        //Origin
        PlaceAutocompleteFragment originFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocompleteOrigin);
        originFragment.setHint("Current location");

        LatLng locationOrigin= new LatLng (myLatitude, myLongitude);

        Log.i("locationOrigin", locationOrigin.toString());

        //if user changes origin
        originFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {

            @Override
            public void onPlaceSelected(Place place) {

                    Log.i(TAG, " =========== 196:onPlaceSelected ========= ");

                    origin = place;

                    placeMarker(place);

            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }


        });

        EditText stopFragment = (EditText)  findViewById(R.id.autocompleteStop);
        stopFragment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus){
                    final CharSequence charCopy = ((EditText) findViewById(R.id.autocompleteStop)).getText();
                    Autocompleter predictor = new Autocompleter();
                    try {
                        Gson predictionStatus = new Gson();
                        Status status = (Status) predictionStatus.fromJson(predictor.execute(charCopy.toString()).get(), Status.class);
                        if(!status.isSuccess()){
                            Log.i(TAG, "Status returned: " + status.toString());
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        Log.e(TAG, e.getMessage());
                    }
                    Log.i(TAG, "Predictions size:" + predictions.size());
                    long shortestSoFar = Long.MAX_VALUE;
                    String bestPlace = "";
                    for (int j = 0;
                         j < (predictions.size() < 5 ? predictions.size() : 5);
                         j++) {
                        long distance = 0;
                        AutocompletePrediction prediction = predictions.get(j);
                        Log.i(TAG, "About to send:" + prediction.getPlaceId());
                        String test_response = sendRequest(waypoints, prediction.getPlaceId(),prediction.getPrimaryText(null));
                        Log.i(TAG, "Received: "+ test_response);
                        try {
                            JSONObject test_JSON = new JSONObject(test_response);
                            JSONArray leg_array = test_JSON.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
                            for (int k=0; k <leg_array.length(); k++ ){
                                distance += Long.valueOf(leg_array.getJSONObject(k).getJSONObject("duration").get("value").toString());
                            }
                            if (distance > 0 && distance <shortestSoFar) {
                                shortestSoFar= distance;
                                bestPlace = predictions.get(j).getPlaceId();
                            }
                        }catch (JSONException e){
                            Log.i(TAG, "Failed to get test object");
                        }
                    }
                    Log.i(TAG, "Before put: " + waypoints.size());
                    waypoints.put(charCopy, bestPlace);
                    Log.i(TAG, "After put: " + waypoints.size());

                    updateStopsView();

                    Places.GeoDataApi.getPlaceById(mGoogleApiClient, bestPlace)
                            .setResultCallback(new ResultCallback<PlaceBuffer>() {
                                @Override
                                public void onResult(PlaceBuffer places) {
                                    if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                        Log.i(TAG, "Place found: " + places.get(0).getName());
                                        placeMarker(places.get(0));
                                    } else {
                                        Log.e(TAG, "Place not found");
                                    }
                                    //Destination
                                    places.release();
                                }
                            });


                }
            }
        });

        //Destination
        PlaceAutocompleteFragment destFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocompleteDest);
        destFragment.setHint(destination != null ?
                destination.getName() :"Choose a destination");

        //if user changes destination
        destFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {

            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, " =========== 254: onPlaceSelected ========= ");
                destinationChanged = place;

                placeMarker(place);

            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }




        });


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, " =========== onConnected ========= ");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //asking users permission to access location
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            }

            return;
        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        requestLocationUpdates();
        myLatitude = location.getLatitude();
        myLongitude = location.getLongitude();
        Log.i(TAG, "Location : " + String.valueOf(myLatitude) + String.valueOf(myLongitude));
        sendJSON();

    }


    private void requestLocationUpdates() {

        Log.i(TAG, " =========== onLocationUpdates ========= ");

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

        //accessing current location
        Log.i(TAG, "======== Before requestLocationUpdates =========");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        Log.i(TAG, "======== After requestLocationUpdates =========");
        mMap.setMyLocationEnabled(true);
        mMap.setPadding(0,400,0,0);



    }

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
    public void onLocationChanged(Location location) {

        Log.i(TAG, "============ onLocationChanged ============");

        //fetching current location

        Log.i("Location", "Latitude: " + String.valueOf(location.getLatitude())
                + "\n" + "Longitude: "+ String.valueOf(location.getLongitude()));

        sendJSON();

    }

    @Override
    protected void onStart() {

        Log.i(TAG, " =========== onStart ========= ");

        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {

        Log.i(TAG, " =========== onResume ========= ");

        super.onResume();

       if(permissionIsGranted) {

            if (mGoogleApiClient.isConnected()) {

                requestLocationUpdates();
            }
        }
    }

    @Override
    protected void onPause() {

        Log.i(TAG, " =========== onPause ========= ");

        super.onPause();

//        if(permissionIsGranted) {

            //pausing the location service while application is not in use
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
//        }
    }

    @Override
    protected void onStop() {

        Log.i(TAG, " =========== onStop ========= ");

        super.onStop();

        if(permissionIsGranted) {
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, " =========== onMapReady ========= ");
        mMap = googleMap;

    }


    public class  HTTPGetRequest  extends AsyncTask<String, Void, String> {

        public static final String REQUEST_METHOD = "GET";
        public static final int READ_TIMEOUT = 15000;
        public static final int CONNECTION_TIMEOUT = 15000;

        @Override
        protected String doInBackground(String... params) {
            Log.i(TAG, " =========== doInBackground ========= ");
            String stringUrl = params[0];
            String result = "";
            String inputLine;


            try {
                //Create a URL object holding our url
                URL myUrl = new URL(stringUrl);

                Log.i("url", stringUrl);

                //Create a connection
                HttpsURLConnection connection =(HttpsURLConnection)
                        myUrl.openConnection();

                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);

                connection.connect();

                InputStreamReader streamReader = new
                        InputStreamReader(connection.getInputStream());

                //Create a new buffered reader and String Builder
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();

                //Check if the line we are reading is not null
                while((inputLine = reader.readLine()) != null){
                    stringBuilder.append(inputLine);
                }

                //Close our InputStream and Buffered reader
                reader.close();
                streamReader.close();

                //Set our result equal to our stringBuilder
                result = stringBuilder.toString();

            } catch ( IOException e){
                Log.e(TAG, e.getMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, " =========== onPostExecute ========= ");
            super.onPostExecute(result);
        }
    }

    public String sendRequest(HashMap<CharSequence, String> waypoints, String place_id, CharSequence name){
        HashMap<CharSequence, String> waypoints2 = new HashMap<CharSequence, String>(waypoints);
        waypoints2.put(name,place_id);
        return sendRequest(waypoints2);

    }
    public String sendRequest(HashMap<CharSequence, String> waypoints) {

        Log.i(TAG, " =========== sendRequest ========= ");

        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        sb.append("origin=");
        if (origin != null) {
            Log.i(TAG + " origin", origin.toString());
            sb.append(origin.getLatLng().latitude);
            sb.append(",");
            sb.append(origin.getLatLng().longitude);
        }else {
            sb.append(myLatitude);
            sb.append(",");
            sb.append(myLongitude);
        }
        sb.append("&destination=");
        if (destinationChanged != null) {
            sb.append(destinationChanged.getLatLng());
            Log.i(TAG + " desChan", destinationChanged.toString());
        } else {
            sb.append("place_id:");
            sb.append(destinationId);
            Log.i(TAG + " desID", destinationId);
        }
        if(!waypoints.isEmpty()){
            Log.i(TAG + " waypoi", waypoints.toString());
            int numWaypoints = waypoints.size();
            int numAdded = 0;
            sb.append("&waypoints=optimize:true|");
            for(String value : waypoints.values()){
                sb.append("place_id:");
                sb.append(value);
                if(numAdded < numWaypoints)
                    sb.append("|");
                numAdded++;
            }
        }
        sb.append("&key=");
        sb.append(getResources().getString(R.string.google_directions_key));


        Log.i(TAG, sb.toString());

        String result = "";
        HTTPGetRequest request = new HTTPGetRequest();
        try {
            result = request.execute(sb.toString()).get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }


    public void updateStopsView (){
        Log.i(TAG, " =========== updateStopsView ========= ");
        StringBuilder stopNames = new StringBuilder();
        int numWaypoints = waypoints.size();
        int numNames = 0;
        for(CharSequence key : waypoints.keySet()){
            stopNames.append(key);
            numNames++;
            Log.i(TAG, String.valueOf(numNames));
            Log.i(TAG, stopNames.toString());
            if(numNames < numWaypoints){
                stopNames.append(", ");
            }
        }
        TextView currentStops = (TextView) findViewById(R.id.currentStopsText);
        currentStops.setText(stopNames.toString());
        currentStops.setVisibility(View.VISIBLE);
        mMap.setPadding(0,450,0,0);
        EditText autocompleteFragmentStops = (EditText)findViewById(R.id.autocompleteStop);
        autocompleteFragmentStops.setText("");
    }

    public void sendJSON(){

        Log.i(TAG, " =========== sendJSON ========= ");

        try {
            json_response = new JSONObject(sendRequest(waypoints));
            if (!json_response.get("status").equals("OK")) {
                Log.e(TAG, "Error getting directions");
                Log.e(TAG, "Status: " + json_response.get("status"));
            } else {
                encodedPolyline = json_response.getJSONArray("routes").getJSONObject(0)
                        .getJSONObject("overview_polyline").getString("points");
                List<LatLng> poly = PolyUtil.decode(encodedPolyline);
                if (currentRoute != null)
                    currentRoute.remove();
                currentRoute = mMap.addPolyline(new PolylineOptions().addAll(poly));
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    public void placeMarker(Place place){
        Log.i(TAG, " =========== sendMarker ========= ");
        if (mMap == null) {
            Log.i(TAG, "MAP is null");
        } else {
            sendJSON();

            mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName().toString()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 12.0f));
            Log.i(TAG, "Place: " + place.getName());


        }
    }

    public class  Autocompleter  extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            CharSequence charSequence = params[0];
            LatLngBounds.Builder bounds = LatLngBounds.builder();
            bounds.include(new LatLng(myLatitude, myLongitude));
            results =
                    Places.GeoDataApi.getAutocompletePredictions(
                            mGoogleApiClient, charSequence.toString(), bounds.build(), null);
            AutocompletePredictionBuffer autocompletePredictions = results.await(60, TimeUnit.SECONDS);

            final com.google.android.gms.common.api.Status status = autocompletePredictions.getStatus();

            if(!status.isSuccess()) {
                Toast.makeText(getApplicationContext(), "Error getting your predictions" + status.toString(),
                        Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Failed to get autocomplete predictions for" + charSequence);
                autocompletePredictions.release();
                return null;
            }
            predictions =
                    DataBufferUtils.freezeAndClose(autocompletePredictions);

            Gson predictionStatus = new Gson();
            return predictionStatus.toJson(status, com.google.android.gms.common.api.Status.class);
        }
    }


}

