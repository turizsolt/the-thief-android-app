package com.example.zsiri.the_thief_android_app;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by zsiri on 2018.01.31..
 */

public class LocationEntry {
    private double latitude;
    private double longitude;
    private double altitude;
    private double verticalAccuracy;
    private double horisontalAccuracy;
    private double speed;
    private Date timestamp;
    private String who;
    private String displayCharacter;
    
    private LocationEntry(){}
    
    public static LocationEntry fromJSONObject(JSONObject obj) throws JSONException {
        LocationEntry le = new LocationEntry();
        le.latitude = obj.getDouble("latitude");
        le.longitude = obj.getDouble("longitude");
        le.who = obj.getString("who");
        le.displayCharacter = obj.getString("displayCharacter");
        return le;
    }

    public String toString() {
        return
            Double.toString(this.latitude)
            + " " + Double.toString(this.longitude)
            + " (" + this.displayCharacter + " - " + this.who + ")";
    }

    public LatLng getLatLng() {
        return new LatLng(this.latitude, this.longitude);
    }

    public String getDisplayName() {
        return this.who;
    }
}
