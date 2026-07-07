package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class StringBox implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public StringBox() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof StringBox)) {
            return false;
        }
        String value = getValue();
        String value2 = ((StringBox) obj).getValue();
        if (value == null) {
            if (value2 != null) {
                return false;
            }
            return true;
        } else if (!value.equals(value2)) {
            return false;
        } else {
            return true;
        }
    }

    public final native String getValue();

    public int hashCode() {
        return Arrays.hashCode(new Object[]{getValue()});
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public final native void setValue(String str);

    public String toString() {
        return "StringBox{Value:" + getValue() + ",}";
    }

    public StringBox(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
