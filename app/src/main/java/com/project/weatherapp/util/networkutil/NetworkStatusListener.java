package com.project.weatherapp.util.networkutil;

public interface NetworkStatusListener {
    void onNetworkAvailable();
    void onNetworkLost();
}
