package com.example.vconference;

import com.google.firebase.firestore.auth.User;
import com.squareup.okhttp.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface Apiservice {
    @POST("send")
    Call<String>remote(
            @HeaderMap HashMap<String,String>headers,
            @Body String body
    );

}
