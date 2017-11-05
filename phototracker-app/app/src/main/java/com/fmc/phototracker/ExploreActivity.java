package com.fmc.phototracker;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;

public class ExploreActivity extends AppCompatActivity {

    private MapView myOpenMapView;
    private IMapController myMapController;
    private CompassOverlay mCompassOverlay;
    private ScaleBarOverlay mScaleBarOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        myOpenMapView = findViewById(R.id.openmapview);
        myOpenMapView.setTileSource(TileSourceFactory.MAPNIK);
        myOpenMapView.setMultiTouchControls(true);
        myOpenMapView.setBuiltInZoomControls(true);

        myMapController = myOpenMapView.getController();
        myMapController.setZoom(4);

        GeoPoint center = new GeoPoint(40.416963, -3.703531);
        myMapController.animateTo(center);

        Context ctx = getApplicationContext();
        this.mCompassOverlay = new CompassOverlay(ctx, new InternalCompassOrientationProvider(ctx), myOpenMapView);
        this.mCompassOverlay.enableCompass();
        myOpenMapView.getOverlays().add(this.mCompassOverlay);

        this.mScaleBarOverlay = new ScaleBarOverlay(ctx);
        this.mScaleBarOverlay.setCentred(true);
    }
}
