package io.nekohasekai.libbox;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public interface HTTPClient {
    void close();

    void keepAlive();

    void modernTLS();

    HTTPRequest newRequest();

    void pinnedSHA256(String str);

    void pinnedTLS12();

    void restrictedTLS();

    void trySocks5(int i10);
}
