package com.example.kostek.myjniekrakow.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.ResultReceiver;

import com.example.kostek.myjniekrakow.R;
import com.example.kostek.myjniekrakow.constants.Status;
import com.example.kostek.myjniekrakow.db.AppDatabase;
import com.example.kostek.myjniekrakow.models.Wash;
import com.example.kostek.myjniekrakow.rest_api.WashApi;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainService extends IntentService {

    private List<Wash> washes;
    private DbAsyncTask dbTask;
    private AppDatabase db;

    public MainService() {
        super("MainService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbTask = new DbAsyncTask();
        db = AppDatabase.getInstance(getApplicationContext());
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

        if (action.equals(getString(R.string.get_wash_list_action))) {
            getWashes(receiver);
        }
    }

    private void getWashes(final ResultReceiver receiver) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.base_api_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WashApi api = retrofit.create(WashApi.class);
        Call<List<Wash>> call = api.getAll();
        call.enqueue(new Callback<List<Wash>>() {
            @Override
            public void onResponse(Call<List<Wash>> call, Response<List<Wash>> response) {
                Task task = new Task();
                if (response.isSuccessful()) {
                    washes = response.body();
                    if (washes != null && washes.isEmpty()) {
                        task.type = TaskType.GET;
                        task.washes = washes;
                        dbTask.execute(task);
                    }
                    success();
                } else {
                    if (washes != null && washes.isEmpty()) {
                        task.type = TaskType.SET;
                        task.washes = washes;
                        dbTask.execute(task);
                        success();
                        return;
                    }
                    receiver.send(Status.FAILURE, Bundle.EMPTY);
                }

            }

            @Override
            public void onFailure(Call<List<Wash>> call, Throwable t) {
                receiver.send(Status.FAILURE, Bundle.EMPTY);
            }

            private void success() {
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(getString(R.string.washes_list_key),
                        (ArrayList<? extends Parcelable>) washes);
                receiver.send(Status.SUCCESSFUL, bundle);
            }
        });
    }

    private class DbAsyncTask extends AsyncTask<Task, Void, String> {

        @Override
        protected String doInBackground(Task... tasks) {
            for (Task task: tasks) {
                if (task.type == TaskType.GET) {
                    task.washes = db.getWashDao().getAllWashes();
                } else if (task.type == TaskType.SET) {
                    db.getWashDao().insertAll(task.washes);
                }
            }
            return null;
        }
    }

    private class Task {
        TaskType type;
        List<Wash> washes;
    }

    private enum TaskType {
        GET,
        SET,
    }
}
