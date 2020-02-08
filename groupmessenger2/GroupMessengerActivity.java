package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.UUID;

import static java.lang.Math.max;


/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */

public class GroupMessengerActivity extends Activity {
    Button button;
    EditText ed;
    String p1 = "11108";
    String p2 = "11112";
    String p3 = "11116";
    String p4 = "11120";
    String p5 = "11124";
    double proposed_sequence = 0;
    double agreed_sequence = 0 ;
    PriorityQueue<messageComparator> pq;

    int [] Fifo_array = {0,0,0,0,0};

    int seq =-1;
    ContentResolver mContentResolver;
    Uri mUri;
    GroupMessengerProvider gp;
    Hashtable<String, Integer> hash_table =  new Hashtable<String,Integer>();
    Hashtable<String,String> msg_id_mapper = new Hashtable<String,String>();
    Queue<String> delivery_queue = new LinkedList<String>();

   // PriorityQueue<String> poq = new PriorityQueue<String>();

    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);
        hash_table.put(p1,1);
        hash_table.put(p2,2);
        hash_table.put(p3,3);
        hash_table.put(p4,4);
        hash_table.put(p5,5);
        pq = new PriorityQueue( );

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        final String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));


        //OnPTestClickListener on = new OnPTestClickListener(null,getContentResolver());
        mUri=buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
        gp = new GroupMessengerProvider();




        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */


        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));


        try {
            ServerSocket serverSocket = new ServerSocket(10000);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.d("server", "cannot be created");
        }




        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         *
         */

        button = (Button) findViewById(R.id.button4);
        ed = (EditText) findViewById(R.id.editText1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String msg = ed.getText().toString() + "\n";
                ed.setText(""); // This is one way to reset the input box.
                TextView localTextView = (TextView) findViewById(R.id.local_text_display);
                localTextView.append("\t" + msg + portStr); // This is one way to display a string.
                Log.v("sdf", "sdfsdf");
                String uniqueID = UUID.randomUUID().toString();
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,msg,p1,p2,p3,p4,p5,uniqueID,myPort);





            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }



    /*priority queue implementation reference  https://www.callicoder.com/java-priority-queue/ */

    class messageComparator implements Comparable<messageComparator>{

        String id ;
        double prop;
        boolean status ;

        messageComparator(String id , double prop){
            this.id =id;
            this.prop = prop;

        }

        messageComparator(String id , double prop,boolean status){
            this.id =id;
            this.prop = prop;
            this.status =status;
        }

        @Override
        public int compareTo(messageComparator another) {
            if(prop<another.prop)
                return -1 ;
            if(prop > another.prop)
                return 1 ;

            return 0;
        }

        @Override
        public String toString() {
            return ""+id+"-"+prop;
        }
    }


    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            Log.d("socke",Integer.toString(sockets.length));
            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
            //Socket [] socks = new sock
            while (true) {
                try {
                    Socket sc = serverSocket.accept();
                    Log.e("server", "conncted to server ");
                    try {

                        /*recieve <m,i>*/
                        ObjectInputStream inputStream = new ObjectInputStream(sc.getInputStream());
                        String[] ObtainedMsgAraay = (String[]) inputStream.readObject();
                        Log.d("readobject",Arrays.toString(ObtainedMsgAraay));
                        msg_id_mapper.put(ObtainedMsgAraay[1],ObtainedMsgAraay[0]);
                        int seq =Integer.parseInt(ObtainedMsgAraay[3]);
                        int remote_id = hash_table.get(ObtainedMsgAraay[4])-1;
                        Log.d("hello","remote_id = "+remote_id+"  seq = "+seq);



                        // put <m,i> into priority queue

                        proposed_sequence = max(agreed_sequence,proposed_sequence) +1 ;
                        proposed_sequence = Math.floor(proposed_sequence);
                        proposed_sequence = proposed_sequence +   0.1*Double.parseDouble(ObtainedMsgAraay[2]);
                        messageComparator cmp = new messageComparator(ObtainedMsgAraay[1],proposed_sequence);
                        pq.add(cmp);


                        Iterator it = pq.iterator();
                        Object[] arr = pq.toArray();
                        Log.d("priority que",Arrays.toString(arr));


                        //send proposed sequence number
                        try{
                            ObjectOutputStream out = new ObjectOutputStream(sc.getOutputStream());
                            out.writeObject(Double.toString(proposed_sequence));
                            out.flush();
                        }catch (Exception e ){
                            e.printStackTrace();
                        }

                        //receive agreed sequence number
                        try {

                            ObjectInputStream inputStream2 = new ObjectInputStream(sc.getInputStream());
                            Double a = Double.valueOf(inputStream.readObject().toString());
                            agreed_sequence = max(a,agreed_sequence);

                        }catch (Exception e ){
                            e.printStackTrace();
                        }

                        Log.d("agreed",Double.toString(agreed_sequence));
                        Log.d("agreed",ObtainedMsgAraay[0]+ObtainedMsgAraay[1]);
                        Log.d("agserver",""+agreed_sequence);

                        pq.remove(cmp);

                        pq.add(new messageComparator(ObtainedMsgAraay[1],max(agreed_sequence,proposed_sequence),true));


                        //pushing msg from holdback to delivery queue

                        if(pq.peek().status)
                        {
                            try{
                                String iod = pq.poll().id;
                                String msg_after_total = msg_id_mapper.get(iod);
                                Log.d("hola", "" + msg_after_total);
                                Log.d("hola2", "" + iod);
                                delivery_queue.add(msg_after_total);
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }


                      }
                        Iterator iterator = delivery_queue.iterator();

                      while(iterator.hasNext()){
                          String msg = (String)delivery_queue.remove();
                          publishProgress(msg);
                      }

                    }catch (Exception e){
                        Log.e("error at","input stream server"+e.toString());
                        e.printStackTrace();
                    }

                    sc.close();

                } catch (IOException e) {
                    e.printStackTrace();



                }
            }

        }


        @Override
        protected void onProgressUpdate(String... strings) {
            String strReceived = strings[0];
            TextView remoteTextView = (TextView) findViewById(R.id.remote_text_display);
            remoteTextView.append(strReceived + "\t\n");
            TextView localTextView = (TextView) findViewById(R.id.local_text_display);
            localTextView.append("\n");
            seq ++;
            ContentValues cv = new ContentValues();
            cv.put("key",Integer.toString(seq));
            cv.put("value",strReceived);
            Log.d("zup",cv.toString());
            ContentResolver cv1 = getContentResolver();
            cv1.insert(mUri,cv);

            //mydb.insertWithOnConflict(TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);



        }
    }



    private class ClientTask extends AsyncTask<String, Void, Void> {

        //int port;



        @Override
        protected Void doInBackground(String... msgs) {

            boolean lostcon =false;
            String msgToSend = msgs[0];
            try {
                Socket[] sockets = new Socket[5];
                ObjectInputStream[] inputStreams = new ObjectInputStream[5];
               // double[] proposed = new double[5];
                ArrayList<Double> proposed = new ArrayList<Double>();

                Fifo_array[hash_table.get(msgs[7])-1] = Fifo_array[hash_table.get(msgs[7])-1] +1 ;
                String fifo_seq = Integer.toString(Fifo_array[hash_table.get(msgs[7])-1]);
                Log.d("fifo"+(hash_table.get(msgs[7])-1),Arrays.toString(Fifo_array));


                for( int i = 1 ; i <= 5 ;i++ ){

                    /* sending message to all processes with unique id */
                String remotePort = msgs[i];
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(remotePort));
                //socket.setSoTimeout(2*1000);

                sockets[i-1] = socket;
                //inputStreams[i-1] = new ObjectInputStream(socket.getInputStream());
                String [] msgarry = {msgToSend,msgs[6],Integer.toString(hash_table.get(remotePort)),fifo_seq,msgs[7]};
                 try   { ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(msgarry);
                out.flush();}
                catch(Exception e){
                     e.printStackTrace();
                }
                try {
                    ObjectInputStream input_stream = new ObjectInputStream(socket.getInputStream());
                    Double x =new Double((String) input_stream.readObject());
                    Log.d("junsu",msgs[7]);
                    proposed.add(x);//new Double((String) input_stream.readObject());
                    Log.d("esdf","wassup");

                }catch (Exception e){
                    e.printStackTrace();
                    Log.d("hello",""+e.toString());
                }

                }


                Log.d("proposed",Arrays.toString(proposed.toArray()));
                //Collections.sort(proposed);
                //Arrays.sort(proposed);
                double ag = Collections.max(proposed);


                for(int i = 0 ; i< 5 ; i++){
                    try{
                    ObjectOutputStream out = new ObjectOutputStream(sockets[i].getOutputStream());
                    out.writeObject(Double.toString(ag));
                    out.flush();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                }


            } catch (UnknownHostException e) {
                Log.e("", "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e("", "ClientTask socket IOException");
            }

            return null;

        }
    }
}

