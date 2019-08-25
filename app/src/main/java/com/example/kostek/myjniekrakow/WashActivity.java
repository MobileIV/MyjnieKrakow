package com.example.kostek.myjniekrakow;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.kostek.myjniekrakow.adapters.RatingsAdapter;
import com.example.kostek.myjniekrakow.models.Rating;
import com.example.kostek.myjniekrakow.models.Wash;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.ObservableArrayMap;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WashActivity extends AppCompatActivity {

    private static final String LOG_TAG = WashActivity.class.getSimpleName();

    private Wash wash;
    private String washKey;
    private TextView infoView;
    private RatingBar ratingBar;
    private EditText commentView;
    private boolean isSortedByRate;
    private boolean isSortedByDate;
    private RatingsAdapter mAdapter;
    private DatabaseReference dbRef;
    private RecyclerView recyclerView;
    private FloatingActionButton postCommentButton;
    private RecyclerView.LayoutManager layoutManager;
    private ObservableArrayMap<String, Rating> ratings;


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
                washKey = extras.getString("dbKey");
            }
        } else {
            washKey = savedInstanceState.getString("dbKey");
        }


        infoView = findViewById(R.id.infoView);
        recyclerView = findViewById(R.id.recyclerview);
        ratingBar = findViewById(R.id.ratingBar);
        commentView = findViewById(R.id.commentText);
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

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        isSortedByDate = false;
        isSortedByRate = false;
        postCommentButton.hide();


        ratings = new ObservableArrayMap<>();
        ratings.addOnMapChangedCallback(new MapListener());
        dbRef = FirebaseDatabase.getInstance().getReference("ratings/" + washKey);
        dbRef.addChildEventListener(new RatingsListener());
        dbRef.keepSynced(true);
    }

    private void setListenersOnViews() {
        commentView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    postCommentButton.show();
                } else {
                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
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
    }

    private void post_comment() {

        String key = dbRef.push().getKey();

        Rating rating = new Rating();
        rating.comment = commentView.getText().toString();
        rating.rate = ratingBar.getRating();
        rating.date = new Date();
        rating.id = rating.date.getTime() + rating.comment.hashCode();

        dbRef.child(key).setValue(rating);
    }

    private void get_info() {
        String text = "";
        text += String.format("Myjnia: %s\nAdres: %s", wash.name, wash.address);
        infoView.setText(text);
    }

    protected void onResume() {
        super.onResume();
        get_info();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putParcelable(getString(R.string.wash_object_key), wash);
        bundle.putString("dbKey", washKey);
        bundle.putBoolean(getString(R.string.is_sorted_by_rate_key), isSortedByRate);
        bundle.putBoolean(getString(R.string.is_sorted_by_date_key), isSortedByDate);
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        wash = bundle.getParcelable(getString(R.string.wash_object_key));
        washKey = bundle.getString("dbKey");
        isSortedByRate = bundle.getBoolean(getString(R.string.is_sorted_by_rate_key));
        isSortedByDate = bundle.getBoolean(getString(R.string.is_sorted_by_date_key));
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

    private class RatingsListener implements ChildEventListener {

        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Rating rating = dataSnapshot.getValue(Rating.class);
            String key = dataSnapshot.getKey();
            if (rating != null) {
                ratings.put(key, rating);
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Rating rating = dataSnapshot.getValue(Rating.class);
            String key = dataSnapshot.getKey();
            if (rating != null) {
                ratings.put(key, rating);
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getKey();
            ratings.remove(key);
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    }

    private class MapListener
            extends ObservableArrayMap.OnMapChangedCallback<ObservableArrayMap
            <String, Rating>, String, Rating> {
        @Override
        public void onMapChanged(ObservableArrayMap<String, Rating> sender, String key) {
            mAdapter.setRatingsData(sender.values());
        }
    }
}
