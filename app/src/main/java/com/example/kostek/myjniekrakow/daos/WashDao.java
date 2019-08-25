package com.example.kostek.myjniekrakow.daos;

import com.example.kostek.myjniekrakow.models.Wash;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface WashDao {
    @Query("select * from Washes")
    List<Wash> getAllWashes();

    @Query("select * from Washes where id = :id")
    Wash getById(int id);

    @Insert
    void insertAll(List<Wash> washes);

    @Delete
    void delete(Wash wash);
}
