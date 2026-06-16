package com.project.weatherapp.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import com.project.weatherapp.model.WeatherModel;
import com.project.weatherapp.util.Constants;

public interface WeatherApi {

    @GET(Constants.SUB_URL)
    Call<WeatherModel> getWeatherByLocation(@Query("lat") double userLatitude, @Query("lon")double userLongitude);

    @GET(Constants.SUB_URL)
    Call<WeatherModel> getWeatherByCityName(@Query("q") String cityName);
}
