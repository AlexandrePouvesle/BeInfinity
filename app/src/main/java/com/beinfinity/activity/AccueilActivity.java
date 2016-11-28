package com.beinfinity.activity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.acs.smartcard.Features;
import com.acs.smartcard.PinModify;
import com.acs.smartcard.PinVerify;
import com.acs.smartcard.ReadKeyOption;
import com.acs.smartcard.Reader;
import com.beinfinity.R;

import com.beinfinity.database.DbContract;
import com.beinfinity.database.DbHelper;
import com.beinfinity.tools.Http;
import com.beinfinity.tools.ProgressView;

import java.io.Console;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class AccueilActivity extends AppCompatActivity {

    private static final String URL_NAME = "urlname";

    private UserAuthTask mAuthTask = null;
    private ProgressView progressView;
    private int shortAnimTime;

    private View mProgressView;
    private View mAccueilFormView;

    private PendingIntent pendingIntent;
    private NfcAdapter mNfcAdapter;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private String reason;
    private String displayName;
    private HashMap<String, String> parameters;
    private String idCard;


    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private UsbManager mManager;
    private Reader mReader;
    private PendingIntent mPermissionIntent;
    private Features mFeatures = new Features();
    private PinVerify mPinVerify = new PinVerify();
    private PinModify mPinModify = new PinModify();
    private ReadKeyOption mReadKeyOption = new ReadKeyOption();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);

        this.parameters = new HashMap<>();
        this.shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        // Récupération des éléments de la vue
        mProgressView = findViewById(R.id.accueil_progress);
        mAccueilFormView = findViewById(R.id.accueil_form);

        progressView = new ProgressView(mAccueilFormView, mProgressView);

//        this.mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
//
//        this.pendingIntent = PendingIntent.getActivity(
//                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
//
//        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
//        try {
//            ndef.addDataType("*/*");
//        } catch (IntentFilter.MalformedMimeTypeException e) {
//            throw new RuntimeException("fail", e);
//        }
//
//        this.intentFiltersArray = new IntentFilter[]{ndef,};
//        this.techListsArray = new String[][]{new String[]{
//                MifareClassic.class.getName()
//        }};

        this.GetDataFromDb();

        this.InitUSB();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Toast.makeText(getApplicationContext(), "Intent action : " + intent.getAction(), Toast.LENGTH_LONG).show();
        super.onNewIntent(intent);

//        if (intent != null && NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
//            Parcelable[] rawMessages =
//                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
//            if (rawMessages != null) {
//                NdefMessage[] messages = new NdefMessage[rawMessages.length];
//                for (int i = 0; i < rawMessages.length; i++) {
//                    messages[i] = (NdefMessage) rawMessages[i];
//                }
//
//                if (messages.length > 0) {
//                    // TODO: Faire vérification  suppl si nécessaire
//                    String message = new String(messages[0].getRecords()[0].getPayload());
//                    this.checkID(message);
//                }
//            }
//        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        this.mNfcAdapter.enableForegroundDispatch(this, this.pendingIntent, this.intentFiltersArray, this.techListsArray);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        this.mNfcAdapter.disableForegroundDispatch(this);
//    }

    @Override
    protected void onDestroy() {

        // Close reader
        mReader.close();

        super.onDestroy();
    }

    public void goToParameters(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void checkID(View view) {
        // this.checkID("1");


        String deviceName = "";
        for (UsbDevice device : mManager.getDeviceList().values()) {
            if (mReader.isSupported(device)) {
                deviceName = device.getDeviceName();
            }
        }

        if (deviceName != null) {

            // For each device
            for (UsbDevice device : mManager.getDeviceList().values()) {

                // If device name is found
                if (deviceName.equals(device.getDeviceName())) {

                    // Request permission
                    mManager.requestPermission(device,
                            mPermissionIntent);
                    break;
                }
            }
        }
    }

    private void checkID(String id) {
        progressView.ShowProgress(true, shortAnimTime);
        mAuthTask = new UserAuthTask(id);
        mAuthTask.execute((Void) null);
    }

    private void GoToBooking() {
        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra(getString(R.string.displayName), this.displayName);
        intent.putExtra(getString(R.string.idCard), this.idCard);
        startActivity(intent);
    }

    private void GetDataFromDb() {
        DbHelper dbHelper = new DbHelper(getBaseContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // LECTURE DES DONNEES DE PARAMETRAGE
        Cursor c = db.query(
                DbContract.ParameterEntry.TABLE_NAME,                     // The table to query
                DbContract.ParameterEntry.ProjectionParameter,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String name = c.getString(c.getColumnIndexOrThrow(DbContract.ParameterEntry.COLUMN_NAME_TITLE));
            String content = c.getString(c.getColumnIndexOrThrow(DbContract.ParameterEntry.COLUMN_NAME_CONTENT));
            parameters.put(name, content);
            c.moveToNext();
        }
        db.close();
    }

    private Boolean checkIDCard(String idCard) {
        String url = this.parameters.get(URL_NAME);
        String response = null;
        try {
            this.idCard = idCard.substring(3);
            response = Http.SendGetRequest(url + "login.php?id=" + this.idCard);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!response.contains("expired") && !response.contains("unregistered")) {
            this.displayName = response;
            return true;
        } else {
            this.reason = response;
            return false;
        }
    }

    private void InitUSB() {

        // Get USB manager
        mManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        // Initialize reader
        mReader = new Reader(mManager);
        mReader.setOnStateChangeListener(new Reader.OnStateChangeListener() {

            @Override
            public void onStateChange(int slotNum, int prevState, int currState) {

                if (prevState < Reader.CARD_UNKNOWN
                        || prevState > Reader.CARD_SPECIFIC) {
                    prevState = Reader.CARD_UNKNOWN;
                }

                if (currState < Reader.CARD_UNKNOWN
                        || currState > Reader.CARD_SPECIFIC) {
                    currState = Reader.CARD_UNKNOWN;
                }
                Toast.makeText(getApplicationContext(), "State - pre: " + prevState + " curr: " + currState, Toast.LENGTH_LONG).show();

                // Create output string
                //final String outputString = "Slot " + slotNum + ": "
                //       + stateStrings[prevState] + " -> "
                //       + stateStrings[currState];

                // Show output
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //logMsg(outputString);
                        Toast.makeText(getApplicationContext(), "RUN THREAD", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        // Register receiver for USB permission
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        // PIN verification command (ACOS3)
        byte[] pinVerifyData = {(byte) 0x80, 0x20, 0x06, 0x00, 0x08,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

        // Initialize PIN verify structure (ACOS3)
        mPinVerify.setTimeOut(0);
        mPinVerify.setTimeOut2(0);
        mPinVerify.setFormatString(0);
        mPinVerify.setPinBlockString(0x08);
        mPinVerify.setPinLengthFormat(0);
        mPinVerify.setPinMaxExtraDigit(0x0408);
        mPinVerify.setEntryValidationCondition(0x03);
        mPinVerify.setNumberMessage(0x01);
        mPinVerify.setLangId(0x0409);
        mPinVerify.setMsgIndex(0);
        mPinVerify.setTeoPrologue(0, 0);
        mPinVerify.setTeoPrologue(1, 0);
        mPinVerify.setTeoPrologue(2, 0);
        mPinVerify.setData(pinVerifyData, pinVerifyData.length);

        // PIN modification command (ACOS3)
        byte[] pinModifyData = {(byte) 0x80, 0x24, 0x00, 0x00, 0x08,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

        // Initialize PIN modify structure (ACOS3)
        mPinModify.setTimeOut(0);
        mPinModify.setTimeOut2(0);
        mPinModify.setFormatString(0);
        mPinModify.setPinBlockString(0x08);
        mPinModify.setPinLengthFormat(0);
        mPinModify.setInsertionOffsetOld(0);
        mPinModify.setInsertionOffsetNew(0);
        mPinModify.setPinMaxExtraDigit(0x0408);
        mPinModify.setConfirmPin(0x01);
        mPinModify.setEntryValidationCondition(0x03);
        mPinModify.setNumberMessage(0x02);
        mPinModify.setLangId(0x0409);
        mPinModify.setMsgIndex1(0);
        mPinModify.setMsgIndex2(0x01);
        mPinModify.setMsgIndex3(0);
        mPinModify.setTeoPrologue(0, 0);
        mPinModify.setTeoPrologue(1, 0);
        mPinModify.setTeoPrologue(2, 0);
        mPinModify.setData(pinModifyData, pinModifyData.length);

        // Initialize read key option
        mReadKeyOption.setTimeOut(0);
        mReadKeyOption.setPinMaxExtraDigit(0x0408);
        mReadKeyOption.setKeyReturnCondition(0x01);
        mReadKeyOption.setEchoLcdStartPosition(0);
        mReadKeyOption.setEchoLcdMode(0x01);
    }

    public class UserAuthTask extends AsyncTask<Void, Void, Boolean> {

        String idCard;

        UserAuthTask(String idCard) {
            this.idCard = idCard;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean isOk = false;
            try {
                Thread.sleep(2000);
                isOk = checkIDCard(this.idCard);
            } catch (InterruptedException e) {
                return false;
            }
            return isOk;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            progressView.ShowProgress(false, shortAnimTime);

            if (success) {
                GoToBooking();
            } else {
                if (reason.contains("unregistered")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.accueil_toast_unregistered), Toast.LENGTH_LONG).show();
                } else if (reason.contains("expired")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.accueil_toast_expired), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.accueil_toast_echec), Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            progressView.ShowProgress(false, shortAnimTime);
        }
    }
}
