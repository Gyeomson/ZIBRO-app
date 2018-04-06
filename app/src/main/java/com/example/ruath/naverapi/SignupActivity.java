package com.example.ruath.naverapi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.util.StringTokenizer;

/**
 * Created by ruath on 2017-10-09.
 */

public class SignupActivity extends Activity {
    String user, start, end, guard1, guard2 = "";

    BroadcastReceiver mReceiver;
    Intent intentCreate;

    EditText userId;
    EditText home;
    EditText school;
    EditText parent1;
    EditText parent2;
    Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        userId = (EditText) findViewById(R.id.Id);
        home = (EditText) findViewById(R.id.home);
        school = (EditText) findViewById(R.id.school);
        parent1 = (EditText) findViewById(R.id.parent1);
        parent2 = (EditText) findViewById(R.id.parent2);
        signupButton = (Button) findViewById(R.id.signupButton);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            user = userId.getText().toString();
            start = home.getText().toString();
            end = school.getText().toString();
            guard1 = parent1.getText().toString();
            guard2 = parent2.getText().toString();

            //Geo Coding
            TmapGeo Geo = new TmapGeo();
            start = Geo.request(start); end = Geo.request(end);
            //Log.d("Conform Geo StartPoint",start); Log.d("Conform Geo endPoint", end);

            TmapJsonRequest path = new TmapJsonRequest();
            String canGo = path.request(start, end);
            if( canGo.isEmpty() ){
                Toast.makeText(getApplicationContext(), "보행 경로가 없습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Intent intentCheck = new Intent(getApplicationContext(), HttpURLConnectionRequest.class);
                intentCheck.putExtra("url_state", "user")
                            .putExtra("user", user);
                startService(intentCheck);
            }
            path = null; canGo="";
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
        unregisterReceiver();
    }

    private void registerReceiver() {
        if(mReceiver != null) return;
        final IntentFilter theFilter = new IntentFilter("com.example.ruaths.SEND_BROAD_CAST");
        this.mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            String name = intent.getAction();
            Log.d("name", name);
            // Intent SendBroadCast로 보낸 action TAG 이름으로 필요한 방송을 찾는다.
            if(name.equals("com.example.ruaths.SEND_BROAD_CAST")){
                String result = intent.getStringExtra("result");

                Log.d("Conform Signup userId", user); Log.d("ConformSign startPoint", start); Log.d("Conform Signup endPoint", end);
                Log.d("Conform Signup parent1", guard1); Log.d("Conform Signup parent2", guard2);
                if(result.equals("Not")){
                    intentCreate = new Intent(getApplicationContext(), HttpURLConnectionRequest.class);
                    intentCreate
                            .putExtra("url_state", "create")
                            .putExtra("user", user)
                            .putExtra("startPoint", start)
                            .putExtra("endPoint", end)
                            .putExtra("parent1", guard1)
                            .putExtra("parent2", guard2);
                    startService(intentCreate);

                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), user+"는 이미 존재하는 아이디입니다.", Toast.LENGTH_SHORT).show();
                }
            }
            }
        };
        this.registerReceiver(this.mReceiver, theFilter);
    }
    private void unregisterReceiver() {
        if(mReceiver != null)
            this.unregisterReceiver(mReceiver);
        stopService(intentCreate);
    }
}
