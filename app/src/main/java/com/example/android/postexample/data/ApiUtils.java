package com.example.android.postexample.data;

import com.example.android.postexample.data.remote.PostService;
import com.example.android.postexample.data.remote.RetrofitClient;
import com.example.android.postexample.data.remote.SpinnerService;

/**
 * Created by Nissan on 7/3/2017.
 */

public class ApiUtils {
    public static final String BASE_URL = "http://10.13.209.28/";
    public static SpinnerService getSpinnerService(){
        return RetrofitClient.getClient(BASE_URL).create(SpinnerService.class);
    }
    public static PostService getPostService(){
        return RetrofitClient.getClient(BASE_URL).create(PostService.class);
    }
}
