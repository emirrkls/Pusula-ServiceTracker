package com.pusulaiklimlendirme; // veya com.pusulaiklimlendirme.model

public class Model {
    private int id;
    private int markaId; // marka_id sütununa karşılık gelir
    private String ad;
    // İsteğe bağlı: İlişkili Marka nesnesini tutmak için (DAO'da doldurulur)
    // private Marka marka;

    public Model() {}

    public Model(int id, int markaId, String ad) {
        this.id = id;
        this.markaId = markaId;
        this.ad = ad;
    }

    // --- Getter ve Setterlar ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMarkaId() { return markaId; }
    public void setMarkaId(int markaId) { this.markaId = markaId; }
    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }

    @Override
    public String toString() {
        return ad; // Genellikle sadece model adı yeterli olur
    }
}