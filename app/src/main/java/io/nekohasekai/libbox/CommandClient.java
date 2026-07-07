package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class CommandClient implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public CommandClient(CommandClientHandler commandClientHandler, CommandClientOptions commandClientOptions) {
        int __NewCommandClient = __NewCommandClient(commandClientHandler, commandClientOptions);
        this.refnum = __NewCommandClient;
        Seq.trackGoRef(__NewCommandClient, this);
    }

    private static native int __NewCommandClient(CommandClientHandler commandClientHandler, CommandClientOptions commandClientOptions);

    public native void clearLogs();

    public native void closeConnection(String str);

    public native void closeConnections();

    public native void connect();

    public native void connectWithFD(int i10);

    public native void disconnect();

    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof CommandClient)) {
            CommandClient commandClient = (CommandClient) obj;
            return true;
        }
        return false;
    }

    public native DeprecatedNoteIterator getDeprecatedNotes();

    public native long getStartedAt();

    public native SystemProxyStatus getSystemProxyStatus();

    public int hashCode() {
        return Arrays.hashCode(new Object[0]);
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public native void selectOutbound(String str, String str2);

    public native void serviceClose();

    public native void serviceReload();

    public native void setClashMode(String str);

    public native void setGroupExpand(String str, boolean z10);

    public native void setSystemProxyEnabled(boolean z10);

    public String toString() {
        return "CommandClient{}";
    }

    public native void urlTest(String str);

    public CommandClient(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
