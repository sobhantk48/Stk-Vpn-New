package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class OutboundGroupItem implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public OutboundGroupItem() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof OutboundGroupItem)) {
            return false;
        }
        OutboundGroupItem outboundGroupItem = (OutboundGroupItem) obj;
        String tag = getTag();
        String tag2 = outboundGroupItem.getTag();
        if (tag == null) {
            if (tag2 != null) {
                return false;
            }
        } else if (!tag.equals(tag2)) {
            return false;
        }
        String type = getType();
        String type2 = outboundGroupItem.getType();
        if (type == null) {
            if (type2 != null) {
                return false;
            }
        } else if (!type.equals(type2)) {
            return false;
        }
        if (getURLTestTime() != outboundGroupItem.getURLTestTime() || getURLTestDelay() != outboundGroupItem.getURLTestDelay()) {
            return false;
        }
        return true;
    }

    public final native String getTag();

    public final native String getType();

    public final native int getURLTestDelay();

    public final native long getURLTestTime();

    public int hashCode() {
        return Arrays.hashCode(new Object[]{getTag(), getType(), Long.valueOf(getURLTestTime()), Integer.valueOf(getURLTestDelay())});
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public final native void setTag(String str);

    public final native void setType(String str);

    public final native void setURLTestDelay(int i10);

    public final native void setURLTestTime(long j6);

    public String toString() {
        return "OutboundGroupItem{Tag:" + getTag() + ",Type:" + getType() + ",URLTestTime:" + getURLTestTime() + ",URLTestDelay:" + getURLTestDelay() + ",}";
    }

    public OutboundGroupItem(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
