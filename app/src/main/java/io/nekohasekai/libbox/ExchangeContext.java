package io.nekohasekai.libbox;

import go.Seq;
import java.util.Arrays;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public final class ExchangeContext implements Seq.Proxy {
    public final int refnum;

    static {
        Libbox.touch();
    }

    public ExchangeContext() {
        int __New = __New();
        this.refnum = __New;
        Seq.trackGoRef(__New, this);
    }

    private static native int __New();

    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof ExchangeContext)) {
            ExchangeContext exchangeContext = (ExchangeContext) obj;
            return true;
        }
        return false;
    }

    public native void errnoCode(int i10);

    public native void errorCode(int i10);

    public int hashCode() {
        return Arrays.hashCode(new Object[0]);
    }

    @Override // go.Seq.GoObject
    public final int incRefnum() {
        Seq.incGoRef(this.refnum, this);
        return this.refnum;
    }

    public native void onCancel(Func func);

    public native void rawSuccess(byte[] bArr);

    public native void success(String str);

    public String toString() {
        return "ExchangeContext{}";
    }

    public ExchangeContext(int i10) {
        this.refnum = i10;
        Seq.trackGoRef(i10, this);
    }
}
