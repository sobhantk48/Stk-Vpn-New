package libcore;

import androidx.annotation.NonNull;

public class Libcore {
    static {
        System.loadLibrary("box");
    }

    public static native BoxInstance newSingBoxInstance(@NonNull String config, @NonNull LocalDNSTransport transport);
}
