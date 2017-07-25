package com.wherzit.sammy.wherzit;

import com.google.maps.model.LatLng;

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


    public static void printRoute(Route routeOBJ) {

        routeOBJ.distance.printDistance(routeOBJ.distance);
        routeOBJ.duration.printDuration(routeOBJ.duration);
        System.out.println(routeOBJ.endAddress);
        System.out.println(routeOBJ.endLocation);
        System.out.println(routeOBJ.startAddress);
        System.out.println(routeOBJ.startLocation);

    }

}

