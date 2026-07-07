package com.v2ray.app.aidl;

interface IServiceCallback {
    void onServiceStatusChanged(int status);
    void onServiceAlert(int type, String message);
}
