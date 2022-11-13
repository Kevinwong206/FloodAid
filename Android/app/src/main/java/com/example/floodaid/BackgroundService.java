package com.example.floodaid;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BackgroundService extends Service {

    DatabaseReference rootDatabaseref;
    int stageLevel = 100;
    int stopInitial = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while(true){
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    readDatabase();
                                }
                            });
                            try{
                                Thread.sleep(3000);
                            } catch (InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
        ).start();
        return super.onStartCommand(intent, flags, startId);
    }

    void readDatabase(){
        rootDatabaseref = FirebaseDatabase.getInstance().getReference();
        rootDatabaseref.child("RealTimeData").child("WaterLevel").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float distanceVal = Float.parseFloat(snapshot.getValue().toString());

                if(distanceVal>=17){
                    if(stageLevel!=1){
                        createNotification(1);
                    }
                    stageLevel = 1;
                }else if(distanceVal>=16 && distanceVal<17){
                    if(stageLevel!=2){
                        createNotification(2);
                    }
                    stageLevel = 2;
                }else if(distanceVal>=14 && distanceVal<16){
                    if(stageLevel!=3){
                        createNotification(3);
                    }
                    stageLevel = 3;
                }else{
                    if(stageLevel!=4 && stopInitial!=0){
                        createNotification(4);
                    }
                    stageLevel = 4;
                }
                stopInitial=1;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    void createNotification(int num) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My Notification", "My Notification", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "My Notification");
        if (num == 1){
            builder.setContentTitle("Danger");
            builder.setContentText("The current water level is at danger level");
        }
        else if (num == 2){
            builder.setContentTitle("Warning");
            builder.setContentText("The current water level is at warning level");
        }
        else if (num == 3){
            builder.setContentTitle("Alert");
            builder.setContentText("The current water level is at alert level");
        }
        else if (num == 4){
            builder.setContentTitle("Normal");
            builder.setContentText("The current water level is at normal level");
        }
        builder.setSmallIcon(R.drawable.charticon);
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_SOUND);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
        managerCompat.notify(1, builder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
