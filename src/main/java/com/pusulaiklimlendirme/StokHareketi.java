package com.pusulaiklimlendirme; // veya com.pusulaiklimlendirme.model

import java.time.LocalDate; // Tarih için daha modern Java tipi

public class StokHareketi {
    private int id;
    private int parcaId;
    private int adet;
    private String hareketTipi; // "giris" veya "cikis" - Enum kullanmak daha iyi olabilir ileride
    private LocalDate tarih; // String yerine LocalDate kullanıyoruz

    // İsteğe bağlı: İlişkili Parca nesnesi
    // private Parca parca;

    public StokHareketi() {}

    public StokHareketi(int id, int parcaId, int adet, String hareketTipi, LocalDate tarih) {
        this.id = id;
        this.parcaId = parcaId;
        this.adet = adet;
        this.hareketTipi = hareketTipi;
        this.tarih = tarih;
    }

    // --- Getter ve Setterlar ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getParcaId() { return parcaId; }
    public void setParcaId(int parcaId) { this.parcaId = parcaId; }
    public int getAdet() { return adet; }
    public void setAdet(int adet) { this.adet = adet; }
    public String getHareketTipi() { return hareketTipi; }
    public void setHareketTipi(String hareketTipi) { this.hareketTipi = hareketTipi; }
    public LocalDate getTarih() { return tarih; }
    public void setTarih(LocalDate tarih) { this.tarih = tarih; }

    // toString() isteğe bağlı, genellikle doğrudan liste elemanı olmaz
}