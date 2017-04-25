package org.prenux.parkin;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;

import org.prenux.parkin.database.ParkinDbHelper;

/**
 * Created by prenux on 12/04/17.
 */

public class ImportFileTask extends AsyncTask<Object, Void, Boolean> {
    SQLiteDatabase db;
    String fileName;
    Context ctx;
    ParkinDbHelper dbHelper;

    ImportFileTask(String fileName, Context ctx, ParkinDbHelper dbHelper) {
        this.fileName = fileName;
        this.ctx = ctx;
        this.dbHelper = dbHelper;
        this.db = dbHelper.db;
    }

    protected Boolean doInBackground(Object... params) {
        try {
            this.dbHelper.importFile(this.fileName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected void onPostExecute(Boolean boule) {
        if (boule) {
            Toast.makeText(ctx, ctx.getResources().getString(R.string.DBUpdated), Toast.LENGTH_SHORT);
        } else {
            Toast.makeText(ctx, ctx.getResources().getString(R.string.DBError), Toast.LENGTH_SHORT);
        }
    }
}
