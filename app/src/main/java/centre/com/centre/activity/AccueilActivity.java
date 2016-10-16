package centre.com.centre.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import centre.com.centre.R;
import centre.com.centre.tools.ProgressView;

public class AccueilActivity extends AppCompatActivity {

    private UserAuthTask mAuthTask = null;
    private ProgressView progressView;
    private int shortAnimTime;

    private View mProgressView;
    private View mAccueilFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);

        this.shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        // Récupération des éléments de la vue
        mProgressView = findViewById(R.id.accueil_progress);
        mAccueilFormView = findViewById(R.id.accueil_form);

        progressView = new ProgressView(mAccueilFormView, mProgressView);
    }

    public void goToParameters(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }


    public void checkID(View view) {
        progressView.ShowProgress(true, shortAnimTime);
        mAuthTask = new UserAuthTask("123456789");
        mAuthTask.execute((Void) null);
    }

    private void GoToBooking(){
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
                Toast.makeText(getApplicationContext(),getString(R.string.accueil_toast_echec), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            progressView.ShowProgress(false, shortAnimTime);
        }
    }
}
