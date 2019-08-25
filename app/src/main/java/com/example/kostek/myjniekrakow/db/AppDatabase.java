package com.example.kostek.myjniekrakow.db;

import android.content.Context;

import com.example.kostek.myjniekrakow.converters.Converters;
import com.example.kostek.myjniekrakow.daos.RatingDao;
import com.example.kostek.myjniekrakow.daos.WashDao;
import com.example.kostek.myjniekrakow.models.Rating;
import com.example.kostek.myjniekrakow.models.Wash;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;


@Database(entities = {Wash.class, Rating.class}, version = 2)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;
    private static final String DB_NAME= "db";

    public abstract WashDao getWashDao();
    public abstract RatingDao getRatingDao();

    protected AppDatabase() {
    }

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, AppDatabase.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
