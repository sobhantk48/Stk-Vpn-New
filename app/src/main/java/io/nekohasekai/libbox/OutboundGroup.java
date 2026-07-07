package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class OutboundGroup implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public OutboundGroup() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof OutboundGroup)) {
            return false;
        }
        OutboundGroup outboundGroup = (OutboundGroup) obj;
        String tag = getTag();
        String tag2 = outboundGroup.getTag();
        if (tag == null) {
            if (tag2 != null) {
                return false;
            }
        } else if (!tag.equals(tag2)) {
            return false;
        }
        String type = getType();
        String type2 = outboundGroup.getType();
        if (type == null) {
            if (type2 != null) {
                return false;
            }
        } else if (!type.equals(type2)) {
            return false;
        }
        if (getSelectable() != outboundGroup.getSelectable()) {
            return false;
        }
        String selected = getSelected();
        String selected2 = outboundGroup.getSelected();
        if (selected == null) {
            if (selected2 != null) {
                return false;
            }
        } else if (!selected.equals(selected2)) {
            return false;
        }
        if (getIsExpand() != outboundGroup.getIsExpand()) {
            return false;
        }
        return true;
    }

    public final native boolean getIsExpand();

    public native OutboundGroupItemIterator getItems();

    public final native boolean getSelectable();

    public final native String getSelected();

    public final native String getTag();

    public final native String getType();

    public int hashCode() {
        return Arrays.hashCode(new Object[]{getTag(), getType(), Boolean.valueOf(getSelectable()), getSelected(), Boolean.valueOf(getIsExpand())});
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public final native void setIsExpand(boolean z10);

    public final native void setSelectable(boolean z10);

    public final native void setSelected(String str);

    public final native void setTag(String str);

    public final native void setType(String str);

    public String toString() {
        return "OutboundGroup{Tag:" + getTag() + ",Type:" + getType() + ",Selectable:" + getSelectable() + ",Selected:" + getSelected() + ",IsExpand:" + getIsExpand() + ",}";
    }

    public OutboundGroup(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
