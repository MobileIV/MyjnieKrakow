package com.example.kostek.myjniekrakow.rest_api;

import com.example.kostek.myjniekrakow.models.Rating;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RatingApi {

    @GET("wash/rating")
    Call<List<Rating>> getAllForWash(@Query("wash_id") String wash_id);

    @POST("wash/rating")
    @FormUrlEncoded
    Call<Rating> postRating(
            @Field("wash_id") Integer wash_id,
            @Field("comment") String comment,
            @Field("rate") Float rate
    );
}
