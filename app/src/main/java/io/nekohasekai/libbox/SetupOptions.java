package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class SetupOptions implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public SetupOptions() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof SetupOptions)) {
            return false;
        }
        SetupOptions setupOptions = (SetupOptions) obj;
        String basePath = getBasePath();
        String basePath2 = setupOptions.getBasePath();
        if (basePath == null) {
            if (basePath2 != null) {
                return false;
            }
        } else if (!basePath.equals(basePath2)) {
            return false;
        }
        String workingPath = getWorkingPath();
        String workingPath2 = setupOptions.getWorkingPath();
        if (workingPath == null) {
            if (workingPath2 != null) {
                return false;
            }
        } else if (!workingPath.equals(workingPath2)) {
            return false;
        }
        String tempPath = getTempPath();
        String tempPath2 = setupOptions.getTempPath();
        if (tempPath == null) {
            if (tempPath2 != null) {
                return false;
            }
        } else if (!tempPath.equals(tempPath2)) {
            return false;
        }
        if (getFixAndroidStack() != setupOptions.getFixAndroidStack() || getCommandServerListenPort() != setupOptions.getCommandServerListenPort()) {
            return false;
        }
        String commandServerSecret = getCommandServerSecret();
        String commandServerSecret2 = setupOptions.getCommandServerSecret();
        if (commandServerSecret == null) {
            if (commandServerSecret2 != null) {
                return false;
            }
        } else if (!commandServerSecret.equals(commandServerSecret2)) {
            return false;
        }
        if (getLogMaxLines() != setupOptions.getLogMaxLines() || getDebug() != setupOptions.getDebug()) {
            return false;
        }
        return true;
    }

    public final native String getBasePath();

    public final native int getCommandServerListenPort();

    public final native String getCommandServerSecret();

    public final native boolean getDebug();

    public final native boolean getFixAndroidStack();

    public final native long getLogMaxLines();

    public final native String getTempPath();

    public final native String getWorkingPath();

    public int hashCode() {
        return Arrays.hashCode(new Object[]{getBasePath(), getWorkingPath(), getTempPath(), Boolean.valueOf(getFixAndroidStack()), Integer.valueOf(getCommandServerListenPort()), getCommandServerSecret(), Long.valueOf(getLogMaxLines()), Boolean.valueOf(getDebug())});
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public final native void setBasePath(String str);

    public final native void setCommandServerListenPort(int i10);

    public final native void setCommandServerSecret(String str);

    public final native void setDebug(boolean z10);

    public final native void setFixAndroidStack(boolean z10);

    public final native void setLogMaxLines(long j6);

    public final native void setTempPath(String str);

    public final native void setWorkingPath(String str);

    public String toString() {
        return "SetupOptions{BasePath:" + getBasePath() + ",WorkingPath:" + getWorkingPath() + ",TempPath:" + getTempPath() + ",FixAndroidStack:" + getFixAndroidStack() + ",CommandServerListenPort:" + getCommandServerListenPort() + ",CommandServerSecret:" + getCommandServerSecret() + ",LogMaxLines:" + getLogMaxLines() + ",Debug:" + getDebug() + ",}";
    }

    public SetupOptions(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
