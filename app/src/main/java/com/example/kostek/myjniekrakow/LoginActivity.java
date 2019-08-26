package com.example.kostek.myjniekrakow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private static final List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build()
    );

    private static final int RC_SIGN_IN = 123;
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    private Button login;
    private Button guest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = findViewById(R.id.sign_in);
        guest = findViewById(R.id.guest);

        login.setOnClickListener(e ->
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .build(),
                        RC_SIGN_IN)
        );

        guest.setOnClickListener(e -> startApp());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startApp();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                startApp();
            } else {
                if (response == null) {
                    Log.d(LOG_TAG, "onActivityResult: " + FirebaseAuth.getInstance().getCurrentUser());
                }
            }
        }
    }

    private void startApp() {
        startActivity(new Intent(this, MapsActivity.class));
    }

}
