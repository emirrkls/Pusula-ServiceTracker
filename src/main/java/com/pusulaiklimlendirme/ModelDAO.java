package com.pusulaiklimlendirme; // veya .dao

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Model nesneleri için Veritabanı Erişim Nesnesi (DAO).
 * Modeller tablosu üzerinde CRUD işlemleri yapar.
 */
public class ModelDAO {

    /**
     * Belirli bir markaya ait tüm modelleri isim sırasına göre listeler.
     *
     * @param markaId Modellerini listelemek istediğimiz markanın ID'si.
     * @return Belirtilen markaya ait Model nesnelerinin bir listesi. Hata veya model yoksa boş liste döner.
     */
    public List<Model> getModellerByMarkaId(int markaId) {
        List<Model> modeller = new ArrayList<>();
        // SQL sorgusu - marka_id'ye göre filtrele ve isme göre sırala
        String sql = "SELECT id, marka_id, ad FROM modeller WHERE marka_id = ? ORDER BY ad ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, markaId); // 1. soru işaretine markaId'yi ata

            try (ResultSet rs = pstmt.executeQuery()) { // ResultSet'i de try-with-resources içine al
                while (rs.next()) {
                    int id = rs.getInt("id");
                    // marka_id'yi tekrar almak yerine parametreden gelen değeri kullanabiliriz
                    // int mId = rs.getInt("marka_id");
                    String ad = rs.getString("ad");
                    modeller.add(new Model(id, markaId, ad));
                }
            }
        } catch (SQLException e) {
            System.err.println("Markaya göre modelleri alırken veritabanı hatası: " + e.getMessage());
        }
        return modeller;
    }

    /**
     * Veritabanına yeni bir model ekler.
     *
     * @param markaId Modelin ait olduğu markanın ID'si.
     * @param modelAdi Eklenecek modelin adı. Boş veya null olmamalıdır.
     * @return Ekleme başarılı ise true, değilse false döner.
     */
    public boolean addModel(int markaId, String modelAdi) {
        if (modelAdi == null || modelAdi.trim().isEmpty()) {
            System.err.println("Model adı boş olamaz.");
            return false;
        }
        if (markaId <= 0) { // Geçersiz marka ID kontrolü
             System.err.println("Geçersiz Marka ID.");
            return false;
        }


        String sql = "INSERT INTO modeller(marka_id, ad) VALUES(?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, markaId);
            pstmt.setString(2, modelAdi.trim());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
             if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.err.println("Hata: Bu model zaten mevcut! (Marka ID: " + markaId + ", Model: " + modelAdi + ")");
            } else if (e.getMessage().contains("FOREIGN KEY constraint failed")) {
                 System.err.println("Hata: Model eklenemedi. Belirtilen Marka ID (" + markaId + ") bulunamadı.");
             }
             else {
                System.err.println("Model eklerken veritabanı hatası: " + e.getMessage());
            }
            return false;
        }
    }

     /**
     * Verilen isme ve marka ID'sine sahip modelin ID'sini döndürür.
     * Parça eklerken model ID'sini bulmak için kullanışlıdır.
     *
     * @param modelAdi Aranacak modelin adı.
     * @param markaId Modelin ait olduğu markanın ID'si.
     * @return Model bulunursa ID'sini, bulunamazsa veya hata olursa -1 döner.
     */
    public int getModelIdByNameAndMarkaId(String modelAdi, int markaId) {
        String sql = "SELECT id FROM modeller WHERE ad = ? AND marka_id = ?";
        int modelId = -1; // Varsayılan olarak -1 (bulunamadı)

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, modelAdi);
            pstmt.setInt(2, markaId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) { // Eğer sonuç varsa
                    modelId = rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Model ID alınırken hata: " + e.getMessage());
        }
        return modelId;
    }

    // ModelDAO.java içine eklenecek metotlar

/**
 * Verilen ID'ye sahip modelin adını ve/veya marka ID'sini günceller.
 * Şimdilik sadece adı güncelleyelim. Marka değiştirmek genellikle istenmez.
 *
 * @param id Güncellenecek modelin ID'si.
 * @param yeniAd Modelin yeni adı. Boş veya null olmamalıdır.
 * @return Güncelleme başarılı ise true, değilse false döner.
 */
public boolean updateModel(int id, String yeniAd) {
    if (yeniAd == null || yeniAd.trim().isEmpty()) {
        System.err.println("Yeni model adı boş olamaz.");
        return false;
    }
     if (id <= 0) {
        System.err.println("Geçersiz model ID.");
        return false;
    }
    String sql = "UPDATE modeller SET ad = ? WHERE id = ?";

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, yeniAd.trim());
        pstmt.setInt(2, id);
        int affectedRows = pstmt.executeUpdate();
        return affectedRows > 0;

    } catch (SQLException e) {
        // Modellerde marka_id + ad UNIQUE idi. Sadece ad değişirse sorun olmaz
        // ama başka bir markadaki aynı adla çakışabilir mi? Hayır, çünkü ID ile güncelliyoruz.
        // Yine de UNIQUE hatası gelirse (belki aynı markada başka bir model o isme sahipse):
         if (e.getMessage().contains("UNIQUE constraint failed")) {
            System.err.println("Hata: Bu model adı, aynı marka altında zaten mevcut! (" + yeniAd + ")");
        } else {
            System.err.println("Model güncellenirken veritabanı hatası: " + e.getMessage());
        }
        return false;
    }
}

/**
 * Verilen ID'ye sahip modeli siler.
 * Dikkat: Eğer bu model parçalar tarafından kullanılıyorsa (ON DELETE RESTRICT),
 * FOREIGN KEY hatası alınacaktır.
 *
 * @param id Silinecek modelin ID'si.
 * @return Silme başarılı ise true, değilse false döner.
 */
public boolean deleteModel(int id) {
     if (id <= 0) {
        System.err.println("Geçersiz model ID.");
        return false;
    }
    String sql = "DELETE FROM modeller WHERE id = ?";

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setInt(1, id);
        int affectedRows = pstmt.executeUpdate();
        return affectedRows > 0;

    } catch (SQLException e) {
        if (e.getMessage().contains("FOREIGN KEY constraint failed")) {
             // Parcalar tablosunda ON DELETE RESTRICT vardı.
             System.err.println("Hata: Bu model, başka kayıtlar (parçalar) tarafından kullanıldığı için silinemiyor. ID: " + id);
         } else {
            System.err.println("Model silinirken veritabanı hatası: " + e.getMessage());
         }
        return false;
    }
}

    // Gerekirse tüm modelleri listeleyen bir metot da eklenebilir:
    // public List<Model> getAllModeller() { ... }
}