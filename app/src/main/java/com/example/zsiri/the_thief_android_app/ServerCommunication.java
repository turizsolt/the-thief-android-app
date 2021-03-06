package com.example.zsiri.the_thief_android_app;

import android.app.Activity;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

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
import java.text.ParseException;

/**
 * Created by zsiri on 2018.01.31..
 */

public class ServerCommunication {
    private static final String LOG_TAG = "ServerCommunication";
    public static final int DEFAULT_CAMERA_ZOOM = 14;
    public static final String DEFAULT_NAME = "Anonymus";
    public static final String DEFAULT_SERVER_ADDRESS = "http://localhost:3000/";
    private static ServerCommunication __instance;
    private Activity activity;
    private GoogleMap map;
    private Socket socket;

    private String name = "";
    private String serverAddress;

    private boolean started = false;

    public static ServerCommunication getInstance() {
        if(ServerCommunication.__instance == null){
            ServerCommunication.__instance = new ServerCommunication();
        }
        return ServerCommunication.__instance;
    }

    private ServerCommunication() {

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
            } catch (ParseException e) {
                return;
            }
            }
        });
        }
    };

    private Emitter.Listener onConnected = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, "Connected to the server.", Toast.LENGTH_SHORT).show();
            }
        });
        }
    };

    private void iterateOnJsonArray(JSONObject data, String part) throws JSONException, ParseException {
        JSONArray people = data.getJSONArray(part);
        Log.v(LOG_TAG, part);
        for (int i = 0; i < people.length(); i++) {
            getAndMarkFromJsonObject(people, i, part.equals("thiefs"));
        }
    }

    private void getAndMarkFromJsonObject(JSONArray people, int i, boolean isThief) throws JSONException, ParseException {
        JSONObject personObject = people.getJSONObject(i);
        LocationEntry person = LocationEntry.fromJSONObject(personObject);
        Log.v(LOG_TAG, person.toString());

        if(map != null){
            map.addMarker(new MarkerOptions()
                .position(person.getLatLng())
                .title(person.getDisplayName())
                .icon(BitmapDescriptorFactory
                    .defaultMarker(
                        ( isThief
                            ? BitmapDescriptorFactory.HUE_RED
                            : BitmapDescriptorFactory.HUE_BLUE)))
                    .alpha(person.isFreshDate()?1f:0.3f)
            );
        }
    }

    public void setMap(GoogleMap map) {
        this.map = map;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }



    public void sendLocation(Location location) {
        LocationEntry le = LocationEntry.fromLocation(location);
        le.setIdentity(Character.toString(this.name.charAt(0)), this.name);
        try {
            socket.emit("check", le.toJsonObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;

        if(socket != null && socket.connected()){
            socket.disconnect();
        }

        try {
            socket = IO.socket(this.serverAddress);
            socket.on("connected", onConnected);
            socket.on("coordinates", onCoordinates);
            socket.connect();
        } catch (URISyntaxException e) {
        }
    }

    public String getName() {
        if(name != null) return name;
        return DEFAULT_NAME;
    }

    public String getServerAddress() {
        if(serverAddress != null) return serverAddress;
        return DEFAULT_SERVER_ADDRESS;
    }

    public void sendStart() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("thief", this.name);

            socket.emit("start", obj);
            started = true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendStop() {
        socket.emit("stop");
        started = false;
    }

    public boolean isStarted() {
        return started;
    }
}
