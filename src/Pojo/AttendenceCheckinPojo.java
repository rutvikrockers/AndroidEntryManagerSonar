package Pojo;

import java.util.List;

/**
 * Created by rockers on 17/3/17.
 */

public class AttendenceCheckinPojo {
    public boolean success;
    public int status;
    public List<Msg> msg;
    public List<String> ticket_type;

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

    public List<Msg> getMsg() {
        return msg;
    }

    public void setMsg(List<Msg> msg) {
        this.msg = msg;
    }

    public List<String> getTicket_type() {
        return ticket_type;
    }

    public void setTicket_type(List<String> ticket_type) {
        this.ticket_type = ticket_type;
    }
}
