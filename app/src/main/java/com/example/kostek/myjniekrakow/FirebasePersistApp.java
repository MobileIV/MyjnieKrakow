package com.example.kostek.myjniekrakow;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class FirebasePersistApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
