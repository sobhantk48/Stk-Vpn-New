package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class ConnectionEvents implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public ConnectionEvents() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ConnectionEvents) || getReset() != ((ConnectionEvents) obj).getReset()) {
            return false;
        }
        return true;
    }

    public final native boolean getReset();

    public int hashCode() {
        return Arrays.hashCode(new Object[]{Boolean.valueOf(getReset())});
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public native ConnectionEventIterator iterator();

    public final native void setReset(boolean z10);

    public String toString() {
        return "ConnectionEvents{Reset:" + getReset() + ",}";
    }

    public ConnectionEvents(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
