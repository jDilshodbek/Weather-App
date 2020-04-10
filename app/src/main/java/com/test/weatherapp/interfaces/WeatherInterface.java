package com.test.weatherapp.interfaces;

import com.test.weatherapp.models.CurrentDayApiResult;
import com.test.weatherapp.models.WeatherApiResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherInterface {
    @GET("/data/2.5/forecast")
    Call<WeatherApiResult> getWeather(@Query("q") String cityName, @Query("APPID") String appID);

    @GET("/data/2.5/weather")
    Call<CurrentDayApiResult> getCurrenDay(@Query("q") String cityName,@Query("units") String units, @Query("APPID") String appID);
}
