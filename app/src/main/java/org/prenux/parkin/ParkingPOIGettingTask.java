package org.prenux.parkin;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sugar on 3/18/17.
 */

public class ParkingPOIGettingTask extends AsyncTask<Object, Void, ArrayList<POI>> {

    public NominatimPOIProvider mParkingPoiProvider;
    public FolderOverlay mPoiMarkers;
    public Context mContext;
    public MapHandler mMapHandler;

    public void setPOIattributes(NominatimPOIProvider npp, FolderOverlay fo, Context ctx, MapHandler mapHandler) {
        this.mParkingPoiProvider = npp;
        this.mPoiMarkers = fo;
        this.mContext = ctx;
        this.mMapHandler = mapHandler;
    }

    protected ArrayList<POI> doInBackground(Object... params) {
        //Points of interests
        BoundingBox box = (BoundingBox) params[0];
        return mParkingPoiProvider.getPOIInside(box, "Parking", 50);
    }

    protected void onPostExecute(ArrayList<POI> pois) {
        mMapHandler.removeAllPOIs();
        Drawable poiIcon = mContext.getResources().getDrawable(R.drawable.marker_parking);
        try {
            for (POI poi : pois) {
                Marker poiMarker = new Marker(mMapHandler);
                poiMarker.setTitle(mContext.getString(R.string.offstreet_parking));
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
            Toast.makeText(mContext, "Error in ParkingPOIGettingTask", Toast.LENGTH_LONG).show();
        }
        mMapHandler.invalidate();
    }
}
