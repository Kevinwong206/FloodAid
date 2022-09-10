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

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.MyViewHolder> {

    Context context;
    ArrayList<DonationGetter> donationArrayList;
    LayoutInflater inflater;
    String address, condition, donatorName, donatorPhone, productTitle, quantity, imageUrL;

    public void setFilteredList (ArrayList<DonationGetter> filteredList){
        this.donationArrayList = filteredList;
        notifyDataSetChanged();
    }

    public DonationAdapter(Context context, ArrayList<DonationGetter> donationArrayList) {
        this.context = context;
        this.donationArrayList = donationArrayList;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public DonationAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.custom_grid_layout,parent,false);
        //View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.donation_items,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DonationAdapter.MyViewHolder holder, int position) {
        DonationGetter donationGetter = donationArrayList.get(position);

        address = donationGetter.address;
        condition = donationGetter.condition;
        donatorName = donationGetter.donatorName;
        donatorPhone = donationGetter.donatorPhone;
        productTitle = donationGetter.productTitle;
        quantity = donationGetter.quantity;
        imageUrL = donationGetter.imageURL;

        Picasso.get().load(imageUrL).into(holder.imageDonateItem);
        holder.productName.setText(productTitle);
        holder.productQuantity.setText(quantity);

        holder.imageDonateItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), DonationDetails.class);
                Bundle bundle = new Bundle();
                bundle.putString("donatorName",donationGetter.donatorName);
                bundle.putString("donatorPhone",donationGetter.donatorPhone);
                bundle.putString("address", donationGetter.address);
                bundle.putString("condition",donationGetter.condition);
                bundle.putString("productTitle",donationGetter.productTitle);
                bundle.putString("quantity",donationGetter.quantity);
                bundle.putString("imageUrL",donationGetter.imageURL);
                bundle.putString("id",donationGetter.itemId);
                i.putExtras(bundle);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return donationArrayList.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView imageDonateItem;
        TextView productName, productQuantity;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.tvItemTitle);
            productQuantity = itemView.findViewById(R.id.tvQuantity);
            imageDonateItem = itemView.findViewById(R.id.imageItem);
        }
    }

}