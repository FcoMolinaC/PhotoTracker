package com.fmc.phototracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.fmc.phototracker.services.RegisterTrack;
import com.fmc.phototracker.util.SphericalUtil;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationListener {
    Boolean login;
    Bundle bundle;
    String track_name, trackType, track_id, photo_id, downloadUrl;

    MapView myOpenMapView;
    IMapController myMapController;
    CompassOverlay mCompassOverlay;

    Location location;
    LocationManager locationManager;
    double lat, lon, alt, accuracy;
    long fix;
    long time = 0;
    float distance = 10;
    GeoPoint pos;
    OverlayItem myLocationOverlayItem;
    Drawable myCurrentLocationMarker;

    ArrayList<Location> track = new ArrayList<>();
    ArrayList<LatLng> trackLatLng = new ArrayList<>();

    private static final int CAMERA_REQUEST = 1888;

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_LOCATION = 99;

    private FloatingActionButton fabRecord, fabTrack, fabPosition, fabPhoto;

    private UploadTask uploadTask;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        fabRecord = findViewById(R.id.fabRecord);
        fabTrack = findViewById(R.id.fabTrack);
        fabPosition = findViewById(R.id.fabPosition);
        fabPhoto = findViewById(R.id.fabPhoto);

        fabRecord.setVisibility(View.INVISIBLE);

        bundle = getIntent().getExtras();
        assert bundle != null;
        login = bundle.getBoolean("login");

        auth = FirebaseAuth.getInstance();

        initializeMap();
        registerLocationListener();
        myMapController.setZoom(22);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("intentTrack"));

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                // Permiso aceptado
            } else {
                requestPermission();
            }
        } else {
            // No es necesario permiso extra
        }

        checkLocationPermission();

        fabTrack.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                if (login) {
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        // todo: Bajar la precision en produccion
                        if (accuracy <= 30.0) {
                            Intent TrackIntent = new Intent(MainActivity.this, RegisterTrack.class);
                            startService(TrackIntent);
                            fabTrack.setVisibility(View.INVISIBLE);
                            fabRecord.setVisibility(View.VISIBLE);

                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                            track_id = "track_" + timeStamp;

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
                } else {
                    registerRequest();
                }
            }
        });

        fabPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myPosition();
            }
        });

        fabPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (login) {
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        if (accuracy <= 50.0) {
                            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(cameraIntent, CAMERA_REQUEST);
                            photo_id = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                        } else {
                            Snackbar.make(view, "No hay señal gps suficiente para geolocalizar la foto", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    } else {
                        Snackbar.make(view, "GPS desactivado. Actívalo para poder geolocalizar tus fotos", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } else {
                    registerRequest();
                }
            }
        });

        fabRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent TrackIntent = new Intent(MainActivity.this, RegisterTrack.class);
                stopService(TrackIntent);
                registerTrack();
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


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            File pictureFileDir = getDir();

            if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

                Toast.makeText(this, "No se ha podido crear el directorio.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
            String date = dateFormat.format(new Date());
            String photoFile = "Picture_" + date + ".jpg";

            String filename = pictureFileDir.getPath() + File.separator + photoFile;

            File pictureFile = new File(filename);

            FileOutputStream out = null;
            try {
                out = new FileOutputStream(pictureFile);
                photo.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        Toast.makeText(this, "Imagen guardada:" + photoFile,
                                Toast.LENGTH_LONG).show();
                        out.close();
                        uploadPhoto(photoFile, filename);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "No se puede guardar la imeagen.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
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
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.routes) {
            if (login) {
                openWebURL("http://phototracker/tracks");
            } else {
                registerRequest();
            }
        } else if (id == R.id.photos) {
            if (login) {
                openWebURL("http://phototracker/photos");
            } else {
                registerRequest();
            }
        } else if (id == R.id.adjust) {
            if (login) {
                openWebURL("http://phototracker/adjust");
            } else {
                registerRequest();
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void openWebURL(String inURL) {
        Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(inURL));
        startActivity(browse);
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
            }
        } else {
        }
    }


    private void registerRequest() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.register_dialog, null);
        final EditText etEmail = alertLayout.findViewById(R.id.email);
        final EditText etPassword = alertLayout.findViewById(R.id.password);

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("Regístrate y podrás grabar rutas y subir fotos");
        alert.setView(alertLayout);
        alert.setCancelable(false);
        alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alert.setPositiveButton("Registrar", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Tienes que introducir un email válido", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Tienes que introducir una contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                    return;
                }

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Toast.makeText(MainActivity.this, "Registrado con éxito" + task.isSuccessful(), Toast.LENGTH_SHORT).show();

                                if (!task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Registro fallido" + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    login = true;
                                    Intent mainIntent = new Intent(MainActivity.this, MainActivity.class);
                                    mainIntent.putExtra("login", login);
                                    startActivity(mainIntent);
                                    finish();
                                }
                            }
                        });

                login = true;
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            track = intent.getParcelableArrayListExtra("Track");
        }
    };

    // ----------------------------------------------------------------------------------------------------------------
    // Métodos para mapas y track
    private void initializeMap() {
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

    private void myPosition() {
        String _lat = String.format("%.4f", lat);
        String _lon = String.format("%.4f", lon);
        String _alt = String.format("%.1f", alt);

        registerLocationListener();
        Toast.makeText(this, "Latitud: " + _lat +
                        "\nLongitud: " + _lon +
                        "\nAltitud: " + _alt +
                        "\nPrecisión: " + Math.round(accuracy) + " m",
                Toast.LENGTH_SHORT).show();
    }

    private void positionMarker(GeoPoint pos) {
        DefaultResourceProxyImpl resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
        myLocationOverlayItem = new OverlayItem("Here", "Current Position", pos);
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
    }

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

        if (location != null) {
            lat = location.getLatitude();
            lon = location.getLongitude();
            alt = location.getAltitude();
            fix = location.getTime();
            accuracy = location.getAccuracy();
        } else {
            Toast.makeText(this, "No hay localización", Toast.LENGTH_SHORT).show();
        }

        pos = new GeoPoint(lat, lon);

        myMapController.animateTo(pos);
        positionMarker(pos);

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
        myOpenMapView.getOverlays().clear();

        GeoPoint pos = new GeoPoint(location.getLatitude(), location.getLongitude());
        myMapController.animateTo(pos);
        positionMarker(pos);
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

    @SuppressLint({"NewApi", "StaticFieldLeak"})
    protected class GeocodeTask extends AsyncTask<LatLong, Void, String> {
        @Override
        protected String doInBackground(LatLong... params) {
            Geocoder gc = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresslist;
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

    private void registerTrack() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.register_track, null);
        final EditText trackName = alertLayout.findViewById(R.id.track_name);

        final Spinner spinner = alertLayout.findViewById(R.id.trackType);
        String[] types = {"Ciclismo", "Senderismo", "Carrera a pie", "Caminata"};
        spinner.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, types));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("Guardar ruta");
        alert.setView(alertLayout);
        alert.setCancelable(false);
        alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                fabRecord.setVisibility(View.INVISIBLE);
                fabTrack.setVisibility(View.VISIBLE);
                Toast.makeText(getBaseContext(), "Cancelado", Toast.LENGTH_SHORT).show();
            }
        });

        alert.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                fabRecord.setVisibility(View.INVISIBLE);
                fabTrack.setVisibility(View.VISIBLE);
                track_name = trackName.getText().toString();
                trackType = spinner.getSelectedItem().toString();
                if (track_name.matches("")) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
                    String date = dateFormat.format(new Date());
                    track_name = "Track_" + date;
                }
                Toast.makeText(getBaseContext(), "¡Ruta grabada!", Toast.LENGTH_SHORT).show();
                generateGpx(track_name, track);
                stopService(new Intent(MainActivity.this, RegisterTrack.class));
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    class LatLong {
        double lat, lon;

        LatLong(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }

    private void generateGpx(String name, ArrayList<Location> track) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>" +
                "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"MapSource 6.15.5\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n" +
                "<trk><name>" + name + "</name><trkseg>";
        StringBuilder segments = new StringBuilder();
        for (Location location : track) {
            trackLatLng.add(new LatLng(location.getLatitude(), location.getLongitude()));

            segments
                    .append("<trkpt lat=\"")
                    .append(location.getLatitude())
                    .append("\" lon=\"")
                    .append(location.getLongitude())
                    .append("\"><ele>")
                    .append(location.getAltitude())
                    .append("</ele><time>")
                    .append(df.format(new Date(location.getTime())))
                    .append("</time></trkpt>\n");
        }
        String footer = "</trkseg></trk></gpx>";

        File trackFileDir = getDir();

        if (!trackFileDir.exists() && !trackFileDir.mkdirs()) {
            Toast.makeText(this, "No se ha podido crear el directorio.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String trackName = "Track_" + name + ".gpx";

        String filename = trackFileDir.getPath() + File.separator + trackName;

        File trackFile = new File(filename);

        try {
            FileWriter writer = new FileWriter(trackFile, true);
            writer.write(header);
            writer.append(segments.toString());
            writer.append(footer);
            writer.flush();
            writer.close();
            //todo: borrar en fase de produccion
            Toast.makeText(this, "Archivo creado", Toast.LENGTH_SHORT).show();

            uploadTrack(filename, trackName);

        } catch (IOException e) {
            Log.e("generateGpx", "Error creando archivo", e);
        }
    }

    private void uploadTrack(String filename, final String trackName) throws FileNotFoundException {
        StorageReference storageRef = storage.getReference();
        final StorageReference trackRef = storageRef.child("tracks/" + trackName);
        final DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        final DecimalFormat f = new DecimalFormat("##.00");
        final double trackLong = SphericalUtil.computeLength(trackLatLng);

        InputStream stream = new FileInputStream(new File(filename));
        uploadTask = trackRef.putStream(stream);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return trackRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    downloadUrl = downloadUri.toString();
                } else {
                    Toast.makeText(MainActivity.this, "Error subiendo ruta", Toast.LENGTH_SHORT).show();
                    Log.e("uploadTrack", "Error creando archivo");
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference(user.getUid());

                Map<String, Object> track = new HashMap<>();
                String trackName = track_name + "_" + track_id;
                track.put(trackName, track_id);
                ref.child("/tracks").updateChildren(track);

                Map<String, Object> trackData = new HashMap<>();
                trackData.put("/name", track_name);
                trackData.put("/trackID", track_id);
                trackData.put("/track", trackLatLng);
                trackData.put("/url", downloadUrl);
                trackData.put("/long", f.format(trackLong));
                trackData.put("/date", df.format(new Date()));
                trackData.put("/type", trackType);
                ref.child("/tracks/" + trackName).updateChildren(trackData);

                Toast.makeText(MainActivity.this, "Ruta guardada", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error subiendo ruta", Toast.LENGTH_SHORT).show();
                Log.e("uploadTrack", "Error creando archivo", e);
            }
        });
    }

    // Fin métodos para mapas y track
    // ----------------------------------------------------------------------------------------------------------------

    // ----------------------------------------------------------------------------------------------------------------
    // Métodos para fotos georreferenciadas
    private File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        return new File(sdDir, "PhotoTrack");
    }

    private void uploadPhoto(String photoFile, String filename) throws FileNotFoundException {
        StorageReference storageRef = storage.getReference();
        final StorageReference photoRef = storageRef.child("photos/" + photoFile);
        final DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        InputStream stream = new FileInputStream(new File(filename));
        uploadTask = photoRef.putStream(stream);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return photoRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    downloadUrl = downloadUri.toString();
                } else {
                    Toast.makeText(MainActivity.this, "Error subiendo ruta", Toast.LENGTH_SHORT).show();
                    Log.e("uploadTrack", "Error creando archivo");
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference(user.getUid());

                Map<String, Object> photo = new HashMap<>();
                String photoName = "photo_" + photo_id;
                photo.put(photoName, photo_id);
                ref.child("/photos").updateChildren(photo);

                Map<String, Object> photoData = new HashMap<>();
                photoData.put("/date", df.format(new Date(location.getTime())));
                photoData.put("/url", downloadUrl);

                if (location != null) {
                    lat = location.getLatitude();
                    lon = location.getLongitude();

                    photoData.put("/latitude", lat);
                    photoData.put("/longitude", lon);
                } else {
                    Log.e("uploadTrack", "Coordenadas no disponibles");
                }

                ref.child("/photos/" + photoName).updateChildren(photoData);

                Toast.makeText(MainActivity.this, "Foto guardada", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error subiendo foto", Toast.LENGTH_SHORT).show();
                Log.e("uploadPhoto", "Error creando archivo", e);
            }
        });
    }
// Fin métodos fotos georreferenciadas
// ----------------------------------------------------------------------------------------------------------------
}
