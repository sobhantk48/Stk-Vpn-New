package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class CommandServer implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public CommandServer(CommandServerHandler commandServerHandler, PlatformInterface platformInterface) {
        int __NewCommandServer = __NewCommandServer(commandServerHandler, platformInterface);
        this.refnum = __NewCommandServer;
        Seq.trackGoRef(__NewCommandServer, this);
    }

    private static native int __NewCommandServer(CommandServerHandler commandServerHandler, PlatformInterface platformInterface);

    public native void checkConfig(String str);

    public native void close();

    public native void closeService();

    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof CommandServer)) {
            CommandServer commandServer = (CommandServer) obj;
            return true;
        }
        return false;
    }

    public native String formatConfig(String str);

    public int hashCode() {
        return Arrays.hashCode(new Object[0]);
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public native boolean needFindProcess();

    public native boolean needWIFIState();

    public native void pause();

    public native void resetNetwork();

    public native void setError(String str);

    public native void start();

    public native void startOrReloadService(String str, OverrideOptions overrideOptions);

    public String toString() {
        return "CommandServer{}";
    }

    public native void updateWIFIState();

    public native void wake();

    public native void writeMessage(int i10, String str);

    public CommandServer(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
