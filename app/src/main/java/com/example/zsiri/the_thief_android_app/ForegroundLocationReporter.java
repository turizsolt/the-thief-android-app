package com.example.zsiri.the_thief_android_app;

/**
 * Created by zsiri on 2018.01.31..
 */

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import android.os.Handler;

import java.util.Random;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class ForegroundLocationReporter extends Service {
        private static final String LOG_TAG = "ForegroundService";
        public static boolean IS_SERVICE_RUNNING = false;
        public static Activity ACTIVITY = null;
        private int counter = 0;
        private Handler h;
        private Runnable r;
        private FusedLocationProviderClient mFusedLocationClient;
        private Location lastLocation = null;
        private LocationCallback mLocationCallback;
        private ServerCommunication server;

        @Override
        public void onCreate() {
            super.onCreate();
            server = ServerCommunication.getInstance();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            System.out.println("INTENT:"+intent);

            if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
                Log.i(LOG_TAG, "Received Start Foreground Intent ");
                System.out.println("INTENT:"+intent);

                showNotification();
                Toast.makeText(this, "Service Started!", Toast.LENGTH_SHORT).show();

                // asking for location

                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

                LocationRequest mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(5000);
                mLocationRequest.setFastestInterval(5000);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                mLocationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        for (Location location : locationResult.getLocations()) {
                            lastLocation = location;

                            Context context = getApplicationContext();
                            CharSequence text = "Loc: "+location.getLongitude()+", "+location.getLatitude();
                            int duration = Toast.LENGTH_SHORT;

                            Random r = new Random();
                            if(r.nextInt(10) < 1) {
                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            }

                            server.sendLocation(location);

                            Log.i(LOG_TAG, "LOCATION: "+text);
                        }
                    }

                    ;
                };

                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                } else {
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                            mLocationCallback,
                            null);
                }
            } else if (intent.getAction().equals(
                    Constants.ACTION.STOPFOREGROUND_ACTION)) {
                Log.i(LOG_TAG, "Received Stop Foreground Intent");
                stopForeground(true);
                stopSelf();
            }
            return START_STICKY;
        }

        private void showNotification() {
            Intent notificationIntent = new Intent(this, MainActivity.class) ;
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.cast_ic_notification_play);

            Notification notification = new NotificationCompat.Builder(this)
                    .setPriority(2)
                    .setContentTitle("TutorialsFace Music Player")
                    .setTicker("TutorialsFace Music Player")
                    .setContentText("My song")
                    .setSmallIcon(R.drawable.cast_ic_notification_play)
                    //.setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification);

            h = new Handler();
            r = new Runnable() {
                @Override
                public void run() {
                    startForeground(101, updateNotification());
                    h.postDelayed(this, 1000);
                }
            };
            h.post(r);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.i(LOG_TAG, "In onDestroy");
            Toast.makeText(this, "Service Detroyed!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        private Notification updateNotification() {
            counter++;

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, MainActivity.class), 0);

            String info = counter + "";

            return new NotificationCompat.Builder(this)
                    .setContentTitle(info)
                    .setTicker(info)
                    .setContentText(lastLocation!=null?(lastLocation.getLongitude()+", "+lastLocation.getLatitude()+" @"+lastLocation.getTime()):"Not available")
                    .setSmallIcon(R.drawable.cast_ic_notification_play)
                    //.setLargeIcon(
                    //        Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                    //                R.drawable.cast_ic_notification_play), 128, 128, false))
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("almafa"))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();
        }
    }
