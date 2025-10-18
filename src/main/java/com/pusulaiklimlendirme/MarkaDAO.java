package com.pusulaiklimlendirme; // veya .dao

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MarkaDAO {

    /** ... @return A list of Marka objects. 
     * Returns an empty list on error or if no brands exist. */
    public List<Marka> getAllMarkalar() {
        List<Marka> markalar = new ArrayList<>();
        String sql = "SELECT id, ad FROM markalar ORDER BY ad ASC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // ResultSet üzerinde satır satır ilerle
            while (rs.next()) {
                // Her satırdan verileri al
                int id = rs.getInt("id");
                String ad = rs.getString("ad");
                // Yeni bir Marka nesnesi oluştur ve listeye ekle
                markalar.add(new Marka(id, ad));
            }
        } catch (SQLException e) {
            System.err.println("Markaları alırken veritabanı hatası: " + e.getMessage());
            // GUI katmanında kullanıcıya bilgi vermek daha iyi olur
        }
        return markalar;
    }

    /**
     * Veritabanına yeni bir marka ekler.
     *
     * @param markaAdi Eklenecek markanın adı. Boş veya null olmamalıdır.
     * @return Ekleme başarılı ise true, değilse false döner.
     */
    public boolean addMarka(String markaAdi) {
        // Gelen marka adı geçerli mi kontrol et (boş veya sadece boşluk olamaz)
        if (markaAdi == null || markaAdi.trim().isEmpty()) {
            System.err.println("Marka adı boş olamaz.");
            // GUI'de kullanıcıya uyarı verilmeli.
            return false;
        }

        // SQL sorgusu - Parametre (?) kullanarak SQL Injection'ı önle
        String sql = "INSERT INTO markalar(ad) VALUES(?)";

        // try-with-resources ile Connection ve PreparedStatement otomatik kapanır
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Parametreyi ayarla (1. soru işaretine markaAdi değerini ata)
            pstmt.setString(1, markaAdi.trim()); // Baştaki/sondaki boşlukları temizle

            // Sorguyu çalıştır ve etkilenen satır sayısını al
            int affectedRows = pstmt.executeUpdate();

            // Etkilenen satır sayısı 0'dan büyükse ekleme başarılıdır
            return affectedRows > 0;

        } catch (SQLException e) {
            // Hata UNIQUE kısıtlamasından mı kaynaklanıyor kontrol et (Python'daki gibi)
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.err.println("Hata: Bu marka zaten mevcut! (" + markaAdi + ")");
                // GUI'de kullanıcıya özel hata mesajı gösterilmeli.
            } else {
                // Diğer SQL hataları
                System.err.println("Marka eklerken veritabanı hatası: " + e.getMessage());
            }
            return false; // Hata durumunda false dön
        }
    }

    // MarkaDAO.java içine eklenecek metot

    /**
     * Verilen isme sahip markanın ID'sini döndürür.
     * Parça eklerken marka ID'sini bulmak için kullanışlıdır.
     *
     * @param markaAdi Aranacak markanın adı.
     * @return Marka bulunursa ID'sini, bulunamazsa veya hata olursa -1 döner.
     */
    public int getMarkaIdByName(String markaAdi) {
        String sql = "SELECT id FROM markalar WHERE ad = ?";
        int markaId = -1;

        if (markaAdi == null || markaAdi.trim().isEmpty()) {
            return -1; // Geçersiz isim
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, markaAdi.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    markaId = rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Marka ID alınırken hata: " + e.getMessage());
        }
        return markaId;
    }

    // MarkaDAO.java içine eklenecek metotlar

/**
 * Verilen ID'ye sahip markanın adını günceller.
 *
 * @param id Güncellenecek markanın ID'si.
 * @param yeniAd Markanın yeni adı. Boş veya null olmamalıdır.
 * @return Güncelleme başarılı ise true, değilse false döner.
 */
public boolean updateMarka(int id, String yeniAd) {
    if (yeniAd == null || yeniAd.trim().isEmpty()) {
        System.err.println("Yeni marka adı boş olamaz.");
        return false;
    }
    if (id <= 0) {
        System.err.println("Geçersiz marka ID.");
        return false;
    }

    String sql = "UPDATE markalar SET ad = ? WHERE id = ?";

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, yeniAd.trim());
        pstmt.setInt(2, id);

        int affectedRows = pstmt.executeUpdate();
        return affectedRows > 0; // 1 satır güncellendiyse true döner

    } catch (SQLException e) {
        if (e.getMessage().contains("UNIQUE constraint failed")) {
            System.err.println("Hata: Bu marka adı zaten başka bir kayıtta mevcut! (" + yeniAd + ")");
        } else {
            System.err.println("Marka güncellenirken veritabanı hatası: " + e.getMessage());
        }
        return false;
    }
}

/**
 * Verilen ID'ye sahip markayı siler.
 * Dikkat: Eğer bu marka modeller tarafından kullanılıyorsa,
 * (ON DELETE CASCADE ayarlanmadıysa) FOREIGN KEY hatası alınabilir.
 *
 * @param id Silinecek markanın ID'si.
 * @return Silme başarılı ise true, değilse false döner.
 */
public boolean deleteMarka(int id) {
     if (id <= 0) {
        System.err.println("Geçersiz marka ID.");
        return false;
    }
    String sql = "DELETE FROM markalar WHERE id = ?";

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setInt(1, id);
        int affectedRows = pstmt.executeUpdate();
        return affectedRows > 0;

    } catch (SQLException e) {
         if (e.getMessage().contains("FOREIGN KEY constraint failed")) {
             // Modeller tablosunda ON DELETE CASCADE yoksa bu hata gelir.
             // SQLite'ta cascade default olarak kapalı olabilir, PRAGMA foreign_keys=ON; gerekir.
             System.err.println("Hata: Bu marka, başka kayıtlar (modeller) tarafından kullanıldığı için silinemiyor. ID: " + id);
         } else {
            System.err.println("Marka silinirken veritabanı hatası: " + e.getMessage());
         }
        return false;
    }
}

    // --- İleride eklenebilecek diğer metotlar ---
    // public Marka getMarkaById(int id) { ... }
    // public boolean updateMarka(Marka marka) { ... }
    // public boolean deleteMarka(int id) { ... }

}