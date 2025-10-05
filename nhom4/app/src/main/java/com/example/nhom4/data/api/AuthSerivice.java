package com.example.nhom4.data.api;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
public interface AuthSerivice {
    @POST("xxx/login")
    @FormUrlEncoded
    Call<String> login(
            @Field("email") String email,
            @Field("password") String password
    );

    @POST("xxx/register")
    @FormUrlEncoded
    Call<String> register(
            @Field("email") String email,
            @Field("password") String password,
            @Field("fullname") String fullname,
            @Field("address") String address
    );
}
