package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class RoutePrefix implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public RoutePrefix() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public native String address();

    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof RoutePrefix)) {
            RoutePrefix routePrefix = (RoutePrefix) obj;
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

    public native String mask();

    public native int prefix();

    public native String string();

    public String toString() {
        return string();
    }

    public RoutePrefix(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
