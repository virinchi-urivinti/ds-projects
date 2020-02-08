package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AtomicFile;
import android.util.Log;

public class SimpleDynamoProvider extends ContentProvider {
	String myPort;
	String portStr;
	String p1 = "11108";String p2 = "11112";
	String p3 = "11116";String p4 = "11120";
	String p5 = "11124";
	String [] myPorts = {p1,p2,p3,p4,p5};
	String [] portStrs = {"5554","5556","5558","5560","5562"};
	BigInteger num = new BigInteger("1");
	Hashtable<String,Integer> port_id =new Hashtable<String, Integer>();
	boolean recover =false;
	int c =0;
	long heart_beat = 0 ;
	BigInteger my_id;
	Map<String ,Boolean> is_alive=  Collections.synchronizedMap(new HashMap<String, Boolean>());
	Map<BigInteger ,Boolean> is_alive_id= Collections.synchronizedMap(new HashMap<BigInteger, Boolean>());
	Map<BigInteger ,Boolean> is_alive_id2= Collections.synchronizedMap(new HashMap<BigInteger, Boolean>());

	HashMap<BigInteger,String> revhash = new HashMap<BigInteger, String>();
	boolean del = false;
	ArrayList<HashMap<String,String>> rep_data = new ArrayList<HashMap<String, String>>();
	Map<String,ArrayList<Long> > node_info =  new ConcurrentHashMap<String, ArrayList<Long>>();
	node mynode = new node();
	SQLiteDatabase mydb ;
	HashMap<String,String> recovery_data ;
	CountDownLatch lat = new CountDownLatch(4);
	CountDownLatch lat2 = new CountDownLatch(2);
	Hashtable<String,String> rd1 = new Hashtable<String, String>();
	Hashtable<String,String> rd2 = new Hashtable<String, String>();
	Hashtable<String,String> rd3 = new Hashtable<String, String>();
	Hashtable<String,String> rd4 = new Hashtable<String, String>();
	Hashtable<String,String> rd5 = new Hashtable<String, String>();
    ArrayList<String> val = new ArrayList<String>();
    ArrayList<String> ke = new ArrayList<String>();

    /*
	MatrixCursor rd2;
	MatrixCursor rd3;
	MatrixCursor rd4;MatrixCursor rd5;*/
	MatrixCursor recover_data ;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		Log.d("delete","enterd");
		mydb.execSQL("delete from " + "dynamo");
		mydb.delete("dynamo", null, null);

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
                    Log.d("delforloop","inside");

                    try {
                        socketobj[i] = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(myPorts[i]));
                        Log.d("*socket", "created");

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    //String [] arr = {"*","1","2","3","4","query"};
                    ArrayList<String> arr = new ArrayList<String>();
                    arr.add("delete");
                    try   { ObjectOutputStream out = new ObjectOutputStream(socketobj[i].getOutputStream());
                        Log.d("deloutputstream","created");
                        out.writeObject(arr);
                        Log.d("delsent","created");
                        out.flush();}
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });thread.start();
        del =true;
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}




	public void initialize(){

		port_id.put("11108",0);port_id.put("11112",1);port_id.put("11116",2);
		port_id.put("11120",3);port_id.put("11124",4);
		rep_data.add(null);rep_data.add(null);rep_data.add(null);rep_data.add(null);rep_data.add(null);
        /*
		rd1  = new MatrixCursor(new String[] {"key", "value"});
		rd2 = new MatrixCursor(new String[]{"key","value"});
		rd3 = new MatrixCursor(new String[]{"key","value"});
		rd4 = new MatrixCursor(new String[]{"key","value"});
		rd5 =new MatrixCursor(new String[]{"key","value"});
        */

		revhash.put(mynode.Hash("5554"), "5554");
		revhash.put(mynode.Hash("5556"), "5556");
		revhash.put(mynode.Hash("5558"), "5558");
		revhash.put(mynode.Hash("5560"), "5560");
		revhash.put(mynode.Hash("5562"), "5562");
		for(int i = 0;i<5;i++){
			is_alive_id2.put((mynode.Hash(portStrs[i])),true);
		}
		//rd1.add(new ArrayList<String>()) ;rd1.add(new ArrayList<String>());rd2.add(new ArrayList<String>()) ;
		//rd2.add(new ArrayList<String>());rd3.add(new ArrayList<String>()) ;rd3.add(new ArrayList<String>());
		//rd4.add(new ArrayList<String>()) ;rd4.add(new ArrayList<String>());rd5.add(new ArrayList<String>());
		//rd5.add(new ArrayList<String>());
		//is_alive_id.put(mynode.Hash(arr.get(1)),true);
		mynode.print_rev_hash(revhash);

	}


	public void enter_repdata(String portno,ContentValues values){
		Log.d("enetering rep data","replication");


		if(portno.equals("11108")){
			synchronized (rd1){
			rd1.put((String)values.get("key"),(String)values.get("value"));
			Log.d("rep rd 1",""+rd1);}
		}
		if(portno.equals("11112")){
			synchronized (rd2){
			//rd2.newRow().add("key",(String)values.get("key")).add("value",(String)values.get("value"));
			//Log.d("repdata",""+DatabaseUtils.dumpCursorToString(rd2));
			rd2.put((String)values.get("key"),(String)values.get("value"));
			/*ArrayList<String> s;
			s=rd2.get(0);
			s.add((String)values.get("key"));
			rd2.set(0,s);
			s= rd2.get(1);
			s.add((String)values.get("value"));
			rd2.set(1,s);;*/
			Log.d("rep rd 2",""+rd2);}
		}
		if(portno.equals("11116")){
			synchronized (rd1){
			Log.d("rep rd 3",""+rd3);
			rd3.put((String)values.get("key"),(String)values.get("value"));}
		}
		if(portno.equals("11120")){
			synchronized (rd4){
			Log.d("rep rd 4",""+rd4);
			rd4.put((String)values.get("key"),(String)values.get("value"));}
        }
			//rd4.newRow().add("key",(String)values.get("key")).add("value",(String)values.get("value"));
			//Log.d("repdata",""+DatabaseUtils.dumpCursorToString(rd4));}
		if(portno.equals("11124")){
			synchronized (rd5){
			Log.d("rep rd 5",""+rd5);
			rd5.put((String)values.get("key"),(String)values.get("value"));}

		}
			//rd5.newRow().add("key",(String)values.get("key")).add("value",(String)values.get("value"));
			//Log.d("repdata",""+DatabaseUtils.dumpCursorToString(rd5));}
	}

	public Hashtable<String, String> return_repdata(String portno){
		if(portno.equals("11108")){
			return  rd1;
		}
		if(portno.equals("11112")){
			return rd2;
		}
		if(portno.equals("11116")){
			return rd3;
		}
		if(portno.equals("11120")){
			return rd4;
		}
		if(portno.equals("11124")){
			return rd5;
		}
		return  null;
	}


	BigInteger succ(BigInteger id){
		BigInteger b1 = new BigInteger("2");
		BigInteger max_val =b1.pow(160);
		if(is_alive_id2.containsKey(id) && is_alive_id2.get(id)){
			return id;
		}else{
			BigInteger min1 = max_val;
			BigInteger min2 = max_val;
			for(BigInteger name : is_alive_id2.keySet()){
				if(is_alive_id2.get(name)){
					if(id.compareTo(name)>0){
						if(min1.compareTo(name)>0){
							//Log.d("yo","yo");
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
				return min2;
			}
			return min1;
		}
	}


	public String store (Uri uri, final ContentValues values){
		final ArrayList<String> ports_to_insert = return_3_succ_ports(values);

		final CountDownLatch latch = new CountDownLatch(1);

			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					Log.d("repsize ",""+TextUtils.join(",", ports_to_insert));
					for(String to_insert_port:ports_to_insert){
						try {
							Log.d("current_port",""+to_insert_port);
							Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
								Integer.parseInt(to_insert_port));
							socket.setSoTimeout(100);
							Log.d("insertconn",""+to_insert_port);
							String l=Integer.toString(Integer.parseInt(to_insert_port)/2);
							try{
							ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
							ArrayList<String> message =  new ArrayList<String>();
							message.add("insert");
							message.add((String)values.get("key"));
							message.add((String)values.get("value"));
							message.add(myPort);
							out.writeObject(message);
							//enter_repdata(to_insert_port,values);

							Log.d("insertconn"+(String)values.get("key"),"sent");
							out.flush();}catch(Exception e){
								Log.d("failure detected"+(String)values.get("key"),"haa") ;
								//rep_data.get(port_id.get(to_insert_port)).put((String)values.get("key"),(String)values.get("value"));
							//rep_data.set(port_id.get(to_insert_port),);
								enter_repdata(to_insert_port,values);
							}
							try{
							    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
							    String x = (String)in.readObject();
							    Log.d("hog_msg",x);
							    if(!x.equals("hogaya")){
                                    enter_repdata(to_insert_port,values);
                                }

                            }catch(Exception e){
							    e.printStackTrace();
                                Log.d("failure detected"+(String)values.get("key"),"haa") ;
                                //rep_data.get(port_id.get(to_insert_port)).put((String)values.get("key"),(String)values.get("value"));
                                enter_repdata(to_insert_port,values);

                            }

							Log.d("insertconn","closed");
							Log.d("current",""+to_insert_port);

					} catch (IOException e) {
						e.printStackTrace();
							Log.d("failure detected"+(String)values.get("key"),"haa") ;
							//rep_data.get(port_id.get(to_insert_port)).put((String)values.get("key"),(String)values.get("value"));
							enter_repdata(to_insert_port,values);

					}
					latch.countDown();
				}}
			});
			thread.setPriority(4);
			thread.start();

	return null;
	}


	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		String x =store(uri,values);


		return null;
	}



	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
        initialize();
		//recover= req_fastrepdata();
		//Log.d("succport", mynode.succ_port(myPort));

		msgdatabase msg = new msgdatabase(getContext());
		mydb = msg.getWritableDatabase();


		/*######################   FIND PORT NUMBER  #######################################*/
		TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		myPort = String.valueOf((Integer.parseInt(portStr) * 2));
       recover =recover_attherate();
		try {
			ServerSocket soc = new ServerSocket(10000);
			new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, soc);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//start_gossip();

		//Log.d("here","ho");




		//Log.d("data recoverd","haa");




		return false;
	}

	public  String return_port(String key){

		BigInteger b = mynode.Hash(key);
		b = succ(b);
		String x = revhash.get(b);

		return String.valueOf((Integer.parseInt(x) * 2));


	}

	@Override
	public Cursor query(Uri uri, String[] projection, final String selection, String[] selectionArgs,
						String sortOrder) {
		int x = is_alive.size();


		//Log.d("query called","from query");
		//Log.d("print alive coiunt",""+alive_count);

		msgdatabase ms = new msgdatabase(getContext());

		SQLiteDatabase db = ms.getReadableDatabase();
		String [] pro ={"key","value"};
		Cursor cur =null;
		String[] Args = {selection};
		final MatrixCursor cursor = new MatrixCursor(new String[] {"key","value"});
		//Log.d("asdf",Hash(selection));

		if(selection.equals("@")){
		    Cursor l = null;
		   // if(del == true){return l; }

		    if(!recover){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			boolean m =recover_attherate();
		   // String[] Arrr= {"*"}

            Cursor cu = db.query("dynamo",pro ,null , null,"key","MAX(num)",null);
			//Cursor cu = db.rawQuery("select * from " + "dynamo", null);
		    Log.d("this db",DatabaseUtils.dumpCursorToString(cu));
            ;

			return cu;
		}

//################################## STAR QUERY ###################################################

		if(selection.equals("*") ){


			Log.d("*query","called");
			Log.d("*del","called"+del);
			//if(del==true){
				//Cursor l =null;
				//return l;
			//}
			//Log.d("*1","called"+del);
			final  MatrixCursor matrixCursor = new MatrixCursor(new String[] {"key", "value"});
			final CountDownLatch latch = new CountDownLatch(1);
			//Log.d("*12","called"+del);
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

							try {
								socketobj[i] = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
										Integer.parseInt(myPorts[i]));
								Log.d("*socket", "created");

							}catch (Exception e){
								e.printStackTrace();
							}
								//String [] arr = {"*","1","2","3","4","query"};
								ArrayList<String> arr = new ArrayList<String>();
								arr.add("query");arr.add("*");
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
									Log.d("cursor from",portStrs[i]);
									Log.d("recieved arraylist",TextUtils.join(",", at));

									// matrixCursor.newRow().add("key",at.get(0).get(0)).add("value",at.get(1).get(0));

									for (int j = 0; j < at.get(0).size(); j++) {
										matrixCursor.newRow().add("key",at.get(0).get(j)).add("value",at.get(1).get(j));

									}}catch (Exception e ){
									e.printStackTrace();
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
			//Log.d("matrixcursordumping",DatabaseUtils.dumpCursorToString(matrixCursor));

			return matrixCursor;

		}


		/*if(selection.equals("*") && x ==2){
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

					for (String porte  : is_alive.keySet())
					{
						Log.d("*forloop","inside");
						if(is_alive.containsKey(mynode.Hash(porte))){
							Log.d("*isalive","inside");
							try
							{
								socketobj[i] = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
										2*Integer.parseInt(porte));
								Log.d("*socket","created");


								ArrayList<String> arr = new ArrayList<String>();
								arr.add("query");arr.add("*");
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
									Log.d("cursor from",portStrs[i]);
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

		}*/




		if(!selection.equals("@") && !selection.equals("*"))
		{
			final MatrixCursor mcc =new MatrixCursor(new String[] {"key", "value","num"});;
			 MatrixCursor ml =new MatrixCursor(new String[] {"key", "value"});;


			final CountDownLatch latch = new CountDownLatch(1);
			final ArrayList<String> ports_to_search = return_ports_for_query(selection);
			final String xo;
			final ArrayList<String> key  =new ArrayList<String>();
			final ArrayList<Integer> greatest= new ArrayList<Integer>();
			 final Hashtable<String,Integer> majority = new Hashtable<String, Integer>();
			//final int greatest = 0 ;
			/*if(return_port(selection).equals(myPort)){
				return db.query("dynamo",pro ,"key = ?" ,Args ,null,null,null);
			}*/

			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					MatrixCursor mb;
					try {
						//String xo = new String();
						String x1 = new String();
						MatrixCursor mc=new MatrixCursor(new String[] {"key", "value"});
						Socket[] socket = new Socket[3];
						ObjectOutputStream[] objOutput = new ObjectOutputStream[3];
						ObjectInputStream[] objInput = new ObjectInputStream[3];
						int h = 0 ;
						for(String port : ports_to_search)
						{

							//Log.d("searchport", port);
							try {
								Log.d("querying" + port, selection);
								 socket[h] = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
										Integer.parseInt(port));
								ObjectOutputStream out = new ObjectOutputStream(socket[h].getOutputStream());
								Log.d("query outstream" + selection, "formed");
								ArrayList<String> arr = new ArrayList<String>();
								arr.add("query");
								arr.add(selection);


							try {
								out.writeObject(arr);
								Log.d("query req"+selection, "sent");
								out.flush();
							} catch (Exception e) {
								e.printStackTrace();
							}}catch (Exception e){e.printStackTrace();}
							try {
								ArrayList<ArrayList<String>> at = new ArrayList<ArrayList<String>>();


								ObjectInputStream in = new ObjectInputStream(socket[h].getInputStream());
								Log.d("query inputstream"+selection, "formed");
								at = (ArrayList<ArrayList<String>>) in.readObject();
								//value.add(at.get(1).get(0));
								//key.add(at.get(0).get(0));
								try{
								if(at.get(2).get(0)!= null){
									greatest.add(Integer.parseInt(at.get(2).get(0)));
								}}catch (Exception e){
									e.printStackTrace();
								}

								mcc.newRow().add("key", at.get(0).get(0)).
										add("value",at.get(1).get(0)).
										add("num",at.get(2).get(0));
								Log.d("at.get(2).get(0)",""+at.get(2).get(0));
								if(majority.contains(at.get(1).get(0))){
									int y = majority.get(at.get(1).get(0));
									y++;
									majority.put(at.get(1).get(0),y);
								}else{
									majority.put(at.get(1).get(0),1);
								}
								//Log.d("at aaagaya", TextUtils.join(", ", at));
								//Log.d("At aaagaya", DatabaseUtils.dumpCursorToString(mc));
								//mc.newRow().add("key", x1).add("value", xo);
								//if(!value.isEmpty()){break;}

							} catch (Exception e) {
								e.printStackTrace();
							}
							h++;

						}//new Object[]{, cursor.getString(1)}
						//Log.d("dumping_mc",DatabaseUtils.dumpCursorToString(mc));

						 mb=new MatrixCursor(new String[] {"key", "value"});


					} catch (Exception e) {
						e.printStackTrace();
					}
					latch.countDown();
				}
			});
			t.start();
			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//Log.d("thist is it ","uoui");
			//mcc.newRow().add("key", key.get(0)).add("value", value.get(0));
			Log.d("majority",""+majority);
			if(majority.size()==2){
                for (mcc.moveToFirst(); !mcc.isAfterLast(); mcc.moveToNext()){
                    try{if(Integer.parseInt(mcc.getString(mcc.getColumnIndex("num")))==Collections.max(greatest)){
                        ml.newRow().add("key", mcc.getString(mcc.getColumnIndex("key"))).
                                add("value",mcc.getString(mcc.getColumnIndex("value")));
                        return ml;
                    }}catch (Exception e){e.printStackTrace();}
                }

            }


			Map.Entry<String, Integer> maxEntry = null;
			for (Map.Entry<String,Integer> entry : majority.entrySet()) {
				if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
					maxEntry = entry;
				}
			}
			try {
				String maxKey = maxEntry.getKey();
			}catch (Exception e){
				e.printStackTrace();
			}

			Log.d("numarray1",greatest.toString());
			greatest.removeAll(Collections.singleton(null));
			Log.d("numarray2",greatest.toString());
			Log.d("obtained cusor",DatabaseUtils.dumpCursorToString(mcc));

			 ml =new MatrixCursor(new String[] {"key", "value"});;
			 ml.newRow().add("key",selection).add("value",maxEntry);
			 return ml;

			/*for (mcc.moveToFirst(); !mcc.isAfterLast(); mcc.moveToNext()){
				try{if(Integer.parseInt(mcc.getString(mcc.getColumnIndex("num")))==Collections.max(greatest)){
					ml.newRow().add("key", mcc.getString(mcc.getColumnIndex("key"))).
							add("value",mcc.getString(mcc.getColumnIndex("value")));
					return ml;
				}}catch (Exception e){e.printStackTrace();}
			}*/

			//Log.d("dumping_mcc",DatabaseUtils.dumpCursorToString(mcc));
			//Cursor o = mcc;
			//return o;
		}


		if(selection.equals("@") || selection.equals("*")){
			Cursor cu = db.rawQuery("select * from " + "dynamo", null);
			Log.d("query","at the reate"+ DatabaseUtils.dumpCursorToString(cu) );
			return  cu;
		}

		//Log.d("espn",selection);
		// Log.d("holoa", getTableAsString(db,TABLE_NAME));
		Cursor cu = db.query("dynamo",pro ,"key = ?" ,Args ,null,null,null);
		String[] columnNames = cu.getColumnNames();

		//Log.d("query", columnNames[0]);
		return cu;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
					  String[] selectionArgs) {
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


	public ArrayList<String> return_ports_for_query(String bkey){
		ArrayList<String> arrayList =new ArrayList<String>();
		final BigInteger key = mynode.Hash(bkey);
		BigInteger To_be_inserted_at_1 = succ(key);
		BigInteger To_be_inserted_at_2 = succ(To_be_inserted_at_1.add(num));
		BigInteger To_be_inserted_at_3 = succ(To_be_inserted_at_2.add(num));
		arrayList.add(Integer.toString(2*Integer.parseInt(revhash.get(To_be_inserted_at_1))));
		arrayList.add(Integer.toString(2*Integer.parseInt(revhash.get(To_be_inserted_at_2))));
		arrayList.add(Integer.toString(2*Integer.parseInt(revhash.get(To_be_inserted_at_3))));
		return  arrayList;
	}

	private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

		@Override
		protected Void doInBackground(ServerSocket... serverSockets) {

			ServerSocket serverSocket = serverSockets[0];


			while (true) {
				try {
					Socket sc = serverSocket.accept();

					Log.e("server", "conncted to server ");

					ObjectInputStream inputStream = new ObjectInputStream(sc.getInputStream());
					ArrayList<String> arr = (ArrayList<String>) inputStream.readObject();

					//update_node_info(arr);
					//Log.d("c",""+c++);
					//Log.d("waiting","for recovey");

                    if(arr.get(0).equals("delete")){
                        Log.d("delelting","haa");
                        mydb.execSQL("delete from " + "dynamo");

                    }


					if(arr.get(0).equals("rep_data@"))
					{


						Log.d("at_@rep","haa");
						Cursor cu = mydb.query("dynamo",new String[]{"key","value","num"} ,null ,null ,"key","MAX(num)",null);

						//Cursor cu = mydb.rawQuery("select * from " + "dynamo", null);
						Hashtable<String,String> x =new Hashtable<String, String>();
						//ArrayList<String> ports_3 = new ArrayList<String>();
						String p = arr.get(2);
						if (cu.moveToFirst()){
							while(!cu.isAfterLast()){
								String key = cu.getString(cu.getColumnIndex("key"));
								String value = cu.getString(cu.getColumnIndex("value"));
								//ports_3 =return_ports_for_query(key);

								if(return_port(key).equals(mynode.pred_port(myPort)) || return_port(key).equals(p) || return_port(key).equals(mynode.pred_port(p))){
									x.put(key,value);
									//Log.d("shouldsendto"+p,key);
								}



								cu.moveToNext();
							}
						}


						try{
							ObjectOutputStream ou = new ObjectOutputStream(sc.getOutputStream());
							ou.writeObject(x);

							//Log.d("rep@ sending to"+arr.get(2),""+x);

							ou.flush();}catch (Exception e){e.printStackTrace();
						}

					}

					if(arr.get(0).equals("rep_data"))
					{
						try{
							ObjectOutputStream ou = new ObjectOutputStream(sc.getOutputStream());
							ou.writeObject(return_repdata(arr.get(2)));
							Log.d("sending rep"+arr.get(1),""+return_repdata(arr.get(2)));

							ou.flush();}catch (Exception e){e.printStackTrace();
						}
					}
					if(arr.get(0).equals("mem"))
					{
						is_alive.put(arr.get(1),true);
						is_alive_id.put(mynode.Hash(arr.get(1)),true);

					}

					if(arr.get(0).equals("insert"))
					{

						Log.d("recived insert msg",arr.get(0));
						ContentValues values = new ContentValues();
						String [] pro ={"key","value"};

						String[] Args = {arr.get(1)};
						//Cursor cu = mydb.rawQuery("select * from " + "dynamo", null);
						//Log.d("queried_while_ins",DatabaseUtils.dumpCursorToString(cu));
						values.put("key", arr.get(1));
						values.put("value", arr.get(2));
						values.put("num",return_latest_num(arr.get(1),arr.get(2)));
					//	Log.d("key_"+arr.get(1),""+return_latest_num(arr.get(1),arr.get(2)));


						try{
							mydb.insertWithOnConflict("dynamo", null, values,SQLiteDatabase.CONFLICT_REPLACE);
							//Log.d("table ",getTableAsString(mydb,TABLE_NAME));
							//Log.d("msg"+arr.get(1)+" ::"+return_latest_num(arr.get(1))+1,"inserted");
							Log.d("after_inserting_",DatabaseUtils.dumpCursorToString(mydb.query("dynamo",new String[]{"key","value","num"} ,"key = ?" ,Args ,null,null,null)));
						}catch (Exception e){
							Log.d("insect","error with insert command");
							e.printStackTrace();
						}
						try{
						ObjectOutputStream ou = new ObjectOutputStream(sc.getOutputStream());
						ou.writeObject("hogaya");
						ou.flush();}catch (Exception e){
							e.printStackTrace();
						}
					}

					if(arr.get(0).equals("query"))
					{

						if(!recover){
							Thread.sleep(5000);
						}
						//Log.d("query waiting for","recoveryedjo");
						if(!arr.get(1).equals("*"))
						{
							msgdatabase ms = new msgdatabase(getContext());
							SQLiteDatabase db = ms.getReadableDatabase();
							String [] pro ={"key","value","num"};

							String[] Args = {arr.get(1)};
							Cursor cu = db.query("dynamo",pro ,"key = ?" ,Args ,"key","MAX(num)",null);
							//Cursor cu = db.query("dynamo",pro ,"key = ?" ,Args ,null,null,null);

							String[] columnNames = cu.getColumnNames();
							Log.d("query_ser1", DatabaseUtils.dumpCursorToString(cu));
							Log.d("query_ser2",DatabaseUtils.dumpCursorToString(db.query("dynamo",pro ,"key = ?" ,Args ,null,null,null)));
							ArrayList<ArrayList<String>> at = new ArrayList<ArrayList<String>>();
							ObjectOutputStream ou = new ObjectOutputStream(sc.getOutputStream());
							ArrayList<String> A_val = new ArrayList<String>();
							ArrayList<String> A_key = new ArrayList<String>();
							ArrayList<String>  num = new ArrayList<String>();

							cu.moveToFirst();
							while(!cu.isAfterLast()) {
								A_val.add(cu.getString(cu.getColumnIndex("value")));
								A_key.add(cu.getString(cu.getColumnIndex("key")));//add the item
								num.add(cu.getString(cu.getColumnIndex("num")));
								cu.moveToNext();
							}
							at.add(A_key);
							at.add(A_val);
							at.add(num);
							//Log.d("sending from qser",Arrays.toString(ObtainedMsgAraay));
							//Log.d("sendiign from qser", TextUtils.join(", ", at));
							ou.writeObject(at);
							ou.flush();

						}else {
							msgdatabase ms = new msgdatabase(getContext());
							SQLiteDatabase db = ms.getReadableDatabase();
							String [] pro ={"key","value"};

							String[] Args = {arr.get(1)};
							Cursor cu = db.rawQuery("select * from " + "dynamo", null);
							String[] columnNames = cu.getColumnNames();
						//	Log.d("query ser", DatabaseUtils.dumpCursorToString(cu));
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
							//Log.d("sending from x",Arrays.toString(a));
							Log.d("sendiign from qser", TextUtils.join(", ", at));
							ou.writeObject(at);
							ou.flush();
						}
					}



				}catch (SocketTimeoutException e){
					e.printStackTrace();
					Log.d("socket","gone");

				} catch (EOFException e){e.printStackTrace();}catch (Exception e) {
					e.printStackTrace();
				}



			}
		}
	}





	private class parlrep extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... strings) {


			final ArrayList<String> al = new ArrayList<String>();
			al.add("rep_data");
			al.add(""+portStr);
			al.add(""+myPort);

			try {


				Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
																Integer.parseInt(strings[0]));

					try{
					ObjectOutputStream obj = new ObjectOutputStream(socket.getOutputStream());
					obj.writeObject(al);
					obj.flush();
					}catch(Exception e){
						e.printStackTrace();
					}

				try {
					ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
					ArrayList<ArrayList<String>> arrayList =new ArrayList<ArrayList<String>>();
					Hashtable<String,String> rec =(Hashtable<String,String>)in.readObject();
					Log.d("recovery data comed",TextUtils.join(", ", arrayList));
				try{
					Iterator it = rec.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry pair = (Map.Entry)it.next();
						System.out.println(pair.getKey() + " = " + pair.getValue());
						it.remove(); // avoids a ConcurrentModificationException
						ContentValues values = new ContentValues();
						values.put("key", (String) pair.getKey());
						values.put("value", (String)pair.getValue());
						Log.d("insec-"+(String) pair.getKey(),""+(String)pair.getValue());

						try{
							mydb.insertWithOnConflict("dynamo", null, values,SQLiteDatabase.CONFLICT_REPLACE);
							Log.d("msg","inserted");
						}catch (Exception e){
							Log.d("insert","error with insert command");
							e.printStackTrace();
						}};}catch (Exception e){
							e.printStackTrace();
						}





				} catch (IOException e) {
					e.printStackTrace();} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}


			} catch (IOException e) {
				e.printStackTrace();
			}
			if(!strings[0].equals(myPort)){lat.countDown();}


		return  null;}


		}


	ArrayList<String> return_3_succ_ports(ContentValues values){
		BigInteger num = new BigInteger("1");
		ArrayList<String> arrayList = new ArrayList<String>();
		final String bkey = (String)values.get("key");
		final String value= (String)values.get("value");
		final BigInteger key = mynode.Hash(bkey);
		BigInteger To_be_inserted_at_1 = succ(key);
		BigInteger To_be_inserted_at_2 = succ(To_be_inserted_at_1.add(num));
		BigInteger To_be_inserted_at_3 = succ(To_be_inserted_at_2.add(num));
		arrayList.add(Integer.toString(2*Integer.parseInt(revhash.get(To_be_inserted_at_1))));
		arrayList.add(Integer.toString(2*Integer.parseInt(revhash.get(To_be_inserted_at_2))));
		arrayList.add(Integer.toString(2*Integer.parseInt(revhash.get(To_be_inserted_at_3))));
		return  arrayList;

	}








	public void start_gossip(){
		recovery_data= new HashMap<String, String>();
		final ArrayList<String> al = new ArrayList<String>();
		ArrayList<Long> nl = new ArrayList<Long>();
		//set_node_info(portStr,heart_beat++,System.nanoTime());
		al.add("mem");
		al.add(""+portStr);
		al.add(""+myPort);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try
				{
						Socket[] sockets = new Socket[5];
						ObjectOutputStream[] ou = new ObjectOutputStream[5];
						Random rand = new Random();

							for (int i = 0; i < 5; i++) {
								try {
									String remotePort = myPorts[i];
									sockets[i] = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
											Integer.parseInt(remotePort));
								} catch (Exception e) {
									e.printStackTrace();
								}

								try {
									ou[i] = new ObjectOutputStream(sockets[i].getOutputStream());
									ou[i].writeObject(al);
									ou[i].flush();
								} catch (Exception e) {
									e.printStackTrace();
									is_alive.remove(portStrs[i]);
									is_alive_id.remove(mynode.Hash(portStr));
								}

								/*
								try{
									ObjectInputStream in = new ObjectInputStream(sockets[i].getInputStream());
									recovery_data=(HashMap<String,String>)in.readObject();
									Log.d("read obj",""+recovery_data);
									if(recovery_data!=null){
									Iterator it = recovery_data.entrySet().iterator();
									while (it.hasNext()) {
										Map.Entry pair = (Map.Entry)it.next();
										ContentValues values = new ContentValues();
										values.put("key",(String) pair.getKey());
										values.put("value",(String)pair.getValue());
										Log.d("recovery values",""+(String) pair.getKey());
										try{
											mydb.insertWithOnConflict("dynamo", null, values,SQLiteDatabase.CONFLICT_REPLACE);
											//Log.d("table ",getTableAsString(mydb,TABLE_NAME)) ;
											Log.d("msg","inserted at recovery");
										}catch (Exception e){
											Log.d("insert","recovery problem");
											e.printStackTrace();
										}
									}}
								}catch(Exception e){
									e.printStackTrace();

								}*/
							}
							//set_node_info(portStr,heart_beat++,timer.mill_sec);
							Thread.sleep(500);


			}  catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			});thread.start();

			//lat.countDown();
	}



	public boolean req_repdata(){
		final ArrayList<String> al = new ArrayList<String>();
		final Hashtable<String, String> rec = new Hashtable<String, String>();
		al.add("rep_data");
		al.add(""+portStr);
		al.add(""+myPort);
		final CountDownLatch latch = new CountDownLatch(1);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Socket[] sockets = new Socket[5];
				ObjectOutputStream[] ou = new ObjectOutputStream[5];
				for (int i = 0; i < 5; i++) {
					try {
						String remotePort = myPorts[i];
						sockets[i] = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
								Integer.parseInt(remotePort));
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						ou[i] = new ObjectOutputStream(sockets[i].getOutputStream());
						ou[i].writeObject(al);
						ou[i].flush();
					} catch (Exception e) {
						e.printStackTrace();

					}
					try {
						ObjectInputStream in = new ObjectInputStream(sockets[i].getInputStream());
                        ArrayList<ArrayList<String>> arrayList =new ArrayList<ArrayList<String>>();
                        Hashtable<String,String> rec =(Hashtable<String,String>)in.readObject();
                        Log.d("recovery data comed",TextUtils.join(", ", arrayList));
						Iterator it = rec.entrySet().iterator();
						while (it.hasNext()) {
							Map.Entry pair = (Map.Entry)it.next();
							System.out.println(pair.getKey() + " = " + pair.getValue());
							it.remove(); // avoids a ConcurrentModificationException
							ContentValues values = new ContentValues();
							values.put("key", (String) pair.getKey());
							values.put("value", (String)pair.getValue());
							//Log.d("insec-"+(String) pair.getKey(),""+(String)pair.getValue());

                            try{
                                mydb.insertWithOnConflict("dynamo", null, values,SQLiteDatabase.CONFLICT_REPLACE);
                                Log.d("msg","inserted");
                            }catch (Exception e){
                                Log.d("insert","error with insert command");
                                e.printStackTrace();
                            }};





					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}

				}
				//lat.countDown();
				latch.countDown();
			}
		});thread.setPriority(10);
		thread.start();
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}

	public boolean req_fastrepdata()
	{

		new parlrep().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,myPorts[0],myPort);
		new parlrep().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,myPorts[1],myPort);
		new parlrep().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,myPorts[2],myPort);
		new parlrep().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,myPorts[3],myPort);
		new parlrep().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,myPorts[4],myPort);


		return true ;
	}

	private class parlrep2 extends AsyncTask<String, Void, Void>{

		@Override
		protected Void doInBackground(String... strings) {




			return null;
		}
	}

	public boolean recover_attherate  (){

		try {
			Log.d("succport", mynode.succ_port(myPort));
			Log.d("myport", myPort);
			Log.d("predport", mynode.pred_port(myPort));
		}catch (Exception e){
			e.printStackTrace();
		}

		try {
			new goodquery().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,mynode.succ_port(myPort)).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}


		try {
			new goodquery().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,mynode.pred_port(myPort)).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return true;
	}

	private class goodquery extends AsyncTask<String, Void, Void>{

		@Override
		protected Void doInBackground(String... strings) {




				final ArrayList<String> al = new ArrayList<String>();
				al.add("rep_data@");
				al.add(""+portStr);
				al.add(""+myPort);

				try {


					Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
							Integer.parseInt(strings[0]));

					try{
						ObjectOutputStream obj = new ObjectOutputStream(socket.getOutputStream());
						obj.writeObject(al);
						obj.flush();
					}catch(Exception e){
						e.printStackTrace();
					}
						try {
						ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
						ArrayList<ArrayList<String>> arrayList =new ArrayList<ArrayList<String>>();
						Hashtable<String,String> rec =(Hashtable<String,String>)in.readObject();
						Log.d("recovery data comed",TextUtils.join(", ", arrayList));
						try{
							Iterator it = rec.entrySet().iterator();
							while (it.hasNext()) {
								Map.Entry pair = (Map.Entry)it.next();
								System.out.println(pair.getKey() + " = " + pair.getValue());
								it.remove(); // avoids a ConcurrentModificationException
								ContentValues values = new ContentValues();
								values.put("key", (String) pair.getKey());
								values.put("value", (String)pair.getValue());
								//int a =return_latest_num((String)pair.getKey());
								//values.put("num",a);

								//Log.d("insec-"+(String) pair.getKey(),""+a);
								String [] pro ={"key","num","value","id"};
								int maxno =0 ;
								String[] Args = {(String) pair.getKey()};
								Cursor cu = mydb.query("dynamo",pro ,"key = ?" ,Args ,null,null,null);
								Log.d("printing cursorg",DatabaseUtils.dumpCursorToString(mydb.query("dynamo",pro ,"key = ?" ,Args ,null,null,null)));
								cu.moveToFirst();
								boolean aal =false;
								String Query = "Select * from " + "dynamo" + " where " + "key" + " = '"+(String) pair.getKey()+"' AND "+"value = '"+(String) pair.getValue()+"'";
								Cursor cursor = mydb.rawQuery(Query, null);
								Log.d("quor",DatabaseUtils.dumpCursorToString(cursor));
								if(cursor.getCount() >0){
									cursor.close();
									aal =true;
								}
								String vol="x";
								boolean p = true;
								try{
								 vol=cu.getString(cu.getColumnIndex("value"));}catch (Exception e){
									e.printStackTrace();
									p = false;
								}
								boolean already_exits = false;

								if(p==false || !vol.equals((String)pair.getValue())){
									int z;
									for (cu.moveToFirst(); !cu.isAfterLast(); cu.moveToNext()) {
										try {
											z = Integer.parseInt(cu.getString(cu.getColumnIndex("num")));
											if(cu.getString(cu.getColumnIndex("value")).equals((String)pair.getValue())){
												already_exits =true;
												Log.d("already exists"+(String)pair.getValue(),DatabaseUtils.dumpCursorToString(cu));

												//break;
											}
											Log.d("printingzg", ""+z);
											if (z > maxno) {
												maxno = z;
											}
										}catch (Exception e){
											e.printStackTrace();
										}
									}


									//if(aal ==true){
										//values.put("num",-1);
									//}else {
										values.put("num", maxno + 1);
									//}
									try{
										mydb.insertWithOnConflict("dynamo", null, values,SQLiteDatabase.CONFLICT_REPLACE);
										Log.d("msg","inserted at recovery");
									}catch (Exception e){
										Log.d("insert","error with insert command");
										e.printStackTrace();
									}}
							}}catch (Exception e){
							e.printStackTrace();
						}





					} catch (IOException e) {
						e.printStackTrace();} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}


				} catch (IOException e) {
					e.printStackTrace();

				}
				if(!strings[0].equals(myPort)){lat2.countDown();}

			return null;
		}
	}
	public String  return_port_pred(String key){

		return mynode.pred_port(return_port(key));



	}

	public int return_latest_num(String key,String value)
	{
		boolean already_exists = true;
		String [] pro ={"key","num","value","id"};
		int maxno =0 ;
		String[] Args = {key};
		Cursor cu = mydb.query("dynamo",pro ,"key = ?" ,Args ,"key","MAX(num)",null);
		Log.d("printing cursorg2",DatabaseUtils.dumpCursorToString(cu));
		cu.moveToFirst();
		String vol="x";






		boolean p = true;
		try{
			vol=cu.getString(cu.getColumnIndex("value"));}catch (Exception e){
			e.printStackTrace();
			p = false;
		}

		//if(p==false || !vol.equals(value)){
			int z;
			for (cu.moveToFirst(); !cu.isAfterLast(); cu.moveToNext()) {
				try {

					//if(cu.getString(cu.getColumnIndex("value")).equals(value)){
					//	return -1;
					//}
					z = Integer.parseInt(cu.getString(cu.getColumnIndex("num")));
					Log.d("printingzg", "" + z);
					if (z > maxno) {
						maxno = z;
					}
				}catch (Exception e){
					e.printStackTrace();
				}
			}

		Log.d("maxno"+key,""+maxno);//}
		return maxno+1;
	}
}


