package libcore;

public interface LocalDNSTransport {
    boolean raw();
    long networkHandle();
    void exchange(ExchangeContext ctx, byte[] message);
    void lookup(ExchangeContext ctx, String network, String domain);
}
