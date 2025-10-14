package com.pusulaiklimlendirme;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * ServisKaydi nesneleri için Veritabanı Erişim Nesnesi (DAO).
 * Servis_kayitlari tablosu üzerinde işlem yapar ve ilgili DAO'larla etkileşime girer.
 * Transaction yönetimini addServisKaydi metodu içinde yapar.
 */
public class ServisKaydiDAO {

    // Diğer DAO'lara erişim (doğrudan new ile oluşturuluyor)
    private final MusteriDAO musteriDAO = new MusteriDAO();
    private final ParcaDAO parcaDAO = new ParcaDAO();
    private final StokHareketDAO stokHareketDAO = new StokHareketDAO();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Yeni bir servis kaydı ekler ve eğer parça kullanıldıysa stoğu günceller.
     * Tüm işlemleri tek bir transaction içinde yapar.
     *
     * @param musteriId Servis verilen müşterinin ID'si.
     * @param tarih Servis tarihi (LocalDate).
     * @param islem Yapılan işlem açıklaması.
     * @param parcaId Kullanılan parçanın ID'si (kullanılmadıysa null veya <= 0 olabilir).
     * @param adet Kullanılan parça adedi (parça kullanılmadıysa 0 olabilir).
     * @param iscilik İşçilik ücreti.
     * @param odemeTutari Müşteriden alınan toplam ödeme.
     * @return Ekleme başarılı ise true, değilse false döner.
     */
    public boolean addServisKaydi(int musteriId, LocalDate tarih, String islem, Integer parcaId, Integer adet, double iscilik, double odemeTutari) {

        // Girdi Kontrolleri (Başlangıçta)
        if (musteriId <= 0 || tarih == null || islem == null || islem.trim().isEmpty() || iscilik < 0 || odemeTutari < 0) {
            System.err.println("Servis kaydı için geçersiz veya eksik girdi (Müşteri ID, Tarih, İşlem, Tutarlar).");
            return false;
        }
        boolean parcaKullanildi = (parcaId != null && parcaId > 0 && adet != null && adet > 0);
        if (parcaKullanildi && adet <= 0) {
             System.err.println("Parça adedi pozitif olmalıdır.");
             return false;
        }


        // Kar Hesaplama (Transaction öncesi yapılabilir)
        double parcaMaliyeti = 0.0;
        if (parcaKullanildi) {
            // Parça fiyatını almak için ayrı bir bağlantı kullanılır (okuma işlemi)
            // Transaction'ı etkilemez ve BUSY hatasına neden olmaz.
            parcaMaliyeti = parcaDAO.getParcaAlisFiyat(parcaId) * adet;
            if(parcaMaliyeti == 0.0 && adet > 0) { // Eğer fiyat alınamadıysa (parça yoksa vb.)
                System.err.println("Parça maliyeti hesaplanamadı. Parça ID: " + parcaId);
                // İsteğe bağlı olarak burada işlem durdurulabilir.
                // return false;
            }
        }
        double kar = odemeTutari - (iscilik + parcaMaliyeti);


        // SQL Sorgusu
        String sql = """
            INSERT INTO servis_kayitlari
            (musteri_id, tarih, islem, parca_id, adet, iscilik, odeme_tutari, kar)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        Connection conn = null;
        boolean kayitBasarili = false;

        // Tek bir bağlantı üzerinde transaction yönetimi
        try {
            conn = DatabaseManager.getConnection();
            if (conn == null) {
                throw new SQLException("Veritabanı bağlantısı alınamadı.");
            }
            conn.setAutoCommit(false); // Transaction başlat

            // 1. Servis Kaydını Ekle
            try (PreparedStatement pstmtKayit = conn.prepareStatement(sql)) {
                pstmtKayit.setInt(1, musteriId);
                pstmtKayit.setString(2, tarih.format(DATE_FORMATTER));
                pstmtKayit.setString(3, islem.trim());

                if (parcaKullanildi) { // Sadece parça kullanıldıysa ID ve adedi ayarla
                    pstmtKayit.setInt(4, parcaId);
                    pstmtKayit.setInt(5, adet);
                } else {
                    pstmtKayit.setNull(4, Types.INTEGER);
                    pstmtKayit.setNull(5, Types.INTEGER);
                }

                pstmtKayit.setDouble(6, iscilik);
                pstmtKayit.setDouble(7, odemeTutari);
                pstmtKayit.setDouble(8, kar);

                int affectedRows = pstmtKayit.executeUpdate();
                if (affectedRows <= 0) {
                    throw new SQLException("Servis kaydı eklenemedi, 0 satır etkilendi.");
                }
            } // pstmtKayit otomatik kapanır

            // 2. Eğer parça kullanıldıysa Stoğu Düşür (AYNI bağlantıyı kullanarak)
            if (parcaKullanildi) {
                 // StokHareketDAO'nun Connection alan metodunu çağır
                 // Bu metot yetersiz stok durumunda SQLException fırlatacak
                stokHareketDAO.addStokHareketi(conn, parcaId, adet, "cikis");
                System.out.println("Stok düşüldü (Transaction içinde): Parça ID: " + parcaId + ", Adet: " + adet);
            }

            // Buraya kadar hata yoksa, transaction'ı commit et
            conn.commit();
            kayitBasarili = true;
            System.out.println("Servis kaydı ve (varsa) stok güncelleme başarıyla commit edildi.");

        } catch (SQLException e) {
            // Herhangi bir SQL hatası (Servis kaydı ekleme, Stok kontrolü, Stok düşürme)
            // veya bağlantı hatası buraya düşer.
            System.err.println("Servis kaydı transaction sırasında HATA: " + e.getMessage());
            // Transaction'ı geri al
            if (conn != null) {
                try {
                    System.err.println("Transaction geri alınıyor...");
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Transaction geri alınırken hata: " + ex.getMessage());
                }
            }
            kayitBasarili = false;
        } finally {
            // Transaction için açılan bağlantıyı her zaman kapat
            if (conn != null) {
                try {
                    // AutoCommit'i geri açmak (diğer bağlantıları etkilemez ama iyi pratik)
                    if (!conn.getAutoCommit()) {
                        conn.setAutoCommit(true);
                    }
                    // Bağlantıyı kapat
                    if (!conn.isClosed()) {
                         // System.out.println("ServisKaydiDAO transaction bağlantısı kapatılıyor.");
                         conn.close();
                    }
                } catch (SQLException e) {
                    System.err.println("ServisKaydiDAO transaction bağlantısı kapatılırken/ayarlanırken hata: " + e.getMessage());
                }
            }
        }

        return kayitBasarili;
    }


    /**
     * Veritabanındaki tüm servis kayıtlarını listeler (Sadece ID'ler).
     * DTO Kullanılmayan versiyon.
     */
    public List<ServisKaydi> getAllServisKayitlari() {
        List<ServisKaydi> kayitlar = new ArrayList<>();
        String sql = "SELECT id, musteri_id, tarih, islem, parca_id, adet, iscilik, odeme_tutari, kar FROM servis_kayitlari ORDER BY id DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                int musteriId = rs.getInt("musteri_id");
                LocalDate tarih = LocalDate.parse(rs.getString("tarih"), DATE_FORMATTER);
                String islem = rs.getString("islem");
                Integer parcaId = (Integer) rs.getObject("parca_id");
                Integer adet = (Integer) rs.getObject("adet");
                double iscilik = rs.getDouble("iscilik");
                double odemeTutari = rs.getDouble("odeme_tutari");
                double kar = rs.getDouble("kar");

                kayitlar.add(new ServisKaydi(id, musteriId, tarih, islem, parcaId, adet, iscilik, odemeTutari, kar));
            }
        } catch (SQLException e) {
            System.err.println("Servis kayıtları alınırken veritabanı hatası: " + e.getMessage());
        }
        return kayitlar;
    }

     /**
     * Veritabanındaki tüm servis kayıtlarını ilişkili verilerle (müşteri adı, parça adı)
     * birlikte alıp ServisGorunum DTO listesi olarak döndürür.
     * MusteriServisViewController'daki TableView'u doldurmak için kullanılır.
     *
     * @return ServisGorunum nesnelerinin bir listesi. Hata veya kayıt yoksa boş liste döner.
     */
    public List<ServisGorunum> getServisGorunumListesi() {
        List<ServisGorunum> servisGorunumListesi = new ArrayList<>();
        String sql = """
            SELECT
                sk.id,
                m.ad_soyad AS musteriAdSoyad,
                sk.tarih,
                sk.islem,
                p.ad AS parcaAdi, -- LEFT JOIN nedeniyle null olabilir
                sk.adet,         -- NULL olabilir
                sk.iscilik,
                sk.odeme_tutari,
                sk.kar
            FROM servis_kayitlari sk
            JOIN musteriler m ON sk.musteri_id = m.id        -- Müşteri her zaman olmalı (INNER JOIN)
            LEFT JOIN parcalar p ON sk.parca_id = p.id       -- Parça olmayabilir (LEFT JOIN)
            ORDER BY sk.id DESC                              -- En son kayıtlar üste gelsin
            """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement(); // Bu sorguda parametre olmadığı için Statement yeterli
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String musteriAdSoyad = rs.getString("musteriAdSoyad");
                LocalDate tarih = LocalDate.parse(rs.getString("tarih"), DATE_FORMATTER);
                String islem = rs.getString("islem");
                String parcaAdi = rs.getString("parcaAdi"); // LEFT JOIN nedeniyle null olabilir
                // Nullable Integer için rs.getObject() kullanmak önemli
                Integer adet = (Integer) rs.getObject("adet");
                double iscilik = rs.getDouble("iscilik");
                double odemeTutari = rs.getDouble("odeme_tutari");
                double kar = rs.getDouble("kar");

                // Yeni ServisGorunum nesnesi oluştur ve listeye ekle
                servisGorunumListesi.add(new ServisGorunum(
                    id, musteriAdSoyad, tarih, islem, parcaAdi, adet, iscilik, odemeTutari, kar
                ));
            }
        } catch (SQLException e) {
            System.err.println("Servis görünüm listesi alınırken veritabanı hatası: " + e.getMessage());
        } catch (Exception e) { // Genel hataları da yakala (örn: tarih parse hatası)
             System.err.println("Servis görünüm listesi işlenirken genel hata: " + e.getMessage());
             e.printStackTrace(); // Hatanın kaynağını görmek için
        }
        return servisGorunumListesi;
    }

    // getServisGorunumListesi metodu hala yorumlu (ServisGorunum DTO'su oluşturulunca aktifleşecek)
    /*
    public List<ServisGorunum> getServisGorunumListesi() { ... }
    */

}