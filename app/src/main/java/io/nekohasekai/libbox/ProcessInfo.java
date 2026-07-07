package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class ProcessInfo implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public ProcessInfo() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ProcessInfo)) {
            return false;
        }
        ProcessInfo processInfo = (ProcessInfo) obj;
        if (getProcessID() != processInfo.getProcessID() || getUserID() != processInfo.getUserID()) {
            return false;
        }
        String userName = getUserName();
        String userName2 = processInfo.getUserName();
        if (userName == null) {
            if (userName2 != null) {
                return false;
            }
        } else if (!userName.equals(userName2)) {
            return false;
        }
        String processPath = getProcessPath();
        String processPath2 = processInfo.getProcessPath();
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

    public final native long getProcessID();

    public final native String getProcessPath();

    public final native int getUserID();

    public final native String getUserName();

    public int hashCode() {
        return Arrays.hashCode(new Object[]{Long.valueOf(getProcessID()), Integer.valueOf(getUserID()), getUserName(), getProcessPath()});
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public native StringIterator packageNames();

    public final native void setProcessID(long j6);

    public final native void setProcessPath(String str);

    public final native void setUserID(int i10);

    public final native void setUserName(String str);

    public String toString() {
        return "ProcessInfo{ProcessID:" + getProcessID() + ",UserID:" + getUserID() + ",UserName:" + getUserName() + ",ProcessPath:" + getProcessPath() + ",}";
    }

    public ProcessInfo(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
