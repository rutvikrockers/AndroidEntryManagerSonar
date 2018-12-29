package api;

import Pojo.AttendenceCheckinPojo;
import Pojo.LogoutPojo;
import Pojo.MyEventDetailPojo;
import Pojo.MyEventPojo;
import Pojo.ResponseUserLogin;
import Pojo.TicketCheckPojo;
import Pojo.UpdateAttendanceSinglePojo;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by rockers on 17/3/17.
 */

public interface RestInterface {
    @FormUrlEncoded
    @POST("mobile_logins/post_login")
    Call<ResponseUserLogin> LoginUsers(@Field("user_email") String Email, @Field("user_password") String Password);

    @FormUrlEncoded
    @POST("mobile_logins/myevent")
    Call<MyEventPojo> Myevent(@Field("id") String Id);

    @FormUrlEncoded
    @POST("mobile_logins/myevent_details")
    Call<MyEventDetailPojo> EventRefresh(@Field("id") String Id, @Field("event_id") int EventId);


    @FormUrlEncoded
    @POST("mobile_logins/attendee")
    Call<AttendenceCheckinPojo> AttendenceCheckin(@Field("id") String Id, @Field("event_id") int EventId);


    @FormUrlEncoded
    @POST("mobile_logins/update_attendee_single")
    Call<UpdateAttendanceSinglePojo> UpdateAttendeeSingle(@Field("id") String Id, @Field("event_id") int EventId,@Field("attendee_id") int AttendeeId,@Field("checkin_status") int Checkinstatus);


    @FormUrlEncoded
    @POST("mobile_logins/ticket_check")
    Call<TicketCheckPojo> TicketCheck(@Field("id") String Id, @Field("event_id") String EventId,@Field("ticket_number") String TicketNumber);

    @FormUrlEncoded
    @POST("mobile_logins/ticket_status")
    Call<TicketCheckPojo> TicketStatus(@Field("id") String Id, @Field("event_id") String EventId,@Field("ticket_number") String TicketNumber);

    @FormUrlEncoded
    @POST("mobile_logins/mobile_logout")
    Call<LogoutPojo> Logout(@Field("id") String Id);
}
