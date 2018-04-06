package com.example.ruath.naverapi;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapCompassManager;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPathData;
import com.nhn.android.maps.overlay.NMapPathLineStyle;
import com.nhn.android.mapviewer.overlay.NMapMyLocationOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;
import com.nhn.android.mapviewer.overlay.NMapPathDataOverlay;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.sql.DriverManager.println;


public class MainGuardActivity extends NMapActivity{

    private NMapView mMapView;// 지도 화면 View
    private NMapController nMapController;//지도 컨트롤러

    //오버레이 관리
    private NMapOverlayManager mOverlayManager;
    private NMapViewerResourceProvider mMApViewerResourceProvider;

    //단말기의 현재 위치
    private NMapMyLocationOverlay mMyLocationOverlay;
    private NMapCompassManager mMapCompassManager;
    NMapLocationManager mMapLocationManager; //단말기의 현재 위치 탐색 기능을 사용하기 위한 클래스이다.

    //private final String Naver_CLIENT_ID = "oqSlJKfQyPWda6E07SR8";// 애플리케이션 클라이언트 아이디 값(Const로 넘김)
    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private static String dummyjson = "";

    //path tracking에 사용할 변수 (경로를 10m이하로 잘라서 저장)
    ArrayList<NGeoPoint> GeopointArray = new ArrayList<>();
    boolean isParent;
    String userId = "", startPoint, endPoint;
    boolean done = true;
    BroadcastReceiver mReceiver;
    NGeoPoint ChildLoaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            //MapView
            mMapView = new NMapView(this);
            setContentView(mMapView);
            mMapView.setClientId(Const.Naver_CLIENT_ID); // 클라이언트 아이디 값 설정
            mMapView.setClickable(true); //지도를 터치할 수 있도록 활성화
            mMapView.setEnabled(true);
            mMapView.setFocusable(true);
            mMapView.setFocusableInTouchMode(true);
            //create MapController
            nMapController = mMapView.getMapController();
            //create resource provider
            mMApViewerResourceProvider = new NMapViewerResourceProvider(this);
            //create overlay manager
            mOverlayManager = new NMapOverlayManager(this, mMapView, mMApViewerResourceProvider);
            // location manager
            mMapLocationManager = new NMapLocationManager(this);
            //!!!!!!!!!!!!!!!!!!!!mMapLocationManager.setOnLocationChangeListener(this);
            //create compass manager
            mMapCompassManager = new NMapCompassManager(this);
            //현재 위치 화면에 표시
            mMyLocationOverlay = mOverlayManager.createMyLocationOverlay(mMapLocationManager, mMapCompassManager);



            //GPS Permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }
            }
            else { //process
                Intent userInfo = getIntent();
                userId = (String) userInfo.getExtras().get("userId");
                startPoint = (String) userInfo.getExtras().get("startPoint");
                endPoint = (String) userInfo.getExtras().get("endPoint");
                String parent1 = (String) userInfo.getExtras().get("parent1");
                String parent2 = (String) userInfo.getExtras().get("parent2");
                isParent = userInfo.getBooleanExtra("isParent", true);
                Log.d("Conform Main userId", userId); Log.d("Conform Main startPoint", startPoint); Log.d("Conform Main endPoint", endPoint);
                Log.d("Conform Main parent1", parent1); Log.d("Conform Main parent2", parent2); Log.d("Conform Main isParent", String.valueOf(isParent));

//                if(isParent){
                    requestChildLocation();
//
//                }else {
//                    requestMyLocation();
//                }
            }

        /**********PATH**********여기부터**********/
        //경로 받아옴
        TmapJsonRequest path = new TmapJsonRequest();
        dummyjson = path.request(startPoint, endPoint);

        //dummyjson에 받아온 값에서 경로 추출
        if (dummyjson != null && dummyjson.length() > 0) {
            JsonObject root = (JsonObject) new JsonParser().parse(dummyjson);
            JsonArray featurelist = root.getAsJsonArray("features");

            ArrayList<NGeoPoint> points = new ArrayList<>();

            for (JsonElement feature : featurelist) {
                JsonObject geometry = (JsonObject) ((JsonObject) feature).get("geometry");
                String type = geometry.get("type").getAsString();
                JsonArray poilist = geometry.getAsJsonArray("coordinates");
                if (type.equals("Point")) {
                    float x = poilist.get(0).getAsFloat();
                    float y = poilist.get(1).getAsFloat();

                    points.add(new NGeoPoint(x, y));

                    //System.out.println(x + ", " + y);
                } else {
                    for (JsonElement poi : poilist) {
                        JsonArray infos = poi.getAsJsonArray();

                        //좌표 값들 받아오기
                        float x = infos.get(0).getAsFloat();
                        float y = infos.get(1).getAsFloat();

                        points.add(new NGeoPoint(x, y));

                        //System.out.println(x + ", " + y);
                    }
                }
            }

            //출발지 목적지 표시
            NMapPOIdata poIdata = new NMapPOIdata(2, mMApViewerResourceProvider);
            poIdata.beginPOIdata(2);
            Drawable startPin = getResources().getDrawable(android.R.drawable.btn_star);
            Drawable endPin = getResources().getDrawable(android.R.drawable.btn_star);
            poIdata.addPOIitem(points.get(0).getLongitude(), points.get(0).getLatitude(), "StarPoint", startPin, 0);
            poIdata.addPOIitem(points.get(points.size() - 1).getLongitude(), points.get(points.size() - 1).getLatitude(), "endPoint", endPin, 0);
            poIdata.endPOIdata();
            NMapPOIdataOverlay nMapPOIdataOverlay = mOverlayManager.createPOIdataOverlay(poIdata, null);


            int k = 0; //확인용 변수

            //경로 그리기
            NMapPathData pathData = new NMapPathData(points.size());
            pathData.initPathData();
            for (int i = 0; i < points.size(); i++) {
                if (i == 0) {
                    pathData.addPathPoint(points.get(i).getLongitude(), points.get(i).getLatitude(), NMapPathLineStyle.TYPE_SOLID);
                    GeopointArray.add(new NGeoPoint(points.get(i).getLongitude(), points.get(i).getLatitude()));
                    //Log.d("start GeopointArray", "[" + String.valueOf(k) + "]" + String.valueOf(GeopointArray.get(k)));

                } else {
                    pathData.addPathPoint(points.get(i).getLongitude(), points.get(i).getLatitude(), 0);

                    double distance = NGeoPoint.getDistance(points.get(i), points.get(i - 1));
                    //Log.d("DISTANCE", String.valueOf(distance));
                    if (distance > 10) {
                        int n = (int) (distance / 10) + 1;

                        //(n+1)등분
                        for (int j = 1; j <= n; j++) {
                            double interLong = (j * points.get(i).getLongitude() + (n - j) * points.get(i - 1).getLongitude()) / n;
                            double interLati = (j * points.get(i).getLatitude() + (n - j) * points.get(i - 1).getLatitude()) / n;
                            NGeoPoint newGeo = new NGeoPoint(interLong, interLati);
                            GeopointArray.add(newGeo);
                            k++;

                            //Log.d("GeopointArray", "[" + String.valueOf(k) + "]" + String.valueOf(GeopointArray.get(k)));
                            distance = NGeoPoint.getDistance(GeopointArray.get(k - 1), GeopointArray.get(k));
                            //Log.d("Interval distance(if)", String.valueOf(distance));
                        }
                    } else {
                        //GeopointArray.add(new NGeoPoint(points.get(i).getLongitude(), points.get(i).getLatitude()));
                        GeopointArray.add(new NGeoPoint(points.get(i).getLongitude(), points.get(i).getLatitude()));
                        k++;
                        //Log.d("GeopointArray", "[" + String.valueOf(k) + "]" + String.valueOf(GeopointArray.get(k)));
                        distance = NGeoPoint.getDistance(GeopointArray.get(k - 1), GeopointArray.get(k));
                        //Log.d("Interval distance(else)", String.valueOf(distance));
                    }

                }
            }
            pathData.endPathData();

            //경로 그리기
            NMapPathDataOverlay pathDataOverlay = mOverlayManager.createPathDataOverlay(pathData);
        }
        /**********PATH**********여기까지**********/
    }//end of onCreate =>requestMyLocation

//    public void requestMyLocation(){
//        boolean result = mMapLocationManager.enableMyLocation(true);
//    }

    //아이의 위치 요청
    public void requestChildLocation() {
        Intent childLocation = new Intent(getApplicationContext(), HttpURLConnectionRequest.class);
        childLocation.putExtra("url_state", "last")
                    .putExtra("user", userId);
        startService(childLocation);
    }

    public void showChildLocation(NGeoPoint nGeoPoint) {
        //mMapLocationManager.enableMyLocation(true);
        //if(nGeoPoint != null) {
            //현재 위치로 이동
            nMapController.animateTo(nGeoPoint);
            nMapController.setMapCenter(nGeoPoint, 15);


            /**********TRACKING*********여기부터**********/
            boolean far = false;
            double tracking;
            int k = 0;
            for (NGeoPoint check : GeopointArray) {
                tracking = NGeoPoint.getDistance(check, nGeoPoint);
                if (tracking >= 50) {
                    far = true;
                } else {
                    far = false;
                    break;
                }
            }
            if (far == true) {
                //알람 R.raw에 알림음 파일을 넣어야되는 듯 => mp3 파일 다운받아서 폴더에 넣어야함 넣으면 될 듯
//                SoundPool pool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
//                int ding = pool.load(this, R.raw.beep, 1);
//                pool.play(ding, 1, 1, 0, 2, 1);
                Toast.makeText(this, "경로 벗어남 far=" + far, Toast.LENGTH_SHORT).show();

                queue = Volley.newRequestQueue(getApplicationContext());
                getRegistrationId();
                String msg =" 경로를 이탈하였습니다.";
                send(msg);

                Intent SMSintent = new Intent(getApplicationContext(), Sendmessage.class);
                startService(SMSintent);
            }
            /**********TRACKING*********여기까지**********/

        //}
    }
//
//    @Override
//    public boolean onLocationChanged(NMapLocationManager nMapLocationManager, NGeoPoint nGeoPoint) {
//
////        if(nMapLocationManager != null) {
////            //현재 위치로 이동
////            nMapController.animateTo(nGeoPoint);
////            nMapController.setMapCenter(nGeoPoint, 15);
////
////            /**********서버에 현재 위치 전송********************/
//////            Intent intent = new Intent(getApplicationContext(), HttpURLConnectionRequest.class);
//////            intent.putExtra("url_state", "position").putExtra("user", userId).putExtra("nGeoPoint", nGeoPoint.toString());
//////            startService(intent);
////
////            /**********TRACKING*********여기부터**********/
////            boolean far = false;
////            double tracking;
////            int k = 0;
////            for (NGeoPoint check : GeopointArray) {
////                tracking = NGeoPoint.getDistance(check, nGeoPoint);
////                if (tracking >= 50) {
////                    far = true;
////                } else {
////                    far = false;
////                    break;
////                }
////            }
////            if (far == true) {
////                //알람 R.raw에 알림음 파일을 넣어야되는 듯 => mp3 파일 다운받아서 폴더에 넣어야함 넣으면 될 듯
//////                SoundPool pool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
//////                int ding = pool.load(this, R.raw.beep, 1);
//////                pool.play(ding, 1, 1, 0, 2, 1);
////                Toast.makeText(this, "경로 벗어남 far=" + far, Toast.LENGTH_SHORT).show();
////
////                queue = Volley.newRequestQueue(getApplicationContext());
////                getRegistrationId();
////                String msg =" 경로를 이탈하였습니다.";
////                send(msg);
////
////                Intent SMSintent = new Intent(getApplicationContext(), Sendmessage.class);
////                startService(SMSintent);
////            }
////            /**********TRACKING*********여기까지**********/
////
////        }
////
//        return true; //다음에 다시 호출 가능
//    }
//
//    @Override //Not use
//    public void onLocationUpdateTimeout(NMapLocationManager nMapLocationManager) {
//
//    }
//
//    @Override //Not use
//    public void onLocationUnavailableArea(NMapLocationManager nMapLocationManager, NGeoPoint nGeoPoint) {
//
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
//                    requestMyLocation();
                    requestChildLocation();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, " 안되욤",  Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**********message***************/
    @Override
    protected void onNewIntent(Intent intent) {
        println("onNewIntent() called");

        if(intent != null) {
            processIntent(intent);
        }
        super.onNewIntent(intent);
    }
    private void processIntent(Intent intent) {
        String from = intent.getStringExtra("from");
        if (from == null) {
            println("from is null");
            return;
        }

        String contents = intent.getStringExtra("contents");

        println("DATA : "+ from +", "+ contents);
        Toast.makeText(this, "["+from+"]로부터 수신한 데이터 : "+contents, Toast.LENGTH_SHORT).show();
//        messageOutput.setText("["+from+"]로부터 수신한 데이터 : "+contents);
    }

    String reqId;
    RequestQueue queue;

    public void getRegistrationId(){
        println("getRegistrationId() 호출됨.");

        reqId = FirebaseInstanceId.getInstance().getToken();
        println("reqId : "+reqId);

    }

    public void send(String input){
        JSONObject requestData = new JSONObject();

        try {
            requestData.put("priority", "high");
            JSONObject dataObj = new JSONObject();
            dataObj.put("Contents", input);
            requestData.put("data", dataObj);

            JSONArray idArray = new JSONArray();
            idArray.put(0, reqId);
            requestData.put("registration_ids", idArray);
        }catch(Exception e) {
            e.printStackTrace();
        }

        sendData(requestData, new Sendmessage.SendResponseListener() {
            @Override
            public void onRequestCompleted() {
                println("onRequestCompleted() 호출됨");
            }

            @Override
            public void onRequestStarted() {
                println("onRequestStarted() 호출됨");
            }

            @Override
            public void onRequestWithError(VolleyError error) {
                println("onRequestWithError() 호출됨");
            }
        });
    }

    public interface  SendResponseListener {
        public void onRequestStarted();
        public void onRequestCompleted();
        public void onRequestWithError(VolleyError error);
    }

    public void sendData(JSONObject requestData, final Sendmessage.SendResponseListener listener) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                "http://fcm.googleapis.com/fcm/send",
                requestData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onRequestCompleted();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onRequestWithError(error);
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "key=AAAAu9FSFm8:APA91bGltxKCMbsOg4PNzDjG0EiyfI_foddivAv_Io19Xmh_8AsvJCqlpWWANvKPAkg0qs1aXVd4e16Qm4aTwaYPhV_8upNTDcMrpcj8SOcT_kU2OTBYY9h1zW4UgVaIt6TwSQ0RO7_v");

                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        request.setShouldCache(false);
        listener.onRequestStarted();
        queue.add(request);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
//        unregisterReceiver(mReceiver);
    }

    private void registerReceiver() {
        if(mReceiver != null) return;
        final IntentFilter theFilter = new IntentFilter("com.example.ruaths.SEND_BROAD_CAST");
        this.mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String name = intent.getAction();

                if(name.equals("com.example.ruaths.SEND_BROAD_CAST")){
                    String child = intent.getStringExtra("result");

                    if(!child.equals("Not")){
                        JsonObject root = (JsonObject) new JsonParser().parse(child);
                        String longi = String.valueOf(root.get("longi")); longi = longi.replace("\"", "");
                        String lati = String.valueOf(root.get("latit")); lati = lati.replace("\"", "");
                        double longitude = Double.parseDouble(longi);
                        double latitude = Double.parseDouble(lati);
                        ChildLoaction = new NGeoPoint(longitude, latitude);
                        showChildLocation(ChildLoaction);
                        requestChildLocation();
                    }
                }
            }
        };
        this.registerReceiver(this.mReceiver, theFilter);
    }

    private void unregisterReceiver() {
        if(mReceiver != null)
            this.unregisterReceiver(mReceiver);
    }

}

