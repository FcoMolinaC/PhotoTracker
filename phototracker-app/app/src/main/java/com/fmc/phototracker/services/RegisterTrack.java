package com.fmc.phototracker.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.fmc.phototracker.MainActivity;
import com.fmc.phototracker.R;

import java.util.ArrayList;

public class RegisterTrack extends Service
        implements LocationListener {

    Location location;
    LocationManager locationManager;
    double accuracy;
    long time = 0;
    float distance = 10;
    ArrayList<Location> track = new ArrayList<>();

    PowerManager mgr;
    PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        assert mgr != null;
        mgr = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        wakeLock.acquire(60 * 60 * 1000L);
        registerLocationListener();
        showForegroundNotification();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        wakeLock.release();
        sendMessageToActivity(track);
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendMessageToActivity(ArrayList track) {
        Intent intent = new Intent("intentTrack");
        intent.putExtra("Track", track);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private static final int NOTIFICATION_ID = 1;

    private void showForegroundNotification() {
        Intent showTaskIntent = new Intent(getApplicationContext(), MainActivity.class);
        showTaskIntent.setAction(Intent.ACTION_MAIN);
        showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        showTaskIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, showTaskIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Grabando ruta")
                .setSmallIcon(R.drawable.logo)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Métodos para track
    @SuppressLint("NewApi")
    private void registerLocationListener() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(false);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_LOW);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        assert locationManager != null;

        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, time, distance, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, this);
        if (locationManager.getAllProviders().contains("network")) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, this);
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //todo; bajar precision en modo produccion
        if (location.getAccuracy() < 10) {
            track.add(location);
            //todo: borrar la tostada en fase de producción
            Toast.makeText(this, "Punto almacenado\nLatitud: " + location.getLatitude() +
                            "\nLongitud: " + location.getLongitude() +
                            "\nAltitud: " + location.getAltitude() +
                            "\nPrecisión: " + Math.round(accuracy) + " m",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Precisión insuficiente para grabar punto", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }
    // Fin métodos para mapas y track
    // ----------------------------------------------------------------------------------------------------------------
}
