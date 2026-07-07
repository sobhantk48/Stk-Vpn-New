package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class Connections implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public Connections() {
        int __NewConnections = __NewConnections();
        this.refnum = __NewConnections;
        Seq.trackGoRef(__NewConnections, this);
    }

    private static native int __NewConnections();

    public native void applyEvents(ConnectionEvents connectionEvents);

    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof Connections)) {
            Connections connections = (Connections) obj;
            return true;
        }
        return false;
    }

    public native void filterState(int i10);

    public int hashCode() {
        return Arrays.hashCode(new Object[0]);
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public native ConnectionIterator iterator();

    public native void sortByDate();

    public native void sortByTraffic();

    public native void sortByTrafficTotal();

    public String toString() {
        return "Connections{}";
    }

    public Connections(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
