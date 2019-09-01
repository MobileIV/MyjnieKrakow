package com.example.kostek.myjniekrakow.workers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.kostek.myjniekrakow.R;
import com.example.kostek.myjniekrakow.ReserveActivity;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static com.example.kostek.myjniekrakow.utils.Constants.NOTIFICATION;
import static com.example.kostek.myjniekrakow.utils.Constants.WASH_KEY;

public class NotificationWorker extends Worker {

    public NotificationWorker(@NonNull Context context,
                              @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String key = getInputData().getString(WASH_KEY);
        Intent intent = new Intent(getApplicationContext(), ReserveActivity.class);
        intent.putExtra(WASH_KEY, key);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentIntent(pendingIntent)
                .setContentText("Washer")
                .setContentTitle("Time's up")
                .setSmallIcon(R.drawable.ic_timer)
                .setAutoCancel(true)
                .build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
        managerCompat.notify(NOTIFICATION, notification);

        return Result.success();
    }
}
