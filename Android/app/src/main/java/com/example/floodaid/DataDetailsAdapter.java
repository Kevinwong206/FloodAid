package com.example.floodaid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DataDetailsAdapter extends RecyclerView.Adapter<DataDetailsAdapter.MyViewHolder> {
    Context context;
    ArrayList<DataDetailsGetter> list;

    public DataDetailsAdapter(Context context, ArrayList<DataDetailsGetter> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public DataDetailsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.activity_data_history_list_items,parent,false);
        return new DataDetailsAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DataDetailsGetter dataHistory = list.get(position);
        holder.setDataHistoyDetails(dataHistory);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView hisDistance, hisWeather,hisDate,hisTime;

        MyViewHolder(View itemView) {
            super(itemView);
            hisDistance = itemView.findViewById(R.id.tvHisDistance);
            hisWeather = itemView.findViewById(R.id.tvHisWeather);
            hisDate = itemView.findViewById(R.id.tvHisDate);
            hisTime = itemView.findViewById(R.id.tvHisTime);
        }

        void setDataHistoyDetails(DataDetailsGetter data){
            hisDistance.setText(data.getDistance());
            hisWeather.setText(data.getWeatherDesc());
            hisDate.setText(data.getDate());
            hisTime.setText(data.getTime());
        }
    }
}
