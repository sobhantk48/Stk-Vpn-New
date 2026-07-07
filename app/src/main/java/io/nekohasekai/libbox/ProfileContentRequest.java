package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class ProfileContentRequest implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public ProfileContentRequest() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public native byte[] encode();

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ProfileContentRequest) || getProfileID() != ((ProfileContentRequest) obj).getProfileID()) {
            return false;
        }
        return true;
    }

    public final native long getProfileID();

    public int hashCode() {
        return Arrays.hashCode(new Object[]{Long.valueOf(getProfileID())});
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public final native void setProfileID(long j6);

    public String toString() {
        return "ProfileContentRequest{ProfileID:" + getProfileID() + ",}";
    }

    public ProfileContentRequest(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
