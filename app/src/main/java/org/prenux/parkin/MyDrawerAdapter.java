package org.prenux.parkin;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by prenux on 03/04/17.
 */

public class MyDrawerAdapter extends BaseAdapter {

    private final Context context;
    private ArrayList<MyDrawerItem> data = null;
    MapHandler mMap;

    public MyDrawerAdapter(Context context, ArrayList<MyDrawerItem> data, MapHandler map)
    {
        super();
        this.mMap = map;
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View v;
        MyDrawerItem choice = data.get(position);
        if(position == 0) {
            v = inflater.inflate(R.layout.layout_toggle, parent, false);
            CheckBox chkBox = (CheckBox) v.findViewById(R.id.chkbox);
            chkBox.setText(choice.name);
            chkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox chkbox = (CheckBox) v;
                            mMap.mOffStreet = chkbox.isChecked();
                    if(!chkbox.isChecked()){
                        mMap.removeAllPOIs();
                    }
                }
            });
        } else {
            v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            TextView textView = (TextView) v.findViewById(android.R.id.text1);
            textView.setText(choice.name);
        }
        return v;
    }
}