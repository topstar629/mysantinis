package com.app.mysantinis.starprnt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.mysantinis.R;
import com.app.mysantinis.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class PrinterSetupActivity extends AppCompatActivity {
    private static final int PRINTER_SET_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_setup);

        LinearLayout llPrinter = findViewById(R.id.ll_printer);
        llPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PrinterSetupActivity.this, CommonActivity.class);
                intent.putExtra(CommonActivity.BUNDLE_KEY_ACTIVITY_LAYOUT, R.layout.activity_printer_search);
                intent.putExtra(CommonActivity.BUNDLE_KEY_TOOLBAR_TITLE, "Search Port");
                intent.putExtra(CommonActivity.BUNDLE_KEY_SHOW_HOME_BUTTON, true);
                intent.putExtra(CommonActivity.BUNDLE_KEY_SHOW_RELOAD_BUTTON, true);
                intent.putExtra(CommonActivity.BUNDLE_KEY_PRINTER_SETTING_INDEX, 0);    // Index of the backup printer

                startActivityForResult(intent, PRINTER_SET_REQUEST_CODE);
            }
        });
        PrinterSettingManager settingManager = new PrinterSettingManager(this);
        PrinterSettings       settings       = settingManager.getPrinterSettings();

        boolean fromSettings = getIntent().getBooleanExtra("fromSettings", false);
        if (settings != null && !fromSettings ) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BLUETOOTH_PRIVILEGED) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_PRIVILEGED},1);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        printerSetting();
    }

    private void printerSetting() {
        PrinterSettingManager settingManager = new PrinterSettingManager(this);
        PrinterSettings       settings       = settingManager.getPrinterSettings();

        boolean isDeviceSelected     = false;
        if (settings != null) {
            isDeviceSelected     = true;
        }
        addPrinterInfo(settingManager.getPrinterSettingsList());

        Button btnNext = findViewById(R.id.btn_next);
        if(isDeviceSelected) {
            btnNext.setText("Next");

        }
        else {
            btnNext.setText("Skip");
        }
    }
    private void addPrinterInfo(List<PrinterSettings> settingsList) {
        TextView deviceTextView = findViewById(R.id.deviceTextView);
        TextView deviceDetailTextView = findViewById(R.id.deviceDetailTextView);
        if (settingsList.size() == 0) {
            deviceTextView.setText("Unselected State");
            deviceTextView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.blink));
        }
        else {
            deviceTextView.clearAnimation();
            // Get a port name, MAC address and model name of the destination printer.
            String portName   = settingsList.get(0).getPortName();
            String macAddress = settingsList.get(0).getMacAddress();
            String modelName  = settingsList.get(0).getModelName();

            if (portName.startsWith(PrinterSettingConstant.IF_TYPE_ETHERNET) ||
                    portName.startsWith(PrinterSettingConstant.IF_TYPE_BLUETOOTH)) {
                deviceTextView.setText(modelName);
                if (macAddress.isEmpty()) {
                    deviceDetailTextView.setText(portName);
                }
                else {
                    deviceDetailTextView.setText(String.format("%s (%s)", portName, macAddress));
                }
            }
            else {  // USB interface
                deviceTextView.setText(modelName);
                deviceDetailTextView.setText(portName);
            }
        }
    }
    public void onNext(View view) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PRINTER_SET_REQUEST_CODE) {
            printerSetting();
        }
    }
}