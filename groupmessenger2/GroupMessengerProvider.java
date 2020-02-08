package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 *
 * Please read:
 *
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 *
 * before you start to get yourself familiarized with ContentProvider.
 *
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 *
 * @author stevko
 *
 */
public class GroupMessengerProvider extends ContentProvider {


    Map<String,String>  db = new HashMap<String, String>();
    SQLiteDatabase mydb ;
    public static final String TABLE_NAME = "messenger";
    public static final String KEY = "'key'";
    public static final String VALUE = "value";

    private static final String CREATE =
            " CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    KEY + " TEXT NOT NULL UNIQUE," +
                    VALUE + " TEXT)";

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         *
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */
        //Log.d("ho",)

        //db.put(values.)
        //msgdatabase msg = new msgdatabase(getContext());
        //mydb = msg.getWritableDatabase();
        // Log.d("hola",CREATE);
        //values.put(KEY, "key1");
        //values.put(VALUE, "value1");
        Log.d("insert111",values.toString());
        try{
            //mydb.insert(TABLE_NAME,null,values);
            mydb.insertWithOnConflict(TABLE_NAME, null, values,SQLiteDatabase.CONFLICT_REPLACE);
            Log.d("table ",getTableAsString(mydb,TABLE_NAME)) ;

        }catch (Exception e){
            e.printStackTrace();
        }
        //String x=getTableAsString(mydb,TABLE_NAME);
        Log.d("table ",getTableAsString(mydb,TABLE_NAME)) ;



        return uri;
    }


    public String getTableAsString(SQLiteDatabase db, String tableName) {
        Log.d("fasf", "getTableAsString called");
        String tableString = String.format("Table %s:\n", tableName);
        Cursor allRows  = db.rawQuery("SELECT * FROM " + tableName, null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }

        return tableString;
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        msgdatabase msg = new msgdatabase(getContext());
        mydb = msg.getWritableDatabase();
        return true;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */
        msgdatabase ms = new msgdatabase(getContext());

        SQLiteDatabase db = ms.getReadableDatabase();
        String [] pro ={"key","value"};

        String[] Args = {""+selection};
        Log.d("espn",selection);
        // Log.d("holoa", getTableAsString(db,TABLE_NAME));
        Cursor cu = db.query(TABLE_NAME,pro ,"key = ?" ,Args ,null,null,null);
        String[] columnNames = cu.getColumnNames();

        Log.d("query", columnNames[0]);
        return cu;
    }

    private  static class msgdatabase extends SQLiteOpenHelper{

        public static final int DATABASE_VERSION = 2;
        public static final String DATABASE_NAME = "msgdatabase.db";
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
}
