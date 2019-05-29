package com.example.kostek.myjniekrakow;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kostek.myjniekrakow.adapters.RatingsAdapter;
import com.example.kostek.myjniekrakow.constants.Status;
import com.example.kostek.myjniekrakow.models.Rating;
import com.example.kostek.myjniekrakow.models.Wash;
import com.example.kostek.myjniekrakow.services.RatingService;
import com.example.kostek.myjniekrakow.utils.MyResultReceiver;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WashActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener, MyResultReceiver.Receiver {

    private static final String LOG_TAG = WashActivity.class.getSimpleName();
    private Wash wash;
    private TextView infoView;
    private RatingBar ratingBar;
    private EditText commentView;
    private List<Rating> ratings;
    private boolean isSortedByRate;
    private boolean isSortedByDate;
    private RatingsAdapter mAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton postCommentButton;
    private RecyclerView.LayoutManager layoutManager;

    public MyResultReceiver mReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wash);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                Log.d(LOG_TAG, "onCreate: no extras :(");
                finish();
            } else {
                wash = extras.getParcelable(getString(R.string.wash_object_key));
            }
        }


        infoView = findViewById(R.id.infoView);
        recyclerView = findViewById(R.id.recyclerview);
        ratingBar = findViewById(R.id.ratingBar);
        commentView = findViewById(R.id.commentText);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        postCommentButton = findViewById(R.id.fab);

        init();
        setListenersOnViews();
    }

    private void init() {
        mAdapter = new RatingsAdapter();
        mAdapter.setHasStableIds(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);

        mReceiver = new MyResultReceiver(new Handler());
        mReceiver.setReceiver(this);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        isSortedByDate = false;
        isSortedByRate = false;
        postCommentButton.hide();
    }

    private void setListenersOnViews() {
        commentView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    postCommentButton.show();
                } else {
                    InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                    postCommentButton.hide();
                }

            }
        });
        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                post_comment();
                commentView.clearFocus();
                commentView.setText("");
            }
        });
        swipeRefreshLayout.setOnRefreshListener(this);

    }

    private void post_comment() {
        Intent intent = new Intent(this, RatingService.class);
        intent.putExtra(getString(R.string.receiver_key), mReceiver);
        intent.putExtra(getString(R.string.wash_id_key), wash.id);
        intent.putExtra(getString(R.string.stars_count_key), ratingBar.getRating());
        intent.putExtra(getString(R.string.comment_key), commentView.getText().toString());
        intent.putExtra(getString(R.string.action_key), getString(R.string.post_comment));
        startService(intent);
    }

    private void get_info() {
        String text = "";
        text += String.format("Myjnia: %s\nAdres: %s", wash.name, wash.address);
        infoView.setText(text);
    }

    private void loadRatings() {
        Intent intent = new Intent(this, RatingService.class);
        intent.putExtra(getString(R.string.receiver_key), mReceiver);
        intent.putExtra(getString(R.string.wash_id_key), wash.id);
        intent.putExtra(getString(R.string.action_key), getString(R.string.get_ratings_action));
        startService(intent);
    }

    protected void onResume() {
        super.onResume();
        get_info();
        if (ratings == null) {
            loadRatings();
        } else {
            mAdapter.setRatingsData(ratings);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putParcelable(getString(R.string.wash_object_key), wash);
        bundle.putBoolean(getString(R.string.is_sorted_by_rate_key), isSortedByRate);
        bundle.putBoolean(getString(R.string.is_sorted_by_date_key), isSortedByDate);
        bundle.putParcelableArrayList(getString(R.string.ratings_list_key), (ArrayList<? extends Parcelable>) ratings);
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        wash = bundle.getParcelable(getString(R.string.wash_object_key));
        isSortedByRate = bundle.getBoolean(getString(R.string.is_sorted_by_rate_key));
        isSortedByDate = bundle.getBoolean(getString(R.string.is_sorted_by_date_key));
        ratings = bundle.getParcelableArrayList(getString(R.string.ratings_list_key));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.wash_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean reverse;
        switch (item.getItemId()) {
            case R.id.sort_by_rate:
                reverse = isSortedByRate;
                isSortedByRate = !isSortedByRate;
                mAdapter.sort(RatingsAdapter.SortOn.RATE, reverse);
                return true;
            case R.id.sort_by_date:
                reverse = isSortedByDate;
                isSortedByDate = !isSortedByDate;
                mAdapter.sort(RatingsAdapter.SortOn.DATE, reverse);
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefresh() {
        loadRatings();
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == Status.RUNNING) {
            swipeRefreshLayout.setRefreshing(true);
        } else if (resultCode == Status.SUCCESSFUL) {
            ratings = resultData.getParcelableArrayList(getString(R.string.rating_array_key));
            mAdapter.setRatingsData(ratings);
            isSortedByRate = false;
            isSortedByDate = false;
            swipeRefreshLayout.setRefreshing(false);
        } else if (resultCode == Status.FAILURE) {
            swipeRefreshLayout.setRefreshing(false);
        } else if (resultCode == Status.POST_FAILURE) {
            Toast.makeText(
                    WashActivity.this, R.string.on_comment_failure, Toast.LENGTH_SHORT
            ).show();
        } else if (resultCode == Status.POST_SUCCESSFUL) {
            Toast.makeText(WashActivity.this, R.string.on_successful_comment, Toast.LENGTH_SHORT).show();
            loadRatings();
        }
    }
}
