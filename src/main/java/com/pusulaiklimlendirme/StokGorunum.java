package com.pusulaiklimlendirme; // Veya .dto

import javafx.beans.property.*; // JavaFX Property sınıflarını import et

/**
 * Stok listesi TableView'da gösterilecek verileri tutan DTO/ViewModel sınıfı.
 * JavaFX Property'leri kullanarak TableView ile kolayca bağlanır.
 */
public class StokGorunum {

    // Normal alanlar yerine JavaFX Property'leri kullanıyoruz.
    // Bu, TableView'un değişiklikleri otomatik algılamasını sağlar.
    private final IntegerProperty id;
    private final StringProperty parcaAdi;
    private final StringProperty markaAdi;
    private final StringProperty modelAdi;
    private final StringProperty tipAdi;
    private final DoubleProperty alisFiyat;
    private final DoubleProperty satisFiyat;
    private final IntegerProperty stokAdet;

    // Kurucu Metot (Constructor)
    public StokGorunum(int id, String parcaAdi, String markaAdi, String modelAdi, String tipAdi, double alisFiyat, double satisFiyat, int stokAdet) {
        this.id = new SimpleIntegerProperty(id);
        this.parcaAdi = new SimpleStringProperty(parcaAdi);
        this.markaAdi = new SimpleStringProperty(markaAdi);
        this.modelAdi = new SimpleStringProperty(modelAdi);
        this.tipAdi = new SimpleStringProperty(tipAdi);
        this.alisFiyat = new SimpleDoubleProperty(alisFiyat);
        this.satisFiyat = new SimpleDoubleProperty(satisFiyat);
        this.stokAdet = new SimpleIntegerProperty(stokAdet);
    }

    // --- Getter Metotları (Property nesnelerini döndüren) ---
    public IntegerProperty idProperty() { return id; }
    public StringProperty parcaAdiProperty() { return parcaAdi; }
    public StringProperty markaAdiProperty() { return markaAdi; }
    public StringProperty modelAdiProperty() { return modelAdi; }
    public StringProperty tipAdiProperty() { return tipAdi; }
    public DoubleProperty alisFiyatProperty() { return alisFiyat; }
    public DoubleProperty satisFiyatProperty() { return satisFiyat; }
    public IntegerProperty stokAdetProperty() { return stokAdet; }

    // --- İsteğe Bağlı: Normal Değerleri Döndüren Getter'lar ---
    // TableView'un CellValueFactory'si genellikle Property metotlarını kullanır,
    // ama bu getter'lar başka yerlerde lazım olabilir.
    public int getId() { return id.get(); }
    public String getParcaAdi() { return parcaAdi.get(); }
    public String getMarkaAdi() { return markaAdi.get(); }
    public String getModelAdi() { return modelAdi.get(); }
    public String getTipAdi() { return tipAdi.get(); }
    public double getAlisFiyat() { return alisFiyat.get(); }
    public double getSatisFiyat() { return satisFiyat.get(); }
    public int getStokAdet() { return stokAdet.get(); }

    // Setter'lar genellikle DTO'larda gerekmez ama istenirse eklenebilir (Property üzerinden)
    // public void setStokAdet(int value) { this.stokAdet.set(value); }
}