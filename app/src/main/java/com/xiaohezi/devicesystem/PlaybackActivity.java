package com.xiaohezi.devicesystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.app.DatePickerDialog;
import android.graphics.PixelFormat;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PlaybackActivity extends Fragment implements SurfaceHolder.Callback {
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

    NET_DVR_TIME timeStart = new NET_DVR_TIME();
    NET_DVR_TIME timeStop = new NET_DVR_TIME();

    EditText ipEditText;
    EditText pwdEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_playback, container, false);

        ipEditText = view.findViewById(R.id.editIP2);
        pwdEditText = view.findViewById(R.id.editPwd2);
        Button loginBtn =view.findViewById(R.id.loginBtn2);
        Button previewBtn = view.findViewById(R.id.previewBtn2);
        Button stopBtn = view.findViewById(R.id.stopBtn2);
        TextView startTimeTextView = view.findViewById(R.id.startTimeTextView);
        TextView endTimeTextView = view.findViewById(R.id.endTimeTextView);
        seekBar_ = view.findViewById(R.id.seekBar);
        surfaceView_ = view.findViewById(R.id.surfaceView2);

        timeStart.dwYear = 2023;
        timeStart.dwMonth = 03;
        timeStart.dwDay = 04;
        timeStart.dwHour = 20;
        timeStart.dwMinute = 00;
        timeStop.dwYear = 2023;
        timeStop.dwMonth = 03;
        timeStop.dwDay = 04;
        timeStop.dwHour = 20;
        timeStop.dwMinute = 02;
        startTimeTextView.setText(timeStart.dwYear + "-" + timeStart.dwMonth + "-" + timeStart.dwDay + " " + timeStart.dwHour + ":" + timeStart.dwMinute);
        endTimeTextView.setText(timeStop.dwYear + "-" + timeStop.dwMonth + "-" + timeStop.dwDay + " " + timeStop.dwHour + ":" + timeStop.dwMinute);

        if (!CameraHelper.OnInit()) {
            Toast.makeText(getContext(), "摄像机SDK初始化失败", Toast.LENGTH_SHORT).show();
        }

        startTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimeSelectDialog dialog = new TimeSelectDialog(getContext());
                dialog.SetCallBack(new TimeSelectDialog.ClickCallback() {
                    @Override
                    public void onClick(int year, int month, int day, int hour, int minute) {
                        timeStart.dwYear = year;
                        timeStart.dwMonth = month;
                        timeStart.dwDay = day;
                        timeStart.dwHour = hour;
                        timeStart.dwMinute = minute;
                        startTimeTextView.setText(timeStart.dwYear + "-" + timeStart.dwMonth + "-" + timeStart.dwDay + " " + timeStart.dwHour + ":" + timeStart.dwMinute);
                        dialog.hide();
                    }
                });
                dialog.show();
                dialog.UpdateTimer(timeStart.dwYear, timeStart.dwMonth, timeStart.dwDay, timeStart.dwHour, timeStart.dwMinute);
            }
        });

        endTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Calendar cal = Calendar.getInstance();
//                DatePickerDialog datePickerDialog = new DatePickerDialog(PlaybackActivity.this, new DatePickerDialog.OnDateSetListener() {
//                    @Override
//                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//                    }
//                },cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
//                DatePicker datePicker = datePickerDialog.getDatePicker();
//                datePicker.setMaxDate(new Date().getTime()); // 设置日期的上限日期
//                datePickerDialog.show();

                TimeSelectDialog dialog = new TimeSelectDialog(getContext());
                dialog.SetCallBack(new TimeSelectDialog.ClickCallback() {
                    @Override
                    public void onClick(int year, int month, int day, int hour, int minute) {
                        timeStop.dwYear = year;
                        timeStop.dwMonth = month;
                        timeStop.dwDay = day;
                        timeStop.dwHour = hour;
                        timeStop.dwMinute = minute;
                        endTimeTextView.setText(timeStop.dwYear + "-" + timeStop.dwMonth + "-" + timeStop.dwDay + " " + timeStop.dwHour + ":" + timeStop.dwMinute);
                        dialog.hide();
                    }
                });
                dialog.show();
                dialog.UpdateTimer(timeStop.dwYear, timeStop.dwMonth, timeStop.dwDay, timeStop.dwHour, timeStop.dwMinute);
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
        return  view;
    }

    void OnLogin() {
        String ip = ipEditText.getText().toString();
        String pwd = pwdEditText.getText().toString();
        userID_ = CameraHelper.OnLogin(ip, "admin", pwd, 8000);
        if (userID_ == -1) {
            Toast.makeText(getContext(), "无法连接到摄像头", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "摄像机初始化成功", Toast.LENGTH_SHORT).show();
        }
    }

    void OnStop() {
        if (playBackID_ == -1) {
            Toast.makeText(getContext(), "plack back first", Toast.LENGTH_SHORT).show();
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

    private final Handler hander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PLATBACK_EXCEPTION:
                    Toast.makeText(getContext(), "playback abnormal termination,error=" + msg.arg1, Toast.LENGTH_SHORT).show();
                    break;
                case PLATBACK_FINISH:
                    seekBar_.setProgress(msg.arg1);
                    Toast.makeText(getContext(), "playback by time over", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getContext(), "maybe plack back already,click stop button first", Toast.LENGTH_SHORT).show();
            return;
        }
        playBackID_ = CameraHelper.OnPlayBackByTime(userID_, vodParma);
        if (playBackID_ < 0) {
            Toast.makeText(getContext(), "play back failed,error=" + CameraHelper.GetLastError() + ",errorID=" + playBackID_, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "NET_DVR_PlayBackSurfaceChanged" + CameraHelper.GetLastError(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "NET_DVR_RealPlaySurfaceChanged" + CameraHelper.GetLastError(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}