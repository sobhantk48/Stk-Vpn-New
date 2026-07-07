package io.nekohasekai.libbox;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public interface LocalDNSTransport {
    void exchange(ExchangeContext exchangeContext, byte[] bArr);

    void lookup(ExchangeContext exchangeContext, String str, String str2);

    boolean raw();
}
