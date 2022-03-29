package com.app.mysantinis.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mysantinis.fragment.FinishedOrdersFragment;
import com.app.mysantinis.fragment.IncomingOrdersFragment;
import com.app.mysantinis.module.FinishedOrderData;
import com.app.mysantinis.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FinishedOrdersAdapter extends RecyclerView.Adapter<FinishedOrdersAdapter.AppViewHolder> {

        JSONArray orderArray;
        FinishedOrdersFragment fragment;
        public interface OnItemClickListener {
            void onItemClick(int index);
        }
        private final OnItemClickListener listener;

        public FinishedOrdersAdapter(FinishedOrdersFragment context, JSONArray orderArray, OnItemClickListener listener ) {

            this.fragment = context;

            this.orderArray = orderArray;
            this.listener = listener;
        }

        class AppViewHolder extends RecyclerView.ViewHolder {
            TextView txtID;
            TextView txtName;
            TextView txtDate;
            TextView txtEmail;
            TextView txtContent;
            TextView txtStauts;

            AppViewHolder(View itemView) {
                super(itemView);
                txtID = itemView.findViewById(R.id.txt_id);
                txtName = itemView.findViewById(R.id.txt_name);
                txtDate = itemView.findViewById(R.id.txt_date);
                txtEmail  = itemView.findViewById(R.id.txt_email);
                txtContent  = itemView.findViewById(R.id.txt_content);
                txtStauts = itemView.findViewById(R.id.txt_status);
            }
        }

        @NonNull
        @Override
        public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item_incoming,
                    parent, false);

            return new AppViewHolder(v);

        }


        @Override
        public void onBindViewHolder(@NonNull AppViewHolder holder, final int position) {
            final JSONObject currentItem;
            try {
                currentItem = orderArray.getJSONObject(position);
                holder.txtID.setText(currentItem.getString("id"));
                String strDate = currentItem.getString("date_modified");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
                SimpleDateFormat outputDateFormat = new SimpleDateFormat("d MMMM, yyyy hh:mm a");
                Date d = dateFormat.parse(strDate);
                holder.txtDate.setText(outputDateFormat.format(d));
                holder.txtName.setText(String.format("%s %s", currentItem.getJSONObject("billing").getString("first_name"), currentItem.getJSONObject("billing").getString("last_name")));
                holder.txtEmail.setText(currentItem.getJSONObject("billing").getString("email"));

                String status = currentItem.getString("status");
                if (status != null && status.equals("completed")) {
                    holder.txtStauts.setText(R.string.accepted);
                    holder.txtStauts.setBackground(this.fragment.getResources().getDrawable(R.drawable.linear_corner5_blue));
                    holder.txtStauts.setTextColor(this.fragment.getResources().getColor(R.color.blue));
                } else {
                    holder.txtStauts.setText(R.string.not_accepted);
                    holder.txtStauts.setBackground(this.fragment.getResources().getDrawable(R.drawable.linear_corner5_red));
                    holder.txtStauts.setTextColor(this.fragment.getResources().getColor(R.color.red));
                }

                JSONArray itemArray = currentItem.getJSONArray("line_items");
                StringBuilder content = new StringBuilder();
                for(int i = 0; i < itemArray.length();i++) {
                    content.append(itemArray.getJSONObject(i).getString("name")).append("\n");
                }
                holder.txtContent.setText(content.toString());
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return orderArray.length();
        }

    }

