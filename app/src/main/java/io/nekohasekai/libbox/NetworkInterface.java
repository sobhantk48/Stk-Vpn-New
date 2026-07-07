package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class NetworkInterface implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public NetworkInterface() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof NetworkInterface)) {
            return false;
        }
        NetworkInterface networkInterface = (NetworkInterface) obj;
        if (getIndex() != networkInterface.getIndex() || getMTU() != networkInterface.getMTU()) {
            return false;
        }
        String name = getName();
        String name2 = networkInterface.getName();
        if (name == null) {
            if (name2 != null) {
                return false;
            }
        } else if (!name.equals(name2)) {
            return false;
        }
        StringIterator addresses = getAddresses();
        StringIterator addresses2 = networkInterface.getAddresses();
        if (addresses == null) {
            if (addresses2 != null) {
                return false;
            }
        } else if (!addresses.equals(addresses2)) {
            return false;
        }
        if (getFlags() != networkInterface.getFlags() || getType() != networkInterface.getType()) {
            return false;
        }
        StringIterator dNSServer = getDNSServer();
        StringIterator dNSServer2 = networkInterface.getDNSServer();
        if (dNSServer == null) {
            if (dNSServer2 != null) {
                return false;
            }
        } else if (!dNSServer.equals(dNSServer2)) {
            return false;
        }
        if (getMetered() != networkInterface.getMetered()) {
            return false;
        }
        return true;
    }

    public final native StringIterator getAddresses();

    public final native StringIterator getDNSServer();

    public final native int getFlags();

    public final native int getIndex();

    public final native int getMTU();

    public final native boolean getMetered();

    public final native String getName();

    public final native int getType();

    public int hashCode() {
        return Arrays.hashCode(new Object[]{Integer.valueOf(getIndex()), Integer.valueOf(getMTU()), getName(), getAddresses(), Integer.valueOf(getFlags()), Integer.valueOf(getType()), getDNSServer(), Boolean.valueOf(getMetered())});
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public final native void setAddresses(StringIterator stringIterator);

    public final native void setDNSServer(StringIterator stringIterator);

    public final native void setFlags(int i10);

    public final native void setIndex(int i10);

    public final native void setMTU(int i10);

    public final native void setMetered(boolean z10);

    public final native void setName(String str);

    public final native void setType(int i10);

    public String toString() {
        return "NetworkInterface{Index:" + getIndex() + ",MTU:" + getMTU() + ",Name:" + getName() + ",Addresses:" + getAddresses() + ",Flags:" + getFlags() + ",Type:" + getType() + ",DNSServer:" + getDNSServer() + ",Metered:" + getMetered() + ",}";
    }

    public NetworkInterface(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
