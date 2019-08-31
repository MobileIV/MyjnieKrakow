package com.example.kostek.myjniekrakow;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import com.example.kostek.myjniekrakow.models.Wash;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ReserveActivity extends AppCompatActivity implements ChildEventListener {

    private Wash wash;
    private String key;
    private HashMap<String, Wash> washes;
    private FusedLocationProviderClient locationClient;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            wash = extras.getParcelable("wash_object");
            key = extras.getString("dbKey");
        }

        setContentView(R.layout.activity_reserve);
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        locationClient.getLastLocation().addOnSuccessListener(this::onLocationObtained);

        washes = new HashMap<>();

        dbSetup();
    }
    private void dbSetup() {
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Washes");
        dbRef.keepSynced(true);
        dbRef.addChildEventListener(this);
    }

    private void onLocationObtained(Location location) {
        Pair<String, Float> result = getNearestWashKeyDist(location);
        Toast.makeText(this, key + " " + result.first, Toast.LENGTH_SHORT).show();
        if (!key.equals(result.first)) {
            View view = findViewById(R.id.snackbarView);
            Wash nearestWash = washes.get(result.first);
            if (nearestWash != null) {
                Snackbar.make(
                        view,
                        "Closer wash found at address " + nearestWash.address,
                        Snackbar.LENGTH_LONG
                ).setAction("Change Wash", e -> {
                    Intent intent = new Intent(this, ReserveActivity.class);
                    intent.putExtra(getString(R.string.wash_object_key), nearestWash);
                    intent.putExtra("dbKey", result.first);
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
        for (Map.Entry<String, Wash> entry: washes.entrySet()) {
            Wash wash = entry.getValue();
            String key = entry.getKey();
            float currDist = getDist(pos, new LatLng(wash.lat, wash.lng));
            if (nearestWash == null) {
                nearestWash =  key;
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
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        Wash wash = dataSnapshot.getValue(Wash.class);
        String key = dataSnapshot.getKey();
        if (wash != null) {
            washes.put(key, wash);
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
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putParcelable(getString(R.string.wash_object_key), wash);
        bundle.putString("dbKey", key);
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        wash = bundle.getParcelable(getString(R.string.wash_object_key));
        key = bundle.getString("dbKey");
    }
}
