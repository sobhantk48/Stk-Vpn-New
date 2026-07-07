package io.nekohasekai.libbox;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public interface PlatformInterface {
    void autoDetectInterfaceControl(int i10);

    void clearDNSCache();

    void closeDefaultInterfaceMonitor(InterfaceUpdateListener interfaceUpdateListener);

    ConnectionOwner findConnectionOwner(int i10, String str, int i11, String str2, int i12);

    NetworkInterfaceIterator getInterfaces();

    boolean includeAllNetworks();

    LocalDNSTransport localDNSTransport();

    int openTun(TunOptions tunOptions);

    WIFIState readWIFIState();

    void sendNotification(Notification notification);

    void startDefaultInterfaceMonitor(InterfaceUpdateListener interfaceUpdateListener);

    StringIterator systemCertificates();

    boolean underNetworkExtension();

    boolean usePlatformAutoDetectInterfaceControl();

    boolean useProcFS();
}
