package org.prenux.parkin.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;



public class ParkinLab {
    //public static ParkinLab spARKINlab;

    //private List <String> mParkin;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public ParkinLab(Context context) {
        mContext=context.getApplicationContext();
        mDatabase=new ParkinDbHelper(mContext).getWritableDatabase();
       // mParkin =new ArrayList<>();

    }
}
