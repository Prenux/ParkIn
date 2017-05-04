package org.prenux.parkin;

import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.database.sqlite.SQLiteCursor;
import android.graphics.Color;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import org.osmdroid.util.BoundingBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sugar on 4/8/17.
 */

public class SearchHandler {

    ListView mListView;
    SearchView mSearchView;
    ArrayAdapter<String> adapter;

    MainActivity mMainActivity;
    MapHandler mMap;
    String mUserAgent;
    public ArrayList<String> mSearchHistory;
    public HashSet<String> mHashSetHistory;
    private SuggestionsDatabase database;


    public SearchHandler(ListView lv, SearchView sv, MainActivity ma, MapHandler map, String ua, HashSet<String> set) {
        mListView = lv;
        mSearchView = sv;
        mMainActivity = ma;
        mMap = map;
        mUserAgent = ua;
        mHashSetHistory = set;

        loadHistory(set);

  }

    public void init() {
        adapter = new ArrayAdapter<String>(mMainActivity,android.R.layout.simple_list_item_1,mSearchHistory);

        mListView.setVisibility(View.GONE);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                String address = (String) adapter.getItemAtPosition(position);
                mSearchView.setQuery(address,true);
                mListView.setVisibility(View.GONE);
            }
        });




        mSearchView.setOnQueryTextFocusChangeListener( new View.OnFocusChangeListener(){
            public void onFocusChange(View v, boolean hasFocus) {
                if( v != null && hasFocus) {
                    mListView.setVisibility(View.VISIBLE);
                }
                else{
                    mListView.setVisibility(View.GONE);
                }
            }
        }
        );

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mListView.setVisibility(View.GONE);
                mListView.clearFocus();
                mSearchView.clearFocus();

                long result = database.insertSuggestion(query);

                addToHistory(query);

                if (query.length() != 0) {
                    BoundingBox viewbox = mMap.getBoundingBox();
                    new GeocodingTask(mUserAgent, mMainActivity, mMap).execute(query, viewbox);
                }
                return result != -1;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
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
        return mHashSetHistory;
    }

    public void loadHistory(HashSet<String> history){
        mSearchHistory = new ArrayList<String>(mHashSetHistory);
    }

    public void addToHistory( String newText){
        if( !(mHashSetHistory.contains(newText.toLowerCase())) ){
                mSearchHistory.add(newText.toLowerCase());
                mHashSetHistory.add(newText.toLowerCase());
                adapter.add(newText.toLowerCase());
                adapter.notifyDataSetChanged();
        }
    }

}
