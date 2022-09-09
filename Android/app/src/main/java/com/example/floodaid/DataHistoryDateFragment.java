package com.example.floodaid;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.Arrays;
import java.util.Calendar;

public class DataHistoryDateFragment extends Fragment {

    int mYear, mMonth, mDay;
    String sizeCounter = "";
    String date = "";
    int counter = 0;
    TextView tvDate, tvRecordNum;
    Button calenderBtn, cancelBtn;
    int cancelStatus = 0;

    DatabaseReference database;

    RecyclerView rvDateDataHistory;
    DataHistoryDateAdapter dateDataHistoryAdapter;
    ArrayList<DataHistoryDateGetter> Datelist;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_history, container, false);
        autoLoad(view);
        //createDate(view);
        return view;
    }

    private void autoLoad(View v) {
        database = FirebaseDatabase.getInstance().getReference();


        tvRecordNum = v.findViewById(R.id.recordCount);
        cancelBtn = v.findViewById(R.id.cancelBtn);
        counter = 0;
        tvDate = v.findViewById(R.id.tvDatePicker);

        rvDateDataHistory = v.findViewById(R.id.rvDataHistoryDate);
        rvDateDataHistory.setLayoutManager(new LinearLayoutManager(v.getContext()));
        ;
        Datelist = new ArrayList<>();
        database = FirebaseDatabase.getInstance().getReference();
        dateDataHistoryAdapter = new DataHistoryDateAdapter(v.getContext(), Datelist);
        rvDateDataHistory.setAdapter(dateDataHistoryAdapter);

        database.child("DataHistory").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DataHistoryDateGetter dataHistory = dataSnapshot.getValue(DataHistoryDateGetter.class);
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

            private void filterDate(String date) {
                Datelist.clear();
                database.child("DataHistory").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            DataHistoryDateGetter dataHistory = dataSnapshot.getValue(DataHistoryDateGetter.class);
                            if (dataHistory.getDate().equals(date))
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
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelBtn.setVisibility(View.INVISIBLE);
                tvDate.setText("Please Select a Date");
                Datelist.clear();
                counter = 0;
                database.child("DataHistory").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            DataHistoryDateGetter dataHistory = dataSnapshot.getValue(DataHistoryDateGetter.class);
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
























/**


    private void createDate(View v) {
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
                        month=month+1;
                        calender.set(year,month,day);
                        date = day+"-"+month+"-"+year;
                        tvDate.setText(date);
                    }
                }, mYear, mMonth, mDay);
                //rvDataHistory.setVisibility(View.VISIBLE);
                //rvDateDataHistory.setVisibility(View.INVISIBLE);
                datePickerDialog.show();

                if(cancelStatus==1) {
                    tvRecordNum.setVisibility(View.VISIBLE);
                    rvDataHistory.setVisibility(View.VISIBLE);
                    cancelStatus = 0;
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelBtn.setVisibility(View.INVISIBLE);
                cancelStatus = 1;
                tvDate.setText("Please Select a Date");
                tvRecordNum.setVisibility(View.INVISIBLE);
                rvDataHistory.setVisibility(View.INVISIBLE);
                rvDateDataHistory.setVisibility(View.VISIBLE);
            }
        });


        //tvDate.addTextChangedListener(new TextWatcher() {
        //    @Override
        //    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        //    @Override
        //    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //        InitializeHisTimeDataCardView(v);
        //    }

        ///    @Override
         //   public void afterTextChanged(Editable editable) { }
        //});

    }

    //When date is selected
    private void InitializeHisTimeDataCardView(View v) {
        rvDataHistory = v.findViewById(R.id.rvDataHistory);

        rvDataHistory.setLayoutManager(new LinearLayoutManager(v.getContext()));
        list = new ArrayList<>();
        database = FirebaseDatabase.getInstance().getReference();

        dataHistoryAdapter = new DataHistoryAdapter (v.getContext(),list);
        rvDataHistory.setAdapter(dataHistoryAdapter);
        tvRecordNum = v.findViewById(R.id.recordCount);
        cancelBtn = v.findViewById(R.id.cancelBtn);
        counter=0;
        tvDate = v.findViewById(R.id.tvDatePicker);

        database.child("SensorData").child(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    tvRecordNum.setText("No data found on this date");
                }

                else{
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                        DataHistoryGetter dataHistory = dataSnapshot.getValue(DataHistoryGetter.class);
                        counter++;
                        if(counter == 1){
                            list.add(dataHistory);
                        }
                        sizeCounter = String.valueOf(counter);
                        tvRecordNum.setText(sizeCounter + " data found on "+date);
                    }
                    dataHistoryAdapter.notifyDataSetChanged();
                }
                if(cancelStatus != 1){
                    cancelBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }
}
**/

// when open date, date hidden, history shown
// when cancel. date visible, history hidden