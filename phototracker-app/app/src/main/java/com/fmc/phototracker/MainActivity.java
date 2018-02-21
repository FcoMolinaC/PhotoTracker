package com.fmc.phototracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationListener {

    Boolean login;

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

    Boolean private_track = false;
    ArrayList<Location> track = new ArrayList<>();

    private static final int CAMERA_REQUEST = 1888;

    private FloatingActionButton fabRecord, fabTrack, fabPosition, fabPhoto;

    private static HttpClient httpclient;
    private static List<NameValuePair> param_POST;
    private static HttpPost httppost;

    private final static String URL_SERVIDOR = "192.168.0.12";
    private final static String URL_PHP = "http://" + URL_SERVIDOR + "/phototrack/";

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

        Bundle bundle = getIntent().getExtras();
        login = bundle.getBoolean("login");

        initializeMap();
        registerLocationListener();
        myMapController.setZoom(22);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("intentTrack"));

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
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
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
                //todo: cargar las rutas del usuario cuando esté disponible la BBDD
                Intent trackIntent = new Intent(MainActivity.this, TrackList.class);
                startActivity(trackIntent);
            } else {
                registerRequest();
            }
        } else if (id == R.id.nav_gallery) {
            if (login) {
                //todo: mostrar las fotos del usuario cuando esté disponible la BBDD
                Intent galleryIntent = new Intent(MainActivity.this, GalleryActivity.class);
                startActivity(galleryIntent);
            } else {
                registerRequest();
            }
        } else if (id == R.id.nav_explore) {
            //todo: cargar las rutas públicas cuando estén disponibles
        } else if (id == R.id.nav_share) {
            //todo: Compartir foto cuando esté disponible la BBDD
        } else if (id == R.id.nav_send) {
            // todo: Añadir la ruta al mensaje cuando esté disponible
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Mira mi ruta de Phototrack!");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void registerRequest() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.register_dialog, null);
        final EditText etUsername = alertLayout.findViewById(R.id.username);
        final EditText etEmail = alertLayout.findViewById(R.id.password);

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
                String user = etUsername.getText().toString();
                String pass = etEmail.getText().toString();
                Toast.makeText(getBaseContext(), "Registro completo", Toast.LENGTH_SHORT).show();
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
        final CheckBox trackPrivate = alertLayout.findViewById(R.id.track_private);

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
                String track_name = trackName.getText().toString();
                if (trackPrivate.isChecked()) {
                    private_track = true;
                }
                if (track_name.matches("")) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
                    String date = dateFormat.format(new Date());
                    track_name = "Track_" + date;
                }
                Toast.makeText(getBaseContext(), "¡Ruta grabada!", Toast.LENGTH_SHORT).show();
                generateGpx(track_name, track);
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

    private boolean uploadTrack() {
        boolean result;
        httpclient = new DefaultHttpClient();
        httppost = new HttpPost(URL_PHP + "upload-track.php");

        param_POST = new ArrayList<NameValuePair>(2);
        param_POST.add(new BasicNameValuePair("id", ""));
        param_POST.add(new BasicNameValuePair("user", ""));

        try {
            httppost.setEntity(new UrlEncodedFormEntity(param_POST));
            httpclient.execute(httppost);
            result = true;
        } catch (UnsupportedEncodingException e) {
            result = false;
            e.printStackTrace();
            Log.d("error ", String.valueOf(e));
        } catch (ClientProtocolException e) {
            result = false;
            e.printStackTrace();
            Log.d("error ", String.valueOf(e));
        } catch (IOException e) {
            result = false;
            e.printStackTrace();
            Log.d("error ", String.valueOf(e));
        }
        return result;
    }

    class WebService_uploadTrack extends AsyncTask<String, String, String> {
        private Activity context;

        WebService_uploadTrack(Activity context) {
            this.context = context;
        }

        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            String result;

            if (uploadPhoto())
                result = "OK";
            else
                result = "ERROR";
            return result;
        }

        protected void onPostExecute(String result) {
            if (result.equals("OK")) {
                Toast.makeText(context, "Ruta guardada", Toast.LENGTH_SHORT).show();
                login = true;
                Intent mainIntent = new Intent().setClass(
                        MainActivity.this, MainActivity.class);
                mainIntent.putExtra("login", login);
                startActivity(mainIntent);
                finish();
            } else
                Toast.makeText(context, "Error, no se ha podido guardar la ruta", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateGpx(String name, ArrayList<Location> track) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>" +
                "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"MapSource 6.15.5\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n" +
                "<trk><name>" + name + "</name><trkseg>";
        StringBuilder segments = new StringBuilder();
        for (Location location : track) {
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
        } catch (IOException e) {
            Log.e("generateGpx", "Error creando archivo", e);
        }
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


    private boolean uploadPhoto() {
        boolean result;
        httpclient = new DefaultHttpClient();
        httppost = new HttpPost(URL_PHP + "upload-photo.php");

        param_POST = new ArrayList<NameValuePair>(2);
        param_POST.add(new BasicNameValuePair("id", ""));
        param_POST.add(new BasicNameValuePair("user", ""));

        try {
            httppost.setEntity(new UrlEncodedFormEntity(param_POST));
            httpclient.execute(httppost);
            result = true;
        } catch (UnsupportedEncodingException e) {
            result = false;
            e.printStackTrace();
            Log.d("error ", String.valueOf(e));
        } catch (ClientProtocolException e) {
            result = false;
            e.printStackTrace();
            Log.d("error ", String.valueOf(e));
        } catch (IOException e) {
            result = false;
            e.printStackTrace();
            Log.d("error ", String.valueOf(e));
        }
        return result;
    }

    class WebService_uploadPhoto extends AsyncTask<String, String, String> {
        private Activity context;

        WebService_uploadPhoto(Activity context) {
            this.context = context;
        }

        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            String result;

            if (uploadPhoto())
                result = "OK";
            else
                result = "ERROR";
            return result;
        }

        protected void onPostExecute(String result) {
            if (result.equals("OK")) {
                Toast.makeText(context, "Fotografía guardada", Toast.LENGTH_SHORT).show();
                login = true;
                Intent mainIntent = new Intent().setClass(
                        MainActivity.this, MainActivity.class);
                mainIntent.putExtra("login", login);
                startActivity(mainIntent);
                finish();
            } else
                Toast.makeText(context, "Error, no se ha podido guardar la foto", Toast.LENGTH_SHORT).show();
        }
    }
// Fin métodos fotos georreferenciadas
// ----------------------------------------------------------------------------------------------------------------
}
