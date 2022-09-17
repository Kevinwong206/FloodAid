package com.example.floodaid;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class EmergencyShelterAdapter extends RecyclerView.Adapter<EmergencyShelterAdapter.MyViewHolder> {

    Context context;
    ArrayList<EmergencyShelterGetter> shelterArrayList;
    LayoutInflater inflater;
    String shelterName, shelterAddress, name, phone, max, current;
    FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    static int PERMISSION_CODE = 100;
    FusedLocationProviderClient fusedLocationProviderClient;
    double destinationLat, destinationLong, currentLat, currentLong;


    public EmergencyShelterAdapter(Context context, ArrayList<EmergencyShelterGetter> shelterArrayList) {
        this.context = context;
        this.shelterArrayList = shelterArrayList;
    }

    @NonNull
    @Override
    public EmergencyShelterAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.emergency_shelter_item,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //Get access to phone GPS
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }

        //Delay to wait and read phone current GPS and get coordinates
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                EmergencyShelterGetter shelterGetter = shelterArrayList.get(position);

                Geocoder geocoder = new Geocoder(context);
                List<Address> addressList;
                try {
                    addressList = geocoder.getFromLocationName(shelterGetter.shelterAddress,1 );
                    if (!addressList.isEmpty()){
                        destinationLat = addressList.get(0).getLatitude();
                        destinationLong = addressList.get(0).getLongitude();
                    }
                    else{
                        Toast.makeText(context, "Unable to convert to long and lat", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                holder.distancetv.setText(shelterGetter.distance+"km");
                holder.shelterNametv.setText(shelterGetter.shelterName);
                holder.contacttv.setText(shelterGetter.phone);
                holder.currenttv.setText(shelterGetter.currentCapacity);
                holder.maxtv.setText(shelterGetter.maxCapacity);
                holder.addresstv.setText(shelterGetter.shelterAddress);
                holder.mapBtn.setVisibility(View.VISIBLE);

                double finalCurrent = Double.parseDouble(shelterGetter.currentCapacity);
                double finalMax = Double.parseDouble(shelterGetter.maxCapacity);
                double percent = (finalCurrent / finalMax) * 100;
                int finalPercent = (int) percent;

                holder.percentagetv.setText(finalPercent + "%");
                
                holder.image.setImageResource(R.drawable.shelter_icon);
                holder.progressBar.setProgress(finalPercent);


                fStore = FirebaseFirestore.getInstance();
                mAuth = FirebaseAuth.getInstance();
                if (mAuth.getCurrentUser() != null) {
                    String userId = mAuth.getCurrentUser().getUid();
                    DocumentReference documentReference = fStore.collection("users").document(userId);
                    documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (documentSnapshot.getString("isUser") != null) {
                                //USER
                                holder.deleteBtn.setVisibility(View.GONE);
                                holder.editBtn.setVisibility(View.GONE);
                                holder.callBtn.setVisibility(View.VISIBLE);
                                holder.navigateBtn.setVisibility(View.VISIBLE);
                            }

                            if (documentSnapshot.getString("isAdmin") != null) {
                                //ADMIN
                                holder.callBtn.setVisibility(View.GONE);
                                holder.navigateBtn.setVisibility(View.GONE);
                                holder.deleteBtn.setVisibility(View.VISIBLE);
                                holder.editBtn.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CODE);
                }

                holder.mapBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(view.getContext(), EmergencyShelterMap.class);
                        i.putExtra("destinationLat",String.valueOf(destinationLat));
                        i.putExtra("destinationLong", String.valueOf(destinationLong));
                        i.putExtra("name", shelterGetter.shelterName);
                        context.startActivity(i);
                    }
                });

                holder.callBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String num = shelterGetter.phone;
                        Intent i = new Intent(Intent.ACTION_CALL);
                        i.setData(Uri.parse("tel:" + num));
                        context.startActivity(i);
                    }
                });

                holder.navigateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + String.valueOf(destinationLat) + "," + String.valueOf(destinationLong) + "&mode=d"));
                        intent.setPackage("com.google.android.apps.maps");
                        context.startActivity(intent);
                    }
                });

                holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fStore.collection("emergencyShelter").document(shelterGetter.shelterName).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Data Deleted, Please refresh page", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                holder.editBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(view.getContext(), add_emergency_shelter.class);
                        i.putExtra("shelterName", shelterGetter.shelterName);
                        i.putExtra("shelterAddress",shelterGetter.shelterAddress);
                        i.putExtra("currentCapacity", shelterGetter.currentCapacity);
                        i.putExtra("maxCapacity",shelterGetter.maxCapacity);
                        i.putExtra("name", shelterGetter.name);
                        i.putExtra("phone",shelterGetter.phone);
                        i.putExtra("distance", shelterGetter.distance);
                        context.startActivity(i);
                    }
                });
            }
        }, 1000);
    } //on bind


    @Override
    public int getItemCount() {
        return shelterArrayList.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView distancetv, shelterNametv, contacttv,  currenttv, maxtv, percentagetv, addresstv;
        Button mapBtn, callBtn, navigateBtn, deleteBtn, editBtn;
        ProgressBar progressBar;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageShelter);
            distancetv = itemView.findViewById(R.id.tvDistance);
            shelterNametv = itemView.findViewById(R.id.tvShelterName);
            contacttv = itemView.findViewById(R.id.tvContact);
            currenttv = itemView.findViewById(R.id.tvCurrent);
            maxtv = itemView.findViewById(R.id.tvMax);
            percentagetv = itemView.findViewById(R.id.tvProgressBar);
            addresstv = itemView.findViewById(R.id.tvAddress);
            mapBtn = itemView.findViewById(R.id.btnMap);
            callBtn = itemView.findViewById(R.id.btCall);
            navigateBtn = itemView.findViewById(R.id.btNavigate);
            deleteBtn = itemView.findViewById(R.id.btDelete);
            editBtn = itemView.findViewById(R.id.btEdit);
            progressBar = itemView.findViewById(R.id.progressBar);

        }
    }

    @SuppressLint("MissingPermission")
    public void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) context.getSystemService(
                LOCATION_SERVICE
        );

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();

                    if (location != null) {
                        currentLat = location.getLatitude();
                        currentLong = location.getLongitude();
                    }
                }
            });
        }
    }
}