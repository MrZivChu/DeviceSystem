package com.xiaohezi.devicesystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.PixelFormat;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.hcnetsdk.jna.CameraHelper;
import com.hikvision.netsdk.NET_DVR_TIME;
import com.hikvision.netsdk.NET_DVR_VOD_PARA;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PlaybackActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    public static final int PLATBACK_EXCEPTION = 1;
    public static final int PLATBACK_FINISH = 2;
    public static final int PLATBACK_PROCESS = 3;

    private SeekBar seekBar_ = null;
    private SurfaceView surfaceView_ = null;
    private int previewHandle_ = -1;
    private int playBackID_ = -1;
    private int userID_ = -1;
    private int process_ = 0;
    private Lock lockPlayBack_ = new ReentrantLock(true);

    private int clickWhichBtn = -1;

    NET_DVR_TIME timeStart = new NET_DVR_TIME();
    NET_DVR_TIME timeStop = new NET_DVR_TIME();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        Button loginBtn = findViewById(R.id.loginBtn2);
        Button previewBtn = findViewById(R.id.previewBtn2);
        Button stopBtn = findViewById(R.id.stopBtn2);
        Button selectTimeBtn = findViewById(R.id.selectTimeBtn);
        TextView startTimeTextView = findViewById(R.id.startTimeTextView);
        TextView endTimeTextView = findViewById(R.id.endTimeTextView);
        ConstraintLayout timeLayout = findViewById(R.id.timeLayout);
        seekBar_ = findViewById(R.id.seekBar);
        surfaceView_ = findViewById(R.id.surfaceView2);

        timeStart.dwYear = 2023;
        timeStart.dwMonth = 02;
        timeStart.dwDay = 25;
        timeStart.dwHour = 14;
        timeStart.dwMinute = 10;
        timeStart.dwSecond = 00;
        timeStop.dwYear = 2023;
        timeStop.dwMonth = 02;
        timeStop.dwDay = 25;
        timeStop.dwHour = 14;
        timeStop.dwMinute = 20;
        timeStop.dwSecond = 00;

        if (!CameraHelper.OnInit()) {
            Toast.makeText(this, "摄像机SDK初始化失败", Toast.LENGTH_SHORT).show();
        }

        startTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickWhichBtn = 0;
                timeLayout.setVisibility(View.VISIBLE);
            }
        });

        endTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickWhichBtn = 1;
                timeLayout.setVisibility(View.VISIBLE);
            }
        });

        selectTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePicker dp = findViewById(R.id.date_picker);
                TimePicker tp = findViewById(R.id.time_picker);
                if (clickWhichBtn == 0) {
                    timeStart.dwYear = dp.getYear();
                    timeStart.dwMonth = dp.getMonth();
                    timeStart.dwDay = dp.getDayOfMonth();
                    timeStart.dwHour = tp.getHour();
                    timeStart.dwMinute = tp.getMinute();
                    startTimeTextView.setText(timeStart.dwYear + "-" + timeStart.dwMonth + "-" + timeStart.dwDay + " " + timeStart.dwHour + ":" + timeStart.dwMinute);
                } else {
                    timeStop.dwYear = dp.getYear();
                    timeStop.dwMonth = dp.getMonth();
                    timeStop.dwDay = dp.getDayOfMonth();
                    timeStop.dwHour = tp.getHour();
                    timeStop.dwMinute = tp.getMinute();
                    endTimeTextView.setText(timeStop.dwYear + "-" + timeStop.dwMonth + "-" + timeStop.dwDay + " " + timeStop.dwHour + ":" + timeStop.dwMinute);
                }
                timeLayout.setVisibility(View.GONE);
            }
        });

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
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnStop();
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

    void OnStop() {
        if (playBackID_ == -1) {
            Toast.makeText(this, "plack back first", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            lockPlayBack_.lock();// get locked
            CameraHelper.OnStopPlayBack(playBackID_);
            process_ = 0;
            seekBar_.setProgress(process_);
            playBackID_ = -1;
        } finally {
            lockPlayBack_.unlock();
        }
    }

    private Handler hander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PLATBACK_EXCEPTION:
                    Toast.makeText(PlaybackActivity.this, "playback abnormal termination,error=" + msg.arg1, Toast.LENGTH_SHORT).show();
                    break;
                case PLATBACK_FINISH:
                    seekBar_.setProgress(msg.arg1);
                    Toast.makeText(PlaybackActivity.this, "playback by time over", Toast.LENGTH_SHORT).show();
                    break;
                case PLATBACK_PROCESS:
                    seekBar_.setProgress(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    };

    void OnPreview() {
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
        playBackID_ = CameraHelper.OnPlayBackByTime(userID_, vodParma);
        if (playBackID_ < 0) {
            Toast.makeText(this, "play back failed,error=" + CameraHelper.GetLastError() + ",errorID=" + playBackID_, Toast.LENGTH_SHORT).show();
            return;
        }
        Thread threadProcess = new Thread() {
            public void run() {
                process_ = -1;
                while (true) {
                    try {
                        //There is less than 0, it stops playing, but it needs to be locked.
                        lockPlayBack_.lock();// Get Locked
                        if (playBackID_ < 0) {
                            break;
                        }
                        process_ = CameraHelper.OnGetPlayBackPos(playBackID_);
                        if (process_ < 0 || process_ > 100) {
                            int iError = CameraHelper.GetLastError();
                            CameraHelper.OnStopPlayBack(playBackID_);
                            playBackID_ = -1;
                            Message msg = new Message();
                            msg.what = PLATBACK_EXCEPTION;
                            msg.arg1 = iError;
                            hander.sendMessage(msg);
                            break;
                        } else if (process_ == 100) {
                            CameraHelper.OnStopPlayBack(playBackID_);
                            playBackID_ = -1;
                            Message msg = new Message();
                            msg.what = PLATBACK_FINISH;
                            msg.arg1 = process_;
                            hander.sendMessage(msg);
                            break;
                        } else {
                            Message msg = new Message();
                            msg.what = PLATBACK_PROCESS;
                            msg.arg1 = process_;
                            hander.sendMessage(msg);
                        }
                    } finally {
                        lockPlayBack_.unlock();// Release lock
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        threadProcess.start();
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