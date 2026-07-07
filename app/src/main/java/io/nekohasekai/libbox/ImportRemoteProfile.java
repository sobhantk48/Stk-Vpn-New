package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class ImportRemoteProfile implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public ImportRemoteProfile() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ImportRemoteProfile)) {
            return false;
        }
        ImportRemoteProfile importRemoteProfile = (ImportRemoteProfile) obj;
        String name = getName();
        String name2 = importRemoteProfile.getName();
        if (name == null) {
            if (name2 != null) {
                return false;
            }
        } else if (!name.equals(name2)) {
            return false;
        }
        String url = getURL();
        String url2 = importRemoteProfile.getURL();
        if (url == null) {
            if (url2 != null) {
                return false;
            }
        } else if (!url.equals(url2)) {
            return false;
        }
        String host = getHost();
        String host2 = importRemoteProfile.getHost();
        if (host == null) {
            if (host2 != null) {
                return false;
            }
            return true;
        } else if (!host.equals(host2)) {
            return false;
        } else {
            return true;
        }
    }

    public final native String getHost();

    public final native String getName();

    public final native String getURL();

    public int hashCode() {
        return Arrays.hashCode(new Object[]{getName(), getURL(), getHost()});
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public final native void setHost(String str);

    public final native void setName(String str);

    public final native void setURL(String str);

    public String toString() {
        return "ImportRemoteProfile{Name:" + getName() + ",URL:" + getURL() + ",Host:" + getHost() + ",}";
    }

    public ImportRemoteProfile(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
