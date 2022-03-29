package com.app.mysantinis.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mysantinis.General;
import com.app.mysantinis.R;
import com.app.mysantinis.adapter.OrderItemDetailsAdapter;
import com.app.mysantinis.utils.Utility;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrderDetailsActivity extends AppCompatActivity {

    IndicatorSeekBar seekBar;

    JSONObject detailedData;
    boolean finished;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        seekBar = findViewById(R.id.seek_delay);

        finished = getIntent().getBooleanExtra("finished",false);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Order Details");

        detailedData = General.detailData;
        try {
            TextView txtId = findViewById(R.id.txt_detail_id);
            txtId.setText(detailedData.getString("id"));
            TextView txtDate = findViewById(R.id.txt_detail_date);
            String strDate = detailedData.getString("date_modified");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("d MMMM, yyyy hh:mm a");
            Date d = dateFormat.parse(strDate);
            txtDate.setText(outputDateFormat.format(d));
            TextView txtName = findViewById(R.id.txt_detail_name);
            txtName.setText(String.format("%s %s", detailedData.getJSONObject("billing").getString("first_name"), detailedData.getJSONObject("billing").getString("last_name")));
            TextView txtEmail = findViewById(R.id.txt_detail_email);
            txtEmail.setText(detailedData.getJSONObject("billing").getString("email"));
            TextView txtPhone = findViewById(R.id.txt_phone);
            txtPhone.setText(detailedData.getJSONObject("billing").getString("phone"));
            LinearLayout ll_bottom = findViewById(R.id.ll_bottom);

            JSONArray itemArray = detailedData.getJSONArray("line_items");

            RecyclerView recyclerViewDetailItems = findViewById(R.id.recycler_view_items);
            recyclerViewDetailItems.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerViewDetailItems.setLayoutManager(layoutManager);
            recyclerViewDetailItems.setAdapter(new OrderItemDetailsAdapter(this,itemArray));

            seekBar.setIndicatorTextFormat("${PROGRESS}mins");
            seekBar.setProgress(5);
            final TextView txtDelayTime = findViewById(R.id.txt_delay_time);
            txtDelayTime.setText("5 mins");
            seekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
                @Override
                public void onSeeking(SeekParams seekParams) {
                    txtDelayTime.setText(seekParams.progress + " mins");
                }

                @Override
                public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

                }
            });

            TextView txtSubtotal = findViewById(R.id.txt_subtotal);
            double subTotal = 0;
            for (int i = 0; i < itemArray.length(); i ++ ) {
                subTotal += itemArray.getJSONObject(i).getDouble("subtotal");
            }
            subTotal = Math.round(subTotal * 100) / 100.0;
            txtSubtotal.setText(String.format("$%s", subTotal));

            TextView txtTax = findViewById(R.id.txt_tax);
            txtTax.setText(String.format("$%s", detailedData.getString("total_tax")));
            TextView txtTotal = findViewById(R.id.txt_total);
            txtTotal.setText(String.format("$%s", detailedData.getString("total")));

            Button btnAccept = findViewById(R.id.btn_accept);
            TextView txtDelayTitle = findViewById(R.id.txt_delay_title);
            if(finished) {
                btnAccept.setText("PRINT");
                ll_bottom.setVisibility(View.GONE);
//                btnAccept.setVisibility(View.GONE);
//                txtDelayTitle.setVisibility(View.GONE);
//                txtDelayTime.setVisibility(View.GONE);
//                seekBar.setVisibility(View.GONE);
            }
            else {
                btnAccept.setText(R.string.accept_print);
//                ll_bottom.setVisibility(View.VISIBLE);
//                btnAccept.setVisibility(View.VISIBLE);
//                txtDelayTitle.setVisibility(View.VISIBLE);
//                txtDelayTime.setVisibility(View.VISIBLE);
//                seekBar.setVisibility(View.VISIBLE);

                String status = detailedData.optString("status");
                if (status != null && status.equals("completed")) {

                    ll_bottom.setVisibility(View.GONE);
                    btnAccept.setVisibility(View.GONE);

                } else {

                    ll_bottom.setVisibility(View.VISIBLE);
                    btnAccept.setVisibility(View.VISIBLE);

                }

            }
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
    }
    public void onAccept(View view) {

        if(finished) {
            onPrint();
        }
        else {
            onAccept();
        }
    }
    void onPrint(){

        try {
            General.orderId = detailedData.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        MainActivity.instanceOfMainActivity.onPrintFromDetail();
        finish();
    }

    void sendSMS() {

        String delayed_time = Utility.getDelayedTime(seekBar.getProgress());

        JSONObject billing = detailedData.optJSONObject("billing");

        if (billing != null) {

            String strPhone = billing.optString("phone");

            if (strPhone != null && strPhone.length() != 0) {

                String message = "Your order is being prepared at " + General.restaurantName + ",\nplease arrive after " + delayed_time + " for pickup";
//                Utility.sendSMS("3397070723", message);
                Utility.sendSMS(strPhone, message);
            }
        }
    }

    void onAccept(){

//        sendSMS();

        General.isPrinting = true;
        String wordpressId = null;

        try {

            wordpressId = detailedData.getString("id");

            String cloverID = "";
            for(int i = 0; i < detailedData.getJSONArray("meta_data").length(); i++ ) {
                if(detailedData.getJSONArray("meta_data").getJSONObject(i).getString("key").equals("clover_order_id")) {
                    cloverID = detailedData.getJSONArray("meta_data").getJSONObject(i).getString("value");
                }
            }

            String delay_time = seekBar.getProgress() + "";
            String restaurantID = General.restaurantId;

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("action", "order_update_status")
                    .addFormDataPart("order_id", wordpressId)
                    .addFormDataPart("clover_order_id", cloverID)
                    .addFormDataPart("order_time", delay_time)
                    .addFormDataPart("order_status", "completed")
                    .build();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://mysantinis.com/orderonline/wp-admin/admin-ajax.php")
                    .post(requestBody)
                    .addHeader("consumer_key", General.consumer_key)
                    .addHeader("consumer_secret", General.consumer_secret)
                    .addHeader("cache-control", "no-cache")
                    .build();


            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        final String myResponse = response.body().string();
                    }
                }
            });

            RequestBody requestBody2 = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("action", "get_order_receipt")
                    .addFormDataPart("restaurant_id", restaurantID)
                    .addFormDataPart("clover_order_id", cloverID)
                    .addFormDataPart("order_id", wordpressId)
                    .build();

            OkHttpClient client2 = new OkHttpClient();
            Request request2 = new Request.Builder()
                    .url("https://mysantinis.com/orderonline/wp-admin/admin-ajax.php")
                    .post(requestBody2)
                    .addHeader("consumer_key", General.consumer_key)
                    .addHeader("consumer_secret", General.consumer_secret)
                    .addHeader("cache-control", "no-cache")
                    .build();

            client2.newCall(request2).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        final String myResponse2 = response.body().string();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Toast.makeText(OrderDetailsActivity.this, "Success! Order accepted.", Toast.LENGTH_LONG).show();

                                sendSMS();

                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        onPrint();
                                    }
                                }, 500);
                            }
                        });
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}