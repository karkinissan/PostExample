package com.example.android.postexample.data.remote;

import com.example.android.postexample.data.model.SpinnerResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Nissan on 7/3/2017.
 */

public interface SpinnerService {
    @GET("/banksmart/department")
    Call<List<SpinnerResponse>> getResponse();
}
