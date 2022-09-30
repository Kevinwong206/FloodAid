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
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTabHost;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;


public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder> {

    Context context;
    ArrayList<ContactGetter> contactArrayList;
    FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    static int PERMISSION_CODE = 100;
    FusedLocationProviderClient fusedLocationProviderClient;
    double destinationLat, destinationLong, currentLat, currentLong;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    public void setFilteredList (ArrayList<ContactGetter> filteredList){
        this.contactArrayList = filteredList;
        notifyDataSetChanged();
    }

    public ContactAdapter(Context context, ArrayList<ContactGetter> contactArrayList) {
        this.context = context;
        this.contactArrayList = contactArrayList;
    }

    @NonNull
    @Override
    public ContactAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_items,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.MyViewHolder holder, int position) {
        //Get access to phone GPS
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getCurrentLocation();
        }

        //Delay to wait and read phone current GPS and get coordinates
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ContactGetter contactGetter = contactArrayList.get(position);

                Geocoder geocoder = new Geocoder(context);
                List<Address> addressList;

                try {
                    addressList = geocoder.getFromLocationName(contactGetter.address,1 );
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

                //Compare the cooridnates with destination to get distance
                Location startPoint=new Location("");
                startPoint.setLatitude(currentLat);
                startPoint.setLongitude(currentLong);

                Location endPoint=new Location("");
                endPoint.setLatitude(destinationLat);
                endPoint.setLongitude(destinationLong);

                double distance=startPoint.distanceTo(endPoint);
                distance = distance/1000; //convert to km
                String finalDistance = df.format(distance);

                holder.tvOfficeName.setText(contactGetter.contactName);
                holder.tvAddress.setText(contactGetter.address);
                holder.tvPhone.setText(contactGetter.phoneNum);
                holder.tvState.setText(contactGetter.state);
                holder.tvDistance.setText(finalDistance+"km");

                if(contactGetter.contactType.equals("1")) {
                    holder.imageContactType.setImageResource(R.drawable.civil);
                }

                if(contactGetter.contactType.equals("2")) {
                    holder.imageContactType.setImageResource(R.drawable.bomba);
                }

                if(contactGetter.contactType.equals("3")) {
                    holder.imageContactType.setImageResource(R.drawable.police);
                }

                fStore = FirebaseFirestore.getInstance();
                mAuth= FirebaseAuth.getInstance();
                if(mAuth.getCurrentUser() != null){
                    String userId = mAuth.getCurrentUser().getUid();
                    DocumentReference documentReference = fStore.collection("users").document(userId);
                    documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (documentSnapshot.getString("isUser")!=null) {
                                //USER
                                holder.btnDelete.setVisibility(View.GONE);
                                holder.btnEdit.setVisibility(View.GONE);
                                holder.btnCall.setVisibility(View.VISIBLE);
                                holder.btnNavigate.setVisibility(View.VISIBLE);
                            }

                            if (documentSnapshot.getString("isAdmin")!=null) {
                                //ADMIN
                                holder.btnCall.setVisibility(View.GONE);
                                holder.btnNavigate.setVisibility(View.GONE);
                                holder.btnDelete.setVisibility(View.VISIBLE);
                                holder.btnEdit.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }

                if(ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions((Activity) context,new String[]{Manifest.permission.CALL_PHONE},PERMISSION_CODE);
                }

                holder.btnCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String num = contactGetter.phoneNum;
                        Intent i = new Intent (Intent.ACTION_CALL);
                        i.setData(Uri.parse("tel:"+num));
                        context.startActivity(i);
                    }
                });

                holder.btnNavigate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("google.navigation:q="+String.valueOf(destinationLat)+","+String.valueOf(destinationLong)+"&mode=d"));
                        intent.setPackage("com.google.android.apps.maps");
                        context.startActivity(intent);
                    }
                });

                holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fStore.collection("emergencyContact").document(contactGetter.contactName).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Data Deleted, Please refresh page", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(view.getContext(), add_contact.class);
                        i.putExtra("office", contactGetter.contactName);
                        i.putExtra("phone", contactGetter.phoneNum);
                        i.putExtra("add", contactGetter.address);
                        i.putExtra("state", contactGetter.state);
                        i.putExtra("type",contactGetter.contactType);
                        context.startActivity(i);
                    }
                });
            }
        }, 1000);
    }



    @Override
    public int getItemCount() {
        return contactArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tvOfficeName, tvPhone,tvAddress,tvState, tvDistance;
        ImageView imageContactType;
        Button btnDelete, btnEdit, btnNavigate, btnCall;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvOfficeName = itemView.findViewById(R.id.tvOfficeName);
            tvPhone = itemView.findViewById(R.id.tvContact);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvState = itemView.findViewById(R.id.tvStateName);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            imageContactType = itemView.findViewById(R.id.imageContact);
            btnDelete = itemView.findViewById(R.id.btDelete);
            btnEdit = itemView.findViewById(R.id.btEdit);
            btnNavigate = itemView.findViewById(R.id.btNavigate);
            btnCall = itemView.findViewById(R.id.btCall);
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