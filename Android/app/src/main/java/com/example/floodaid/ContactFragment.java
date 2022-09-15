package com.example.floodaid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
//import android.widget.SearchView;
import androidx.appcompat.widget.SearchView;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class ContactFragment extends Fragment {

    private FloatingActionButton contact;
    RecyclerView rvContact;
    ArrayList<ContactGetter> contactArrayList;
    ContactAdapter mContactAdapter;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    Button btnDelete, btnEdit, btnNavigate, btnCall, btnShelter;
    SwipeRefreshLayout swipeRefreshLayout;
    FloatingActionButton floatBtn;
    SearchView searchView;

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        rvContact = view.findViewById(R.id.rvContacts);
        rvContact.setHasFixedSize(true);
        rvContact.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        mAuth= FirebaseAuth.getInstance();
        contactArrayList = new ArrayList<ContactGetter>();
        mContactAdapter = new ContactAdapter(getContext(),contactArrayList);

        rvContact.setAdapter(mContactAdapter);

        contactArrayList.clear();
        EventChangeListener ();
        btnDelete = view.findViewById(R.id.btDelete);
        btnEdit = view.findViewById(R.id.btEdit);
        btnNavigate = view.findViewById(R.id.btNavigate);
        btnCall = view.findViewById(R.id.btCall);
        btnShelter = view.findViewById(R.id.btnEmergencyShelter);


        //when floaring button clicked
        contact = view.findViewById(R.id.floatingBtn);
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(view.getContext(), add_contact.class);
                startActivity(i);
            }
        });

        //refresh entire recycler view
        swipeRefreshLayout = view.findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                contactArrayList.clear();
                EventChangeListener ();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        //Disable floating button for user
        floatBtn = view.findViewById(R.id.floatingBtn);
        if(mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            DocumentReference documentReference = db.collection("users").document(userId);
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot.getString("isUser") != null) {
                        //USER
                        floatBtn.setVisibility(View.GONE);
                    }

                    if (documentSnapshot.getString("isAdmin") != null) {
                        //ADMIN
                        floatBtn.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        //recycler view change based on search result
        searchView = view.findViewById(R.id.searchView);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                filterList(s);
                return false;
            }
        });

        btnShelter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "clicked", Toast.LENGTH_SHORT).show();
                Fragment second = new EmergencyShelterFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.contactFragment,second).commit();
                btnShelter.setVisibility(View.GONE);
                rvContact.setVisibility(View.GONE);
                searchView.setVisibility(View.GONE);
                floatBtn.setVisibility(View.GONE);
                swipeRefreshLayout.setVisibility(View.GONE);
            }
        });

        return view;
    }

    private void filterList(String s) {
        ArrayList<ContactGetter> filteredContact = new ArrayList<>();
        for(ContactGetter item: contactArrayList){
            if(item.getOfficeName().toLowerCase().contains(s.toLowerCase())){
                filteredContact.add(item);
            }
        }
        if (filteredContact.isEmpty()){
            Toast.makeText(getContext(), "No Data Found", Toast.LENGTH_SHORT).show();
        }
        else{
            mContactAdapter.setFilteredList(filteredContact);
        }
    }

    private void EventChangeListener() {

        db.collection("contact").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                //if recycler view not empty
                if(!value.isEmpty()) {
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            contactArrayList.add(dc.getDocument().toObject(ContactGetter.class));
                        }
                        mContactAdapter.notifyDataSetChanged();
                    }
                }
                //run to ensure final element deleted
                else{
                    contactArrayList.clear();
                    mContactAdapter.notifyDataSetChanged();
                }
            }

        });
    }
}