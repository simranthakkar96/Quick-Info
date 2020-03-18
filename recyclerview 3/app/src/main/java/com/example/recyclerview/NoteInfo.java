package com.example.recyclerview;


import android.provider.BaseColumns;

public class NoteInfo {


    public static final class NoteColumns implements BaseColumns {
        public static final String TABLE_NAME = "noteList";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_LATITUDE= "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_PATH = "path";


        public static final String TABLE_NAME1 = "catList";
        public static final String COLUMN_CATEGORY1 = "category";
    }
}