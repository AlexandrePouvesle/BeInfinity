package com.beinfinity.model;

import java.util.Calendar;

public class BookingDto {

    private String Terrain;
    private Calendar HeureDebut;
    private Integer Duree;

    public Integer getDuree() {
        return Duree;
    }

    public void setDuree(Integer duree) {
        Duree = duree;
    }

    public Calendar getHeureDebut() {
        return HeureDebut;
    }

    public void setHeureDebut(Calendar heureDebut) {
        HeureDebut = heureDebut;
    }

    public String getTerrain() {
        return Terrain;
    }

    public void setTerrain(String terrain) {
        Terrain = terrain;
    }
}
