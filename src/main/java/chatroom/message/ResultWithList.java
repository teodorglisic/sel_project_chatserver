package chatroom.message;

import java.util.ArrayList;

public class ResultWithList extends Result {
    private ArrayList<String> list;

    public ResultWithList() {} // Required by Jackson
    public ResultWithList(Class<?> msgClass, boolean result, ArrayList<String> list) {
        super(msgClass, result);
        this.list = list;
    }
}
