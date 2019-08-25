package com.example.kostek.myjniekrakow.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.maps.android.ui.IconGenerator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

public class BitmapCache extends LruCache<String, Bitmap> {

    private IconGenerator iconGenerator;

    public BitmapCache(int maxSize, Context context) {
        super(maxSize);
        iconGenerator = new IconGenerator(context);
    }

    @Nullable
    @Override
    protected Bitmap create(@NonNull String key) {
        Integer number = Integer.valueOf(key);
        int color;
        if (number > 0) {
            color = IconGenerator.STYLE_GREEN;
        } else {
            color = IconGenerator.STYLE_RED;
        }
        iconGenerator.setStyle(color);
        return iconGenerator.makeIcon(key);
    }
}
