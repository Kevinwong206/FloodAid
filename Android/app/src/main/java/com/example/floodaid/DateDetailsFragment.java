package com.example.floodaid;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class DateDetailsFragment extends Fragment {

    int mYear, mMonth, mDay;
    String sizeCounter = "";
    String date = "";
    int counter = 0;
    TextView tvDate, tvRecordNum;
    Button calenderBtn, cancelBtn;
    int cancelStatus = 0;
    SwipeRefreshLayout swipeRefreshLayout;

    DatabaseReference database;

    RecyclerView rvDateDataHistory;
    DateDetailsAdapter dateDataHistoryAdapter;
    ArrayList<DateDetailsGetter> Datelist;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_history, container, false);
        swipeRefreshLayout = view.findViewById(R.id.swipeDate);
        getDatesData(view);
        calenderSelectDate (view);
        cancelBtnClicked();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Datelist.clear();
                getDatesData(view);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        //createDate(view);
        return view;
    }

    private void getDatesData(View v) {
        database = FirebaseDatabase.getInstance().getReference();

        tvRecordNum = v.findViewById(R.id.recordCount);
        cancelBtn = v.findViewById(R.id.cancelBtn);
        counter = 0;
        tvDate = v.findViewById(R.id.tvDatePicker);

        rvDateDataHistory = v.findViewById(R.id.rvDataHistoryDate);
        rvDateDataHistory.setLayoutManager(new LinearLayoutManager(v.getContext()));

        Datelist = new ArrayList<>();
        database = FirebaseDatabase.getInstance().getReference();
        dateDataHistoryAdapter = new DateDetailsAdapter(v.getContext(), Datelist);
        rvDateDataHistory.setAdapter(dateDataHistoryAdapter);

        database.child("DateDetails").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DateDetailsGetter dataHistory = dataSnapshot.getValue(DateDetailsGetter.class);
                    Datelist.add(dataHistory);
                    counter++;
                    sizeCounter = String.valueOf(counter);
                    tvRecordNum.setText(sizeCounter + " dates of data available");
                }
                dateDataHistoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    void calenderSelectDate(View v) {
        final Calendar calender = Calendar.getInstance();
        mYear = calender.get(Calendar.YEAR);
        mMonth = calender.get(Calendar.MONTH);
        mDay = calender.get(Calendar.DAY_OF_MONTH);

        calenderBtn = v.findViewById(R.id.searchBtn);
        tvDate = v.findViewById(R.id.tvDatePicker);

        calenderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        month = month + 1;
                        calender.set(year, month, day);
                        date = day + "-" + month + "-" + year;
                        tvDate.setText(date);
                        filterDate(date);
                    }
                }, mYear, mMonth, mDay);
                //rvDataHistory.setVisibility(View.VISIBLE);
                //rvDateDataHistory.setVisibility(View.INVISIBLE);
                datePickerDialog.show();

                if (cancelStatus == 1) {
                    tvRecordNum.setVisibility(View.VISIBLE);
                    cancelStatus = 0;
                }
            }
        });
    }

    private void filterDate(String date) {
        Datelist.clear();
        database.child("DateDetails").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DateDetailsGetter dataHistory = dataSnapshot.getValue(DateDetailsGetter.class);
                    if (dataHistory.getLastDate().equals(date))
                        Datelist.add(dataHistory);
                    if(Datelist.size()>0)
                        tvRecordNum.setText("1 date available");
                    else
                        tvRecordNum.setText("0 date available");
                    cancelBtn.setVisibility(View.VISIBLE);
                }
                dateDataHistoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    void cancelBtnClicked() {
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelBtn.setVisibility(View.INVISIBLE);
                tvDate.setText("Please Select a Date");
                Datelist.clear();
                counter = 0;
                database.child("DateDetails").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            DateDetailsGetter dataHistory = dataSnapshot.getValue(DateDetailsGetter.class);
                            Datelist.add(dataHistory);
                            counter++;
                            sizeCounter = String.valueOf(counter);
                            tvRecordNum.setText(sizeCounter + " dates available");
                        }
                        dateDataHistoryAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
    }

}