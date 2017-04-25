package org.prenux.parkin.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.prenux.parkin.database.ParkinSchema.ParkinTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.content.ContentValues.TAG;


public class ParkinDbHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "parkinBase.db";
    public SQLiteDatabase db;
    private Context ctx;
    public String fileName = "places.csv";

    public ParkinDbHelper(Context context) {

        super(context, DATABASE_NAME, null, VERSION);
        ctx = context;
        db = this.getWritableDatabase();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + ParkinTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                ParkinTable.Cols.ID + ", " +
                ParkinTable.Cols.Longtitude + ", " +
                ParkinTable.Cols.Magnetude + ", " +
                // ParkinTable.Cols.LongtitudeC + ", " +
                // ParkinTable.Cols.MagnetudeC + ", " +
                // ParkinTable.Cols.Rue + ", " +
                ParkinTable.Cols.Tarif +
                ")"
        );


    }


    public void importFile(String fileName, SQLiteDatabase db) {
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
                cv.put(ParkinTable.Cols.ID, columns[0].trim());
                cv.put(ParkinTable.Cols.Longtitude, columns[1].trim());
                cv.put(ParkinTable.Cols.Magnetude, columns[2].trim());
                // cv.put(ParkinTable.Cols.LongtitudeC, columns[3].trim());
                // cv.put(ParkinTable.Cols.MagnetudeC, columns[4].trim());
                // cv.put(ParkinTable.Cols.Rue, columns[5].trim());
                cv.put(ParkinTable.Cols.Tarif, columns[3].trim());
                // put data in key value
                db.insert(ParkinTable.NAME, null, cv);
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
        db.execSQL("DROP TABLE IF EXISTS " + ParkinTable.NAME);
        onCreate(db);
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
            }
            new Reglement(information);
        } finally {
            cursor.close();
        }
    }
}