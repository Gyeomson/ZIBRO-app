package com.example.ruath.naverapi;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skp.openplatform.android.sdk.api.APIRequest;
import com.skp.openplatform.android.sdk.common.PlanetXSDKConstants;
import com.skp.openplatform.android.sdk.common.PlanetXSDKException;
import com.skp.openplatform.android.sdk.common.RequestBundle;
import com.skp.openplatform.android.sdk.common.RequestListener;
import com.skp.openplatform.android.sdk.common.ResponseMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by ruath on 2017-08-06.
 */

public class TmapGeo {

    //API Call
    static APIRequest api;
    static RequestBundle requestBundle;

    static String URL = Const.SERVER_PUBLIC + "/tmap/geo/fullAddrGeo";
    static Map<String, Object> param;

    //Result of Json Communication
    static private String JsonResult = "";
    static private boolean isComplete = false;

    public static String request(String address){
        clearResult();

        requestASync(address);

        while (!isComplete);

        return JsonResult;
    }


    public static void clearResult() {
        isComplete = false;
        JsonResult = "";
    }

    public static void requestASync(String fullAddr) {
        api = new APIRequest();
        api.setAppKey(Const.TmapKey); //앱 정보 등록
        initRequestBundle(fullAddr);

        try { //요청
            api.request(requestBundle, reqListener);

        } catch (PlanetXSDKException e) {
            e.printStackTrace();
        }
    }

    //URL만들기
    public static void initRequestBundle(String fullAddr) {
        param = new HashMap<String, Object>();
        param.put("version", "1");
        param.put("coordType", "WGS84GEO");
        param.put("fullAddr", fullAddr);


        requestBundle = new RequestBundle();
        requestBundle.setUrl(URL);
        requestBundle.setParameters(param);
        requestBundle.setHttpMethod(PlanetXSDKConstants.HttpMethod.GET);
        requestBundle.setResponseType(PlanetXSDKConstants.CONTENT_TYPE.JSON);
        Log.d("URL",requestBundle.toString());
    }


    //JsonResult에 요청한 값을 넣는다.
    static RequestListener reqListener = new RequestListener() {

        @Override
        public void onPlanetSDKException(PlanetXSDKException e) {
            //Log.d("onPlanetSDKException", "통신 실패");
            JsonResult = "";
            Log.d("Geo err",e.toString());
        }

        @Override
        public void onComplete(ResponseMessage result) {
            //통신 성공 시 호출
//            result에서 값을 추출해서 넘겨야한다.!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            Log.d("TmapGeo", result.toString());
            if (result != null) {
                JsonObject root = (JsonObject) new JsonParser().parse(String.valueOf(result));
                JsonObject coordinateInfo = (JsonObject) root.get("coordinateInfo");
                JsonArray coordinate = (JsonArray) coordinateInfo.get("coordinate");
                JsonObject coor = (JsonObject) coordinate.get(0);

                //도로명주소 newLat,newLon 구주소 lat,lon
                String Lati = String.valueOf(coor.get("lat"));
                if(Lati.equals("\"\"")) Lati = String.valueOf(coor.get("newLat"));
                Lati = Lati.replace("\"", "");
                String Lon = String.valueOf(coor.get("lon"));
                if(Lon.equals("\"\"")) Lon = String.valueOf(coor.get("newLon"));
                Lon = Lon.replace("\"", "");

                JsonResult = Lon+","+Lati;
                Log.d("Conform Geo result", JsonResult);
            }

            isComplete = true;
        }
    };

}