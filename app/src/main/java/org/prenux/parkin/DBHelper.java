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

    private static final String DATABASE_NAME = "Data.db";
    private static final String SPENDING_TABLE_NAME = "SPENDING";
    private static final String SPENDING_COLUMN_ID = "id";
    private static final String SPENDING_COLUMN_CATID = "catid";
    private static final String SPENDING_COLUMN_WHAT = "what";
    private static final String SPENDING_COLUMN_PRICE = "price";
    private static final String SPENDING_COLUMN_CAT = "category";
    private static final String SPENDING_COLUMN_DATE = "date";

    DBHelper(Context context)
    {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table if not exists "+ SPENDING_TABLE_NAME +" " +
                        "(" + SPENDING_COLUMN_ID + " integer primary key, "+ SPENDING_COLUMN_CATID +" integer,"
                        + SPENDING_COLUMN_CAT +" text,"+ SPENDING_COLUMN_WHAT +" text,"+ SPENDING_COLUMN_PRICE +" float,"
                        + SPENDING_COLUMN_DATE + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS SPENDING");
        onCreate(db);
    }

    //USELESS FOR THE CONTEXT BUT STAYS AS EXAMPLE FOR NOW

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

    public Cursor getData(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from spending where id="+id, null );
    }

    Cursor getCatTot(int catId){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from spending where catId="+catId,null);
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, SPENDING_TABLE_NAME);
    }

    public String updateExpense (Integer id, int catId, String catStr, String whatStr, Float priceFlt, String datStr)
    {
        String res= "";
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(SPENDING_COLUMN_CATID, catId);
            contentValues.put(SPENDING_COLUMN_CAT, catStr);
            contentValues.put(SPENDING_COLUMN_WHAT, whatStr);
            contentValues.put(SPENDING_COLUMN_PRICE, priceFlt);
            contentValues.put(SPENDING_COLUMN_DATE, datStr);
            db.update("SPENDING", contentValues, "id = ? ", new String[]{Integer.toString(id)});
        } catch (Exception e){
            res = e.toString();
        }
        return res;
    }

    public Integer deleteExpense (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("SPENDING",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    void deleteDb(Context context){
        context.deleteDatabase(DATABASE_NAME);
    }

    private ArrayList<String> getAllExpense()
    {
        ArrayList<String> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from SPENDING", null );
        res.moveToFirst();

        while(!res.isAfterLast()){
            array_list.add(res.getString(res.getColumnIndex(SPENDING_COLUMN_WHAT)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }

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

    private ArrayList<Integer> getAllId()
    {
        ArrayList<Integer> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from SPENDING", null );
        res.moveToFirst();

        while(!res.isAfterLast()){
            array_list.add(res.getInt(res.getColumnIndex(SPENDING_COLUMN_ID)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }

    private ArrayList<String> getAllDate()
    {
        ArrayList<String> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from SPENDING", null );
        res.moveToFirst();

        while(!res.isAfterLast()){
            array_list.add(res.getString(res.getColumnIndex(SPENDING_COLUMN_DATE)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }

    ArrayList<String> getAllExpPrice(){
        ArrayList<String> whatArr = getAllExpense();
        ArrayList<Float> priceArr = getAllPrice();
        ArrayList<Integer> idArr = getAllId();
        ArrayList<String> datArr = getAllDate();
        ArrayList<String> resArr = new ArrayList<>();

        for(int i=0;i<whatArr.size();i++){
            resArr.add(String.valueOf(idArr.get(i)) + ".  " + String.format("%s :         %s    at      %s",whatArr.get(i),String.valueOf(priceArr.get(i)), datArr.get(i)));
        }
        Collections.reverse(resArr);
        return resArr;
    }

    double getTotal(){

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

    }

}
