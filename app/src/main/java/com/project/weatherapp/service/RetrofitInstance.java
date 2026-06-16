package com.project.weatherapp.service;

import com.project.weatherapp.util.Constants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {
    public static Retrofit retrofit;
    public static Retrofit getRetrofit(){
        if(retrofit==null){
            retrofit=new Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }
}
