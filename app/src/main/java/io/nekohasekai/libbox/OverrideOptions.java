package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class OverrideOptions implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public OverrideOptions() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof OverrideOptions)) {
            return false;
        }
        OverrideOptions overrideOptions = (OverrideOptions) obj;
        if (getAutoRedirect() != overrideOptions.getAutoRedirect()) {
            return false;
        }
        StringIterator includePackage = getIncludePackage();
        StringIterator includePackage2 = overrideOptions.getIncludePackage();
        if (includePackage == null) {
            if (includePackage2 != null) {
                return false;
            }
        } else if (!includePackage.equals(includePackage2)) {
            return false;
        }
        StringIterator excludePackage = getExcludePackage();
        StringIterator excludePackage2 = overrideOptions.getExcludePackage();
        if (excludePackage == null) {
            if (excludePackage2 != null) {
                return false;
            }
            return true;
        } else if (!excludePackage.equals(excludePackage2)) {
            return false;
        } else {
            return true;
        }
    }

    public final native boolean getAutoRedirect();

    public final native StringIterator getExcludePackage();

    public final native StringIterator getIncludePackage();

    public int hashCode() {
        return Arrays.hashCode(new Object[]{Boolean.valueOf(getAutoRedirect()), getIncludePackage(), getExcludePackage()});
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public final native void setAutoRedirect(boolean z10);

    public final native void setExcludePackage(StringIterator stringIterator);

    public final native void setIncludePackage(StringIterator stringIterator);

    public String toString() {
        return "OverrideOptions{AutoRedirect:" + getAutoRedirect() + ",IncludePackage:" + getIncludePackage() + ",ExcludePackage:" + getExcludePackage() + ",}";
    }

    public OverrideOptions(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
