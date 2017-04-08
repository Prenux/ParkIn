package org.prenux.parkin;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;

class DBHelper extends SQLiteOpenHelper {

    private static final String
            DATABASE_NAME = "Data.db";
    private static final String SPENDING_TABLE_NAME = "SPENDING";
    private static final String SPENDING_COLUMN_ID = "id";
    private static final String SPENDING_COLUMN_CATID = "catid";
    private static final String SPENDING_COLUMN_WHAT = "what";
    private static final String SPENDING_COLUMN_PRICE = "price";
    private static final String SPENDING_COLUMN_CAT = "category";
    private static final String SPENDING_COLUMN_DATE = "date";

    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }


    //Create table if it doesn't exist
    //TODO CHANGE COLUMN NAMES and COLUMN NAMES VARIABLES

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table if not exists " + SPENDING_TABLE_NAME + " " +
                        "(" + SPENDING_COLUMN_ID + " integer primary key, " + SPENDING_COLUMN_CATID + " integer,"
                        + SPENDING_COLUMN_CAT + " text," + SPENDING_COLUMN_WHAT + " text," + SPENDING_COLUMN_PRICE + " float,"
                        + SPENDING_COLUMN_DATE + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SPENDING_TABLE_NAME);
        onCreate(db);
    }


    //Get Data by SQL unique ID
    public Cursor getDataByID(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from spending where id=" + id, null);
    }


    // Get number of rows in the table
    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, SPENDING_TABLE_NAME);
    }

    public String updateExpense(Integer id, int catId, String catStr, String whatStr,
                                Float priceFlt, String datStr) {
        String res = "";
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(SPENDING_COLUMN_CATID, catId);
            contentValues.put(SPENDING_COLUMN_CAT, catStr);
            contentValues.put(SPENDING_COLUMN_WHAT, whatStr);
            contentValues.put(SPENDING_COLUMN_PRICE, priceFlt);
            contentValues.put(SPENDING_COLUMN_DATE, datStr);
            db.update("SPENDING", contentValues, "id = ? ", new String[]{Integer.toString(id)});
        } catch (Exception e) {
            res = e.toString();
        }
        return res;
    }

    //Delete a row based on its unique ID
    public Integer deleteRowbyID(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("SPENDING",
                "id = ? ",
                new String[]{Integer.toString(id)});
    }

    //Delete the database
    void deleteDb(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }


    //USELESS FOR THE CONTEXT BUT STAYS AS EXAMPLES FOR NOW

    /*boolean insertExpense(int catId, String catStr, String whatStr, Float priceFlt, String datStr)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SPENDING_COLUMN_CATID, catId);
        contentValues.put(SPENDING_COLUMN_CAT, catStr);
        contentValues.put(SPENDING_COLUMN_WHAT, whatStr);
        contentValues.put(SPENDING_COLUMN_PRICE, priceFlt);
        contentValues.put(SPENDING_COLUMN_DATE, datStr);
        db.insert(SPENDING_TABLE_NAME, null, contentValues);
        return true;
    }*/

    //Example of fetching all of a column data
    /*
    private ArrayList<Float> getAllPrice()
    {
        ArrayList<Float> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from SPENDING", null );
        res.moveToFirst();

        while(!res.isAfterLast()){
            array_list.add(res.getFloat(res.getColumnIndex(SPENDING_COLUMN_PRICE)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }
    */


    // Example of raw SQL query

    /*double getTotal(){

        double tot = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from SPENDING", null );
        res.moveToFirst();

        while(!res.isAfterLast()){
            tot += res.getFloat(res.getColumnIndex(SPENDING_COLUMN_PRICE));
            res.moveToNext();
        }
        res.close();
        return tot;

    }*/


}
