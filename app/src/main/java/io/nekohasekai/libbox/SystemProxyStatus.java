package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class SystemProxyStatus implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public SystemProxyStatus() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof SystemProxyStatus)) {
            return false;
        }
        SystemProxyStatus systemProxyStatus = (SystemProxyStatus) obj;
        if (getAvailable() != systemProxyStatus.getAvailable() || getEnabled() != systemProxyStatus.getEnabled()) {
            return false;
        }
        return true;
    }

    public final native boolean getAvailable();

    public final native boolean getEnabled();

    public int hashCode() {
        return Arrays.hashCode(new Object[]{Boolean.valueOf(getAvailable()), Boolean.valueOf(getEnabled())});
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public final native void setAvailable(boolean z10);

    public final native void setEnabled(boolean z10);

    public String toString() {
        return "SystemProxyStatus{Available:" + getAvailable() + ",Enabled:" + getEnabled() + ",}";
    }

    public SystemProxyStatus(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
