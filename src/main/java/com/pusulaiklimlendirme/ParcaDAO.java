package com.pusulaiklimlendirme; // veya .dao

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Parca nesneleri için Veritabanı Erişim Nesnesi (DAO).
 * Parcalar tablosu üzerinde CRUD işlemleri ve ilgili yardımcı metotları içerir.
 */
public class ParcaDAO {

    // Diğer DAO'lara erişim (doğrudan new ile)
    private final MarkaDAO markaDAO = new MarkaDAO();
    private final ModelDAO modelDAO = new ModelDAO();
    private final TipDAO tipDAO = new TipDAO();

    // --- Mevcut Metotlar (addParca, getAllParcalar, getParcaById, vb.) ---

    /**
     * Veritabanına yeni bir parça ekler. Marka, Model, Tip adlarına göre ID'leri bulur.
     *
     * @param ad Parça adı.
     * @param markaAdi Parçanın ait olduğu marka adı.
     * @param modelAdi Parçanın ait olduğu model adı.
     * @param tipAdi Parçanın ait olduğu tip adı.
     * @param alisFiyat Parçanın alış fiyatı.
     * @param satisFiyat Parçanın satış fiyatı.
     * @return Ekleme başarılı ise true, değilse false döner.
     */
    public boolean addParca(String ad, String markaAdi, String modelAdi, String tipAdi, double alisFiyat, double satisFiyat) {
        // Girdi kontrolleri
        if (ad == null || ad.trim().isEmpty() ||
            markaAdi == null || markaAdi.trim().isEmpty() ||
            modelAdi == null || modelAdi.trim().isEmpty() ||
            tipAdi == null || tipAdi.trim().isEmpty()) {
            System.err.println("Parça adı, marka, model ve tip boş olamaz.");
            return false;
        }
        if (alisFiyat < 0 || satisFiyat < 0) {
             System.err.println("Fiyatlar negatif olamaz.");
             return false;
        }

        // Adlardan ID'leri al
        int markaId = markaDAO.getMarkaIdByName(markaAdi);
        if (markaId == -1) {
            System.err.println("Parça eklenemedi. Marka bulunamadı: " + markaAdi);
            return false;
        }
        int modelId = modelDAO.getModelIdByNameAndMarkaId(modelAdi, markaId);
        if (modelId == -1) {
            System.err.println("Parça eklenemedi. Model bulunamadı: " + modelAdi + " (Marka: " + markaAdi + ")");
            return false;
        }
        int tipId = tipDAO.getTipIdByName(tipAdi);
        if (tipId == -1) {
            System.err.println("Parça eklenemedi. Tip bulunamadı: " + tipAdi);
            return false;
        }

        // Şimdi ID'lerle birlikte parçayı ekle
        String sql = "INSERT INTO parcalar(ad, marka_id, model_id, tip_id, alis_fiyat, satis_fiyat) VALUES(?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ad.trim());
            pstmt.setInt(2, markaId);
            pstmt.setInt(3, modelId);
            pstmt.setInt(4, tipId);
            pstmt.setDouble(5, alisFiyat);
            pstmt.setDouble(6, satisFiyat);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
             if (e.getMessage().contains("FOREIGN KEY constraint failed")) {
                 System.err.println("Parça eklerken FOREIGN KEY hatası. ID'ler geçerli mi?");
             } else {
                System.err.println("Parça eklerken veritabanı hatası: " + e.getMessage());
             }
            return false;
        }
    }

    /**
     * Veritabanındaki tüm parçaları listeler.
     *
     * @return Parca nesnelerinin bir listesi. Hata veya parça yoksa boş liste döner.
     */
    public List<Parca> getAllParcalar() {
        List<Parca> parcalar = new ArrayList<>();
        String sql = "SELECT id, ad, marka_id, model_id, tip_id, alis_fiyat, satis_fiyat FROM parcalar ORDER BY ad ASC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                parcalar.add(mapResultSetToParca(rs));
            }
        } catch (SQLException e) {
            System.err.println("Parçaları alırken veritabanı hatası: " + e.getMessage());
        }
        return parcalar;
    }

    /**
     * ID'sine göre belirli bir parçayı getirir.
     *
     * @param id Aranacak parçanın ID'si.
     * @return Parça bulunursa Parca nesnesi, bulunamazsa veya hata olursa null döner.
     */
    public Parca getParcaById(int id) {
        String sql = "SELECT id, ad, marka_id, model_id, tip_id, alis_fiyat, satis_fiyat FROM parcalar WHERE id = ?";
        Parca parca = null;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    parca = mapResultSetToParca(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Parça ID ile alınırken hata: " + e.getMessage());
        }
        return parca;
    }

    /**
     * Verilen isme sahip parçanın ID'sini döndürür (ilk bulduğunu).
     *
     * @param parcaAdi Aranacak parçanın adı.
     * @return Parça bulunursa ID'sini, bulunamazsa veya hata olursa -1 döner.
     */
    public int getParcaIdByName(String parcaAdi) {
        String sql = "SELECT id FROM parcalar WHERE ad = ?";
        int parcaId = -1;
         if (parcaAdi == null || parcaAdi.trim().isEmpty()) return -1;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, parcaAdi.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    parcaId = rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Parça ID alınırken hata: " + e.getMessage());
        }
        return parcaId;
    }

    /**
     * Belirli bir parçanın alış fiyatını döndürür.
     *
     * @param parcaId Alış fiyatı öğrenilmek istenen parçanın ID'si.
     * @return Parçanın alış fiyatı. Parça bulunamazsa veya hata olursa 0.0 döner.
     */
    public double getParcaAlisFiyat(int parcaId) {
        String sql = "SELECT alis_fiyat FROM parcalar WHERE id = ?";
        double alisFiyat = 0.0;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parcaId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    alisFiyat = rs.getDouble("alis_fiyat");
                } else {
                    System.err.println("Alış fiyatı alınamadı. Parça ID bulunamadı: " + parcaId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Parça alış fiyatı alınırken hata: " + e.getMessage());
        }
        return alisFiyat;
    }

    /**
     * ResultSet'in mevcut satırını bir Parca nesnesine dönüştüren yardımcı metot.
     *
     * @param rs Sonuç kümesi.
     * @return Oluşturulan Parca nesnesi.
     * @throws SQLException Okuma hatası olursa.
     */
    private Parca mapResultSetToParca(ResultSet rs) throws SQLException {
        return new Parca(
                rs.getInt("id"),
                rs.getString("ad"),
                rs.getInt("marka_id"),
                rs.getInt("model_id"),
                rs.getInt("tip_id"),
                rs.getDouble("alis_fiyat"),
                rs.getDouble("satis_fiyat")
        );
    }

    /**
     * Stok listesi görünümü için gerekli verileri hesaplar.
     *
     * @return StokGorunum nesnelerinin listesi.
     */
    public List<StokGorunum> getStokGorunumListesi() {
        List<StokGorunum> stokListesi = new ArrayList<>();
        String sql = """
            SELECT
                p.id, p.ad AS parcaAdi, ma.ad AS markaAdi, mo.ad AS modelAdi, ti.ad AS tipAdi,
                p.alis_fiyat, p.satis_fiyat,
                COALESCE((SELECT SUM(adet) FROM stok_hareketleri WHERE parca_id = p.id AND hareket_tipi = 'giris'), 0) -
                COALESCE((SELECT SUM(adet) FROM stok_hareketleri WHERE parca_id = p.id AND hareket_tipi = 'cikis'), 0) AS stokAdet
            FROM parcalar p
            LEFT JOIN markalar ma ON p.marka_id = ma.id
            LEFT JOIN modeller mo ON p.model_id = mo.id
            LEFT JOIN tipler ti ON p.tip_id = ti.id
            ORDER BY p.id ASC
            """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stokListesi.add(new StokGorunum(
                    rs.getInt("id"), rs.getString("parcaAdi"),
                    rs.getString("markaAdi") != null ? rs.getString("markaAdi") : "-",
                    rs.getString("modelAdi") != null ? rs.getString("modelAdi") : "-",
                    rs.getString("tipAdi") != null ? rs.getString("tipAdi") : "-",
                    rs.getDouble("alis_fiyat"), rs.getDouble("satis_fiyat"),
                    rs.getInt("stokAdet")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Stok görünüm listesi alınırken veritabanı hatası: " + e.getMessage());
        }
        return stokListesi;
    }

    // --- YENİ EKLENEN METOTLAR ---

    /**
     * Veritabanındaki bir parçanın bilgilerini günceller.
     * Bu versiyonda sadece ad, alış fiyatı ve satış fiyatı güncellenir.
     * Marka/Model/Tip değişikliği desteklenmemektedir.
     *
     * @param parca Güncellenecek bilgileri içeren Parca nesnesi (ID'si dolu olmalı).
     * @return Güncelleme başarılı ise true, değilse false döner.
     */
    public boolean updateParca(Parca parca) {
        if (parca == null || parca.getId() <= 0) {
            System.err.println("Güncellenecek parça veya ID geçersiz.");
            return false;
        }
        // Girdi kontrolleri
        if (parca.getAd() == null || parca.getAd().trim().isEmpty()) {
            System.err.println("Parça adı boş olamaz.");
            return false;
        }
        if (parca.getAlisFiyat() < 0 || parca.getSatisFiyat() < 0) {
            System.err.println("Fiyatlar negatif olamaz.");
            return false;
        }

        // Sadece ad, alis_fiyat ve satis_fiyat güncelleniyor
        String sql = "UPDATE parcalar SET ad = ?, alis_fiyat = ?, satis_fiyat = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, parca.getAd().trim());
            pstmt.setDouble(2, parca.getAlisFiyat());
            pstmt.setDouble(3, parca.getSatisFiyat());
            pstmt.setInt(4, parca.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            // Parçalar tablosunda ad için UNIQUE kısıtlaması yoktu.
            System.err.println("Parça güncellenirken veritabanı hatası: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verilen ID'ye sahip parçayı veritabanından siler.
     * Dikkat: Bu parçaya ait stok hareketleri de silinir (ON DELETE CASCADE).
     * Bu parçanın kullanıldığı servis kayıtlarında parca_id NULL olur (ON DELETE SET NULL).
     *
     * @param id Silinecek parçanın ID'si.
     * @return Silme başarılı ise true, değilse false döner.
     */
    public boolean deleteParca(int id) {
        if (id <= 0) {
            System.err.println("Geçersiz parça ID.");
            return false;
        }
        String sql = "DELETE FROM parcalar WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
             // Normalde CASCADE ve SET NULL nedeniyle FK hatası gelmemeli,
             // ama başka bir veritabanı hatası olabilir.
            System.err.println("Parça silinirken veritabanı hatası: " + e.getMessage());
            return false;
        }
    }

}