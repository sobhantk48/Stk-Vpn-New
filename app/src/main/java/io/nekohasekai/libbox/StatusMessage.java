package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class StatusMessage implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public StatusMessage() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof StatusMessage)) {
            return false;
        }
        StatusMessage statusMessage = (StatusMessage) obj;
        if (getMemory() != statusMessage.getMemory() || getGoroutines() != statusMessage.getGoroutines() || getConnectionsIn() != statusMessage.getConnectionsIn() || getConnectionsOut() != statusMessage.getConnectionsOut() || getTrafficAvailable() != statusMessage.getTrafficAvailable() || getUplink() != statusMessage.getUplink() || getDownlink() != statusMessage.getDownlink() || getUplinkTotal() != statusMessage.getUplinkTotal() || getDownlinkTotal() != statusMessage.getDownlinkTotal()) {
            return false;
        }
        return true;
    }

    public final native int getConnectionsIn();

    public final native int getConnectionsOut();

    public final native long getDownlink();

    public final native long getDownlinkTotal();

    public final native int getGoroutines();

    public final native long getMemory();

    public final native boolean getTrafficAvailable();

    public final native long getUplink();

    public final native long getUplinkTotal();

    public int hashCode() {
        return Arrays.hashCode(new Object[]{Long.valueOf(getMemory()), Integer.valueOf(getGoroutines()), Integer.valueOf(getConnectionsIn()), Integer.valueOf(getConnectionsOut()), Boolean.valueOf(getTrafficAvailable()), Long.valueOf(getUplink()), Long.valueOf(getDownlink()), Long.valueOf(getUplinkTotal()), Long.valueOf(getDownlinkTotal())});
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public final native void setConnectionsIn(int i10);

    public final native void setConnectionsOut(int i10);

    public final native void setDownlink(long j6);

    public final native void setDownlinkTotal(long j6);

    public final native void setGoroutines(int i10);

    public final native void setMemory(long j6);

    public final native void setTrafficAvailable(boolean z10);

    public final native void setUplink(long j6);

    public final native void setUplinkTotal(long j6);

    public String toString() {
        return "StatusMessage{Memory:" + getMemory() + ",Goroutines:" + getGoroutines() + ",ConnectionsIn:" + getConnectionsIn() + ",ConnectionsOut:" + getConnectionsOut() + ",TrafficAvailable:" + getTrafficAvailable() + ",Uplink:" + getUplink() + ",Downlink:" + getDownlink() + ",UplinkTotal:" + getUplinkTotal() + ",DownlinkTotal:" + getDownlinkTotal() + ",}";
    }

    public StatusMessage(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
