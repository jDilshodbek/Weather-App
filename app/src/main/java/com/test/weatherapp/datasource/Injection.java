package com.test.weatherapp.datasource;


import android.content.Context;

import com.test.weatherapp.db.AppDatabase;
import com.test.weatherapp.viewmodels.WeatherModelFactory;

public class Injection {

    public static WeatherDataSource provideWeatherDateSource(Context context) {
        AppDatabase appDatabase = AppDatabase.getDatabse(context);
        return new LocalDataSource(appDatabase.weatherDao());
    }

    public static WeatherModelFactory provideViewModelFactory(Context context) {
        WeatherDataSource weatherDataSource = provideWeatherDateSource(context);
        return new WeatherModelFactory(weatherDataSource);
    }
}
