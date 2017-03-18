package org.prenux.parkin;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sugar on 3/18/17.
 */

public class MapHandler extends MapView {
    //Initializing fields
    public RotationGestureOverlay mRotationGestureOverlay;
    public final static int M_ZOOM_THRESHOLD = 14;
    public MainActivity mMainActivity;
    public NominatimPOIProvider mParkingPoiProvider;
    public FolderOverlay mPoiMarkers;
    public String mUserAgent;
    public MapHandler mMapHandler;
    private Polyline mPolyline;


    //TODO: find version of controller used
    public MapHandler(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    void intializeMap(final MainActivity ma, String ua) {
        //Set context and user agent
        mMainActivity = ma;
        mUserAgent = ua;

        //Initialize map
        this.setTileSource(TileSourceFactory.MAPNIK);
        this.setBuiltInZoomControls(true);
        this.setMultiTouchControls(true);
        this.setMaxZoomLevel(18);
        this.setMinZoomLevel(2);
        this.setTilesScaledToDpi(true);

        //Set default view point
        IMapController mapController = this.getController();
        mapController.setZoom(18);
        GeoPoint startPoint = new GeoPoint(45.500997, -73.615783);
        mapController.setCenter(startPoint);

        //Enable rotation of the map
        mRotationGestureOverlay = new RotationGestureOverlay(this.mMainActivity, this);
        mRotationGestureOverlay.setEnabled(true);
        this.getOverlays().add(this.mRotationGestureOverlay);

        //Set event listener overlay
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mMainActivity, mMainActivity);
        this.getOverlays().add(0, mapEventsOverlay);

        //Points of interests
        mParkingPoiProvider = new NominatimPOIProvider(mUserAgent);
        mPoiMarkers = new FolderOverlay(this.mMainActivity);
        this.getOverlays().add(mPoiMarkers);

        //Set mapHandler obj to be passed in AsyncTask
        mMapHandler = this;

        //Set scroll and zoom event actions to update POI
        this.setMapListener(new DelayedMapListener(new MapListener() {
            @Override
            public boolean onZoom(ZoomEvent arg0) {
                if (getZoomLevel() >= M_ZOOM_THRESHOLD) {
                    new ParkingPOIGettingTask(mParkingPoiProvider, mPoiMarkers, mMainActivity, mMapHandler).
                            execute(mMapHandler.getBoundingBox());
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean onScroll(ScrollEvent arg0) {
                if (getZoomLevel() >= M_ZOOM_THRESHOLD) {
                    new ParkingPOIGettingTask(mParkingPoiProvider, mPoiMarkers, mMainActivity, mMapHandler).
                            execute(mMapHandler.getBoundingBox());
                    return true;
                } else {
                    return false;
                }
            }
        }));
    }

    //Remove all POI markers
    public void removeAllPOIs() {
        try {
            List<Overlay> overlays = mPoiMarkers.getItems();
            for (Overlay item : overlays) {
                mPoiMarkers.remove(item);
            }
        } catch (Exception e) {
            Toast.makeText(mMainActivity, "Error in removing all POIs", Toast.LENGTH_LONG).show();
        }
    }

    void setViewOn(BoundingBox bb) {
        if (bb != null) {
            this.zoomToBoundingBox(bb, true);
        }
    }

    //add or replace the polygon overlay
    public void updateUIWithPolygon(ArrayList<GeoPoint> polygon, String name) {
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