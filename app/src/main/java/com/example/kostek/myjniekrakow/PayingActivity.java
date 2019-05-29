package com.example.kostek.myjniekrakow;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.kostek.myjniekrakow.models.Wash;

import java.util.Objects;


public class PayingActivity  extends AppCompatActivity
        implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = PayingActivity.class.getSimpleName();
    private String wash_address;
    private EditText washInfo;
    private Integer spot_id;
    private String currency;
    private Float cost_per_min;
    private EditText minutesText;
    private EditText infoText;
    private Button payButton;
    private SeekBar minutesBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paying);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                finish();
            } else {
                spot_id = extras.getInt(getString(R.string.spot_id_key));
                currency = extras.getString(getString(R.string.currency_key));
                cost_per_min = extras.getFloat(getString(R.string.cost_per_min_key));
                wash_address = extras.getString(getString(R.string.wash_address_key));
                if (currency == null) {
                    currency = "PLN";
                }
            }
        }

        minutesText = findViewById(R.id.minutesText);
        infoText = findViewById(R.id.pay_info_text);
        minutesBar = findViewById(R.id.minutesBar);
        payButton = findViewById(R.id.pay_button);
        washInfo = findViewById(R.id.washInfoText);
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(
                        PayingActivity.this, "Dokonano płatności, możesz zacząć myć samochód", Toast.LENGTH_SHORT
                ).show();
                // jk
                finish();
            }
        });
        minutesBar.setOnSeekBarChangeListener(this);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        progressChanged(progress);
    }

    private void progressChanged(int progress) {
        minutesText.setText(""+progress);
        infoText.setText(String.format("Do zapłaty %.2f%s", progress * cost_per_min, currency));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        progressChanged(seekBar.getProgress());
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    @Override
    protected void onResume() {
        super.onResume();
        washInfo.setText(
                String.format(
                        "Upewnij się że jesteś na myjnii pod adresem %s" +
                                "\nNa miejscu nr %d", wash_address, spot_id));
        progressChanged(minutesBar.getProgress());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(getString(R.string.wash_address_key), wash_address);
        bundle.putInt(getString(R.string.spot_id_key), spot_id);
        bundle.putString(getString(R.string.currency_key), currency);
        bundle.putFloat(getString(R.string.cost_per_min_key), cost_per_min);
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        wash_address = bundle.getString(getString(R.string.wash_address_key));
        spot_id = bundle.getInt(getString(R.string.spot_id_key));
        currency = bundle.getString(getString(R.string.currency_key));
        cost_per_min = bundle.getFloat(getString(R.string.cost_per_min_key));
        super.onRestoreInstanceState(bundle);
    }

}
