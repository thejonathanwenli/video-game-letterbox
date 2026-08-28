package learn.videogameletterbox.domain;

import java.util.ArrayList;
import java.util.List;

public class Result<T> {

    private ResultType type = ResultType.SUCCESS;
    private final ArrayList<String> messages = new ArrayList<>();
    private T payload;

    public boolean isSuccess() {
        return type == ResultType.SUCCESS;
    }

    public void addErrorMessage(String message) {
        type = ResultType.INVALID;
        messages.add(message);
    }

    public ResultType getType() {
        return type;
    }

    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }
}
