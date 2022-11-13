package com.example.floodaid;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class sign_up extends AppCompatActivity{

    private TextInputEditText mFullName, mEmail, mPhoneNum, mPass, mPrivilege;
    private Button signUpClicked;
    Boolean namePassed = false;
    Boolean emailPassed = false;
    Boolean phonePassed = false;
    Boolean passPassed = false;
    Boolean userTypeChecked = false;
    CheckBox isUser, isAdmin;

    FirebaseAuth fAuth; //Firebase Authentication
    FirebaseFirestore fStore; //Firestore
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mFullName = findViewById(R.id.EditFullName);
        mEmail = findViewById(R.id.EditEmail);
        mPhoneNum = findViewById(R.id.EditPhoneNum);
        mPass = findViewById(R.id.EditPass);
        isUser = findViewById(R.id.cbIsUser);
        isAdmin = findViewById(R.id.cbIsAdmin);
        signUpClicked = findViewById(R.id.signUpBtnClicked);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        checkOneCheckBox();

        signUpClicked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = mFullName.getText().toString().trim();
                String email = mEmail.getText().toString().trim();
                String phoneNum = mPhoneNum.getText().toString().trim();
                String pass = mPass.getText().toString().trim();

                //FULL NAME
                validateFullName(fullName);
                //EMAIL
                validateEmail(email);
                //PHONE NUMBER
                validatePhone(phoneNum);
                //PASSWORD
                validatePassword(pass);
                //PRIVILEGE
                validateUserType();

                if(namePassed == true && emailPassed == true && phonePassed==true && passPassed == true && userTypeChecked==true) {
                        registerAccount(fullName,email,phoneNum,pass);
                }
            }
        });
    }

    void checkOneCheckBox() {
        isUser.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()){
                    isAdmin.setChecked(false);
                }
            }
        });

        isAdmin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()){
                    isUser.setChecked(false);
                }
            }
        });
    }

    void validateFullName(String fullName) {
        String tempFullName = fullName.replaceAll("\\s+","");
        if(TextUtils.isEmpty (fullName)){
            mFullName.setError("Username cannot be empty");
        } else if(!tempFullName.matches("[a-zA-Z]+")){
            mFullName.setError("Username must contain alphabets only");
        } else {
            namePassed = true;
            mFullName.setError(null);
        }
    }

    void validateEmail(String email) {
        //EMAIL
        if(TextUtils.isEmpty (email)){
            mEmail.setError("Email cannot be empty");
        }else if(!email.matches("^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$")){
            mEmail.setError("Email format incorrect");
        } else{
            emailPassed = true;
            mEmail.setError(null);
        }
    }

    void validatePhone(String phoneNum) {
        //Phone
        if(TextUtils.isEmpty (phoneNum)){
            mPhoneNum.setError("Phone Number cannot be empty");
        }else if(!phoneNum.matches("[0-9]+")){
            mPhoneNum.setError("Phone Number must only contain integer");
        }else if(!(phoneNum.length() == 10)){
            mPhoneNum.setError("Phone Number must have only 10 digits");
        } else{
            phonePassed = true;
            mPhoneNum.setError(null);
        }
    }

    void validatePassword(String pass) {
        //PASSWORD
        if(TextUtils.isEmpty (pass)){
            mPass.setError("Password cannot be empty");
        }else if(!(!pass.matches("[a-zA-Z]+") && !pass.matches("[0-9]+"))) {
            mPass.setError("Password must contain alphabet and integer");
        }else if((pass.length() < 8)){
            mPass.setError("Password must contain at least 8 characters");
        } else{
            passPassed = true;
            mPass.setError(null);
        }
    }

    void validateUserType() {
        if(!(isUser.isChecked() || isAdmin.isChecked())){
            Toast.makeText(sign_up.this, "Please select an account type", Toast.LENGTH_SHORT).show();
        }
        else if(isUser.isChecked() || isAdmin.isChecked()){
            userTypeChecked = true;
        }
    }

    void registerAccount(String fullName, String email, String phoneNum, String pass) {
        fAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(sign_up.this, "User Account Created", Toast.LENGTH_SHORT).show();
                    userID = fAuth.getCurrentUser().getUid(); // get user ID
                    DocumentReference documentReference = fStore.collection("users").document(userID); // document is based on user ID, collection is users
                    Map<String,Object> user = new HashMap<>();

                    user.put ("fullName", fullName);
                    user.put ("email", email);
                    user.put ("phoneNum", phoneNum);
                    user.put ("pass", pass);
                    if(isUser.isChecked()){
                        user.put("isUser", "1");
                    }
                    if(isAdmin.isChecked()){
                        user.put("isAdmin", "1");
                    }

                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    });
                    startActivity(new Intent(getApplicationContext(), launch_screen.class));
                } else {
                    Toast.makeText(sign_up.this, "Error, " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent new_intent = new Intent(sign_up.this, launch_screen.class);
        this.startActivity(new_intent);
    }
}