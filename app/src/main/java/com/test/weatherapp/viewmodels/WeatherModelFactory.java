package com.test.weatherapp.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.test.weatherapp.datasource.WeatherDataSource;


public class WeatherModelFactory implements ViewModelProvider.Factory {
    private final WeatherDataSource weatherDataSource;

    public WeatherModelFactory(WeatherDataSource weatherDataSource) {
        this.weatherDataSource = weatherDataSource;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(WeatherViewModel.class)) {
            return (T) new WeatherViewModel(weatherDataSource);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
