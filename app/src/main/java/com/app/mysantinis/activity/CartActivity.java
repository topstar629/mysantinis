package com.app.mysantinis.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mysantinis.adapter.CartAdapter;
import com.app.mysantinis.module.ProductImage;
import com.app.mysantinis.General;
import com.app.mysantinis.R;


import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static com.app.mysantinis.adapter.ProductAdapter.cartModels;


public class CartActivity extends AppCompatActivity {

    public static TextView grandTotal;
    public static float grandTotalplus;
    // create a temp list and add cartitem list
    public static ArrayList<ProductImage> temparraylist;
    RecyclerView cartRecyclerView;
    CartAdapter cartAdapter;
    LinearLayout proceedToBook;
    Context context;
    private Toolbar mToolbar;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        context = this;
        temparraylist = new ArrayList<>();
        mToolbar = findViewById(R.id.toolbar);
        proceedToBook = findViewById(R.id.proceed_to_book);
        grandTotal = findViewById(R.id.grand_total_cart);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Cart");

        // testing part


        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back_arrow));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // these lines of code for show the same  cart for future refrence
                grandTotalplus = 0;
                for (int i = 0; i < temparraylist.size(); i++) {

                }
                cartModels.addAll(temparraylist);
                CreateOrderActivity.cart_count = (temparraylist.size());
//                addItemInCart.clear();
                finish();
            }
        });
        CreateOrderActivity.cart_count = 0;

        //addInCart();

        Log.d("sizecart_1", String.valueOf(temparraylist.size()));
        Log.d("sizecart_2", String.valueOf(cartModels.size()));

        // from these lines of code we remove the duplicacy of cart and set last added quantity in cart
        // for replace same item
        for (int i = 0; i < cartModels.size(); i++) {
            for (int j = i + 1; j < cartModels.size(); j++) {
               // if (cartModels.get(i).getProductImage().equals(cartModels.get(j).getProductImage())) {
                if(cartModels.get(i).getProductID().equals(cartModels.get(j).getProductID())){
                    cartModels.get(i).setProductQuantity(cartModels.get(j).getProductQuantity());
                    cartModels.get(i).setTotalCash(cartModels.get(j).getTotalCash());
                    cartModels.remove(j);
                    j--;
                    Log.d("remove", String.valueOf(cartModels.size()));

                }
            }

        }
        temparraylist.addAll(cartModels);
        cartModels.clear();
        Log.d("sizecart_11", String.valueOf(temparraylist.size()));
        Log.d("sizecart_22", String.valueOf(cartModels.size()));
        // this code is for get total cash
        for (int i = 0; i < temparraylist.size(); i++) {
            grandTotalplus = grandTotalplus + temparraylist.get(i).getTotalCash();
        }
        grandTotal.setText("Total: " + grandTotalplus +" $");
        cartRecyclerView = findViewById(R.id.recycler_view_cart);
        cartAdapter = new CartAdapter(temparraylist, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        cartRecyclerView.setLayoutManager(mLayoutManager);
        cartRecyclerView.setAdapter(cartAdapter);


        proceedToBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView tv_cfname= findViewById(R.id.c_fname);
                String fname = tv_cfname.getText().toString();

                TextView tv_clname= findViewById(R.id.c_lname);
                String lname = tv_clname.getText().toString();

                TextView tv_cphone = findViewById(R.id.c_phone);
                String cphone = tv_cphone.getText().toString();

                TextView tv_email = findViewById(R.id.c_email);
                String cemail = tv_email.getText().toString();

                if(fname.intern() == "")
                {
                    Toast.makeText(CartActivity.this, "First name is required", Toast.LENGTH_SHORT).show();
                }
                else if(lname.intern() == "")
                {
                    Toast.makeText(CartActivity.this, "Last name is required", Toast.LENGTH_SHORT).show();
                }
                else if(cphone.intern() == "")
                {
                    Toast.makeText(CartActivity.this, "Phone number is required", Toast.LENGTH_SHORT).show();
                }
                else if(cemail.intern() == "")
                {
                    Toast.makeText(CartActivity.this, "Email address is required", Toast.LENGTH_SHORT).show();
                }
                else
                {

                    String order_product = "{";
                    for (int k = 0; k < temparraylist.size(); k++) {
                        order_product = order_product+"\""+k+"\":{\"id\":"+temparraylist.get(k).getProductID()+",\"q\":"+temparraylist.get(k).getProductQuantity()+"},";
                    }
                    order_product = order_product.substring(0, order_product.length() - 1);
                    order_product = order_product+"}";

                    temparraylist.clear();
                    tv_cfname.setText("");
                    tv_clname.setText("");
                    tv_cphone.setText("");
                    tv_email.setText("");


                   RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("first_name", fname )
                            .addFormDataPart("last_name", lname )
                            .addFormDataPart("email", cemail )
                            .addFormDataPart("phone", cphone )
                            .addFormDataPart("products",order_product )
                            .build();


                    OkHttpClient client = new OkHttpClient();
                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .url(General.create_order_url)
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
                        public void onResponse(Call call, okhttp3.Response response) throws IOException {
                            if (response.isSuccessful()) {
                                final String myResponse = response.body().string();

                                CartActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        System.out.println("response: "+myResponse);

                                    }
                                });
                            }
                        }

                    });


                    //Toast.makeText(CartActivity.this, "Order placed", Toast.LENGTH_SHORT).show();
                    AlertDialog alertDialog = new AlertDialog.Builder(CartActivity.this).create();
                    alertDialog.setTitle("Success");
                    alertDialog.setMessage("Order placed");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
//                                    Intent intent = new Intent(CartActivity.this, OrdersView_Incoming.class);
//                                    startActivity(intent);
                                CreateOrderActivity.instanceOfCreatOrderActivity.finish();
                                finish();
                                }
                            });
                    alertDialog.show();
                }


                //bookMyOrder();
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        grandTotalplus = 0;
        for (int i = 0; i < temparraylist.size(); i++) {
            CreateOrderActivity.cart_count = (temparraylist.size());

        }
        cartModels.addAll(temparraylist);
        //cartModels.clear();
    }


}