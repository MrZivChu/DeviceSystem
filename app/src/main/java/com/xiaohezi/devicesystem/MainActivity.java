package com.xiaohezi.devicesystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        List<Fragment> fragmentLis = new ArrayList<>();
        PreviewActivity previewActivity = new PreviewActivity();
        PlaybackActivity playbackActivity = new PlaybackActivity();
        fragmentLis.add(previewActivity);
        fragmentLis.add(playbackActivity);
        ViewPageAdapter viewPageAdapter = new ViewPageAdapter(this, fragmentLis);
        Toast.makeText(this, "4", Toast.LENGTH_LONG);
        viewPager.setAdapter(viewPageAdapter);
        Toast.makeText(this, "5", Toast.LENGTH_LONG);
        viewPager.setCurrentItem(0);
    }
}