package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class ProfileContent implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public ProfileContent() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public native byte[] encode();

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ProfileContent)) {
            return false;
        }
        ProfileContent profileContent = (ProfileContent) obj;
        String name = getName();
        String name2 = profileContent.getName();
        if (name == null) {
            if (name2 != null) {
                return false;
            }
        } else if (!name.equals(name2)) {
            return false;
        }
        if (getType() != profileContent.getType()) {
            return false;
        }
        String config = getConfig();
        String config2 = profileContent.getConfig();
        if (config == null) {
            if (config2 != null) {
                return false;
            }
        } else if (!config.equals(config2)) {
            return false;
        }
        String remotePath = getRemotePath();
        String remotePath2 = profileContent.getRemotePath();
        if (remotePath == null) {
            if (remotePath2 != null) {
                return false;
            }
        } else if (!remotePath.equals(remotePath2)) {
            return false;
        }
        if (getAutoUpdate() != profileContent.getAutoUpdate() || getAutoUpdateInterval() != profileContent.getAutoUpdateInterval() || getLastUpdated() != profileContent.getLastUpdated()) {
            return false;
        }
        return true;
    }

    public final native boolean getAutoUpdate();

    public final native int getAutoUpdateInterval();

    public final native String getConfig();

    public final native long getLastUpdated();

    public final native String getName();

    public final native String getRemotePath();

    public final native int getType();

    public int hashCode() {
        return Arrays.hashCode(new Object[]{getName(), Integer.valueOf(getType()), getConfig(), getRemotePath(), Boolean.valueOf(getAutoUpdate()), Integer.valueOf(getAutoUpdateInterval()), Long.valueOf(getLastUpdated())});
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public final native void setAutoUpdate(boolean z10);

    public final native void setAutoUpdateInterval(int i10);

    public final native void setConfig(String str);

    public final native void setLastUpdated(long j6);

    public final native void setName(String str);

    public final native void setRemotePath(String str);

    public final native void setType(int i10);

    public String toString() {
        return "ProfileContent{Name:" + getName() + ",Type:" + getType() + ",Config:" + getConfig() + ",RemotePath:" + getRemotePath() + ",AutoUpdate:" + getAutoUpdate() + ",AutoUpdateInterval:" + getAutoUpdateInterval() + ",LastUpdated:" + getLastUpdated() + ",}";
    }

    public ProfileContent(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
