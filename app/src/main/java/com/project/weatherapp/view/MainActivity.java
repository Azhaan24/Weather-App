package com.project.weatherapp.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.project.weatherapp.R;
import com.project.weatherapp.databinding.ActivityMainBinding;
import com.project.weatherapp.databinding.BottomSheetDialogBinding;
import com.project.weatherapp.util.Constants;
import com.project.weatherapp.util.NetworkAlertDialog;
import com.project.weatherapp.util.networkutil.NetworkConnectionObserver;
import com.project.weatherapp.util.networkutil.NetworkStatusListener;

public class MainActivity extends AppCompatActivity implements NetworkStatusListener {

    ActivityMainBinding mainBinding;
    ActivityResultLauncher<String[]> permissionsResultLauncher;
    BottomSheetDialogBinding bottomSheetDialogBinding;
    int deniedAllPermissionsCount;
    int deniedFinePermissionCount;
    SharedPreferences sharedPreferences;
    AlertDialog dialog;
    NetworkConnectionObserver networkConnectionObserver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding=ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(mainBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sharedPreferences = this.getSharedPreferences(Constants.nameOfSharedPreferences,Context.MODE_PRIVATE);
        deniedAllPermissionsCount=sharedPreferences.getInt(Constants.keyForAllPermissionDeniedCounts,0);
        deniedFinePermissionCount=sharedPreferences.getInt(Constants.keyForOnlyFinePermissionDeniedCounts,0);

        registerForPermission();

        mainBinding.buttonWeatherByLocation.setOnClickListener(v->{
            if(hasFineLocationPermission()){
                checkLocationSettings();
            }
            else if(hasCoarseLocationPermission()){
                saveDeniedOnlyFinePermissionCount();
                if(deniedFinePermissionCount>2){
                    checkLocationSettings();
                }
                else {
                    showBottomSheetDialog("Give precise location permission for better results.","fine","permission");
                }
            }
            else{
                permissionsResultLauncher.launch(new String[]{Constants.COARSE_LOCATION,Constants.FINE_LOCATION});
            }
        });

        mainBinding.buttonWeatherByCity.setOnClickListener(v->{
            openWeatherActivity(Constants.byCityName);
        });

        dialog = NetworkAlertDialog.createNetworkAlertDialog(this).create();
        networkConnectionObserver = new NetworkConnectionObserver(this,this);
    }
    public void registerForPermission(){
        permissionsResultLauncher=registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),result ->{
            Boolean b1 = result.get(Constants.FINE_LOCATION);
            Boolean b2 = result.get(Constants.COARSE_LOCATION);

            if(b1!=null && b2!=null){
                boolean isFineLocationGranted = b1;
                boolean isCoarseLocationGranted = b2;
                if(isFineLocationGranted){
                    deniedAllPermissionsCount = 0;
                    deniedFinePermissionCount = 0;

                    sharedPreferences.edit().putInt(Constants.keyForAllPermissionDeniedCounts,0).putInt(Constants.keyForOnlyFinePermissionDeniedCounts,0).apply();

                    checkLocationSettings();
                }
                else if(isCoarseLocationGranted){
                    saveDeniedOnlyFinePermissionCount();
                    showBottomSheetDialog("Give precise location permission for better results.","fine","permission");
                }
                else{
                    deniedAllPermissionsCount++;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(Constants.keyForOnlyFinePermissionDeniedCounts,deniedAllPermissionsCount);
                    editor.putInt(Constants.keyForOnlyFinePermissionDeniedCounts,0);
                    editor.apply();

                    showBottomSheetDialog("To get the weather by location, You need to enable location permissions","all","permission");
                }
            }
        });
    }
    private boolean hasFineLocationPermission(){
        return ContextCompat.checkSelfPermission(this,Constants.FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    private boolean hasCoarseLocationPermission(){
        return ContextCompat.checkSelfPermission(this,Constants.COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    public void showBottomSheetDialog(String message,String deniedPermissions,String useFor){
        bottomSheetDialogBinding = BottomSheetDialogBinding.inflate(getLayoutInflater());
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        bottomSheetDialog.setContentView(bottomSheetDialogBinding.getRoot());

        if(useFor.equals("Location")){
            bottomSheetDialogBinding.buttonAllow.setText("Go");
            bottomSheetDialogBinding.textViewTitle.setText("Location");
            bottomSheetDialogBinding.textViewMessage.setText(message);
        }
        else {
            if(deniedAllPermissionsCount>2 || deniedFinePermissionCount>2){
                bottomSheetDialogBinding.buttonAllow.setText("Go");
                bottomSheetDialogBinding.textViewMessage.setText("Open the App Settings to give Precise Location Permissions");
            }
        }

        bottomSheetDialogBinding.buttonAllow.setOnClickListener(v ->{

            if(useFor.equals("Location")){
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
            else {
                if(deniedAllPermissionsCount>2 || deniedFinePermissionCount>2){
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package",getPackageName(),null);
                    intent.setData(uri);
                    startActivity(intent);
                }
                else {
                    permissionsResultLauncher.launch(new String[]{Constants.COARSE_LOCATION,Constants.FINE_LOCATION});
                }
            }
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialogBinding.buttonDeny.setOnClickListener(v ->{
            if(deniedPermissions.equals("fine")){
                checkLocationSettings();
            }
            bottomSheetDialog.dismiss();
        });
        bottomSheetDialog.show();
    }
    public void checkLocationSettings(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            openWeatherActivity(Constants.byLocation);
            //Toast.makeText(this, "Second Activity", Toast.LENGTH_SHORT).show();
        }
        else{
            showBottomSheetDialog("Go to Location Settings to turn on the location","null","Location");
        }
    }
    public void saveDeniedOnlyFinePermissionCount(){
        deniedFinePermissionCount++;
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt(Constants.keyForOnlyFinePermissionDeniedCounts,deniedFinePermissionCount);
        editor.apply();
    }
    public void openWeatherActivity(String prefer){
        Intent i = new Intent(MainActivity.this, WeatherActivity.class);
        i.putExtra(Constants.intentName,prefer);
        startActivity(i);
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
        networkConnectionObserver.checkNetworkConnection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        networkConnectionObserver.unregisterCallback();
    }
}