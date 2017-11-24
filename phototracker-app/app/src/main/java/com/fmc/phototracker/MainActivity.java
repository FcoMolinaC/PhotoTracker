package com.fmc.phototracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationListener {

    MapView myOpenMapView;
    IMapController myMapController;
    CompassOverlay mCompassOverlay;

    Location location;
    LocationManager locationManager;
    double lat, lon, alt, accuracy;
    long fix;
    long time = 5000;
    float distance = 5;
    GeoPoint pos;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    Drawable myCurrentLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeMap();
        registerLocationListener();

        FloatingActionButton fabTrack = findViewById(R.id.fabTrack);
        fabTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    if (accuracy <= 20.0) {
                        //To-do: código para comenzar a grabar el recorrido
                        tracking();
                        Snackbar.make(view, "Comenzando a grabar recorrido", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else {
                        Snackbar.make(view, "No hay precisión suficiente para grabar un recorrido", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } else {
                    Snackbar.make(view, "GPS desactivado. Actívalo para comenzar a grabar recorrido", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        FloatingActionButton fabPhoto = findViewById(R.id.fabPhoto);
        fabPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Foto georreferenciada", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                try {
                    dispatchTakePictureIntent();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.routes) {

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_explore) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void initializeMap() {
        Context ctx = getApplicationContext();

        myOpenMapView = findViewById(R.id.openmapview);
        myOpenMapView.setTileSource(TileSourceFactory.MAPNIK);
        myOpenMapView.setMultiTouchControls(true);
        myOpenMapView.setBuiltInZoomControls(true);

        myMapController = myOpenMapView.getController();

        mCompassOverlay = new CompassOverlay(ctx, new InternalCompassOrientationProvider(ctx), myOpenMapView);
        mCompassOverlay.enableCompass();
        myOpenMapView.getOverlays().add(this.mCompassOverlay);
    }

    public void registerLocationListener() {
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

        if (location != null) {
            lat = location.getLatitude();
            lon = location.getLongitude();
            alt = location.getAltitude();
            fix = location.getTime();
            accuracy = location.getAccuracy();
            Toast.makeText(this, "Latitud: " + lat +
                            ",\nLongitud: " + lon +
                            "\nAltitud: " + alt +
                            "\nPrecision: " + accuracy + " m",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No hay localización", Toast.LENGTH_SHORT).show();

        }

        pos = new GeoPoint(lat, lon);

        myMapController.animateTo(pos);
        myMapController.setZoom(15);

        DefaultResourceProxyImpl resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
        OverlayItem myLocationOverlayItem = new OverlayItem("Here", "Current Position", pos);
        myCurrentLocationMarker = this.getResources().getDrawable(R.drawable.point);
        myLocationOverlayItem.setMarker(myCurrentLocationMarker);
        final ArrayList<OverlayItem> items = new ArrayList<>();
        items.add(myLocationOverlayItem);
        Overlay currentLocationOverlay = new ItemizedIconOverlay<>(items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                return true;
            }

            public boolean onItemLongPress(final int index, final OverlayItem item) {
                return true;
            }
        }, resourceProxy);
        this.myOpenMapView.getOverlays().add(currentLocationOverlay);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GeocodeTask task = new GeocodeTask();
                task.execute(new LatLong(lat, lon));
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        myCurrentLocationMarker.setVisible(false, true);
        registerLocationListener();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
        Toast.makeText(this, "La señal GPS está disponible", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(this, "La señal GPS se ha desactivado", Toast.LENGTH_SHORT).show();
    }

    protected class GeocodeTask extends AsyncTask<LatLong, Void, String> {
        @Override
        protected String doInBackground(LatLong... params) {
            Geocoder gc = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresslist = null;
            StringBuffer returnString = new StringBuffer();

            try {
                addresslist = gc.getFromLocation(lat, lon, 3);

                if (addresslist == null) {
                    return "Unknown Address";
                } else {
                    if (addresslist.size() > 0) {
                        Address add = addresslist.get(0);
                        for (int i = 0; i < add.getMaxAddressLineIndex(); i++) {
                            returnString.append(add.getAddressLine(i)).append("\n");
                        }

                        returnString.append(add.getLocality()).append("\n");
                    }
                }
            } catch (IOException e) {
                returnString = new StringBuffer("Unknown Address");
            } finally {
                if (returnString.toString().length() < 5) {
                    return "Unknown Address";
                }
                return returnString.toString();
            }
        }
    }

    private void dispatchTakePictureIntent() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            createImageFile();
        }
    }

    private void createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PhT_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpeg",
                storageDir
        );

        String currentPhoto = image.getAbsolutePath();
    }

    private void tracking() {
        //To-do: Código para grabar recorrido
    }

    class LatLong {
        double lat, lon;

        LatLong(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }
}
