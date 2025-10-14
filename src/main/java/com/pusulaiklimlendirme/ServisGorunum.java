package com.pusulaiklimlendirme; // veya com.pusulaiklimlendirme.dto

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * Servis Kayıtları TableView'unda gösterilecek verileri tutan DTO/ViewModel sınıfı.
 * JavaFX Property'leri kullanarak TableView ile kolayca bağlanır.
 * Bu sınıf, ServisKaydiDAO içindeki JOIN sorgusu ile doldurulur.
 */
public class ServisGorunum {

    private final IntegerProperty id;
    private final StringProperty musteriAdSoyad; // Musteri tablosundan JOIN ile gelecek
    private final ObjectProperty<LocalDate> tarih; // LocalDate için ObjectProperty
    private final StringProperty islem;
    private final StringProperty parcaAdi;      // Parca tablosundan JOIN ile gelecek (NULL olabilir)
    private final IntegerProperty adet;         // NULL olabilir (IntegerProperty null değerleri yönetebilir)
    private final DoubleProperty iscilik;
    private final DoubleProperty odemeTutari;
    private final DoubleProperty kar;

    // Kurucu Metot (Constructor)
    // ServisKaydiDAO'dan gelen verilere göre doldurulacak
    public ServisGorunum(int id, String musteriAdSoyad, LocalDate tarih, String islem,
                         String parcaAdi, Integer adet, double iscilik, double odemeTutari, double kar) {
        this.id = new SimpleIntegerProperty(id);
        this.musteriAdSoyad = new SimpleStringProperty(musteriAdSoyad);
        this.tarih = new SimpleObjectProperty<>(tarih); // LocalDate için SimpleObjectProperty
        this.islem = new SimpleStringProperty(islem);
        this.parcaAdi = new SimpleStringProperty(parcaAdi); // parcaAdi null olabilir, SimpleStringProperty bunu yönetir
        this.adet = new SimpleIntegerProperty(adet != null ? adet : 0); // Null ise 0 gösterelim (veya -1?) - VEYA ObjectProperty<Integer> kullan
        // Alternatif: IntegerProperty null olamaz, bu yüzden ObjectProperty<Integer> daha doğru olabilir:
        // this.adetProperty = new SimpleObjectProperty<>(adet);

        this.iscilik = new SimpleDoubleProperty(iscilik);
        this.odemeTutari = new SimpleDoubleProperty(odemeTutari);
        this.kar = new SimpleDoubleProperty(kar);
    }

    // --- Property Getter Metotları (TableView için gerekli) ---
    public IntegerProperty idProperty() { return id; }
    public StringProperty musteriAdSoyadProperty() { return musteriAdSoyad; }
    public ObjectProperty<LocalDate> tarihProperty() { return tarih; }
    public StringProperty islemProperty() { return islem; }
    public StringProperty parcaAdiProperty() { return parcaAdi; }
    public IntegerProperty adetProperty() { return adet; } // Veya ObjectProperty<Integer> ise o döner
    public DoubleProperty iscilikProperty() { return iscilik; }
    public DoubleProperty odemeTutariProperty() { return odemeTutari; }
    public DoubleProperty karProperty() { return kar; }

    // --- İsteğe Bağlı: Normal Değerleri Döndüren Getter'lar ---
    public int getId() { return id.get(); }
    public String getMusteriAdSoyad() { return musteriAdSoyad.get(); }
    public LocalDate getTarih() { return tarih.get(); }
    public String getIslem() { return islem.get(); }
    public String getParcaAdi() { return parcaAdi.get(); }
    public Integer getAdet() { return adet.get(); } // Veya ObjectProperty<Integer> ise adetProperty.get()
    public double getIscilik() { return iscilik.get(); }
    public double getOdemeTutari() { return odemeTutari.get(); }
    public double getKar() { return kar.get(); }

}