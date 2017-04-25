package org.prenux.parkin;

import android.content.SearchRecentSuggestionsProvider;
import android.database.sqlite.SQLiteCursor;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import org.osmdroid.util.BoundingBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sugar on 4/8/17.
 */

public class SearchHandler extends SearchRecentSuggestionsProvider {

    int MAX_HISTORY_RESULTS = 10;
    int mIndex = 0;

    SearchView mSearchView;
    MainActivity mMainActivity;
    MapHandler mMap;
    String mUserAgent;
    public String[] mSearchHistory = new String[MAX_HISTORY_RESULTS];
    private SuggestionsDatabase database;


    public SearchHandler(SearchView sv, MainActivity ma, MapHandler map, String ua, HashSet<String> set) {
        mSearchView = sv;
        mMainActivity = ma;
        mMap = map;
        mUserAgent = ua;

        //Retrieve saved searches
        int i = 0;
        for (String s : set) {
            if (s != null && s.length() > 0)
                mSearchHistory[i++ % MAX_HISTORY_RESULTS] = s;
        }
    }

    public void init() {

        mSearchView.setOnSearchClickListener( new SearchView.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchView.setFocusable(true);
                mSearchView.requestFocusFromTouch();
            }

        });


        /*mSearchView.onFocusChangeListener(

        )
        */
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                long result = database.insertSuggestion(query);
                mSearchHistory[mIndex++ % MAX_HISTORY_RESULTS] = query; // add new queries in search history

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

        database = new SuggestionsDatabase(mMainActivity);
        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                SQLiteCursor cursor = (SQLiteCursor) mSearchView.getSuggestionsAdapter().getItem(position);
                int indexColumnSuggestion = cursor.getColumnIndex(SuggestionsDatabase.FIELD_SUGGESTION);

                mSearchView.setQuery(cursor.getString(indexColumnSuggestion), false);

                return true;
            }
        });

    }

    public HashSet<String> getSearchHistory() {
        HashSet<String> set = new HashSet<>();
        for (String s : mSearchHistory) {
            if (s != null && s.length() > 0)
                set.add(s);
        }
        return set;
    }
}
