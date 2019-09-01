package com.example.kostek.myjniekrakow.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import com.example.kostek.myjniekrakow.utils.LogoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class LogoutFragment extends DialogFragment {

    private static final String logoutMessage = "Are you sure you want to logout?";
    private static final String onLogout = "You have logout successfully";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstance) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        return builder
                .setMessage(logoutMessage)
                .setPositiveButton("logout", (dialog, e) -> {
                    LogoutManager.logOut();
                    Toast.makeText(getContext(), onLogout, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("cancel", (dialog, e) -> {
                })
                .create();
    }


}
