package com.test.weatherapp.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.test.weatherapp.dao.WeatherDao;
import com.test.weatherapp.models.CurrentDayApiResult;


@Database(entities = {CurrentDayApiResult.class}, version = 1)
@TypeConverters(TypeConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract WeatherDao weatherDao();
    private static AppDatabase instance;
    public static synchronized AppDatabase getDatabse(Context context){
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),      AppDatabase.class, "weather_db")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
