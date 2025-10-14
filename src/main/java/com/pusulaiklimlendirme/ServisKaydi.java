package com.pusulaiklimlendirme; // veya com.pusulaiklimlendirme.model

import java.time.LocalDate;

public class ServisKaydi {
    private int id;
    private int musteriId;
    private LocalDate tarih;
    private String islem;
    // Veritabanında NULL olabilen ID'ler için Wrapper Class kullanmak daha güvenlidir
    private Integer parcaId; // int yerine Integer
    private Integer adet;    // int yerine Integer
    private double iscilik;
    private double odemeTutari;
    private double kar;

    // İsteğe bağlı: İlişkili nesneler
    // private Musteri musteri;
    // private Parca parca;

    public ServisKaydi() {}

    public ServisKaydi(int id, int musteriId, LocalDate tarih, String islem, Integer parcaId, Integer adet, double iscilik, double odemeTutari, double kar) {
        this.id = id;
        this.musteriId = musteriId;
        this.tarih = tarih;
        this.islem = islem;
        this.parcaId = parcaId;
        this.adet = adet;
        this.iscilik = iscilik;
        this.odemeTutari = odemeTutari;
        this.kar = kar;
    }

    // --- Getter ve Setterlar ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMusteriId() { return musteriId; }
    public void setMusteriId(int musteriId) { this.musteriId = musteriId; }
    public LocalDate getTarih() { return tarih; }
    public void setTarih(LocalDate tarih) { this.tarih = tarih; }
    public String getIslem() { return islem; }
    public void setIslem(String islem) { this.islem = islem; }
    public Integer getParcaId() { return parcaId; } // Dönüş tipi Integer
    public void setParcaId(Integer parcaId) { this.parcaId = parcaId; } // Parametre Integer
    public Integer getAdet() { return adet; } // Dönüş tipi Integer
    public void setAdet(Integer adet) { this.adet = adet; } // Parametre Integer
    public double getIscilik() { return iscilik; }
    public void setIscilik(double iscilik) { this.iscilik = iscilik; }
    public double getOdemeTutari() { return odemeTutari; }
    public void setOdemeTutari(double odemeTutari) { this.odemeTutari = odemeTutari; }
    public double getKar() { return kar; }
    public void setKar(double kar) { this.kar = kar; }

    // toString() isteğe bağlı
}