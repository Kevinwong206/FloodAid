package com.example.floodaid;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class launch_screen extends AppCompatActivity {
    private Button signIn, signUp, logOut;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    TextView msg;
    String userId, userPrivilege;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent serviceIntent = new Intent (this, BackgroundService.class);
        startService(serviceIntent);
        setContentView(R.layout.activity_launch_screen);
        signIn = findViewById(R.id.signInBtn);
        signUp = findViewById(R.id.signUpBtn);
        msg = findViewById(R.id.welcomeMSG);

        fAuth = FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        if(fAuth.getCurrentUser() != null){
            signIn.setEnabled(false);
            signUp.setEnabled(false);
            userId = fAuth.getCurrentUser().getUid();
            DocumentReference documentReference = fStore.collection("users").document(userId);
            documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    msg.setText("Welcome " + documentSnapshot.getString("fullName"));
                }
            });

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(launch_screen.this, MainActivity.class));
                    finish();
                }
            },2000);
        }
        else{
            signIn.setEnabled(true);
            signUp.setEnabled(true);
        }

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (launch_screen.this, sign_in.class);
                startActivity(intent);
                finish();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (launch_screen.this, sign_up.class);
                startActivity(intent);
                finish();
            }
        });

        //logOut.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        FirebaseAuth.getInstance().signOut();
        //        startActivity(new Intent(getApplicationContext(), launch_screen.class));
        //        finish();
        //    }
        //});

        //display a loading screen for 2 second
        //Handler handler = new Handler();
        //handler.postDelayed(new Runnable() {
        //    @Override
        //    public void run() {
        //        startActivity(new Intent(launch_screen.this, MainActivity.class));
        //        finish();
        //    }
        //},2000);




    }
}