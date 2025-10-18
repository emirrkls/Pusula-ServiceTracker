package com.pusulaiklimlendirme;

public class Model {
    private int id;
    private int markaId;
    private String ad;

    public Model() {}

    public Model(int id, int markaId, String ad) {
        this.id = id;
        this.markaId = markaId;
        this.ad = ad;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMarkaId() { return markaId; }
    public void setMarkaId(int markaId) { this.markaId = markaId; }
    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }

    @Override
    public String toString() {
        return ad;
    }
}