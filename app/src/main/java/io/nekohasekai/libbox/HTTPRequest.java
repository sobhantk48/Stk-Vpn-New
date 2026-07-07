package io.nekohasekai.libbox;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public interface HTTPRequest {
    HTTPResponse execute();

    void randomUserAgent();

    void setContent(byte[] bArr);

    void setContentString(String str);

    void setHeader(String str, String str2);

    void setMethod(String str);

    void setURL(String str);

    void setUserAgent(String str);
}
