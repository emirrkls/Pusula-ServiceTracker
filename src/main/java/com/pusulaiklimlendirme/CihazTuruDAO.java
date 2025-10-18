package com.pusulaiklimlendirme; // veya .dao

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Tip nesneleri için Veritabanı Erişim Nesnesi (DAO).
 * Tipler tablosu üzerinde CRUD işlemleri yapar.
 */
public class CihazTuruDAO {

    /**
     * Veritabanındaki tüm tipleri isim sırasına göre listeler.
     *
     * @return Tip nesnelerinin bir listesi. Hata durumunda veya tip yoksa boş liste döner.
     */
    public List<CihazTuru> getAllCihazTurleri() {
        List<CihazTuru> cihazTurleri = new ArrayList<>();
        String sql = "SELECT id, ad FROM cihaz_turleri ORDER BY ad ASC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String ad = rs.getString("ad");
                cihazTurleri.add(new CihazTuru(id, ad));
            }
        } catch (SQLException e) {
            System.err.println("Cihaz türleri alınırken veritabanı hatası: " + e.getMessage());
        }
        return cihazTurleri;
    }

    /**
     * Veritabanına yeni bir tip ekler.
     *
     * @param cihazTuruAdi Eklenecek tipin adı. Boş veya null olmamalıdır.
     * @return Ekleme başarılı ise true, değilse false döner.
     */
    public boolean addCihazTuru(String cihazTuruAdi) {
        if (cihazTuruAdi == null || cihazTuruAdi.trim().isEmpty()) {
            System.err.println("Cihaz Türü adı boş olamaz.");
            return false;
        }

        String sql = "INSERT INTO cihaz_turleri(ad) VALUES(?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cihazTuruAdi.trim());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.err.println("Hata: Bu cihaz türü zaten mevcut! (" + cihazTuruAdi + ")");
            } else {
                System.err.println("Cihaz türü eklerken veritabanı hatası: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * Verilen isme sahip tipin ID'sini döndürür.
     * Parça eklerken tip ID'sini bulmak için kullanışlıdır.
     *
     * @param cihazTuruAdi Aranacak tipin adı.
     * @return Tip bulunursa ID'sini, bulunamazsa veya hata olursa -1 döner.
     */
    public int getCihazTuruIdByName(String cihazTuruAdi) {
        String sql = "SELECT id FROM cihaz_turleri WHERE ad = ?";
        int CihazTuruId = -1;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cihazTuruAdi);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    CihazTuruId = rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Cihaz Türü ID alınırken hata: " + e.getMessage());
        }
        return CihazTuruId;
    }

    // TipDAO.java içine eklenecek metotlar

/**
 * Verilen ID'ye sahip tipin adını günceller.
 *
 * @param id Güncellenecek tipin ID'si.
 * @param yeniAd Tipin yeni adı. Boş veya null olmamalıdır.
 * @return Güncelleme başarılı ise true, değilse false döner.
 */
public boolean updateCihazTuru(int id, String yeniAd) {
    if (yeniAd == null || yeniAd.trim().isEmpty()) {
        System.err.println("Yeni cihaz türü adı boş olamaz.");
        return false;
    }
     if (id <= 0) {
        System.err.println("Geçersiz cihaz türü ID.");
        return false;
    }
    String sql = "UPDATE cihaz_turleri SET ad = ? WHERE id = ?";

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, yeniAd.trim());
        pstmt.setInt(2, id);
        int affectedRows = pstmt.executeUpdate();
        return affectedRows > 0;

    } catch (SQLException e) {
         if (e.getMessage().contains("UNIQUE constraint failed")) {
            System.err.println("Hata: Bu Cihaz adı zaten başka bir kayıtta mevcut! (" + yeniAd + ")");
        } else {
            System.err.println("Cihaz türü güncellenirken veritabanı hatası: " + e.getMessage());
        }
        return false;
    }
}

/**
 * Verilen ID'ye sahip tipi siler.
 * Dikkat: Eğer bu tip parçalar tarafından kullanılıyorsa (ON DELETE RESTRICT varsayılan),
 * FOREIGN KEY hatası alınacaktır.
 *
 * @param id Silinecek tipin ID'si.
 * @return Silme başarılı ise true, değilse false döner.
 */
public boolean deleteCihazTuru(int id) {
     if (id <= 0) {
        System.err.println("Geçersiz Cihaz Türü ID.");
        return false;
    }
    String sql = "DELETE FROM cihaz_turleri WHERE id = ?";

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setInt(1, id);
        int affectedRows = pstmt.executeUpdate();
        return affectedRows > 0;

    } catch (SQLException e) {
         if (e.getMessage().contains("FOREIGN KEY constraint failed")) {
             // Parcalar tablosunda ON DELETE RESTRICT vardı.
             System.err.println("Hata: Bu Cihaz Türü, başka kayıtlar (parçalar) tarafından kullanıldığı için silinemiyor. ID: " + id);
         } else {
            System.err.println("Cihaz Türü silinirken veritabanı hatası: " + e.getMessage());
         }
        return false;
    }
}

    // Gerekirse diğer metotlar (update, delete, getById) eklenebilir.
}