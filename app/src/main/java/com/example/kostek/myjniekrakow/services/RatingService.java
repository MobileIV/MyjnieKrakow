package com.example.kostek.myjniekrakow.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;

import com.example.kostek.myjniekrakow.R;
import com.example.kostek.myjniekrakow.constants.Status;
import com.example.kostek.myjniekrakow.models.Rating;
import com.example.kostek.myjniekrakow.rest_api.RatingApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RatingService extends IntentService {

    private List <Rating> ratings;

    public RatingService() {
        super("RatingService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        String action = intent.getStringExtra(getString(R.string.action_key));
        final ResultReceiver receiver = intent.getParcelableExtra(getString(R.string.receiver_key));

        if (action == null) {
            receiver.send(Status.FAILURE, Bundle.EMPTY);
            return;
        }

        if (action.equals(getString(R.string.post_comment_action))) {
            Integer id = intent.getIntExtra(getString(R.string.wash_id_key), -1);
            Float stars = intent.getFloatExtra(getString(R.string.stars_count_key), 0);
            String comment = intent.getStringExtra(getString(R.string.comment_key));
            post_comment(id, stars, comment, receiver);

        } else if (action.equals(getString(R.string.get_ratings_action))) {
            Integer id = intent.getIntExtra(getString(R.string.wash_id_key), -1);
            loadRatings(id, receiver);
        }
    }

    private void loadRatings(Integer id, final ResultReceiver receiver) {

        receiver.send(Status.RUNNING, Bundle.EMPTY);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.base_api_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RatingApi api = retrofit.create(RatingApi.class);
        Call<List<Rating>> call = api.getAllForWash(id.toString());

        call.enqueue(new Callback<List<Rating>>() {
            @Override
            public void onResponse(Call<List<Rating>> call, Response<List<Rating>> response) {
                if (response.isSuccessful()) {
                    ratings = response.body();
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList(getString(R.string.rating_array_key), (ArrayList<? extends Parcelable>) ratings);
                    receiver.send(Status.SUCCESSFUL, bundle);
                } else {
                    receiver.send(Status.FAILURE, Bundle.EMPTY);
                }
            }

            @Override
            public void onFailure(Call<List<Rating>> call, Throwable t) {
                receiver.send(Status.FAILURE, Bundle.EMPTY);
            }
        });
    }

    private void post_comment(Integer id, Float stars, String comment, final ResultReceiver receiver) {
        if (comment.isEmpty()) {
            comment = getString(R.string.no_comment);
        }
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.base_api_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RatingApi api = retrofit.create(RatingApi.class);

        Call<Rating> call = api.postRating(id, comment, stars);
        call.enqueue(new Callback<Rating>() {
            @Override
            public void onResponse(Call<Rating> call, Response<Rating> response) {

                if (response.isSuccessful()) {
                    receiver.send(Status.POST_SUCCESSFUL, Bundle.EMPTY);
                } else {
                    receiver.send(Status.POST_FAILURE, Bundle.EMPTY);
                }
            }

            @Override
            public void onFailure(Call<Rating> call, Throwable t) {
                receiver.send(Status.POST_FAILURE, Bundle.EMPTY);
            }
        });
    }
}
