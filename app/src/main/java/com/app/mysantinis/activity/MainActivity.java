package com.app.mysantinis.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.app.mysantinis.General;
import com.app.mysantinis.R;
import com.app.mysantinis.adapter.MainPagerAdapter;
import com.app.mysantinis.adapter.OrderItemDetailsAdapter;
import com.app.mysantinis.module.CloseOutData;
import com.app.mysantinis.module.LineItemData;
import com.app.mysantinis.starprnt.CommonAlertDialogFragment;
import com.app.mysantinis.starprnt.Communication;
import com.app.mysantinis.starprnt.Communication.CommunicationResult;
import com.app.mysantinis.starprnt.ILocalizeReceipts;
import com.app.mysantinis.starprnt.ModelCapability;
import com.app.mysantinis.starprnt.PrinterFunctions;
import com.app.mysantinis.starprnt.PrinterSettingManager;
import com.app.mysantinis.starprnt.PrinterSettings;
import com.app.mysantinis.starprnt.PrinterSetupActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.starmicronics.starioextension.StarIoExt;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CommonAlertDialogFragment.Callback {
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    boolean isShowedDetail = false;
    JSONObject detailedData = new JSONObject();
    RelativeLayout rlDetail;
    ViewPager viewPager;
    MainPagerAdapter pagerAdapter;
    TabLayout tabLayout;
    JSONArray closeOutOrderData;
    JSONArray closeOutPaymentData;
    Date startDate;
    Date endDate;
    public static MainActivity instanceOfMainActivity;
    private ProgressDialog mProgressDialog;

    float totalPaymentsWithoutTax = 0;
    float totalPaymentsTax = 0;
    float totalTips = 0;
    float cash = 0;
    float gift = 0;
    float creditCard = 0;
    float debitCard = 0;
    float totalRefunds = 0;
    float totalUberEats = 0;
    float totalGrubHub = 0;
    float totalDoorDash = 0;
    float totalPostmates = 0;
    float totalOnlineOrder = 0;
    float totalDiscounts = 0;
    float cardTip = 0;
    int orderCount = 0;
    int paymentCount = 0;
    int cardPaymentCount = 0;
    HashMap<String, Integer> paymentsData;
    HashMap<String, Integer> discountsData;
    HashMap<String, Integer> refundsData;
    HashMap<String, LineItemData> lineItemMapData;
    /////Close Out////
    ArrayList<String> employes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instanceOfMainActivity = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        pagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                hideDetail();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this, R.style.RobotoBoldTextAppearance);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        drawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        rlDetail = findViewById(R.id.rl_detail);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (isShowedDetail) {
                rlDetail.setVisibility(View.VISIBLE);
            } else {
                rlDetail.setVisibility(View.GONE);
            }
        }

//        printerInit();
        mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setMessage("Communicating...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);

        try {
            View header = navigationView.getHeaderView(0);
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
            TextView txtVersion = header.findViewById(R.id.txt_version);
            txtVersion.setText("MySantis Version" + info.versionCode + "(" + info.versionName + ")");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setStatue(final boolean statue) {
        runOnUiThread(() -> {
            TextView txtState = findViewById(R.id.txt_state);
            if (statue)
                txtState.setBackgroundResource(R.drawable.round_yellow);
            else txtState.setBackgroundResource(R.drawable.round_red_16);
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_settings,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.restaurant_settings:
                onRestaurantSettings();
                break;
            case R.id.printer_settings:
                onPrinterSettings();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetTextI18n")
    public void configurationDetail(JSONObject data){
        detailedData = data;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rlDetail.setVisibility(View.VISIBLE);
            isShowedDetail = true;
            try {
                TextView txtId = findViewById(R.id.txt_detail_id);
                txtId.setText(data.getString("id"));
                TextView txtDate = findViewById(R.id.txt_detail_date);
                String strDate = data.getString("date_modified");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
                SimpleDateFormat outputDateFormat = new SimpleDateFormat("d MMMM, yyyy hh:mm a");
                Date d = dateFormat.parse(strDate);
                txtDate.setText(outputDateFormat.format(d));
                TextView txtName = findViewById(R.id.txt_detail_name);
                txtName.setText(String.format("%s %s", data.getJSONObject("billing").getString("first_name"), data.getJSONObject("billing").getString("last_name")));
                TextView txtEmail = findViewById(R.id.txt_detail_email);
                txtEmail.setText(data.getJSONObject("billing").getString("email"));
                TextView txtPhone = findViewById(R.id.txt_phone);
                txtPhone.setText(data.getJSONObject("billing").getString("phone"));

                JSONArray itemArray = data.getJSONArray("line_items");

                RecyclerView recyclerViewDetailItems = findViewById(R.id.recycler_view_items);
                recyclerViewDetailItems.setHasFixedSize(true);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
                recyclerViewDetailItems.setLayoutManager(layoutManager);
                recyclerViewDetailItems.setAdapter(new OrderItemDetailsAdapter(this,itemArray));

                IndicatorSeekBar seekBar = findViewById(R.id.seek_delay);
                seekBar.setIndicatorTextFormat("${PROGRESS}mins");
                seekBar.setProgress(5);

                final TextView txtDelayTime = findViewById(R.id.txt_delay_time);
                txtDelayTime.setText("5 mins");
                seekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
                    @Override
                    public void onSeeking(SeekParams seekParams) {
                        txtDelayTime.setText(String.format("%d mins", seekParams.progress));
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
                txtSubtotal.setText("$"+ subTotal);

                TextView txtTax = findViewById(R.id.txt_tax);
                txtTax.setText("$"+detailedData.getString("total_tax"));
                TextView txtTotal = findViewById(R.id.txt_total);
                txtTotal.setText("$"+detailedData.getString("total"));

                LinearLayout ll_bottom = findViewById(R.id.ll_bottom);
                Button btnAccept = findViewById(R.id.btn_accept);
                TextView txtDelayTitle = findViewById(R.id.txt_delay_title);
//                if(tabLayout.getSelectedTabPosition() == 1 || data.getString("status").equals("pending") ) {
                if(tabLayout.getSelectedTabPosition() == 1) {
                    btnAccept.setVisibility(View.VISIBLE);
                    btnAccept.setText("PRINT");
                    txtDelayTitle.setVisibility(View.GONE);
                    txtDelayTime.setVisibility(View.GONE);
                    seekBar.setVisibility(View.GONE);
                } else {
//                    btnAccept.setVisibility(View.VISIBLE);
                    btnAccept.setText(R.string.accept_print);

                    String status = data.optString("status");
                    if (status != null && status.equals("completed")) {

                        ll_bottom.setVisibility(View.GONE);
                        btnAccept.setVisibility(View.GONE);

                    } else {

                        ll_bottom.setVisibility(View.VISIBLE);
                        btnAccept.setVisibility(View.VISIBLE);

                    }

//                    txtDelayTitle.setVisibility(View.VISIBLE);
//                    txtDelayTime.setVisibility(View.VISIBLE);
//                    seekBar.setVisibility(View.VISIBLE);
                }
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
        else {
            isShowedDetail = false;
//            rlDetail.setVisibility(View.GONE);

            General.detailData = data;
            Intent intent = new Intent(this, OrderDetailsActivity.class);
            if(tabLayout.getSelectedTabPosition() == 1 ) {
                intent.putExtra("finished", true);
            }
            else {
                intent.putExtra("finished", false);
            }
            startActivity(intent);

        }
    }

    void hideDetail() {
        if(rlDetail != null) {
            rlDetail.setVisibility(View.GONE);
        }
        isShowedDetail = false;
    }

    void onAccept(){
        String wordpressId = null;
        try {
            wordpressId = detailedData.getString("id");
            String cloverID = "";
            for(int i = 0; i < detailedData.getJSONArray("meta_data").length(); i++ ) {
                if(detailedData.getJSONArray("meta_data").getJSONObject(i).getString("key").equals("clover_order_id")) {
                    cloverID = detailedData.getJSONArray("meta_data").getJSONObject(i).getString("value");
                }
            }

            IndicatorSeekBar seekBar = findViewById(R.id.seek_delay);
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

                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this,"Success! Order accepted.",Toast.LENGTH_LONG).show();

                            final Handler handler = new Handler();
                            handler.postDelayed(() -> {
                                viewPager.setCurrentItem(1);
                                hideDetail();
                            }, 500);
                        });
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.createOrder) {
            startActivity(new Intent(getApplicationContext(), CreateOrderActivity.class));
        }
        else if(id == R.id.select_restaurant) {
            onRestaurantSettings();
        }
        else if(id==R.id.close_restaurant) {
            if (General.status.equals("publish")) {
                onCloseRestaurant("draft");
            }
            else {
                onCloseRestaurant("publish");
            }
        }
        else if(id==R.id.printer_settings) {
            onPrinterSettings();
        }
        else if(id==R.id.print_out_font_size) {
            onPrintOutFontSize();
        }
        else if(id==R.id.print_close_out) {
//            getEmployees();
            onSelectDate();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = navigationView.getMenu().findItem(R.id.close_restaurant);
        MenuItem restaurantSetting = menu.findItem(R.id.restaurant_settings);
        if(General.status.equals("publish")) {
            item.setTitle("Close Restaurant");
            restaurantSetting.setIcon(R.drawable.icon_restaurant);
        }
        else {
            item.setTitle("Open Restaurant");
            restaurantSetting.setIcon(R.drawable.icon_restaurant_closed);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    void onCloseRestaurant(String status){
        String locationUrl = General.site_url_location + General.restaurantId;

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("status", status)
                .build();

        Request request = new Request.Builder()
                .url(locationUrl)
                .post(requestBody)
//                .addHeader("consumer_key", General.consumer_key)
//                .addHeader("consumer_secret", General.consumer_secret)
//                .addHeader("cache-control", "no-cache")
                .addHeader("Authorization","Bearer " + General.auth_key)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                e.printStackTrace();
                setStatue(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String responseOrders = Objects.requireNonNull(response.body()).string();

                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(responseOrders);
                        Log.d("close restaurant", jsonObject.toString());
                        General.status = jsonObject.getString("status");
                        invalidateOptionsMenu();
                        if(General.status.equals("draft")) {
                            Toast.makeText(MainActivity.this, General.restaurantName + " is closed!", Toast.LENGTH_LONG).show();
                        }
                        else if(General.status.equals("publish")) {
                            Toast.makeText(MainActivity.this, General.restaurantName + " is opened!", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e("close restaurant",e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
        });
    }
    void onPrintOutFontSize() {
        String[] array = {"x0.5","x0.6","x0.7","x0.8","x0.9","x1.0 (Default)","x1.1","x1.2","x1.3","x1.4","x1.5","x1.6","x1.7","x1.8","x1.9","x2.0"};
        Float[] fontArray = {0.5f,0.6f,0.7f,0.8f,0.9f,1.0f,1.1f,1.2f,1.3f,1.4f,1.5f,1.6f,1.7f,1.8f,1.9f,2.0f};
        int checkedItem = 0;
        final Float[] selectedFont = {General.printerFontSize};
        for(int i = 0; i < fontArray.length; i++) {
            if(fontArray[i].equals(General.printerFontSize)) {
                checkedItem = i;
                break;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.print_out_font_size)
                .setSingleChoiceItems(array, checkedItem, (dialogInterface, selected) -> {
                    Log.d("TAG",""+fontArray[selected]);
                    selectedFont[0] = fontArray[selected];
                })
                // Set the action buttons
                .setPositiveButton("OK", (dialog, id) -> {
                    General.printerFontSize = selectedFont[0];
                    SharedPreferences pref = getSharedPreferences("MySantinis",MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putFloat("printer_font_size",General.printerFontSize);
                    editor.apply();
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                });

        builder.create().show();
    }

    public void onAccept(View view) {
        General.isPrinting = true;
        General.detailData = detailedData;
        try {
            if(tabLayout.getSelectedTabPosition() == 0 && General.detailData.getString("status").equals("processing")) {
                onAccept();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        onPrint();
    }

    public void onPrintFromDetail() {
        General.isPrinting = true;
        final Handler handler = new Handler();
        handler.postDelayed(this::onPrint, 500);
    }
/*
    void getEmployees(){

        mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setMessage("Loading Close Out Data...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        String employeesUrl = General.clover_url + General.merchantId + "/employees";
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(employeesUrl)
                .addHeader("Authorization","Bearer " + General.accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseProducts = response.body().string();

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject object = new JSONObject(responseProducts);

                            JSONArray jsonArray = object.getJSONArray("elements");

                            employes = new ArrayList<>();
                            for( int i = 0; i < jsonArray.length(); i++) {
                                JSONObject element = jsonArray.getJSONObject(i);
                                employes.add(element.getString("id"));
                            }

//                            onCloseOutSheet();
                            mProgressDialog.dismiss();
                            onSelectDate();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mProgressDialog.dismiss();
                        }
                    }
                });
            }
        });
    }

 */
    void onSelectDate(){
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("America/New_York"));
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int selectedYear,
                                  int selectedMonth, int selectedDay) {
                onCloseOutPaymentSheet(selectedYear, selectedMonth, selectedDay);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }
    @SuppressLint("SimpleDateFormat")
    void onCloseOutPaymentSheet(final int y, final int m, final int d){
        mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setMessage("Loading Close Out Payment Data...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();

        final Calendar startCal = new GregorianCalendar(TimeZone.getTimeZone("America/New_York"));
        startCal.set(y,m,d,0,0,0);

        final Calendar endCal = new GregorianCalendar(TimeZone.getTimeZone("America/New_York"));
        endCal.set(y,m,d,23,59,59);

        final String closeOutPaymentUrl = General.clover_url + General.merchantId +
                "/payments?filter=createdTime>=" + startCal.getTimeInMillis() +
                "&filter=createdTime<" + endCal.getTimeInMillis() +
                "&expand=tender,externalReferenceId,taxRates,lineItemPayments,cardTransaction,dccInfo,appTracking,paymentAttributes,order,transactionInfo&limit=1000&offset=0";

        startDate = startCal.getTime();
        endDate = endCal.getTime();

        Log.d("Close out payment url", closeOutPaymentUrl);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(closeOutPaymentUrl)
                .addHeader("Authorization","Bearer " + General.accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseProducts = response.body().string();

                MainActivity.this.runOnUiThread(new Runnable() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        try {
                            JSONObject object = new JSONObject(responseProducts);
                            closeOutPaymentData = object.getJSONArray("elements");
                             totalPaymentsWithoutTax = 0;
                             totalPaymentsTax = 0;
                             totalTips = 0;
                             cash = 0;
                             gift = 0;
                             creditCard = 0;
                             debitCard = 0;
                             totalRefunds = 0;
                             totalUberEats = 0;
                             totalGrubHub = 0;
                             totalDoorDash = 0;
                             totalPostmates = 0;
                             totalOnlineOrder = 0;
                             totalDiscounts = 0;
                             orderCount = 0;
                             paymentCount = 0;
                             cardPaymentCount = 0;
                             cardTip = 0;
                             paymentsData = new HashMap<>();

                            for( int i = 0; i < closeOutPaymentData.length(); i++) {
                                JSONObject payment = closeOutPaymentData.getJSONObject(i);
                                paymentsData.put(payment.getString("id"),i);
                                if (payment.getJSONObject("order").getString("state").equals("locked")) {
                                    paymentCount ++;
                                }
                            }
                            onCloseOutRefundsSheet(y,m,d);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });
            }
        });
    }
    @SuppressLint("SimpleDateFormat")
    void onCloseOutRefundsSheet(final int y, final int m, final int d){
        mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setMessage("Loading Close Out Refunds Data...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();

        final Calendar startCal = new GregorianCalendar(TimeZone.getTimeZone("America/New_York"));
        startCal.set(y,m,d,0,0,0);

        final Calendar endCal = new GregorianCalendar(TimeZone.getTimeZone("America/New_York"));
        endCal.set(y,m,d,23,59,59);

        final String closeOutRefundsUrl = General.clover_url + General.merchantId +
                "/refunds?filter=order.modifiedTime>=" + startCal.getTimeInMillis() +
                "&filter=order.modifiedTime<" + endCal.getTimeInMillis() +
                "&limit=1000&offset=0";

        startDate = startCal.getTime();
        endDate = endCal.getTime();

        Log.d("Close out refunds url", closeOutRefundsUrl);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(closeOutRefundsUrl)
                .addHeader("Authorization","Bearer " + General.accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                runOnUiThread(() -> mProgressDialog.dismiss());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseProducts = response.body().string();

                MainActivity.this.runOnUiThread(() -> {
                    mProgressDialog.dismiss();
                    try {
                        JSONObject object = new JSONObject(responseProducts);
                        JSONArray refundsArray = object.getJSONArray("elements");
                        for( int i = 0; i < refundsArray.length(); i++) {
                            JSONObject refund = refundsArray.getJSONObject(i);
                            totalRefunds += refund.getJSONObject("payment").getInt("amount") - refund.getJSONObject("payment").getInt("taxAmount");
                        }
                        onCloseOutLineItemSheet(y,m,d);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }
    void onCloseOutLineItemSheet(final int y, final int m, final int d){
        mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setMessage("Loading Close Out Line Item Data...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();

        final Calendar startCal = new GregorianCalendar(TimeZone.getTimeZone("America/New_York"));
        startCal.set(y,m,d,0,0,0);

        final Calendar endCal = new GregorianCalendar(TimeZone.getTimeZone("America/New_York"));
        endCal.set(y,m,d,23,59,59);

        final String closeOutPaymentUrl = General.clover_url + General.merchantId +
                "/line_items?filter=createdTime>=" + startCal.getTimeInMillis() +
                "&filter=createdTime<" + endCal.getTimeInMillis() +
                "&expand=modifications&limit=1000&offset=0";

        startDate = startCal.getTime();
        endDate = endCal.getTime();

        Log.d("Close out line item url", closeOutPaymentUrl);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(closeOutPaymentUrl)
                .addHeader("Authorization","Bearer " + General.accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                runOnUiThread(() -> mProgressDialog.dismiss());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseProducts = response.body().string();

                MainActivity.this.runOnUiThread(() -> {
                    mProgressDialog.dismiss();
                    try {
                        JSONObject object = new JSONObject(responseProducts);
                        JSONArray jsonArray = object.getJSONArray("elements");

                        lineItemMapData = new HashMap<>();

                        for( int i = 0; i < jsonArray.length(); i++) {
                            JSONObject lineItem = jsonArray.getJSONObject(i);
                            LineItemData data = new LineItemData();
                            String orderId = lineItem.getJSONObject("orderRef").getString("id");
                            if(lineItemMapData.containsKey(orderId)) {
                                data = lineItemMapData.get(orderId);
                            }

                            if (lineItem.getString("name").equals("Uber Eats")) {
                                data.uberEats += lineItem.getInt("price");
                            }
                            else if (lineItem.getString("name").equals("GrubHub")) {
                                data.grubHub += lineItem.getInt("price");
                            }
                            else if (lineItem.getString("name").equals("DoorDash")) {
                                data.doorDash += lineItem.getInt("price");
                            }
                            else if (lineItem.getString("name").equals("Postmates")) {
                                data.postmates += lineItem.getInt("price");
                            }

                            data.total += lineItem.getInt("price");

                            if (lineItem.has("modifications") && lineItem.getJSONObject("modifications").getJSONArray("elements").length() > 0 ) {
                                JSONArray subItems = lineItem.getJSONObject("modifications").getJSONArray("elements");
                                for ( int j = 0; j < subItems.length(); j++) {
                                    JSONObject subItem = subItems.getJSONObject(j);
                                    data.total += subItem.getInt("amount");
                                }
                            }

                            lineItemMapData.put(orderId, data);
                        }
                        onCloseOutOrderSheet(y,m,d);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }
    @SuppressLint("SimpleDateFormat")
    void onCloseOutOrderSheet(int y, int m, int d){
        mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setMessage("Loading Close Out Order Data...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();

        final Calendar startCal = new GregorianCalendar(TimeZone.getTimeZone("America/New_York"));
        startCal.set(y,m,d,0,0,0);

        final Calendar endCal = new GregorianCalendar(TimeZone.getTimeZone("America/New_York"));
        endCal.set(y,m,d,23,59,59);

        final String closeOutUrl = General.clover_url + General.merchantId +
                "/orders?filter=createdTime>=" + startCal.getTimeInMillis() +
                "&filter=createdTime<" + endCal.getTimeInMillis() +
                "&expand=discounts,payments,lineItems&limit=1000&offset=0";

        startDate = startCal.getTime();
        endDate = endCal.getTime();

        Log.d("Create Order", closeOutUrl);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(closeOutUrl)
                .addHeader("Authorization","Bearer " + General.accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                runOnUiThread(() -> mProgressDialog.dismiss());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseProducts = response.body().string();

                MainActivity.this.runOnUiThread(() -> {
                    mProgressDialog.dismiss();
                    try {
                        JSONObject object = new JSONObject(responseProducts);
                        closeOutOrderData = object.getJSONArray("elements");
                        discountsData = new HashMap<>();

                        for( int i = 0; i < closeOutOrderData.length(); i++) {
                            JSONObject element = closeOutOrderData.getJSONObject(i);
                            if (element.getString("state").equals("locked")) {
                                totalPaymentsWithoutTax += element.getDouble("total");
                                if( element.has("discounts") && element.getJSONObject("discounts").getJSONArray("elements").length() > 0) {
                                    JSONArray discounts = element.getJSONObject("discounts").getJSONArray("elements");
                                    for( int j = 0; j < discounts.length(); j++) {
                                        JSONObject discount = discounts.getJSONObject(j);
                                        discountsData.put(discount.getJSONObject("orderRef").getString("id"), discount.getInt("percentage"));
                                    }
                                }

                                if(lineItemMapData.containsKey(element.getString("id"))) {
                                    LineItemData lineItemData = lineItemMapData.get(element.getString("id"));
                                    totalUberEats += lineItemData.uberEats;
                                    totalGrubHub += lineItemData.grubHub;
                                    totalDoorDash += lineItemData.doorDash;
                                    totalPostmates += lineItemData.postmates;
                                    if (discountsData.containsKey(element.getString("id")) && discountsData.get(element.getString("id")) != 100) {
                                        Float discountValue = discountsData.get(element.getString("id")) * lineItemData.total / 100.0f;
                                        DecimalFormat twoDForm = new DecimalFormat("#.##");
                                        discountValue = Float.valueOf(twoDForm.format(discountValue));
                                        totalDiscounts += discountValue;
                                        Log.d("Discounts", discountValue + "");
                                    }
                                }
                                else {
                                    if( element.has("lineItems") &&
                                            element.getJSONObject("lineItems").getJSONArray("elements").length() > 0 &&
                                            discountsData.containsKey(element.getString("id")) &&
                                            discountsData.get(element.getString("id")) != 100) {
                                        Float total = 0.0f;
                                        for ( int j = 0; j < element.getJSONObject("lineItems").getJSONArray("elements").length(); j++) {
                                            total += element.getJSONObject("lineItems").getJSONArray("elements").getJSONObject(j).getInt("price");
                                        }

                                        Float discountValue = discountsData.get(element.getString("id")) * total / 100.0f;
                                        DecimalFormat twoDForm = new DecimalFormat("#.##");
                                        discountValue = Float.valueOf(twoDForm.format(discountValue));
                                        totalDiscounts += discountValue;
                                        Log.d("Discounts", discountValue + "");
                                    }
                                }

                                if(element.getJSONObject("employee").getString("id").equals(General.onlineEmployee)) {
                                    totalOnlineOrder += element.getDouble("total");
//                                        Log.d("total",""+element.getDouble("total"));
                                }
                                orderCount ++;
                            }

                            if(element.has("payments"))
                            for(int pi = 0; pi < element.getJSONObject("payments").getJSONArray("elements").length(); pi++) {
                                String id = element.getJSONObject("payments").getJSONArray("elements").getJSONObject(pi).getString("id");
                                JSONObject payment = closeOutPaymentData.getJSONObject(paymentsData.get(id));
                                if (payment.has("tipAmount")) {
                                    totalTips += payment.getInt("tipAmount");
                                }

                                switch (payment.getJSONObject("tender").getString("label")) {
                                    case "Gift Cards":
                                        gift += payment.getInt("amount");
                                        break;
                                    case "Credit Card":
                                        creditCard += payment.getInt("amount");
                                        cardPaymentCount++;
                                        cardTip += payment.optInt("tipAmount");
                                        break;
                                    case "Debit Card":
                                        debitCard += payment.getInt("amount");
                                        cardPaymentCount++;
                                        cardTip += payment.optInt("tipAmount");
                                        break;
                                    case "Cash":
                                        cash += payment.getInt("amount");
                                        break;
                                }

//                                    if(refundsData.containsKey(payment.getString("id"))) {
//                                        totalRefunds += refundsData.get(payment.getString("id"));
//                                    }
//                                    paymentTotal += element.getInt("amount");

//                                    if (payment.has("refunds") && payment.getJSONObject("refunds").getJSONArray("elements").length() > 0) {
//                                        for (int j = 0; j < payment.getJSONObject("refunds").getJSONArray("elements").length(); j++) {
//                                            JSONObject refund = payment.getJSONObject("refunds").getJSONArray("elements").getJSONObject(j);
//                                            totalRefunds += refund.getDouble("amount");
////                                            totalRefundsTax += refund.getDouble("taxAmount");
//                                        }
//                                    }
                            }
                        }

                        onCalculationCloseOut();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                });
            }
        });
    }
    @SuppressLint({"DefaultLocale","SimpleDateFormat"})
    void onCalculationCloseOut(){
//            amountCollected = totalPaymentsWithoutTax + totalTips;
//            unpaidBalance = orderTotal - totalPaymentsWithoutTax;
            String message = "Reports for period: ";

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy hh a");

            new CloseOutData(
                    dateFormat.format(startDate) + " - " + dateFormat.format(endDate),
                    String.format("$%.2f", (totalPaymentsWithoutTax /*+ totalTips*/) / 100),
                    String.format("$%.2f", (debitCard+creditCard+cardTip)/100),
                    String.format("$%.2f", totalDiscounts / 100),
                    String.format("$%.2f", (cash / 100)),
                    String.format("$%.2f", totalGrubHub/100),
                    String.format("$%.2f", totalDoorDash /100),
                    String.format("$%.2f", totalPostmates /100),
                    String.format("$%.2f", totalOnlineOrder/100),
                    String.format("$%.2f", totalRefunds / 100),
                    String.format("$%.2f", gift / 100),
                    String.format("$%.2f", totalUberEats / 100),
                    String.format("$%.2f", totalTips / 100)
            );

            message += CloseOutData.period + "\n";

//                            message += "Total Payment: " + total / 100  + "\n";
//                            message += "Orders: " + lockedOrders + "\n";

            message += "Daily Sales: " + CloseOutData.dailySales + "\n";
            message += "Discounts: " + CloseOutData.discounts + "\n";
            message += "Credit/Debit Cards: " + CloseOutData.cardPayment  + "\n";
            message += "Cash: " + CloseOutData.cashTotal + "\n";
            message += "GiftCard Sold: " + CloseOutData.giftPayment + "\n\n";//

//                            message += "GiftCard Sold: " + CloseOutData.giftSold + "\n";

            message += "Uber Eats: " + CloseOutData.uberEat + "\n";
            message += "GrubHub: " + CloseOutData.grubHub+ "\n";
            message += "DoorDash: " + CloseOutData.doorDash + "\n";
            message += "Postmates: " + CloseOutData.postmates + "\n";
            message += "Online Ordering: " + CloseOutData.onlineOrdering + "\n";
            message += "Tips: " + CloseOutData.tips + "\n";
            message += "Refunds: " + CloseOutData.refunds + "\n";
/*
            message += "\n\n";

            message += "Credit Card: " + creditCard/100 + "\n";
            message += "Debit Card: " + debitCard/100 + "\n";
        message += "Card + Tip: " + (debitCard+creditCard+cardTip)/100 + "\n";
        message += "Order Count: " + orderCount + "\n";
        message += "Payment Count: " + paymentCount + "\n";
        message += "Card Payment Count: " + cardPaymentCount + "\n";*/

//                            message += "Online Orders: " + totalOnline / 100 + "\n";
/*
                            message += "total: " + (total ) / 100 + "\n";
                            message += "Order Total: " + (orderTotal ) / 100 + "\n";
                            message += "Amount Collected: " + amountCollected / 100 + "\n";
                            message += "Unpaid Balance: " + unpaidBalance / 100 + "\n";
                            message += "Tax: " + totalPaymentsTax / 100 + "\n";
                            message += "Total with open orders: " + totalWithOpenOrders / 100  + "\n";
*/
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle(General.restaurantName);
            alertDialog.setMessage(message);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "PRINT",
                    (dialog, which) -> onPrintCloseOut());
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                    (dialog, which) -> {

                    });
            alertDialog.show();

    }

    //*******************************************************************************/
    //************************StarPrint**********************************************/
    //*******************************************************************************/
    void onPrint(){
        General.isPrinting = true;
        mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setMessage("Communicating...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        byte[] commands;

        PrinterSettingManager settingManager = new PrinterSettingManager(this);
        PrinterSettings settings       = settingManager.getPrinterSettings();

        if (settings != null) {
            StarIoExt.Emulation emulation = ModelCapability.getEmulation(settings.getModelIndex());
            int paperSize = settings.getPaperSize();
            int modelIndex = settings.getModelIndex();
            ILocalizeReceipts localizeReceipts = ILocalizeReceipts.createLocalizeReceipts(paperSize);


            if(ModelCapability.canPrintRasterReceiptSample(modelIndex)) {
                commands = PrinterFunctions.createRasterReceiptData(emulation, localizeReceipts, getResources());
            }
            else /*if(ModelCapability.canPrintUtf8EncodedText(modelIndex)) */{
                commands = PrinterFunctions.createTextReceiptData(emulation, localizeReceipts, true);
            }

            Communication.sendCommands(this, commands, settings.getPortName(), settings.getPortSettings(), 10000, 30000, this, mCallback);     // 10000mS!!!
        }
        else {
            ///TEST
//            PrinterFunctions.createRasterReceiptData(emulation, localizeReceipts, getResources());
//            EnglishReceiptsImpl.receiptFormatRaster(null,38,23, PrinterSettingConstant.PAPER_SIZE_THREE_INCH);
            General.isPrinting = false;
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        }
    }
    void onPrintCloseOut(){
        General.isPrinting = true;
        mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setMessage("Communicating...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        byte[] commands;

        PrinterSettingManager settingManager = new PrinterSettingManager(this);
        PrinterSettings settings       = settingManager.getPrinterSettings();

        if (settings != null) {
            StarIoExt.Emulation emulation = ModelCapability.getEmulation(settings.getModelIndex());
            int paperSize = settings.getPaperSize();
            int modelIndex = settings.getModelIndex();
            ILocalizeReceipts localizeReceipts = ILocalizeReceipts.createLocalizeReceipts(paperSize);

            if(ModelCapability.canPrintRasterReceiptSample(modelIndex)) {
                commands = PrinterFunctions.createRasterCloseOutData(emulation, localizeReceipts, getResources());
            }
            else /*if(ModelCapability.canPrintUtf8EncodedText(modelIndex)) */{
                commands = PrinterFunctions.createTextCloseOutData(emulation, localizeReceipts, true);
            }

            Communication.sendCommands(this, commands, settings.getPortName(), settings.getPortSettings(), 10000, 30000, this, mCallback);     // 10000mS!!!
        }
        else {
            ///TEST
//            PrinterFunctions.createRasterReceiptData(emulation, localizeReceipts, getResources());
//            EnglishReceiptsImpl.receiptFormatRaster(null,38,23, PrinterSettingConstant.PAPER_SIZE_THREE_INCH);
            General.isPrinting = false;
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        }
    }
    private final Communication.SendCallback mCallback = new Communication.SendCallback() {
        @Override
        public void onStatus(CommunicationResult communicationResult) {
//            if (!mIsForeground) {
//                return;
//            }

            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }

            Toast.makeText(MainActivity.this, Communication.getCommunicationResultMessage(communicationResult), Toast.LENGTH_LONG).show();
            General.isPrinting = false;
//            CommonAlertDialogFragment dialog = CommonAlertDialogFragment.newInstance("CommResultDialog");
//            dialog.setTitle("Communication Result");
//            dialog.setMessage(Communication.getCommunicationResultMessage(communicationResult));
//            dialog.setPositiveButton("OK");
//            dialog.show(getSupportFragmentManager());
        }
    };
    @Override
    public void onResume() {
        super.onResume();

        TextView txtRestaurantTitle = findViewById(R.id.txt_restaurant_title);
        txtRestaurantTitle.setText(General.restaurantName);
        txtRestaurantTitle.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://mysantinis.com/orderonline/"));
            startActivity(browserIntent);
        });
    }

    @Override
    public void onPause() {
        super.onPause();


        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onDialogResult(String tag, Intent data) {
        General.isPrinting = false;
    }

    public void onPrinterSettings() {
        Intent intent = new Intent(this,PrinterSetupActivity.class);
        intent.putExtra("fromSettings", true);
        startActivity(intent);
    }
    public void onRestaurantSettings() {
        Intent intent = new Intent(this,SelectRestaurantActivity.class);
        intent.putExtra("fromSettings", true);
        startActivity(intent);
    }
    //*******************************************************************************/
    //************************Clover Printer*****************************************/
    //*******************************************************************************/

//    private static final String TAG = POSActivity.class.getSimpleName();
//    public static final String EXAMPLE_POS_SERVER_KEY = "clover_device_endpoint";
//    public static final int WS_ENDPOINT_ACTIVITY = 123;
//    public static final String EXTRA_CLOVER_CONNECTOR_CONFIG = "EXTRA_CLOVER_CONNECTOR_CONFIG";
//    public static final String EXTRA_WS_ENDPOINT = "WS_ENDPOINT";
//    public static final String EXTRA_CLEAR_TOKEN = "CLEAR_TOKEN";
//    private static int RESULT_LOAD_IMG = 1;
//    public static List<Printer> printers;
//    private Printer printer;
//    public static String lastPrintRequestId;
//    private int printRequestId = 0;
//
//    boolean usb = true;
//
//    ICloverConnector cloverConnector;
//
//    private android.app.AlertDialog pairingCodeDialog;
//
//    private transient CloverDeviceEvent.DeviceEventState lastDeviceEvent;
//    private SharedPreferences sharedPreferences;

//    void printerInit(){
//        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//
//        String posName = "My Santis";
//        String applicationId = posName + ":1.0";
//        CloverDeviceConfiguration config;
//
//        String configType = getIntent().getStringExtra(EXTRA_CLOVER_CONNECTOR_CONFIG);
//        if ("USB".equals(configType)) {
//            config = new USBCloverDeviceConfiguration(this, applicationId);
//        } else if ("WS".equals(configType)) {
//
//            String serialNumber = "Aisle 3";
//            String authToken = null;
//
//            URI uri = (URI) getIntent().getSerializableExtra(EXTRA_WS_ENDPOINT);
//
//            String query = uri.getRawQuery();
//            if (query != null) {
//                try {
//                    String[] nameValuePairs = query.split("&");
//                    for (String nameValuePair : nameValuePairs) {
//                        String[] nameAndValue = nameValuePair.split("=", 2);
//                        String name = URLDecoder.decode(nameAndValue[0], "UTF-8");
//                        String value = URLDecoder.decode(nameAndValue[1], "UTF-8");
//
//                        if("authenticationToken".equals(name)) {
//                            authToken = value;
//                        } else {
//                            Log.w(TAG, String.format("Found query parameter \"%s\" with value \"%s\"",
//                                    name, value));
//                        }
//                    }
//                    uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),uri.getPort(), uri.getPath(), null,uri.getFragment());
//                } catch (Exception e) {
//                    Log.e(TAG, "Error extracting query information from uri.", e);
//                    setResult(RESULT_CANCELED);
//                    finish();
//                    return;
//                }
//            }
//
//            // NOTE:  At the moment, we are always loading our certs from resources.  Opened JIRA SEMI-2147 to
//            // add capability to load from the network endpoints dynamically.  Will need to refactor this code
//            // to pull network access off the main thread though...
//            KeyStore trustStore = SecurityUtils.createTrustStore(true);
//
//            if(authToken == null) {
//                boolean clearToken = getIntent().getBooleanExtra(EXTRA_CLEAR_TOKEN, false);
//                if (!clearToken) {
//                    authToken = sharedPreferences.getString("AUTH_TOKEN", null);
//                }
//            }
//            config = new WebSocketCloverDeviceConfiguration(uri, applicationId, trustStore, posName, serialNumber, authToken) {
//                @Override
//                public int getMaxMessageCharacters() {
//                    return 0;
//                }
//
//                @Override
//                public void onPairingCode(final String pairingCode) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            // If we previously created a dialog and the pairing failed, reuse
//                            // the dialog previously created so that we don't get a stack of dialogs
//                            if (pairingCodeDialog != null) {
//                                pairingCodeDialog.setMessage("Enter pairing code: " + pairingCode);
//                            } else {
//                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
//                                builder.setTitle("Pairing Code");
//                                builder.setMessage("Enter pairing code: " + pairingCode);
//                                pairingCodeDialog = builder.create();
//                            }
//                            pairingCodeDialog.show();
//                        }
//                    });
//                }
//
//                @Override
//                public void onPairingSuccess(String authToken) {
//                    Preferences.userNodeForPackage(POSActivity.class).put("AUTH_TOKEN", authToken);
//                    sharedPreferences.edit().putString("AUTH_TOKEN", authToken).apply();
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (pairingCodeDialog != null && pairingCodeDialog.isShowing()) {
//                                pairingCodeDialog.dismiss();
//                                pairingCodeDialog = null;
//                            }
//                        }
//                    });
//                }
//            };
//        } else {
//            finish();
//            return;
//        }
//
//        cloverConnector = CloverConnectorFactory.createICloverConnector(config);
//        initialize();
//    }
//    public void initialize() {
//
//        if (cloverConnector != null) {
//            cloverConnector.dispose();
//        }
//
//        ICloverConnectorListener ccListener = new ICloverConnectorListener() {
//            public void onDeviceDisconnected() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
//                        Log.d(TAG, "onDeviceDisconnected");
//                        ((TextView) findViewById(R.id.ConnectionStatusLabel)).setText("Disconnected");
//                    }
//                });
//
//            }
//
//            public void onDeviceConnected() {
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        showMessage("Connecting...", Toast.LENGTH_SHORT);
//                        Log.d(TAG, "onDeviceConnected");
//                        ((TextView) findViewById(R.id.ConnectionStatusLabel)).setText("Connecting");
//                    }
//                });
//            }
//
//            public void onDeviceReady(final MerchantInfo merchantInfo) {
//                Log.d(TAG, "onDeviceReady: MerchantInfo: " + merchantInfo.toString());
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        if (pairingCodeDialog != null && pairingCodeDialog.isShowing()) {
//                            pairingCodeDialog.dismiss();
//                            pairingCodeDialog = null;
//                        }
//                        showMessage("Ready!", Toast.LENGTH_SHORT);
//                        ((TextView) findViewById(R.id.ConnectionStatusLabel)).setText(String.format("Connected: %s (%s)", merchantInfo.getDeviceInfo().getSerial(), merchantInfo.getMerchantName()));
//                    }
//                });
//                RetrievePrintersRequest rpr = new RetrievePrintersRequest();
//                cloverConnector.retrievePrinters(rpr);
//            }
//
//            @Override
//            public void onDeviceActivityStart(final CloverDeviceEvent deviceEvent) {
//                Log.d(TAG, "onDeviceActivityStart: CloverDeviceEvent: " + deviceEvent);
//                lastDeviceEvent = deviceEvent.getEventState();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ((TextView) findViewById(R.id.DeviceStatus)).setText(deviceEvent.getMessage());
//                        Toast.makeText(MainActivity.this, deviceEvent.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//
//            @Override
//            public void onDeviceActivityEnd(final CloverDeviceEvent deviceEvent) {
//                Log.d(TAG, "onDeviceActivityEnd: CloverDeviceEvent: " + deviceEvent);
//                if (deviceEvent.getEventState() == lastDeviceEvent) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            ((TextView) findViewById(R.id.DeviceStatus)).setText("");
//                            LinearLayout ll = findViewById(R.id.DeviceOptionsPanel);
//                            ll.removeAllViews();
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onDeviceError(CloverDeviceErrorEvent deviceErrorEvent) {
//                Log.d(TAG, "onDeviceError: CloverDeviceErrorEvent: " + deviceErrorEvent);
//                showMessage("DeviceError: " + deviceErrorEvent.getMessage(), Toast.LENGTH_LONG);
//            }
//
//            @Override
//            public void onAuthResponse(final AuthResponse response) {
//                Log.d(TAG, "onAuthResponse: " + response.toString());
//            }
//
//            @Override
//            public void onPreAuthResponse(final PreAuthResponse response) {
//                Log.d(TAG, "onPreAuthResponse: " + response.toString());
//            }
//
//            @Override
//            public void onRetrievePendingPaymentsResponse(RetrievePendingPaymentsResponse response) {
//                Log.d(TAG, "onRetrievePendingPaymentsResponse: " + response.toString());
//            }
//
//            @Override
//            public void onReadCardDataResponse(ReadCardDataResponse response) {
//
//            }
//
//            @Override
//            public void onTipAdjustAuthResponse(TipAdjustAuthResponse response) {
//
//            }
//
//            @Override
//            public void onCapturePreAuthResponse(final CapturePreAuthResponse response) {
//                Log.d(TAG, "onCapturePreAuthResponse: " + response);
//
//            }
//
//            @Override
//            public void onVerifySignatureRequest(final VerifySignatureRequest request) {
//
//            }
//
//            @Override
//            public void onMessageFromActivity(MessageFromActivity message) {
//                Log.d(TAG, "onMessageFromActivity: " + message.toString());
//            }
//
//            @Override
//            public void onConfirmPaymentRequest(ConfirmPaymentRequest request) {
//                Log.d(TAG, "onConfirmPaymentRequest: " + request.toString());
//            }
//
//            @Override
//            public void onCloseoutResponse(CloseoutResponse response) {
//                Log.d(TAG, "onCloseoutResponse: " + response.toString());
//            }
//
//            @Override
//            public void onSaleResponse(final SaleResponse response) {
//                Log.d(TAG, "onSaleResponse: " + response.toString());
//            }
//
//            @Override
//            public void onManualRefundResponse(final ManualRefundResponse response) {
//                Log.d(TAG, "onManualRefundResponse: " + response.toString());
//            }
//
//            @Override
//            public void onRefundPaymentResponse(final RefundPaymentResponse response) {
//                Log.d(TAG, "onRefundPaymentResponse: " + response.toString());
//            }
//
//
//            @Override
//            public void onTipAdded(TipAddedMessage message) {
//                Log.d(TAG, "onTipAdded: " + message.toString());
//            }
//
//            @Override
//            public void onVoidPaymentResponse(VoidPaymentResponse response) {
//                Log.d(TAG, "onVoidPaymentResponse: " + response.toString());
//            }
//
//            /**
//             * Called in response to a void payment refund request
//             *
//             * @param response The response
//             */
//            @Override
//            public void onVoidPaymentRefundResponse(final VoidPaymentRefundResponse response) {
//                Log.d(TAG, "onVoidPaymentRefundResponse: " + response.toString());
//            }
//
//            @Override
//            public void onVaultCardResponse(final VaultCardResponse response) {
//                Log.d(TAG, "onVaultCardResponse" + response.toString());
//            }
//
//            @Override
//            public void onPrintJobStatusResponse(PrintJobStatusResponse response) {
//                Log.d(TAG, "onPrintJobStatusResponse: " + response.toString());
//                showMessage("PrintJobStatus: " + response.getStatus(), Toast.LENGTH_SHORT);
//            }
//
//            @Override
//            public void onRetrievePrintersResponse(RetrievePrintersResponse response) {
//                Log.d(TAG, "onRetrievePrintersResponse: " + response.toString());
//                printers = response.getPrinters();
//                if(printers != null){
//                    printer = printers.get(0);
//                }
//            }
//
//            @Override
//            public void onPrintManualRefundReceipt(PrintManualRefundReceiptMessage pcm) {
//                Log.d(TAG, "onPrintManualRefundReceipt: " + pcm.toString());
//                showMessage("Print Request for ManualRefund", Toast.LENGTH_SHORT);
//            }
//
//            @Override
//            public void onPrintManualRefundDeclineReceipt(PrintManualRefundDeclineReceiptMessage pcdrm) {
//                Log.d(TAG, "onPrintManualRefundDeclineReceipt: " + pcdrm.toString());
//                showMessage("Print Request for Declined ManualRefund", Toast.LENGTH_SHORT);
//            }
//
//            @Override
//            public void onPrintPaymentReceipt(PrintPaymentReceiptMessage pprm) {
//                Log.d(TAG, "onPrintPaymentReceipt: " + pprm.toString());
//                showMessage("Print Request for Payment Receipt", Toast.LENGTH_SHORT);
//            }
//
//            @Override
//            public void onPrintPaymentDeclineReceipt(PrintPaymentDeclineReceiptMessage ppdrm) {
//                Log.d(TAG, "onPrintPaymentDeclineReceipt: " + ppdrm.toString());
//                showMessage("Print Request for DeclinedPayment Receipt", Toast.LENGTH_SHORT);
//            }
//
//            @Override
//            public void onPrintPaymentMerchantCopyReceipt(PrintPaymentMerchantCopyReceiptMessage ppmcrm) {
//                Log.d(TAG, "onPrintPaymentMerchantCopyReceipt: " + ppmcrm.toString());
//                showMessage("Print Request for MerchantCopy of a Payment Receipt", Toast.LENGTH_SHORT);
//            }
//
//            @Override
//            public void onPrintRefundPaymentReceipt(PrintRefundPaymentReceiptMessage pprrm) {
//                Log.d(TAG, "onPrintRefundPaymentReceipt: " + pprrm.toString());
//                showMessage("Print Request for RefundPayment Receipt", Toast.LENGTH_SHORT);
//            }
//
//            @Override
//            public void onCustomActivityResponse(CustomActivityResponse response) {
//                Log.d(TAG, "onCustomActivityResponse: " + response.toString());
//            }
//
//            @Override
//            public void onRetrieveDeviceStatusResponse(RetrieveDeviceStatusResponse response) {
//                Log.d(TAG, "onRetrieveDeviceStatusResponse: " + response.toString());
//                showPopupMessage("Device Status", new String[]{response.isSuccess() ? "Success!" : "Failed!",
//                        "State: " + response.getState(), "ExternalActivityId: " + response.getData().toString(), "Reason: " + response.getReason() }, false);
//            }
//
//            @Override
//            public void onInvalidStateTransitionResponse(InvalidStateTransitionResponse response) {
//
//            }
//
//            @Override
//            public void onResetDeviceResponse(ResetDeviceResponse response) {
//                Log.d(TAG, "onResetDeviceResponse: " + response.toString());
//                showPopupMessage("Reset Device", new String[]{response.isSuccess() ? "Success!" : "Failed!", "State: " + response.getState(), "Reason: " + response.getReason()}, false);
//            }
//
//            @Override
//            public void onRetrievePaymentResponse(RetrievePaymentResponse response) {
//                Log.d(TAG, "onRetrievePaymentResponse: " + response.toString());
//                if (response.isSuccess()) {
//                    showPopupMessage("Retrieve Payment", new String[]{"Retrieve Payment successful for Payment ID: " + response.getExternalPaymentId(),
//                            " QueryStatus: " + response.getQueryStatus(),
//                            " Payment: " + response.getPayment(),
//                            " reason: " + response.getReason()}, false);
//                } else {
//                    showPopupMessage(null, new String[]{"Retrieve Payment error: " + response.getResult()}, false);
//                }
//            }
//
//            @Override
//            public void onCustomerProvidedData(CustomerProvidedDataEvent event) {
//
//            }
//
//            @Override
//            public void onDisplayReceiptOptionsResponse(DisplayReceiptOptionsResponse response) {
//                Log.d(TAG, "onDisplayReceiptOptionsResponse: " + response.toString());
//                showMessage("Display Receipt Options", Toast.LENGTH_SHORT);
//            }
//        };
//        cloverConnector.addCloverConnectorListener(ccListener);
//        cloverConnector.initializeConnection();
//    }
//    public void printTextClover(List<String> lines) {
//        if(printer != null){
//            PrintRequest pr = new PrintRequest(lines);
//            lastPrintRequestId = String.valueOf(getNextPrintRequestId());
//            pr.setPrintRequestId(lastPrintRequestId);
//            pr = new PrintRequest(lines, lastPrintRequestId, printer.getId());
//            Log.d(TAG, "PrintRequest - Print Text: " + pr.toString());
//            cloverConnector.print(pr);
//        }
//        else {
//            Log.e(TAG,"Printer is null");
//        }
//    }
//    private int getNextPrintRequestId(){
//        return ++printRequestId;
//    }
//    private void showMessage(final String msg, final int duration) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(MainActivity.this, msg, duration).show();
//            }
//        });
//    }

//    protected void showPopupMessage (final String title, final String[] content, final boolean monospace) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                FragmentManager fm = getFragmentManager();
//                PopupMessageFragment popupMessageFragment = PopupMessageFragment.newInstance(title, content, monospace);
//                popupMessageFragment.show(fm, "fragment_popup_message");
//            }
//        });
//    }
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (cloverConnector != null) {
//            cloverConnector.dispose();
//        }
//    }

}
