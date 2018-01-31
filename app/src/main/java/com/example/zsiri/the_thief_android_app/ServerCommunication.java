package com.example.zsiri.the_thief_android_app;

import android.app.Activity;
import android.location.Location;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 * Created by zsiri on 2018.01.31..
 */

public class ServerCommunication {
    private static final String LOG_TAG = "ServerCommunication";
    public static final int DEFAULT_CAMERA_ZOOM = 14;
    private static ServerCommunication __instance;
    private Activity activity;
    private GoogleMap map;
    private boolean mapPositioned;
    private Socket socket;

    public static ServerCommunication getInstance() {
        if(ServerCommunication.__instance == null){
            ServerCommunication.__instance = new ServerCommunication();
        }
        return ServerCommunication.__instance;
    }

    private ServerCommunication() {
        this.mapPositioned = false;

        try {
            socket = IO.socket("http://192.168.0.192:3000/");
            socket.on("coordinates", onCoordinates);
            socket.connect();
        } catch (URISyntaxException e) {
        }
    }

    private Emitter.Listener onCoordinates = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        map.clear();
                        JSONObject data = (JSONObject) args[0];
                        iterateOnJsonArray(data, "cops");
                        iterateOnJsonArray(data, "thiefs");
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

    private void iterateOnJsonArray(JSONObject data, String part) throws JSONException {
        JSONArray people = data.getJSONArray(part);
        Log.i(LOG_TAG, part);
        for (int i = 0; i < people.length(); i++) {
            getAndMarkFromJsonObject(people, i, part.equals("thiefs"));
        }
    }

    private void getAndMarkFromJsonObject(JSONArray people, int i, boolean isThief) throws JSONException {
        JSONObject personObject = people.getJSONObject(i);
        LocationEntry person = LocationEntry.fromJSONObject(personObject);
        Log.i(LOG_TAG, person.toString());

        if(map != null){
            map.addMarker(new MarkerOptions()
                .position(person.getLatLng())
                .title(person.getDisplayName())
                .icon(BitmapDescriptorFactory
                    .defaultMarker(
                        isThief
                        ? BitmapDescriptorFactory.HUE_RED
                        : BitmapDescriptorFactory.HUE_BLUE))
            );

            if(!mapPositioned) {
                map.moveCamera(CameraUpdateFactory.newLatLng(person.getLatLng()));
                map.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_CAMERA_ZOOM));
                mapPositioned = true;
            }
        }
    }

    public void setMap(GoogleMap map) {
        this.map = map;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void sendLocation(Location location, String displayCharacter, String who) {
        LocationEntry le = LocationEntry.fromLocation(location);
        le.setIdentity(displayCharacter, who);
        try {
            socket.emit("check", le.toJsonObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
