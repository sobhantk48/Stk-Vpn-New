package io.nekohasekai.libbox;

public interface PlatformInterface {
    int openTun(TunOptions options);
    void autoDetectInterfaceControl(int fd);
}
