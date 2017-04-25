package org.prenux.parkin;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.prenux.parkin.database.ParkinDbHelper;

import java.util.ArrayList;

/**
 * Created by prenux on 25/04/17.
 */

public class ParkingStreetRegGettingTask extends AsyncTask<Object, Void, Cursor> {
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

    protected ArrayList<POI> doInBackground(Object... params) {
        return mDb.getFreeParkings((BoundingBox) params[0]);
    }

    protected void onPostExecute(ArrayList<POI> pois) {
        mMapHandler.removeAllPOIs();
        Drawable poiIcon = mMainActivity.getResources().getDrawable(R.drawable.marker_parking_green); //green markers for free parkings
        try {
            for (POI poi : pois) {
                Marker poiMarker = new Marker(mMapHandler);
                poiMarker.setTitle(mMainActivity.getString(R.string.free_parkings));
                poiMarker.setSnippet(poi.mDescription);
                poiMarker.setPosition(poi.mLocation);
                poiMarker.setIcon(poiIcon);
                if (poi.mThumbnail != null) {
                    poiMarker.setImage(new BitmapDrawable(poi.mThumbnail));
                }
                mPoiMarkers.add(poiMarker);
            }
        } catch (Exception e) {
            Log.d("ParkingStreetRegGettingTask", e.toString());
        }
        mMapHandler.invalidate();
    }
}
