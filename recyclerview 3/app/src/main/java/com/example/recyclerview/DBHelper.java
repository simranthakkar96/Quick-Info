package com.example.recyclerview;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.recyclerview.NoteInfo.*;

import java.util.ArrayList;
import java.util.List;

import static com.example.recyclerview.NoteInfo.NoteColumns.COLUMN_CATEGORY;
import static com.example.recyclerview.NoteInfo.NoteColumns.COLUMN_CATEGORY1;
import static com.example.recyclerview.NoteInfo.NoteColumns.TABLE_NAME;
import static com.example.recyclerview.NoteInfo.NoteColumns.TABLE_NAME1;


public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "notes.db";
    public static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    Cursor cursor1;
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_GROCERYLIST_TABLE = "CREATE TABLE " +
                NoteColumns.TABLE_NAME + " (" +
                NoteColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NoteColumns.COLUMN_TITLE + " TEXT, " +
                NoteColumns.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                NoteColumns.COLUMN_CATEGORY + " TEXT NOT NULL, " +
                NoteColumns.COLUMN_DATE + " TEXT NOT NULL, " +
                NoteColumns.COLUMN_IMAGE + " TEXT ," +
                NoteColumns.COLUMN_LATITUDE + " DOUBLE NOT NULL," +
                NoteColumns.COLUMN_LONGITUDE + " DOUBLE NOT NULL," +
                NoteColumns.COLUMN_PATH + " TEXT " +
                ");";

        final String SQL_CREATE_CATEGORYLIST_TABLE = "CREATE TABLE " +
                NoteColumns.TABLE_NAME1 + " (" +
                NoteColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NoteColumns.COLUMN_CATEGORY1 + " TEXT NOT NULL " +
                ");";

        db.execSQL(SQL_CREATE_GROCERYLIST_TABLE);
        db.execSQL(SQL_CREATE_CATEGORYLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + NoteColumns.TABLE_NAME);
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS " + NoteColumns.TABLE_NAME1);
        onCreate(db);
    }

    /**
     * Inserting new lable into lables table
     * */
    public void insertLabel(String label){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY1, label);

        // Inserting Row
        db.insert(TABLE_NAME1
                , null, values);
    }

    /**
     * Getting all labels
     * returns list of labels
     * */
    public List<String> getAllLabels(){
        List<String> labels = new ArrayList<String>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME1;

        SQLiteDatabase db1 = this.getReadableDatabase();
        cursor1 = db1.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor1.moveToFirst()) {
            do {
                labels.add(cursor1.getString(1));
            } while (cursor1.moveToNext());
        }

        // closing connection
        cursor1.close();

        // returning lables
        return labels;
    }
}