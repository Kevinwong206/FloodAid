package com.example.floodaid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DateDetailsAdapter extends RecyclerView.Adapter<DateDetailsAdapter.MyViewHolder> {
    Context context;
    ArrayList<DateDetailsGetter> list;

    public DateDetailsAdapter(Context context, ArrayList<DateDetailsGetter> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public DateDetailsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.datahistory_items,parent,false);
        return new DateDetailsAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DateDetailsGetter dataHistory = list.get(position);
        holder.setDataHistoyDetails(dataHistory);

        holder.oneImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String checkInsufficient= dataHistory.getNumData();

                Intent intent = new Intent(context, DataGraph.class);
                Bundle bundle = new Bundle();
                if(checkInsufficient.equals("1")){
                    bundle.putString("passingDate", dataHistory.getLastDate());
                    bundle.putString("showGraph", "0");
                }else{
                    bundle.putString("passingDate", dataHistory.getLastDate());
                    bundle.putString("showGraph", "1");
                }
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView oneDate, oneUpdated,oneNumData,oneSevere;
        ImageView oneImage;

        MyViewHolder(View itemView) {
            super(itemView);
            oneDate = itemView.findViewById(R.id.tvDate);
            oneUpdated = itemView.findViewById(R.id.tvUpdated);
            oneNumData = itemView.findViewById(R.id.tvNumData);
            oneSevere = itemView.findViewById(R.id.tvSevere);
            oneImage = itemView.findViewById(R.id.imageSevere);
        }

        void setDataHistoyDetails(DateDetailsGetter data){
            oneDate.setText(data.getLastDate());
            oneUpdated.setText(data.getLastTime());
            oneNumData.setText(data.getNumData());
            oneSevere.setText(data.getDangerousStage());

            if(data.getDangerousStage().equals("1")){
                oneSevere.setText("Normal");
                oneImage.setImageResource(R.drawable.waternormal);
            } else if(data.getDangerousStage().equals("2")){
                oneSevere.setText("Alert");
                oneImage.setImageResource(R.drawable.wateralert);
            } else if(data.getDangerousStage().equals("3")){
                oneSevere.setText("Warning");
                oneImage.setImageResource(R.drawable.waterwarning);
            } else if(data.getDangerousStage().equals("4")){
                oneSevere.setText("Danger");
                oneImage.setImageResource(R.drawable.waterdanger);
            }
        }
    }
}