package Pojo;

/**
 * Created by rockers on 17/3/17.
 */

public class TicketCheckPojo {
    public boolean success;
    public int status;
    public int attendee_id;
    public String message;

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

    public int getAttendee_id() {
        return attendee_id;
    }

    public void setAttendee_id(int attendee_id) {
        this.attendee_id = attendee_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
