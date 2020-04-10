package com.test.weatherapp.datasource;

import com.test.weatherapp.dao.WeatherDao;
import com.test.weatherapp.models.CurrentDayApiResult;

import io.reactivex.Flowable;

public class LocalDataSource implements WeatherDataSource {

    private final WeatherDao weatherDao;

    public LocalDataSource(WeatherDao weatherDao) {
        this.weatherDao = weatherDao;
    }

    @Override
    public Flowable<CurrentDayApiResult> getCurrentDayWeather() {
        return weatherDao.getCurrentDayWeather();
    }

    @Override
    public long insert(CurrentDayApiResult currentDayApiResult) {
        return weatherDao.insert(currentDayApiResult);
    }

    @Override
    public int update(CurrentDayApiResult currentDayApiResult) {
        return weatherDao.update(currentDayApiResult);
    }
}
