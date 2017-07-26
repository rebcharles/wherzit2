package com.wherzit.sammy.wherzit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;


/**
 * Created by saritm on 7/25/17.
 */

public class DirectionsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<String> listValues;
    String instructions;
    public static final int FROM_HTML_MODE_LEGACY = 0;
    JSONObject jsonResponse;
    TextView textViewToChange;
    JSONArray steps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        Intent intent = getIntent();
        String jsonString = intent.getStringExtra("jsonResponse");
        try {
            jsonResponse = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("jsonResponse", jsonResponse.toString());


        textViewToChange = (TextView) findViewById(R.id.stepDirections);

        try {
            setText(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            changeStep(jsonResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    protected void setText(int i) throws JSONException {

        steps = jsonResponse.getJSONArray("routes").getJSONObject(0).getJSONArray("legs")
                .getJSONObject(0).getJSONArray("steps");


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

            instructions = Html.fromHtml(steps.getJSONObject(i).getString("html_instructions"),
                    Html.FROM_HTML_MODE_LEGACY).toString();

            textViewToChange.setText(instructions);
        } else {

            instructions = Html.fromHtml(steps.getJSONObject(i).getString("html_instructions")).toString();

            textViewToChange.setText(instructions);
        }
    }

    protected void changeStep(JSONObject jsonOBJ) throws JSONException {

        for (int i = 0; i < steps.length(); i++) {

            JSONObject stepsOBJ = steps.getJSONObject(i);
            int distance = Integer.parseInt(stepsOBJ.getString("value"));

            if (distance == 0) {

                setText(i + 1);

            }

        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //asking users permission to access location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            }

            return;

        }

        mMap.setMyLocationEnabled(true);

    }

}
