package com.example.floodaid;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class add_contact extends AppCompatActivity {

    private TextInputEditText mOfficeName, mPhone, mAddress, mState;
    CheckBox cDefence, cFire, cPolice;
    Boolean officePassed = false;
    Boolean phonePassed = false;
    Boolean addressPassed = false;
    Boolean statepassed = false;
    Button createdClicked;


    FirebaseAuth fAuth; //Firebase Authentication
    FirebaseFirestore fStore; //Firestore
    Uri imageUri;
    FirebaseStorage storage;
    StorageReference storageReference;
    String generatedFilePath;
    String passedOffice, passedPhone, passedAdd, passedState, passedType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        Intent intent = getIntent();

        mOfficeName = findViewById(R.id.EditOfficeName);
        mPhone = findViewById(R.id.EditPhone);
        mAddress = findViewById(R.id.EditAddress);
        mState = findViewById(R.id.EditState);

        cDefence = findViewById(R.id.cbDefence);
        cFire = findViewById(R.id.cbFire);
        cPolice = findViewById(R.id.cbPolice);

        if (intent.hasExtra("office")){
            Bundle bundle = intent.getExtras();
            passedOffice = bundle.getString("office");
            passedPhone = bundle.getString("phone");
            passedAdd = bundle.getString("add");
            passedState = bundle.getString("state");
            passedType = bundle.getString("type");

            mOfficeName.setText(passedOffice);
            mPhone.setText(passedPhone);
            mAddress.setText(passedAdd);
            mState.setText(passedState);

            if (passedType.equals("1"))
                cDefence.setChecked(true);
            if (passedType.equals("2"))
                cFire.setChecked(true);
            if (passedType.equals("3"))
                cPolice.setChecked(true);

            mOfficeName.setEnabled(false);
            cDefence.setEnabled(false);
            cFire.setEnabled(false);
            cPolice.setEnabled(false);

        }


        createdClicked = findViewById(R.id.createBtnClicked);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        cDefence.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    cFire.setChecked(false);
                    cPolice.setChecked(false);
                }
            }
        });

        cFire.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    cDefence.setChecked(false);
                    cPolice.setChecked(false);
                }
            }
        });

        cPolice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    cDefence.setChecked(false);
                    cFire.setChecked(false);
                }
            }
        });

        createdClicked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String officeName = mOfficeName.getText().toString().trim();
                String phone = mPhone.getText().toString().trim();
                String address = mAddress.getText().toString().trim();
                String state = mState.getText().toString().trim();

                //OFFICE NAME
                String tempOfficeName = officeName.replaceAll("\\s+", "");
                String tempOfficeNametwo = officeName.toLowerCase();
                String typeOne = ("civil defence force");
                String typeTwo = ("fire and rescue department");
                String typeThree = ("police headquarters");

                //Checkbox checked
                if (!(cDefence.isChecked() || cFire.isChecked() || cPolice.isChecked())) {
                    Toast.makeText(add_contact.this, "Please select a contact type", Toast.LENGTH_SHORT).show();
                }

                //OFFICE NAME
                if (TextUtils.isEmpty(officeName)) {
                    mOfficeName.setError("Office Name cannot be empty");
                } else if (!tempOfficeName.matches("[a-zA-Z]+")) {
                    mOfficeName.setError("Office Name must contain alphabets only");
                } else if(tempOfficeNametwo.contains(typeOne)){
                    officePassed = true;
                    mOfficeName.setError(null);
                } else if(tempOfficeNametwo.contains(typeTwo)){
                    officePassed = true;
                    mOfficeName.setError(null);
                } else if(tempOfficeNametwo.contains(typeThree)){
                    officePassed = true;
                    mOfficeName.setError(null);
                } else {
                    mOfficeName.setError("Office Name must contain type of contact");
                }

                //Ensure checkbox and office type is same
                if((cDefence.isChecked() || cFire.isChecked() || cPolice.isChecked()) && !officeName.isEmpty()){
                    if(cDefence.isChecked()&&tempOfficeNametwo.contains(typeOne)){
                        officePassed = true;
                        mOfficeName.setError(null);
                    }else if(cFire.isChecked()&&tempOfficeNametwo.contains(typeTwo)){
                        officePassed = true;
                        mOfficeName.setError(null);
                    }else if(cPolice.isChecked()&&tempOfficeNametwo.contains(typeThree)){
                        officePassed = true;
                        mOfficeName.setError(null);
                    }else{
                        mOfficeName.setError("Contact type does not match with checkbox");
                        officePassed = false;
                    }
                }

                //PHONE NUMBER
                if (TextUtils.isEmpty(phone)) {
                    mPhone.setError("Phone Number cannot be empty");
                } else if (!phone.matches("[0-9]+")) {
                    mPhone.setError("Phone Number must only contain integer");
                } else if (!(phone.length() == 10)) {
                    mPhone.setError("Phone Number must have only 10 digits");
                } else {
                    phonePassed = true;
                    mPhone.setError(null);
                }

                addressPassed = true;
                mAddress.setError(null);

                //ADDRESS
                if (TextUtils.isEmpty(address)) {
                    mAddress.setError("Address cannot be empty");
                } else if(!TextUtils.isEmpty(address)){
                    Geocoder geocoder = new Geocoder(add_contact.this);
                    List<Address> addressList;
                    try {
                        addressList = geocoder.getFromLocationName(address,1 );
                        //If valid address
                        if (!addressList.isEmpty()){
                            addressPassed = true;
                            mAddress.setError(null);
                        }
                        //if fail
                        else{
                            mAddress.setError("Address is invalid");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //STATE
                String tempState = state.replaceAll("\\s+", "");
                if (TextUtils.isEmpty(state)) {
                    mState.setError("State cannot be empty");
                } else {
                    statepassed = true;
                    mState.setError(null);
                }




                if (officePassed == true && phonePassed == true && addressPassed == true && statepassed == true) { // && privilegePassed == true) {
                    DocumentReference documentReference = fStore.collection("emergencyContact").document(officeName);
                    Map<String,Object> contactItems = new HashMap<>();
                    int contactTypeID = 0;

                    if(cDefence.isChecked()){
                        contactTypeID = 1;
                        contactItems.put("contactType","1");
                    }
                    if(cFire.isChecked()){
                        contactTypeID = 2;
                        contactItems.put("contactType", "2");
                    }
                    if(cPolice.isChecked()){
                        contactTypeID = 3;
                        contactItems.put("contactType", "3");
                    }
                    //uploadPicture(contactTypeID);


                    contactItems.put("contactName",officeName);
                    contactItems.put ("phoneNum", phone);
                    contactItems.put("address",address);
                    contactItems.put ("state", state);

                    documentReference.set(contactItems).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Fragment backToContact = new ContactFragment();
                            androidx.fragment.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.container,backToContact);
                            finish();
                        }
                    });



                    }
                }

        });
    }

    private void uploadPicture(int contactTypeID) {
        if(contactTypeID == 1){
            imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + getResources().getResourcePackageName(R.drawable.police)
                    + '/' + getResources().getResourceTypeName(R.drawable.police) + '/' + getResources().getResourceEntryName(R.drawable.police) );
        }

        if(imageUri!=null){
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final String randomKey = UUID.randomUUID().toString();
            StorageReference contactTypeRef = storageReference.child("contactType/police");
            contactTypeRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(add_contact.this, "Image Upload Successful", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        }
}







