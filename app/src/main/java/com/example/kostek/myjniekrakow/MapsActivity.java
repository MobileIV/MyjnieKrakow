package com.example.kostek.myjniekrakow;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Parcelable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.kostek.myjniekrakow.constants.Status;
import com.example.kostek.myjniekrakow.models.Wash;
import com.example.kostek.myjniekrakow.services.MainService;
import com.example.kostek.myjniekrakow.utils.BitmapCache;
import com.example.kostek.myjniekrakow.utils.MyResultReceiver;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

public class MapsActivity extends FragmentActivity
        implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        MyResultReceiver.Receiver {


    private GoogleMap mMap;
    private BitmapCache bitmapCache;
    private ArrayList<Marker> markers;
    private final Handler handler = new Handler();
    private FloatingActionButton openScanner;
    private boolean mPermissionDenied = false;

    private List<Wash> washes;

    private static final String LOG_TAG = MapsActivity.class.getSimpleName();
    private static final Integer ACTIVITY_REQUEST_CODE = 2;
    private static final Integer PERMISSION_CODE = 1;

    public MyResultReceiver mReceiver;
    private boolean isVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        openScanner = findViewById(R.id.scanner);
        isVisible = true;

        markers = new ArrayList<>();
        bitmapCache = new BitmapCache(20, this);
        mReceiver = new MyResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        openScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, ScannerActivity.class);
                startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
            }
        });

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        LatLng cracow = new LatLng(50.0647, 19.9450);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cracow, 12));
        enableLocation();
        if (washes != null) {
            populate_markers();
        }
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
        getWashes();
        isVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
    }

    private void getWashes() {
        Intent intent = new Intent(this, MainService.class);
        intent.putExtra(getString(R.string.receiver_key), mReceiver);
        intent.putExtra(getString(R.string.action_key), getString(R.string.get_wash_list_action));
        startService(intent);
    }

    private void populate_markers() {
        markers.clear();
        if (mMap == null)
            return;
        mMap.clear();
        for (int i = 0; i < washes.size(); i++) {
            Wash wash = washes.get(i);
            Marker marker = mMap.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(wash.lat, wash.lng))
                            .title(wash.name)
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmapCache.get(wash.freeSpots().toString())))
            );
            marker.setTag(i);
            markers.add(marker);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Intent intent = new Intent(this, WashActivity.class);
        int i = (int) marker.getTag();
        intent.putExtra(getString(R.string.wash_object_key), washes.get(i));
        startActivity(intent);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putParcelableArrayList(getString(R.string.washes_list_key), (ArrayList<? extends Parcelable>) washes);
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        washes =  bundle.getParcelableArrayList(getString(R.string.washes_list_key));
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        Log.d(LOG_TAG, "onReceiveResult: " + resultCode);
        if (resultCode == Status.SUCCESSFUL) {
            washes = resultData.getParcelableArrayList(getString(R.string.washes_list_key));
            populate_markers();
        }
        if (isVisible) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getWashes();
                }
            }, 2000);
        }
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
}
