package com.xiaohezi.devicesystem;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hcnetsdk.jna.CameraHelper;
import com.hikvision.netsdk.PTZCommand;

public class PreviewActivity extends Fragment implements SurfaceHolder.Callback {
    private SurfaceView surfaceView_ = null;
    private int previewHandle_ = -1;
    EditText ipEditText;
    EditText pwdEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_preview, container, false);

        ipEditText = view.findViewById(R.id.editIP);
        pwdEditText = view.findViewById(R.id.editPwd);
        Button loginBtn = view.findViewById(R.id.loginBtn);
        Button previewBtn = view.findViewById(R.id.previewBtn);
        Button leftBtn = view.findViewById(R.id.leftBtn);
        Button rightBtn = view.findViewById(R.id.rightBtn);
        Button upBtn = view.findViewById(R.id.upBtn);
        Button downBtn = view.findViewById(R.id.downBtn);
        surfaceView_ = view.findViewById(R.id.surfaceView);

        if (!CameraHelper.OnInit()) {
            Toast.makeText(this.getContext(), "摄像机SDK初始化失败", Toast.LENGTH_SHORT).show();
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
        return view;
    }

    void OnLogin() {
        String ip = ipEditText.getText().toString();
        String pwd = pwdEditText.getText().toString();
        int userID = CameraHelper.OnLogin(ip, "admin", pwd, 8000);
        if (userID == -1) {
            Toast.makeText(this.getContext(), "无法连接到摄像头", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this.getContext(), "摄像机初始化成功", Toast.LENGTH_SHORT).show();
        }
    }

    void OnPreview() {
        if (previewHandle_ != -1) {
            CameraHelper.OnStopRealPlay(previewHandle_);
        }
        previewHandle_ = CameraHelper.OnRealPlay(surfaceView_);
        if (previewHandle_ < 0) {
            Toast.makeText(this.getContext(), "NET_DVR_RealPlay_V40 fail, Err:" + CameraHelper.GetLastError(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this.getContext(), "NET_DVR_RealPlay_V40 Succ ", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this.getContext(), "NET_DVR_PlayBackSurfaceChanged" + CameraHelper.GetLastError(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this.getContext(), "NET_DVR_RealPlaySurfaceChanged" + CameraHelper.GetLastError(), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
