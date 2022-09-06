package com.example.floodaid;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class sign_in extends AppCompatActivity {

    private Button redirect, signInClicked;
    private TextInputEditText mEmail, mPass;
    Boolean emailPassed = false;
    Boolean passpassed = false;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        redirect = findViewById(R.id.signUpBtnClicked);
        signInClicked = findViewById(R.id.signInBtnClicked);
        mEmail = findViewById(R.id.EditEmail);
        mPass = findViewById(R.id.EditPass);
        fAuth = FirebaseAuth.getInstance();

        redirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (sign_in.this, sign_up.class);
                startActivity(intent);
                finish();
            }
        });

        signInClicked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String pass = mPass.getText().toString().trim();

                //EMAIL
                if(TextUtils.isEmpty (email)){
                    mEmail.setError("Email cannot be empty");
                }else if(!email.matches("^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$")){
                    mEmail.setError("Email format incorrect");
                }
                else{
                    emailPassed = true;
                    mEmail.setError(null);
                }

                //PASSWORD
                if(TextUtils.isEmpty (pass)){
                    mPass.setError("Password cannot be empty");

                }else if(!(!pass.matches("[a-zA-Z]+") && !pass.matches("[0-9]+"))) {
                    mPass.setError("Password must contain alphabet and integer");
                }else if((pass.length() < 8)){
                    mPass.setError("Password must contain at least 8 characters");
                }
                else{
                    passpassed = true;
                    mPass.setError(null);
                }

                if(passpassed == true && emailPassed == true){
                    fAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(sign_in.this, "Log In Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            }else{
                                Toast.makeText(sign_in.this, "Error, " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent new_intent = new Intent(sign_in.this, launch_screen.class);
        this.startActivity(new_intent);
    }
}