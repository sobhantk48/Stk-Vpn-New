package com.v2ray.app.aidl;

interface IService {
    int getStatus();
    String getErrorMessage();
    void registerCallback(IServiceCallback callback);
    void unregisterCallback(IServiceCallback callback);
}
