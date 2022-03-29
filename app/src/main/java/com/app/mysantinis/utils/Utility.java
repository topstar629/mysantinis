package com.app.mysantinis.utils;

import android.os.StrictMode;
import android.util.Base64;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Utility {

    public static String Twilio_SID = "AC3b26e1bf78f5a97fbd6a55ce07494ee3";
    public static String Twilio_TOKEN = "a2b5cc8750a7b79677416dbdace0c4fd";
    public static String Twilio_PHONE = "+15712523205";

    public static void sendSMS(String toNumber, String message) {

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        String url = "https://api.twilio.com/2010-04-01/Accounts/" + Twilio_SID + "/Messages";
        String base64EncodedCredentials = "Basic " + Base64.encodeToString((Twilio_SID + ":" + Twilio_TOKEN).getBytes(), Base64.NO_WRAP);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("From", Twilio_PHONE)
                .addFormDataPart("To", toNumber)
                .addFormDataPart("Body", message)
                .build();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", base64EncodedCredentials)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    final String myResponse = response.body().string();
//                }
            }
        });
    }

    public static String getDelayedTime(int delay_time) {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, +delay_time);

        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        String delayed_time = dateFormat.format(calendar.getTime());

        return delayed_time;
    }
}
