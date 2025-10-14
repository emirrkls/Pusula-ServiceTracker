package com.pusulaiklimlendirme; // veya com.pusulaiklimlendirme.model

public class Musteri {
    private int id;
    private String adSoyad; // ad_soyad -> adSoyad (Java Naming Convention: camelCase)
    private String telefon;
    private String adres;

    public Musteri() {}

    public Musteri(int id, String adSoyad, String telefon, String adres) {
        this.id = id;
        this.adSoyad = adSoyad;
        this.telefon = telefon;
        this.adres = adres;
    }

    // --- Getter ve Setterlar ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getAdSoyad() { return adSoyad; }
    public void setAdSoyad(String adSoyad) { this.adSoyad = adSoyad; }
    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }
    public String getAdres() { return adres; }
    public void setAdres(String adres) { this.adres = adres; }

     @Override
    public String toString() {
        return adSoyad + " (" + telefon + ")"; // Listelerde vb. kullanışlı olabilir
    }
}