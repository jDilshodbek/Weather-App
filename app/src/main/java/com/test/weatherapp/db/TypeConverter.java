package com.test.weatherapp.db;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.test.weatherapp.models.CurrentDayApi;

import java.lang.reflect.Type;

public class TypeConverter {
    @androidx.room.TypeConverter
    public static CurrentDayApi fromString(String value) {
        Type listType = new TypeToken<CurrentDayApi>() {
        }.getType();
        return new Gson().fromJson(value, listType);
    }

    @androidx.room.TypeConverter
    public static String fromArrayList(CurrentDayApi currentDayApi) {
        Gson gson = new Gson();
        return gson.toJson(currentDayApi);
    }
}
