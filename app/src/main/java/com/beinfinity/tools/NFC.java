package com.beinfinity.tools;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Created by Alexandre on 16/10/2016.
 */

public class NFC {

    public static NdefMessage createNDefMessage(NdefRecord record)
    {
        NdefRecord[] records = new NdefRecord[2];
        // si le AAR ne marche pas, mettre un filtre en [0] du record et config le manifest
        records[0] = record;
        // Ajout du AAR
        records[1] = NdefRecord.createApplicationRecord("com.beinfinity");
        NdefMessage message = new NdefMessage(records);


        return message;
    }

    public static NdefRecord createRecord(String message)
    {
        byte[] langBytes = Locale.FRANCE.getLanguage().getBytes(Charset.forName("US-ASCII"));
        byte[] textBytes = message.getBytes(Charset.forName("UTF-8"));
        char status = (char) (langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }
}
