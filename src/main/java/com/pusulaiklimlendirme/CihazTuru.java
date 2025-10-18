package com.pusulaiklimlendirme;

public class CihazTuru {
    private int id;
    private String ad;

    public CihazTuru() {}

    public CihazTuru(int id, String ad) {
        this.id = id;
        this.ad = ad;
    }

    // --- Getter ve Setterlar ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }

     @Override
    public String toString() {
        return ad;
    }
}