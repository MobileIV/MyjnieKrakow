package com.example.kostek.myjniekrakow;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.kostek.myjniekrakow.models.Wash;
import com.example.kostek.myjniekrakow.utils.BitmapCache;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableArrayMap;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

public class MapsActivity extends FragmentActivity
        implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener {


    private GoogleMap mMap;
    private BitmapCache bitmapCache;
    private DatabaseReference dbRef;
    private FloatingActionButton openScanner;
    private boolean mPermissionDenied = false;

    private HashMap<String, Marker> markers;
    private ObservableArrayMap<String, Wash> washes;

    private static final String LOG_TAG = MapsActivity.class.getSimpleName();
    private static final Integer ACTIVITY_REQUEST_CODE = 2;
    private static final Integer PERMISSION_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        openScanner = findViewById(R.id.scanner);

        washes = new ObservableArrayMap<>();
        washes.addOnMapChangedCallback(new MapListener());
        markers = new HashMap<>();
        bitmapCache = new BitmapCache(20, this);

        openScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, ScannerActivity.class);
                startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
            }
        });

        dbRef = FirebaseDatabase.getInstance().getReference("Washes");
        dbRef.keepSynced(true);
        dbRef.addChildEventListener(new ChildListener());

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        LatLng cracow = new LatLng(50.0647, 19.9450);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cracow, 12));
        enableLocation();
    }

    private void enableLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CODE);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != PERMISSION_CODE) {
            return;
        }
        if (permissions.length > 0 && permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableLocation();
        } else {
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            Toast.makeText(this, R.string.permission_required_toast,
                    Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Intent intent = new Intent(this, WashActivity.class);
        String key = (String) marker.getTag();
        intent.putExtra(getString(R.string.wash_object_key), washes.get(key));
        intent.putExtra("dbKey", key);
        startActivity(intent);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult: " + resultCode);
        if (requestCode == ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Bundle bundle = data.getExtras();
            Intent intent = new Intent(this, PayingActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        } else  {
            Toast.makeText(
                    this, "Niepoprawny kod qr", Toast.LENGTH_SHORT
            ).show();
        }
    }

    private class ChildListener implements ChildEventListener {

        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Wash wash = dataSnapshot.getValue(Wash.class);
            String key = dataSnapshot.getKey();
            if (wash != null) {
                washes.put(key, wash);
            }
            Log.d(LOG_TAG, "onChildAdded: " + wash.address + " " + wash.lat + " " + wash.lng);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Wash wash = dataSnapshot.getValue(Wash.class);
            String key = dataSnapshot.getKey();
            if (wash != null) {
                washes.put(key, wash);
            }
            Log.d(LOG_TAG, "onChildAdded: " + wash.address + " " + wash.lat + " " + wash.lng);
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            Wash wash = dataSnapshot.getValue(Wash.class);
            String key = dataSnapshot.getKey();
            if (wash != null) {
                washes.remove(key);
            }
            Log.d(LOG_TAG, "onChildRemoved: " + wash.address);
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Wash wash = dataSnapshot.getValue(Wash.class);
            Log.d(LOG_TAG, "onChildMoved: " + wash.address);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(LOG_TAG, databaseError.getMessage());
        }
    }

    private class MapListener
            extends ObservableArrayMap.OnMapChangedCallback<ObservableArrayMap
            <String, Wash>, String, Wash> {
        @Override
        public void onMapChanged(ObservableArrayMap<String, Wash> sender, String key) {
            Wash wash = sender.get(key);
            if (wash == null) {
                deleteMarker(key);
            } else {
                addMarker(key, wash);
            }
        }

        private void addMarker(String key, Wash wash) {
            Log.d(LOG_TAG, "addMarker: " + wash.address);
            Marker marker = mMap.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(wash.lat, wash.lng))
                            .title(wash.name)
                            .icon(BitmapDescriptorFactory
                                    .fromBitmap(bitmapCache.get(wash.freeSpots().toString())))
            );
            marker.setTag(key);
            marker.setVisible(true);
            markers.put(key, marker);
        }

        private void deleteMarker(String key) {
            Marker marker = markers.get(key);
            if (marker != null) {
                marker.remove();
            }
            markers.remove(key);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}
