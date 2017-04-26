package org.prenux.parkin;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.prenux.parkin.database.ParkinDbHelper;

import java.util.ArrayList;

/**
 * Created by prenux on 25/04/17.
 */

public class ParkingStreetRegGettingTask extends AsyncTask<Object, Void, ArrayList<GeoPoint>> {
    RadiusMarkerClusterer mfreeParkinMarkers;
    Context mMainActivity;
    MapHandler mMapHandler;
    ParkinDbHelper mDb;

    ParkingStreetRegGettingTask(ParkinDbHelper db, RadiusMarkerClusterer rmc, MainActivity ma, MapHandler mapHandler) {
        mDb = db;
        mfreeParkinMarkers = rmc;
        mMainActivity = ma;
        mMapHandler = mapHandler;
    }

    protected ArrayList<GeoPoint> doInBackground(Object... params) {
        return mDb.getFreeParkings((BoundingBox) params[0]);
    }

    protected void onPostExecute(ArrayList<GeoPoint> geopoints) {
        mMapHandler.removeAllStreetReg();
        Drawable freeParkinIcon = mMainActivity.getResources().getDrawable(R.drawable.marker_parking_green); //green markers for free parkings
        try {
            for (GeoPoint gp : geopoints) {
                Marker streetRegMarker = new Marker(mMapHandler);
                streetRegMarker.setTitle(mMainActivity.getString(R.string.free_parkings));
                streetRegMarker.setPosition(gp);
                streetRegMarker.setIcon(freeParkinIcon);
                mfreeParkinMarkers.add(streetRegMarker);
                streetRegMarker.setInfoWindow(new MarkerInfoWindow(R.layout.bubble_marker, mMapHandler));
                streetRegMarker.showInfoWindow();
            }
        } catch (Exception e) {
            Log.d("FreeParkingAsync", e.toString());
        }
        mMapHandler.invalidate();
    }
}
