package com.example.kostek.myjniekrakow;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.widget.Toast;

import com.example.kostek.myjniekrakow.models.QRResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ScannerActivity extends Activity {

    private static final Integer PERMISSION_CODE = 1;
    private static final String LOG_TAG = MapsActivity.class.getSimpleName();

    private String lastText;
    private BeepManager beepManager;
    private Gson gson;
    private DecoratedBarcodeView barcodeView;
    private boolean mPermissionDenied = false;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if(result.getText() == null || result.getText().equals(lastText)) {
                return;
            }

            lastText = result.getText();
            beepManager.playBeepSoundAndVibrate();
            try {
                QRResult qrResult = gson.fromJson(lastText, QRResult.class);

                Intent output = new Intent();
                output.putExtra(getString(R.string.wash_address_key), qrResult.wash_address);
                output.putExtra(getString(R.string.spot_id_key), qrResult.spot_id);
                output.putExtra(getString(R.string.cost_per_min_key), qrResult.cost_per_min);
                output.putExtra(getString(R.string.currency_key), qrResult.currency);
                setResult(Activity.RESULT_OK, output);
            }
            catch (Exception e) {
                setResult(Activity.RESULT_CANCELED, new Intent());
            }
            finish();
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        barcodeView = findViewById(R.id.barcode_scanner);

        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeView.decodeContinuous(callback);

        beepManager = new BeepManager(this);

        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();
        gson.serializeNulls();

        requestPermissions();
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPermissionDenied) {
            Toast.makeText(this, R.string.camera_permission_required_toast, Toast.LENGTH_SHORT).show();
            finish();
        }
        barcodeView.setStatusText(getString(R.string.scanner_status_text));
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != PERMISSION_CODE) {
            return;     
        }
        mPermissionDenied = permissions.length <= 0 || !permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)
                || grantResults[0] != PackageManager.PERMISSION_GRANTED;
    }


}
