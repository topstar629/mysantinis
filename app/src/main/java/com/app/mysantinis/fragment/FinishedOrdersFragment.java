package com.app.mysantinis.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mysantinis.General;
import com.app.mysantinis.R;
import com.app.mysantinis.activity.MainActivity;
import com.app.mysantinis.adapter.FinishedOrdersAdapter;
import com.app.mysantinis.module.FinishedOrderData;
import com.app.mysantinis.utils.EndlessRecyclerViewScrollListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FinishedOrdersFragment extends Fragment {
    public FinishedOrdersAdapter adapter;
//    private List<FinishedOrderData> orderList;
    private RecyclerView recyclerView;
    private TextView txtEmpty;
    private ProgressBar progressBar;
    private JSONArray orderArray;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_finished_orders, container, false);

        orderArray = new JSONArray();

        recyclerView = mView.findViewById(R.id.recyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 1);
        adapter = new FinishedOrdersAdapter(FinishedOrdersFragment.this, orderArray, this::onItemClicked);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        progressBar = mView.findViewById(R.id.progressBar);
        txtEmpty = mView.findViewById(R.id.txt_empty);

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();

        orderArray = new JSONArray();
//        if (adapter != null) adapter.notifyDataSetChanged();

        run(1);

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener((GridLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {

                run(page + 1);

            }

            @Override
            public void onScrolledUp() {

            }

            @Override
            public void onScrolledDown() {

            }
        });
    }
    private void onItemClicked(int index){
//        Log.d("incoming order", item.getText1());
        MainActivity activity = (MainActivity) getActivity();
        try {
            if (activity != null) {
                activity.configurationDetail(orderArray.getJSONObject(index));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void run(int page) {

        String getOrders_url = General.getOrdersFinished_url + "&page=" + page;

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(getOrders_url)
                .build();

        progressBar.setVisibility(View.VISIBLE);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                Log.e("Finished Order", e.getLocalizedMessage());
                if(getActivity()!=null) getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        txtEmpty.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseOrders;
                if (response.body() != null) {
                    responseOrders = response.body().string();
                }
                else {
                    Log.e("Finished Order", "Unexpected null body from response");
                    return;
                }

                if(getActivity() != null) getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setHasFixedSize(true);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());

                    try {
                        JSONArray arrayOrdersIncoming = new JSONArray(responseOrders);
                        JSONArray temp_array = General.filterData(arrayOrdersIncoming);

                        for (int i = 0; i < temp_array.length(); i++) {
                            orderArray.put(temp_array.getJSONObject(i));
                        }

                        if (page == 1) {
                            adapter = new FinishedOrdersAdapter(FinishedOrdersFragment.this, orderArray, FinishedOrdersFragment.this::onItemClicked);
                            recyclerView.setAdapter(adapter);
                        }
                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(orderArray.length() == 0 ) {
                        txtEmpty.setVisibility(View.VISIBLE);
                    }
                    else {
                        txtEmpty.setVisibility(View.GONE);
                    }

                });

            }
        });
    }
}