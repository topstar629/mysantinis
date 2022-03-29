package com.app.mysantinis.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.mysantinis.General;
import com.app.mysantinis.R;
import com.app.mysantinis.adapter.RestaurantMenuAdapter;
import com.app.mysantinis.module.RestaurantData;
import com.app.mysantinis.starprnt.PrinterSetupActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SelectRestaurantActivity extends AppCompatActivity {

    ArrayList<RestaurantData> menuArray = new ArrayList<>();
    Spinner spinner;
    ProgressDialog progress;
    boolean fromSettings;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_restaurant);

        spinner = findViewById(R.id.spinner);
        spinner.setPrompt("Select your restaurant...");
        fromSettings = getIntent().getBooleanExtra("fromSettings", false);
        run();

        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
            TextView txtVersion = findViewById(R.id.txt_version);
            txtVersion.setText("MySantis Version" + info.versionCode + "(" + info.versionName + ")");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        SharedPreferences pref = getSharedPreferences("MySantinis",MODE_PRIVATE);
        General.printerFontSize = pref.getFloat("printer_font_size", 1.0f);
    }
    void run(){

        progress = new ProgressDialog(this);
        progress.setMessage("Please Wait...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setCancelable(false);
        progress.show();
        String location_url = General.site_url_location;

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(location_url)
                .addHeader("Authorization","Bearer " + General.auth_key)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                Log.e("Loading Failed",e.getLocalizedMessage());
                progress.dismiss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                progress.dismiss();
                final String responseOrders = response.body().string();

                runOnUiThread(() -> {
                    menuArray = new ArrayList<>();

                    try {
                        JSONArray jsonArray = new JSONArray(responseOrders);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String consumer_key = jsonArray.getJSONObject(i).getString("merchant_id");
                            String consumer_secret = jsonArray.getJSONObject(i).getString("merchant_api_key");

                            int id = jsonArray.getJSONObject(i).getInt("id");
                            String title = jsonArray.getJSONObject(i).getString("merchant_title");
                            String postalCode = jsonArray.getJSONObject(i).getString("postcode");
                            String state = jsonArray.getJSONObject(i).getString("state");
                            String address = jsonArray.getJSONObject(i).getString("address");
                            String status = jsonArray.getJSONObject(i).getString("status");

                            menuArray.add(new RestaurantData(id,title,address,state,postalCode,consumer_key,consumer_secret,status));
                        }

                        RestaurantMenuAdapter adapter = new RestaurantMenuAdapter(SelectRestaurantActivity.this,
                                R.layout.restaurant_menu_item, menuArray);
                        spinner.setAdapter(adapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                });

            }
        });
    }

    public void onSelect(View view) {
        if (menuArray.size()==0) return;
        int index = spinner.getSelectedItemPosition();
        Log.d("Rest selected",index + ": " + menuArray.get(index).getName());
        General.restaurantId = menuArray.get(index).getId() + "";
        General.restaurantName = menuArray.get(index).getName();
        General.merchantId = menuArray.get(index).getConsumerKey();
        General.accessToken = menuArray.get(index).getConsumerSecret();
        General.status = menuArray.get(index).getStatus();
//        startActivity(new Intent(this, PrinterStartupActivity.class));//clover
        if(!fromSettings) startActivity(new Intent(this, PrinterSetupActivity.class));
        finish();
    }
    @Override
    public void onPause(){
        super.onPause();
        if(progress != null)
            progress.dismiss();
    }
}