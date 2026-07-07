package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class Connection implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public Connection() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public native StringIterator chain();

    public native String displayDestination();

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Connection)) {
            return false;
        }
        Connection connection = (Connection) obj;
        String id2 = getID();
        String id3 = connection.getID();
        if (id2 == null) {
            if (id3 != null) {
                return false;
            }
        } else if (!id2.equals(id3)) {
            return false;
        }
        String inbound = getInbound();
        String inbound2 = connection.getInbound();
        if (inbound == null) {
            if (inbound2 != null) {
                return false;
            }
        } else if (!inbound.equals(inbound2)) {
            return false;
        }
        String inboundType = getInboundType();
        String inboundType2 = connection.getInboundType();
        if (inboundType == null) {
            if (inboundType2 != null) {
                return false;
            }
        } else if (!inboundType.equals(inboundType2)) {
            return false;
        }
        if (getIPVersion() != connection.getIPVersion()) {
            return false;
        }
        String network = getNetwork();
        String network2 = connection.getNetwork();
        if (network == null) {
            if (network2 != null) {
                return false;
            }
        } else if (!network.equals(network2)) {
            return false;
        }
        String source = getSource();
        String source2 = connection.getSource();
        if (source == null) {
            if (source2 != null) {
                return false;
            }
        } else if (!source.equals(source2)) {
            return false;
        }
        String destination = getDestination();
        String destination2 = connection.getDestination();
        if (destination == null) {
            if (destination2 != null) {
                return false;
            }
        } else if (!destination.equals(destination2)) {
            return false;
        }
        String domain = getDomain();
        String domain2 = connection.getDomain();
        if (domain == null) {
            if (domain2 != null) {
                return false;
            }
        } else if (!domain.equals(domain2)) {
            return false;
        }
        String protocol = getProtocol();
        String protocol2 = connection.getProtocol();
        if (protocol == null) {
            if (protocol2 != null) {
                return false;
            }
        } else if (!protocol.equals(protocol2)) {
            return false;
        }
        String user = getUser();
        String user2 = connection.getUser();
        if (user == null) {
            if (user2 != null) {
                return false;
            }
        } else if (!user.equals(user2)) {
            return false;
        }
        String fromOutbound = getFromOutbound();
        String fromOutbound2 = connection.getFromOutbound();
        if (fromOutbound == null) {
            if (fromOutbound2 != null) {
                return false;
            }
        } else if (!fromOutbound.equals(fromOutbound2)) {
            return false;
        }
        if (getCreatedAt() != connection.getCreatedAt() || getClosedAt() != connection.getClosedAt() || getUplink() != connection.getUplink() || getDownlink() != connection.getDownlink() || getUplinkTotal() != connection.getUplinkTotal() || getDownlinkTotal() != connection.getDownlinkTotal()) {
            return false;
        }
        String rule = getRule();
        String rule2 = connection.getRule();
        if (rule == null) {
            if (rule2 != null) {
                return false;
            }
        } else if (!rule.equals(rule2)) {
            return false;
        }
        String outbound = getOutbound();
        String outbound2 = connection.getOutbound();
        if (outbound == null) {
            if (outbound2 != null) {
                return false;
            }
        } else if (!outbound.equals(outbound2)) {
            return false;
        }
        String outboundType = getOutboundType();
        String outboundType2 = connection.getOutboundType();
        if (outboundType == null) {
            if (outboundType2 != null) {
                return false;
            }
        } else if (!outboundType.equals(outboundType2)) {
            return false;
        }
        ProcessInfo processInfo = getProcessInfo();
        ProcessInfo processInfo2 = connection.getProcessInfo();
        if (processInfo == null) {
            if (processInfo2 != null) {
                return false;
            }
            return true;
        } else if (!processInfo.equals(processInfo2)) {
            return false;
        } else {
            return true;
        }
    }

    public final native long getClosedAt();

    public final native long getCreatedAt();

    public final native String getDestination();

    public final native String getDomain();

    public final native long getDownlink();

    public final native long getDownlinkTotal();

    public final native String getFromOutbound();

    public final native String getID();

    public final native int getIPVersion();

    public final native String getInbound();

    public final native String getInboundType();

    public final native String getNetwork();

    public final native String getOutbound();

    public final native String getOutboundType();

    public final native ProcessInfo getProcessInfo();

    public final native String getProtocol();

    public final native String getRule();

    public final native String getSource();

    public final native long getUplink();

    public final native long getUplinkTotal();

    public final native String getUser();

    public int hashCode() {
        return Arrays.hashCode(new Object[]{getID(), getInbound(), getInboundType(), Integer.valueOf(getIPVersion()), getNetwork(), getSource(), getDestination(), getDomain(), getProtocol(), getUser(), getFromOutbound(), Long.valueOf(getCreatedAt()), Long.valueOf(getClosedAt()), Long.valueOf(getUplink()), Long.valueOf(getDownlink()), Long.valueOf(getUplinkTotal()), Long.valueOf(getDownlinkTotal()), getRule(), getOutbound(), getOutboundType(), getProcessInfo()});
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public final native void setClosedAt(long j6);

    public final native void setCreatedAt(long j6);

    public final native void setDestination(String str);

    public final native void setDomain(String str);

    public final native void setDownlink(long j6);

    public final native void setDownlinkTotal(long j6);

    public final native void setFromOutbound(String str);

    public final native void setID(String str);

    public final native void setIPVersion(int i10);

    public final native void setInbound(String str);

    public final native void setInboundType(String str);

    public final native void setNetwork(String str);

    public final native void setOutbound(String str);

    public final native void setOutboundType(String str);

    public final native void setProcessInfo(ProcessInfo processInfo);

    public final native void setProtocol(String str);

    public final native void setRule(String str);

    public final native void setSource(String str);

    public final native void setUplink(long j6);

    public final native void setUplinkTotal(long j6);

    public final native void setUser(String str);

    public String toString() {
        return "Connection{ID:" + getID() + ",Inbound:" + getInbound() + ",InboundType:" + getInboundType() + ",IPVersion:" + getIPVersion() + ",Network:" + getNetwork() + ",Source:" + getSource() + ",Destination:" + getDestination() + ",Domain:" + getDomain() + ",Protocol:" + getProtocol() + ",User:" + getUser() + ",FromOutbound:" + getFromOutbound() + ",CreatedAt:" + getCreatedAt() + ",ClosedAt:" + getClosedAt() + ",Uplink:" + getUplink() + ",Downlink:" + getDownlink() + ",UplinkTotal:" + getUplinkTotal() + ",DownlinkTotal:" + getDownlinkTotal() + ",Rule:" + getRule() + ",Outbound:" + getOutbound() + ",OutboundType:" + getOutboundType() + ",ProcessInfo:" + getProcessInfo() + ",}";
    }

    public Connection(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
