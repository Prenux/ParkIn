package org.prenux.parkin;

import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sugar on 3/18/17.
 */

//Geocoding Task
class GeocodingTask extends AsyncTask<Object, Void, List<Address>> {
    int mIndex;
    String mUserAgent;
    MainActivity mMainActivity;
    MapHandler mMapHandler;

    GeocodingTask(String ua, MainActivity ma, MapHandler mh) {
        mUserAgent = ua;
        mMainActivity = ma;
        mMapHandler = mh;
    }

    protected List<Address> doInBackground(Object... params) {
        String locationAddress = (String) params[0];
        BoundingBox box = (BoundingBox) params[1];
        FixedGeocoderNominatim geocoder = new FixedGeocoderNominatim(mUserAgent);
        geocoder.setOptions(true); //ask for enclosing polygon (if any)
        try {
            return geocoder.getFromLocationName(locationAddress, 1,
                    box.getLatSouth(), box.getLonEast(),
                    box.getLatNorth(), box.getLonWest(), false);
        } catch (Exception e) {
            return null;
        }
    }

    protected void onPostExecute(List<Address> foundAdresses) {
        if (foundAdresses == null) {
            Toast.makeText(mMainActivity, "Geocoding error", Toast.LENGTH_SHORT).show();
        } else if (foundAdresses.size() == 0) { //if no address found, display an error
            Toast.makeText(mMainActivity, "Address not found.", Toast.LENGTH_SHORT).show();
        } else {
            Address address = foundAdresses.get(0); //get first address
            //Log.i("DEBUG",Integer.toString(foundAdresses.size()));
            String addressDisplayName = address.getExtras().getString("display_name");
            GeoPoint destinationPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
            //Marker markerDestination = new Marker(mMap);
            mMapHandler.getController().setCenter(destinationPoint);
            //get and display enclosing polygon:
            Bundle extras = address.getExtras();
            if (extras != null && extras.containsKey("polygonpoints")) {
                ArrayList<GeoPoint> polygon = extras.getParcelableArrayList("polygonpoints");
                //Log.i("DEBUG", "polygon:"+polygon.size());
                mMapHandler.updateUIWithPolygon(polygon, addressDisplayName);
            } else {
                mMapHandler.updateUIWithPolygon(null, "");
            }
        }
    }
}
