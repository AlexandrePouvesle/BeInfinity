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
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
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

public class BookingActivity extends AppCompatActivity {

    private static final String CENTRE_NAME = "centerName";
    private static final String CENTRE_ID = "centerId";
    private static final String URL_NAME = "urlname";

    private HashMap<String, String> parameters;
    private ArrayList<String> terrains;

    private TextView textViewTitle;
    private TextView textViewDateJour;
    private TextView textViewDisplayName;
    private Spinner spinnerTerrain;
    private NumberPicker numberDuree;
    private NumberPicker numberDuree2;
    private NumberPicker numberMinDebut;
    private NumberPicker numberHeureDebut;

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
        this.spinnerTerrain = (Spinner) findViewById(R.id.booking_spinnerTerrain);
        this.numberDuree = (NumberPicker) findViewById(R.id.numberPickerDuree);
        this.numberDuree2 = (NumberPicker) findViewById(R.id.numberPickerDuree2);
        this.numberMinDebut = (NumberPicker) findViewById(R.id.numberPickerMinDebut);
        this.numberHeureDebut = (NumberPicker) findViewById(R.id.numberPickerHeureDebut);

        // Initialisation des variables
        this.numberDuree.setMinValue(0);
        this.numberDuree.setMaxValue(4);
        this.numberDuree.setWrapSelectorWheel(true);
        this.numberDuree.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        this.numberDuree2.setMinValue(0);
        this.numberDuree2.setMaxValue(1);
        this.numberDuree2.setWrapSelectorWheel(true);
        this.numberDuree2.setDisplayedValues(new String[]{"0", "30"});
        this.numberDuree2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        this.numberHeureDebut.setMinValue(0);
        this.numberHeureDebut.setMaxValue(23);
        this.numberHeureDebut.setWrapSelectorWheel(true);
        this.numberHeureDebut.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        this.numberMinDebut.setMinValue(0);
        this.numberMinDebut.setMaxValue(1);
        this.numberMinDebut.setWrapSelectorWheel(true);
        this.numberMinDebut.setDisplayedValues(new String[]{"0", "30"});
        this.numberMinDebut.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
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

    public void Valider(View view) {
        String terrain = (String) this.spinnerTerrain.getSelectedItem();
        int heureDebut = this.numberHeureDebut.getValue();
        int minuteDebut = this.numberMinDebut.getValue() == 1 ? 30 : 0;
        int duree = this.numberDuree.getValue();
        int dureeMinute = this.numberDuree2.getValue() == 1 ? 30 : 0;

        Calendar c = Calendar.getInstance();
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), heureDebut, minuteDebut, 0);

        BookingDto dto = new BookingDto();
        dto.setCentre(Integer.parseInt(this.parameters.get(CENTRE_ID)));
        dto.setTerrain(terrain);
        dto.setHeureDebut(c);
        dto.setDuree(duree);
        dto.setDureeMinute(dureeMinute);

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
            minute = 1;
        }

        //this.simpleTimePicker.setHour(hour);
        //this.simpleTimePicker.setMinute(minute);
        //this.simpleTimePicker.setCurrentMinute(hour);
        //this.simpleTimePicker.setCurrentMinute(minute);
        // TODO
        this.numberMinDebut.setValue(minute);
        this.numberHeureDebut.setValue(hour);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, terrains);
        this.spinnerTerrain.setAdapter(adapter);
    }

    private void SendBooking(BookingDto dto) {
        // booking.php?centre=1&abonne=1&date=%272016-11-03%27&heure=%2720:00:00%27&duree=2&terrain=%27Zidane%27
        Traitement traitementBooking = new Traitement(dto);
        traitementBooking.execute((Void) null);
    }

    public class Traitement extends AsyncTask<Void, Void, Integer> {

        private final BookingDto dto;

        public Traitement(BookingDto dto) {
            this.dto = dto;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            String url = parameters.get(URL_NAME);
            DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd%20HH:mm:ss", Locale.FRANCE);

            Calendar dateFin = (Calendar) dto.getHeureDebut().clone();
            dateFin.add(Calendar.HOUR, dto.getDuree());
            dateFin.add(Calendar.MINUTE, dto.getDureeMinute());

            String param = "centre=\"" + dto.getCentre()
                    + "\"&abonne=\"" + idCard
                    + "\"&dateDebut=\"" + dateFormat2.format(dto.getHeureDebut().getTime())
                    + "\"&dateFin=\"" + dateFormat2.format(dateFin.getTime())
                    + "\"&duree=\"" + (dto.getDuree() + (dto.getDureeMinute() == 30 ? 0.5 : 0 ))
                    + "\"&terrain=\"" + dto.getTerrain() + "\"";

            String response = null;
            try {
                response = Http.SendGetRequest(url + "booking.php?" + param);
            } catch (IOException e) {
                return -1;
            }

            if (response.equals("OK")) {
                return 0;
            } else {
                return -2;
            }
        }


        @Override
        protected void onPostExecute(final Integer codeReponse) {

            // On enregistre en base de données si pas envoyer
            if (codeReponse == -1) {
                DbHelper dbHelper = new DbHelper(getBaseContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                ContentValues initialValues = new ContentValues();
                initialValues.put(DbContract.BookingEntry.COLUMN_NAME_DATE, textViewDateJour.getText().toString());
                //initialValues.put(DbContract.BookingEntry.COLUMN_NAME_HEURE_DEBUT, c.getTimeInMillis());
                initialValues.put(DbContract.BookingEntry.COLUMN_NAME_DUREE, numberDuree.getValue());
                initialValues.put(DbContract.BookingEntry.COLUMN_NAME_DUREE_MINUTE, numberDuree2.getValue());
                //initialValues.put(DbContract.BookingEntry.COLUMN_NAME_TERRAIN, terrain);

                db.insert(DbContract.BookingEntry.TABLE_NAME, null, initialValues);
                db.close();

                Toast.makeText(getApplicationContext(), getString(R.string.booking_toast), Toast.LENGTH_SHORT).show();
                finish();
            } else if (codeReponse == -2) {
                Toast.makeText(getApplicationContext(), getString(R.string.booking_echec), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.booking_toast), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
