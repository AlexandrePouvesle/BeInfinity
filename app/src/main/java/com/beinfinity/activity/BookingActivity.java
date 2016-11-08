package com.beinfinity.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

import com.beinfinity.R;
import com.beinfinity.database.DbContract;
import com.beinfinity.database.DbHelper;
import com.beinfinity.model.BookingDto;
import com.beinfinity.tools.Http;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class BookingActivity extends AppCompatActivity {

    private static final String CENTRE_NAME = "centerName";
    private static final String CENTRE_ID = "centerId";
    private static final String URL_NAME = "urlname";

    private HashMap<String, String> parameters;
    private ArrayList<String> terrains;

    private TextView textViewTitle;
    private TextView textViewDateJour;
    private TextView textViewDisplayName;
    private TimePicker simpleTimePicker;
    private Spinner spinnerTerrain;
    private RadioButton radioButton;

    private int supprHour;
    private String abonne;
    private String idCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // Récupération des éléments de la vue
        this.textViewTitle = (TextView) findViewById(R.id.booking_title);
        this.textViewDateJour = (TextView) findViewById(R.id.booking_DateDuJour);
        this.textViewDisplayName = (TextView) findViewById(R.id.booking_Display_Name);
        this.simpleTimePicker = (TimePicker) findViewById(R.id.simpleTimePicker);
        this.spinnerTerrain = (Spinner) findViewById(R.id.booking_spinnerTerrain);
        this.radioButton = (RadioButton) findViewById(R.id.radio_un);

        // Initialisation des variables
        this.supprHour = 1;
        this.radioButton.setChecked(true);
        this.simpleTimePicker.setIs24HourView(true);
        this.terrains = new ArrayList<>();
        this.parameters = new HashMap<>();

        // Récupération des données
        this.GetDb();
        this.FillElement();

        // Récupération du nom de l'utilisateur
        Intent myIntent = getIntent();
        this.abonne = myIntent.getStringExtra(getString(R.string.displayName));
        this.idCard = myIntent.getStringExtra(getString(R.string.idCard));
        this.textViewDisplayName.setText(this.abonne);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        // On récupère la valeur de la checkbox sélectionnée
        switch (view.getId()) {
            case R.id.radio_un:
                if (checked)
                    supprHour = 1;
                break;
            case R.id.radio_deux:
                if (checked)
                    supprHour = 2;
                break;
            case R.id.radio_trois:
                if (checked)
                    supprHour = 3;
                break;
            case R.id.radio_quatre:
                if (checked)
                    supprHour = 4;
                break;
        }
    }

    public void Valider(View view) {
        String terrain = (String) this.spinnerTerrain.getSelectedItem();
        int heureDebut = this.simpleTimePicker.getCurrentHour();
        int minuteDebut = this.simpleTimePicker.getCurrentMinute();

        Calendar c = Calendar.getInstance();
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), heureDebut, minuteDebut, 0);

        BookingDto dto = new BookingDto();
        dto.setCentre(Integer.parseInt(this.parameters.get(CENTRE_ID)));
        dto.setTerrain(terrain);
        dto.setHeureDebut(c);
        dto.setDuree(this.supprHour);

        this.SendBooking(dto);
    }

    public void Annuler(View view) {
        this.finish();
    }

    private void GetDb() {
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
        while (!c.isLast()) {
            String name = c.getString(c.getColumnIndexOrThrow(DbContract.ParameterEntry.COLUMN_NAME_TITLE));
            String content = c.getString(c.getColumnIndexOrThrow(DbContract.ParameterEntry.COLUMN_NAME_CONTENT));
            parameters.put(name, content);
            c.moveToNext();
        }

        // LECTURE DES DONNEES DE TERRAINS
        Cursor ct = db.query(
                DbContract.TerrainEntry.TABLE_NAME,                     // The table to query
                DbContract.TerrainEntry.ProjectionTerrain,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );
        ct.moveToFirst();
        while (!ct.isAfterLast()) {
            String name = ct.getString(ct.getColumnIndexOrThrow(DbContract.TerrainEntry.COLUMN_NAME_TITLE));
            terrains.add(name);
            ct.moveToNext();
        }

        db.close();
    }

    private void FillElement() {
        String center = parameters.get(CENTRE_NAME);
        textViewTitle.setText(center);

        Date aujourdhui = new Date();
        SimpleDateFormat formater = new SimpleDateFormat("E dd MMMM yyyy", Locale.FRANCE);
        textViewDateJour.setText(formater.format(aujourdhui));
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(aujourdhui);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        if (minute > 30) {
            hour++;
            minute = 0;
        } else {
            minute = 30;
        }

        //this.simpleTimePicker.setHour(hour);
        //this.simpleTimePicker.setMinute(minute);
        this.simpleTimePicker.setCurrentMinute(hour);
        this.simpleTimePicker.setCurrentMinute(minute);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, terrains);
        this.spinnerTerrain.setAdapter(adapter);
    }

    private void SendBooking(BookingDto dto) {
        // booking.php?centre=1&abonne=1&date=%272016-11-03%27&heure=%2720:00:00%27&duree=2&terrain=%27Zidane%27
        Traitement traitementBooking = new Traitement(dto);
        traitementBooking.execute((Void) null);
    }

    public class Traitement extends AsyncTask<Void, Void, Boolean> {

        private final BookingDto dto;

        public Traitement(BookingDto dto) {
            this.dto = dto;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = parameters.get(URL_NAME);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
            DateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ss", Locale.FRANCE);
            Date date = new Date();

            String param = "centre=" + dto.getCentre()
                    + "&&abonne=" + idCard
                    + "&&date=\"" + dateFormat.format(date)
                    + "\"&&heure=\"" + dateFormat2.format(dto.getHeureDebut().getTime())
                    + "\"&&duree=" + dto.getDuree()
                    + "&&terrain=\"" + dto.getTerrain()+"\"";

            String response = null;
            try {
                response = Http.SendGetRequest(url + "booking.php?" + param);
            } catch (IOException e) {
            }

            if (response.equals("OK")) {
                return true;
            } else {
                return false;
            }
        }


        @Override
        protected void onPostExecute(final Boolean isSended) {

            // On enregistre en base de données si pas envoyer
            if (!isSended) {
                DbHelper dbHelper = new DbHelper(getBaseContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                ContentValues initialValues = new ContentValues();
                initialValues.put(DbContract.BookingEntry.COLUMN_NAME_DATE, textViewDateJour.getText().toString());
                //initialValues.put(DbContract.BookingEntry.COLUMN_NAME_HEURE_DEBUT, c.getTimeInMillis());
                initialValues.put(DbContract.BookingEntry.COLUMN_NAME_DUREE, supprHour);
                //initialValues.put(DbContract.BookingEntry.COLUMN_NAME_TERRAIN, terrain);

                db.insert(DbContract.BookingEntry.TABLE_NAME, null, initialValues);
                db.close();
            }

            Toast.makeText(getApplicationContext(), getString(R.string.booking_toast), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
