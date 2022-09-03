package com.example.floodaid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class MainActivity extends AppCompatActivity {

    ChipNavigationBar userChipNavigationBar;
    ChipNavigationBar adminChipNavigationBar;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId,userPrivilege;
    String[] data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fAuth = FirebaseAuth.getInstance();
        fStore= FirebaseFirestore.getInstance();
        userChipNavigationBar = findViewById(R.id.user_bottom_nav_menu);
        adminChipNavigationBar = findViewById(R.id.admin_bottom_nav_menu);

        if(fAuth.getCurrentUser() != null){
            userId = fAuth.getCurrentUser().getUid();
            DocumentReference documentReference = fStore.collection("users").document(userId);
            documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot.getString("isUser")!=null) {
                        //USER NAVIGATION BAR
                        userChipNavigationBar.setVisibility(userChipNavigationBar.VISIBLE);
                        userChipNavigationBar.setItemSelected(R.id.bottom_nav_real_data, true);
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RealTimeDataFragment()).commit();
                        bottomMenu();
                    }

                    if (documentSnapshot.getString("isAdmin")!=null) {
                        //ADMIN NAVIGATION BAR
                        adminChipNavigationBar.setVisibility(adminChipNavigationBar.VISIBLE);
                        adminChipNavigationBar.setItemSelected(R.id.bottom_nav_real_data, true);
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RealTimeDataFragment()).commit();
                        adminBottomMenu();
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.signOut){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Do you want to sign out ?");
            builder.setCancelable(true);

            builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), launch_screen.class));
                    Toast.makeText(MainActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

            builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.cancel();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        return true;
    }

    private void bottomMenu() {
        userChipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                Fragment fragment = null;
                switch (i){
                    case R.id.bottom_nav_real_data:
                        fragment = new RealTimeDataFragment();
                        break;

                    case R.id.bottom_nav_data:
                        fragment = new DataHistoryDateFragment();
                        break;

                    case R.id.bottom_nav_donation:
                        fragment = new DonationFragment();
                        break;

                    case R.id.bottom_nav_contact:
                        fragment = new ContactFragment();
                        break;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment).commit();
            }
        });
    }

    private void adminBottomMenu() {
        adminChipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                Fragment fragment = null;
                switch (i){
                    case R.id.bottom_nav_real_data:
                        fragment = new RealTimeDataFragment();
                        break;

                    case R.id.bottom_nav_data:
                        fragment = new DataHistoryDateFragment();
                        break;

                    case R.id.bottom_nav_contact:
                        fragment = new ContactFragment();
                        break;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment).commit();
            }
        });
    }

}