package org.prenux.parkin;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MapEventsReceiver {

    private String mUserAgent = "org.prenux.parkin";

    private android.widget.SearchView mSearch;

    private ArrayList<Marker> mMarkerArrayList;
    private MapView mMap;
    private RotationGestureOverlay mRotationGestureOverlay;

    private NominatimPOIProvider mParkingPoiProvider;
    private FolderOverlay mPoiMarkers;
    private Polyline mPolyline;

    LocationManager mLocationManager;

    private final static int M_ZOOM_THRESHOLD = 14;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);

        //GPS Postion things
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Search things
        mSearch = (SearchView) findViewById(R.id.searchbar);

        //Marker references arraylist
        mMarkerArrayList = new ArrayList<>();

        //Initiate Map
        mMap = (MapView) findViewById(R.id.map);
        mMap.setTileSource(TileSourceFactory.MAPNIK);
        mMap.setBuiltInZoomControls(true);
        mMap.setMultiTouchControls(true);
        mMap.setMaxZoomLevel(18);
        mMap.setMinZoomLevel(2);
        mMap.setTilesScaledToDpi(true);

        //Set default view point
        IMapController mapController = mMap.getController();
        mapController.setZoom(18);
        GeoPoint startPoint = new GeoPoint(45.500997, -73.615783);
        mapController.setCenter(startPoint);

        //Enable rotation of the map
        mRotationGestureOverlay = new RotationGestureOverlay(this, mMap);
        mRotationGestureOverlay.setEnabled(true);
        mMap.getOverlays().add(this.mRotationGestureOverlay);

        //Set mMap event listener overlay
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, this);
        mMap.getOverlays().add(0, mapEventsOverlay);

        //Set scroll and zoom event actions to update POI
        mMap.setMapListener(new DelayedMapListener(new MapListener() {
            @Override
            public boolean onZoom(ZoomEvent arg0) {
                if (mMap.getZoomLevel() >= M_ZOOM_THRESHOLD) {
                    new ParkingPOIGettingTask().execute(mMap.getBoundingBox());
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean onScroll(ScrollEvent arg0) {
                if (mMap.getZoomLevel() >= M_ZOOM_THRESHOLD) {
                    new ParkingPOIGettingTask().execute(mMap.getBoundingBox());
                    return true;
                } else {
                    return false;
                }
            }
        }));

        //Points of interests
        mParkingPoiProvider = new NominatimPOIProvider(mUserAgent);
        mPoiMarkers = new FolderOverlay(getApplicationContext());
        mMap.getOverlays().add(mPoiMarkers);

        BoundingBox viewbox = mMap.getBoundingBox();
        new GeocodingTask().execute("J4B 7T9, Qc, Canada", viewbox);
    }

    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
    }

    // ------------------------------ Map events ---------------------------------------
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        Toast.makeText(this, "Tap on (" + p.getLatitude() + "," + p.getLongitude() + ")", Toast.LENGTH_SHORT).show();
        InfoWindow.closeAllInfoWindowsOn(mMap);
        removeAllMarkers();
        removeAllPOIs();
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        Marker pressedMarker = new Marker(mMap);
        pressedMarker.setPosition(p);
        pressedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mMarkerArrayList.add(pressedMarker);
        mMap.getOverlays().add(pressedMarker);
        new ReverseGeocodingTask().execute(pressedMarker);
        return true;
    }

    //------------------------------ Geocoding & Markers-----------------------------------------
    public String getAddressFromGeoPoint(GeoPoint p) {
        GeocoderNominatim geocoder = new GeocoderNominatim(mUserAgent);
        StringBuilder theAddress = new StringBuilder();
        try {
            double dLatitude = p.getLatitude();
            double dLongitude = p.getLongitude();
            List<Address> addresses = geocoder.getFromLocation(dLatitude, dLongitude, 1);
            //Address address = addresses.get(0);
            for (Address address : addresses) {
                int n = address.getMaxAddressLineIndex();
                for (int i = 0; i <= n; i++) {
                    if (i != 0)
                        theAddress.append(", ");
                    theAddress.append(address.getAddressLine(i));
                }
            }
        } catch (IOException e) {
        }
        return theAddress.toString();
    }


    //Async task to reverse-geocode the marker position in a separate thread:
    class ReverseGeocodingTask extends AsyncTask<Object, Void, String> {
        Marker marker;

        protected String doInBackground(Object... params) {
            marker = (Marker) params[0];
            return getAddressFromGeoPoint(marker.getPosition());
        }

        protected void onPostExecute(String result) {
            marker.setTitle(result);
            //marker.showInfoWindow();
            mMap.invalidate();
        }
    }

    //Geocoding Task
    private class GeocodingTask extends AsyncTask<Object, Void, List<Address>> {
        int mIndex;

        protected List<Address> doInBackground(Object... params) {
            String locationAddress = (String) params[0];
            BoundingBox box = (BoundingBox) params[1];
            FixedGeocoderNominatim geocoder = new FixedGeocoderNominatim(mUserAgent);
            geocoder.setOptions(true); //ask for enclosing polygon (if any)
            try {
                List<Address> foundAdresses = geocoder.getFromLocationName(locationAddress, 1,
                        box.getLatSouth(), box.getLonEast(),
                        box.getLatNorth(), box.getLonWest(), false);
                return foundAdresses;
            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(List<Address> foundAdresses) {
            if (foundAdresses == null) {
                Toast.makeText(getApplicationContext(), "Geocoding error", Toast.LENGTH_SHORT).show();
            } else if (foundAdresses.size() == 0) { //if no address found, display an error
                Toast.makeText(getApplicationContext(), "Address not found.", Toast.LENGTH_SHORT).show();
            } else {
                Address address = foundAdresses.get(0); //get first address
                String addressDisplayName = address.getExtras().getString("display_name");
                GeoPoint destinationPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
                //Marker markerDestination = new Marker(mMap);
                mMap.getController().setCenter(destinationPoint);
                //get and display enclosing polygon:
                Bundle extras = address.getExtras();
                if (extras != null && extras.containsKey("polygonpoints")) {
                    ArrayList<GeoPoint> polygon = extras.getParcelableArrayList("polygonpoints");
                    //Log.d("DEBUG", "polygon:"+polygon.size());
                    updateUIWithPolygon(polygon, addressDisplayName);
                } else {
                    updateUIWithPolygon(null, "");
                }
            }
        }
    }

    //add or replace the polygon overlay
    public void updateUIWithPolygon(ArrayList<GeoPoint> polygon, String name) {
        List<Overlay> mapOverlays = mMap.getOverlays();
        int location = -1;
        if (mPolyline != null)
            location = mapOverlays.indexOf(mPolyline);
        mPolyline = new Polyline();
        mPolyline.setColor(0x800000FF);
        mPolyline.setWidth(5.0f);
        mPolyline.setTitle(name);
        BoundingBox bb = null;
        if (polygon != null) {
            mPolyline.setPoints(polygon);
            bb = BoundingBox.fromGeoPoints(polygon);
        }
        if (location != -1)
            mapOverlays.set(location, mPolyline);
        else
            mapOverlays.add(1, mPolyline); //insert just above the MapEventsOverlay.
        setViewOn(bb);
        mMap.invalidate();
    }

    void setViewOn(BoundingBox bb) {
        if (bb != null) {
            mMap.zoomToBoundingBox(bb, true);
        }
    }

    //Remove all user placed markers
    private void removeAllMarkers() {
        for (Marker marker : mMarkerArrayList) {
            marker.remove(mMap);
        }
        mMarkerArrayList.clear();
        mMap.invalidate();
    }

    // ------------------------------------- POI ---------------------------------------------------
    //Async task to get POIs near a geopoint
    private class ParkingPOIGettingTask extends AsyncTask<Object, Void, ArrayList<POI>> {

        protected ArrayList<POI> doInBackground(Object... params) {
            //Points of interests
            BoundingBox box = (BoundingBox) params[0];
            return mParkingPoiProvider.getPOIInside(box, "Parking", 50);
        }

        protected void onPostExecute(ArrayList<POI> pois) {
            removeAllPOIs();
            Drawable poiIcon = getResources().getDrawable(R.drawable.marker_parking);
            try {
                for (POI poi : pois) {
                    Marker poiMarker = new Marker(mMap);
                    poiMarker.setTitle(getString(R.string.offstreet_parking));
                    poiMarker.setSnippet(poi.mDescription);
                    poiMarker.setPosition(poi.mLocation);
                    poiMarker.setIcon(poiIcon);
                    if (poi.mThumbnail != null) {
                        poiMarker.setImage(new BitmapDrawable(poi.mThumbnail));
                    }
                    mPoiMarkers.add(poiMarker);
                }
            } catch (Exception e) {
                Log.d("ParkingPOIGettingTask", e.toString());
                Toast.makeText(getApplicationContext(), "Error in ParkingPOIGettingTask", Toast.LENGTH_LONG);
            }
            mMap.invalidate();
        }
    }

    //Remove all POI markers
    private void removeAllPOIs() {
        try {
            List<Overlay> overlays = mPoiMarkers.getItems();
            for (Overlay item : overlays) {
                mPoiMarkers.remove(item);
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error in removing all POIs", Toast.LENGTH_LONG);
        }
    }

    //Executed when GPS position is requested
    public void getPosition(View view) {
        //Check if location services are enabled
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                !mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Unable to get position");
            builder.setMessage("Do you want to enable location services?");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
            return;
        }

        //if location services are enabled
        LocationListener locationListener = new myLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        new ReverseGeocodingTask().execute(mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
    }

}