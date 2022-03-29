package com.app.mysantinis;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.multidex.MultiDexApplication;

import com.app.mysantinis.activity.MainActivity;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class General extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {

    public static String site_url_orders = "https://mysantinis.com/orderonline/wp-json/wc/v2/orders";
    public static String site_url_products = "https://mysantinis.com/orderonline/wp-json/wc/v3/products";
    public static String site_url_corder = "https://mysantinis.com/orderonline/createorderandroidapp";
    public static String site_url_location = "https://mysantinis.com/orderonline/wp-json/santinis/v1/locations/";
    public static String login_url = "https://mysantinis.com/orderonline/wp-json/jwt-auth/v1/token";
    public static String validate_token_url = "https://mysantinis.com/orderonline/wp-json/jwt-auth/v1/token/validate";
//    public static String auth_key = "cmVzdG9uX3RhYmxldDpzYW50aW5pczRhbGwh";
//    public static String auth_key = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczpcL1wvbXlzYW50aW5pcy5jb21cL29yZGVyb25saW5lIiwiaWF0IjoxNTk5NTA0NDkzLCJuYmYiOjE1OTk1MDQ0OTMsImV4cCI6MTYwMDEwOTI5MywiZGF0YSI6eyJ1c2VyIjp7ImlkIjoyfX19.rWWMAQW0v2numU2XaF_Gew4ac13BsjK9X4K7_4xzIc4";
    public static String auth_key = "";
    public static String auth_email = "";
    public static String auth_password = "";
    public static String consumer_key = "ck_7d0a415228bffd4e95609fbf4a2eeebef46291df";
    public static String consumer_secret = "cs_b9a2628041a9cf73039cea87faeb26d56175f819";
    public static String onlineEmployee = "0WDXTQG7AQTNG";
    public static String processing = "processing";
    public static String pending = "pending";
    public static String completed = "completed";
    public static String status = "publish";
    public static boolean isPrinting = false;
//    public static int restaurant_id;
    public static String getOrdersIncoming_url = site_url_orders+"?consumer_key="+consumer_key+"&consumer_secret="+consumer_secret+"&status="+processing;
    public static String getOrdersToday_url = "https://mysantinis.com/orderonline/wp-json/wc/v3/orders"+"?consumer_key="+consumer_key+"&consumer_secret="+consumer_secret;
    public static String getOrdersPending_url = site_url_orders+"?consumer_key="+consumer_key+"&consumer_secret="+consumer_secret+"&status="+pending;
    //public static String getOrdersIncoming_url = site_url_orders+"?consumer_key="+consumer_key+"&consumer_secret="+consumer_secret;
    public static String getOrdersFinished_url = site_url_orders+"?consumer_key="+consumer_key+"&consumer_secret="+consumer_secret+"&status="+completed+"&per_page=100";
    public static String restaurantId="";
    public static String orderId="";
    public static String restaurantName="";
    public static String merchantId = "";
    public static String accessToken = "";
    public static JSONObject detailData;
    public static JSONObject newOrderData;
    public static int newOrderNumber = 0;

    public static String getProducts_url_1 = site_url_products + "?status=publish&per_page=100&page=1" + "&consumer_key="+consumer_key+"&consumer_secret="+consumer_secret;
//    public static String getProducts_url_2 = site_url_products + "?status=publish&per_page=100&page=2" + "&consumer_key="+consumer_key+"&consumer_secret="+consumer_secret;
    public static String create_order_url = site_url_corder + "?consumer_key="+consumer_key+"&consumer_secret="+consumer_secret;

    public static String clover_url = "https://api.clover.com/v3/merchants/";
    public static String notificationOrder_title = "Order Notification";
    public static String notificationOrder_text = "New order received";
    public final String CHANNEL_ID = "my_channel_01";
    public static Float printerFontSize = 1.0f;
    public static String userLogged = "0";
    private static boolean activityVisible;

    public static MultiDexApplication instanceOfApplication;
    public static JSONArray filterData(JSONArray array){
        try {
            for( int i = 0; i < array.length(); i++) {
                if(!array.getJSONObject(i).getString("restaurant_id").equals(General.restaurantId)) {
                    array.remove(i);
                    i--;
                }
            }
            return array;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new JSONArray();
    }
    public static JSONArray filterPendingData(JSONArray array){
        if(array == null ) return new JSONArray();
        try {
            for( int i = 0; i < array.length(); i++) {
                if(!array.getJSONObject(i).getString("status").equals("processing")) {
                    array.remove(i);
                    i--;
                }
            }
            return array;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new JSONArray();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        providerInstall();

        instanceOfApplication = this;
    }



    public void providerInstall() {
        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());
            SSLContext sslContext;
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            sslContext.createSSLEngine();
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException
                | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }
    public String checkNewOrderIncoming (){

        String getOrders_url = General.getOrdersIncoming_url + "&restaurant_id=" + General.restaurantId;
        int newOrdersIncoming;
        String check ="";

        Request request = new Request.Builder()
                .url(getOrders_url)
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        try {
            JSONArray arrayOrdersIncoming = new JSONArray(Objects.requireNonNull(client.newCall(request).execute().body()).string());
            newOrdersIncoming = arrayOrdersIncoming.length();
            if(newOrdersIncoming!=0)
            {
                check = "1";
            }
            else
            {
                check = "0";
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }


        return check;
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if(activity instanceof MainActivity )
            activityVisible = true;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if(activity instanceof MainActivity ) activityVisible = true;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if(activity instanceof MainActivity )
            activityVisible = false;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if(activity instanceof MainActivity )
            activityVisible = false;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if(activity instanceof MainActivity )
            activityVisible = false;
    }
}
