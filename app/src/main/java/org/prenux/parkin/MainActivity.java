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
        map.setMaxZoomLevel(19);

        //Set default view point
        IMapController mapController = map.getController();
        mapController.setZoom(18);
        GeoPoint startPoint = new GeoPoint(45.500997, -73.615783);
        mapController.setCenter(startPoint);

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
        new ReverseGeocodingTask().execute(pressedMarker);
        //pressedMarker.setTitle( + "\n" +p.getLatitude()+","+p.getLongitude());
        map.invalidate();
        return true;
    }

    private void removeAllMarkers() {
        for (Marker marker: markerArrayList) {
            marker.remove(map);
        }
        markerArrayList.clear();
    }

    //http://nominatim.openstreetmap.org/reverse?format=json&accept-language=en&lat=45.49967331062899&lon=-73.6155492067337

    public String getAddress(GeoPoint p){
        GeocoderNominatim geocoder = new GeocoderNominatim(userAgent);
        String theAddress;
        try {
            double dLatitude = p.getLatitude();
            double dLongitude = p.getLongitude();
            List<Address> addresses = geocoder.getFromLocation(dLatitude, dLongitude, 1);
            StringBuilder sb = new StringBuilder();
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                int n = address.getMaxAddressLineIndex();
                for (int i=0; i<=n; i++) {
                    if (i!=0)
                        sb.append(", ");
                    sb.append(address.getAddressLine(i));
                }
                theAddress = sb.toString();
            } else {
                theAddress = null;
            }
        } catch (IOException e) {
            theAddress = null;
        }
        if (theAddress != null) {
            return theAddress;
        } else {
            return "";
        }
    }


    //Async task to reverse-geocode the marker position in a separate thread:
    private class ReverseGeocodingTask extends AsyncTask<Object, Void, String> {
        Marker marker;
        protected String doInBackground(Object... params) {
            marker = (Marker)params[0];
            return getAddress(marker.getPosition());
        }
        protected void onPostExecute(String result) {
            marker.setSnippet(result);
            marker.showInfoWindow();
        }
    }
}
