package com.test.weatherapp.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.test.weatherapp.db.TypeConverter;

@Entity(tableName = "current_day_weather")
public class CurrentDayApiResult {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "userid")
    public int uid;
    @TypeConverters(TypeConverter.class)
    @SerializedName("main")
    @Expose
    @ColumnInfo(name = "current_day")
    private CurrentDayApi currentDayApi;

    @ColumnInfo(name = "city")
    private String city;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCurrentDayApi(CurrentDayApi currentDayApi) {
        this.currentDayApi = currentDayApi;
    }

    public CurrentDayApi getCurrentDayApi() {
        return currentDayApi;
    }

    public CurrentDayApiResult(int uid, CurrentDayApi currentDayApi, String city) {
        this.uid = uid;
        this.currentDayApi = currentDayApi;
        this.city = city;
    }

    @Ignore
    public CurrentDayApiResult(CurrentDayApi currentDayApi, String city) {
        this.currentDayApi = currentDayApi;
        this.city = city;
    }
}
