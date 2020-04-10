package com.test.weatherapp.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.test.weatherapp.models.CurrentDayApiResult;

import io.reactivex.Flowable;

@Dao
public interface WeatherDao {
    @Query("SELECT * FROM current_day_weather LIMIT 1")
    Flowable<CurrentDayApiResult> getCurrentDayWeather();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CurrentDayApiResult currentDayApiResult);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int update(CurrentDayApiResult currentDayApiResult);

}
