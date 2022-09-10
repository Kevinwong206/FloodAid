package com.example.floodaid;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class add_donation extends AppCompatActivity {

    ImageView imageDonate;
    TextInputEditText eProduct, eCondition, eQuantity, eAddress;

    Boolean imagePassed = false;
    Boolean productPassed = false;
    Boolean conditionPassed = false;
    Boolean quantityPassed = false;
    Boolean addressPassed = false;
    Button submitBtn;

    FirebaseAuth fAuth; //Firebase Authentication
    FirebaseFirestore fStore; //Firestore
    Uri imageUri;
    FirebaseStorage storage;
    StorageReference storageReference;
    StorageTask mUploadTask;
    String userId, name, phone;
    Uri downloadUrI;
    String title, condition, quantity, address;
    String passedAddress, passedCondition, passedTitle, passedQuantity, passedURL, passedId;
    String key = "empty";

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_donation);


        imageDonate = findViewById(R.id.imageDonate);
        eProduct = findViewById(R.id.etTitle);
        eCondition = findViewById(R.id.etCondition);
        eQuantity = findViewById(R.id.etQuantity);
        eAddress = findViewById(R.id.etAddress);
        submitBtn = findViewById(R.id.btnSubmit);

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        imageDonate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
                    //permission not grandted
                    if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, PERMISSION_CODE);
                    }else{
                        //permission granted
                        pickImageFromGallery();
                    }
                }
                else{
                    pickImageFromGallery();
                }
            }
        });

        Intent intent = getIntent();

        if (intent.hasExtra("address")){
            Bundle bundle = intent.getExtras();
            passedAddress = bundle.getString("address");
            passedCondition = bundle.getString("condition");
            passedTitle = bundle.getString("productTitle");
            passedQuantity = bundle.getString("quantity");
            passedURL = bundle.getString("imageUrL");
            passedId = bundle.getString("id");
            key=passedId;

            Picasso.get().load(passedURL).into(imageDonate);
            eProduct.setText(passedTitle);
            eCondition.setText(passedCondition);
            eQuantity.setText(passedQuantity);
            eAddress.setText(passedAddress);
        }
        if(key.equals("empty")){
            key = UUID.randomUUID().toString();
        }

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        if(fAuth.getCurrentUser() != null){
            userId = fAuth.getCurrentUser().getUid();
            DocumentReference documentReferencetest = fStore.collection("users").document(userId);
            documentReferencetest.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    name =  documentSnapshot.getString("fullName");
                    phone = documentSnapshot.getString("phoneNum");
                }
            });
        }

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = eProduct.getText().toString().trim();
                condition = eCondition.getText().toString().trim();
                quantity = eQuantity.getText().toString().trim();
                address = eAddress.getText().toString().trim();

                //Product Name
                if (TextUtils.isEmpty(title)) {
                    eProduct.setError("Product Name cannot be empty");
                } else {
                    productPassed = true;
                    eProduct.setError(null);
                }

                //Condition
                if (TextUtils.isEmpty(condition)) {
                    eCondition.setError("Condition cannot be empty");
                } else if (!condition.matches("[a-zA-Z]+")) {
                    eCondition.setError("Condition Type must contain alphabets only");
                } else {
                    conditionPassed = true;
                    eCondition.setError(null);
                }

                //Quantity
                if (TextUtils.isEmpty(quantity)) {
                    eQuantity.setError("Phone Number cannot be empty");
                } else if (!quantity.matches("[0-9]+")) {
                    eQuantity.setError("Phone Number must only contain integer");
                } else {
                    quantityPassed = true;
                    eQuantity.setError(null);
                }

                //ADDRESS
                if (TextUtils.isEmpty(address)) {
                    eAddress.setError("Address cannot be empty");
                } else if(!TextUtils.isEmpty(address)){
                    Geocoder geocoder = new Geocoder(add_donation.this);
                    List<Address> addressList;
                    try {
                        addressList = geocoder.getFromLocationName(address,1 );
                        //If valid address
                        if (!addressList.isEmpty()){
                            addressPassed = true;
                            eAddress.setError(null);
                        }
                        //if fail
                        else{
                            eAddress.setError("Address is invalid");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //error here, after print, wont run upload--------------------------------------------------------------------------------------------------------------------------------------------------------
                if (imagePassed==false){
                    Toast.makeText(add_donation.this, "Please a upload picture for the product", Toast.LENGTH_SHORT).show();
                    uploadPicture(key);
                }

                if (productPassed == true && conditionPassed == true && quantityPassed == true && addressPassed == true) { // && imagepassed == true) {
                    uploadPicture(key);
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void pickImageFromGallery() {
        Intent intent = new Intent (Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(add_donation.this, "Image permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE){
            imageDonate.setImageURI(data.getData());
            imageUri = data.getData();
            imagePassed=true;
        }
    }


    private void uploadPicture(String key) {

        if(imageUri!=null){

            StorageReference contactTypeRef = storageReference.child("donatedItems/"+key+"/image");
            mUploadTask = contactTypeRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(add_donation.this, "Image Upload Successful", Toast.LENGTH_SHORT).show();

                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful());
                            Uri downloadUrl = urlTask.getResult();


                            DocumentReference documentReference = fStore.collection("donatedItems").document(key);
                            Map<String,Object> donateItems = new HashMap<>();
                            donateItems.put("productTitle",title);
                            donateItems.put ("condition", condition);
                            donateItems.put("quantity",quantity);
                            donateItems.put ("address", address);
                            donateItems.put ("itemId", key);
                            donateItems.put ("donatorName", name);
                            donateItems.put ("donatorPhone", phone);
                            donateItems.put ("imageURL", downloadUrl.toString());

                            //Return to Donation page after successful upload
                            documentReference.set(donateItems).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Fragment backToDonation = new DonationFragment();
                                    androidx.fragment.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                                    fragmentTransaction.replace(R.id.add_donation_page,backToDonation);
                                    finish();
                                }
                            });
                        }
                    });
        }
    }

}







