package com.example.ruath.naverapi;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

//클라우드 서버에 단말을 등록하는 서비스
public class MyFIrebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyIID";

    @Override
    public void onTokenRefresh(){ //단말의 등록ID를 전달받으면 호출
        Log.d(TAG, "onTokenRefresh 호출됨.");

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed Token : "+refreshedToken); //전달받은 등록ID 확인
    }
}
