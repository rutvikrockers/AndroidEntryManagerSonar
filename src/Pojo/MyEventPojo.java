package Pojo;

import java.util.List;

/**
 * Created by rockers on 17/3/17.
 */

public class MyEventPojo {
    public List<AllEvent> all_events;
    public boolean success;
    public int status;

    public List<AllEvent> getAll_events() {
        return all_events;
    }

    public void setAll_events(List<AllEvent> all_events) {
        this.all_events = all_events;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
