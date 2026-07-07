package com.v2ray.app.aidl;

import com.v2ray.app.aidl.IServiceCallback;

interface IService {
    int getStatus();
    String getErrorMessage();
    void registerCallback(in IServiceCallback callback);
    oneway void unregisterCallback(in IServiceCallback callback);
}
