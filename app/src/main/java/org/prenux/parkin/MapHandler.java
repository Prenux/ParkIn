package org.prenux.parkin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.events.DelayedMapListener;
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

import java.util.ArrayList;
import java.util.List;

class MapHandler extends MapView {
    //Initializing fields
    double mLatitude, mLongitude;
    RotationGestureOverlay mRotationGestureOverlay;
    final static int M_ZOOM_THRESHOLD = 14;
    MainActivity mMainActivity;
    NominatimPOIProvider mParkingPoiProvider;
    RadiusMarkerClusterer mPoiMarkers;
    RadiusMarkerClusterer mFreeParkinMarkers;
    String mUserAgent;
    MapHandler mMapHandler;
    Polyline mPolyline;
    ArrayList<Marker> mMarkerArrayList;
    public boolean mOffStreet;
    public boolean mStreetReg;
    GeocodingHandler mGeoHandler;
    boolean mMachineScroll;

    MapHandler(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    void intializeMap(final MainActivity ma, String ua, float latitude, float longitude) {
        //Set context and user agent
        mMainActivity = ma;
        mUserAgent = ua;
        mLatitude = (double) latitude;
        mLongitude = (double) longitude;

        mMachineScroll = false;
        mGeoHandler = mMainActivity.mGeoHandler;

        //Initialize map
        this.setTileSource(TileSourceFactory.MAPNIK);
        this.setBuiltInZoomControls(true);
        this.setMultiTouchControls(true);
        this.setMaxZoomLevel(18);
        this.setMinZoomLevel(2);
        this.setTilesScaledToDpi(true);
        mOffStreet = true;
        mStreetReg = true;

        //Set default view point
        mMachineScroll = true;
        IMapController mapController = this.getController();
        mapController.setZoom(18);
        GeoPoint startPoint = new GeoPoint(mLatitude, mLongitude);
        mapController.setCenter(startPoint);
        mMachineScroll = false;

        //Enable rotation of the map
        mRotationGestureOverlay = new RotationGestureOverlay(this.mMainActivity, this);
        mRotationGestureOverlay.setEnabled(true);
        this.getOverlays().add(this.mRotationGestureOverlay);

        //Set event listener overlay
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mMainActivity, mMainActivity);
        this.getOverlays().add(0, mapEventsOverlay);

        //Points of interests (Offstreet parking)
        mParkingPoiProvider = new NominatimPOIProvider(mUserAgent);
        mPoiMarkers = new RadiusMarkerClusterer(getContext());
        Drawable clusterIconD = getResources().getDrawable(R.drawable.marker_parking);
        Bitmap clusterIcon = ((BitmapDrawable)clusterIconD).getBitmap();
        mPoiMarkers.setIcon(clusterIcon);
        mPoiMarkers.mAnchorV = Marker.ANCHOR_BOTTOM;
        mPoiMarkers.mTextAnchorU = 0.70f;
        mPoiMarkers.mTextAnchorV = 0.20f;
        mPoiMarkers.getTextPaint().setTextSize(12 * getResources().getDisplayMetrics().density);
        mPoiMarkers.getTextPaint().setColor(Color.RED);
        this.getOverlays().add(mPoiMarkers);

        //Free street Parkin
        mFreeParkinMarkers = new RadiusMarkerClusterer(getContext());
        Drawable freeClusterIconD = getResources().getDrawable(R.drawable.marker_parking_green);
        Bitmap freeClusterIcon = ((BitmapDrawable)freeClusterIconD).getBitmap();
        mFreeParkinMarkers.setIcon(freeClusterIcon);
        mFreeParkinMarkers.mAnchorV = Marker.ANCHOR_BOTTOM;
        mFreeParkinMarkers.mTextAnchorU = 0.70f;
        mFreeParkinMarkers.mTextAnchorV = 0.20f;
        mFreeParkinMarkers.getTextPaint().setTextSize(12 * getResources().getDisplayMetrics().density);
        mFreeParkinMarkers.getTextPaint().setColor(Color.RED);
        this.getOverlays().add(mFreeParkinMarkers);

        //Set mapHandler obj to be passed in AsyncTask
        mMapHandler = this;

        //Marker references arraylist
        mMarkerArrayList = new ArrayList<>();

        //Delayed Map Listener for less sensitive events
        final DelayedMapListener delayedMapListener = new DelayedMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                if (getZoomLevel() >= M_ZOOM_THRESHOLD) {
                    if(mOffStreet){
                        new ParkingPOIGettingTask(mParkingPoiProvider, mPoiMarkers, mMainActivity, mMapHandler).
                                execute(mMapHandler.getBoundingBox());
                    }
                    if(mStreetReg){
                        new ParkingStreetRegGettingTask(mMainActivity.mDbHelper, mFreeParkinMarkers, mMainActivity, mMapHandler).
                                execute(mMapHandler.getBoundingBox());
                    }
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                if (getZoomLevel() >= M_ZOOM_THRESHOLD) {
                    if(mOffStreet){
                        new ParkingPOIGettingTask(mParkingPoiProvider, mPoiMarkers, mMainActivity, mMapHandler).
                                execute(mMapHandler.getBoundingBox());
                    }
                    if(mStreetReg){
                        new ParkingStreetRegGettingTask(mMainActivity.mDbHelper, mFreeParkinMarkers, mMainActivity, mMapHandler).
                                execute(mMapHandler.getBoundingBox());
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });

        //Set scroll and zoom event actions to update POI
        this.setMapListener(new MapListener() {
            @Override
            public boolean onZoom(ZoomEvent arg0) {
                if (mGeoHandler.mIsGPS && !mMachineScroll && mGeoHandler.isFollowing) {
                    Log.d("noooooooooooo", "map on zoom");
                    mGeoHandler.isFollowing = false;
                    mMainActivity.showRecenterButton();
                }
                delayedMapListener.onZoom(arg0);
                return true;
            }

            @Override
            public boolean onScroll(ScrollEvent arg0) {
                if (mGeoHandler.mIsGPS && !mMachineScroll && mGeoHandler.isFollowing) {
                    Log.d("noooooooooooo", "map on scroll  " + Boolean.toString(mMachineScroll));
                    mGeoHandler.isFollowing = false;
                    mMainActivity.showRecenterButton();
                }
                delayedMapListener.onScroll(arg0);
                return true;
            }
        });
    }

    //Remove all POI markers
    void removeAllPOIs() {
        try {
            ArrayList<Marker> overlays = mPoiMarkers.getItems();
            int size = overlays.size();
            for (int i = 0; i < size; i++) {
                overlays.remove(0);
            }
        } catch (Exception e) {
            Log.d("MapHandlerDebug", e.toString());

            Toast.makeText(mMainActivity, "Error in removing all POIs", Toast.LENGTH_LONG).show();
        }
    }

    void removeAllStreetReg() {
        try {
            ArrayList<Marker> markers = mFreeParkinMarkers.getItems();
            int size = markers.size();
            for (int i = 0; i < size; i++) {
                markers.remove(0);
            }
        } catch (Exception e) {
            Log.d("MapHandlerDebug", e.toString());

            Toast.makeText(mMainActivity, "Error in removing all Street Reg.", Toast.LENGTH_LONG).show();
        }
    }

    //remove the location marker
    void removeLocationMarker(){
        mGeoHandler.mLocationMarker.remove(this);
        mGeoHandler.mLocationMarker = null;
        this.invalidate();
    }

    //Remove all user placed markers
    void removeAllMarkers() {
        for (Marker marker : mMarkerArrayList) {
            marker.remove(this);
        }
        mMarkerArrayList.clear();
        this.invalidate();
    }

    void setViewOn(BoundingBox bb) {
        if (bb != null) {
            this.zoomToBoundingBox(bb, true);
        }
    }

    //add or replace the polygon overlay
    void updateUIWithPolygon(ArrayList<GeoPoint> polygon, String name) {
        List<Overlay> mapOverlays = this.getOverlays();
        int location = -1;
        if (mPolyline != null)
            location = mapOverlays.indexOf(mPolyline);
        mPolyline = new Polyline();
        mPolyline.setColor(0x800000FF);
        mPolyline.setWidth(10.0f);
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
        this.invalidate();
    }
}
