package com.hcnetsdk.jna;

import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_PREVIEWINFO;
import com.sun.jna.Pointer;

public class CameraHelper {

    public static boolean OnInit() {
        return HCNetSDK.getInstance().NET_DVR_Init();
    }

    public static int OnLogin(String userip, String username, String userpassward, int port) {
        HCNetSDKByJNA.NET_DVR_USER_LOGIN_INFO loginInfo = new HCNetSDKByJNA.NET_DVR_USER_LOGIN_INFO();
        System.arraycopy(userip.getBytes(), 0, loginInfo.sDeviceAddress, 0, userip.length());
        System.arraycopy(username.getBytes(), 0, loginInfo.sUserName, 0, username.length());
        System.arraycopy(userpassward.getBytes(), 0, loginInfo.sPassword, 0, userpassward.length());
        loginInfo.wPort = (short) port;
        HCNetSDKByJNA.NET_DVR_DEVICEINFO_V40 deviceInfo = new HCNetSDKByJNA.NET_DVR_DEVICEINFO_V40();
        loginInfo.write();
        return HCNetSDKJNAInstance.getInstance().NET_DVR_Login_V40(loginInfo.getPointer(), deviceInfo.getPointer());
    }

    public static int OnRealPlay(SurfaceView surfaceView) {
        NET_DVR_PREVIEWINFO playInfo = new NET_DVR_PREVIEWINFO();
        playInfo.lChannel = 1;
        playInfo.dwStreamType = 0;
        playInfo.bBlocked = 1;
        playInfo.hHwnd = surfaceView.getHolder();
        return RealPlay_V40_jni(0, playInfo, null);
    }

    public static boolean OnStopRealPlay(int previewHandle){
        if (previewHandle < 0) {
            Log.e("DeviceSystem", "RealPlay_Stop_jni failed with error param");
            return false;
        }
        if(!HCNetSDK.getInstance().NET_DVR_StopRealPlay(previewHandle))
        {
            Log.e("DeviceSystem", "RealPlay_Stop_jni failed");
            return false;
        }
        return true;
    }

    public static int OnRealPlaySurfaceChanged(int previewHandle, int nRegionNum, SurfaceView surfaceView) {
        if (previewHandle < 0 || nRegionNum < 0) {
            Log.e("SimpleDemo", "RealPlaySurfaceChanged_jni failed with error param");
            return -1;
        }
        return HCNetSDK.getInstance().NET_DVR_RealPlaySurfaceChanged(previewHandle, nRegionNum, surfaceView.getHolder());
    }

    public static int GetLastError() {
        return HCNetSDK.getInstance().NET_DVR_GetLastError();
    }

    private static int RealPlay_V40_jni(int iUserID, NET_DVR_PREVIEWINFO playInfo, Pointer pUser) {
        if (iUserID < 0) {
            Log.e("DeviceSystem", "RealPlay_V40_jni failed with error param");
            return -1;
        }
        int iRet = HCNetSDK.getInstance().NET_DVR_RealPlay_V40(iUserID, playInfo, null);
        if (iRet < 0) {
            Log.e("DeviceSystem", "NET_DVR_RealPlay_V40 error!");
            return -1;
        }
        boolean bRet = HCNetSDKJNAInstance.getInstance().NET_DVR_OpenSound((short) iRet);
        if (bRet) {
            Log.e("DeviceSystem", "NET_DVR_OpenSound Succ!");
        }
        return iRet;
    }

}
