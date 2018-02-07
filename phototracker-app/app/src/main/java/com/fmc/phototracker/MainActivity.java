package com.fmc.phototracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
    long time = 5000;
    float distance = 5;
    GeoPoint pos;
    OverlayItem myLocationOverlayItem;
    Drawable myCurrentLocationMarker;
    Boolean tracking = false;

    Boolean private_track = false;
    ArrayList<GeoPoint> track = new ArrayList<>();

    private static final int CAMERA_REQUEST = 1888;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static final int ACTION_TAKE_PHOTO = 1;
    private ImageView mImageView;
    private Bitmap mImageBitmap;
    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
    private String mCurrentPhotoPath;

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

        fabRecord = findViewById(R.id.fabRecord);
        fabTrack = findViewById(R.id.fabTrack);
        fabPosition = findViewById(R.id.fabPosition);
        fabPhoto = findViewById(R.id.fabPhoto);

        fabRecord.setVisibility(View.INVISIBLE);

        Bundle bundle = getIntent().getExtras();
        login = bundle.getBoolean("login");

        mAlbumStorageDirFactory = new BaseAlbumDirFactory();

        initializeMap();
        registerLocationListener();
        myMapController.setZoom(22);

        fabTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (login) {
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        if (accuracy <= 30.0) {
                            //To-do: código para comenzar a grabar el recorrido
                            fabTrack.setVisibility(View.INVISIBLE);
                            fabRecord.setVisibility(View.VISIBLE);
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
                tracking = false;
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
        /*if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            dispatchTakePictureIntent(ACTION_TAKE_PHOTO);
        }*/
        switch (requestCode) {
            case ACTION_TAKE_PHOTO: {
                if (resultCode == RESULT_OK) {
                    handleBigCameraPhoto();
                }
                break;
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
                //To-do: cargar las rutas del usuario cuando esté disponible la BBDD
                Intent trackIntent = new Intent(MainActivity.this, TrackList.class);
                startActivity(trackIntent);
            } else {
                registerRequest();
            }
        } else if (id == R.id.nav_gallery) {
            if (login) {
                //To-do: mostrar las fotos del usuario cuando esté disponible la BBDD
                Intent galleryIntent = new Intent(MainActivity.this, GalleryActivity.class);
                startActivity(galleryIntent);
            } else {
                registerRequest();
            }
        } else if (id == R.id.nav_explore) {
            //To-do: cargar las rutas públicas cuando estén disponibles
        } else if (id == R.id.nav_share) {
            //To-do: Compartir foto cuando esté disponible la BBDD
        } else if (id == R.id.nav_send) {
            //To-do: Enviar foto cuando esté disponible la BBDD
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void registerRequest() {
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

    // ----------------------------------------------------------------------------------------------------------------
    // Métodos para mapas y track
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

    public void myPosition() {
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

    public void positionMarker() {
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
        } else {
            Toast.makeText(this, "No hay localización", Toast.LENGTH_SHORT).show();
        }

        pos = new GeoPoint(lat, lon);

        myMapController.animateTo(pos);
        positionMarker();

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
        registerLocationListener();
        if (tracking) {
            track.add(pos);
            //To-do: borrar la tostada en fase de producción
            Toast.makeText(this, "Punto almacenado\nLatitud: " + pos.getLatitude() +
                            "\nLongitud: " + pos.getLongitude() +
                            "\nAltitud: " + pos.getAltitude() +
                            "\nPrecisión: " + Math.round(accuracy) + " m",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
        //Toast.makeText(this, "La señal GPS está disponible", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String s) {
        //Toast.makeText(this, "La señal GPS se ha desactivado", Toast.LENGTH_SHORT).show();
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

    private void tracking() {
        //To-do: Código para grabar recorrido
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
                tracking = false;
                fabRecord.setVisibility(View.INVISIBLE);
                fabTrack.setVisibility(View.VISIBLE);
                Toast.makeText(getBaseContext(), "Cancelado", Toast.LENGTH_SHORT).show();
            }
        });

        alert.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String track_name = trackName.getText().toString();
                if (trackPrivate.isChecked()) {
                    private_track = true;
                }
                tracking = false;
                fabRecord.setVisibility(View.INVISIBLE);
                fabTrack.setVisibility(View.VISIBLE);
                Toast.makeText(getBaseContext(), "¡Ruta grabada!", Toast.LENGTH_SHORT).show();
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

    public boolean uploadTrack() {
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
    // Fin métodos para mapas y track
    // ----------------------------------------------------------------------------------------------------------------

    // ----------------------------------------------------------------------------------------------------------------
    // Métodos para fotos georreferenciadas
    private String getAlbumName() {
        return getString(R.string.album_name);
    }

    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()) {
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }

    private File setUpPhotoFile() throws IOException {
        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }

    private void dispatchTakePictureIntent(int actionCode) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        switch (actionCode) {
            case ACTION_TAKE_PHOTO:
                File f = null;

                try {
                    f = setUpPhotoFile();
                    mCurrentPhotoPath = f.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                } catch (IOException e) {
                    e.printStackTrace();
                    f = null;
                    mCurrentPhotoPath = null;
                }
                break;

            default:
                break;
        }

        startActivityForResult(takePictureIntent, actionCode);
    }

    private void handleBigCameraPhoto() {
        if (mCurrentPhotoPath != null) {
            setPic();
            galleryAddPic();
            mCurrentPhotoPath = null;
        }
    }

        private void setPic() {

        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH); 
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        
        mImageView.setImageBitmap(bitmap);
        mImageView.setVisibility(View.VISIBLE);
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    public boolean uploadPhoto() {
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
