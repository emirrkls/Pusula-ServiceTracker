package com.pusulaiklimlendirme; // veya com.pusulaiklimlendirme.model

public class Marka {
    private int id;
    private String ad;

    // Kurucu Metotlar (Constructors)
    public Marka() { // Varsayılan (boş) kurucu
    }

    public Marka(int id, String ad) {
        this.id = id;
        this.ad = ad;
    }

    // Getter ve Setter Metotları
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    // ComboBox'ta düzgün görünmesi için toString() metodunu override et
    @Override
    public String toString() {
        return ad; // Sadece adı göster
    }
}