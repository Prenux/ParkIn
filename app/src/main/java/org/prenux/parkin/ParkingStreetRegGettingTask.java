package org.prenux.parkin;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.prenux.parkin.database.ParkinDbHelper;

import java.util.ArrayList;

/**
 * Created by prenux on 25/04/17.
 */

public class ParkingStreetRegGettingTask extends AsyncTask<Object, Void, ArrayList<GeoPoint>> {
    FolderOverlay mPoiMarkers;
    Context mMainActivity;
    MapHandler mMapHandler;
    ParkinDbHelper mDb;

    ParkingStreetRegGettingTask(ParkinDbHelper db, FolderOverlay fo, MainActivity ma, MapHandler mapHandler) {
        mDb = db;
        mPoiMarkers = fo;
        mMainActivity = ma;
        mMapHandler = mapHandler;
    }

    protected ArrayList<GeoPoint> doInBackground(Object... params) {
        return mDb.getFreeParkings((BoundingBox) params[0]);
    }

    protected void onPostExecute(ArrayList<GeoPoint> geopoints) {
        mMapHandler.removeAllPOIs();
        Drawable poiIcon = mMainActivity.getResources().getDrawable(R.drawable.marker_parking_green); //green markers for free parkings
        try {
            for (GeoPoint gp : geopoints) {
                Marker poiMarker = new Marker(mMapHandler);
                poiMarker.setTitle(mMainActivity.getString(R.string.free_parkings));
                poiMarker.setPosition(gp);
                poiMarker.setIcon(poiIcon);
                mPoiMarkers.add(poiMarker);
            }
        } catch (Exception e) {
            Log.d("FreeParkingAsync", e.toString());
        }
        mMapHandler.invalidate();
    }
}
