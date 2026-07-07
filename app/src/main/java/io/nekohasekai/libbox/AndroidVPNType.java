package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class AndroidVPNType implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public AndroidVPNType() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AndroidVPNType)) {
            return false;
        }
        AndroidVPNType androidVPNType = (AndroidVPNType) obj;
        String coreType = getCoreType();
        String coreType2 = androidVPNType.getCoreType();
        if (coreType == null) {
            if (coreType2 != null) {
                return false;
            }
        } else if (!coreType.equals(coreType2)) {
            return false;
        }
        String corePath = getCorePath();
        String corePath2 = androidVPNType.getCorePath();
        if (corePath == null) {
            if (corePath2 != null) {
                return false;
            }
        } else if (!corePath.equals(corePath2)) {
            return false;
        }
        String goVersion = getGoVersion();
        String goVersion2 = androidVPNType.getGoVersion();
        if (goVersion == null) {
            if (goVersion2 != null) {
                return false;
            }
            return true;
        } else if (!goVersion.equals(goVersion2)) {
            return false;
        } else {
            return true;
        }
    }

    public final native String getCorePath();

    public final native String getCoreType();

    public final native String getGoVersion();

    public int hashCode() {
        return Arrays.hashCode(new Object[]{getCoreType(), getCorePath(), getGoVersion()});
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public final native void setCorePath(String str);

    public final native void setCoreType(String str);

    public final native void setGoVersion(String str);

    public String toString() {
        return "AndroidVPNType{CoreType:" + getCoreType() + ",CorePath:" + getCorePath() + ",GoVersion:" + getGoVersion() + ",}";
    }

    public AndroidVPNType(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
