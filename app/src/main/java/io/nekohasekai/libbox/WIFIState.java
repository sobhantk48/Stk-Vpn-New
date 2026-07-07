package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class WIFIState implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public WIFIState(String str, String str2) {
        int __NewWIFIState = __NewWIFIState(str, str2);
        this.refnum = __NewWIFIState;
        Seq.trackGoRef(__NewWIFIState, this);
    }

    private static native int __NewWIFIState(String str, String str2);

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof WIFIState)) {
            return false;
        }
        WIFIState wIFIState = (WIFIState) obj;
        String ssid = getSSID();
        String ssid2 = wIFIState.getSSID();
        if (ssid == null) {
            if (ssid2 != null) {
                return false;
            }
        } else if (!ssid.equals(ssid2)) {
            return false;
        }
        String bssid = getBSSID();
        String bssid2 = wIFIState.getBSSID();
        if (bssid == null) {
            if (bssid2 != null) {
                return false;
            }
            return true;
        } else if (!bssid.equals(bssid2)) {
            return false;
        } else {
            return true;
        }
    }

    public final native String getBSSID();

    public final native String getSSID();

    public int hashCode() {
        return Arrays.hashCode(new Object[]{getSSID(), getBSSID()});
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public final native void setBSSID(String str);

    public final native void setSSID(String str);

    public String toString() {
        return "WIFIState{SSID:" + getSSID() + ",BSSID:" + getBSSID() + ",}";
    }

    public WIFIState(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
