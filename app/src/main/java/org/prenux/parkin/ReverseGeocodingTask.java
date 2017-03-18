package org.prenux.parkin;

import android.os.AsyncTask;

import org.osmdroid.views.overlay.Marker;

/**
 * Created by sugar on 3/18/17.
 */

//Async task to reverse-geocode the marker position in a separate thread:
class ReverseGeocodingTask extends AsyncTask<Object, Void, String> {
    Marker marker;
    public GeocodingHandler mGeoHandler;
    public MapHandler mMapHandler;

    public ReverseGeocodingTask(GeocodingHandler gh, MapHandler mh) {
        mGeoHandler = gh;
        mMapHandler = mh;
    }

    protected String doInBackground(Object... params) {
        marker = (Marker) params[0];
        return mGeoHandler.getAddressFromGeoPoint(marker.getPosition());
    }

    protected void onPostExecute(String result) {
        marker.setTitle(result);
        //marker.showInfoWindow();
        mMapHandler.invalidate();
    }
}
