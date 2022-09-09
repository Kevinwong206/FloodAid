package com.example.floodaid;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DataGraph extends AppCompatActivity {

    DatabaseReference database, database2;
    private LineChart mChart;
    int counter = 0;
    ArrayList<Entry> yValues;
    ArrayList<String> xAxisLabel;
    String date,showGraph;

    RecyclerView rvDataHistory;
    DataDetailsAdapter mDataDetailsAdapter;
    ArrayList<DataDetailsGetter> list;
    DatabaseReference database3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_history_graph);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        date = bundle.getString("passingDate");
        showGraph = bundle.getString("showGraph");

        // --------------------------------------------------------------Recycler View
        rvDataHistory = findViewById(R.id.rvDataHistoryList);
        list = new ArrayList<DataDetailsGetter>();

        database3 = FirebaseDatabase.getInstance().getReference();
        mDataDetailsAdapter = new DataDetailsAdapter(this, list);
        rvDataHistory.setLayoutManager(new LinearLayoutManager(this));
        rvDataHistory.setAdapter(mDataDetailsAdapter);


        database3.child("SensorData").child(date).addListenerForSingleValueEvent(new ValueEventListener() {
            int counter =0;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    DataDetailsGetter dataHistory = dataSnapshot.getValue(DataDetailsGetter.class);
                    list.add(dataHistory);
                    counter++;
                }
                mDataDetailsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //----------------------------------------------------------------Graph
        if(showGraph.equals("1")){
            mChart = (LineChart) findViewById(R.id.lineChart);

            mChart.setDragEnabled(true);
            mChart.setScaleEnabled(false);
            mChart.setNoDataText("Tap to Generate Graph");

            //The line to show level
            LimitLine upper_limit = new LimitLine(5f, "Danger");
            upper_limit.setLineWidth(4f);
            upper_limit.enableDashedLine(10f,10f,0f);
            upper_limit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
            upper_limit.setTextSize(20f);

            //The line to show level
            LimitLine lower_limit = new LimitLine(10f, "Warning");
            upper_limit.setLineWidth(4f);
            upper_limit.enableDashedLine(10f,10f,0f);
            upper_limit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
            upper_limit.setTextSize(20f);

            //The line to show level
            LimitLine alert_Limit = new LimitLine(15f, "Alert");
            upper_limit.setLineWidth(4f);
            upper_limit.enableDashedLine(10f,10f,0f);
            upper_limit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
            upper_limit.setTextSize(15f);

            YAxis leftAxis = mChart.getAxisLeft();
            leftAxis.removeAllLimitLines();
            leftAxis.addLimitLine(upper_limit);
            leftAxis.addLimitLine(lower_limit);
            leftAxis.addLimitLine(alert_Limit);

            //The Y axis maximum number shown
            leftAxis.setAxisMaximum(40f);
            leftAxis.setAxisMinimum(0f);
            leftAxis.enableGridDashedLine(10f,10f,0);
            leftAxis.setDrawTopYLabelEntry(true);
            mChart.getAxisRight().setEnabled(false);

            //Firebase get data
            database = FirebaseDatabase.getInstance().getReference();
            database2 = FirebaseDatabase.getInstance().getReference();
            yValues = new ArrayList<>();


            readDataYAxis(new FirebaseCallYAxisback() {
                @Override
                public void onCallback(ArrayList<Entry> list1) {
                    LineDataSet set1 = new LineDataSet(list1, "Distance between Sensor and Water Level");
                    set1.setFillAlpha(110);
                    set1.setColor(Color.RED);
                    set1.setLineWidth(3f);
                    set1.setValueTextColor(Color.DKGRAY);
                    set1.setValueTextSize(10f);

                    //Modify X Axis name
                    xAxisLabel = new ArrayList<>();
                    readDataXAxis(new FirebaseCallXAxisback() {
                        @Override
                        public void onCallback(ArrayList<String> list2) {
                            XAxis xAxis = mChart.getXAxis();
                            xAxis.setValueFormatter(new IAxisValueFormatter() {
                                @Override
                                public String getFormattedValue(float value, AxisBase axis) {
                                    return list2.get((int) value);
                                }
                            });

                            xAxis.setGranularity(1f);
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                            dataSets.add(set1);
                            LineData data = new LineData(dataSets);
                            mChart.setData(data);
                        }
                    });


                }
            });

        }
    }

    private void readDataYAxis (FirebaseCallYAxisback firebaseCallback){
        database.child("SensorData").child(date).addListenerForSingleValueEvent(new ValueEventListener() { //ACCESS DATE
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                for (DataSnapshot data : snapshot1.getChildren()) { //GETTING TIME
                    //Distance value
                    String itemName = data.child("Distance").getValue().toString();
                    float numFloat = Float.parseFloat(itemName.toString());
                    int numInt = Math.round(numFloat);
                    yValues.add(new Entry(counter, numInt));
                    counter++;
                }//end for loop
                firebaseCallback.onCallback(yValues);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private interface FirebaseCallYAxisback{
        void  onCallback (ArrayList<Entry> list1);
    }


    private void readDataXAxis (FirebaseCallXAxisback firebaseCallback){
        database.child("SensorData").child(date).addListenerForSingleValueEvent(new ValueEventListener() { //ACCESS DATE
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot2) {
                for (DataSnapshot data : snapshot2.getChildren()) { //GETTING TIME
                    //Time value
                    String itemName = data.child("Time").getValue().toString();
                    xAxisLabel.add(itemName);
                }//end for loop
                firebaseCallback.onCallback(xAxisLabel);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private interface FirebaseCallXAxisback{
        void  onCallback (ArrayList<String> list2);
    }
}
