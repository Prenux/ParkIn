package org.prenux.parkin;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by prenux on 16/03/17.
 */

class myLocationListener implements LocationListener {
    GeocodingHandler gh;

    public myLocationListener(GeocodingHandler gh){
        this.gh = gh;
    }

    @Override
    public void onLocationChanged(Location location) {
        gh.getPosition();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
