package io.nekohasekai.libbox;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public interface CommandClientHandler {
    void clearLogs();

    void connected();

    void disconnected(String str);

    void initializeClashMode(StringIterator stringIterator, String str);

    void setDefaultLogLevel(int i10);

    void updateClashMode(String str);

    void writeConnectionEvents(ConnectionEvents connectionEvents);

    void writeGroups(OutboundGroupIterator outboundGroupIterator);

    void writeLogs(LogIterator logIterator);

    void writeStatus(StatusMessage statusMessage);
}
