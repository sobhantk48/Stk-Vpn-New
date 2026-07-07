package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class ProfilePreview implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public ProfilePreview() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ProfilePreview)) {
            return false;
        }
        ProfilePreview profilePreview = (ProfilePreview) obj;
        if (getProfileID() != profilePreview.getProfileID()) {
            return false;
        }
        String name = getName();
        String name2 = profilePreview.getName();
        if (name == null) {
            if (name2 != null) {
                return false;
            }
        } else if (!name.equals(name2)) {
            return false;
        }
        if (getType() != profilePreview.getType()) {
            return false;
        }
        return true;
    }

    public final native String getName();

    public final native long getProfileID();

    public final native int getType();

    public int hashCode() {
        return Arrays.hashCode(new Object[]{Long.valueOf(getProfileID()), getName(), Integer.valueOf(getType())});
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public final native void setName(String str);

    public final native void setProfileID(long j6);

    public final native void setType(int i10);

    public String toString() {
        return "ProfilePreview{ProfileID:" + getProfileID() + ",Name:" + getName() + ",Type:" + getType() + ",}";
    }

    public ProfilePreview(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
