package com.fmc.phototracker;

import android.app.Activity;
import android.os.Bundle;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class TrackMap extends Activity {

    MapView myOpenMapView;
    IMapController myMapController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.track_map);

        myOpenMapView = findViewById(R.id.map);
        myOpenMapView.setTileSource(TileSourceFactory.MAPNIK);

        myMapController = myOpenMapView.getController();

        myOpenMapView.setBuiltInZoomControls(true);
        myOpenMapView.setMultiTouchControls(true);

        GeoPoint pos = new GeoPoint(37.076854, -3.092044);
        myMapController.animateTo(pos);
        myMapController.setZoom(18);
    }
}
