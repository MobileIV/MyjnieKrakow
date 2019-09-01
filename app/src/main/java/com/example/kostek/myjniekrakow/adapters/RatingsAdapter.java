package com.example.kostek.myjniekrakow.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.kostek.myjniekrakow.R;
import com.example.kostek.myjniekrakow.models.Rating;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RatingsAdapter extends RecyclerView.Adapter<RatingsAdapter.RatingViewHolder> {

    private ArrayList<Rating> ratings;

    public RatingsAdapter() {
    }

    @NonNull
    @Override
    public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.ratings_list_item, viewGroup, false);
        return new RatingViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder myViewHolder, int i) {
        Rating rating = ratings.get(i);
        String comment = rating.comment;

        // following google best practices
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDate = new SimpleDateFormat("kk:mm dd/MM/yyyy");

        String dateString = simpleDate.format(rating.date);
        myViewHolder.commentView.setText(comment);
        myViewHolder.dateView.setText(dateString);
        myViewHolder.rateBar.setRating(rating.rate);
    }

    @Override
    public int getItemCount() {
        if (ratings == null)
            return 0;
        return ratings.size();
    }

    public void setRatingsData(Collection<Rating> ratings) {
        this.ratings = new ArrayList<>(ratings);
        notifyDataSetChanged();
    }

    public void sort(SortOn s, final boolean reverse) {
        switch (s) {
            case RATE:
                Collections.sort(ratings, (o1, o2) -> {
                    if (reverse) {
                        return o2.rate.compareTo(o1.rate);
                    }
                    return o1.rate.compareTo(o2.rate);
                });
                break;
            case DATE:
                Collections.sort(ratings, (o1, o2) -> {
                    if (reverse) {
                        return o2.date.compareTo(o1.date);
                    }
                    return o1.date.compareTo(o2.date);
                });
                break;
        }
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return ratings.get(position).id;
    }

    public enum SortOn {
        RATE,
        DATE
    }

    public static class RatingViewHolder extends RecyclerView.ViewHolder {

        public TextView dateView;
        public RatingBar rateBar;
        public TextView commentView;

        public RatingViewHolder(View v) {
            super(v);
            rateBar = v.findViewById(R.id.wash_rating_bar);
            dateView = v.findViewById(R.id.wash_date);
            commentView = v.findViewById(R.id.wash_comment);
        }
    }

}
