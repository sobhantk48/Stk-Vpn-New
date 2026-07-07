package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class FDroidUpdateInfo implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public FDroidUpdateInfo() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof FDroidUpdateInfo)) {
            return false;
        }
        FDroidUpdateInfo fDroidUpdateInfo = (FDroidUpdateInfo) obj;
        if (getVersionCode() != fDroidUpdateInfo.getVersionCode()) {
            return false;
        }
        String versionName = getVersionName();
        String versionName2 = fDroidUpdateInfo.getVersionName();
        if (versionName == null) {
            if (versionName2 != null) {
                return false;
            }
        } else if (!versionName.equals(versionName2)) {
            return false;
        }
        String downloadURL = getDownloadURL();
        String downloadURL2 = fDroidUpdateInfo.getDownloadURL();
        if (downloadURL == null) {
            if (downloadURL2 != null) {
                return false;
            }
        } else if (!downloadURL.equals(downloadURL2)) {
            return false;
        }
        if (getFileSize() != fDroidUpdateInfo.getFileSize()) {
            return false;
        }
        String fileSHA256 = getFileSHA256();
        String fileSHA2562 = fDroidUpdateInfo.getFileSHA256();
        if (fileSHA256 == null) {
            if (fileSHA2562 != null) {
                return false;
            }
            return true;
        } else if (!fileSHA256.equals(fileSHA2562)) {
            return false;
        } else {
            return true;
        }
    }

    public final native String getDownloadURL();

    public final native String getFileSHA256();

    public final native long getFileSize();

    public final native int getVersionCode();

    public final native String getVersionName();

    public int hashCode() {
        return Arrays.hashCode(new Object[]{Integer.valueOf(getVersionCode()), getVersionName(), getDownloadURL(), Long.valueOf(getFileSize()), getFileSHA256()});
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public final native void setDownloadURL(String str);

    public final native void setFileSHA256(String str);

    public final native void setFileSize(long j6);

    public final native void setVersionCode(int i10);

    public final native void setVersionName(String str);

    public String toString() {
        return "FDroidUpdateInfo{VersionCode:" + getVersionCode() + ",VersionName:" + getVersionName() + ",DownloadURL:" + getDownloadURL() + ",FileSize:" + getFileSize() + ",FileSHA256:" + getFileSHA256() + ",}";
    }

    public FDroidUpdateInfo(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
