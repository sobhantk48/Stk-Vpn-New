package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class PProfServer implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public PProfServer(long j6) {
        int __NewPProfServer = __NewPProfServer(j6);
        this.refnum = __NewPProfServer;
        Seq.trackGoRef(__NewPProfServer, this);
    }

    private static native int __NewPProfServer(long j6);

    public native void close();

    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof PProfServer)) {
            PProfServer pProfServer = (PProfServer) obj;
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Arrays.hashCode(new Object[0]);
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public native void start();

    public String toString() {
        return "PProfServer{}";
    }

    public PProfServer(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
