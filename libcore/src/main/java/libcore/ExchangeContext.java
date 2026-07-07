package libcore;

public interface ExchangeContext {
    void onCancel(Runnable cancel);
    void rawSuccess(byte[] answer);
    void success(String answer);
    void errorCode(int code);
    void errnoCode(int code);
}
