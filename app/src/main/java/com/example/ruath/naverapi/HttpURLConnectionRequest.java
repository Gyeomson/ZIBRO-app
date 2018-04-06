package com.example.ruath.naverapi;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by ruath on 2017-08-18.
 * 여기서는 URL을 만든 다음에 서버에 요청을 한다.
 */

public class HttpURLConnectionRequest extends Service{

    public HttpURLConnectionRequest() {
//        String Geo = (String) intent.getExtras().get("nGeoPoint");
//        Log.d("nGeoPoint(Service)", Geo);
//        setPosition(3.14, 7.56);
//        new RequestTask().execute(null , Const.GPS_Server+url, longi, lati);
    }

    @Override //인터넷 검색
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override //시작할 때
    public int onStartCommand(Intent intent, int flags, int startId) {

        String url_state = (String) intent.getExtras().get("url_state");
        String userId = (String) intent.getExtras().get("user");
        String url = "";

        if(url_state != null){
            switch (url_state) {
                case "position":
                    url = "position";
                    String Geo = (String) intent.getExtras().get("nGeoPoint");
                    StringTokenizer stringTokenizer = new StringTokenizer(Geo, ",");
                    String longi = stringTokenizer.nextToken(); String lati = stringTokenizer.nextToken();
                    new RequestTask().execute(null , Const.GPS_Server+url, longi, lati, userId);
                    break;
                case "last":
                    url = "last/"+userId;
                    new RequestTask().execute(null , Const.GPS_Server+url, null, null, userId);
                    break;
                case "user":
                    url = "user/"+userId;
                    new RequestTask().execute(null , Const.GPS_Server+url, null, null, userId);
                    break;
                case "create":
                    url = "create";
                    String home = (String) intent.getExtras().get("startPoint");
                    String school = (String) intent.getExtras().get("endPoint");
                    String parent1 = (String) intent.getExtras().get("parent1");
                    String parent2 = (String) intent.getExtras().get("parent2");
                    new RequestTask().execute(null , Const.GPS_Server+url, home, school, userId, parent1, parent2);
                    break;
                default:
                    break;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    //서버랑 통신하는 비동기 작업
    private class RequestTask extends AsyncTask<String, Void, Object> {
        @Override
        protected void onPostExecute(Object obj) {
            super.onPostExecute(obj);
            Log.d("Object", obj.toString());
            if ( !( (obj.equals("") || obj.equals("{\"data\":{\"rows\":[]}}") || obj.equals("[]")) ) ) { //null이면 broadcast 안하고싶음
                Intent sendintent = new Intent("com.example.ruaths.SEND_BROAD_CAST");
                sendintent.putExtra("result", String.valueOf(obj));
                sendBroadcast(sendintent);
            } else {
                //Toast.makeText(getApplicationContext(), "ID가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                Intent sendintent = new Intent("com.example.ruaths.SEND_BROAD_CAST");
                sendintent.putExtra("result", "Not");
                sendBroadcast(sendintent);
            }
        }

        @Override //백그라운드에서 작업
        protected Object doInBackground(String... params) {
            HttpURLConnection conn = null;
            String url = params[1];

            String response = "";

            try {
                URL sUrl = new URL(url);
                conn = (HttpURLConnection) sUrl.openConnection();

                if( url.contains("create") ){
                    String home = "startingPoint="+params[2];
                    String school = "destination="+params[3];
                    String userId = "userId="+params[4];
                    String parent1 = "parentNumber="+params[5];
                    String parent2 = "parentNumber2="+params[6];

                    WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                    WifiInfo info = manager.getConnectionInfo();
                    String address = "Mac="+info.getMacAddress();
                    Log.d("Mac", address);

                    conn.setRequestMethod("POST"); //post
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();

                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                    writer.write(userId); writer.write("&");
                    writer.write(home); writer.write("&");
                    writer.write(school); writer.write("&");
                    writer.write(address); writer.write("&");
                    writer.write(parent1); writer.write("&");
                    writer.write(parent2);
                    writer.flush();
                    writer.close();
                    os.close();
                }
                else if( url.contains("position") ) {
                    String longi = "longi="+params[2];
                    String lati = "latit="+params[3];
                    String userId = "userId="+params[4];

                    conn.setRequestMethod("POST"); //post
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();

                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                    writer.write(userId); writer.write("&");
                    writer.write(longi); writer.write("&");
                    writer.write(lati);
                    writer.flush();
                    writer.close();
                    os.close();
                }
                else {
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);

                }

                int result = conn.getResponseCode(); //inputstream

                if (result == 200) { //주소 정상
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    response = reader.readLine();
                    reader.close();
                    is.close();
                } else {
                    response = result + " Error";
                }
                //Log.d("Conform HTTP response", response);
            } catch (Exception e) {
                Log.e("api", params[2] + "/" + e.toString());
            } finally {
                if (conn != null)
                    conn.disconnect();
            }

            return response;
        }
    }
}
