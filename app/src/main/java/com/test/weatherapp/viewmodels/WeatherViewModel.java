package com.test.weatherapp.viewmodels;

import androidx.lifecycle.ViewModel;


import com.test.weatherapp.datasource.WeatherDataSource;
import com.test.weatherapp.models.CurrentDayApiResult;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public class WeatherViewModel extends ViewModel {
    private WeatherDataSource weatherDataSource;
    private CurrentDayApiResult mCurrentDayApiResult;

    public WeatherViewModel(WeatherDataSource weatherDataSource) {
        this.weatherDataSource = weatherDataSource;
    }

    public Flowable<CurrentDayApiResult> getList() {
        return weatherDataSource.getCurrentDayWeather().map(currentDayApiResult -> {
            mCurrentDayApiResult = currentDayApiResult;
            return currentDayApiResult;
        });
    }

    public Completable insert(CurrentDayApiResult currentDayApiResult) {
        return Completable.fromAction(() -> {
            mCurrentDayApiResult = mCurrentDayApiResult == null
                    ? new CurrentDayApiResult(currentDayApiResult.getCurrentDayApi(), currentDayApiResult.getCity()) :
                    new CurrentDayApiResult(currentDayApiResult.uid, currentDayApiResult.getCurrentDayApi(), currentDayApiResult.getCity());
            weatherDataSource.insert(mCurrentDayApiResult);
        });
    }

}
