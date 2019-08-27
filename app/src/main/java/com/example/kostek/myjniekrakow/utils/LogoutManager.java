package com.example.kostek.myjniekrakow.utils;


import com.google.firebase.auth.FirebaseAuth;

public class LogoutManager {
    public static void logOut() {
        FirebaseAuth.getInstance().signOut();
    }
}
