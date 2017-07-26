package com.wherzit.sammy.wherzit;

import android.util.Log;

import com.google.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by saritm on 7/24/17.
 */

public class Route {


    public Distance distance;
    public Duration duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;
    public String instructions;



    public static void printRoute(Route routeOBJ) {

        routeOBJ.distance.printDistance(routeOBJ.distance);
        routeOBJ.duration.printDuration(routeOBJ.duration);
        Log.i("testEndAddress", routeOBJ.endAddress);
        Log.i("testLocation",routeOBJ.endLocation.toString());
        Log.i("testStart", routeOBJ.startAddress);
        Log.i("testLocation", routeOBJ.startLocation.toString());

    }

}

