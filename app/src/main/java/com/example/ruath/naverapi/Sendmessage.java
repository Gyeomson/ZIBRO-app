package com.example.ruath.naverapi;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static java.sql.DriverManager.println;

/**
 * Created by ruath on 2017-09-15.
 */

public class Sendmessage {
    String reqId;
    RequestQueue queue;

    public Sendmessage() {
//        String msg =" 경로를 이탈하였습니다.";
//        send(msg);
//
//        queue = Volley.newRequestQueue(getApplicationContext());
//
//        getRegistrationId();
    }

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

        sendData(requestData, new SendResponseListener() {
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

    public void sendData(JSONObject requestData, final SendResponseListener listener) {
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
}

