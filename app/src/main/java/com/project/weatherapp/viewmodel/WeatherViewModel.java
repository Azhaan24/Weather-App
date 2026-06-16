package com.project.weatherapp.viewmodel;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.project.weatherapp.model.WeatherModel;
import com.project.weatherapp.service.RetrofitInstance;
import com.project.weatherapp.service.WeatherApi;
import com.project.weatherapp.util.networkutil.NetworkConnectionObserver;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherViewModel extends ViewModel {
    MutableLiveData<WeatherModel> weatherResponseLiveData;
    MutableLiveData<Boolean> progressBarLiveData;
    WeatherApi weatherService;
    NetworkConnectionObserver networkConnectionObserver;

    public WeatherViewModel(){
        weatherResponseLiveData=new MutableLiveData<>();
        progressBarLiveData=new MutableLiveData<>();
        weatherService= RetrofitInstance.getRetrofit().create(WeatherApi.class);
    }

    public MutableLiveData<WeatherModel> getWeatherResponseLiveData() {
        return weatherResponseLiveData;
    }

    public MutableLiveData<Boolean> getProgressBarLiveData() {
        return progressBarLiveData;
    }

    public void setNetworkConnectionObserver(NetworkConnectionObserver networkConnectionObserver) {
        this.networkConnectionObserver = networkConnectionObserver;
    }

    public void sendRequestByLocation(Context context, double userLatitude, double userLongitude){
        progressBarLiveData.setValue(true);
        Call<WeatherModel> call=weatherService.getWeatherByLocation(userLatitude, userLongitude);
        call.enqueue(new Callback<WeatherModel>() {
            @Override
            public void onResponse(Call<WeatherModel> call, Response<WeatherModel> response) {
                if(response.isSuccessful()){
                    weatherResponseLiveData.setValue(response.body());
                }
                else{
                    Toast.makeText(context,response.message(), Toast.LENGTH_SHORT).show();
                }
                progressBarLiveData.setValue(false);
            }

            @Override
            public void onFailure(Call<WeatherModel> call, Throwable throwable) {
                Log.d("onFailureByLocation",throwable.getLocalizedMessage());
                progressBarLiveData.setValue(false);
                networkConnectionObserver.checkNetworkConnection();
            }
        });
    }
    public void sendRequestByCityName(Context context,String cityName){
        progressBarLiveData.setValue(true);
        Call<WeatherModel> call=weatherService.getWeatherByCityName(cityName);
        call.enqueue(new Callback<WeatherModel>() {
            @Override
            public void onResponse(Call<WeatherModel> call, Response<WeatherModel> response) {
                if(response.isSuccessful()){
                    weatherResponseLiveData.setValue(response.body());
                }
                else{
                    Toast.makeText(context,response.message(), Toast.LENGTH_SHORT).show();
                }
                progressBarLiveData.setValue(false);
            }

            @Override
            public void onFailure(Call<WeatherModel> call, Throwable throwable) {
                Log.d("onFailureByCityName",throwable.getLocalizedMessage());
                progressBarLiveData.setValue(false);
                networkConnectionObserver.checkNetworkConnection();
            }
        });
    }
}