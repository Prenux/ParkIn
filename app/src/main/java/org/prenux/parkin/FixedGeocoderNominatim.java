package org.prenux.parkin;

import android.location.Address;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.utils.BonusPackHelper;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by prenux on 14/03/17.
 */

public class FixedGeocoderNominatim extends GeocoderNominatim {


    public FixedGeocoderNominatim(Locale locale, String userAgent) {
        super(locale, userAgent);
    }

    public FixedGeocoderNominatim(String userAgent) {
        super(userAgent);
    }

    public List<Address> getFromLocationName(String locationName, int maxResults,
                                             double lowerLeftLatitude, double lowerLeftLongitude,
                                             double upperRightLatitude, double upperRightLongitude,
                                             boolean bounded)
            throws IOException {
        String url = mServiceUrl + "search?";
        if (mKey != null)
            url += "key=" + mKey + "&";
        url += "format=json"
                + "&accept-language=" + mLocale.getLanguage()
                + "&addressdetails=1"
                + "&limit=" + maxResults
                + "&q=" + URLEncoder.encode(locationName);
        if (lowerLeftLatitude != 0.0 && upperRightLatitude != 0.0){
            //viewbox = left, top, right, bottom:
            url += "&box=" + lowerLeftLongitude
                    + "," + upperRightLatitude
                    + "," + upperRightLongitude
                    + "," + lowerLeftLatitude
                    + "&bounded="+(bounded ? 1 : 0);
        }
        if (mPolygon){
            //get polygon outlines for items found:
            url += "&polygon=1";
            //TODO: polygon param is obsolete. Should be replaced by polygon_geojson.
            //Upgrade is on hold, waiting for MapQuest service to become compatible.
        }
        Log.d(BonusPackHelper.LOG_TAG, "GeocoderNominatim::getFromLocationName:"+url);
        String result = BonusPackHelper.requestStringFromUrl(url, mUserAgent);
        //Log.d(BonusPackHelper.LOG_TAG, result);
        if (result == null)
            throw new IOException();
        try {
            JsonParser parser = new JsonParser();
            JsonElement json = parser.parse(result);
            JsonArray jResults = json.getAsJsonArray();
            List<Address> list = new ArrayList<Address>(jResults.size());
            for (int i=0; i<jResults.size(); i++){
                JsonObject jResult = jResults.get(i).getAsJsonObject();
                Address gAddress = buildAndroidAddress(jResult);
                if (gAddress != null)
                    list.add(gAddress);
            }
            //Log.d(BonusPackHelper.LOG_TAG, "done");
            return list;
        } catch (JsonSyntaxException e) {
            throw new IOException();
        }
    }
}
