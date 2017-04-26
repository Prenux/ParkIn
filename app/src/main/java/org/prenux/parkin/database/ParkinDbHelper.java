package org.prenux.parkin.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.prenux.parkin.ImportFileTask;
import org.prenux.parkin.database.ParkinSchema.Parcometer;
import org.prenux.parkin.database.ParkinSchema.ParkinFree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ParkinDbHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "parkinBase.db";
    public SQLiteDatabase db;
    private Context ctx;

    //Sets used to process rules
    String[] DENYTERMS = {"excepte", "interdit", "debarcadere", "reserve", "entretien", "sortie", "autobus", "pompier", "en tout temps"};

    //Minuit = 0, enleve le 1er 0
    Map<String, Double> HOURS_MAP = new HashMap<String, Double>() {{
        put("0h", 0.0);
        put("0h30", 0.5);
        put("1h", 1.0);
        put("1h30", 1.5);
        put("2h", 2.0);
        put("2h30", 2.5);
        put("3h", 3.0);
        put("3h30", 3.5);
        put("4h", 4.0);
        put("4h30", 4.5);
        put("5h", 5.0);
        put("5h30", 5.5);
        put("6h", 6.0);
        put("6h30", 6.5);
        put("7h", 7.0);
        put("7h30", 7.5);
        put("8h", 8.0);
        put("8h30", 8.5);
        put("9h", 9.0);
        put("9h30", 9.5);
        put("10h", 10.0);
        put("10h30", 10.5);
        put("11h", 11.0);
        put("11h30", 11.5);
        put("12h", 12.0);
        put("12h30", 12.5);
        put("13h", 13.0);
        put("13h30", 13.5);
        put("14h", 14.0);
        put("14h30", 14.5);
        put("15h", 15.0);
        put("15h30", 15.5);
        put("16h", 16.0);
        put("16h30", 16.5);
        put("17h", 17.0);
        put("17h30", 17.5);
        put("18h", 18.0);
        put("18h30", 18.5);
        put("19h", 19.0);
        put("19h30", 19.5);
        put("20h", 20.0);
        put("20h30", 20.5);
        put("21h", 21.0);
        put("21h30", 21.5);
        put("22h", 22.0);
        put("22h30", 22.5);
        put("23h", 23.0);
        put("23h30", 23.5);
    }};

    //DIM = 1
    Map<String, Double> DAYS_MAP = new HashMap<String, Double>() {{
        put("dim", 1.0);
        put("dimanche", 1.0);
        put("lun", 2.0);
        put("lundi", 2.0);
        put("mar", 3.0);
        put("mardi", 3.0);
        put("mer", 4.0);
        put("mercredi", 4.0);
        put("jeu", 5.0);
        put("jeudi", 5.0);
        put("ven", 6.0);
        put("vendredi", 6.0);
        put("sam", 7.0);
        put("samedi", 7.0);
    }};

    //JAN = 0
    Map<String, Double> MONTHS_MAP = new HashMap<String, Double>() {{
        put("jan", 0.0);
        put("janvier", 0.0);
        put("fev", 1.0);
        put("fevrier", 1.0);
        put("mars", 2.0);
        put("avril", 3.0);
        put("mai", 4.0);
        put("juin", 5.0);
        put("juillet", 6.0);
        put("aout", 7.0);
        put("sept", 8.0);
        put("oct", 9.0);
        put("nov", 10.0);
        put("novembre", 10.0);
        put("dec", 11.0);
        put("decembre", 11.0);
    }};

    public ParkinDbHelper(Context context) {

        super(context, DATABASE_NAME, null, VERSION);
        ctx = context;
        db = this.getWritableDatabase();

/*        //TESTING for rules
        String[] rules = {
                "09h-18h MAR. MER. VEN. 1 MARS AU 1 DEC.",
                "LUN 17H À MAR 17H - MER 17H À JEU 17H - VEN 17H À SAM 17H",
                "Yp 08h - 12h mercredi 1er mars - 1er déc",
                "RESERVE AUTOBUS 15h-18h30 LUN. AU VEN.",
                "06h-07h LUN. MER. VEN. SAM. 1 AVRIL AU 1 DEC.",
                "14h30-15h30 LUN. JEU. 1 MARS AU 1 DEC.",
                "RESERVE VEHICULES DE LA VILLE",
                "P 30 MIN 8h - 10h et 15h30 - 17h30 LUN À VEN",
        };

        for (String str : rules) {
            Log.d("sugarraysam" + str, "Value: " + validateTime(str));
        }*/
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
                ParkinFree.Cols.LONGITUDE + " REAL, " +
                ParkinFree.Cols.LATITUDE + " REAL, " +
                ParkinFree.Cols.DESCRIPTION + ", " +
                ParkinFree.Cols.CODE +
                ")"
        );
        new ImportFileTask("sign4.csv", ctx, this).execute();
    }


    public void importFile(String fileName) {
        db.beginTransaction();
        try {
            InputStream inStream = ctx.getResources().getAssets().open(fileName);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(inStream));
            String line = "";

            while ((line = buffer.readLine()) != null) {
                String[] columns = line.split(",");

                ContentValues cv = new ContentValues();

                if (fileName.equals("places.csv")) {
                    cv.put(Parcometer.Cols.ID, columns[0].trim());
                    cv.put(Parcometer.Cols.LONGITUDE, columns[1].trim());
                    cv.put(Parcometer.Cols.MAGNITUDE, columns[2].trim());
                    cv.put(Parcometer.Cols.TARIF, columns[3].trim());
                    db.insert(Parcometer.NAME, null, cv);
                } else if(fileName.equals("sign4.csv")) {
                    cv.put(ParkinFree.Cols.LONGITUDE, columns[0].trim());
                    cv.put(ParkinFree.Cols.LATITUDE, columns[1].trim());
                    cv.put(ParkinFree.Cols.DESCRIPTION, columns[2].trim());
                    cv.put(ParkinFree.Cols.CODE, columns[3].trim());
                    db.insert(ParkinFree.NAME, null, cv);
                }
            }
            db.setTransactionSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Exception", e.toString());
        }
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ParkinSchema.Parcometer.NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ParkinSchema.ParkinFree.NAME);
        onCreate(db);
    }

    //Method used to find free parkings that are in distance of the bounding box,
    //which is the region identified by the size of your screen
    public ArrayList<GeoPoint> getFreeParkings(BoundingBox bb) {
        Log.d("ParkinDBHelperDebug", "getting free parkings");
        ArrayList<GeoPoint> free_parkings = new ArrayList<>();
        double north = bb.getLatNorth();
        double south = bb.getLatSouth();
        double east = bb.getLonEast();
        double west = bb.getLonWest();
        try {
            Cursor res = db.rawQuery("SELECT * FROM PARKINFREE WHERE longitude<=" + east + " AND longitude>=" + west +
                    " AND latitude<=" + north + " AND latitude>=" + south, null); //SELECT all from PARKING where parking allowed (bon code)
            res.moveToFirst();
            Log.d("ParkinDBHelperDebug", "SELECT * FROM PARKINFREE WHERE longitude<=" + east + " AND longitude>=" + west +
                    " AND latitude<=" + north + " AND latitude>=" + south);
            Log.d("ParkinDBHelperDebug", "Before WHILE");
            //Ajoute les parkings trouves a ArrayList
            while (!res.isAfterLast()) {
                if (validateTime(res.getString(res.getColumnIndex(ParkinFree.Cols.DESCRIPTION)))) {
                    Log.d("ParkinDBHelperDebug", "IN IF");
                    free_parkings.add(new GeoPoint(res.getDouble(res.getColumnIndex(ParkinFree.Cols.LATITUDE)),
                            res.getDouble(res.getColumnIndex(ParkinFree.Cols.LONGITUDE))));
                }
                res.moveToNext();
            }

            res.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("ParkinDBHelperDebug", "Size : " + free_parkings.size());
        return free_parkings;
    }

    //Method to validate if a user can park in a certain free parking at a certain time
    //according to the reglementation and our analysis of it
    public boolean validateTime(String rule) {
        //lowercase, enlever "." et "-" et les accents
        rule = rule.toLowerCase().replace(".", "").replace("-", " ");
        rule = Normalizer.normalize(rule, Normalizer.Form.NFD);
        rule = rule.replaceAll("[^\\p{ASCII}]", "");

        //Check deny terms
        for (String str : DENYTERMS) if (rule.contains(str)) return false;

        //Split rule in parts and create ArrayList that will be used to retrieve bounds on values
        String[] splited_rule = rule.split(" ");
        List<Double> hours = new ArrayList<>();
        List<Double> days = new ArrayList<>();
        List<Double> months = new ArrayList<>();

        outerLoop:
        for (String rule_part : splited_rule) {
            //Remove leading 0 to reduce HOURS_MAP length
            if (rule_part.startsWith("0"))
                rule_part = rule_part.substring(1);

            //Populate hours bounds
            for (Map.Entry<String, Double> entry : HOURS_MAP.entrySet()) {
                String key = entry.getKey();
                Double value = entry.getValue();

                //ajoute la regle et quitte la boucle
                if (rule_part.equals(key)) {
                    hours.add(value);
                    continue outerLoop; //go to next rule part
                }
            }

            //Populate days bounds
            for (Map.Entry<String, Double> entry : DAYS_MAP.entrySet()) {
                String key = entry.getKey();
                Double value = entry.getValue();

                //ajoute la regle et quitte la boucle
                if (rule_part.equals(key)) {
                    days.add(value);
                    continue outerLoop; //go to next rule part
                }
            }

            //Populate month bounds
            for (Map.Entry<String, Double> entry : MONTHS_MAP.entrySet()) {
                String key = entry.getKey();
                Double value = entry.getValue();

                //ajoute la regle et quitte la boucle
                if (rule_part.equals(key)) {
                    months.add(value);
                    continue outerLoop; //go to next rule part
                }
            }
        }
        //after processing all words, check bounds with current time
        return decideValidity(hours, days, months);
    }

    public boolean decideValidity(List<Double> hours, List<Double> days, List<Double> months) {
        //Get current time
        Calendar cal = Calendar.getInstance();
        double day_of_week = cal.get(Calendar.DAY_OF_WEEK);
        double hour_of_day = cal.get(Calendar.HOUR);
        double min_of_day = cal.get(Calendar.MINUTE);
        double month = cal.get(Calendar.MONTH);

        hour_of_day = hour_of_day + (min_of_day / 100);

        //Create two arrays to synchronize checking of bounds
        Double[] values = {hour_of_day, day_of_week, month};
        Object[] bounds = {hours, days, months};

        for (int i = 0; i < values.length; i++) {
            List<Double> b = (List<Double>) bounds[i];
            Double v = values[i];
            switch (b.size()) {
                case 0: //aucune bound dans la liste
                    break;
                case 1: //une bound alors fait un exact match
                    if (v.equals(b.get(0)))
                        break;
                    else
                        return false;
                case 2: //deux bound alors interval
                    if (v >= b.get(0) && v <= b.get(1))
                        break;
                    else
                        return false;
                case 3: //3 test equality
                    if (v.equals(b.get(0)) || v.equals(b.get(1)) || v.equals(b.get(2)))
                        break;
                    else
                        return false;
                case 4: //2 intervals
                    if ((v >= b.get(0) && v <= b.get(1)) ||
                            (v >= b.get(2) && v <= b.get(3)))
                        break;
                    else
                        return false;
                case 6: //3 intervals
                    if ((v >= b.get(0) && v <= b.get(1)) ||
                            (v >= b.get(2) && v <= b.get(3)) ||
                            (v >= b.get(4) && v <= b.get(5)))
                        break;
                    else
                        return false;
                default: //tous les autres cas, format invalide retourne faux dans le doute
                    return false;
            }
        }
        //passe tous les tests retourne true
        return true;
    }
}








