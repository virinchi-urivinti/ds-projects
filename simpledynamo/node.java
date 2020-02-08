package edu.buffalo.cse.cse486586.simpledynamo;

import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class node {



    HashMap<String ,Boolean> is_aliveid ;
    HashMap<BigInteger ,Boolean> is_alive;
    HashMap<BigInteger,String> revhash = new HashMap<BigInteger, String>();

    node(){
        revhash.put(Hash("5554"), "5554");
        revhash.put(Hash("5556"), "5556");
        revhash.put(Hash("5558"), "5558");
        revhash.put(Hash("5560"), "5560");
        revhash.put(Hash("5562"), "5562");
    }
    String p1 = "11108";String p2 = "11112";
    String p3 = "11116";String p4 = "11120";
    String p5 = "11124";




    BigInteger succ(BigInteger id){
        BigInteger b1 = new BigInteger("2");
        BigInteger max_val =b1.pow(160);
        if(is_alive.containsKey(id) && is_alive.get(id)){
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
                return min2;
            }
            return min1;
        }
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



    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }


    public BigInteger Hash(String key){
        String actual_key = null;
        try {
            actual_key = genHash(key);
            //Log.d("key recived ",actual_key);
            BigInteger b1 = new BigInteger(actual_key, 16);
            //Log.d("converted keu",""+b1);
            BigInteger b2 = new BigInteger("16");
            BigInteger final_key = b1.mod(b2);
            //Log.d("ihd",""+b1);

            return b1;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;

    }


    public void print_node_info(Map<String,ArrayList<Long>> hm){
        Log.d("print node_info","called");
        ArrayList<String> al = new ArrayList<String>();
        ArrayList<ArrayList<String>> all = new ArrayList<ArrayList<String>>();
        Iterator hmIterator = hm.entrySet().iterator();
        String y= "[";
        int i =0;
        while (hmIterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry)hmIterator.next();
            ArrayList<Long>  x = (ArrayList<Long>)mapElement.getValue() ;
            al.add("["+mapElement.getKey() +": "+x.get(0)+".."+x.get(1)+"]");
            //all.add(al.get(i));
            //i++;
        }
        Log.d("node_info", TextUtils.join(",",al));
    }



    ArrayList<String> return_3_succ_ports(ContentValues values){
        BigInteger num = new BigInteger("1");

        ArrayList<String> arrayList = new ArrayList<String>();
        final String bkey = (String)values.get("key");
        final String value= (String)values.get("value");
        final BigInteger key = Hash(bkey);
        BigInteger To_be_inserted_at_1 = succ(key);
        BigInteger To_be_inserted_at_2 = succ(To_be_inserted_at_1.add(num));
        BigInteger To_be_inserted_at_3 = succ(To_be_inserted_at_2.add(num));
        arrayList.add(Integer.toString(2*Integer.parseInt(revhash.get(To_be_inserted_at_1))));
        arrayList.add(Integer.toString(2*Integer.parseInt(revhash.get(To_be_inserted_at_2))));
        arrayList.add(Integer.toString(2*Integer.parseInt(revhash.get(To_be_inserted_at_3))));
        return  arrayList;

    }


    public String pred_port(String port){
        if(port.equals(p1)){
            return p2;
        }
        if(port.equals(p2)){
            return p5;
        }
        if(port.equals(p3)){
            return p1;
        }
        if(port.equals(p4)){
            return p3;
        }
        if(port.equals(p5)){
            return p4;
        }
        return null;
    }



    public String succ_port(String port){
        if(port.equals(p1)){
            return p3;
        }
        if(port.equals(p2)){
            return p1;
        }
        if(port.equals(p3)){
            return p4;
        }
        if(port.equals(p4)){
            return p5;
        }
        if(port.equals(p5)){
            return p2;
        }
        return null;
    }

/*

    public void set_node_info(String port,long heart_beat,long timestamp){
        ArrayList<Long> nl = new ArrayList<Long>();
        nl.add(heart_beat);
        nl.add(timer.mill_sec);
        synchronized (node_info) {
            node_info.put(port, nl);
        }
    }

    public void update_node_info(Map<String,ArrayList<Long>> hm){
        Iterator hmIterator = hm.entrySet().iterator();

        synchronized (node_info) {
            while (hmIterator.hasNext()) {
                Map.Entry mapElement = (Map.Entry) hmIterator.next();
                ArrayList<Long> x = (ArrayList<Long>) mapElement.getValue();


                if (node_info.containsKey("" + mapElement.getKey())) {
                    if (x.get(0) > node_info.get("" + mapElement.getKey()).get(0)) {
                        Log.d("replace", "ha");
                        set_node_info("" + mapElement.getKey(), x.get(0), timer.mill_sec);
                    }
                } else {

                    set_node_info("" + mapElement.getKey(), x.get(0), timer.mill_sec);
                }
            }
        }
    }

*/

}
