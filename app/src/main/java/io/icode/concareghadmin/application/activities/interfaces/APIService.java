package io.icode.concareghadmin.application.activities.interfaces;
import io.icode.concareghadmin.application.activities.notifications.Data;
import io.icode.concareghadmin.application.activities.notifications.MyResponse;
import io.icode.concareghadmin.application.activities.notifications.Sender;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    //TODO: todo demo
    @FormUrlEncoded
    @POST("send")
    Call<ResponseBody> sendSingleNotification(
            @Field("token") String token,
            @Field("title") String title,
            @Field("body") String body
            //@Field("data") Data data
    );

}
