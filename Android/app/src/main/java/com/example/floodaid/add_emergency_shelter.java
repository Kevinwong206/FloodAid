package com.example.floodaid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class add_emergency_shelter  extends AppCompatActivity {

    TextInputEditText eShelterName, eShelterAddress, eName, ePhone, eMax, eCurrent;
    String shelterName, shelterAddress, name, phone, max, current;
    FirebaseAuth fAuth; //Firebase Authentication
    FirebaseFirestore fStore; //Firestore
    String key;
    double currentLat, currentLong, destinationLat, destinationLong;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    Boolean shelterNamePassed = false;
    Boolean shelterAddressPassed = false;
    Boolean namePassed = false;
    Boolean phonePassed = false;
    Boolean maxPassed = false;
    Boolean currentPassed = false;

    Button submitBtn;

    String passedShelterName, passedShelterAddress, passedMax, passedCurrent, passedName, passedPhone, passedDistance;
    int checkUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_emergency_shelter);

        Intent intent = getIntent();
        eShelterName = findViewById(R.id.EditOfficeName);
        eShelterAddress = findViewById(R.id.EditAddress);
        eName = findViewById(R.id.EditName);
        ePhone = findViewById(R.id.EditPhone);
        eMax = findViewById(R.id.EditMax);
        eCurrent = findViewById(R.id.EditCurrent);

        submitBtn = findViewById(R.id.createBtnClicked);

        checkIntent(intent);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shelterName = eShelterName.getText().toString().trim();
                shelterAddress = eShelterAddress.getText().toString().trim();
                name = eName.getText().toString().trim();
                phone = ePhone.getText().toString().trim();
                max = eMax.getText().toString().trim();
                current = eCurrent.getText().toString().trim();

                //Shelter NAME
                validateShelterName(shelterName);
                //Shelter Address
                validateShelterAddress(shelterAddress);
                //Person Name
                validateName(name);
                //Phone
                valdiatePhone(phone);
                //Max
                validateMax(max);
                //Current
                validateCurrent(current);

                if (shelterNamePassed == true && shelterAddressPassed == true && namePassed == true && phonePassed == true && maxPassed == true && currentPassed == true) {

                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(add_emergency_shelter.this);
                    if(ContextCompat.checkSelfPermission(add_emergency_shelter.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(add_emergency_shelter.this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        getCurrentLocation();
                    }

                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Geocoder geocoder = new Geocoder(add_emergency_shelter.this);
                            List<Address> addressList;

                            try {
                                addressList = geocoder.getFromLocationName(shelterAddress,1 );
                                if (!addressList.isEmpty()){
                                    destinationLat = addressList.get(0).getLatitude();
                                    destinationLong = addressList.get(0).getLongitude();
                                }
                                else{
                                    Toast.makeText(add_emergency_shelter.this, "Unable to convert to long and lat", Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            //Compare the cooridnates with destination to get distance
                            Location startPoint = new Location("");
                            startPoint.setLatitude(currentLat);
                            startPoint.setLongitude(currentLong);

                            Location endPoint = new Location("");
                            endPoint.setLatitude(destinationLat);
                            endPoint.setLongitude(destinationLong);

                            double distance = startPoint.distanceTo(endPoint);
                            distance = distance / 1000; //convert to km
                            String finalDistance = df.format(distance);

                            uploadEmergencyContact(finalDistance);
                        }
                    }, 1000);
                }
            }

            private void uploadEmergencyContact(String finalDistance) {
                DocumentReference documentReference = fStore.collection("emergencyShelter").document(shelterName);
                Map<String, Object> shelterItems = new HashMap<>();

                shelterItems.put("shelterName", shelterName);
                shelterItems.put("shelterAddress", shelterAddress);
                shelterItems.put("name", name);
                shelterItems.put("phone", phone);
                shelterItems.put("maxCapacity", max);
                shelterItems.put("currentCapacity", current);
                shelterItems.put("distance", finalDistance);

                documentReference.set(shelterItems).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(checkUpdate==0){
                            Toast.makeText(add_emergency_shelter.this, "Emergency Shelter Upload Successful", Toast.LENGTH_SHORT).show();
                        }
                        else if(checkUpdate==1){
                            Toast.makeText(add_emergency_shelter.this, "Emergency Shelter Update Successful", Toast.LENGTH_SHORT).show();
                        }
                        Fragment backToContact = new EmergencyShelterFragment();
                        androidx.fragment.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.add_shelter_page, backToContact);
                        finish();
                    }
                });
            }
        });
    }

    private void validateCurrent(String current) {
        if (TextUtils.isEmpty(current)) {
            eCurrent.setError("Current Capacity number cannot be empty");
        } else{
            int tempCurrent = Integer.parseInt(current);
            int tempMax = Integer.parseInt(max);
            if (!current.matches("[0-9]+")) {
                eCurrent.setError("Current Capacity number must only contain integer");
            } else if (tempCurrent>tempMax) {
                eCurrent.setError("Current Capacity cannot be larger than maximum capacity");
            } else {
                currentPassed = true;
                eCurrent.setError(null);
            }
        }
    }

    private void validateMax(String max) {
        if (TextUtils.isEmpty(max)) {
            eMax.setError("Max Capacity number cannot be empty");
        } else if (!max.matches("[0-9]+")) {
            eMax.setError("Max Capacity number must only contain integer");
        } else {
            maxPassed = true;
            eMax.setError(null);
        }
    }

    private void valdiatePhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            ePhone.setError("Phone Number cannot be empty");
        } else if (!phone.matches("[0-9]+")) {
            ePhone.setError("Phone Number must only contain integer");
        } else if (!(phone.length() == 10)) {
            ePhone.setError("Phone Number must have only 10 digits");
        } else {
            phonePassed = true;
            ePhone.setError(null);
        }
    }

    private void validateName(String name) {
        String tempName = name.replaceAll("\\s+", "");
        if (TextUtils.isEmpty(name)) {
            eName.setError("Name cannot be empty");
        } else if (!tempName.matches("[a-zA-Z]+")) {
            eName.setError("Name must contain alphabets only");
        } else {
            namePassed = true;
            eName.setError(null);
        }
    }

    private void validateShelterName(String shelterName) {
        if (TextUtils.isEmpty(shelterName)) {
            eShelterName.setError("Shelter Name cannot be empty");
        } else {
            shelterNamePassed = true;
            eShelterName.setError(null);
        }
    }

    private void validateShelterAddress(String shelterAddress) {
        if (TextUtils.isEmpty(shelterAddress)) {
            eShelterAddress.setError("Shelter Address cannot be empty");
        } else if(!TextUtils.isEmpty(shelterAddress)){
            Geocoder geocoder = new Geocoder(add_emergency_shelter.this);
            List<Address> addressList;
            try {
                addressList = geocoder.getFromLocationName(shelterAddress,1 );
                //If valid address
                if (!addressList.isEmpty()){
                    shelterAddressPassed = true;
                    eShelterAddress.setError(null);
                }
                //if fail
                else{
                    eShelterAddress.setError("Shelter Address is invalid");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void getCurrentLocation(){
        LocationManager locationManager = (LocationManager) getSystemService(
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

    private void checkIntent(Intent intent) {
        if (intent.hasExtra("shelterName")){
            Bundle bundle = intent.getExtras();
            passedShelterName = bundle.getString("shelterName");
            passedShelterAddress = bundle.getString("shelterAddress");
            passedCurrent = bundle.getString("currentCapacity");
            passedMax = bundle.getString("maxCapacity");
            passedName = bundle.getString("name");
            passedPhone = bundle.getString("phone");
            passedDistance = bundle.getString("distance");

            eShelterName.setText(passedShelterName);
            eShelterAddress.setText(passedShelterAddress);
            eName.setText(passedName);
            ePhone.setText(passedPhone);
            eCurrent.setText(passedCurrent);
            eMax.setText(passedMax);
            eShelterName.setEnabled(false);
            checkUpdate = 1;
        }
        else{
            checkUpdate = 0;
        }
    }

}