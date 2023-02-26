package com.xiaohezi.devicesystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hcnetsdk.jna.CameraHelper;
import com.hcnetsdk.jna.HCNetSDKJNAInstance;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_PREVIEWINFO;
import com.hikvision.netsdk.PTZCommand;
import com.sun.jna.Pointer;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView surfaceView_ = null;
    private int previewHandle_ = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button loginBtn = findViewById(R.id.loginBtn);
        Button previewBtn = findViewById(R.id.previewBtn);
        Button leftBtn = findViewById(R.id.leftBtn);
        Button rightBtn = findViewById(R.id.rightBtn);
        Button upBtn = findViewById(R.id.upBtn);
        Button downBtn = findViewById(R.id.downBtn);
        surfaceView_ = findViewById(R.id.surfaceView);

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
        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraHelper.OnPTZControl(previewHandle_, PTZCommand.PAN_LEFT);
            }
        });
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraHelper.OnPTZControl(previewHandle_, PTZCommand.PAN_RIGHT);
            }
        });
        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraHelper.OnPTZControl(previewHandle_, PTZCommand.TILT_UP);
            }
        });
        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraHelper.OnPTZControl(previewHandle_, PTZCommand.TILT_DOWN);
            }
        });
    }

    void OnLogin() {
        EditText ipEditText = findViewById(R.id.editIP);
        String ip = ipEditText.getText().toString();
        EditText pwdEditText = findViewById(R.id.editPwd);
        String pwd = pwdEditText.getText().toString();
        int userID = CameraHelper.OnLogin(ip, "admin", pwd, 8000);
        if (userID == -1) {
            Toast.makeText(this, "无法连接到摄像头", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "摄像机初始化成功", Toast.LENGTH_SHORT).show();
        }
    }

    void OnPreview() {
        if (previewHandle_ != -1) {
            CameraHelper.OnStopRealPlay(previewHandle_);
        }
        previewHandle_ = CameraHelper.OnRealPlay(surfaceView_);
        if (previewHandle_ < 0) {
            Toast.makeText(this, "NET_DVR_RealPlay_V40 fail, Err:" + CameraHelper.GetLastError(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "NET_DVR_RealPlay_V40 Succ ", Toast.LENGTH_SHORT).show();
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