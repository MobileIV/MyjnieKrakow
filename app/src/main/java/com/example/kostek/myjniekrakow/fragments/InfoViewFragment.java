package com.example.kostek.myjniekrakow.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kostek.myjniekrakow.R;
import com.example.kostek.myjniekrakow.WashActivity;
import com.example.kostek.myjniekrakow.models.Wash;

import androidx.fragment.app.Fragment;

public class InfoViewFragment extends Fragment {

    private String dbKey;
    private Wash wash;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setup();

        final TextView text = view.findViewById(R.id.info_section);
        String info = wash.name + " lalal " + wash.address;
        text.setText(info);

        view.findViewById(R.id.comment_section).setOnClickListener(e -> {
            Intent intent = new Intent(getContext(), WashActivity.class);
            intent.putExtra(getString(R.string.wash_object_key), wash);
            intent.putExtra("dbKey", dbKey);
            startActivity(intent);
        });
        view.findViewById(R.id.paying_section).setOnClickListener(e -> {
            Toast.makeText(getContext(), "ALOHA", Toast.LENGTH_LONG).show();
        });

    }

    private void setup() {
        Bundle args = getArguments();
        if (args != null) {
            dbKey = args.getString("dbKey");
            wash = args.getParcelable("wash_object");
        }
    }
}
