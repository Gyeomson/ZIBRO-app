package com.example.ruath.naverapi;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.skp.openplatform.android.sdk.api.APIRequest;
import com.skp.openplatform.android.sdk.common.PlanetXSDKConstants;
import com.skp.openplatform.android.sdk.common.PlanetXSDKException;
import com.skp.openplatform.android.sdk.common.RequestBundle;
import com.skp.openplatform.android.sdk.common.RequestListener;
import com.skp.openplatform.android.sdk.common.ResponseMessage;
import com.skp.openplatform.android.sdk.oauth.OAuthInfoManager;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by ruath on 2017-08-06.
 */

public class TmapJsonRequest{

    static APIRequest api;
    static RequestBundle requestBundle;

    static String URL = Const.SERVER_PUBLIC + "/tmap/routes/pedestrian";
    static Map<String, Object> param;

    //Result of Json Communication
    static private String JsonResult = "";
    static private boolean isComplete = false;

    public static String request(String startPoint, String endPoint){
        clearResult();

        StringTokenizer start = new StringTokenizer(startPoint, ",");
        String startX = start.nextToken(); String startY = start.nextToken();
        StringTokenizer end = new StringTokenizer(endPoint, ",");
        String endX = end.nextToken(); String endY = end.nextToken();
        requestASync(startX, startY, endX, endY);

        while (!isComplete);

        return JsonResult;
    }

    public static void clearResult() {
        JsonResult = "";
    }

    public static void requestASync(String startX, String startY, String endX, String endY) {
        api = new APIRequest();
        api.setAppKey(Const.TmapKey); //앱 정보 등록
        initRequestBundle(startX, startY, endX, endY);

        try { //요청
            api.request(requestBundle, reqListener);

        } catch (PlanetXSDKException e) {
            e.printStackTrace();
        }
    }

    //URL만들기
    public static void initRequestBundle(String startX, String startY, String endX, String endY) {
        param = new HashMap<String, Object>();
        param.put("version", "1");
        param.put("callback", "application/json");
        param.put("startX", startX);
        param.put("startY", startY);
        param.put("endX", endX);
        param.put("endY", endY);
        param.put("reqCoordType", "WGS84GEO");
        param.put("resCoordType", "WGS84GEO");
        param.put("startName", "출발지");
        param.put("endName", "목적지");


        requestBundle = new RequestBundle();
        requestBundle.setUrl(URL);
        requestBundle.setParameters(param);
        requestBundle.setHttpMethod(PlanetXSDKConstants.HttpMethod.GET);
        requestBundle.setResponseType(PlanetXSDKConstants.CONTENT_TYPE.JSON);
        requestBundle.toString();
    }
    //JsonResult에 요청한 값을 넣는다.
    static RequestListener reqListener = new RequestListener() {

        @Override
        public void onPlanetSDKException(PlanetXSDKException e) {
            //Log.d("onPlanetSDKException", "통신 실패");
            JsonResult = e.toString();
        }

        @Override //통신 성공 시 호출
        public void onComplete(ResponseMessage result) {
            //ResponseMessage는 StatusCode와 ResultMessage로 이루어져 있다.
            Log.d("TmapJsonRequest test", result.getStatusCode() + "\n" + result.toString());

            if( result.getStatusCode().toString().equals("200") )
                JsonResult = result.toString();

            isComplete = true;
        }
    };

}