package com.xiaohezi.devicesystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Message;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hcnetsdk.jna.CameraHelper;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_TIME;
import com.hikvision.netsdk.NET_DVR_VOD_PARA;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PlaybackActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private SurfaceView surfaceView_ = null;
    private int previewHandle_ = -1;
    private int playBackID_ = -1;
    private int userID_ = -1;
    private int iProcess_ = 0;
    private Lock lockPlayBack_ = new ReentrantLock(true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        Button loginBtn = findViewById(R.id.loginBtn2);
        Button previewBtn = findViewById(R.id.previewBtn2);
        surfaceView_ = findViewById(R.id.surfaceView2);

        if (!CameraHelper.OnInit()) {
            Toast.makeText(this, "摄像机SDK初始化失败", Toast.LENGTH_SHORT).show();
        }
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnLogin();
            }
        });
        previewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnPreview();
            }
        });
    }

    void OnLogin() {
        EditText ipEditText = findViewById(R.id.editIP2);
        String ip = ipEditText.getText().toString();
        EditText pwdEditText = findViewById(R.id.editPwd2);
        String pwd = pwdEditText.getText().toString();
        userID_ = CameraHelper.OnLogin(ip, "admin", pwd, 8000);
        if (userID_ == -1) {
            Toast.makeText(this, "无法连接到摄像头", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "摄像机初始化成功", Toast.LENGTH_SHORT).show();
        }
    }

    void OnPreview() {
        NET_DVR_TIME timeStart = new NET_DVR_TIME();
        timeStart.dwYear = 2023;
        timeStart.dwMonth = 02;
        timeStart.dwDay = 25;
        timeStart.dwHour = 14;
        timeStart.dwMinute = 10;
        timeStart.dwSecond = 00;
        NET_DVR_TIME timeStop = new NET_DVR_TIME();
        timeStop.dwYear = 2023;
        timeStop.dwMonth = 02;
        timeStop.dwDay = 25;
        timeStop.dwHour = 14;
        timeStop.dwMinute = 20;
        timeStop.dwSecond = 00;

        NET_DVR_VOD_PARA vodParma = new NET_DVR_VOD_PARA();
        vodParma.struBeginTime = timeStart;
        vodParma.struEndTime = timeStop;
        vodParma.byStreamType = 0;
        vodParma.struIDInfo.dwChannel = 1;
        vodParma.hWnd = surfaceView_.getHolder().getSurface();

        if (playBackID_ != -1) {
            Toast.makeText(this, "maybe plack back already,click stop button first", Toast.LENGTH_SHORT).show();
            return;
        }
        playBackID_ = CameraHelper.OnPlayBackByTime_v40(userID_, vodParma);
        if (playBackID_ < 0) {
            Toast.makeText(this, "play back failed,error=" + CameraHelper.GetLastError() + ",errorID=" + playBackID_, Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        surfaceView_.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        if (-1 == previewHandle_) {
            return;
        }
        Surface surface = surfaceView_.getHolder().getSurface();
        if (surface.isValid()) {
            if (-1 == CameraHelper.OnRealPlaySurfaceChanged(previewHandle_, 0, surfaceView_))
                Toast.makeText(this, "NET_DVR_PlayBackSurfaceChanged" + CameraHelper.GetLastError(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        if (-1 == previewHandle_) {
            return;
        }
        if (surfaceView_.getHolder().getSurface().isValid()) {
            if (-1 == CameraHelper.OnRealPlaySurfaceChanged(previewHandle_, 0, null)) {
                Toast.makeText(this, "NET_DVR_RealPlaySurfaceChanged" + CameraHelper.GetLastError(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}