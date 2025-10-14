package com.pusulaiklimlendirme; // veya com.pusulaiklimlendirme.model

public class Parca {
    private int id;
    private String ad;
    private int markaId;
    private int modelId;
    private int tipId;
    private double alisFiyat; // alis_fiyat -> alisFiyat
    private double satisFiyat; // satis_fiyat -> satisFiyat

    // İsteğe bağlı: İlişkili nesneleri tutmak için
    // private Marka marka;
    // private Model model;
    // private Tip tip;

    public Parca() {}

    public Parca(int id, String ad, int markaId, int modelId, int tipId, double alisFiyat, double satisFiyat) {
        this.id = id;
        this.ad = ad;
        this.markaId = markaId;
        this.modelId = modelId;
        this.tipId = tipId;
        this.alisFiyat = alisFiyat;
        this.satisFiyat = satisFiyat;
    }

    // --- Getter ve Setterlar ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }
    public int getMarkaId() { return markaId; }
    public void setMarkaId(int markaId) { this.markaId = markaId; }
    public int getModelId() { return modelId; }
    public void setModelId(int modelId) { this.modelId = modelId; }
    public int getTipId() { return tipId; }
    public void setTipId(int tipId) { this.tipId = tipId; }
    public double getAlisFiyat() { return alisFiyat; }
    public void setAlisFiyat(double alisFiyat) { this.alisFiyat = alisFiyat; }
    public double getSatisFiyat() { return satisFiyat; }
    public void setSatisFiyat(double satisFiyat) { this.satisFiyat = satisFiyat; }

    @Override
    public String toString() {
        return ad; // Combobox vb. için genellikle sadece ad yeterlidir
    }
}