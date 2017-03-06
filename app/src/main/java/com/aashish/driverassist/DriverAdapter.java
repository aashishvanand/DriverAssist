package com.aashish.driverassist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by AASHI on 3/6/2017.
 */

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.MyViewHolder> {

    private List<Driver> driverList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, rating;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            rating = (TextView) view.findViewById(R.id.rating);
        }
    }


    public DriverAdapter(List<Driver> driverList) {
        this.driverList = driverList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Driver driver = driverList.get(position);
        holder.name.setText(driver.getName());
        holder.rating.setText(driver.getRating());
    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }
}
