package com.example.kostek.myjniekrakow.utils;

import android.location.Location;
import android.util.Pair;

import com.example.kostek.myjniekrakow.models.Wash;
import com.google.android.gms.maps.model.LatLng;

import java.util.Map;

public class Helpers {
    public static Pair<String, Float> getNearestWashKeyDist(
            Location location, Map<String, Wash> washes) {
        String nearestWash = null;
        float dist = 0;
        LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
        for (Map.Entry<String, Wash> entry : washes.entrySet()) {
            Wash wash = entry.getValue();
            String key = entry.getKey();
            float currDist = getDist(pos, new LatLng(wash.lat, wash.lng));
            if (nearestWash == null) {
                nearestWash = key;
                dist = currDist;
            } else if (dist > currDist) {
                dist = currDist;
                nearestWash = key;
            }
        }
        return new Pair<>(nearestWash, dist);
    }

    private static float getDist(LatLng pos1, LatLng pos2) {
        float[] results = new float[1];
        Location.distanceBetween(
                pos1.latitude,
                pos1.longitude,
                pos2.latitude,
                pos2.longitude,
                results
        );
        return results[0];
    }
}
