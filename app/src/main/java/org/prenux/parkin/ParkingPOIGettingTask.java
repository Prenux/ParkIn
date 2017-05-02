package org.prenux.parkin;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

/**
 * Created by sugar on 3/18/17.
 */

class ParkingPOIGettingTask extends AsyncTask<Object, Void, ArrayList<POI>> {

    NominatimPOIProvider mParkingPoiProvider;
    RadiusMarkerClusterer mPoiMarkers;
    Context mMainActivity;
    MapHandler mMapHandler;

    ParkingPOIGettingTask(NominatimPOIProvider npp, RadiusMarkerClusterer rcm, MainActivity ma, MapHandler mapHandler) {
        mParkingPoiProvider = npp;
        mPoiMarkers = rcm;
        mMainActivity = ma;
        mMapHandler = mapHandler;
    }

    protected ArrayList<POI> doInBackground(Object... params) {
        //Points of interests
        BoundingBox box = (BoundingBox) params[0];
        return mParkingPoiProvider.getPOIInside(box, "Parking", 50);
    }

    protected void onPostExecute(ArrayList<POI> pois) {
        mMapHandler.removeAllPOIs();
        Drawable poiIcon = mMainActivity.getResources().getDrawable(R.drawable.marker_parking);
        try {
            for (POI poi : pois) {
                Marker poiMarker = new Marker(mMapHandler);
                poiMarker.setTitle(mMainActivity.getString(R.string.offstreet_parking));
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
        }
        Log.d("ParkingPOIGettingTask", "Refreshing Map");
        mMapHandler.invalidate();
    }
}
