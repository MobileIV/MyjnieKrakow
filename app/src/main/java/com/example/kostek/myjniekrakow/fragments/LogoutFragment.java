package com.example.kostek.myjniekrakow.fragments;

import android.app.Dialog;
import android.os.Bundle;

import com.example.kostek.myjniekrakow.utils.LogoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class LogoutFragment extends DialogFragment {

    private static final String logoutMessage = "Are you sure?";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstance) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        return builder
                .setMessage(logoutMessage)
                .setPositiveButton("logout", (dialog, e) -> LogoutManager.logOut())
                .setNegativeButton("cancel", (dialog, e) -> {})
                .create();
    }


}
