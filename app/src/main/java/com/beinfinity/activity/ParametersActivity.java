package com.beinfinity.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.beinfinity.R;

import com.beinfinity.database.DbContract;
import com.beinfinity.database.DbHelper;
import com.beinfinity.tools.Http;

public class ParametersActivity extends AppCompatActivity {

    private static final String CENTRE_NAME = "centerName";
    private static final String CENTRE_ID = "centerId";
    private static final String URL_NAME = "urlname";
    private static final String URL = "https://beinfiny.fr/app/";

    private ListView mListView;
    private EditText edtTxtUrlName;
    private EditText edtTxtAddTerrain;
    private ArrayAdapter<String> adapter;
    private Spinner spinnerCentre;

    private HashMap<String, String> parameters;
    private HashMap<String, Integer> centres;
    private ArrayList<String> terrains;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters);

        // Récupération des éléments de la vue
        edtTxtAddTerrain = (EditText) findViewById(R.id.editTextAddTerrain);
        edtTxtUrlName = (EditText) findViewById(R.id.editTextUrlName);
        spinnerCentre = (Spinner) findViewById(R.id.booking_spinnerCentre);
        mListView = (ListView) findViewById(R.id.listViewTerrain);

        // Initialisation des variables
        parameters = new HashMap<>();
        terrains = new ArrayList<>();
        centres = new HashMap<>();

        // Récupération des données
        this.GetDataFromDb();
        this.FillParameters();
        this.GetCentres();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, AccueilActivity.class);
        startActivity(intent);
    }

    public void Back(View view) {
        Intent intent = new Intent(this, AccueilActivity.class);
        startActivity(intent);
    }

    public void AddTerrain(View view) {
        String newTerrain = edtTxtAddTerrain.getText().toString();
        if (newTerrain != null
                && newTerrain.length() > 0
                && newTerrain.matches(".*\\w.*")) {
            this.terrains.add(newTerrain);
            this.adapter.notifyDataSetChanged();
            edtTxtAddTerrain.setText(null);
        }
    }

    public void LeaveApp(View view) {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    public void SaveEntries(View view) {
        String centreName = (String) spinnerCentre.getSelectedItem();
        String urlName = edtTxtUrlName.getText().toString();

        DbHelper dbHelper = new DbHelper(getBaseContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // On supprime tous les paramètres (évite la gestion des indexs)
        db.delete(DbContract.BookingEntry.TABLE_NAME, null, null);

        ContentValues centreValues = new ContentValues();
        centreValues.put(DbContract.ParameterEntry.COLUMN_NAME_TITLE, CENTRE_NAME);
        centreValues.put(DbContract.ParameterEntry.COLUMN_NAME_CONTENT, centreName);

        db.insert(DbContract.ParameterEntry.TABLE_NAME, null, centreValues);

        Integer id = this.centres.get(centreName);
        ContentValues centreIdValues = new ContentValues();
        centreIdValues.put(DbContract.ParameterEntry.COLUMN_NAME_TITLE, CENTRE_ID);
        centreIdValues.put(DbContract.ParameterEntry.COLUMN_NAME_CONTENT, id.toString());

        db.insert(DbContract.ParameterEntry.TABLE_NAME, null, centreIdValues);

        ContentValues urlValues = new ContentValues();
        urlValues.put(DbContract.ParameterEntry.COLUMN_NAME_TITLE, URL_NAME);
        urlValues.put(DbContract.ParameterEntry.COLUMN_NAME_CONTENT, urlName);

        db.insert(DbContract.ParameterEntry.TABLE_NAME, null, urlValues);

        // On supprime tous les terrains (évite la gestion des indexs)
        db.delete(DbContract.TerrainEntry.TABLE_NAME, null, null);

        // On insère les nouveaux terrains
        for (String terrain : terrains) {
            ContentValues terrainsValues = new ContentValues();
            terrainsValues.put(DbContract.TerrainEntry.COLUMN_NAME_TITLE, terrain);
            db.insert(DbContract.TerrainEntry.TABLE_NAME, null, terrainsValues);
        }

        Toast.makeText(getApplicationContext(), getString(R.string.parameters_toast), Toast.LENGTH_SHORT).show();
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

    private void FillParameters() {
        String url = parameters.get(URL_NAME);

        if (url == null || url.isEmpty()) {
            url = URL;
        }

        edtTxtUrlName.setText(url);

        this.adapter = new ArrayAdapter<>(ParametersActivity.this,
                android.R.layout.simple_list_item_1, terrains);
        mListView.setAdapter(this.adapter);

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int arg2, long arg3) {
                terrains.remove(arg2);
                adapter.notifyDataSetChanged();

                return false;
            }
        });
    }

    private void GetCentres() {
        Traitement traitementCentres = new Traitement();
        traitementCentres.execute((Void) null);
    }

    public class Traitement extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            String url = parameters.get(URL_NAME);

            if (url == null || url.isEmpty()) {
                url = URL;
            }
            String response = null;
            try {
                response = Http.SendGetRequest(url + "centres.php");
            } catch (IOException e) {
            }
            return response;
        }


        @Override
        protected void onPostExecute(final String response) {

            if (!response.isEmpty()) {
                String centerFromDb = parameters.get(CENTRE_NAME);
                ArrayList<String> tmpCentre = new ArrayList<>();
                String[] centresResponse = response.split(";");
                for (String centre : centresResponse) {
                    int id = Integer.parseInt(centre.split(",")[0]);
                    String name = centre.split(",")[1];
                    centres.put(name, id);
                    tmpCentre.add(name);
                }
                ArrayAdapter adapter = new ArrayAdapter<>(ParametersActivity.this,
                        android.R.layout.simple_list_item_1, tmpCentre);
                spinnerCentre.setAdapter(adapter);
                if (centerFromDb != null && !centerFromDb.isEmpty()) {
                    int pos = ((ArrayAdapter) spinnerCentre.getAdapter()).getPosition(centerFromDb);
                    spinnerCentre.setSelection(pos);
                }
            } else {
                Toast.makeText(ParametersActivity.this, getString(R.string.echecConnexion), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
