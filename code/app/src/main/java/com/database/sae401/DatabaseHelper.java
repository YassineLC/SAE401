package com.database.sae401;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sae.db";
    private static String DATABASE_PATH = ""; // Ce sera défini dans le constructeur
    private static final int DATABASE_VERSION = 1;
    private final Context myContext;
    private SQLiteDatabase myDatabase;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.myContext = context;
        DATABASE_PATH = context.getDatabasePath(DATABASE_NAME).getPath(); // Définir le chemin de la base de données
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Pas besoin de créer de table car nous utilisons une base de données existante
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Pas besoin de gérer la mise à jour pour l'instant
    }

    public void createDatabase() throws IOException {
        boolean dbExist = checkDatabase();
        if (!dbExist) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDatabase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private boolean checkDatabase() {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLException e) {
            // La base de données n'existe pas encore
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null;
    }

    private void copyDatabase() throws IOException {
        InputStream myInput = myContext.getAssets().open(DATABASE_NAME);
        OutputStream myOutput = new FileOutputStream(DATABASE_PATH);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void openDatabase() throws SQLException {
        myDatabase = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {
        if (myDatabase != null) {
            myDatabase.close();
        }
        super.close();
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return myDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }
}
