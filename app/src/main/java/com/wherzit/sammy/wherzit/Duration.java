package com.wherzit.sammy.wherzit;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by saritm on 7/24/17.
 */

public class Duration {

    public String text;
    public String value;

    public Duration(JSONObject jsonDuration) throws JSONException {

        text = jsonDuration.getString("text");
        value = jsonDuration.getString("value");

    }

    public void printDuration(Duration durOBJ) {

        System.out.println("Duration: \n" + "text: " + durOBJ.text + " value: " + durOBJ.value);

    }


}
