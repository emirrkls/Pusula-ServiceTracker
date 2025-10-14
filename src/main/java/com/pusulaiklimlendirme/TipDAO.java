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
public class TipDAO {

    /**
     * Veritabanındaki tüm tipleri isim sırasına göre listeler.
     *
     * @return Tip nesnelerinin bir listesi. Hata durumunda veya tip yoksa boş liste döner.
     */
    public List<Tip> getAllTipler() {
        List<Tip> tipler = new ArrayList<>();
        String sql = "SELECT id, ad FROM tipler ORDER BY ad ASC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String ad = rs.getString("ad");
                tipler.add(new Tip(id, ad));
            }
        } catch (SQLException e) {
            System.err.println("Tipleri alırken veritabanı hatası: " + e.getMessage());
        }
        return tipler;
    }

    /**
     * Veritabanına yeni bir tip ekler.
     *
     * @param tipAdi Eklenecek tipin adı. Boş veya null olmamalıdır.
     * @return Ekleme başarılı ise true, değilse false döner.
     */
    public boolean addTip(String tipAdi) {
        if (tipAdi == null || tipAdi.trim().isEmpty()) {
            System.err.println("Tip adı boş olamaz.");
            return false;
        }

        String sql = "INSERT INTO tipler(ad) VALUES(?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tipAdi.trim());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.err.println("Hata: Bu tip zaten mevcut! (" + tipAdi + ")");
            } else {
                System.err.println("Tip eklerken veritabanı hatası: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * Verilen isme sahip tipin ID'sini döndürür.
     * Parça eklerken tip ID'sini bulmak için kullanışlıdır.
     *
     * @param tipAdi Aranacak tipin adı.
     * @return Tip bulunursa ID'sini, bulunamazsa veya hata olursa -1 döner.
     */
    public int getTipIdByName(String tipAdi) {
        String sql = "SELECT id FROM tipler WHERE ad = ?";
        int tipId = -1;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tipAdi);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    tipId = rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Tip ID alınırken hata: " + e.getMessage());
        }
        return tipId;
    }

    // TipDAO.java içine eklenecek metotlar

/**
 * Verilen ID'ye sahip tipin adını günceller.
 *
 * @param id Güncellenecek tipin ID'si.
 * @param yeniAd Tipin yeni adı. Boş veya null olmamalıdır.
 * @return Güncelleme başarılı ise true, değilse false döner.
 */
public boolean updateTip(int id, String yeniAd) {
    if (yeniAd == null || yeniAd.trim().isEmpty()) {
        System.err.println("Yeni tip adı boş olamaz.");
        return false;
    }
     if (id <= 0) {
        System.err.println("Geçersiz tip ID.");
        return false;
    }
    String sql = "UPDATE tipler SET ad = ? WHERE id = ?";

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, yeniAd.trim());
        pstmt.setInt(2, id);
        int affectedRows = pstmt.executeUpdate();
        return affectedRows > 0;

    } catch (SQLException e) {
         if (e.getMessage().contains("UNIQUE constraint failed")) {
            System.err.println("Hata: Bu tip adı zaten başka bir kayıtta mevcut! (" + yeniAd + ")");
        } else {
            System.err.println("Tip güncellenirken veritabanı hatası: " + e.getMessage());
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
public boolean deleteTip(int id) {
     if (id <= 0) {
        System.err.println("Geçersiz tip ID.");
        return false;
    }
    String sql = "DELETE FROM tipler WHERE id = ?";

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setInt(1, id);
        int affectedRows = pstmt.executeUpdate();
        return affectedRows > 0;

    } catch (SQLException e) {
         if (e.getMessage().contains("FOREIGN KEY constraint failed")) {
             // Parcalar tablosunda ON DELETE RESTRICT vardı.
             System.err.println("Hata: Bu tip, başka kayıtlar (parçalar) tarafından kullanıldığı için silinemiyor. ID: " + id);
         } else {
            System.err.println("Tip silinirken veritabanı hatası: " + e.getMessage());
         }
        return false;
    }
}

    // Gerekirse diğer metotlar (update, delete, getById) eklenebilir.
}