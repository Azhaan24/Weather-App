package com.project.weatherapp.util;

import android.Manifest;

public class Constants {
    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String nameOfSharedPreferences = "com.project.weatherapp";
    public static final String keyForAllPermissionDeniedCounts = "deniedAllPermissionsCount";
    public static final String keyForOnlyFinePermissionDeniedCounts = "deniedFinePermissionCount";
    public static final String intentName = "weather";
    public static final String byCityName = "by city name";
    public static final String byLocation = "by location";
    public static final String BASE_URL = "https://api.openweathermap.org/";
    public static final String SUB_URL = "data/2.5/weather?appid=6c5c4e227e3bf5e6b423703373eedaed&units=metric";
}
