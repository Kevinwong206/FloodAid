package com.example.floodaid;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

public class DonationDetails extends AppCompatActivity {

    private TextView mTextView;
    String pickupAddress, productCondition, donatorName, donatorPhone, productTitle, productQuantity, imageUrL,itemId;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    StorageReference storageReference;
    FirebaseStorage storage;
    double destinationLat, destinationLong, currentLat, currentLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_details);
        db = FirebaseFirestore.getInstance();
        mAuth= FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        TextView title = findViewById(R.id.tvProductName);
        TextView name = findViewById(R.id.tvdonatorName);
        TextView phone = findViewById(R.id.tvDonatorPhone);
        TextView quantity = findViewById(R.id.tvQuantity);
        TextView condition = findViewById(R.id.tvCondition);
        TextView address = findViewById(R.id.tvAddress);
        ImageView imageItem = findViewById(R.id.imageItem);

        loadIntent(title,name,phone,quantity,condition,address,imageItem);

        Button editBtn = findViewById(R.id.btnEdit);
        Button deleteBtn = findViewById(R.id.btnDelete);
        Button callBtn = findViewById(R.id.btnCall);
        Button navigateBtn = findViewById(R.id.btnNavigate);

        enableButtons(editBtn, deleteBtn);

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), add_donation.class);
                i.putExtra("address", pickupAddress);
                i.putExtra("condition",productCondition);
                i.putExtra("productTitle",productTitle);
                i.putExtra("quantity",productQuantity);
                i.putExtra("imageUrL",imageUrL);
                i.putExtra("id",itemId);
                startActivity(i);
            }
        });

        //delete firestore
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("donatedItems").document(itemId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //delete data storage
                        storageReference.child("donatedItems/"+itemId+"/image").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(DonationDetails.this, "Item Deleted", Toast.LENGTH_SHORT).show();
                                getSupportFragmentManager().beginTransaction().replace(R.id.donationDetails, new DonationFragment()).commit();
                            }
                        });
                    }
                });
            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String num =donatorPhone;
                Intent i = new Intent (Intent.ACTION_CALL);
                i.setData(Uri.parse("tel:"+num));
                startActivity(i);
            }
        });

        getDesCoordinates();
        navigateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("google.navigation:q="+String.valueOf(destinationLat)+","+String.valueOf(destinationLong)+"&mode=d"));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });
    }

    private void getDesCoordinates() {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addressList;
        try {
            addressList = geocoder.getFromLocationName(pickupAddress,1 );
            if (!addressList.isEmpty()){
                destinationLat = addressList.get(0).getLatitude();
                destinationLong = addressList.get(0).getLongitude();
            }
            else{
                Toast.makeText(this, "Unable to convert to long and lat", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enableButtons(Button editBtn, Button deleteBtn) {
        if(mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            DocumentReference documentReference = db.collection("users").document(userId);
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    String compareName = documentSnapshot.getString("fullName");
                    if (compareName.equals(donatorName)) {
                        //USER
                        editBtn.setVisibility(View.VISIBLE);
                        deleteBtn.setVisibility(View.VISIBLE);

                    }
                    else{
                        editBtn.setVisibility(View.GONE);
                        deleteBtn.setVisibility(View.GONE);
                    }
                }
            });
        }
    }


    private void loadIntent(TextView title, TextView name, TextView phone, TextView quantity, TextView condition, TextView address, ImageView imageItem) {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        pickupAddress = bundle.getString("address");
        productCondition = bundle.getString("condition");
        donatorName = bundle.getString("donatorName");
        donatorPhone = bundle.getString("donatorPhone");
        productTitle = bundle.getString("productTitle");
        productQuantity = bundle.getString("quantity");
        imageUrL = bundle.getString("imageUrL");
        itemId = bundle.getString("id");

        Picasso.get().load(imageUrL).into(imageItem);
        title.setText(productTitle);
        name.setText(donatorName);
        phone.setText(donatorPhone);
        quantity.setText(productQuantity);
        condition.setText(productCondition);
        address.setText(pickupAddress);
    }
}