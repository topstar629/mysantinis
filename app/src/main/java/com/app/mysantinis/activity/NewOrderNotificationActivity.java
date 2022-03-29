package com.app.mysantinis.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.mysantinis.General;
import com.app.mysantinis.R;
import com.app.mysantinis.starprnt.CommonAlertDialogFragment;
import com.warkiz.widget.IndicatorSeekBar;

import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class NewOrderNotificationActivity extends AppCompatActivity{
    MediaPlayer mp;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order_notification);

        TextView txtNumber = findViewById(R.id.txt_number);
        txtNumber.setText(""+General.newOrderNumber);
        mp = MediaPlayer.create(this, R.raw.music);
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mp.start();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Handler handler = new Handler();
        //                onReject(); //TODO: On last update
        handler.postDelayed(this::finish, 60000); // dismiss after 5 min
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mp.isPlaying()) {
            mp.stop();
            mp.release();
        }
    }

    public void onTap(View view) {
        General.detailData = General.newOrderData;
        startActivity(new Intent(this, OrderDetailsActivity.class));
        finish();
    }
    void onReject(){
        General.detailData = General.newOrderData;
        String order_id;
        try {
            order_id = General.detailData.getString("id");
            String cloverID = "";
            for(int i = 0; i < General.detailData.getJSONArray("meta_data").length(); i++ ) {
                if(General.detailData.getJSONArray("meta_data").getJSONObject(i).getString("key").equals("clover_order_id")) {
                    cloverID = General.detailData.getJSONArray("meta_data").getJSONObject(i).getString("value");
                }
            }

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("action", "order_update_status")
                    .addFormDataPart("order_id", order_id)
                    .addFormDataPart("clover_order_id", cloverID)
                    .addFormDataPart("order_time", "0")
                    .addFormDataPart("order_status", "rejected")
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
                        Log.d("New Order", myResponse);
                    }
                    finish();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}