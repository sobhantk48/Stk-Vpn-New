package io.nekohasekai.libbox;

public class Libbox {
    static {
        System.loadLibrary("box");
    }

    public static native BoxInstance newBoxInstance(String config, LocalDNSTransport transport, PlatformInterface platform);
}
