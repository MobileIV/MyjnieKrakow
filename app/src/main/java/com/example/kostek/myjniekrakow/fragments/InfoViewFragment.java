package com.example.kostek.myjniekrakow.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kostek.myjniekrakow.R;
import com.example.kostek.myjniekrakow.ReserveActivity;
import com.example.kostek.myjniekrakow.WashActivity;
import com.example.kostek.myjniekrakow.models.Wash;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import static com.example.kostek.myjniekrakow.utils.Constants.WASH;
import static com.example.kostek.myjniekrakow.utils.Constants.WASH_KEY;

public class InfoViewFragment extends Fragment {

    private String dbKey;
    private Wash wash;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setup();

        final TextView text = view.findViewById(R.id.info_section);
        String info = wash.name + " " + wash.address;
        text.setText(info);

        view.findViewById(R.id.comment_section).setOnClickListener(e ->
                runActivity(WashActivity.class));
        view.findViewById(R.id.paying_section).setOnClickListener(e ->
                runActivity(ReserveActivity.class));

    }

    private void runActivity(Class<? extends Activity> clazz) {
        Intent intent = new Intent(getContext(), clazz);
        intent.putExtra(WASH, wash);
        intent.putExtra(WASH_KEY, dbKey);
        startActivity(intent);
        if (getFragmentManager() != null) {
            getFragmentManager()
                    .beginTransaction()
                    .remove(InfoViewFragment.this)
                    .commit();
        }
    }

    private void setup() {
        Bundle args = getArguments();
        if (args != null) {
            dbKey = args.getString(WASH_KEY);
            wash = args.getParcelable(WASH);
        }
    }
}
