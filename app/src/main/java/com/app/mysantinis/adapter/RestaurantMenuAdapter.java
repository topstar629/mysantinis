package com.app.mysantinis.adapter;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.app.mysantinis.R;
import com.app.mysantinis.fragment.FinishedOrdersFragment;
import com.app.mysantinis.fragment.IncomingOrdersFragment;
import com.app.mysantinis.module.RestaurantData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mobile World on 12/5/2019.
 */

public class RestaurantMenuAdapter extends ArrayAdapter<RestaurantData> {

    LayoutInflater flater;
    ArrayList<RestaurantData> dataList;

    public RestaurantMenuAdapter(Activity context, int resouceId, ArrayList<RestaurantData> list){

        super(context,resouceId, list);
//        flater = context.getLayoutInflater();
        dataList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        return rowview(convertView,position);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return rowview(convertView,position);
    }

    private View rowview(View convertView , int position){

        RestaurantData rowItem = dataList.get(position);

        viewHolder holder ;
        View rowview = convertView;
        if (rowview==null) {

            holder = new viewHolder();
            flater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowview = flater.inflate(R.layout.restaurant_menu_item, null, false);

            holder.txtName = rowview.findViewById(R.id.txt_name);
            holder.txtAddress = rowview.findViewById(R.id.txt_address);
            rowview.setTag(holder);
        }else{
            holder = (viewHolder) rowview.getTag();
        }
        holder.txtName.setText(rowItem.getName());
        holder.txtAddress.setText(rowItem.getAddress());

        return rowview;
    }

    private class viewHolder{
        TextView txtName;
        TextView txtAddress;
    }
}
