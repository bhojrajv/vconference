package com.example.vconference;

import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class UserClient {
    public static Retrofit retrofit=null;
    public static Retrofit getRetrofitclinet(){
         if(retrofit==null){
             retrofit=new Retrofit.Builder()
                     .baseUrl("https://fcm.googleapis.com/fcm/")
                     .addConverterFactory(ScalarsConverterFactory.create())
                     .build();
         }
         return  retrofit;
    }
}
