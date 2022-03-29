package com.app.mysantinis.getStarted;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.app.mysantinis.General;
import com.app.mysantinis.activity.MainActivity;
import com.app.mysantinis.R;
import com.app.mysantinis.activity.SelectRestaurantActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText Name;
    private EditText Password;
    SharedPreferences pref;
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Name = findViewById(R.id.etName);

        Drawable drawable = LoginActivity.this.getResources().getDrawable(R.drawable.user);
        drawable.setBounds(0, 0, 45,45);
        Name.setCompoundDrawables(null, null, drawable, null);

        Password = findViewById(R.id.etPassword);

        Drawable drawable2 = LoginActivity.this.getResources().getDrawable(R.drawable.padlock);
        drawable2.setBounds(0, 0, 45,45);
        Password.setCompoundDrawables(null, null, drawable2, null);

        Button login = findViewById(R.id.btnLogin);

        login.setOnClickListener(v -> onLoginAPI(Name.getText().toString(),Password.getText().toString()));

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);

        pref = getSharedPreferences("MySantinis", Context.MODE_PRIVATE);
        General.auth_key =  pref.getString("auth_key", "");
        General.auth_email = pref.getString("auth_email", "");
        General.auth_password = pref.getString("auth_password", "");

        assert General.auth_key != null;
        if(General.auth_key.equals("")) {
            General.userLogged="0";
        }
        else {
            onValidateToken();
        }
    }

    private void validate(String userName, String userPassword){
        String correctUserName = "reston";
        String correctPassword = "1234";
        if((userName.intern().equals(correctUserName)) && (userPassword.intern().equals(correctPassword))){
//            onLoginAPI();
        }
        else
        {
            AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
            alertDialog.setTitle("Login failed");
            alertDialog.setMessage("Invalid username or password");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    (dialog, which) -> dialog.dismiss());
            alertDialog.show();
            General.userLogged="0";
        }
    }
    void errorAlert(String err){
        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        alertDialog.setTitle("Login failed");
        alertDialog.setMessage("Please contact developer to fix the issue\n"+err);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }
    void onLoginAPI(String userName, String userPassword){

        mProgressDialog.show();

        String loginUrl = General.login_url;

        General.auth_email = userName;
        General.auth_password = userPassword;

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", userName)
                .addFormDataPart("password", userPassword)
                .build();

        Request request = new Request.Builder()
                .url(loginUrl)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                e.printStackTrace();
                errorAlert(e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseOrders = Objects.requireNonNull(response.body()).string();
                runOnUiThread(() -> {

                    mProgressDialog.dismiss();

                    try {
                        JSONObject jsonObject = new JSONObject(responseOrders);
                        Log.d("login response", jsonObject.toString());
                        if(jsonObject.getBoolean("success")) {
                            General.auth_key = jsonObject.getJSONObject("data").getString("token");
                            goToNext();
                        }
                        else {
                            AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                            alertDialog.setTitle("Login failed");
                            alertDialog.setMessage(jsonObject.getString("message"));
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    (dialog, which) -> dialog.dismiss());
                            alertDialog.show();
                            General.userLogged="0";
                        }
                    } catch (JSONException e) {
                        Log.e("login",e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    void onRelogin(String username, String password) {

        String loginUrl = General.login_url;

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", username)
                .addFormDataPart("password", password)
                .build();

        Request request = new Request.Builder()
                .url(loginUrl)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                e.printStackTrace();
                errorAlert(e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseOrders = Objects.requireNonNull(response.body()).string();
                runOnUiThread(() -> {

                    mProgressDialog.dismiss();

                    try {
                        JSONObject jsonObject = new JSONObject(responseOrders);
                        Log.d("login response", jsonObject.toString());
                        if(jsonObject.getBoolean("success")) {
                            General.auth_key = jsonObject.getJSONObject("data").getString("token");
                            goToNext();
                        }
                        else {
                            AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                            alertDialog.setTitle("Server error!");
                            alertDialog.setMessage(jsonObject.getString("message"));
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    (dialog, which) -> dialog.dismiss());
                            alertDialog.show();
                            General.userLogged="0";
                        }
                    } catch (JSONException e) {
                        Log.e("login",e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
        });

    }

    void onValidateToken(){

        mProgressDialog.show();

        String validateTokenUrl = General.validate_token_url;

        RequestBody requestBody = RequestBody.create(null, "");

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(validateTokenUrl)
                .post(requestBody)
                .addHeader("Authorization","Bearer " + General.auth_key)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                e.printStackTrace();
                mProgressDialog.dismiss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseOrders = Objects.requireNonNull(response.body()).string();
                runOnUiThread(() -> {
//                    mProgressDialog.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(responseOrders);
                        Log.d("validate response", jsonObject.toString());
                        boolean success = jsonObject.getBoolean("success");
                        if (success) {
                            gotoResActivity();
                            mProgressDialog.dismiss();
                        } else {
                            onRelogin(General.auth_email, General.auth_password);
                        }

                    } catch (JSONException e) {
                        Log.e("close restaurant",e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    void gotoResActivity() {
        Intent intent = new Intent(LoginActivity.this, SelectRestaurantActivity.class);
        startActivity(intent);
        General.userLogged="1";
        finish();
    }

    void saveToken() {
        pref = getSharedPreferences("MySantinis", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("auth_key",General.auth_key);
        editor.putString("auth_email", General.auth_email);
        editor.putString("auth_password", General.auth_password);
        editor.apply();
    }

    void goToNext(){
        gotoResActivity();
        saveToken();
    }
//
//    @Override
//    public void onBackPressed() {
//        moveTaskToBack(true);
//        android.os.Process.killProcess(android.os.Process.myPid());
//        System.exit(1);
//    }
}

