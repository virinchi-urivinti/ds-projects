package edu.buffalo.cse.cse486586.simpledht;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

public class SimpleDhtProvider extends ContentProvider {
    HashMap<BigInteger,Boolean> is_alive = new HashMap<BigInteger, Boolean>();
    HashMap<BigInteger,String> revhash = new HashMap<BigInteger, String>();
    String p1 = "11108";
    String p2 = "11112";
    String p3 = "11116";
    String p4 = "11120";
    String p5 = "11124";
    String my_port = null;
    boolean del =false;
    Object obj = new Object();
    int a = 0 ;
    int insert_count = 0;
    boolean lock = true;
    ArrayList<String> alive_ports =new ArrayList<String>();
    int alive_count = 0;
    String ports[] = {p1,p2,p3,p4,p5};
    String ports_by2 [] ={"5554","5556","5558","5560","5562"};
    SQLiteDatabase mydb ;
    BigInteger my_succ ;
    BigInteger my_pred;
    BigInteger my_id;
    ServerSocket serverSocket =null;
    public static final String TABLE_NAME = "dht";
    public static final String KEY = "'key'";
    public static final String VALUE = "value";

    private static final String CREATE =
            " CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    KEY + " TEXT NOT NULL UNIQUE," +
                    VALUE + " TEXT)";

    public String main_node = "5554";

    BigInteger succ(BigInteger id){

        BigInteger b1 = new BigInteger("2");
        BigInteger max_val =b1.pow(160);
        if(is_alive.containsKey(id) && is_alive.get(id)){
            Log.d("yos","id");

            return id;
        }else{
            BigInteger min1 = max_val;
            BigInteger min2 = max_val;
            for(BigInteger name : is_alive.keySet()){
                if(is_alive.get(name)){
                    if(id.compareTo(name)>0){
                        if(min1.compareTo(name)>0){
                            Log.d("yo","yo");
                            min1 = name;
                        }
                    }
                    if(id.compareTo(name)<0){
                        if(min2.compareTo(name)>0){
                            min2 = name;
                        }
                    }
                }
            }
            if(min2.compareTo(max_val)!=0){
                Log.d("yos","+min1");

                return min2;
            }
            Log.d("yos","+min2");
            return min1;
        }
    }



    public Cursor convert_stringarry_tocursor(String[] arr){
        Log.d("calledconvertstring","cursor");
        Cursor cu = null;
        return cu ;
    }


    public void store (Uri uri, ContentValues values){
       final String bkey = (String)values.get("key");
       insert_count =insert_count+1;
        Log.d("insertcountstart",""+insert_count);
        Log.d("Store id",(String)values.get("key"));
        final String value= (String)values.get("value");
        final BigInteger key = Hash(bkey);
        BigInteger To_be_inserted_at = succ(key);
        Log.d("tobeinsertat",""+To_be_inserted_at);
        final String to_insert_port =  Integer.toString(2*Integer.parseInt(revhash.get(To_be_inserted_at)));
        Log.d("store"+(String)values.get("key")+"to be ins",to_insert_port);
        String []info = {"obtained key: "+bkey, "hashed key: "+key ,"succ: "+To_be_inserted_at, "port to be: "+to_insert_port };
        Log.d("main info",Arrays.toString(info));
        TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        final String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        if(to_insert_port.equals(myPort)){
            try{
                Log.d("myport this is ","insert");
                mydb.insertWithOnConflict(TABLE_NAME, null, values,SQLiteDatabase.CONFLICT_REPLACE);
                Log.d("table ",getTableAsString(mydb,TABLE_NAME)) ;

            }catch (Exception e){
                Log.d("insert","error with insert command");
                e.printStackTrace();
            }
        }else
            {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {


                try {

                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(to_insert_port));
                    Log.d("insertconn","formed");
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    String [] array =  {""+bkey,""+value,""+my_pred,portStr,"sf","insert"};
                    out.writeObject(array);
                    Log.d("insertconn","sent");
                    out.flush();

                    Log.d("insertconn","closed");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        //new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "insert",to_insert_port,""+key,value);
        Log.d("insertcount",""+insert_count);

     }
    }



    public  String return_port(String key){

        BigInteger b = Hash(key);
        b = succ(b);
        String x = revhash.get(b);

        return String.valueOf((Integer.parseInt(x) * 2));


    }





    public void join(){
        String msg = "join";

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
                final String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
                final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
                try {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt("11108"));
                    String arr[] = {""+my_id,"value","x","y",portStr, "join"};

                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    out.writeObject(arr);
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
        thread.start();
    }



    public  void print_hash(HashMap<BigInteger,Boolean> map){
        for (BigInteger name: map.keySet()){

            String key =name.toString();
            String value = map.get(name).toString();
            Log.d( "keez"+key,value);
        }
    }



    public  void print_rev_hash(HashMap<BigInteger,String> map){
        for (BigInteger name: map.keySet()){
            String key =name.toString();
            String value = map.get(name);
            Log.d("keezu"+key,value);
        }
    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        Log.d("called","oncreate");


        msgdatabase msg = new msgdatabase(getContext());
        mydb = msg.getWritableDatabase();

        TelephonyManager tel = (TelephonyManager)getContext().getSystemService(Context.TELEPHONY_SERVICE);
        final String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        my_port = myPort;
        my_id = Hash(portStr);
        my_succ = my_id;
        my_pred= my_id;
        is_alive.put(my_id,true);

    //         Log.d("node info", "id"+n54.my_id);
   //       Log.d("node succ",""+n54.my_succ.my_id);
        //    Log.d("mp",portStr);

        try {

             serverSocket =new ServerSocket(10000);
            //serverSocket.setReuseAddress(true);
            //serverSocket.bind(new InetSocketAddress(10000));

            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            Thread.sleep(1000);
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String join_msg = "join";
        join();
        Log.d("hola","sdf");

        //print_hash(is_alive);
        revhash.put(Hash("5554"), "5554");
        revhash.put(Hash("5556"), "5556");
        revhash.put(Hash("5558"), "5558");
        revhash.put(Hash("5560"), "5560");
        revhash.put(Hash("5562"), "5562");
        print_rev_hash(revhash);

        try {
            Thread.sleep(5000);
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(alive_count>1){
        if(portStr.equals("5554")) {
            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "alive_table", myPort);
        }}
        Log.d("about to notify ","notify");
       // synchronized(obj){
            //obj.notifyAll();
        //}

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lock =false;

        return true;

    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        Log.d("delete called","from delete");


            mydb.execSQL("delete from "+ TABLE_NAME);

            del =true;
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }





    public BigInteger Hash(String key){
        String actual_key = null;
        try {
            actual_key = genHash(key);
            Log.d("key recived ",actual_key);
            BigInteger b1 = new BigInteger(actual_key, 16);
            Log.d("converted keu",""+b1);
            BigInteger b2 = new BigInteger("16");
            BigInteger final_key = b1.mod(b2);
            Log.d("ihd",""+b1);

            return b1;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;

    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
      store(uri,values);

        Log.d("from insert","insert called");

        String bkey = new String() ;
        String value = new String();


        bkey = (String)values.get("key");
        Log.d("id",(String)values.get("key"));
        value= (String)values.get(bkey);


        Log.d("insert111",values.toString());
        try{
           // mydb.insert(TABLE_NAME,null,values);
            //mydb.insertWithOnConflict(TABLE_NAME, null, values,SQLiteDatabase.CONFLICT_REPLACE);
            Log.d("table ",getTableAsString(mydb,TABLE_NAME)) ;

        }catch (Exception e){
            Log.d("insert","error with insert command");
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
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    @Override
    public Cursor query(Uri uri, String[] projection, final String selection, String[] selectionArgs,
                        String sortOrder) {
        int x = is_alive.size();


        Log.d("query called","from query");
        Log.d("print alive coiunt",""+alive_count);

        msgdatabase ms = new msgdatabase(getContext());

        SQLiteDatabase db = ms.getReadableDatabase();
        String [] pro ={"key","value"};
        Cursor cur =null;
        String[] Args = {selection};
       final MatrixCursor cursor = new MatrixCursor(new String[] {"key","value"});
        //Log.d("asdf",Hash(selection));

        if(selection.equals("@")){
            Cursor cu = db.rawQuery("select * from " + TABLE_NAME, null);
            return cu;
        }
        if(selection.equals("*") && x < 2)
        {
            if(del==true){
                Cursor l =null;
                return l;
            }
            return db.rawQuery("select * from " + TABLE_NAME, null);
        }

        if(selection.equals("*") && x >2){
            Log.d("*query","called");
            Log.d("*del","called"+del);
            if(del==true){
                Cursor l =null;
                return l;
            }
            Log.d("*1","called"+del);
           final  MatrixCursor matrixCursor = new MatrixCursor(new String[] {"key", "value"});
            final CountDownLatch latch = new CountDownLatch(1);
            Log.d("*12","called"+del);
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    //Socket[] sockets = new Socket[5];
                    Socket[] socketobj = new Socket[5];
                    // Object stream has been referred from stack overflow: https://stackoverflow.com/questions/12895450/sending-an-arrayliststring-from-the-server-side-to-the-client-side-over-tcp-us
                    ObjectOutputStream[] objOutput = new ObjectOutputStream[5];
                    ObjectInputStream[] objInput = new ObjectInputStream[5];

                    for (int i = 0 ;i<5 ;i++)
                    {
                        Log.d("*forloop","inside");
                        if(is_alive.containsKey(Hash(ports_by2[i]))){
                            Log.d("*isalive","inside");
                        try
                        {
                             socketobj[i] = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                    Integer.parseInt(ports[i]));
                            Log.d("*socket","created");


                            String [] arr = {"*","1","2","3","4","query"};
                            try   { ObjectOutputStream out = new ObjectOutputStream(socketobj[i].getOutputStream());
                                Log.d("*outputstream","created");
                                out.writeObject(arr);
                                Log.d("*sent","created");
                                out.flush();}
                            catch(Exception e){
                                e.printStackTrace();
                            }

                            try{
                                objInput[i] = new ObjectInputStream(socketobj[i].getInputStream());
                            ArrayList<ArrayList<String>> at =(ArrayList<ArrayList<String>>)objInput[i].readObject();
                            Log.d("cursor from",ports[i]);
                            Log.d("recieved arraylist",TextUtils.join(",", at));

                           // matrixCursor.newRow().add("key",at.get(0).get(0)).add("value",at.get(1).get(0));

                            for (int j = 0; j < at.get(0).size(); j++) {
                                matrixCursor.newRow().add("key",at.get(0).get(j)).add("value",at.get(1).get(j));

                            }}catch (Exception e ){
                                e.printStackTrace();
                            }


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        }

                    }
                    latch.countDown();


                }
            });
            thread.start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d("matrixcursordumping",DatabaseUtils.dumpCursorToString(matrixCursor));

            return matrixCursor;

        }

        if(selection.equals("*") && x ==2){
            Log.d("*query","called");
            Log.d("*del","called"+del);
            if(del==true){
                Cursor l =null;
                return l;
            }
            Log.d("*1","called"+del);
            final  MatrixCursor matrixCursor = new MatrixCursor(new String[] {"key", "value"});
            final CountDownLatch latch = new CountDownLatch(1);
            Log.d("*12","called"+del);
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    //Socket[] sockets = new Socket[5];
                    Socket[] socketobj = new Socket[5];
                    // Object stream has been referred from stack overflow: https://stackoverflow.com/questions/12895450/sending-an-arrayliststring-from-the-server-side-to-the-client-side-over-tcp-us
                    ObjectOutputStream[] objOutput = new ObjectOutputStream[5];
                    ObjectInputStream[] objInput = new ObjectInputStream[5];
                    int i=0;
                    for (String porte  : alive_ports)
                    {
                        Log.d("*forloop","inside");
                        if(is_alive.containsKey(Hash(porte))){
                            Log.d("*isalive","inside");
                            try
                            {
                                socketobj[i] = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                        2*Integer.parseInt(porte));
                                Log.d("*socket","created");


                                String [] arr = {"*","1","2","3","4","query"};
                                try   { ObjectOutputStream out = new ObjectOutputStream(socketobj[i].getOutputStream());
                                    Log.d("*outputstream","created");
                                    out.writeObject(arr);
                                    Log.d("*sent","created");
                                    out.flush();}
                                catch(Exception e){
                                    e.printStackTrace();
                                }

                                try{
                                    objInput[i] = new ObjectInputStream(socketobj[i].getInputStream());
                                    ArrayList<ArrayList<String>> at =(ArrayList<ArrayList<String>>)objInput[i].readObject();
                                    Log.d("cursor from",ports[i]);
                                    Log.d("recieved arraylist",TextUtils.join(",", at));

                                    // matrixCursor.newRow().add("key",at.get(0).get(0)).add("value",at.get(1).get(0));

                                    for (int j = 0; j < at.get(0).size(); j++) {
                                        matrixCursor.newRow().add("key",at.get(0).get(j)).add("value",at.get(1).get(j));

                                    }}catch (Exception e ){
                                    e.printStackTrace();
                                }


                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    latch.countDown();


                }
            });
            thread.start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d("matrixcursordumping",DatabaseUtils.dumpCursorToString(matrixCursor));

            return matrixCursor;

        }




        if(!selection.equals("@") && !selection.equals("*"))
            {

                final CountDownLatch latch = new CountDownLatch(1);


                if(return_port(selection).equals(my_port)){
                    return db.query(TABLE_NAME,pro ,"key = ?" ,Args ,null,null,null);
                }

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            Log.d("searchport", return_port(selection));

                            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                    Integer.parseInt(return_port(selection)));
                            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                            Log.d("query outstream","formed");
                            String [] arr = {selection,"1","2","3","4","query"};
                            out.writeObject(arr);
                            Log.d("query req","sent");
                            out.flush();
                            try{
                             ArrayList<ArrayList<String>> at = new ArrayList<ArrayList<String>>();



                             ObjectInputStream in  = new ObjectInputStream(socket.getInputStream());
                                Log.d("query inputstream","formed");
                             at=(ArrayList<ArrayList<String>>)in.readObject();
                             cursor.newRow().add("key",at.get(0).get(0)).add("value",at.get(1).get(0));
                             Log.d("at aaagaya",TextUtils.join(", ", at));
                             Log.d("At aaagaya",DatabaseUtils.dumpCursorToString(cursor));
                             latch.countDown();
                            }catch (Exception e ){
                                e.printStackTrace();
                            }



                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    });
                t.start();
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("thist is it ","uoui");
                Cursor o = cursor;
                return o;
        }


        if(selection.equals("@") || selection.equals("*")){
            Cursor cu = db.rawQuery("select * from " + TABLE_NAME, null);
            Log.d("query","at the reate"+ DatabaseUtils.dumpCursorToString(cu) );
            return  cu;
        }

        Log.d("espn",selection);
        // Log.d("holoa", getTableAsString(db,TABLE_NAME));
        Cursor cu = db.query(TABLE_NAME,pro ,"key = ?" ,Args ,null,null,null);
        String[] columnNames = cu.getColumnNames();

        Log.d("query", columnNames[0]);
        return cu;
    }

    private  static class msgdatabase extends SQLiteOpenHelper {

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



    private class ServerTask extends  AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... serverSockets) {

            ServerSocket serverSocket = serverSockets[0];
            //serverSocket.setReuseAddress(true);

            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
            //Socket [] socks = new sock
            while (true) {
                try {
                    Socket sc = serverSocket.accept();
                    Log.e("server", "conncted to server ");
                    ObjectInputStream inputStream = new ObjectInputStream(sc.getInputStream());
                    String[] ObtainedMsgAraay = (String[]) inputStream.readObject();
                    BigInteger x =new BigInteger("0");
                    try{ x = new BigInteger(ObtainedMsgAraay[0]);}catch (Exception e) {e.printStackTrace();}


                    if(ObtainedMsgAraay[5].equals("join")){

                        Log.d("recv alive from",ObtainedMsgAraay[3]);
                        is_alive.put(x,true);
                        Log.d("keezs","   ");
                        Log.d("is_alive",ObtainedMsgAraay[3]);
                        alive_count++;
                        Log.d("alivecoutn",""+alive_count);

                    }

                    else if(ObtainedMsgAraay[5].equals("alive_table")){
                        Log.d("this is alivetable","sdf");
                        alive_count = Integer.parseInt(ObtainedMsgAraay[6]);
                        for (int i = 0 ; i<5 ; i++){
                            if(ObtainedMsgAraay[i]!= null){
                                is_alive.put(Hash(ObtainedMsgAraay[i]),true);
                                Log.d("is it","fsdf");
                                alive_ports.add(ObtainedMsgAraay[i]);
                            }
                        }
                        print_hash(is_alive);
                       //inputStream.close();

                    }


                    else if(ObtainedMsgAraay[5].equals("insert")){
                            Log.d("recived insert msg",ObtainedMsgAraay[0]);
                            ContentValues values = new ContentValues();
                            values.put("key", ObtainedMsgAraay[0] );
                            values.put("value", ObtainedMsgAraay[1]);
                            Log.d("value of obt",ObtainedMsgAraay[1]);

                            try{
                                mydb.insertWithOnConflict(TABLE_NAME, null, values,SQLiteDatabase.CONFLICT_REPLACE);
                                Log.d("table ",getTableAsString(mydb,TABLE_NAME)) ;

                            }catch (Exception e){
                                Log.d("insert","error with insert command");
                                e.printStackTrace();
                            }

                        }
                    else if(ObtainedMsgAraay[5].equals("query")){
                            Log.d("query","queried");
                            if(!ObtainedMsgAraay[0].equals("*"))
                            {
                                msgdatabase ms = new msgdatabase(getContext());
                                SQLiteDatabase db = ms.getReadableDatabase();
                                String [] pro ={"key","value"};

                                String[] Args = {ObtainedMsgAraay[0]};
                                Cursor cu = db.query(TABLE_NAME,pro ,"key = ?" ,Args ,null,null,null);
                                String[] columnNames = cu.getColumnNames();
                                Log.d("query ser", DatabaseUtils.dumpCursorToString(cu));
                                ArrayList<ArrayList<String>> at = new ArrayList<ArrayList<String>>();
                                ObjectOutputStream ou = new ObjectOutputStream(sc.getOutputStream());
                                ArrayList<String> A_val = new ArrayList<String>();
                                ArrayList<String> A_key = new ArrayList<String>();

                                cu.moveToFirst();
                                while(!cu.isAfterLast()) {
                                    A_val.add(cu.getString(cu.getColumnIndex("value")));
                                    A_key.add(cu.getString(cu.getColumnIndex("key")));//add the item
                                    cu.moveToNext();
                                }
                                at.add(A_key);
                                at.add(A_val);
                                Log.d("sending from qser",Arrays.toString(ObtainedMsgAraay));
                                Log.d("sendiign from qser", TextUtils.join(", ", at));
                                ou.writeObject(at);
                                ou.flush();

                            }else {
                                msgdatabase ms = new msgdatabase(getContext());
                                SQLiteDatabase db = ms.getReadableDatabase();
                                String [] pro ={"key","value"};

                                String[] Args = {ObtainedMsgAraay[0]};
                                Cursor cu = db.rawQuery("select * from " + TABLE_NAME, null);
                                String[] columnNames = cu.getColumnNames();
                                Log.d("query ser", DatabaseUtils.dumpCursorToString(cu));
                                ArrayList<ArrayList<String>> at = new ArrayList<ArrayList<String>>();
                                ObjectOutputStream ou = new ObjectOutputStream(sc.getOutputStream());
                                ArrayList<String> A_val = new ArrayList<String>();
                                ArrayList<String> A_key = new ArrayList<String>();

                                cu.moveToFirst();
                                while(!cu.isAfterLast()) {
                                    A_val.add(cu.getString(cu.getColumnIndex("value")));
                                    A_key.add(cu.getString(cu.getColumnIndex("key")));//add the item
                                    cu.moveToNext();
                                }
                                at.add(A_key);
                                at.add(A_val);
                                Log.d("sending from x",Arrays.toString(ObtainedMsgAraay));
                                Log.d("sendiign from qser", TextUtils.join(", ", at));
                                ou.writeObject(at);
                                ou.flush();
                            }

                    }


                    else{}

                    //sc.close();
                    Log.d("readobject",Arrays.toString(ObtainedMsgAraay));
                } catch (Exception e ){
                    e.printStackTrace();
                }



        }
    }

    }

    private class ClientTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            try {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt("11108"));
                TelephonyManager tel = (TelephonyManager)getContext().getSystemService(Context.TELEPHONY_SERVICE);
                final String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
                final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
                String arr [] = {""+my_id,""+my_succ,""+my_pred,portStr,"sf",strings[0]};
                ObjectOutputStream out1;
                String array[] = new String [7];

                if(strings[0].equals("alive_table"))
                {
                    a++;
                    Log.d("ais",""+a );
                    if(a>1){
                        return null;
                    }
                    int i = 0 ;
                    Log.d("hew","jer");
                    for (BigInteger name: is_alive.keySet()){
                        array[i] = revhash.get(name);
                        i++;
                    }
                    array[5]="alive_table";
                    array[6]= Integer.toString(alive_count);
                    Log.d("this is alive table",Arrays.toString(array));
                    for(int j= 0 ;j<5 ;j++)
                    {
                        Log.d("hew","jerloop");
                        try
                        {
                            Socket soc = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(ports[j]));
                            Log.d("sent live msg","to"+ports[j]);
                            out1 = new ObjectOutputStream(soc.getOutputStream());
                            out1.writeObject(array);
                            out1.flush();

                        }
                         catch (Exception e){
                            e.printStackTrace();
                         }


                    }

                    return null;

                }

                if(strings[0].equals("insert"))
                {
                    String to_insert_port = strings[1];

                    Socket so = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(to_insert_port));
                    Log.d("insertconn","formed");
                    ObjectOutputStream out2 = new ObjectOutputStream(socket.getOutputStream());
                    String [] arra =  {strings[2],strings[3],"bh",portStr,"sf","insert"};
                    out2.writeObject(arra);
                    Log.d("insertconn","sent");
                    out2.flush();
                    //out2.close();
                    Log.d("insertconn","closed");
                }




                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(arr);
                out.flush();
                //out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

}
