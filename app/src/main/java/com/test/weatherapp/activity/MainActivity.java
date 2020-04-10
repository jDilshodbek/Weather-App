package com.test.weatherapp.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.test.weatherapp.BuildConfig;
import com.test.weatherapp.R;
import com.test.weatherapp.datasource.Injection;
import com.test.weatherapp.interfaces.WeatherInterface;
import com.test.weatherapp.models.CurrentDayApiResult;
import com.test.weatherapp.net.ApiClent;
import com.test.weatherapp.viewmodels.WeatherModelFactory;
import com.test.weatherapp.viewmodels.WeatherViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    // Permission code for loacation
    private static final int REQUEST_PERMISSIONS_LOCATION_SETTINGS_REQUEST_CODE = 33;
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private FusedLocationProviderClient fusedLocationProviderClient;
    private double Lat, Lon;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private String city = "";
    WeatherViewModel weatherViewModel;
    private Dialog dialog;
    //Variables
    private TextView cityText, tempText, minTempText, maxTempText, humidityText, pressureText;
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        // Setup Viewmodel and factory
        WeatherModelFactory weatherModelFactory = Injection.provideViewModelFactory(this);
        weatherViewModel = new ViewModelProvider(MainActivity.this, weatherModelFactory).get(WeatherViewModel.class);
        // declare variables
        cityText = findViewById(R.id.cityText);
        tempText = findViewById(R.id.tempText);
        minTempText = findViewById(R.id.minTempText);
        maxTempText = findViewById(R.id.maxTempText);
        humidityText = findViewById(R.id.humidityText);
        pressureText = findViewById(R.id.pressureText);

        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog_layout);
        // check for location permission
        checkPermissions();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    dialog.hide();
                    Lat = locationResult.getLastLocation().getLatitude();
                    Lon = locationResult.getLastLocation().getLongitude();
                    city = currentLocation(Lat, Lon, MainActivity.this);
                    setDefaultCityWeather(city);
                }


            }
        };

        checkForLocationRequest();
        checkForLocationSettings();
    }

    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            for (int index = permissions.length - 1; index >= 0; --index) {
                if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    // exit the app if one permission is not granted
                    finish();
                    return;
                }
            }

            checkForLocationRequest();
            checkForLocationSettings();
        }
    }


    private static String currentLocation(double lat, double lon, Context context) {
        String cityName = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(lat, lon, 10);
            if (addresses.size() > 0) {
                for (Address adr : addresses) {
                    if (adr.getLocality() != null && adr.getLocality().length() > 0) {
                        cityName = adr.getLocality();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;
    }

    private void setDefaultCityWeather(String city) {
        WeatherInterface weatherInterface = ApiClent.getRetrofitInstance().create(WeatherInterface.class);
        Call<CurrentDayApiResult> currentDayApiResultCall = weatherInterface.getCurrenDay(city, "metric", BuildConfig.ApiKey);
        // Single<List<CurrentDayApiResult>> single = appDatabase.weatherDao().getCurrentDayAll();
        currentDayApiResultCall.enqueue(new Callback<CurrentDayApiResult>() {
            @Override
            public void onResponse(Call<CurrentDayApiResult> call, Response<CurrentDayApiResult> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        tempText.setText(String.valueOf(response.body().getCurrentDayApi().getTemp()));
                        minTempText.setText(String.valueOf(response.body().getCurrentDayApi().getTemp_min()));
                        maxTempText.setText(String.valueOf(response.body().getCurrentDayApi().getTemp_max()));
                        humidityText.setText(String.valueOf(response.body().getCurrentDayApi().getHumidity()));
                        pressureText.setText(String.valueOf(response.body().getCurrentDayApi().getPressure()));
                        cityText.setText(String.valueOf(city));
                        CurrentDayApiResult currentDayApiResult = response.body();
                        currentDayApiResult.setCity(city);
                        mDisposable.add(weatherViewModel.insert(currentDayApiResult)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe()
                        );
                    }

                } else {
                    mDisposable.add(weatherViewModel.getList()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(weatherCurrentDay -> {
                                if (weatherCurrentDay != null) {
                                    tempText.setText(String.valueOf(weatherCurrentDay.getCurrentDayApi().getTemp()));
                                    minTempText.setText(String.valueOf(weatherCurrentDay.getCurrentDayApi().getTemp_min()));
                                    maxTempText.setText(String.valueOf(weatherCurrentDay.getCurrentDayApi().getTemp_max()));
                                    humidityText.setText(String.valueOf(weatherCurrentDay.getCurrentDayApi().getHumidity()));
                                    pressureText.setText(String.valueOf(weatherCurrentDay.getCurrentDayApi().getPressure()));
                                    cityText.setText(weatherCurrentDay.getCity());
                                } else {
                                    Toast.makeText(MainActivity.this, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                                }
                            })
                    );
                }

                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<CurrentDayApiResult> call, Throwable t) {
                mDisposable.add(weatherViewModel.getList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(weatherCurrentDay -> {
                            if (weatherCurrentDay != null) {
                                tempText.setText(String.valueOf(weatherCurrentDay.getCurrentDayApi().getTemp()));
                                minTempText.setText(String.valueOf(weatherCurrentDay.getCurrentDayApi().getTemp_min()));
                                maxTempText.setText(String.valueOf(weatherCurrentDay.getCurrentDayApi().getTemp_max()));
                                humidityText.setText(String.valueOf(weatherCurrentDay.getCurrentDayApi().getHumidity()));
                                pressureText.setText(String.valueOf(weatherCurrentDay.getCurrentDayApi().getPressure()));
                                cityText.setText(weatherCurrentDay.getCity());
                            } else {
                                Toast.makeText(MainActivity.this, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                            }
                        })
                );
                dialog.dismiss();
            }
        });
    }

    private void checkForLocationRequest() {
        dialog.show();
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setNumUpdates(1);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        mDisposable.clear();
    }

    private void checkForLocationSettings() {
        try {
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            builder.addLocationRequest(locationRequest);
            SettingsClient settingsClient = LocationServices.getSettingsClient(MainActivity.this);
            Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());
            task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    requestLocationUpate();
                }
            });

            task.addOnFailureListener(MainActivity.this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(), and check the
                                // result in onActivityResult().
                                ResolvableApiException resolvable = (ResolvableApiException) e;
                                resolvable.startResolutionForResult(MainActivity.this, REQUEST_PERMISSIONS_LOCATION_SETTINGS_REQUEST_CODE);
                            } catch (IntentSender.SendIntentException sie) {
                                sie.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            Toast.makeText(MainActivity.this, "Изменение настроек недоступно.Попробуйте на другом устройстве", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void requestLocationUpate() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSIONS_LOCATION_SETTINGS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                requestLocationUpate();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            checkForLocationRequest();
            checkForLocationSettings();
        }
        return super.onOptionsItemSelected(item);
    }
}
