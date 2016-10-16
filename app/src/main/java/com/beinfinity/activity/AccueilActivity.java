package com.beinfinity.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import centre.com.centre.R;
import centre.com.centre.activity.BookingActivity;
import centre.com.centre.activity.LoginActivity;

import com.beinfinity.tools.ProgressView;

public class AccueilActivity extends AppCompatActivity {

    private UserAuthTask mAuthTask = null;
    private ProgressView progressView;
    private int shortAnimTime;

    private View mProgressView;
    private View mAccueilFormView;

    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);

        this.shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        // Récupération des éléments de la vue
        mProgressView = findViewById(R.id.accueil_progress);
        mAccueilFormView = findViewById(R.id.accueil_form);

        progressView = new ProgressView(mAccueilFormView, mProgressView);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");    /* Handles all MIME based dispatches.
                                       You should specify only the ones that you need. */
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
       /** if (intent != null && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages =
                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMessages != null) {
                NdefMessage[] messages = new NdefMessage[rawMessages.length];
                for (int i = 0; i < rawMessages.length; i++) {
                    messages[i] = (NdefMessage) rawMessages[i];
                }

                if (messages.length > 0) {
                    // TODO: Faire ici l'adaptation en fonction du contenu du tag
                    this.checkID("123456789");
                }
            }
        }*/

        // TODO: Faire test ici
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
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
        startActivity(intent);
    }

    private Boolean checkIDCard(String idCard) {
        //TODO: mettre ici la vérification de l'existance de la carte ou de sa validité
        return true;
    }

    public class UserAuthTask extends AsyncTask<Void, Void, Boolean> {

        String idCard;

        UserAuthTask(String idCard) {
            this.idCard = idCard;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            Boolean isOk = false;
            try {
                //TODO: Simulate network access.
                Thread.sleep(2000);
                isOk = checkIDCard(this.idCard);
            } catch (InterruptedException e) {
                return false;
            }

            // TODO: register the new account here.
            return isOk;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            progressView.ShowProgress(false, shortAnimTime);

            if (success) {
                GoToBooking();
                // finish();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.accueil_toast_echec), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            progressView.ShowProgress(false, shortAnimTime);
        }
    }
}
