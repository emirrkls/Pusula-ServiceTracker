package com.pusulaiklimlendirme; // veya com.pusulaiklimlendirme.model

public class Tip {
    private int id;
    private String ad;

    public Tip() {}

    public Tip(int id, String ad) {
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