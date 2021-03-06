//package com.beinfinity.activity;
///*
// * Copyright (C) 2011-2013 Advanced Card Systems Ltd. All Rights Reserved.
// *
// * This software is the confidential and proprietary information of Advanced
// * Card Systems Ltd. ("Confidential Information").  You shall not disclose such
// * Confidential Information and shall use it only in accordance with the terms
// * of the license agreement you entered into with ACS.
// */
//
//import android.app.Activity;
//import android.app.PendingIntent;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.hardware.usb.UsbDevice;
//import android.hardware.usb.UsbManager;
//import android.nfc.NfcAdapter;
//import android.nfc.tech.MifareClassic;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.text.method.ScrollingMovementMethod;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.acs.smartcard.ReaderException;
//import com.beinfinity.R;
//
//import com.acs.smartcard.Features;
//import com.acs.smartcard.Reader;
//import com.beinfinity.database.DbContract;
//import com.beinfinity.database.DbHelper;
//import com.beinfinity.tools.Http;
//import com.beinfinity.tools.ProgressView;
//
//import java.io.IOException;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.HashMap;
//
//
///**
// * Test program for ACS smart card readers.
// *
// * @author Godfrey Chung
// * @version 1.1.1, 16 Apr 2013
// */
//public class MainActivity extends Activity {
//
//    private static final String URL_NAME = "urlname";
//
//    private UserAuthTask mAuthTask = null;
//    private ProgressView progressView;
//    private int shortAnimTime;
//
//    private PendingIntent pendingIntent;
//    private IntentFilter[] intentFiltersArray;
//    private String[][] techListsArray;
//    ;
//
//    private View mProgressView;
//    private View mAccueilFormView;
//    private String reason;
//    private String displayName;
//    private HashMap<String, String> parameters;
//    private String idCard;
//    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
//
//    private static final String[] stateStrings = {"Unknown", "Absent",
//            "Present", "Swallowed", "Powered", "Negotiable", "Specific"};
//
//    private UsbManager mManager;
//    private Reader mReader;
//    private PendingIntent mPermissionIntent;
//
//    private static final int MAX_LINES = 25;
//    private TextView mResponseTextView;
//
//    private Features mFeatures = new Features();
//    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//
//        public void onReceive(Context context, Intent intent) {
//
//            String action = intent.getAction();
//
//            if (ACTION_USB_PERMISSION.equals(action)) {
//
//                synchronized (this) {
//
//                    UsbDevice device = intent
//                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);
//
//                    if (intent.getBooleanExtra(
//                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
//
//                        if (device != null) {
//
//                            // Open reader
//                            logMsg("Opening reader: " + device.getDeviceName()
//                                    + "...");
//                            new OpenTask().execute(device);
//                        }
//
//                    } else {
//
//                        logMsg("Permission denied for device "
//                                + device.getDeviceName());
//                    }
//                }
//
//            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
//
//                synchronized (this) {
//                    UsbDevice device = intent
//                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);
//
//                    if (device != null && device.equals(mReader.getDevice())) {
//
//                        // Close reader
//                        logMsg("Closing reader...");
//                        new CloseTask().execute();
//                    }
//                }
//            }
//        }
//    };
//
//    private class OpenTask extends AsyncTask<UsbDevice, Void, Exception> {
//
//        @Override
//        protected Exception doInBackground(UsbDevice... params) {
//
//            Exception result = null;
//
//            try {
//
//                mReader.open(params[0]);
//
//            } catch (Exception e) {
//
//                result = e;
//            }
//
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Exception result) {
//
//            if (result != null) {
//
//                logMsg(result.toString());
//
//            } else {
//                logMsg("Reader name: " + mReader.getReaderName());
//
//                int numSlots = mReader.getNumSlots();
//                logMsg("Number of slots: " + numSlots);
//
//
//                // Remove all control codes
//                mFeatures.clear();
//            }
//        }
//    }
//
//    private class CloseTask extends AsyncTask<Void, Void, Void> {
//
//        @Override
//        protected Void doInBackground(Void... params) {
//
//            mReader.close();
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//        }
//
//    }
//
//    /**
//     * Called when the activity is first created.
//     */
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//
//        super.onCreate(savedInstanceState);
//        // setContentView(R.layout.activity_main);
//        setContentView(R.layout.activity_accueil);
//        this.parameters = new HashMap<>();
//        this.shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//        // Récupération des éléments de la vue
//        mProgressView = findViewById(R.id.accueil_progress);
//        mAccueilFormView = findViewById(R.id.accueil_form);
//        mResponseTextView = (TextView) findViewById(R.id.main_text_view_response);
//
//
//        progressView = new ProgressView(mAccueilFormView, mProgressView);
//
//        this.GetDataFromDb();
//
//        // Get USB manager
//        mManager = (UsbManager) getSystemService(Context.USB_SERVICE);
//
//        // Initialize reader
//        mReader = new Reader(mManager);
//        mReader.setOnStateChangeListener(new Reader.OnStateChangeListener() {
//
//            @Override
//            public void onStateChange(int slotNum, int prevState, int currState) {
//
//                if (prevState < Reader.CARD_UNKNOWN
//                        || prevState > Reader.CARD_SPECIFIC) {
//                    prevState = Reader.CARD_UNKNOWN;
//                }
//
//                if (currState < Reader.CARD_UNKNOWN
//                        || currState > Reader.CARD_SPECIFIC) {
//                    currState = Reader.CARD_UNKNOWN;
//                }
//
//                // Create output string
//                final String outputString = "Slot " + slotNum + ": "
//                        + stateStrings[prevState] + " -> "
//                        + stateStrings[currState];
//
//                // Set parameters
//                new PowerTask().execute();
//
//                // Show output
//                runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        logMsg("attrb : " + mReader.getAtr(0));
//                        logMsg("state : " + mReader.getState(0));
//                        logMsg(outputString);
//                    }
//                });
//            }
//        });
//
//        // Register receiver for USB permission
//        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
//                ACTION_USB_PERMISSION), 0);
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(ACTION_USB_PERMISSION);
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//        registerReceiver(mReceiver, filter);
//
//        // Initialize response text view
////        mResponseTextView = (TextView) findViewById(R.id.main_text_view_response);
////        mResponseTextView.setMovementMethod(new ScrollingMovementMethod());
////        mResponseTextView.setMaxLines(MAX_LINES);
////        mResponseTextView.setText("");
//
//        String deviceName = "";
//        for (UsbDevice device : mManager.getDeviceList().values()) {
//            if (mReader.isSupported(device)) {
//                deviceName = device.getDeviceName();
//            }
//        }
//
//        if (deviceName != null) {
//
//            // For each device
//            for (UsbDevice device : mManager.getDeviceList().values()) {
//
//                // If device name is found
//                if (deviceName.equals(device.getDeviceName())) {
//
//                    // Request permission
//                    mManager.requestPermission(device,
//                            mPermissionIntent);
//                    break;
//                }
//            }
//        }
//
//        // Hide input window
//        getWindow().setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//    }
//
//    public void checkID(View view) {
//        this.checkID("66625");
//    }
//
//    private class PowerResult {
//
//        public byte[] atr;
//        public Exception e;
//        public String result;
//    }
//
//    private class PowerTask extends AsyncTask<Void, Void, PowerResult> {
//
//        @Override
//        protected PowerResult doInBackground(Void... params) {
//
//
//            PowerResult result = new PowerResult();
//            try {
//
//                logMsg("state before power :" + mReader.getState(0));
//                result.atr = mReader.power(0, 2);
//
//            } catch (Exception e) {
//                result.e = e;
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(PowerResult result) {
//
//            if (result.e != null) {
//
//                logMsg(result.e.toString());
//
//            } else {
//
//                // Show ATR
//                if (result.atr != null) {
//                    logMsg("state after power :" + mReader.getState(0));
//                    logMsg("Power set");
//                    new SetProtocolTask().execute();
//
//                } else {
//
//                    logMsg("ATR: None");
//                }
//            }
//        }
//    }
//
//
//    private class SetProtocolResult {
//
//        public int activeProtocol;
//        public Exception e;
//    }
//
//    private class SetProtocolTask extends
//            AsyncTask<Void, Void, SetProtocolResult> {
//
//        @Override
//        protected SetProtocolResult doInBackground(Void... params) {
//
//            SetProtocolResult result = new SetProtocolResult();
//
//            try {
//
//                result.activeProtocol = mReader.setProtocol(0,
//                        Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
//
//            } catch (Exception e) {
//
//                result.e = e;
//            }
//
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(SetProtocolResult result) {
//
//            if (result.e != null) {
//
//                logMsg(result.e.toString());
//
//            } else {
//
//                String activeProtocolString = "Active Protocol: ";
//
//                switch (result.activeProtocol) {
//
//                    case Reader.PROTOCOL_T0:
//                        activeProtocolString += "T=0";
//                        break;
//
//                    case Reader.PROTOCOL_T1:
//                        activeProtocolString += "T=1";
//                        break;
//
//                    default:
//                        activeProtocolString += "Unknown";
//                        break;
//                }
//
//                // Show active protocol
//                logMsg(activeProtocolString);
//                new Transmit().execute();
//            }
//        }
//    }
//
//    private class Transmit extends AsyncTask<Void, Void, String> {
//
//        @Override
//        protected String doInBackground(Void... params) {
//
//            String result = "";
//            byte[] command = {(byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x04};
//            byte[] command2 = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, (byte) 0x10, (byte) 0x10};
//            byte[] response = new byte[10000];
//            byte[] response2 = new byte[1000];
//            int responseLength = 0;
//            try {
//                //responseLength = mReader.transmit(0, command, command.length, response, response.length);
//                //result = "response length : " + responseLength + "response : " + response + "attrb strg : " + new String(response);
//                //responseLength = 0;
//                responseLength = mReader.transmit(0, command2, command2.length, response2, response2.length);
//                result += "response length : " + responseLength + "response2 : " + response2 + "attrb strg : " + new String(response2);
//            } catch (ReaderException e) {
//                // TODO Auto-generated catch block
//                logMsg("exception: " + e);
//            } finally {
//                return result;
//            }
//        }
//
//
//        @Override
//        protected void onPostExecute(String result) {
//            logMsg("Transmit result" + result);
//        }
//    }
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        logMsg("On new intent");
//        super.onNewIntent(intent);
//
////        if (intent != null && NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
////            Parcelable[] rawMessages =
////                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
////            if (rawMessages != null) {
////                NdefMessage[] messages = new NdefMessage[rawMessages.length];
////                for (int i = 0; i < rawMessages.length; i++) {
////                    messages[i] = (NdefMessage) rawMessages[i];
////                }
////
////                if (messages.length > 0) {
////                    // TODO: Faire vérification  suppl si nécessaire
////                    String message = new String(messages[0].getRecords()[0].getPayload());
////                    this.checkID(message);
////                }
////            }
////        }
//    }
//
//    @Override
//    protected void onDestroy() {
//
//        // Close reader
//        mReader.close();
//
//        // Unregister receiver
//        unregisterReceiver(mReceiver);
//
//        super.onDestroy();
//    }
//
//    /**
//     * Logs the message.
//     *
//     * @param msg the message.
//     */
//    private void logMsg(String msg) {
//
//        DateFormat dateFormat = new SimpleDateFormat("[dd-MM-yyyy HH:mm:ss]: ");
//        Date date = new Date();
//        String oldMsg = mResponseTextView.getText().toString();
//
//        mResponseTextView
//                .setText(oldMsg + "\n" + dateFormat.format(date) + msg);
//
//        if (mResponseTextView.getLineCount() > MAX_LINES) {
//            mResponseTextView.scrollTo(0,
//                    (mResponseTextView.getLineCount() - MAX_LINES)
//                            * mResponseTextView.getLineHeight());
//        }
//    }
//
//    public void goToParameters(View view) {
//        Intent intent = new Intent(this, LoginActivity.class);
//        startActivity(intent);
//    }
//
//    private void checkID(String id) {
//        progressView.ShowProgress(true, shortAnimTime);
//        mAuthTask = new UserAuthTask(id);
//        mAuthTask.execute((Void) null);
//    }
//
//    private void GoToBooking() {
//        Intent intent = new Intent(this, BookingActivity.class);
//        intent.putExtra(getString(R.string.displayName), this.displayName);
//        intent.putExtra(getString(R.string.idCard), this.idCard);
//        startActivity(intent);
//    }
//
//    private void GetDataFromDb() {
//        DbHelper dbHelper = new DbHelper(getBaseContext());
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
//
//        // LECTURE DES DONNEES DE PARAMETRAGE
//        Cursor c = db.query(
//                DbContract.ParameterEntry.TABLE_NAME,                     // The table to query
//                DbContract.ParameterEntry.ProjectionParameter,                               // The columns to return
//                null,                                // The columns for the WHERE clause
//                null,                            // The values for the WHERE clause
//                null,                                     // don't group the rows
//                null,                                     // don't filter by row groups
//                null                                 // The sort order
//        );
//        c.moveToFirst();
//        while (!c.isAfterLast()) {
//            String name = c.getString(c.getColumnIndexOrThrow(DbContract.ParameterEntry.COLUMN_NAME_TITLE));
//            String content = c.getString(c.getColumnIndexOrThrow(DbContract.ParameterEntry.COLUMN_NAME_CONTENT));
//            parameters.put(name, content);
//            c.moveToNext();
//        }
//        db.close();
//    }
//
//    private Boolean checkIDCard(String idCard) {
//        String url = this.parameters.get(URL_NAME);
//        String response = null;
//        try {
//            this.idCard = idCard.substring(3);
//            response = Http.SendGetRequest(url + "login.php?id=" + this.idCard);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        if (!response.contains("expired") && !response.contains("unregistered")) {
//            this.displayName = response;
//            return true;
//        } else {
//            this.reason = response;
//            return false;
//        }
//    }
//
//    public class UserAuthTask extends AsyncTask<Void, Void, Boolean> {
//
//        String idCard;
//
//        UserAuthTask(String idCard) {
//            this.idCard = idCard;
//        }
//
//        @Override
//        protected Boolean doInBackground(Void... params) {
//            Boolean isOk = false;
//            try {
//                Thread.sleep(2000);
//                isOk = checkIDCard(this.idCard);
//            } catch (InterruptedException e) {
//                return false;
//            }
//            return isOk;
//        }
//
//        @Override
//        protected void onPostExecute(final Boolean success) {
//            mAuthTask = null;
//            progressView.ShowProgress(false, shortAnimTime);
//
//            if (success) {
//                GoToBooking();
//            } else {
//                if (reason.contains("unregistered")) {
//                    Toast.makeText(getApplicationContext(), getString(R.string.accueil_toast_unregistered), Toast.LENGTH_LONG).show();
//                } else if (reason.contains("expired")) {
//                    Toast.makeText(getApplicationContext(), getString(R.string.accueil_toast_expired), Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(getApplicationContext(), getString(R.string.accueil_toast_echec), Toast.LENGTH_LONG).show();
//                }
//            }
//        }
//
//        @Override
//        protected void onCancelled() {
//            mAuthTask = null;
//            progressView.ShowProgress(false, shortAnimTime);
//        }
//    }
//}
//
