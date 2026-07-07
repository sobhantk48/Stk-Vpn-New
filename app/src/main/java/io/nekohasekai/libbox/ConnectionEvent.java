package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class ConnectionEvent implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public ConnectionEvent() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ConnectionEvent)) {
            return false;
        }
        ConnectionEvent connectionEvent = (ConnectionEvent) obj;
        if (getType() != connectionEvent.getType()) {
            return false;
        }
        String id2 = getID();
        String id3 = connectionEvent.getID();
        if (id2 == null) {
            if (id3 != null) {
                return false;
            }
        } else if (!id2.equals(id3)) {
            return false;
        }
        Connection connection = getConnection();
        Connection connection2 = connectionEvent.getConnection();
        if (connection == null) {
            if (connection2 != null) {
                return false;
            }
        } else if (!connection.equals(connection2)) {
            return false;
        }
        if (getUplinkDelta() != connectionEvent.getUplinkDelta() || getDownlinkDelta() != connectionEvent.getDownlinkDelta() || getClosedAt() != connectionEvent.getClosedAt()) {
            return false;
        }
        return true;
    }

    public final native long getClosedAt();

    public final native Connection getConnection();

    public final native long getDownlinkDelta();

    public final native String getID();

    public final native int getType();

    public final native long getUplinkDelta();

    public int hashCode() {
        return Arrays.hashCode(new Object[]{Integer.valueOf(getType()), getID(), getConnection(), Long.valueOf(getUplinkDelta()), Long.valueOf(getDownlinkDelta()), Long.valueOf(getClosedAt())});
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public final native void setClosedAt(long j6);

    public final native void setConnection(Connection connection);

    public final native void setDownlinkDelta(long j6);

    public final native void setID(String str);

    public final native void setType(int i10);

    public final native void setUplinkDelta(long j6);

    public String toString() {
        return "ConnectionEvent{Type:" + getType() + ",ID:" + getID() + ",Connection:" + getConnection() + ",UplinkDelta:" + getUplinkDelta() + ",DownlinkDelta:" + getDownlinkDelta() + ",ClosedAt:" + getClosedAt() + ",}";
    }

    public ConnectionEvent(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
