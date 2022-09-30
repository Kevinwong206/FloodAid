package com.example.floodaid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RealTimeDataFragment extends Fragment {

    TextView dateText;
    TextView realTimeDistance, realTimeTemp,realTimeHumid, realCityName, realTimeWeather, realTimeWindSpeed, realTimeWindDirection;
    DatabaseReference rootDatabaseref;
    String temp,humidity,distance, cityName, weather, windSpeed, windDirection;
    ImageView imageWaterLevel, imageWeather, imageTemp, imageHumidity, imageWindSpeed, imageWindDirection, bgWaterLevel;
    ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_real_time_data, container, false);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        loadClock(view);
        getLocationNameData(view);
        getDistanceData(view);
        getWeatherData(view);
        getTempData(view);
        getHumidityData(view);
        getWindSpeedData(view);
        getWindDirectionData(view);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        },1500);

        return view;
    }


    private void loadClock(View v) {
        Thread t = new Thread() {
            @Override
            public void run () {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        if(getActivity() == null)
                            return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dateText = v.findViewById(R.id.dateText);
                                long date = System.currentTimeMillis();
                                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy   |   hh-mm-ss a");
                                String dateString = sdf.format(date);
                                dateText.setText(dateString);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        t.start();
    }

    private void getLocationNameData(View v) {
        realCityName = v.findViewById(R.id.locationName);
        rootDatabaseref = FirebaseDatabase.getInstance().getReference();

        rootDatabaseref.child("RealTimeData").child("CityName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cityName = snapshot.getValue().toString();
                realCityName.setText(cityName);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void getDistanceData(View v) {
        realTimeDistance = v.findViewById(R.id.realDistance);
        rootDatabaseref = FirebaseDatabase.getInstance().getReference();
        imageWaterLevel = v.findViewById(R.id.WaterLevelImage);
        bgWaterLevel = v.findViewById(R.id.imageView);

        rootDatabaseref.child("RealTimeData").child("WaterLevel").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                distance = snapshot.getValue().toString();
                distance = distance + " cm";
                realTimeDistance.setText(distance);
                float compareDistance = Float.parseFloat(snapshot.getValue().toString());

                if(compareDistance>=17){
                    imageWaterLevel.setImageResource(R.drawable.waterdanger);
                    bgWaterLevel.setImageResource(R.drawable.bgdanger);
                }else if(compareDistance>=16 && compareDistance<17){
                    imageWaterLevel.setImageResource(R.drawable.waterwarning);
                    bgWaterLevel.setImageResource(R.drawable.bgwarning);
                }else if(compareDistance>=14 && compareDistance<16){
                    imageWaterLevel.setImageResource(R.drawable.wateralert);
                    bgWaterLevel.setImageResource(R.drawable.bgalert);
                }else{
                    imageWaterLevel.setImageResource(R.drawable.waternormal);
                    bgWaterLevel.setImageResource(R.drawable.bgnormal);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void getWeatherData(View v) {
        realTimeWeather = v.findViewById(R.id.realWeather);
        rootDatabaseref = FirebaseDatabase.getInstance().getReference();
        imageWeather = v.findViewById(R.id.weatherImage);

        rootDatabaseref.child("RealTimeData").child("WeatherID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int compareID = Integer.valueOf(snapshot.getValue().toString());
                //change image
                //Send notification
                //Specify what level
                if(compareID>=200 && compareID<=232){
                    imageWeather.setImageResource(R.drawable.thunder);
                }else if(compareID>=300 && compareID<=321){
                    imageWeather.setImageResource(R.drawable.drizzle);
                }else if(compareID>=500 && compareID<=531){
                    imageWeather.setImageResource(R.drawable.rain);
                }else if(compareID>=701 && compareID<=781){
                    imageWeather.setImageResource(R.drawable.atmosphere);
                }else if(compareID==800){
                    imageWeather.setImageResource(R.drawable.clear);
                }else if(compareID==801){
                    imageWeather.setImageResource(R.drawable.fewcloud);
                }else if(compareID>=802 && compareID<=804){
                    imageWeather.setImageResource(R.drawable.cloud);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        rootDatabaseref.child("RealTimeData").child("WeatherDesc").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                weather = snapshot.getValue().toString();
                realTimeWeather.setText(weather);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void getTempData(View v) {
        realTimeTemp = v.findViewById(R.id.realTemp);
        rootDatabaseref = FirebaseDatabase.getInstance().getReference();
        imageTemp = v.findViewById(R.id.tempImage);

        imageTemp.setImageResource(R.drawable.temperature);
        rootDatabaseref.child("RealTimeData").child("Temperature").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                temp = snapshot.getValue().toString();
                temp = temp + " Celsius";
                realTimeTemp.setText(temp);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void getHumidityData(View v) {
        realTimeHumid = v.findViewById(R.id.realHumidity);
        rootDatabaseref = FirebaseDatabase.getInstance().getReference();
        imageHumidity = v.findViewById(R.id.humidImage);

        imageHumidity.setImageResource(R.drawable.humidity);
        rootDatabaseref.child("RealTimeData").child("Humidity").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                humidity = snapshot.getValue().toString();
                humidity = humidity + "%";
                realTimeHumid.setText(humidity);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void getWindSpeedData(View v) {
        realTimeWindSpeed = v.findViewById(R.id.realWindSpeed);
        imageWindSpeed = v.findViewById(R.id.windSpeedImage);
        rootDatabaseref = FirebaseDatabase.getInstance().getReference();

        imageWindSpeed.setImageResource(R.drawable.windspeed);
        rootDatabaseref.child("RealTimeData").child("WindSpeed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                windSpeed = snapshot.getValue().toString();
                windSpeed = windSpeed + " m/s";
                realTimeWindSpeed.setText(windSpeed);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void getWindDirectionData(View v) {
        realTimeWindDirection = v.findViewById(R.id.realWindDirection);
        imageWindDirection = v.findViewById(R.id.windDirectionImage);
        rootDatabaseref = FirebaseDatabase.getInstance().getReference();

        rootDatabaseref.child("RealTimeData").child("WindDegree").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                windDirection = snapshot.getValue().toString();
                int compareDirection = Integer.valueOf(snapshot.getValue().toString());
                windDirection = windDirection + " Degree";
                realTimeWindDirection.setText(windDirection);


                if(compareDirection>=0 && compareDirection<=22 || compareDirection>=338 && compareDirection<360 ){
                    imageWindDirection.setImageResource(R.drawable.n);
                }else if(compareDirection>=23 && compareDirection<=67){
                    imageWindDirection.setImageResource(R.drawable.ne);
                }else if(compareDirection>=68 && compareDirection<=112){
                    imageWindDirection.setImageResource(R.drawable.e);
                }else if(compareDirection>=113 && compareDirection<=157){
                    imageWindDirection.setImageResource(R.drawable.se);
                }else if(compareDirection>=158 && compareDirection<=202){
                    imageWindDirection.setImageResource(R.drawable.s);
                }else if(compareDirection>=203 && compareDirection<=247){
                    imageWindDirection.setImageResource(R.drawable.sw);
                }else if(compareDirection>=248 && compareDirection<=292){
                    imageWindDirection.setImageResource(R.drawable.w);
                }else if(compareDirection>=293 && compareDirection<=337){
                    imageWindDirection.setImageResource(R.drawable.nw);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

}