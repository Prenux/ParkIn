package org.prenux.parkin;

import android.content.Context;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MapEventsReceiver{

    MapView map;
    private ArrayList<Marker> markerArrayList;
    String userAgent = "org.prenux.parkin";

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);

        markerArrayList = new ArrayList<>();

        //Initiate Map
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setMaxZoomLevel(21);

        //Set default view point
        IMapController mapController = map.getController();
        mapController.setZoom(19);
        GeoPoint startPoint = new GeoPoint(45.500997, -73.615783);
        mapController.setCenter(startPoint);

        //Set Marker on default view point
        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        markerArrayList.add(startMarker);
        map.getOverlays().add(startMarker);
        startMarker.setTitle("Super Smash School");

        //Set map event listener overlay
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, this);
        map.getOverlays().add(0, mapEventsOverlay);
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        Toast.makeText(this, "Tap on ("+p.getLatitude()+","+p.getLongitude()+")", Toast.LENGTH_SHORT).show();
        InfoWindow.closeAllInfoWindowsOn(map);
        removeAllMarkers();
        map.invalidate();
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        Marker pressedMarker = new Marker(map);
        pressedMarker.setPosition(p);
        pressedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        markerArrayList.add(pressedMarker);
        map.getOverlays().add(pressedMarker);
        pressedMarker.setTitle(p.getLatitude()+","+p.getLongitude());
        map.invalidate();
        return true;
    }

    private void removeAllMarkers() {
        for (Marker marker: markerArrayList) {
            marker.remove(map);
        }
        markerArrayList.clear();
    }

    //Get address from a geolocalisation
    public String getAddressFromGeoPoint(GeoPoint p){
        String address;
        GeocoderNominatim gc = new GeocoderNominatim(userAgent);
        double latitude = p.getLatitude();
        double longitude = p.getLongitude();
        try {
            List<Address> addresses = gc.getFromLocation(latitude, longitude, 1);
            Log.i("ADDRESSES", ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }
}
