package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class ConnectionOwner implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public ConnectionOwner() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public native StringIterator androidPackageNames();

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ConnectionOwner)) {
            return false;
        }
        ConnectionOwner connectionOwner = (ConnectionOwner) obj;
        if (getUserId() != connectionOwner.getUserId()) {
            return false;
        }
        String userName = getUserName();
        String userName2 = connectionOwner.getUserName();
        if (userName == null) {
            if (userName2 != null) {
                return false;
            }
        } else if (!userName.equals(userName2)) {
            return false;
        }
        String processPath = getProcessPath();
        String processPath2 = connectionOwner.getProcessPath();
        if (processPath == null) {
            if (processPath2 != null) {
                return false;
            }
            return true;
        } else if (!processPath.equals(processPath2)) {
            return false;
        } else {
            return true;
        }
    }

    public final native String getProcessPath();

    public final native int getUserId();

    public final native String getUserName();

    public int hashCode() {
        return Arrays.hashCode(new Object[]{Integer.valueOf(getUserId()), getUserName(), getProcessPath()});
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public native void setAndroidPackageNames(StringIterator stringIterator);

    public final native void setProcessPath(String str);

    public final native void setUserId(int i10);

    public final native void setUserName(String str);

    public String toString() {
        return "ConnectionOwner{UserId:" + getUserId() + ",UserName:" + getUserName() + ",ProcessPath:" + getProcessPath() + ",}";
    }

    public ConnectionOwner(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
