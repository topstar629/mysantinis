/*
 * Copyright (C) 2018 Clover Network, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.app.mysantinis.cloverprint;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.mysantinis.R;
import com.app.mysantinis.utils.SecurityUtils;
import com.clover.remote.InputOption;
import com.clover.remote.client.CloverConnectorFactory;
import com.clover.remote.client.CloverDeviceConfiguration;
import com.clover.remote.client.ICloverConnector;
import com.clover.remote.client.ICloverConnectorListener;
import com.clover.remote.client.MerchantInfo;
import com.clover.remote.client.USBCloverDeviceConfiguration;
import com.clover.remote.client.WebSocketCloverDeviceConfiguration;
import com.clover.remote.client.messages.AuthResponse;
import com.clover.remote.client.messages.CapturePreAuthResponse;
import com.clover.remote.client.messages.CloseoutResponse;
import com.clover.remote.client.messages.CloverDeviceErrorEvent;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.ConfirmPaymentRequest;
import com.clover.remote.client.messages.CustomActivityResponse;
import com.clover.remote.client.messages.CustomerProvidedDataEvent;
import com.clover.remote.client.messages.DisplayReceiptOptionsResponse;
import com.clover.remote.client.messages.InvalidStateTransitionResponse;
import com.clover.remote.client.messages.ManualRefundResponse;
import com.clover.remote.client.messages.MessageFromActivity;
import com.clover.remote.client.messages.PreAuthResponse;
import com.clover.remote.client.messages.PrintJobStatusResponse;
import com.clover.remote.client.messages.PrintManualRefundDeclineReceiptMessage;
import com.clover.remote.client.messages.PrintManualRefundReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentDeclineReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentMerchantCopyReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentReceiptMessage;
import com.clover.remote.client.messages.PrintRefundPaymentReceiptMessage;
import com.clover.remote.client.messages.PrintRequest;
import com.clover.remote.client.messages.ReadCardDataResponse;
import com.clover.remote.client.messages.RefundPaymentResponse;
import com.clover.remote.client.messages.ResetDeviceResponse;
import com.clover.remote.client.messages.RetrieveDeviceStatusRequest;
import com.clover.remote.client.messages.RetrieveDeviceStatusResponse;
import com.clover.remote.client.messages.RetrievePaymentResponse;
import com.clover.remote.client.messages.RetrievePendingPaymentsResponse;
import com.clover.remote.client.messages.RetrievePrintersRequest;
import com.clover.remote.client.messages.RetrievePrintersResponse;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.remote.client.messages.TipAdjustAuthResponse;
import com.clover.remote.client.messages.VaultCardResponse;
import com.clover.remote.client.messages.VerifySignatureRequest;
import com.clover.remote.client.messages.VoidPaymentRefundResponse;
import com.clover.remote.client.messages.VoidPaymentResponse;
import com.clover.remote.message.TipAddedMessage;
import com.clover.sdk.v3.printer.Printer;

import java.net.URI;
import java.net.URLDecoder;
import java.security.KeyStore;
import java.util.List;
import java.util.prefs.Preferences;

public class POSActivity extends AppCompatActivity {

  private static final String TAG = POSActivity.class.getSimpleName();
  public static final String EXAMPLE_POS_SERVER_KEY = "clover_device_endpoint";
  public static final int WS_ENDPOINT_ACTIVITY = 123;
  public static final String EXTRA_CLOVER_CONNECTOR_CONFIG = "EXTRA_CLOVER_CONNECTOR_CONFIG";
  public static final String EXTRA_WS_ENDPOINT = "WS_ENDPOINT";
  public static final String EXTRA_CLEAR_TOKEN = "CLEAR_TOKEN";
  private static int RESULT_LOAD_IMG = 1;
  public static List<Printer> printers;
  private Printer printer;
  public static String lastPrintRequestId;
  private int printRequestId = 0;

  boolean usb = true;

  ICloverConnector cloverConnector;

  private AlertDialog pairingCodeDialog;

  private transient CloverDeviceEvent.DeviceEventState lastDeviceEvent;
  private SharedPreferences sharedPreferences;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_pos);

    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    String posName = "My Santis";
    String applicationId = posName + ":1.0";
    CloverDeviceConfiguration config;

    String configType = getIntent().getStringExtra(EXTRA_CLOVER_CONNECTOR_CONFIG);
    if ("USB".equals(configType)) {
      config = new USBCloverDeviceConfiguration(this, applicationId);
    } else if ("WS".equals(configType)) {

      String serialNumber = "Aisle 3";
      String authToken = null;

      URI uri = (URI) getIntent().getSerializableExtra(EXTRA_WS_ENDPOINT);

      String query = uri.getRawQuery();
      if (query != null) {
        try {
          String[] nameValuePairs = query.split("&");
          for (String nameValuePair : nameValuePairs) {
            String[] nameAndValue = nameValuePair.split("=", 2);
            String name = URLDecoder.decode(nameAndValue[0], "UTF-8");
            String value = URLDecoder.decode(nameAndValue[1], "UTF-8");

            if("authenticationToken".equals(name)) {
              authToken = value;
            } else {
              Log.w(TAG, String.format("Found query parameter \"%s\" with value \"%s\"",
                  name, value));
            }
          }
          uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),uri.getPort(), uri.getPath(), null,uri.getFragment());
        } catch (Exception e) {
          Log.e(TAG, "Error extracting query information from uri.", e);
          setResult(RESULT_CANCELED);
          finish();
          return;
        }
      }

      // NOTE:  At the moment, we are always loading our certs from resources.  Opened JIRA SEMI-2147 to
      // add capability to load from the network endpoints dynamically.  Will need to refactor this code
      // to pull network access off the main thread though...
      KeyStore trustStore = SecurityUtils.createTrustStore(true);

      if(authToken == null) {
        boolean clearToken = getIntent().getBooleanExtra(EXTRA_CLEAR_TOKEN, false);
        if (!clearToken) {
          authToken = sharedPreferences.getString("AUTH_TOKEN", null);
        }
      }
      config = new WebSocketCloverDeviceConfiguration(uri, applicationId, trustStore, posName, serialNumber, authToken) {
        @Override
        public int getMaxMessageCharacters() {
          return 0;
        }

        @Override
        public void onPairingCode(final String pairingCode) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              // If we previously created a dialog and the pairing failed, reuse
              // the dialog previously created so that we don't get a stack of dialogs
              if (pairingCodeDialog != null) {
                pairingCodeDialog.setMessage("Enter pairing code: " + pairingCode);
              } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(POSActivity.this);
                builder.setTitle("Pairing Code");
                builder.setMessage("Enter pairing code: " + pairingCode);
                pairingCodeDialog = builder.create();
              }
              pairingCodeDialog.show();
            }
          });
        }

        @Override
        public void onPairingSuccess(String authToken) {
          Preferences.userNodeForPackage(POSActivity.class).put("AUTH_TOKEN", authToken);
          sharedPreferences.edit().putString("AUTH_TOKEN", authToken).apply();
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              if (pairingCodeDialog != null && pairingCodeDialog.isShowing()) {
                pairingCodeDialog.dismiss();
                pairingCodeDialog = null;
              }
            }
          });
        }
      };
    } else {
      finish();
      return;
    }

    cloverConnector = CloverConnectorFactory.createICloverConnector(config);
    initialize();

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      Log.d(TAG, "onActivityResult: requestCode: " + requestCode + " resultCode: " + requestCode + " Intent: " + data);
      if (requestCode == WS_ENDPOINT_ACTIVITY) {
          if (!usb) {
              initialize();
          }
      }
      if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
              && null != data) {
          // Get the Image from data

          Uri selectedImage = data.getData();
          String[] filePathColumn = {MediaStore.Images.Media.DATA};

          // Get the cursor
          Cursor cursor = getContentResolver().query(selectedImage,
                  filePathColumn, null, null, null);
          // Move to first row
          cursor.moveToFirst();

          int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
          String imgDecodableString = cursor.getString(columnIndex);
          printImage(imgDecodableString);
      }
  }

  public void initialize() {

    if (cloverConnector != null) {
      cloverConnector.dispose();
    }

    ICloverConnectorListener ccListener = new ICloverConnectorListener() {
      public void onDeviceDisconnected() {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(POSActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onDeviceDisconnected");
            ((TextView) findViewById(R.id.ConnectionStatusLabel)).setText("Disconnected");
          }
        });

      }

      public void onDeviceConnected() {

        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            showMessage("Connecting...", Toast.LENGTH_SHORT);
            Log.d(TAG, "onDeviceConnected");
            ((TextView) findViewById(R.id.ConnectionStatusLabel)).setText("Connecting");
          }
        });
      }

      public void onDeviceReady(final MerchantInfo merchantInfo) {
        Log.d(TAG, "onDeviceReady: MerchantInfo: " + merchantInfo.toString());
        runOnUiThread(new Runnable() {
          public void run() {
            if (pairingCodeDialog != null && pairingCodeDialog.isShowing()) {
              pairingCodeDialog.dismiss();
              pairingCodeDialog = null;
            }
            showMessage("Ready!", Toast.LENGTH_SHORT);
            ((TextView) findViewById(R.id.ConnectionStatusLabel)).setText(String.format("Connected: %s (%s)", merchantInfo.getDeviceInfo().getSerial(), merchantInfo.getMerchantName()));
          }
        });
        RetrievePrintersRequest rpr = new RetrievePrintersRequest();
        cloverConnector.retrievePrinters(rpr);
      }

      @Override
      public void onDeviceActivityStart(final CloverDeviceEvent deviceEvent) {
        Log.d(TAG, "onDeviceActivityStart: CloverDeviceEvent: " + deviceEvent);
        lastDeviceEvent = deviceEvent.getEventState();
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            ((TextView) findViewById(R.id.DeviceStatus)).setText(deviceEvent.getMessage());
            Toast.makeText(POSActivity.this, deviceEvent.getMessage(), Toast.LENGTH_SHORT).show();
            LinearLayout ll = findViewById(R.id.DeviceOptionsPanel);
            ll.removeAllViews();

            for (final InputOption io : deviceEvent.getInputOptions()) {
              Button btn = new Button(POSActivity.this);
              btn.setText(io.description);
              btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  cloverConnector.invokeInputOption(io);
                }
              });
              ll.addView(btn);
            }
          }
        });
      }

      @Override
      public void onDeviceActivityEnd(final CloverDeviceEvent deviceEvent) {
        Log.d(TAG, "onDeviceActivityEnd: CloverDeviceEvent: " + deviceEvent);
        if (deviceEvent.getEventState() == lastDeviceEvent) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              ((TextView) findViewById(R.id.DeviceStatus)).setText("");
              LinearLayout ll = findViewById(R.id.DeviceOptionsPanel);
              ll.removeAllViews();
            }
          });
        }
      }

      @Override
      public void onDeviceError(CloverDeviceErrorEvent deviceErrorEvent) {
        Log.d(TAG, "onDeviceError: CloverDeviceErrorEvent: " + deviceErrorEvent);
        showMessage("DeviceError: " + deviceErrorEvent.getMessage(), Toast.LENGTH_LONG);
      }

      @Override
      public void onAuthResponse(final AuthResponse response) {
        Log.d(TAG, "onAuthResponse: " + response.toString());
      }

      @Override
      public void onPreAuthResponse(final PreAuthResponse response) {
        Log.d(TAG, "onPreAuthResponse: " + response.toString());
      }

      @Override
      public void onRetrievePendingPaymentsResponse(RetrievePendingPaymentsResponse response) {
        Log.d(TAG, "onRetrievePendingPaymentsResponse: " + response.toString());
      }

        @Override
        public void onReadCardDataResponse(ReadCardDataResponse response) {

        }

        @Override
        public void onTipAdjustAuthResponse(TipAdjustAuthResponse response) {

        }

      @Override
      public void onCapturePreAuthResponse(final CapturePreAuthResponse response) {
        Log.d(TAG, "onCapturePreAuthResponse: " + response);

      }

      @Override
      public void onVerifySignatureRequest(final VerifySignatureRequest request) {

      }

      @Override
      public void onMessageFromActivity(MessageFromActivity message) {
        Log.d(TAG, "onMessageFromActivity: " + message.toString());
      }

      @Override
      public void onConfirmPaymentRequest(ConfirmPaymentRequest request) {
        Log.d(TAG, "onConfirmPaymentRequest: " + request.toString());
      }

      @Override
      public void onCloseoutResponse(CloseoutResponse response) {
        Log.d(TAG, "onCloseoutResponse: " + response.toString());
      }

      @Override
      public void onSaleResponse(final SaleResponse response) {
        Log.d(TAG, "onSaleResponse: " + response.toString());
      }

      @Override
      public void onManualRefundResponse(final ManualRefundResponse response) {
        Log.d(TAG, "onManualRefundResponse: " + response.toString());
      }

      @Override
      public void onRefundPaymentResponse(final RefundPaymentResponse response) {
        Log.d(TAG, "onRefundPaymentResponse: " + response.toString());
      }


      @Override
      public void onTipAdded(TipAddedMessage message) {
        Log.d(TAG, "onTipAdded: " + message.toString());
      }

      @Override
      public void onVoidPaymentResponse(VoidPaymentResponse response) {
        Log.d(TAG, "onVoidPaymentResponse: " + response.toString());
      }

      /**
       * Called in response to a void payment refund request
       *
       * @param response The response
       */
      @Override
      public void onVoidPaymentRefundResponse(final VoidPaymentRefundResponse response) {
        Log.d(TAG, "onVoidPaymentRefundResponse: " + response.toString());
      }

      @Override
      public void onVaultCardResponse(final VaultCardResponse response) {
        Log.d(TAG, "onVaultCardResponse" + response.toString());
      }

      @Override
      public void onPrintJobStatusResponse(PrintJobStatusResponse response) {
        Log.d(TAG, "onPrintJobStatusResponse: " + response.toString());
        showMessage("PrintJobStatus: " + response.getStatus(), Toast.LENGTH_SHORT);
      }

      @Override
      public void onRetrievePrintersResponse(RetrievePrintersResponse response) {
        Log.d(TAG, "onRetrievePrintersResponse: " + response.toString());
        printers = response.getPrinters();
        if(printers != null){
          printer = printers.get(0);
        }
      }

      @Override
      public void onPrintManualRefundReceipt(PrintManualRefundReceiptMessage pcm) {
        Log.d(TAG, "onPrintManualRefundReceipt: " + pcm.toString());
        showMessage("Print Request for ManualRefund", Toast.LENGTH_SHORT);
      }

      @Override
      public void onPrintManualRefundDeclineReceipt(PrintManualRefundDeclineReceiptMessage pcdrm) {
        Log.d(TAG, "onPrintManualRefundDeclineReceipt: " + pcdrm.toString());
        showMessage("Print Request for Declined ManualRefund", Toast.LENGTH_SHORT);
      }

      @Override
      public void onPrintPaymentReceipt(PrintPaymentReceiptMessage pprm) {
        Log.d(TAG, "onPrintPaymentReceipt: " + pprm.toString());
        showMessage("Print Request for Payment Receipt", Toast.LENGTH_SHORT);
      }

      @Override
      public void onPrintPaymentDeclineReceipt(PrintPaymentDeclineReceiptMessage ppdrm) {
        Log.d(TAG, "onPrintPaymentDeclineReceipt: " + ppdrm.toString());
        showMessage("Print Request for DeclinedPayment Receipt", Toast.LENGTH_SHORT);
      }

      @Override
      public void onPrintPaymentMerchantCopyReceipt(PrintPaymentMerchantCopyReceiptMessage ppmcrm) {
        Log.d(TAG, "onPrintPaymentMerchantCopyReceipt: " + ppmcrm.toString());
        showMessage("Print Request for MerchantCopy of a Payment Receipt", Toast.LENGTH_SHORT);
      }

      @Override
      public void onPrintRefundPaymentReceipt(PrintRefundPaymentReceiptMessage pprrm) {
        Log.d(TAG, "onPrintRefundPaymentReceipt: " + pprrm.toString());
        showMessage("Print Request for RefundPayment Receipt", Toast.LENGTH_SHORT);
      }

      @Override
      public void onCustomActivityResponse(CustomActivityResponse response) {
        Log.d(TAG, "onCustomActivityResponse: " + response.toString());
      }

      @Override
      public void onRetrieveDeviceStatusResponse(RetrieveDeviceStatusResponse response) {
        Log.d(TAG, "onRetrieveDeviceStatusResponse: " + response.toString());
        showPopupMessage("Device Status", new String[]{response.isSuccess() ? "Success!" : "Failed!",
            "State: " + response.getState(), "ExternalActivityId: " + response.getData().toString(), "Reason: " + response.getReason() }, false);
      }

        @Override
        public void onInvalidStateTransitionResponse(InvalidStateTransitionResponse response) {

        }

        @Override
      public void onResetDeviceResponse(ResetDeviceResponse response) {
        Log.d(TAG, "onResetDeviceResponse: " + response.toString());
        showPopupMessage("Reset Device", new String[]{response.isSuccess() ? "Success!" : "Failed!", "State: " + response.getState(), "Reason: " + response.getReason()}, false);
      }

      @Override
      public void onRetrievePaymentResponse(RetrievePaymentResponse response) {
        Log.d(TAG, "onRetrievePaymentResponse: " + response.toString());
        if (response.isSuccess()) {
          showPopupMessage("Retrieve Payment", new String[]{"Retrieve Payment successful for Payment ID: " + response.getExternalPaymentId(),
              " QueryStatus: " + response.getQueryStatus(),
              " Payment: " + response.getPayment(),
              " reason: " + response.getReason()}, false);
        } else {
          showPopupMessage(null, new String[]{"Retrieve Payment error: " + response.getResult()}, false);
        }
      }

        @Override
        public void onCustomerProvidedData(CustomerProvidedDataEvent event) {

        }

        @Override
      public void onDisplayReceiptOptionsResponse(DisplayReceiptOptionsResponse response) {
        Log.d(TAG, "onDisplayReceiptOptionsResponse: " + response.toString());
        showMessage("Display Receipt Options", Toast.LENGTH_SHORT);
      }
    };
    cloverConnector.addCloverConnectorListener(ccListener);
    cloverConnector.initializeConnection();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (cloverConnector != null) {
      cloverConnector.dispose();
    }
  }

  private void showMessage(final String msg, final int duration) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(POSActivity.this, msg, duration).show();
      }
    });
  }

  protected void showPopupMessage (final String title, final String[] content, final boolean monospace) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        FragmentManager fm = getFragmentManager();
        PopupMessageFragment popupMessageFragment = PopupMessageFragment.newInstance(title, content, monospace);
        popupMessageFragment.show(fm, "fragment_popup_message");
      }
    });
  }

  private int getNextPrintRequestId(){
    return ++printRequestId;
  }

  public void printTextClick(List<String> lines) {
    if(printer != null){
      PrintRequest pr = new PrintRequest(lines);
      lastPrintRequestId = String.valueOf(getNextPrintRequestId());
      pr.setPrintRequestId(lastPrintRequestId);
      pr = new PrintRequest(lines, lastPrintRequestId, printer.getId());
      Log.d(TAG, "PrintRequest - Print Text: " + pr.toString());
      cloverConnector.print(pr);
    }
    else {
      Log.e(TAG,"Printer is null");
    }
  }

//  public void printImageURLClick(View view) {
//    String URL = ((TextView) findViewById(R.id.PrintImageURLText)).getText().toString();
//    if(printer != null){
//      PrintRequest pr = new PrintRequest(URL);
//      lastPrintRequestId = String.valueOf(getNextPrintRequestId());
//      pr.setPrintRequestId(lastPrintRequestId);
//      pr = new PrintRequest(URL, lastPrintRequestId, printer.getId());
//      Log.d(TAG, "PrintRequest - Print Image URL: " + pr.toString());
//      cloverConnector.print(pr);
//    }
//  }
//
//  public void showMessageClick(View view) {
//    cloverConnector.showMessage(((TextView) findViewById(R.id.ShowMessageText)).getText().toString());
//  }
//
//  public void showWelcomeMessageClick(View view) {
//    cloverConnector.showWelcomeScreen();
//  }
//
//  public void showThankYouClick(View view) {
//    cloverConnector.showThankYouScreen();
//  }
//
//  public void onOpenCashDrawerClick(View view) {
//    OpenCashDrawerRequest ocdr = new OpenCashDrawerRequest("Test");
//    if(printer != null) {
//      ocdr.setDeviceId(printer.getId());
//    }
//    Log.d(TAG, "OpenCashDrawerRequest: " + ocdr.toString());
//    cloverConnector.openCashDrawer(ocdr);
//  }
//
//  public void onClickCloseout(View view) {
//    CloseoutRequest request = new CloseoutRequest();
//    request.setAllowOpenTabs(false);
//    request.setBatchId(null);
//    Log.d(TAG, "CloseoutRequest: " + request.toString());
//    cloverConnector.closeout(request);
//  }
//
//
//  public void printImageClick(View view) {
//    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//    startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
//  }

  public void setPrinter(Printer printer){
    this.printer = printer;
  }

  public void printImage(String imgDecodableString){
    Bitmap bitmap = BitmapFactory.decodeFile(imgDecodableString);
    if(this.printer != null){
      PrintRequest pr = new PrintRequest(bitmap);
      lastPrintRequestId = String.valueOf(getNextPrintRequestId());
      pr.setPrintRequestId(lastPrintRequestId);
      pr = new PrintRequest(bitmap, lastPrintRequestId, printer.getId());
      Log.d(TAG, "PrintRequest - Print Image: " + pr.toString());
      cloverConnector.print(pr);
    }
  }

  public void onResetDeviceClick(View view) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        new AlertDialog.Builder(POSActivity.this)
            .setTitle("Reset Device")
            .setMessage("Are you sure you want to reset the device? Warning: You may lose any pending transaction information.")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "Resetting Device");
                cloverConnector.resetDevice();
              }
            })
            .setNegativeButton("No", null)
            .show();
      }
    });
  }

  public void onGetDeviceStatusClick(View view) {
    RetrieveDeviceStatusRequest request = new RetrieveDeviceStatusRequest(false);
    Log.d(TAG, "RetrieveDeviceStatusRequest: " + request.toString());
    cloverConnector.retrieveDeviceStatus(request);
  }

  public void onGetDeviceStatusCBClick(View view) {
    RetrieveDeviceStatusRequest request = new RetrieveDeviceStatusRequest(true);
    Log.d(TAG, "RetrieveDeviceStatusRequest: " + request.toString());
    cloverConnector.retrieveDeviceStatus(new RetrieveDeviceStatusRequest(true));
  }
}
