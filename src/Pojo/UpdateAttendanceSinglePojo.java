package Pojo;

/**
 * Created by rockers on 17/3/17.
 */

public class UpdateAttendanceSinglePojo {
    public boolean success;
    public int status;
    public int msg;

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

    public int getMsg() {
        return msg;
    }

    public void setMsg(int msg) {
        this.msg = msg;
    }
}
