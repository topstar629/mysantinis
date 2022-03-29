package com.app.mysantinis.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mysantinis.module.Converter;
import com.app.mysantinis.adapter.ProductAdapter;
import com.app.mysantinis.module.ProductModel;
import com.app.mysantinis.General;
import com.app.mysantinis.R;

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

public class CreateOrderActivity extends AppCompatActivity implements ProductAdapter.CallBackUs, ProductAdapter.HomeCallBack {

    public static ArrayList<ProductModel> arrayList = new ArrayList<>();
    public static int cart_count = 0;
    public static CreateOrderActivity instanceOfCreatOrderActivity;
    ProductAdapter productAdapter;
    RecyclerView productRecyclerView;
    TextView txtEmpty;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);
        progressBar = findViewById(R.id.progressBar);
        txtEmpty = findViewById(R.id.txt_empty);
        addProduct();

        instanceOfCreatOrderActivity = this;
    }


    private void addProduct() {
        progressBar.setVisibility(View.VISIBLE);

        String getProducts_url = General.getProducts_url_1;
        Log.d("Create Order", getProducts_url);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(getProducts_url)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        txtEmpty.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
                final String responseProducts = response.body().string();

                CreateOrderActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONArray arrayProducts = new JSONArray(responseProducts);


                            for (int i = 0; i < arrayProducts.length(); i++) {
                                String product_name = arrayProducts.getJSONObject(i).getString("name");
                                String product_id = arrayProducts.getJSONObject(i).getString("id");

                                String product_price = arrayProducts.getJSONObject(i).getString("price");
                                String quantity = "999";

                                String image_obj = arrayProducts.getJSONObject(i).getString("images");

                                JSONArray imageArray = new JSONArray(image_obj);

                                String image = "";

                                if(imageArray.length()>0)
                                {
                                    for (int j = 0; j < imageArray.length(); j++) {
                                        image = imageArray.getJSONObject(j).getString("src");
                                    }
                                }
//                                else
//                                {
//                                    image = "https://mysantinis.com/orderonline/wp-content/uploads/2019/02/placeholder-product-600x706.jpg";
//                                }

                                ProductModel productModel = new ProductModel(product_name, product_price, quantity, image,product_id);

                                arrayList.add(productModel);

                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if(arrayList.size() == 0 ) {
                            txtEmpty.setVisibility(View.VISIBLE);
                        }
                        else {
                            txtEmpty.setVisibility(View.GONE);
                        }

                        productAdapter = new ProductAdapter(arrayList, CreateOrderActivity.this, CreateOrderActivity.this);
                        productRecyclerView = findViewById(R.id.product_recycler_view);

                        int column = 3;
                        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            column = 4;
                        }
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(CreateOrderActivity.this, column, LinearLayoutManager.VERTICAL, false);
                        productRecyclerView.setLayoutManager(gridLayoutManager);
                        productRecyclerView.setAdapter(productAdapter);

                    }
                });
            }
        });

        /*
        String getProducts_url2 = General.getProducts_url_2;

        OkHttpClient client2 = new OkHttpClient();

        Request request2 = new Request.Builder()
                .url(getProducts_url2)
                .build();


        client2.newCall(request2).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String responseProducts = response.body().string();

                CreateOrderActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONArray arrayProducts = new JSONArray(responseProducts);

                            for (int i = 0; i < arrayProducts.length(); i++) {
                                String product_name = arrayProducts.getJSONObject(i).getString("name");

                                String product_id = arrayProducts.getJSONObject(i).getString("id");

                                String product_price = arrayProducts.getJSONObject(i).getString("price");
                                String quantity = "999";

                                String image_obj = arrayProducts.getJSONObject(i).getString("images");

                                JSONArray imageArray = new JSONArray(image_obj);

                                String image = "";

                                if(imageArray.length()>0)
                                {
                                    for (int j = 0; j < imageArray.length(); j++) {
                                        image = imageArray.getJSONObject(j).getString("src");
                                    }
                                }
                                else
                                {
                                    image = "https://mysantinis.com/orderonline/wp-content/uploads/2019/02/placeholder-product-600x706.jpg";
                                }

                                ProductModel productModel = new ProductModel(product_name, product_price, quantity, image,product_id);

                                arrayList.add(productModel);

                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        productAdapter = new ProductAdapter(arrayList, CreateOrderActivity.this, CreateOrderActivity.this);
                        productRecyclerView = findViewById(R.id.product_recycler_view);


                        int column = 3;
                        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            column = 4;
                        }
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(CreateOrderActivity.this, column, LinearLayoutManager.VERTICAL, false);

                        productRecyclerView.setLayoutManager(gridLayoutManager);
                        productRecyclerView.setAdapter(productAdapter);

                    }
                });
            }
        });

*/

    }

    @Override
    public void addCartItemView() {
        //addItemToCartMethod();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.cart_action);
        menuItem.setIcon(Converter.convertLayoutToImage(CreateOrderActivity.this, cart_count, R.drawable.ic_shopping_cart_white_24dp));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.cart_action:
                if (cart_count < 1) {
                    Toast.makeText(this, "There is no item in cart", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(this, CartActivity.class));
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void updateCartCount(Context context) {
        invalidateOptionsMenu();
    }

    @Override
    protected void onStart() {
        super.onStart();
        invalidateOptionsMenu();
    }
}
