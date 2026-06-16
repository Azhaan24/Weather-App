package com.project.weatherapp.util;

import android.app.AlertDialog;
import android.content.Context;

public class NetworkAlertDialog {
    public static AlertDialog.Builder createNetworkAlertDialog(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Weather App").setMessage("No Internet Connection").setPositiveButton("OK",(dialog, which) -> {
            dialog.dismiss();
        });
        return builder;
    }
}
