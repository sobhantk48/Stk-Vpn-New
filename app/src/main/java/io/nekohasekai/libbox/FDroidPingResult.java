package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class FDroidPingResult implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public FDroidPingResult() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof FDroidPingResult)) {
            return false;
        }
        FDroidPingResult fDroidPingResult = (FDroidPingResult) obj;
        String url = getURL();
        String url2 = fDroidPingResult.getURL();
        if (url == null) {
            if (url2 != null) {
                return false;
            }
        } else if (!url.equals(url2)) {
            return false;
        }
        if (getLatencyMs() != fDroidPingResult.getLatencyMs()) {
            return false;
        }
        String error = getError();
        String error2 = fDroidPingResult.getError();
        if (error == null) {
            if (error2 != null) {
                return false;
            }
            return true;
        } else if (!error.equals(error2)) {
            return false;
        } else {
            return true;
        }
    }

    public final native String getError();

    public final native int getLatencyMs();

    public final native String getURL();

    public int hashCode() {
        return Arrays.hashCode(new Object[]{getURL(), Integer.valueOf(getLatencyMs()), getError()});
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public final native void setError(String str);

    public final native void setLatencyMs(int i10);

    public final native void setURL(String str);

    public String toString() {
        return "FDroidPingResult{URL:" + getURL() + ",LatencyMs:" + getLatencyMs() + ",Error:" + getError() + ",}";
    }

    public FDroidPingResult(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
