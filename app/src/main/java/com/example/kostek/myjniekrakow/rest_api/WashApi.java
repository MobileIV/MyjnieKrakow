package com.example.kostek.myjniekrakow.rest_api;

import com.example.kostek.myjniekrakow.models.Wash;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface WashApi {

    @GET("wash/wash_list")
    Call<List<Wash>> getAll();

}

