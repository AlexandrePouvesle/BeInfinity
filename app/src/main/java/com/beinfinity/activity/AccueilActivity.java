package com.beinfinity.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.beinfinity.R;
import com.beinfinity.database.DbContract;
import com.beinfinity.database.DbHelper;
import com.beinfinity.tools.Http;
import com.beinfinity.tools.ProgressView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import com.skjolberg.nfc.NfcReader;
import com.skjolberg.nfc.NfcService;
import com.skjolberg.nfc.NfcTag;
import com.skjolberg.nfc.acs.Acr1222LReader;
import com.skjolberg.nfc.acs.Acr122UReader;
import com.skjolberg.nfc.acs.Acr1283LReader;
import com.skjolberg.nfc.acs.AcrFont;
import com.skjolberg.nfc.acs.AcrPICC;
import com.skjolberg.nfc.acs.AcrReader;
//import com.skjolberg.nfc.desfire.DesfireReader;
//import com.skjolberg.nfc.desfire.VersionInfo;
//import com.skjolberg.nfc.util.CommandAPDU;
//import com.skjolberg.nfc.util.ResponseAPDU;
import com.skjolberg.nfc.util.activity.NfcExternalDetectorActivity;

import org.ndeftools.Message;
import org.ndeftools.Record;
import org.ndeftools.UnsupportedRecord;

public class AccueilActivity extends NfcExternalDetectorActivity {

    private static final String URL_NAME = "urlname";

    protected Boolean tag = null;
    protected Boolean reader = null;
    protected Boolean service = null;

    // pour l'écriture
    private NdefFormatable ndefFormatable;
    private Ndef ndef;

    private UserAuthTask mAuthTask = null;
    private ProgressView progressView;
    private int shortAnimTime;

    private View mProgressView;
    private View mAccueilFormView;
    private String reason;
    private String displayName;
    private HashMap<String, String> parameters;
    private String idCard;
    private String urlImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);

        this.parameters = new HashMap<>();
        this.shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        // Récupération des éléments de la vue
        this.mProgressView = findViewById(R.id.accueil_progress);
        this.mAccueilFormView = findViewById(R.id.accueil_form);

        this.progressView = new ProgressView(mAccueilFormView, mProgressView);

        this.GetDataFromDb();

        initializeExternalNfc();

        setDetecting(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onNfcStateEnabled() {
        toast("Native NFC available and enabled.");
    }

    @Override
    protected void onNfcStateDisabled() {
        toast("Native NFC available and disabled.");
    }

    @Override
    protected void onNfcStateChange(boolean enabled) {
        if (enabled) {
            toast("Native NFC available and enabled.");
        } else {
            toast("Native NFC available and disabled.");
        }
    }

    @Override
    protected void onNfcIntentDetected(Intent intent, String action) {
        this.tag = true;
        toast("NfcIntentDetected.");
        if (intent.hasExtra(NfcAdapter.EXTRA_ID)) {
            byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            this.checkID(toHexString(id));
            toast("ID : " + toHexString(id));
        }
/*
        if (intent.hasExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)) {

            Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (messages != null) {

                NdefMessage[] ndefMessages = new NdefMessage[messages.length];
                for (int i = 0; i < messages.length; i++) {
                    ndefMessages[i] = (NdefMessage) messages[i];
                }

                // read as much as possible
                Message message = new Message();
                for (int i = 0; i < messages.length; i++) {
                    NdefMessage ndefMessage = (NdefMessage) messages[i];

                    for (NdefRecord ndefRecord : ndefMessage.getRecords()) {

                        Record record;
                        try {
                            record = Record.parse(ndefRecord);

                        } catch (FormatException e) {
                            // if the record is unsupported or corrupted, keep as unsupported record
                            record = UnsupportedRecord.parse(ndefRecord);
                        }

                        message.add(record);
                    }
                }
                // ICI CONTENU DU TAG
                // message;
                toast("Message taille du message : " + message.size());
                for (int i = 0; i < message.getNdefMessage().getRecords().length; i++) {
                    toast("Contenu message : " + new String(message.getNdefMessage().getRecords()[i].getPayload()));

                }
                // message.getNdefMessage().getRecords()
            }
        }

        else

        {
            // TAG VIDE
            toast("No NDEF message");
        }

        if(intent.hasExtra(NfcAdapter.EXTRA_TAG))

        {
            toast("Extra tag");

            Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            try {
                String[] techList = tag.getTechList();

                for (String tech : techList) {

                    if (tech.equals(android.nfc.tech.MifareUltralight.class.getName())) {
                        toast("Mifare ultra light");

                        MifareUltralight mifareUltralight = MifareUltralight.get(tag);
                        if (mifareUltralight == null) {
                            throw new IllegalArgumentException("No Mifare Ultralight");
                        }
                        try {
                            mifareUltralight.connect();

                            int offset = 4;
                            int length;

                            int type = mifareUltralight.getType();
                            switch (type) {
                                case MifareUltralight.TYPE_ULTRALIGHT: {
                                    length = 12;

                                    break;
                                }
                                case MifareUltralight.TYPE_ULTRALIGHT_C: {
                                    length = 36;

                                    break;
                                }
                                default:
                                    throw new IllegalArgumentException("Unknown mifare ultralight tag " + type);
                            }

                            int readLength = 4;

                            ByteArrayOutputStream bout = new ByteArrayOutputStream();

                            for (int i = offset; i < offset + length; i += readLength) {
                                bout.write(mifareUltralight.readPages(i));
                            }

                            byte[] buffer = bout.toByteArray();

                            StringBuilder builder = new StringBuilder();
                            for (int k = 0; k < buffer.length; k += readLength) {
                                builder.append((offset + k) + " " + toHexString(buffer, k, readLength));
                                builder.append('\n');
                            }

                            // CONTENU DE MESSAGE EN PLUS
                            toast("MIFARE : " + builder.toString());

                            mifareUltralight.close();
                        } catch (Exception e) {

                        }
                    } else if (tech.equals(android.nfc.tech.NfcA.class.getName())) {
                        toast("NfcA");
                    } else if (tech.equals(android.nfc.tech.NfcB.class.getName())) {
                        toast("NfcB");
                    } else if (tech.equals(android.nfc.tech.NfcF.class.getName())) {
                        toast("NfcF");
                    } else if (tech.equals(android.nfc.tech.NfcV.class.getName())) {
                        toast("NfcV");
                    } else if (tech.equals(android.nfc.tech.IsoDep.class.getName())) {
                        toast("Isodep");
                        android.nfc.tech.IsoDep isoDep = IsoDep.get(tag);

                        boolean hostCardEmulation = intent.getBooleanExtra(NfcTag.EXTRA_HOST_CARD_EMULATION, false);

                            /*if (hostCardEmulation) {


                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

                                boolean autoSelectIsoApplication = prefs.getBoolean(PreferencesActivity.PREFERENCE_HOST_CARD_EMULATION_AUTO_SELECT_ISO_APPLICATION, true);

                                if (autoSelectIsoApplication) {
                                    isoDep.connect();

                                    // attempt to select demo HCE application using iso adpu
                                    String isoApplicationString = prefs.getString(PreferencesActivity.PREFERENCE_HOST_CARD_EMULATION_ISO_APPLICATION_ID, null);

                                    // clean whitespace
                                    isoApplicationString = isoApplicationString.replaceAll("\\s", "");

                                    try {
                                        byte[] key = hexStringToByteArray(isoApplicationString);

                                        // send ISO select application.
                                        // All commands starting with 0x00 are passed through without ADPU wrapping for HCE
                                        CommandAPDU command = new CommandAPDU(0x00, 0xA4, 0x04, 00, key);

                                        Log.d(TAG, "Send request " + toHexString(command.getBytes()));

                                        byte[] responseBytes = isoDep.transceive(command.getBytes());

                                        Log.d(TAG, "Got response " + toHexString(responseBytes));

                                        ResponseAPDU response = new ResponseAPDU(responseBytes);

                                        if (response.getSW1() == 0x91 && response.getSW2() == 0x00) {
                                            Log.d(TAG, "Selected HCE application " + isoApplicationString);

                                            // issue command which now should be routed to the same HCE client
                                            // pretend to select application of desfire card

                                            DesfireReader reader = new DesfireReader(isoDep);
                                            reader.selectApplication(0x00112233);

                                            Log.d(TAG, "Selected application using desfire select application command");
                                        } else if (response.getSW1() == 0x82 && response.getSW2() == 0x6A) {
                                            Log.d(TAG, "HCE application " + isoApplicationString + " not found on remote device");
                                        } else {
                                            Log.d(TAG, "Unknown error selecting HCE application " + isoApplicationString);
                                        }
                                    } catch (Exception e) {
                                        Log.w(TAG, "Unable to decode HEX string " + isoApplicationString + " into binary data", e);
                                    }
                                    isoDep.close();

                                }


                            } else {
                                isoDep.connect();

                                DesfireReader reader = new DesfireReader(isoDep);

                                VersionInfo versionInfo = reader.getVersionInfo();

                                Log.d(TAG, "Got version info - hardware version " + versionInfo.getHardwareVersion() + " / software version " + versionInfo.getSoftwareVersion());

                                isoDep.close();
                            }

                    } else if (tech.equals(android.nfc.tech.MifareClassic.class.getName())) {
                        toast("Mifare classic");
                        android.nfc.tech.MifareClassic mifareClassic = MifareClassic.get(tag);

                        mifareClassic.connect();
                        if (mifareClassic.isConnected()) {
                            toast("CONNECTED");
                            new ReadTagTask().execute(mifareClassic);
                        }

                    } else if (tech.equals(android.nfc.tech.Ndef.class.getName())) {
                        toast("NDEF");
                        this.ndef = Ndef.get(tag);

                    } else if (tech.equals(android.nfc.tech.NdefFormatable.class.getName())) {
                        toast("NDEFFORMAT");
                        this.ndefFormatable = NdefFormatable.get(tag);
                    }
                }
            } catch (Exception e) {
                toast("ExCEPTION"+e.getMessage());
            }
        }*/
    }

    @Override
    protected void onNfcFeatureNotFound() {
        toast("Native NFC is not available");
    }

    @Override
    protected void onNfcTagLost(Intent intent) {
        this.tag = false;
    }

    @Override
    protected void onExternalNfcServiceStopped(Intent intent) {
        this.service = false;
    }

    @Override
    protected void onExternalNfcServiceStarted(Intent intent) {
        this.service = true;
    }

    @Override
    protected void onExternalNfcReaderOpened(Intent intent) {
        this.reader = true;

        if (intent.hasExtra(NfcReader.EXTRA_READER_CONTROL)) {
            AcrReader reader = intent.getParcelableExtra(NfcReader.EXTRA_READER_CONTROL);

            if (reader instanceof Acr122UReader) {
                Acr122UReader acr122uReader = (Acr122UReader) reader;
                acr122uReader.setBuzzerForCardDetection(true);

                acr122uReader.setPICC(
                        AcrPICC.AUTO_PICC_POLLING,
                        AcrPICC.POLL_ISO14443_TYPE_B,
                        AcrPICC.POLL_ISO14443_TYPE_A,
                        AcrPICC.AUTO_ATS_GENERATION
                );

                //acr122uReader.setPICC(AcrPICC.AUTO_PICC_POLLING, AcrPICC.POLL_ISO14443_TYPE_B, AcrPICC.POLL_ISO14443_TYPE_A);
            } /*else if(reader instanceof Acr1222LReader) {
                Acr1222LReader acr1222lReader = (Acr1222LReader)reader;

                // display font example - note that also font type C
                acr1222lReader.lightDisplayBacklight(true);
                acr1222lReader.clearDisplay();
                acr1222lReader.displayText(AcrFont.FontA, Typeface.BOLD, 0, 0, "Hello ACR1222L!");
                acr1222lReader.displayText(AcrFont.FontB, Typeface.BOLD, 1, 0, "ABCDE 0123456789");
            } else if(reader instanceof Acr1283LReader) {
                Acr1283LReader acr1283LReader = (Acr1283LReader)reader;

                // display font example - note that also font type C
                acr1283LReader.lightDisplayBacklight(true);
                acr1283LReader.clearDisplay();
                acr1283LReader.displayText(AcrFont.FontA, Typeface.BOLD, 0, 0, "Hello ACR1283L!");
                acr1283LReader.displayText(AcrFont.FontB, Typeface.BOLD, 1, 0, "ABCDE 0123456789");
            }*/
        }
    }

    @Override
    protected void onExternalNfcReaderClosed(Intent intent) {
        this.reader = false;
    }

    @Override
    protected void onExternalNfcTagLost(Intent intent) {
        this.ndef = null;
        this.ndefFormatable = null;
        this.tag = false;
    }

    @Override
    protected void onExternalNfcIntentDetected(Intent intent, String action) {
        // default to same as native NFC
        onNfcIntentDetected(intent, action);
    }

    protected void initializeExternalNfc() {
        broadcast(NfcService.ACTION_SERVICE_STATUS);
        broadcast(NfcReader.ACTION_READER_STATUS);
    }

    public void toast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        //toast.show();
    }

    public void goToParameters(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void checkID(View view) {
        this.checkID("04935CFACD4080");
    }

    private void checkID(String id) {
        this.progressView.ShowProgress(true, shortAnimTime);
        mAuthTask = new UserAuthTask(id);
        mAuthTask.execute((Void) null);
    }

    private void GoToBooking() {
        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra(getString(R.string.displayName), this.displayName);
        intent.putExtra(getString(R.string.idCard), this.idCard);
        intent.putExtra(getString(R.string.urlImage), this.urlImage);
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
            this.idCard = idCard;
            response = Http.SendGetRequest(url + "login.php?id=" + this.idCard);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!response.contains("expired") && !response.contains("unregistered")) {
            this.displayName = response.split(";")[1];
            this.idCard = response.split(";")[0];
            this.urlImage = response.split(";")[2];
            return true;
        } else {
            this.reason = response;
            return false;
        }
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
