package org.prenux.parkin;

import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteCursor;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
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


public class MainActivity extends AppCompatActivity implements MapEventsReceiver {

    public String mUserAgent = "org.prenux.parkin";
    public MapHandler mMap;
    LocationManager mLocationManager;
    GeocodingHandler mGeoHandler;
    private DrawerLayout mDrawer;
    private boolean mIsDrawerOpen;
    public MainActivity mMainActivity;
    NotificationManager mNotificationManager;
    private SuggestionsDatabase database;
    ParkinDbHelper mDbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);

        //Main Activity reference
        mMainActivity = this;

        //database
         mDbHelper=  new ParkinDbHelper(ctx);
        mDbHelper.importFile("test.csv",mDbHelper.db);
        //Initiate Map in constructor class
        mMap = (MapHandler) findViewById(R.id.map);
        mMap.intializeMap(mMainActivity, mUserAgent);

        Resources res = getResources();
        String[] strDrawerItems = res.getStringArray(R.array.drawer_options);
        ArrayList<MyDrawerItem> drawerItems = new ArrayList<>();
        for(String s : strDrawerItems){
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
                Toast.makeText(getApplicationContext(), "" + Integer.toString(pos), Toast.LENGTH_SHORT).show();

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

        //GPS Postion things
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mGeoHandler = new GeocodingHandler(mLocationManager, mUserAgent, mMainActivity, mMap);

        //Search things
        final SearchView search = (SearchView) findViewById(R.id.searchbar);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                long result = database.insertSuggestion(query);

                if (query.length() != 0) {
                    BoundingBox viewbox = mMap.getBoundingBox();
                    new GeocodingTask(mUserAgent, mMainActivity, mMap).execute(query, viewbox);
                    return result != -1;
                }
                return result != -1;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        database = new SuggestionsDatabase(this);
        search.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                SQLiteCursor cursor = (SQLiteCursor) search.getSuggestionsAdapter().getItem(position);
                int indexColumnSuggestion = cursor.getColumnIndex(SuggestionsDatabase.FIELD_SUGGESTION);

                search.setQuery(cursor.getString(indexColumnSuggestion), false);

                return true;
            }
        });

    }


    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        Configuration.getInstance().load(mMainActivity, PreferenceManager.getDefaultSharedPreferences(mMainActivity));
    }

    // ------------------------------ Map events ---------------------------------------
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        //Toast.makeText(this, "Tap on (" + p.getLatitude() + "," + p.getLongitude() + ")", Toast.LENGTH_SHORT).show();
        //InfoWindow.closeAllInfoWindowsOn(mMap);
        mMap.removeAllMarkers();
        mMap.removeAllPOIs();
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        Marker pressedMarker = new Marker(mMap);
        pressedMarker.setPosition(p);
        pressedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mMap.mMarkerArrayList.add(pressedMarker);
        mMap.getOverlays().add(pressedMarker);
        new ReverseGeocodingTask(mGeoHandler, mMap).execute(pressedMarker);
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
    public void getPosition(View v) {
        Log.d("DEBUG", "getposition called");
        mGeoHandler.getPosition();
    }
}