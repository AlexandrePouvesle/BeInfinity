package com.beinfinity.database;

import android.provider.BaseColumns;

/**
 * Created by Alexandre on 09/10/2016.
 */

public class DbContract {
    private DbContract() {}

    /* Inner class that defines the table contents */
    public static class ParameterEntry implements BaseColumns {

        public static String[] ProjectionParameter = {
                DbContract.ParameterEntry._ID,
                DbContract.ParameterEntry.COLUMN_NAME_TITLE,
                DbContract.ParameterEntry.COLUMN_NAME_CONTENT
        };

        public static final String TABLE_NAME = "Parameter";
        public static final String COLUMN_NAME_TITLE = "name";
        public static final String COLUMN_NAME_CONTENT = "content";
    }

    public static class TerrainEntry implements BaseColumns {

        public static String[] ProjectionTerrain = {
                DbContract.TerrainEntry._ID,
                DbContract.TerrainEntry.COLUMN_NAME_TITLE
        };

        public static final String TABLE_NAME = "Terrain";
        public static final String COLUMN_NAME_TITLE = "name";
    }

    public static class BookingEntry implements BaseColumns {

        public static final String TABLE_NAME = "Booking";
        public static final String COLUMN_NAME_TERRAIN = "terrain";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_HEURE_DEBUT = "debut";
        public static final String COLUMN_NAME_DUREE = "duree";
        public static final String COLUMN_NAME_DUREE_MINUTE = "dureeMinute";
    }
}