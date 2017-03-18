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
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.List;

/**
 * Created by sugar on 3/18/17.
 */

public class MapHandler extends MapView {
    //Initializing fields
    public RotationGestureOverlay mRotationGestureOverlay;
    public final static int M_ZOOM_THRESHOLD = 14;
    public Context mContext;
    public NominatimPOIProvider mParkingPoiProvider;
    public FolderOverlay mPoiMarkers;
    public String mUserAgent;
    public MapHandler mMapHandler;

    //TODO: find version of controller used
    public MapHandler(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public MapHandler(final Context context) {
        super(context);
    }


    public MapHandler(final Context context,
                      final MapTileProviderBase aTileProvider) {
        super(context, aTileProvider);
    }

    public MapHandler(final Context context,
                      final MapTileProviderBase aTileProvider,
                      final Handler tileRequestCompleteHandler) {
        super(context, aTileProvider, tileRequestCompleteHandler);
    }

    public MapHandler(final Context context, MapTileProviderBase tileProvider,
                      final Handler tileRequestCompleteHandler, final AttributeSet attrs) {
        super(context, tileProvider, tileRequestCompleteHandler, attrs);

    }

    void intializeMap(final Context ctx, String ua) {
        //Set context and user agent
        mContext = ctx;
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
        mRotationGestureOverlay = new RotationGestureOverlay(this.mContext, this);
        mRotationGestureOverlay.setEnabled(true);
        this.getOverlays().add(this.mRotationGestureOverlay);

        //Set event listener overlay
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mContext, (MapEventsReceiver) mContext);
        this.getOverlays().add(0, mapEventsOverlay);

        //Points of interests
        mParkingPoiProvider = new NominatimPOIProvider(mUserAgent);
        mPoiMarkers = new FolderOverlay(this.mContext);
        this.getOverlays().add(mPoiMarkers);

        //Set mapHandler obj to be passed in AsyncTask
        mMapHandler = this;

        //Set scroll and zoom event actions to update POI
        this.setMapListener(new DelayedMapListener(new MapListener() {
            @Override
            public boolean onZoom(ZoomEvent arg0) {
                if (getZoomLevel() >= M_ZOOM_THRESHOLD) {
                    ParkingPOIGettingTask ppgt = new ParkingPOIGettingTask();
                    ppgt.setPOIattributes(mParkingPoiProvider, mPoiMarkers, MapHandler.this.mContext, mMapHandler);
                    ppgt.execute(getBoundingBox());
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean onScroll(ScrollEvent arg0) {
                if (getZoomLevel() >= M_ZOOM_THRESHOLD) {
                    new ParkingPOIGettingTask().execute(getBoundingBox());
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
            Toast.makeText(mContext, "Error in removing all POIs", Toast.LENGTH_LONG).show();
        }
    }
}
