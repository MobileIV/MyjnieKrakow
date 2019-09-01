package com.example.kostek.myjniekrakow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Pair;
import android.view.View;
import android.widget.Chronometer;
import android.widget.NumberPicker;

import com.example.kostek.myjniekrakow.models.Wash;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static com.example.kostek.myjniekrakow.utils.Constants.BASE;
import static com.example.kostek.myjniekrakow.utils.Constants.IS_TIMER;
import static com.example.kostek.myjniekrakow.utils.Constants.VALUE;
import static com.example.kostek.myjniekrakow.utils.Constants.WASH;
import static com.example.kostek.myjniekrakow.utils.Constants.WASH_KEY;

public class ReserveActivity extends AppCompatActivity implements ChildEventListener {

    private boolean firstRun = true;
    private Wash wash;
    private String dbKey;
    private int value = -1;
    private boolean isTimer = false;
    private Chronometer chronometer;
    private NumberPicker picker;
    private HashMap<String, Wash> washes;
    private MaterialButton button;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        firstRun = bundle == null;

        dbSetup();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            wash = extras.getParcelable(WASH);
            dbKey = extras.getString(WASH_KEY);
        }

        setContentView(R.layout.activity_reserve);

        washes = new HashMap<>();
        button = findViewById(R.id.start);
        button.setOnClickListener(e -> startTimer());

        chronometer = findViewById(R.id.timer);
        chronometer.setVisibility(View.INVISIBLE);
        chronometer.setSaveEnabled(true);
        chronometer.setOnChronometerTickListener(chr -> {
            if (SystemClock.elapsedRealtime() >= chr.getBase()) {
                chr.stop();
                hideTimer();
            }
        });

        picker = findViewById(R.id.picker);
        picker.setMaxValue(15);
        picker.setMinValue(1);
        picker.setOnValueChangedListener((np, oldV, v) -> {
            value = v;
        });
        value = picker.getValue();
    }

    private void showTimer() {
        button.setVisibility(View.INVISIBLE);
        picker.setVisibility(View.INVISIBLE);
        chronometer.setVisibility(View.VISIBLE);
    }

    private void hideTimer() {
        isTimer = false;
        button.setVisibility(View.VISIBLE);
        picker.setVisibility(View.VISIBLE);
        chronometer.setVisibility(View.INVISIBLE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTimer) {
            showTimer();
        } else {
            if (firstRun) {
                FusedLocationProviderClient locationClient
                        = LocationServices.getFusedLocationProviderClient(this);
                locationClient.getLastLocation().addOnSuccessListener(this::onLocationObtained);
            }
            hideTimer();
        }
    }

    private void startTimer() {
        isTimer = true;
        showTimer();
        chronometer.setCountDown(true);
        chronometer.setBase(SystemClock.elapsedRealtime() + 60 * 1000 * value);
        chronometer.start();
    }

    private void dbSetup() {
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Washes");
        dbRef.keepSynced(true);
        dbRef.addChildEventListener(this);
    }

    private void onLocationObtained(Location location) {
        Pair<String, Float> result = getNearestWashKeyDist(location);
        if (!dbKey.equals(result.first)) {
            View view = findViewById(R.id.snackbarView);
            Wash nearestWash = washes.get(result.first);
            if (nearestWash != null) {
                Snackbar.make(
                        view,
                        "Closer wash found at address " + nearestWash.address,
                        Snackbar.LENGTH_LONG
                ).setAction("Change Wash", e -> {
                    Intent intent = new Intent(this, ReserveActivity.class);
                    intent.putExtra(WASH, nearestWash);
                    intent.putExtra(WASH_KEY, result.first);
                    finish();
                    startActivity(intent);
                }).show();
            }
        }
    }

    private Pair<String, Float> getNearestWashKeyDist(Location location) {
        String nearestWash = null;
        float dist = 0;
        LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
        for (Map.Entry<String, Wash> entry : washes.entrySet()) {
            Wash wash = entry.getValue();
            String key = entry.getKey();
            float currDist = getDist(pos, new LatLng(wash.lat, wash.lng));
            if (nearestWash == null) {
                nearestWash = key;
                dist = currDist;
            } else if (dist > currDist) {
                dist = currDist;
                nearestWash = key;
            }
        }
        return new Pair<>(nearestWash, dist);
    }

    private float getDist(LatLng pos1, LatLng pos2) {
        float[] results = new float[1];
        Location.distanceBetween(
                pos1.latitude,
                pos1.longitude,
                pos2.latitude,
                pos2.longitude,
                results
        );
        return results[0];
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        Wash wash = dataSnapshot.getValue(Wash.class);
        String key = dataSnapshot.getKey();
        if (wash != null) {
            washes.put(key, wash);
        }
        if (dbKey.equals(key)) {
            this.wash = wash;
        }
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        Wash wash = dataSnapshot.getValue(Wash.class);
        String key = dataSnapshot.getKey();
        if (wash != null) {
            washes.put(key, wash);
        }
        if (dbKey.equals(key)) {
            this.wash = wash;
        }
    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
        String key = dataSnapshot.getKey();
        washes.remove(key);
    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        Wash wash = dataSnapshot.getValue(Wash.class);
        String key = dataSnapshot.getKey();
        if (wash != null) {
            washes.put(key, wash);
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(IS_TIMER, isTimer);
        if (isTimer) {
            editor.putString(WASH_KEY, dbKey);
            editor.putLong(BASE, chronometer.getBase());
        }
        editor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        boolean timerRunning = preferences.getBoolean(IS_TIMER, false);
        if (timerRunning) {
            isTimer = true;
            String washKey = preferences.getString(WASH_KEY, dbKey);
            if (!washKey.equals(dbKey)) {
                wash = washes.getOrDefault(washKey, wash);
            }
            chronometer.setBase(preferences.getLong(BASE, chronometer.getBase()));
            chronometer.start();
            chronometer.setCountDown(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putParcelable(WASH, wash);
        bundle.putString(WASH_KEY, dbKey);
        bundle.putInt(VALUE, value);
        bundle.putBoolean(IS_TIMER, isTimer);
        bundle.putLong(BASE, chronometer.getBase());
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        wash = bundle.getParcelable(WASH);
        dbKey = bundle.getString(WASH_KEY);
        value = bundle.getInt(VALUE);
        isTimer = bundle.getBoolean(IS_TIMER);
        if (value != -1) {
            picker.setValue(value);
        }
        if (isTimer) {
            chronometer.setBase(bundle.getLong(BASE));
            chronometer.start();
            chronometer.setCountDown(true);
        }
    }
}
