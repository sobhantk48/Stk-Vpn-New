package io.nekohasekai.libbox;

import go.Seq;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public abstract class Libbox {
    public static final int CommandClashMode = 3;
    public static final int CommandConnections = 4;
    public static final int CommandGroup = 2;
    public static final int CommandLog = 0;
    public static final int CommandStatus = 1;
    public static final long ConnectionEventClosed = 2;
    public static final long ConnectionEventNew = 0;
    public static final long ConnectionEventUpdate = 1;
    public static final long ConnectionStateActive = 1;
    public static final long ConnectionStateAll = 0;
    public static final long ConnectionStateClosed = 2;
    public static final int InterfaceTypeCellular = 1;
    public static final int InterfaceTypeEthernet = 2;
    public static final int InterfaceTypeOther = 3;
    public static final int InterfaceTypeWIFI = 0;
    public static final long MessageTypeError = 0;
    public static final long MessageTypeProfileContent = 3;
    public static final long MessageTypeProfileContentRequest = 2;
    public static final long MessageTypeProfileList = 1;
    public static final int ProfileTypeLocal = 0;
    public static final int ProfileTypeRemote = 2;
    public static final int ProfileTypeiCloud = 1;

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyCommandClientHandler implements Seq.Proxy, CommandClientHandler {
        public final int refnum;

        public proxyCommandClientHandler(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.CommandClientHandler
        public native void clearLogs();

        @Override // io.nekohasekai.libbox.CommandClientHandler
        public native void connected();

        @Override // io.nekohasekai.libbox.CommandClientHandler
        public native void disconnected(String str);

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.CommandClientHandler
        public native void initializeClashMode(StringIterator stringIterator, String str);

        @Override // io.nekohasekai.libbox.CommandClientHandler
        public native void setDefaultLogLevel(int i10);

        @Override // io.nekohasekai.libbox.CommandClientHandler
        public native void updateClashMode(String str);

        @Override // io.nekohasekai.libbox.CommandClientHandler
        public native void writeConnectionEvents(ConnectionEvents connectionEvents);

        @Override // io.nekohasekai.libbox.CommandClientHandler
        public native void writeGroups(OutboundGroupIterator outboundGroupIterator);

        @Override // io.nekohasekai.libbox.CommandClientHandler
        public native void writeLogs(LogIterator logIterator);

        @Override // io.nekohasekai.libbox.CommandClientHandler
        public native void writeStatus(StatusMessage statusMessage);
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyCommandServerHandler implements Seq.Proxy, CommandServerHandler {
        public final int refnum;

        public proxyCommandServerHandler(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.CommandServerHandler
        public native SystemProxyStatus getSystemProxyStatus();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.CommandServerHandler
        public native void serviceReload();

        @Override // io.nekohasekai.libbox.CommandServerHandler
        public native void serviceStop();

        @Override // io.nekohasekai.libbox.CommandServerHandler
        public native void setSystemProxyEnabled(boolean z10);

        @Override // io.nekohasekai.libbox.CommandServerHandler
        public native void writeDebugMessage(String str);
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyConnectionEventIterator implements Seq.Proxy, ConnectionEventIterator {
        public final int refnum;

        public proxyConnectionEventIterator(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.ConnectionEventIterator
        public native boolean hasNext();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.ConnectionEventIterator
        public native ConnectionEvent next();
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyConnectionIterator implements Seq.Proxy, ConnectionIterator {
        public final int refnum;

        public proxyConnectionIterator(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.ConnectionIterator
        public native boolean hasNext();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.ConnectionIterator
        public native Connection next();
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyDeprecatedNoteIterator implements Seq.Proxy, DeprecatedNoteIterator {
        public final int refnum;

        public proxyDeprecatedNoteIterator(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.DeprecatedNoteIterator
        public native boolean hasNext();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.DeprecatedNoteIterator
        public native DeprecatedNote next();
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyFDroidMirrorIterator implements Seq.Proxy, FDroidMirrorIterator {
        public final int refnum;

        public proxyFDroidMirrorIterator(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.FDroidMirrorIterator
        public native boolean hasNext();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.FDroidMirrorIterator
        public native int len();

        @Override // io.nekohasekai.libbox.FDroidMirrorIterator
        public native FDroidMirror next();
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyFDroidPingResultIterator implements Seq.Proxy, FDroidPingResultIterator {
        public final int refnum;

        public proxyFDroidPingResultIterator(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.FDroidPingResultIterator
        public native boolean hasNext();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.FDroidPingResultIterator
        public native int len();

        @Override // io.nekohasekai.libbox.FDroidPingResultIterator
        public native FDroidPingResult next();
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyFunc implements Seq.Proxy, Func {
        public final int refnum;

        public proxyFunc(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.Func
        public native void invoke();
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyHTTPClient implements Seq.Proxy, HTTPClient {
        public final int refnum;

        public proxyHTTPClient(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.HTTPClient
        public native void close();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.HTTPClient
        public native void keepAlive();

        @Override // io.nekohasekai.libbox.HTTPClient
        public native void modernTLS();

        @Override // io.nekohasekai.libbox.HTTPClient
        public native HTTPRequest newRequest();

        @Override // io.nekohasekai.libbox.HTTPClient
        public native void pinnedSHA256(String str);

        @Override // io.nekohasekai.libbox.HTTPClient
        public native void pinnedTLS12();

        @Override // io.nekohasekai.libbox.HTTPClient
        public native void restrictedTLS();

        @Override // io.nekohasekai.libbox.HTTPClient
        public native void trySocks5(int i10);
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyHTTPRequest implements Seq.Proxy, HTTPRequest {
        public final int refnum;

        public proxyHTTPRequest(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.HTTPRequest
        public native HTTPResponse execute();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.HTTPRequest
        public native void randomUserAgent();

        @Override // io.nekohasekai.libbox.HTTPRequest
        public native void setContent(byte[] bArr);

        @Override // io.nekohasekai.libbox.HTTPRequest
        public native void setContentString(String str);

        @Override // io.nekohasekai.libbox.HTTPRequest
        public native void setHeader(String str, String str2);

        @Override // io.nekohasekai.libbox.HTTPRequest
        public native void setMethod(String str);

        @Override // io.nekohasekai.libbox.HTTPRequest
        public native void setURL(String str);

        @Override // io.nekohasekai.libbox.HTTPRequest
        public native void setUserAgent(String str);
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyHTTPResponse implements Seq.Proxy, HTTPResponse {
        public final int refnum;

        public proxyHTTPResponse(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.HTTPResponse
        public native StringBox getContent();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.HTTPResponse
        public native void writeTo(String str);

        @Override // io.nekohasekai.libbox.HTTPResponse
        public native void writeToWithProgress(String str, HTTPResponseWriteToProgressHandler hTTPResponseWriteToProgressHandler);
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyHTTPResponseWriteToProgressHandler implements Seq.Proxy, HTTPResponseWriteToProgressHandler {
        public final int refnum;

        public proxyHTTPResponseWriteToProgressHandler(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.HTTPResponseWriteToProgressHandler
        public native void update(long j6, long j10);
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyInt32Iterator implements Seq.Proxy, Int32Iterator {
        public final int refnum;

        public proxyInt32Iterator(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.Int32Iterator
        public native boolean hasNext();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.Int32Iterator
        public native int len();

        @Override // io.nekohasekai.libbox.Int32Iterator
        public native int next();
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyInterfaceUpdateListener implements Seq.Proxy, InterfaceUpdateListener {
        public final int refnum;

        public proxyInterfaceUpdateListener(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.InterfaceUpdateListener
        public native void updateDefaultInterface(String str, int i10, boolean z10, boolean z11);
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyLocalDNSTransport implements Seq.Proxy, LocalDNSTransport {
        public final int refnum;

        public proxyLocalDNSTransport(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.LocalDNSTransport
        public native void exchange(ExchangeContext exchangeContext, byte[] bArr);

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.LocalDNSTransport
        public native void lookup(ExchangeContext exchangeContext, String str, String str2);

        @Override // io.nekohasekai.libbox.LocalDNSTransport
        public native boolean raw();
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyLogIterator implements Seq.Proxy, LogIterator {
        public final int refnum;

        public proxyLogIterator(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.LogIterator
        public native boolean hasNext();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.LogIterator
        public native int len();

        @Override // io.nekohasekai.libbox.LogIterator
        public native LogEntry next();
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyNetworkInterfaceIterator implements Seq.Proxy, NetworkInterfaceIterator {
        public final int refnum;

        public proxyNetworkInterfaceIterator(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.NetworkInterfaceIterator
        public native boolean hasNext();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.NetworkInterfaceIterator
        public native NetworkInterface next();
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyOnDemandRule implements Seq.Proxy, OnDemandRule {
        public final int refnum;

        public proxyOnDemandRule(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.OnDemandRule
        public native StringIterator dnsSearchDomainMatch();

        @Override // io.nekohasekai.libbox.OnDemandRule
        public native StringIterator dnsServerAddressMatch();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.OnDemandRule
        public native int interfaceTypeMatch();

        @Override // io.nekohasekai.libbox.OnDemandRule
        public native String probeURL();

        @Override // io.nekohasekai.libbox.OnDemandRule
        public native StringIterator ssidMatch();

        @Override // io.nekohasekai.libbox.OnDemandRule
        public native int target();
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyOnDemandRuleIterator implements Seq.Proxy, OnDemandRuleIterator {
        public final int refnum;

        public proxyOnDemandRuleIterator(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.OnDemandRuleIterator
        public native boolean hasNext();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.OnDemandRuleIterator
        public native OnDemandRule next();
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyOutboundGroupItemIterator implements Seq.Proxy, OutboundGroupItemIterator {
        public final int refnum;

        public proxyOutboundGroupItemIterator(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.OutboundGroupItemIterator
        public native boolean hasNext();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.OutboundGroupItemIterator
        public native OutboundGroupItem next();
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyOutboundGroupIterator implements Seq.Proxy, OutboundGroupIterator {
        public final int refnum;

        public proxyOutboundGroupIterator(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.OutboundGroupIterator
        public native boolean hasNext();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.OutboundGroupIterator
        public native OutboundGroup next();
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyPlatformInterface implements Seq.Proxy, PlatformInterface {
        public final int refnum;

        public proxyPlatformInterface(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.PlatformInterface
        public native void autoDetectInterfaceControl(int i10);

        @Override // io.nekohasekai.libbox.PlatformInterface
        public native void clearDNSCache();

        @Override // io.nekohasekai.libbox.PlatformInterface
        public native void closeDefaultInterfaceMonitor(InterfaceUpdateListener interfaceUpdateListener);

        @Override // io.nekohasekai.libbox.PlatformInterface
        public native ConnectionOwner findConnectionOwner(int i10, String str, int i11, String str2, int i12);

        @Override // io.nekohasekai.libbox.PlatformInterface
        public native NetworkInterfaceIterator getInterfaces();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.PlatformInterface
        public native boolean includeAllNetworks();

        @Override // io.nekohasekai.libbox.PlatformInterface
        public native LocalDNSTransport localDNSTransport();

        @Override // io.nekohasekai.libbox.PlatformInterface
        public native int openTun(TunOptions tunOptions);

        @Override // io.nekohasekai.libbox.PlatformInterface
        public native WIFIState readWIFIState();

        @Override // io.nekohasekai.libbox.PlatformInterface
        public native void sendNotification(Notification notification);

        @Override // io.nekohasekai.libbox.PlatformInterface
        public native void startDefaultInterfaceMonitor(InterfaceUpdateListener interfaceUpdateListener);

        @Override // io.nekohasekai.libbox.PlatformInterface
        public native StringIterator systemCertificates();

        @Override // io.nekohasekai.libbox.PlatformInterface
        public native boolean underNetworkExtension();

        @Override // io.nekohasekai.libbox.PlatformInterface
        public native boolean usePlatformAutoDetectInterfaceControl();

        @Override // io.nekohasekai.libbox.PlatformInterface
        public native boolean useProcFS();
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyProfilePreviewIterator implements Seq.Proxy, ProfilePreviewIterator {
        public final int refnum;

        public proxyProfilePreviewIterator(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.ProfilePreviewIterator
        public native boolean hasNext();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.ProfilePreviewIterator
        public native ProfilePreview next();
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyRoutePrefixIterator implements Seq.Proxy, RoutePrefixIterator {
        public final int refnum;

        public proxyRoutePrefixIterator(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.RoutePrefixIterator
        public native boolean hasNext();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.RoutePrefixIterator
        public native RoutePrefix next();
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyStringIterator implements Seq.Proxy, StringIterator {
        public final int refnum;

        public proxyStringIterator(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.StringIterator
        public native boolean hasNext();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.StringIterator
        public native int len();

        @Override // io.nekohasekai.libbox.StringIterator
        public native String next();
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyTunOptions implements Seq.Proxy, TunOptions {
        public final int refnum;

        public proxyTunOptions(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.TunOptions
        public native boolean getAutoRoute();

        @Override // io.nekohasekai.libbox.TunOptions
        public native StringBox getDNSServerAddress();

        @Override // io.nekohasekai.libbox.TunOptions
        public native StringIterator getExcludePackage();

        @Override // io.nekohasekai.libbox.TunOptions
        public native StringIterator getHTTPProxyBypassDomain();

        @Override // io.nekohasekai.libbox.TunOptions
        public native StringIterator getHTTPProxyMatchDomain();

        @Override // io.nekohasekai.libbox.TunOptions
        public native String getHTTPProxyServer();

        @Override // io.nekohasekai.libbox.TunOptions
        public native int getHTTPProxyServerPort();

        @Override // io.nekohasekai.libbox.TunOptions
        public native StringIterator getIncludePackage();

        @Override // io.nekohasekai.libbox.TunOptions
        public native RoutePrefixIterator getInet4Address();

        @Override // io.nekohasekai.libbox.TunOptions
        public native RoutePrefixIterator getInet4RouteAddress();

        @Override // io.nekohasekai.libbox.TunOptions
        public native RoutePrefixIterator getInet4RouteExcludeAddress();

        @Override // io.nekohasekai.libbox.TunOptions
        public native RoutePrefixIterator getInet4RouteRange();

        @Override // io.nekohasekai.libbox.TunOptions
        public native RoutePrefixIterator getInet6Address();

        @Override // io.nekohasekai.libbox.TunOptions
        public native RoutePrefixIterator getInet6RouteAddress();

        @Override // io.nekohasekai.libbox.TunOptions
        public native RoutePrefixIterator getInet6RouteExcludeAddress();

        @Override // io.nekohasekai.libbox.TunOptions
        public native RoutePrefixIterator getInet6RouteRange();

        @Override // io.nekohasekai.libbox.TunOptions
        public native int getMTU();

        @Override // io.nekohasekai.libbox.TunOptions
        public native boolean getStrictRoute();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }

        @Override // io.nekohasekai.libbox.TunOptions
        public native boolean isHTTPProxyEnabled();
    }

    /* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
    /* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
    public static final class proxyXPCDialer implements Seq.Proxy, XPCDialer {
        public final int refnum;

        public proxyXPCDialer(int i10) {
            this.refnum = i10;
            Seq.trackGoRef(i10, this);
        }

        @Override // io.nekohasekai.libbox.XPCDialer
        public native int dialXPC();

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }
    }

    static {
        Seq.touch();
        _init();
    }

    private Libbox() {
    }

    private static native void _init();

    public static native int availablePort(int i10);

    public static native void checkConfig(String str);

    public static native FDroidUpdateInfo checkFDroidUpdate(String str, String str2, int i10, String str3);

    public static native boolean compareSemver(String str, String str2);

    public static native ErrorMessage decodeErrorMessage(byte[] bArr);

    public static native int decodeLengthChunk(byte[] bArr);

    public static native ProfileContent decodeProfileContent(byte[] bArr);

    public static native ProfileContentRequest decodeProfileContentRequest(byte[] bArr);

    public static native byte[] encodeChunkedMessage(byte[] bArr);

    public static native String formatBytes(long j6);

    public static native StringBox formatConfig(String str);

    public static native String formatDuration(long j6);

    public static native String formatMemoryBytes(long j6);

    public static native String generateRemoteProfileImportLink(String str, String str2);

    public static native FDroidMirrorIterator getFDroidMirrors();

    public static native CommandClient newCommandClient(CommandClientHandler commandClientHandler, CommandClientOptions commandClientOptions);

    public static native CommandServer newCommandServer(CommandServerHandler commandServerHandler, PlatformInterface platformInterface);

    public static native Connections newConnections();

    public static native HTTPClient newHTTPClient();

    public static native PProfServer newPProfServer(long j6);

    public static native CommandClient newStandaloneCommandClient();

    public static native WIFIState newWIFIState(String str, String str2);

    public static native ImportRemoteProfile parseRemoteProfileImportLink(String str);

    public static native FDroidPingResult pingFDroidMirror(String str);

    public static native FDroidPingResultIterator pingFDroidMirrors(String str);

    public static native String proxyDisplayType(String str);

    public static native StringBox randomHex(int i10);

    public static native AndroidVPNType readAndroidVPNType(StringIterator stringIterator);

    public static native void redirectStderr(String str);

    public static native void setLocale(String str);

    public static native void setMemoryLimit(boolean z10);

    public static native void setXPCDialer(XPCDialer xPCDialer);

    public static native void setup(SetupOptions setupOptions);

    public static native String version();

    public static void touch() {
    }
}
