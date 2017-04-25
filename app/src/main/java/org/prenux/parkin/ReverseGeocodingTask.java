package org.prenux.parkin;

import android.os.AsyncTask;

import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

//Async task to reverse-geocode the marker position in a separate thread:
class ReverseGeocodingTask extends AsyncTask<Object, Void, String> {
    Marker marker;
    GeocodingHandler mGeoHandler;
    MapHandler mMapHandler;

    ReverseGeocodingTask(GeocodingHandler gh, MapHandler mh) {
        mGeoHandler = gh;
        mMapHandler = mh;
    }

    protected String doInBackground(Object... params) {
        marker = (Marker) params[0];
        return mGeoHandler.getAddressFromGeoPoint(marker.getPosition());
    }

    protected void onPostExecute(String result) {
        marker.setSnippet(result);
        mMapHandler.invalidate();
    }
}
