package com.example.ruath.naverapi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.StringTokenizer;

import static android.R.attr.data;
import static com.skp.openplatform.android.sdk.oauth.OAuthInfoManager.context;

/**
* Created by ruath on 2017-10-08.
*/

public class LoginActivity extends Activity {
    String id="";
    boolean isParent;
    BroadcastReceiver mReceiver;
    Intent intentCheck;

    EditText userId;
    CheckBox checkParent;
    Button loginButton;
    Button goSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        userId = (EditText) findViewById(R.id.userId);
        checkParent = (CheckBox) findViewById(R.id.checkParent);
        loginButton = (Button) findViewById(R.id.loginButton);
        goSignup = (Button) findViewById(R.id.goSignup);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            id = userId.getText().toString();
            isParent = checkParent.isChecked();

            //서버에서 return한 값(user)을 받아와서 user에 넣는다.

            intentCheck = new Intent(getApplicationContext(), HttpURLConnectionRequest.class);
            intentCheck.putExtra("url_state", "user")
                        .putExtra("user", id.toString());
            startService(intentCheck);
            }
        });

        goSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intentGo = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intentGo);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(intentCheck != null) stopService(intentCheck);
        unregisterReceiver();
    }

    private void registerReceiver() {
        if(mReceiver != null) return;
        final IntentFilter theFilter = new IntentFilter("com.example.ruaths.SEND_BROAD_CAST");
        this.mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            String name = intent.getAction();
            //Log.d("name", name);
            // Intent SendBroadCast로 보낸 action TAG 이름으로 필요한 방송을 찾는다.
            if(name.equals("com.example.ruaths.SEND_BROAD_CAST")){
                String userInfo = intent.getStringExtra("result");

                if(userInfo.equals("Not")){
                    Toast.makeText(getApplicationContext(), "존재하지 않는 아이디입니다.", Toast.LENGTH_SHORT).show();
                } else {
                    JsonArray root = (JsonArray) new JsonParser().parse(userInfo);
                    JsonObject user = (JsonObject) root.get(0);
                    String userID = String.valueOf(user.get("userId")); userID = userID.replace("\"", "");
                    String startPoint = String.valueOf(user.get("startingPoint")); startPoint = startPoint.replace("\"", "");
                    String endPoint = String.valueOf(user.get("destination")); endPoint = endPoint.replace("\"", "");
                    String parent1 = String.valueOf(user.get("parentNumber")); parent1 = parent1.replace("\"", "");
                    String parent2 = String.valueOf(user.get("parentNumber2")); parent2 = parent2.replace("\"", "");
                    Log.d("Conform Login userId", userID); Log.d("ConformLogin startPoint", startPoint); Log.d("Conform Login endPoint", endPoint);
                    Log.d("Conform Login parent1", parent1); Log.d("Conform Login parent2", parent2); Log.d("Conform Login isParent", String.valueOf(isParent));
                    if(isParent){
                        Intent intentGuardActivity = new Intent(LoginActivity.this, MainGuardActivity.class);
                        intentGuardActivity
                                .putExtra("userId", userID)
                                .putExtra("startPoint", startPoint)
                                .putExtra("endPoint", endPoint)
                                .putExtra("parent1", parent1)
                                .putExtra("parent2", parent2);
                        startActivity(intentGuardActivity);
                        finish();
                    } else {
                        Intent intentActivity = new Intent(LoginActivity.this, MainActivity.class);
                        intentActivity
                                .putExtra("userId", userID)
                                .putExtra("startPoint", startPoint)
                                .putExtra("endPoint", endPoint)
                                .putExtra("parent1", parent1)
                                .putExtra("parent2", parent2);
                        startActivity(intentActivity);
                        finish();
                    }


                }

            }
            }
        };
        this.registerReceiver(this.mReceiver, theFilter);
    }
    private void unregisterReceiver() {
        if(mReceiver != null)
            this.unregisterReceiver(mReceiver);
        stopService(intentCheck);
    }

}
