package com.pusulaiklimlendirme; // veya .dao

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types; // setNull için
import java.util.ArrayList;
import java.util.List;

/**
 * Musteri nesneleri için Veritabanı Erişim Nesnesi (DAO).
 * Musteriler tablosu üzerinde CRUD işlemleri yapar.
 */
public class MusteriDAO {

    /**
     * Veritabanına yeni bir müşteri ekler.
     *
     * @param musteri Eklenecek Müşteri nesnesi (ID'si genellikle 0 veya null olur).
     * @return Ekleme başarılı ise true, değilse false döner.
     */
    public boolean addMusteri(Musteri musteri) {
        if (musteri == null || musteri.getAdSoyad() == null || musteri.getAdSoyad().trim().isEmpty() ||
            musteri.getTelefon() == null || musteri.getTelefon().trim().isEmpty()) {
            System.err.println("Müşteri adı/soyadı ve telefon boş olamaz.");
            return false;
        }
        String sql = "INSERT INTO musteriler(ad_soyad, telefon, adres) VALUES(?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, musteri.getAdSoyad().trim());
            pstmt.setString(2, musteri.getTelefon().trim());
            if (musteri.getAdres() != null && !musteri.getAdres().trim().isEmpty()) { // Adres boş değilse ekle
                pstmt.setString(3, musteri.getAdres().trim());
            } else {
                pstmt.setNull(3, Types.VARCHAR);
            }
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Müşteri eklerken veritabanı hatası: " + e.getMessage());
            return false;
        }
    }

    /**
     * Veritabanındaki tüm müşterileri soyada göre listeler.
     *
     * @return Musteri nesnelerinin listesi.
     */
    public List<Musteri> getAllMusteriler() {
        List<Musteri> musteriler = new ArrayList<>();
        String sql = "SELECT id, ad_soyad, telefon, adres FROM musteriler ORDER BY ad_soyad ASC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                musteriler.add(mapResultSetToMusteri(rs)); // Yardımcı metot kullanımı
            }
        } catch (SQLException e) {
            System.err.println("Müşterileri alırken veritabanı hatası: " + e.getMessage());
        }
        return musteriler;
    }

     /**
     * ID'sine göre belirli bir müşteriyi getirir.
     *
     * @param id Aranacak müşterinin ID'si.
     * @return Müşteri nesnesi veya null.
     */
    public Musteri getMusteriById(int id) {
        String sql = "SELECT id, ad_soyad, telefon, adres FROM musteriler WHERE id = ?";
        Musteri musteri = null;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    musteri = mapResultSetToMusteri(rs); // Yardımcı metot kullanımı
                }
            }
        } catch (SQLException e) {
            System.err.println("Müşteri ID ile alınırken hata: " + e.getMessage());
        }
        return musteri;
    }

    /**
     * En son eklenen müşterinin ID'sini döndürür. (Güvenilirlik notu geçerlidir)
     *
     * @return Son ID veya -1.
     */
    public int getLastMusteriId() {
        String sql = "SELECT id FROM musteriler ORDER BY id DESC LIMIT 1";
        int lastId = -1;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                lastId = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Son müşteri ID alınırken hata: " + e.getMessage());
        }
        return lastId;
    }

    // --- YENİ EKLENEN METOTLAR ---

    /**
     * Veritabanındaki bir müşterinin bilgilerini günceller.
     *
     * @param musteri Güncellenecek bilgileri içeren Musteri nesnesi (ID'si dolu olmalı).
     * @return Güncelleme başarılı ise true, değilse false döner.
     */
    public boolean updateMusteri(Musteri musteri) {
        if (musteri == null || musteri.getId() <= 0) {
            System.err.println("Güncellenecek müşteri veya ID geçersiz.");
            return false;
        }
        // Girdi kontrolü
        if (musteri.getAdSoyad() == null || musteri.getAdSoyad().trim().isEmpty() ||
            musteri.getTelefon() == null || musteri.getTelefon().trim().isEmpty()) {
            System.err.println("Müşteri adı/soyadı ve telefon boş olamaz.");
            return false;
        }

        String sql = "UPDATE musteriler SET ad_soyad = ?, telefon = ?, adres = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, musteri.getAdSoyad().trim());
            pstmt.setString(2, musteri.getTelefon().trim());
            if (musteri.getAdres() != null && !musteri.getAdres().trim().isEmpty()) {
                pstmt.setString(3, musteri.getAdres().trim());
            } else {
                pstmt.setNull(3, Types.VARCHAR);
            }
            pstmt.setInt(4, musteri.getId()); // WHERE koşulu için ID

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0; // 1 satır etkilendiyse başarılı

        } catch (SQLException e) {
            System.err.println("Müşteri güncellenirken veritabanı hatası: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verilen ID'ye sahip müşteriyi veritabanından siler.
     * Dikkat: Bu müşteriye ait servis kayıtları da silinir (ON DELETE CASCADE).
     *
     * @param id Silinecek müşterinin ID'si.
     * @return Silme başarılı ise true, değilse false döner.
     */
    public boolean deleteMusteri(int id) {
        if (id <= 0) {
            System.err.println("Geçersiz müşteri ID.");
            return false;
        }
        String sql = "DELETE FROM musteriler WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
             // Normalde CASCADE nedeniyle FK hatası gelmemeli,
             // ama başka bir veritabanı hatası olabilir.
            System.err.println("Müşteri silinirken veritabanı hatası: " + e.getMessage());
            return false;
        }
    }

    // --- YARDIMCI METOT ---

    /**
     * ResultSet'in mevcut satırını bir Musteri nesnesine dönüştürür.
     * Kod tekrarını azaltır.
     *
     * @param rs Sonuç kümesi.
     * @return Oluşturulan Musteri nesnesi.
     * @throws SQLException Okuma hatası olursa.
     */
    private Musteri mapResultSetToMusteri(ResultSet rs) throws SQLException {
        return new Musteri(
                rs.getInt("id"),
                rs.getString("ad_soyad"),
                rs.getString("telefon"),
                rs.getString("adres") // getString null dönebilir, sorun değil
        );
    }
}