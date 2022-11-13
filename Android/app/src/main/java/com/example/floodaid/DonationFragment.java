package com.example.floodaid;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DonationFragment extends Fragment {

    RecyclerView rvDonation;
    FloatingActionButton floatBtn;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    ArrayList<DonationGetter> donationArrayList;
    DonationAdapter mDonationAdapter;
    GridLayoutManager gridLayoutManager;
    SwipeRefreshLayout swipeRefreshLayout;
    SearchView searchView;
    Button sortBtn;
    String donerName;

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_donation, container, false);

        rvDonation = view.findViewById(R.id.rvDonations);
        rvDonation.setHasFixedSize(true);

        db = FirebaseFirestore.getInstance();
        mAuth= FirebaseAuth.getInstance();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);
        rvDonation.setLayoutManager(gridLayoutManager);

        donationArrayList = new ArrayList<DonationGetter>();
        mDonationAdapter = new DonationAdapter(getContext(),donationArrayList);

        donationArrayList.clear();
        reloadDonatedItems ();
        rvDonation.setAdapter(mDonationAdapter);

        sortBtn= view.findViewById(R.id.btnSort);

        floatBtn = view.findViewById(R.id.floatingBtn);
        floatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(view.getContext(), add_donation.class);
                startActivity(i);
            }
        });

        //refresh entire recycler view
        swipeRefreshLayout = view.findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                donationArrayList.clear();
                reloadDonatedItems ();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

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
                filterListName(s);
                return false;
            }
        });

        sortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSignedInName();
                filterList();
            }
        });
        return view;
    }

    private void reloadDonatedItems() {
        db.collection("donatedItems").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                //if recycler view not empty
                if(!value.isEmpty()) {
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            donationArrayList.add(dc.getDocument().toObject(DonationGetter.class));
                        }
                        mDonationAdapter.notifyDataSetChanged();
                    }
                }
                //run to ensure final element deleted
                else{
                    Toast.makeText(getContext(), "No donation items", Toast.LENGTH_SHORT).show();
                    donationArrayList.clear();
                    mDonationAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void filterListName(String s) {
        ArrayList<DonationGetter> filteredContact = new ArrayList<>();
        for(DonationGetter item: donationArrayList){
            if(item.getProductTitle().toLowerCase().contains(s.toLowerCase())){
                filteredContact.add(item);
            }
        }
        if (filteredContact.isEmpty()){
            Toast.makeText(getContext(), "No Data Found", Toast.LENGTH_SHORT).show();
        }
        else{
            mDonationAdapter.setFilteredList(filteredContact);
        }
    }

    private void getSignedInName(){
        if(mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            DocumentReference documentReference = db.collection("users").document(userId);
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    donerName = documentSnapshot.getString("fullName");
                }
            });
        }
    }

    private void filterList() {
        ArrayList<DonationGetter> filteredContact = new ArrayList<>();
        for(DonationGetter item: donationArrayList){
            if(item.getDonatorName().equals(donerName)){
                filteredContact.add(item);
            }
        }
        if (filteredContact.isEmpty()){
            Toast.makeText(getContext(), "You did not donate any item", Toast.LENGTH_SHORT).show();
        }
        else{
            mDonationAdapter.setFilteredList(filteredContact);
        }
    }
}