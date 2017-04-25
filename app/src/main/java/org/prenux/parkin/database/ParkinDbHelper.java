package org.prenux.parkin.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.util.BoundingBox;
import org.prenux.parkin.database.ParkinSchema.Parcometer;
import org.prenux.parkin.database.ParkinSchema.ParkinFree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

//TODO: 1) import fichier csv, 2) Creer methode bounding box

public class ParkinDbHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "parkinBase.db";
    public SQLiteDatabase db;
    private Context ctx;

    public ParkinDbHelper(Context context) {

        super(context, DATABASE_NAME, null, VERSION);
        ctx = context;
        db = this.getWritableDatabase();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + Parcometer.NAME + "(" +
                " _id integer primary key autoincrement, " +
                Parcometer.Cols.ID + ", " +
                Parcometer.Cols.LONGITUDE + ", " +
                Parcometer.Cols.MAGNITUDE + ", " +
                Parcometer.Cols.TARIF +
                ")"
        );

        db.execSQL("create table " + ParkinFree.NAME + "(" +
                ParkinFree.Cols.LONGITUDE + ", " +
                ParkinFree.Cols.LATITUDE + ", " +
                ParkinFree.Cols.DESCRIPTION + ", " +
                ParkinFree.Cols.CODE +
                ")"
        );
    }


    public void importFile(String fileName) {
        db.beginTransaction();
        try {
            InputStream inStream = ctx.getResources().getAssets().open(fileName);
            Log.d("CSV", "in try");
            BufferedReader buffer = new BufferedReader(new InputStreamReader(inStream));
            String line = "";
            Log.d("CSV", "in transaction");

            while ((line = buffer.readLine()) != null) {
                String[] columns = line.split(",");
                Log.d("CSV", "in while");

                if (columns.length != 4) {
                    Log.d("CSVParser", "Skipping Bad CSV Row : " + columns.length);
                    continue;
                }
                ContentValues cv = new ContentValues();

                if (fileName.equals("places.csv")) {
                    cv.put(Parcometer.Cols.ID, columns[0].trim());
                    cv.put(Parcometer.Cols.LONGITUDE, columns[1].trim());
                    cv.put(Parcometer.Cols.MAGNITUDE, columns[2].trim());
                    cv.put(Parcometer.Cols.TARIF, columns[3].trim());
                } else {
                    cv.put(ParkinFree.Cols.LONGITUDE, columns[0].trim());
                    cv.put(ParkinFree.Cols.LATITUDE, columns[1].trim());
                    cv.put(ParkinFree.Cols.DESCRIPTION, columns[2].trim());
                    cv.put(ParkinFree.Cols.CODE, columns[3].trim());
                }

                // put data in key value
                db.insert(ParkinSchema.Parcometer.NAME, null, cv);
                Log.d("CSV", "end while");
            }
            Log.d("CSV", "after a  while");
            db.setTransactionSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Exception", e.toString());
        }
        db.endTransaction();
        Log.d("CSV", "Fin");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ParkinSchema.Parcometer.NAME);
        onCreate(db);

    }

    public ArrayList<POI> getFreeParkings(BoundingBox bb) {
        ArrayList<POI> free_parkings = new ArrayList<POI>();
        Cursor res = db.rawQuery("select * from SPENDING", null); //SELECT all from PARKING where parking allowed (bon code)
        res.moveToFirst();

        double north = bb.getLatNorth();
        double south = bb.getLatSouth();
        double east = bb.getLonEast();
        double west = bb.getLonWest();


        res.close();
        return free_parkings;

    }

}



//
   /* public void  getZone(String LONGITUDE, String MAGNITUDE){
        Cursor cursor=null;
        String [] information=new String[4]; // a changer

        try{
            Log.d("Value","BeginTry");
            cursor = db.rawQuery("SELECT id FROM Reglementation WHERE magnetude= ? AND longtitude= ?" ,
                    new String[] {MAGNITUDE + "", LONGITUDE+ ""} );
            Log.d("Value","After query");


            if(cursor.getCount() > 0) {
            cursor.moveToFirst();
                Log.d("Value","After movetofirst");

                for (int i = 0; i < 5; i++) { //SUPER NOOB :)
                information[i] = cursor.getString(cursor.getColumnIndex("id") + i); //
=======
    }


    //
    public void getValueById(String id) {
        Cursor cursor = null;
        String[] information = new String[7]; // a changer
        try {
            cursor = db.rawQuery("SELECT id FROM Reglementation WHERE id=", new String[]{id + ""});  // ..id=" + id ne marchait pas
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                for (int i = 0; i < 8; i++) { //SUPER NOOB :)
                    information[i] = cursor.getString(cursor.getColumnIndex(id) + i); //
                }
>>>>>>> a0dc5b3b110811798284140ac17c1aec2a0959fa
            }
            new Reglement(information);
        } finally {
            cursor.close();
        }
<<<<<<< HEAD
        Log.d("tab", information[0]);
    }
    finally {
        cursor.close();
    }
}
*/










