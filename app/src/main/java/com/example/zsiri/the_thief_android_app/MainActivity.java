package com.example.zsiri.the_thief_android_app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private static final String LOG_TAG = "MainActivity";
    public static final int ACTIVITY_GET_USER_SETTINGS = 1;

    private GoogleMap mMap;
    private ServerCommunication server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        server = ServerCommunication.getInstance();
        server.setActivity(this);

        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        String name = settings.getString("name", ServerCommunication.DEFAULT_NAME);
        String serverAddress = settings.getString("serverAddress", ServerCommunication.DEFAULT_SERVER_ADDRESS);

        server.setName(name);
        server.setServerAddress(serverAddress);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        String name = settings.getString("name", ServerCommunication.DEFAULT_NAME);
        String serverAddress = settings.getString("serverAddress", ServerCommunication.DEFAULT_SERVER_ADDRESS);

        TextView textViewMenuServerAddress = (TextView) findViewById(R.id.textViewMenuServerAddress);
        TextView textViewMenuName = (TextView) findViewById(R.id.textViewMenuName);
        textViewMenuName.setText(name);
        textViewMenuServerAddress.setText(serverAddress);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            Intent activity = new Intent(MainActivity.this, UserSettingsActivity.class);
            activity.putExtra("name", server.getName());
            activity.putExtra("serverAddress", server.getServerAddress());
            startActivityForResult(activity, ACTIVITY_GET_USER_SETTINGS);


        } else if (id == R.id.nav_toggle) {

            final Intent service = new Intent(MainActivity.this, ForegroundLocationReporter.class);
            if (!ForegroundLocationReporter.IS_SERVICE_RUNNING) {
                //MenuItem i = (MenuItem) findViewById(R.id.nav_toggle);
                item.setTitle("STOP tracking");

                service.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                ForegroundLocationReporter.IS_SERVICE_RUNNING = true;
                ForegroundLocationReporter.ACTIVITY = this;
                startService(service);
            } else {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //MenuItem i = (MenuItem) findViewById(R.id.nav_toggle);
                                item.setTitle("Start tracking");

                                service.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
                                ForegroundLocationReporter.IS_SERVICE_RUNNING = false;
                                startService(service);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Are you sure to stop tracking?\n(You should not stop during a game.)").setPositiveButton("STOP IT", dialogClickListener)
                        .setNegativeButton("Cancel", dialogClickListener).show();



            }

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        server.setMap(mMap);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(LOG_TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(LOG_TAG, "onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(LOG_TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v(LOG_TAG, "onRestart");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ACTIVITY_GET_USER_SETTINGS) {
            if(resultCode == Activity.RESULT_OK){
                String name = data.getStringExtra("name");
                String serverAddress = data.getStringExtra("serverAddress");
                Log.v(LOG_TAG, "name: "+name);
                Log.v(LOG_TAG, "name serverA: "+serverAddress);
                // setting the new data on server
                server.setName(name);
                server.setServerAddress(serverAddress);

                // setting the new data in shared preferences
                SharedPreferences settings = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("name", name);
                editor.putString("serverAddress", serverAddress);
                editor.commit();

                TextView textViewMenuServerAddress = (TextView) findViewById(R.id.textViewMenuServerAddress);
                TextView textViewMenuName = (TextView) findViewById(R.id.textViewMenuName);
                textViewMenuName.setText(name);
                textViewMenuServerAddress.setText(serverAddress);

                Toast.makeText(this, "Settings changed successfully.", Toast.LENGTH_SHORT).show();

            }
        }
    }
}
