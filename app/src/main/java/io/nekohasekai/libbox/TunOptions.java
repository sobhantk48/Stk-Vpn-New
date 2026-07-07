package io.nekohasekai.libbox;

public class TunOptions {
    public int mtu;
    public String[] inet4Address;
    public String[] inet6Address;
    public boolean autoRoute;
    public String[] inet4RouteAddress;
    public String[] inet6RouteAddress;
    public String[] includePackage;
    public String[] excludePackage;
    public int dnsMode;
    public String[] dnsServerAddress;
}
