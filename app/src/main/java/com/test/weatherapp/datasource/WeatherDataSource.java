package com.test.weatherapp.datasource;

import com.test.weatherapp.models.CurrentDayApiResult;

import io.reactivex.Flowable;

public interface WeatherDataSource {
    Flowable<CurrentDayApiResult> getCurrentDayWeather();
    long insert(CurrentDayApiResult currentDayApiResult);
    int update(CurrentDayApiResult currentDayApiResult);
}
