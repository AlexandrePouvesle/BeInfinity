package com.beinfinity.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.beinfinity.database.DbContract;

public class DbHelper extends SQLiteOpenHelper {

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    // SCRIPT DE CREATION DES TABLES
    private static final String SQL_CREATE_PARAMETERS =
            "CREATE TABLE " + DbContract.ParameterEntry.TABLE_NAME + " (" +
                    DbContract.ParameterEntry._ID + " INTEGER PRIMARY KEY," +
                    DbContract.ParameterEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    DbContract.ParameterEntry.COLUMN_NAME_CONTENT + TEXT_TYPE + " )";

    private static final String SQL_CREATE_TERRAIN =
            "CREATE TABLE " + DbContract.TerrainEntry.TABLE_NAME + " (" +
            DbContract.TerrainEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            DbContract.ParameterEntry.COLUMN_NAME_TITLE + TEXT_TYPE + " )";

    private static final String SQL_CREATE_BOOKING =
            "CREATE TABLE " + DbContract.BookingEntry.TABLE_NAME + " (" +
                    DbContract.BookingEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    DbContract.BookingEntry.COLUMN_NAME_DATE + TEXT_TYPE + COMMA_SEP +
                    DbContract.BookingEntry.COLUMN_NAME_HEURE_DEBUT + TEXT_TYPE + COMMA_SEP +
                    DbContract.BookingEntry.COLUMN_NAME_HEURE_FIN + TEXT_TYPE + COMMA_SEP +
                    DbContract.BookingEntry.COLUMN_NAME_TERRAIN + TEXT_TYPE + " )";


    // SCRIPTS DE SUPPRESSION DES TABLES
    private static final String SQL_DELETE_PARAMETERS =
            "DROP TABLE IF EXISTS " + DbContract.ParameterEntry.TABLE_NAME;

    private static final String SQL_DELETE_TERRAINS =
            "DROP TABLE IF EXISTS " + DbContract.TerrainEntry.TABLE_NAME;

    private static final String SQL_DELETE_BOOKING =
            "DROP TABLE IF EXISTS " + DbContract.BookingEntry.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 8;
    public static final String DATABASE_NAME = "BeInfinity.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PARAMETERS);
        db.execSQL(SQL_CREATE_TERRAIN);
        db.execSQL(SQL_CREATE_BOOKING);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_PARAMETERS);
        db.execSQL(SQL_DELETE_TERRAINS);
        db.execSQL(SQL_DELETE_BOOKING);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}