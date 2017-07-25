package com.wherzit.sammy.wherzit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by saritm on 7/24/17.
 */

public class Distance {

    public String text;
    public String value;

    public Distance(JSONObject jsonDistance) throws JSONException {

       text = jsonDistance.getString("text");
       value = jsonDistance.getString("value");

    }

    public void printDistance(Distance disOBJ) {

        System.out.println("Distance \n" + "text: " + disOBJ.text + " value: " + disOBJ.value);

    }

}

