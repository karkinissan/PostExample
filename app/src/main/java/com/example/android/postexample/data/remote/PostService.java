package com.example.android.postexample.data.remote;

import com.example.android.postexample.data.model.PostResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Nissan on 7/3/2017.
 */

public interface PostService {
    @FormUrlEncoded
    @POST("/banksmart/registerUser")
    Call<PostResponse> savePost(@Field("name") String name,
                                @Field("email") String email,
                                @Field("phone") String phone,
                                @Field("location") String location,
                                @Field("password") String password,
                                @Field("department") Integer department);
}
