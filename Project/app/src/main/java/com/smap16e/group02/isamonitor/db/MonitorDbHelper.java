package com.smap16e.group02.isamonitor.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Lars on 29-09-2016.
 */

public class MonitorDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "monitor.db";

    public MonitorDbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private final String CREATE_TABLE_PARAMETER = "CREATE TABLE " + Table.ParameterEntry.TABLE_NAME + " ("
            + Table.ParameterEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + Table.ParameterEntry.COLUMN_NAME + " TEXT NOT NULL,"
            + Table.ParameterEntry.COLUMN_SURNAME + " TEXT NOT NULL,"
            + Table.ParameterEntry.COLUMN_UNIT + " TEXT,"
            + Table.ParameterEntry.COLUMN_ISACTIVE + " INTEGER DEFAULT 0"
            + ")";

    private final String CREATE_TABLE_MEASUREMENT = "CREATE TABLE " + Table.MeasurementEntry.TABLE_NAME + " ("
            + Table.MeasurementEntry.COLUMN_PARAM_KEY + " INTEGER NOT NULL,"
            + Table.MeasurementEntry.COLUMN_VALUE + " REAL NOT NULL,"
            + Table.MeasurementEntry.COLUMN_MEASURE_TIME + " INTEGER NOT NULL"
            + ")";
    //todo: Add Unique constraint to parameterId + MeasureTime with replace conflict-clause??

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
