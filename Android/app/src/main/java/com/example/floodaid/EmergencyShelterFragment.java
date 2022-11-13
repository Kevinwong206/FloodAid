package com.example.floodaid;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class EmergencyShelterFragment extends Fragment {

    Button btnContact, btnSort;
    SearchView searchView;
    SwipeRefreshLayout swipeRefreshLayout;
    FloatingActionButton floatBtn;
    RecyclerView rvShelter;

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    ArrayList<EmergencyShelterGetter> shelterArrayList;
    EmergencyShelterAdapter mEmergencyShelterAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency_shelter, container, false);
        btnContact = view.findViewById(R.id.btnEmergencyContact);
        btnSort = view.findViewById(R.id.btnShelterSort);
        searchView = view.findViewById(R.id.shelterSearch);
        rvShelter = view.findViewById(R.id.rvShelter);
        floatBtn = view.findViewById(R.id.floatingBtnShelter);
        swipeRefreshLayout = view.findViewById(R.id.swipeShelter);
        btnSort = view.findViewById(R.id.btnShelterSort);

        rvShelter.setHasFixedSize(true);
        rvShelter.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        mAuth= FirebaseAuth.getInstance();
        shelterArrayList = new ArrayList<EmergencyShelterGetter>();
        mEmergencyShelterAdapter = new EmergencyShelterAdapter(getContext(),shelterArrayList);

        rvShelter.setAdapter(mEmergencyShelterAdapter);
        reloadSheter ();
        sortDistance ();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                shelterArrayList.clear();
                reloadSheter ();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        enableFloatBtn (floatBtn);

        floatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), add_emergency_shelter.class);
                startActivity(i);
            }
        });

        //recycler view change based on search result
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

        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment second = new ContactFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.shelterFragment,second).commit();
                btnContact.setVisibility(View.GONE);
                btnSort.setVisibility(View.GONE);
                searchView.setVisibility(View.GONE);
                rvShelter.setVisibility(View.GONE);
                floatBtn.setVisibility(View.GONE);
                swipeRefreshLayout.setVisibility(View.GONE);
            }
        });

        return view;
    }

    private void enableFloatBtn(FloatingActionButton floatBtn) {
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
    }

    private void sortDistance() {
        btnSort.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                shelterArrayList.clear();
                db.collection("emergencyShelter").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        //if recycler view not empty
                        if(!value.isEmpty()) {
                            for (DocumentChange dc : value.getDocumentChanges()) {
                                if (dc.getType() == DocumentChange.Type.ADDED) {
                                    shelterArrayList.add(dc.getDocument().toObject(EmergencyShelterGetter.class));
                                }
                                Collections.sort(shelterArrayList, distanceComparator);
                                mEmergencyShelterAdapter.notifyDataSetChanged();
                            }
                        }
                        //run to ensure final element deleted
                        else{
                            shelterArrayList.clear();
                            mEmergencyShelterAdapter.notifyDataSetChanged();
                        }
                    }

                });
            }
        });
    }

    public static Comparator<EmergencyShelterGetter> distanceComparator = new Comparator<EmergencyShelterGetter>() {
        @Override
        public int compare(EmergencyShelterGetter p1, EmergencyShelterGetter p2) {

            Double d1 = Double.parseDouble(p1.getDistance());
            Double d2 = Double.parseDouble(p2.getDistance());
            return Double.compare(d1, d2);
        }
    };

    private void reloadSheter() {
        db.collection("emergencyShelter").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                //if recycler view not empty
                if(!value.isEmpty()) {
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            shelterArrayList.add(dc.getDocument().toObject(EmergencyShelterGetter.class));
                        }
                        mEmergencyShelterAdapter.notifyDataSetChanged();
                    }
                }
                //run to ensure final element deleted
                else{
                    shelterArrayList.clear();
                    mEmergencyShelterAdapter.notifyDataSetChanged();
                }
            }

        });
    }

    private void filterListName(String s) {
        ArrayList<EmergencyShelterGetter> filteredContact = new ArrayList<>();
        for(EmergencyShelterGetter item: shelterArrayList){
            if(item.getShelterName().toLowerCase().contains(s.toLowerCase())){
                filteredContact.add(item);
            }
        }
        if (filteredContact.isEmpty()){
            Toast.makeText(getContext(), "No Data Found", Toast.LENGTH_SHORT).show();
        }
        else{
            mEmergencyShelterAdapter.setFilteredList(filteredContact);
        }
    }
}