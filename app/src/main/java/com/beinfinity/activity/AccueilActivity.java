package com.beinfinity.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.beinfinity.R;

import com.beinfinity.database.DbContract;
import com.beinfinity.database.DbHelper;
import com.beinfinity.tools.Http;
import com.beinfinity.tools.ProgressView;

import java.io.IOException;
import java.util.HashMap;

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

        this.mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        this.pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }

        this.intentFiltersArray = new IntentFilter[]{ndef,};
        this.techListsArray = new String[][]{new String[]{
                MifareClassic.class.getName()
        }};
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages =
                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMessages != null) {
                NdefMessage[] messages = new NdefMessage[rawMessages.length];
                for (int i = 0; i < rawMessages.length; i++) {
                    messages[i] = (NdefMessage) rawMessages[i];
                }

                if (messages.length > 0) {
                    // TODO: Faire vérification  suppl si nécessaire
                    String message = new String(messages[0].getRecords()[0].getPayload());
                    this.checkID(message);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.mNfcAdapter.enableForegroundDispatch(this, this.pendingIntent, this.intentFiltersArray, this.techListsArray);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.mNfcAdapter.disableForegroundDispatch(this);
    }

    public void goToParameters(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void checkID(View view) {
        this.checkID("12345678");
    }

    private void checkID(String id) {
        progressView.ShowProgress(true, shortAnimTime);
        mAuthTask = new UserAuthTask(id);
        mAuthTask.execute((Void) null);
    }

    private void GoToBooking() {
        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra(getString(R.string.displayName), this.displayName);
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
        while (c.isLast()) {
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
            response = Http.SendGetRequest(url + "login.php?id=" + idCard.substring(3));
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
