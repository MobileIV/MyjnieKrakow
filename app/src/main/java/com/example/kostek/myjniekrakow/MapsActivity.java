package com.example.kostek.myjniekrakow;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment;
import com.example.kostek.myjniekrakow.fragments.InfoViewFragment;
import com.example.kostek.myjniekrakow.fragments.LogoutFragment;
import com.example.kostek.myjniekrakow.models.Wash;
import com.example.kostek.myjniekrakow.utils.BitmapCache;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.ObservableArrayMap;

import static com.example.kostek.myjniekrakow.utils.Constants.WASH;
import static com.example.kostek.myjniekrakow.utils.Constants.WASH_KEY;

public class MapsActivity extends AppCompatActivity
        implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener {


    private static final String LOG_TAG = MapsActivity.class.getSimpleName();
    private static final Integer PERMISSION_CODE = 1;
    private final InfoWindow.MarkerSpecification specs =
            new InfoWindow.MarkerSpecification(0, 118);
    private final HashMap<String, Marker> markers = new HashMap<>();
    private final ObservableArrayMap<String, Wash> washes = new ObservableArrayMap<>();
    private GoogleMap mMap;
    private InfoWindowManager windowManager;
    private BitmapCache bitmapCache;
    private boolean mPermissionDenied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        MapInfoWindowFragment mapFragment = (MapInfoWindowFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        windowManager = mapFragment.infoWindowManager();

        final FloatingActionButton openScanner = findViewById(R.id.scanner);
        bitmapCache = new BitmapCache(20, this);

        openScanner.setOnClickListener(v -> {
            //
        });

        dbSetup();
        washes.addOnMapChangedCallback(new MapListener());
    }

    private void dbSetup() {
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Washes");
        dbRef.keepSynced(true);
        dbRef.addChildEventListener(new ChildListener());
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
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CODE);
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
    public boolean onMarkerClick(Marker marker) {
        String key = (String) marker.getTag();
        Bundle bundle = new Bundle();
        bundle.putParcelable(WASH, washes.get(key));
        bundle.putString(WASH_KEY, key);
        InfoViewFragment fragment = new InfoViewFragment();
        fragment.setArguments(bundle);
        InfoWindow view = new InfoWindow(marker, specs, fragment);
        windowManager.toggle(view);
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.maps_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logOut:
                new LogoutFragment()
                        .show(getSupportFragmentManager(), "LogoutFragment");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ChildListener implements ChildEventListener {

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
            Wash wash = dataSnapshot.getValue(Wash.class);
            String key = dataSnapshot.getKey();
            if (wash != null) {
                washes.remove(key);
            }
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
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

            Marker marker;
            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(
                    bitmapCache.get(wash.freeSpots().toString()));
            LatLng position = new LatLng(wash.lat, wash.lng);
            String title = wash.name;

            if (markers.containsKey(key)) {
                marker = markers.get(key);
                marker.setIcon(icon);
                marker.setPosition(position);
                marker.setTitle(title);
            } else {
                marker = mMap.addMarker(
                        new MarkerOptions()
                                .position(position)
                                .title(title)
                                .icon(icon)
                );
            }
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
}
