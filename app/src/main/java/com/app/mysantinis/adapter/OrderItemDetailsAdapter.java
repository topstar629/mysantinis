package com.app.mysantinis.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mysantinis.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class OrderItemDetailsAdapter extends RecyclerView.Adapter<OrderItemDetailsAdapter.ViewHolder> {

    JSONArray itemJsonArray;
    private Context context;

    public OrderItemDetailsAdapter(Context context, JSONArray productsArray) {
        this.itemJsonArray = productsArray;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderItemDetailsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_detail_item, viewGroup, false);
        return new OrderItemDetailsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final OrderItemDetailsAdapter.ViewHolder viewHolder, final int index) {
        //viewHolder.productImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bag));
        try {
            JSONObject item = itemJsonArray.getJSONObject(index);
            viewHolder.txtName.setText(item.getString("name"));
            viewHolder.txtQty.setText(item.getString("quantity"));
            viewHolder.txtAmount.setText(String.format("$%.2f", item.getDouble("price")));
            StringBuilder strDetails = new StringBuilder();
            boolean addedDetail = false;
            StringBuilder productNote = new StringBuilder();
            for( int i = 0; i < item.getJSONArray("meta_data").length(); i++) {
                if (item.getJSONArray("meta_data").getJSONObject(i).getString("key").equals("_ywapo_meta_data") && !addedDetail) {
                    JSONArray detailData = item.getJSONArray("meta_data").getJSONObject(i).getJSONArray("value");
                    for(int j = 0; j < detailData.length(); j++ ) {
                        JSONObject detailItem = detailData.getJSONObject(j);
                        double price = detailItem.getDouble("price");
                        price = (double)Math.round(price * 100) / 100.0;
                        if(price==0) {
                            strDetails.append(detailItem.getString("value"));
//                            strDetails.append(detailItem.getString("name")).append(":").append(detailItem.getString("value"));
                        }
                        else {
                            strDetails.append("($").append(price).append("):").append(detailItem.getString("value"));
//                            strDetails.append(detailItem.getString("name")).append("($").append(price).append("):").append(detailItem.getString("value"));
                        }
                        strDetails.append("\n");
                    }
                    addedDetail = true;
                }
                else if(productNote.length() == 0 && item.getJSONArray("meta_data").getJSONObject(i).getString("key").equals("Product Note")){
                    productNote.append(item.getJSONArray("meta_data").getJSONObject(i).getString("value"));
//                    productNote.append("Product Note: ").append(item.getJSONArray("meta_data").getJSONObject(i).getString("value"));
                }
            }
            strDetails.append(productNote.toString());
            viewHolder.txtDetails.setText(strDetails.toString());
            Picasso.get()
                    .load(item.getString("image"))
                    .placeholder(R.drawable.img_product_default)
                    .error(R.drawable.img_product_default)
                    .into(viewHolder.thumbImage);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return itemJsonArray.length();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbImage;
        TextView txtName;
        TextView txtQty;
        TextView txtAmount;
        TextView txtDetails;

        ViewHolder(View itemView) {
            super(itemView);
            thumbImage = itemView.findViewById(R.id.img_thumb);
            txtName = itemView.findViewById(R.id.txt_name);
            txtQty = itemView.findViewById(R.id.txt_qty);
            txtAmount = itemView.findViewById(R.id.txt_amount);
            txtDetails = itemView.findViewById(R.id.txt_details);
        }
    }


}
