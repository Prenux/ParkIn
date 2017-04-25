package org.prenux.parkin;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.sqlite.SQLiteCursor;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.prenux.parkin.database.ParkinDbHelper;

import java.util.ArrayList;
import java.util.HashSet;


public class MainActivity extends AppCompatActivity implements MapEventsReceiver {

    //Default value to Initialize view on Andre-Aisenstadt
    public float INIT_LATITUDE = (float) 45.500997;
    public float INIT_LONGITUDE = (float) -73.615783;

    public SearchHandler mSearch;
    public String mUserAgent = "org.prenux.parkin";
    public MapHandler mMap;
    LocationManager mLocationManager;
    GeocodingHandler mGeoHandler;
    private DrawerLayout mDrawer;
    private boolean mIsDrawerOpen;
    public MainActivity mMainActivity;
    NotificationManager mNotificationManager;

    public ListView mListView;
    Context ctx;
    public ListView mLV;
    private SuggestionsDatabase database;
    ParkinDbHelper mDbHelper;
    public FloatingActionButton mGpsButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ctx = getApplicationContext();
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);

        //Layout
        mGpsButton = (FloatingActionButton) findViewById(R.id.locationFloatingActionButton);
        mGpsButton.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));

        //Main Activity reference
        mMainActivity = this;

        //database
        mDbHelper = new ParkinDbHelper(ctx);

        //Initiate Map in constructor class
        mMap = (MapHandler) findViewById(R.id.map);

        //GPS Postion things
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mGeoHandler = new GeocodingHandler(mLocationManager, mUserAgent, mMainActivity, mMap);
        Log.d("noooooooooooo","map on after Geo");

        //If saved localization exist, use it, else use default values
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        mMap.intializeMap(mMainActivity, mUserAgent,
                sharedPref.getFloat("latitude", INIT_LATITUDE),
                sharedPref.getFloat("longitude", INIT_LONGITUDE));

        Resources res = getResources();
        String[] strDrawerItems = res.getStringArray(R.array.drawer_options);
        ArrayList<MyDrawerItem> drawerItems = new ArrayList<>();
        for (String s : strDrawerItems) {
            drawerItems.add(new MyDrawerItem(s));
        }

        MyDrawerAdapter adapter = new MyDrawerAdapter(this, drawerItems, mMap);

        mIsDrawerOpen = false;
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ListView navList = (ListView) findViewById(R.id.left_drawer);
        navList.setAdapter(adapter);
        navList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int pos, long id) {
                mDbHelper.importFile("places.csv",mDbHelper.db);
                //mDbHelper.getValueById("sefaf","afafafs");
               // mDbHelper.getValueById("aes","se");


                //Update DB item
                if(pos == 1) new ImportFileTask("places.csv", ctx, mDbHelper).execute();
                mDrawer.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                    @Override
                    public void onDrawerClosed(View drawerView) {
                        super.onDrawerClosed(drawerView);
                    }
                });
                mDrawer.closeDrawer(navList);
            }
        });

        //Notifications things
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //Initialize SearchHandler
        mSearch = new SearchHandler((SearchView) findViewById(R.id.searchbar),
                mMainActivity, mMap, mUserAgent, (HashSet<String>) sharedPref.getStringSet("search", new HashSet<String>()));
        mListView = (ListView) findViewById(R.id.searchListView);
        mSearch.init();


        ArrayAdapter<String> lsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mSearch.mSearchHistory);
        mListView.setAdapter(lsAdapter);
        mListView.setVisibility(View.GONE);

        Button recenter = (Button) findViewById(R.id.recenter_button);
        recenter.setVisibility(View.GONE);
    }

    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        Configuration.getInstance().load(mMainActivity, PreferenceManager.getDefaultSharedPreferences(mMainActivity));
    }

    @Override
    protected void onPause() {
        super.onPause();
        GeoPoint mSavedPosition = (GeoPoint) mMap.getMapCenter(); //save map center on app close/pause
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat("latitude", (float) mSavedPosition.getLatitude());
        editor.putFloat("longitude", (float) mSavedPosition.getLongitude());
        editor.putStringSet("search", mSearch.getSearchHistory()); //save search history on app close/pause
        editor.commit();
    }

    // ------------------------------ Map events ---------------------------------------
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        InfoWindow.closeAllInfoWindowsOn(mMap);
        mMap.removeAllMarkers();
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        //Put Marker on the map
        Marker pressedMarker = new Marker(mMap);
        pressedMarker.setPosition(p);
        pressedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mMap.mMarkerArrayList.add(pressedMarker);
        mMap.getOverlays().add(pressedMarker);
        new ReverseGeocodingTask(mGeoHandler, mMap).execute(pressedMarker);
        pressedMarker.showInfoWindow();
        if(pressedMarker.isInfoWindowShown()){
            Log.d("DEBUG","info windown is shown");
        }
        return true;
    }


    public void toggleDrawer(View v) {
        if (mIsDrawerOpen) {
            mDrawer.closeDrawer(Gravity.LEFT);
        } else {
            mDrawer.openDrawer(Gravity.LEFT);
        }
        mIsDrawerOpen = !mIsDrawerOpen;
    }

    //Called from location button in layout with onClick attribute
    public void gpsButtonClicked(View v) {
        mGeoHandler.mIsGPS = !mGeoHandler.mIsGPS;
        if(!mGeoHandler.mIsGPS){
            hideRecenterButton();
            mMap.removeLocationMarker();
        }
        Log.d("DEBUG", "gpsButtonClicked called");
        mGeoHandler.getPosition();
    }

    public void showRecenterButton(){
        Log.d("noooooooooooo","map on show recenter");
        final Button recenter = (Button) findViewById(R.id.recenter_button);
        recenter.setVisibility(View.VISIBLE);
        recenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideRecenterButton();
            }
        });
    }

    public void hideRecenterButton(){
        final Button recenter = (Button) findViewById(R.id.recenter_button);
        mGeoHandler.isFollowing = true;
        recenter.setVisibility(View.GONE);
        mGeoHandler.getPosition();
    }



}