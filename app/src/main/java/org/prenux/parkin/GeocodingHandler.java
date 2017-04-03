package org.prenux.parkin;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.List;

class GeocodingHandler {
    LocationManager mLocationManager;
    String mUserAgent;
    MainActivity mMainActivity;
    MapHandler mMapHandler;
    LocationListener mLocationListener;

    GeocodingHandler(LocationManager lm, String ua, MainActivity ma, MapHandler mh) {
        mLocationManager = lm;
        mUserAgent = ua;
        mMainActivity = ma;
        mMapHandler = mh;
        mLocationListener = new myLocationListener();
    }

    String getAddressFromGeoPoint(GeoPoint p) {
        GeocoderNominatim geocoder = new GeocoderNominatim(mUserAgent);
        StringBuilder theAddress = new StringBuilder();
        try {
            double dLatitude = p.getLatitude();
            double dLongitude = p.getLongitude();
            List<Address> addresses = geocoder.getFromLocation(dLatitude, dLongitude, 1);
            //Address address = addresses.get(0);
            for (Address address : addresses) {
                int n = address.getMaxAddressLineIndex();
                for (int i = 0; i <= n; i++) {
                    if (i != 0)
                        theAddress.append(", ");
                    theAddress.append(address.getAddressLine(i));
                }
            }
        } catch (IOException ignored) {
        }
        return theAddress.toString();
    }

    //Executed when GPS position is requested
    void getPosition() {
        //Check if location services are enabled
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                !mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
            builder.setTitle("Unable to get position");
            builder.setMessage("Do you want to enable location services?");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    mMainActivity.startActivity(intent);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
            return;
        }
        Log.d("DEBUG", "Verify if permissons granted");
        //if location services are disabled
        if (ActivityCompat.checkSelfPermission(mMainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mMainActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //TODO : POP ERROR MESSAGE
            Log.d("DEBUG", "Permissions DENIED");
            ActivityCompat.requestPermissions(mMainActivity, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            return;
        }
        Log.d("DEBUG", "Permissions GRANTED");
        Location netLocation = null;
        Location gpsLocation = null;

        //Network Position
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2,2, mLocationListener);
        Log.d("DEBUG", "Network Position");
        if (mLocationManager != null) {
            netLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        //GPS Position
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2, 2, mLocationListener);
        Log.d("DEBUG", "GPS Position");
        if (mLocationManager != null) gpsLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        Marker locationMarker = new Marker(mMapHandler);
        mMapHandler.mMarkerArrayList.add(locationMarker);
        mMapHandler.getOverlays().add(locationMarker);
        if(gpsLocation != null) {
            try {
                locationMarker.setPosition(new GeoPoint(gpsLocation));
                locationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                new ReverseGeocodingTask(this, mMapHandler).execute(locationMarker);
                mMapHandler.getController().setCenter(new GeoPoint(gpsLocation));
            } catch (Exception e) {
                e.printStackTrace();
                mMapHandler.mMarkerArrayList.remove(locationMarker);
            }
        } else if (netLocation != null){
            try {
                locationMarker.setPosition(new GeoPoint(netLocation));
                locationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                new ReverseGeocodingTask(this, mMapHandler).execute(locationMarker);
                mMapHandler.getController().setCenter(new GeoPoint(netLocation));
            } catch (Exception e) {
                e.printStackTrace();
                mMapHandler.mMarkerArrayList.remove(locationMarker);
            }
        mMapHandler.invalidate();
        }

    }


}
