package edu.buffalo.cse.cse486586.simpledynamo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class msgdatabase extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "msgdatabase.db";
    public static final String TABLE_NAME = "dynamo";
    public static final String KEY = "'key'";
    public static final String VALUE = "value";

    private static final String CREATE =
            " CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    KEY + " TEXT NOT NULL ," +
                    VALUE + " TEXT ,"+
                    "id"+" integer primary key autoincrement ,"+
                    "num"+" integer"+")";
    msgdatabase(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase mydb) {
        mydb.execSQL(CREATE);
        Log.d("instert","created");

    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
