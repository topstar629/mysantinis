package com.app.mysantinis.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mysantinis.General;
import com.app.mysantinis.R;
import com.app.mysantinis.activity.MainActivity;
import com.app.mysantinis.activity.NewOrderNotificationActivity;
import com.app.mysantinis.adapter.IncomingOrdersAdapter;
import com.app.mysantinis.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IncomingOrdersFragment extends Fragment {
    private IncomingOrdersAdapter adapter;
//    private static JSONArray orderJsonArray;
    private static JSONArray orderTodayJsonArray;
    private RecyclerView recyclerView;
    private TextView txtEmpty;
    private ProgressBar progressBar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_incoming_orders, container, false);
        recyclerView = mView.findViewById(R.id.recycler_view_incoming);
        adapter = new IncomingOrdersAdapter(IncomingOrdersFragment.this, new JSONArray(), this::onItemClicked);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        progressBar = mView.findViewById(R.id.progressBar);
        txtEmpty = mView.findViewById(R.id.txt_empty);

        int delay = 0;
        int period = 60000; // repeat every 5 minutes.
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                try {
                    if(!General.isPrinting) {
                        runInBackground();
                        getRestaurantStatus();
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, delay,period);

        return mView;
    }

    private void onItemClicked(int index){

        MainActivity activity = (MainActivity) getActivity();
        try {
            if (activity != null) {
                activity.configurationDetail(orderTodayJsonArray.getJSONObject(index));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        try {
            runInBackground();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void getTodayOrders(){

        ZoneId zoneId = ZoneId.of("US/Eastern");
        LocalDate today = LocalDate.now(zoneId);
        ZonedDateTime zdtStart = today.atStartOfDay(zoneId);
//        ZonedDateTime zdtStart = today.minusDays(1).atStartOfDay(zoneId);
        String txtStart = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(zdtStart);
        String getOrders_url = General.getOrdersToday_url + "&after=" + txtStart + "&per_page="+100;
        Log.e("Today Order API URL", getOrders_url);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(getOrders_url)
                .build();

//        progressBar.setVisibility(View.VISIBLE);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

//                progressBar.setVisibility(View.GONE);

                call.cancel();
                e.printStackTrace();
                if(getActivity() != null) {
                    ((MainActivity) getActivity()).setStatue(false);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

//                progressBar.setVisibility(View.GONE);

                final String responseOrders = Objects.requireNonNull(response.body()).string();
                Log.e("Today Orders", responseOrders);
                if(getActivity()!= null) getActivity().runOnUiThread(() -> {
                    try {
                        JSONArray jsonArray = new JSONArray(responseOrders);
                        orderTodayJsonArray = General.filterData(jsonArray);
                        Log.e("Today Orders Number", String.valueOf(orderTodayJsonArray.length()));
                        refreshView();
                    } catch (JSONException e) {
                        Log.e("today Orders","today failure");
                        e.printStackTrace();
                    }
                });
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) activity.setStatue(true);
            }
        });
    }

    private void runInBackground() throws Exception{

        String getOrders_url = General.getOrdersIncoming_url;
        System.out.print(getOrders_url);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(getOrders_url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                e.printStackTrace();
                if(getActivity() != null) {
                    ((MainActivity) getActivity()).setStatue(false);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String responseOrders = Objects.requireNonNull(response.body()).string();

                if(getActivity()!= null) getActivity().runOnUiThread(() -> {
                        try {
                            JSONArray jsonArray = new JSONArray(responseOrders);
                            jsonArray = General.filterData(jsonArray);
                            if(jsonArray.length()!= 0) {
                                JSONArray filteredTodayOrder = General.filterPendingData(orderTodayJsonArray);
                                if(filteredTodayOrder == null || filteredTodayOrder.length()==0) {
                                    General.newOrderNumber = jsonArray.length();
                                    newOrderComing(jsonArray.getJSONObject(0));
                                    if(orderTodayJsonArray == null) orderTodayJsonArray = new JSONArray();
                                    orderTodayJsonArray.put(jsonArray.getJSONObject(0));
                                }
                                else if(jsonArray.getJSONObject(0).getInt("id") !=
                                        filteredTodayOrder.getJSONObject(0).getInt("id")) {
                                    General.newOrderNumber = jsonArray.length();
                                    newOrderComing(jsonArray.getJSONObject(0));
                                    orderTodayJsonArray.put(jsonArray.getJSONObject(0));
                                }
                            }

                        } catch (JSONException e) {
                            Log.e("Incoming Orders","Incoming failure");
                            e.printStackTrace();
                        }
                        getTodayOrders();
                    });
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) activity.setStatue(true);
            }
        });
    }
    private void getRestaurantStatus() throws Exception{

        String locationUrl = General.site_url_location + General.restaurantId;

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(locationUrl)
                .addHeader("Authorization","Bearer " + General.auth_key)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                e.printStackTrace();
                if(getActivity() != null) {
                    ((MainActivity) getActivity()).setStatue(false);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String responseOrders = Objects.requireNonNull(response.body()).string();
                MainActivity activity = (MainActivity) getActivity();
                if(activity!= null) activity.runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(responseOrders);
                        Log.d("close restaurant", jsonObject.toString());
                        if(!General.status.equals(jsonObject.getString("status"))) {
                            General.status = jsonObject.getString("status");
                            activity.invalidateOptionsMenu();
                        }
                    } catch (JSONException e) {
                        Log.e("Incoming Orders","Incoming failure");
                        e.printStackTrace();
                    }
                });

                if (activity != null) activity.setStatue(true);
            }
        });
    }
//    void getPendingOrders(final JSONArray processOrders){
//
//        String getOrders_url = General.getOrdersPending_url;
//
//        OkHttpClient client = new OkHttpClient.Builder()
//                .connectTimeout(30, TimeUnit.SECONDS)
//                .writeTimeout(30, TimeUnit.SECONDS)
//                .readTimeout(30, TimeUnit.SECONDS)
//                .build();
//
//        Request request = new Request.Builder()
//                .url(getOrders_url)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                call.cancel();
//                e.printStackTrace();
//                refreshView();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//
//                final String responseOrders = Objects.requireNonNull(response.body()).string();
//
//                if(getActivity()!= null)
//                    getActivity().runOnUiThread(() -> {
//                        try {
//                            JSONArray jsonArray = new JSONArray(responseOrders);
//                            jsonArray = General.filterData(jsonArray);
//                            for( int i = 0; i < jsonArray.length(); i++) {
//                                processOrders.put(jsonArray.getJSONObject(i));
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                        orderJsonArray = processOrders;
//                        refreshView();
//
//                    });
//                MainActivity activity = (MainActivity) getActivity();
//                if (activity != null) activity.setStatue(true);
//            }
//        });
//    }
    private void refreshView() {
        if(getActivity()!=null) getActivity().runOnUiThread(() -> {
            if (orderTodayJsonArray.length() == 0) {
                if (txtEmpty != null)
                    txtEmpty.setVisibility(View.VISIBLE);
            } else {
                if (txtEmpty != null)
                    txtEmpty.setVisibility(View.GONE);
            }

            adapter = new IncomingOrdersAdapter(IncomingOrdersFragment.this, orderTodayJsonArray, this::onItemClicked);

            final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
            if (recyclerView != null) {
                recyclerView = Objects.requireNonNull(getView()).findViewById(R.id.recycler_view_incoming);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
            }
        });
    }
    private void newOrderComing(JSONObject newOrder) {
        General.newOrderData = newOrder;
        startActivity(new Intent(getActivity(), NewOrderNotificationActivity.class));
    }
}