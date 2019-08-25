package com.example.kostek.myjniekrakow.daos;


import com.example.kostek.myjniekrakow.models.Rating;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface RatingDao {
    @Query("select * from Ratings")
    List<Rating> getAllRatings();

    @Query("select * from Ratings where id = :id")
    Rating getById(int id);

    @Query("select * from Ratings where wash_id = :wash_id")
    List<Rating> getAllForWash(int wash_id);

    @Insert
    void insertAll(Rating ...ratings);

    @Delete
    void delete(Rating rating);
}
