package com.smap16e.group02.isamonitor.db;

import android.provider.BaseColumns;

/**
 * Created by Lars on 29-09-2016.
 */

public class Table {
    public static final class MeasurementEntry implements BaseColumns{
        public static final String TABLE_NAME = "measurement";

        public static final String COLUMN_PARAM_KEY = "parameterId";
        public static final String COLUMN_VALUE = "value";
        public static final String COLUMN_MEASURE_TIME = "measureTime";


    }

    public static final class ParameterEntry implements BaseColumns{
        public static final String TABLE_NAME = "parameter";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SURNAME = "surname";
        public static final String COLUMN_UNIT = "unit";
        public static final String COLUMN_ISACTIVE = "isActive";
    }
}
