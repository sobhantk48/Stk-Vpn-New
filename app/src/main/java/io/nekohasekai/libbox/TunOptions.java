package io.nekohasekai.libbox;
/* compiled from: r8-map-id-168c12c85222c28a4fcc435f67d1c89bb1945b6c708a61a875435f7921dd4390 */
/* loaded from: /data/data/com.termux/files/home/sfa_extracted/classes.dex */
public interface TunOptions {
    boolean getAutoRoute();

    StringBox getDNSServerAddress();

    StringIterator getExcludePackage();

    StringIterator getHTTPProxyBypassDomain();

    StringIterator getHTTPProxyMatchDomain();

    String getHTTPProxyServer();

    int getHTTPProxyServerPort();

    StringIterator getIncludePackage();

    RoutePrefixIterator getInet4Address();

    RoutePrefixIterator getInet4RouteAddress();

    RoutePrefixIterator getInet4RouteExcludeAddress();

    RoutePrefixIterator getInet4RouteRange();

    RoutePrefixIterator getInet6Address();

    RoutePrefixIterator getInet6RouteAddress();

    RoutePrefixIterator getInet6RouteExcludeAddress();

    RoutePrefixIterator getInet6RouteRange();

    int getMTU();

    boolean getStrictRoute();

    boolean isHTTPProxyEnabled();
}
