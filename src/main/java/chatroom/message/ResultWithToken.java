package chatroom.message;

public class ResultWithToken extends Result {
    private String token;

    public ResultWithToken() {} // Required by Jackson
    public ResultWithToken(Class<?> msgClass, boolean result, String token) {
        super(msgClass, result);
        this.token = token;
    }
}
