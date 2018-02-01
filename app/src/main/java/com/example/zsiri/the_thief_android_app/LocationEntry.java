package com.example.zsiri.the_thief_android_app;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by zsiri on 2018.01.31..
 */

public class LocationEntry {
    public static final int TIMEOUT_FRESH_DATE = 5 * 60 * 1000;
    private double latitude;
    private double longitude;
    private double altitude;
    private double verticalAccuracy;
    private double horisontalAccuracy;
    private double speed;
    private long timestamp;
    private String who;
    private String displayCharacter;
    
    private LocationEntry(){}
    
    public static LocationEntry fromJSONObject(JSONObject obj) throws JSONException, ParseException {
        LocationEntry le = new LocationEntry();
        le.latitude = obj.getDouble("latitude");
        le.longitude = obj.getDouble("longitude");
        le.who = obj.getString("who");
        le.displayCharacter = obj.getString("displayCharacter");
        le.timestamp = obj.getLong("timestamp");
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

    public static LocationEntry fromLocation(Location location) {
        LocationEntry le = new LocationEntry();
        le.latitude = location.getLatitude();
        le.longitude = location.getLongitude();
        le.altitude = location.getAltitude();
        le.horisontalAccuracy = location.getAccuracy();
        le.verticalAccuracy = location.getAccuracy();
        le.speed = location.getSpeed();
        le.timestamp = location.getTime();
        return le;
    }

    public void setIdentity(String displayCharacter, String who) {
        this.displayCharacter = displayCharacter;
        this.who = who;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("latitude", this.latitude);
        obj.put("longitude", this.longitude);
        obj.put("altitude", this.altitude);
        obj.put("horisontalAccuracy", this.horisontalAccuracy);
        obj.put("verticalAccuracy", this.verticalAccuracy);
        obj.put("speed", this.speed);
        obj.put("timestamp", this.timestamp);
        obj.put("displayCharacter", this.displayCharacter);
        obj.put("who", this.who);
        return obj;

    }

    public boolean isFreshDate() {
        return this.timestamp > (new Date().getTime() - TIMEOUT_FRESH_DATE);
    }
}
