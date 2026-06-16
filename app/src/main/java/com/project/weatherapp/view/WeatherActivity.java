package com.project.weatherapp.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.project.weatherapp.R;
import com.project.weatherapp.databinding.ActivityWeatherBinding;
import com.project.weatherapp.model.WeatherModel;
import com.project.weatherapp.util.Constants;
import com.project.weatherapp.util.NetworkAlertDialog;
import com.project.weatherapp.util.networkutil.NetworkConnectionObserver;
import com.project.weatherapp.util.networkutil.NetworkStatusListener;
import com.project.weatherapp.viewmodel.WeatherViewModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class WeatherActivity extends AppCompatActivity implements NetworkStatusListener {
    ActivityWeatherBinding weatherBinding;
    String prefer;
    WeatherViewModel weatherViewModel;

    LocationManager locationManager;
    LocationListener locationListener;
    double lat;
    double lon;
    AlertDialog dialog;
    NetworkConnectionObserver networkConnectionObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weatherBinding = ActivityWeatherBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(weatherBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(weatherBinding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        weatherViewModel=new ViewModelProvider(this).get(WeatherViewModel.class);

        weatherBinding.linearLayoutWeatherData.setVisibility(View.INVISIBLE);
        weatherBinding.progressBarWeatherData.setVisibility(View.INVISIBLE);
        weatherBinding.progressbarWeatherIcon.setVisibility(View.INVISIBLE);
        weatherBinding.linearLayoutWeatherDetails.setVisibility(View.INVISIBLE);

        prefer = getIntent().getStringExtra(Constants.intentName);
        if(prefer!=null){
            if (prefer.equals(Constants.byLocation)){
                getWeatherDataByLocation();
            }
            else{
                weatherBinding.progressBarWeatherData.setVisibility(View.INVISIBLE);
            }
        }

        weatherBinding.toolBar.setNavigationOnClickListener(v ->{
            finish();
        });

        weatherBinding.search.setOnClickListener(v ->{
            getWeatherDataByCityName();
        });

        weatherBinding.refreshLayout.setOnRefreshListener(()->{
            if(prefer.equals(Constants.byLocation)){
                getWeatherDataByLocation();
            }
            else {
                getWeatherDataByCityName();
            }
            weatherBinding.refreshLayout.setRefreshing(false);
        });

        dialog = NetworkAlertDialog.createNetworkAlertDialog(this).create();
        networkConnectionObserver = new NetworkConnectionObserver(this,this);
        weatherViewModel.setNetworkConnectionObserver(networkConnectionObserver);
    }

    public void getWeatherDataByCityName(){
        String cityName = weatherBinding.editTextCityName.getText().toString();
        if(cityName.isEmpty()){
            Toast.makeText(this, "Nothing Found", Toast.LENGTH_SHORT).show();
        }
        else{
            weatherViewModel.getProgressBarLiveData().observe(WeatherActivity.this,progressState->{
                if(progressState){
                    weatherBinding.progressBarWeatherData.setVisibility(View.VISIBLE);
                    weatherBinding.linearLayoutWeatherData.setVisibility(View.INVISIBLE);
                }
                else {
                    weatherBinding.progressBarWeatherData.setVisibility(View.INVISIBLE);
                }
            });
            weatherViewModel.sendRequestByCityName(getApplicationContext(),cityName);
            weatherViewModel.getWeatherResponseLiveData().observe(WeatherActivity.this,this::showWeatherData);
        }
    }
    @SuppressLint("MissingPermission")
    public void getWeatherDataByLocation(){
        weatherBinding.progressBarWeatherData.setVisibility(View.VISIBLE);
        weatherBinding.linearLayoutSearch.setVisibility(View.INVISIBLE);
        locationManager= (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener=new LocationListener() {
            private void onChanged(WeatherModel weatherResponse) {
                showWeatherData(weatherResponse);
            }

            @Override
            public void onLocationChanged(@NonNull Location location) {
                lat=location.getLatitude();
                lon=location.getLongitude();

                Log.d("userLatitude",String.valueOf(lat));
                Log.d("userLongitude",String.valueOf(lon));

                weatherViewModel.getProgressBarLiveData().observe(WeatherActivity.this,progressState->{
                    if(progressState){
                        weatherBinding.progressBarWeatherData.setVisibility(View.VISIBLE);
                        weatherBinding.linearLayoutWeatherData.setVisibility(View.INVISIBLE);
                    }
                    else {
                        weatherBinding.progressBarWeatherData.setVisibility(View.INVISIBLE);
                    }
                });
                weatherViewModel.sendRequestByLocation(getApplicationContext(),lat,lon);
                weatherViewModel.getWeatherResponseLiveData().observe(WeatherActivity.this, this::onChanged);
            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,500,50,locationListener);
    }
    public void showWeatherData(WeatherModel response){
        weatherBinding.textViewCity.setText(response.getName()+" , "+response.getSys().getCountry());
        weatherBinding.textViewTemperature.setText(response.getMain().getTemp()+" °C");
        weatherBinding.textViewWeatherDescription.setText(response.getWeather().get(0).getDescription());
        weatherBinding.textViewHumidity.setText(response.getMain().getHumidity()+" %");
        weatherBinding.textViewMaxTemp.setText(response.getMain().getTemp_max()+" °C");
        weatherBinding.textViewMinTemp.setText(response.getMain().getTemp_min()+" °C");
        weatherBinding.textViewWind.setText(response.getWind().getSpeed()+"");
        weatherBinding.textViewPressure.setText(response.getMain().getPressure()+"");

        weatherBinding.progressBarWeatherData.setVisibility(View.INVISIBLE);
        weatherBinding.linearLayoutWeatherData.setVisibility(View.VISIBLE);
        weatherBinding.progressbarWeatherIcon.setVisibility(View.VISIBLE);
        weatherBinding.linearLayoutWeatherDetails.setVisibility(View.VISIBLE);

        String iconCode = response.getWeather().get(0).getIcon();
        Picasso.get().load("https://openweathermap.org/img/wn/" + iconCode + "@2x.png").into(weatherBinding.imageViewWeatherIcon, new Callback() {
            @Override
            public void onSuccess() {
                weatherBinding.progressbarWeatherIcon.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(Exception e) {
                weatherBinding.imageViewWeatherIcon.setImageResource(R.drawable.partly_cloudy_day_24px);
                Log.d("iconError",e.getLocalizedMessage());
                weatherBinding.progressbarWeatherIcon.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onNetworkAvailable() {
        this.runOnUiThread(()->{
            if(dialog!=null && dialog.isShowing()){
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onNetworkLost() {
        this.runOnUiThread(()->{
            if(dialog!=null && !dialog.isShowing()){
                dialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        networkConnectionObserver.registerCallback();
    }

    @Override
    protected void onPause() {
        super.onPause();
        networkConnectionObserver.unregisterCallback();
    }
}