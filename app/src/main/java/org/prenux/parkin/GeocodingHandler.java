package org.prenux.parkin;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.List;

/**
 * Created by sugar on 3/18/17.
 */

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

        //if location services are enabled
        if (ActivityCompat.checkSelfPermission(mMainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mMainActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10, mLocationListener);
        new ReverseGeocodingTask(this, mMapHandler).execute(mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
    }


}
