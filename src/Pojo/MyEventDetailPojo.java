package Pojo;

/**
 * Created by rockers on 17/3/17.
 */

public class MyEventDetailPojo {
    public boolean success;
    public int status;
    public Event event;

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

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
