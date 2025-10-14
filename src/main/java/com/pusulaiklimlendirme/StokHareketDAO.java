package com.pusulaiklimlendirme;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * StokHareketi nesneleri için Veritabanı Erişim Nesnesi (DAO).
 * Stok_hareketleri tablosu üzerinde işlem yapar ve stok miktarlarını hesaplar.
 * Transaction yönetimi için Connection alan metotlar içerir.
 */
public class StokHareketDAO {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD

    // --- Transaction İçinde Kullanılacak Metotlar (Connection Alan) ---

    /**
     * Dışarıdan sağlanan bir bağlantı ile stok adedini hesaplar.
     * Transaction içinde tutarlı stok kontrolü için kullanılır.
     *
     * @param conn Kullanılacak veritabanı bağlantısı.
     * @param parcaId Stok adedi öğrenilmek istenen parçanın ID'si.
     * @return Parçanın mevcut stok adedi.
     * @throws SQLException Bağlantı veya SQL hatası olursa.
     */
    public int getParcaStokAdet(Connection conn, int parcaId) throws SQLException {
        if (conn == null) {
            throw new SQLException("Stok adedi almak için geçerli bağlantı sağlanmadı.");
        }
        int stokAdet = 0;
        String sql = """
            SELECT
                COALESCE(SUM(CASE WHEN hareket_tipi = 'giris' THEN adet ELSE 0 END), 0) -
                COALESCE(SUM(CASE WHEN hareket_tipi = 'cikis' THEN adet ELSE 0 END), 0) AS stok
            FROM stok_hareketleri
            WHERE parca_id = ?
            """;

        // Gelen bağlantıyı kullan, try-with-resources PreparedStatement için
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parcaId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stokAdet = rs.getInt("stok");
                }
            }
        }
        // SQLException olursa metot dışına fırlatılır
        return stokAdet;
    }

    /**
     * Dışarıdan sağlanan bir bağlantı ile yeni bir stok hareketi ekler.
     * Transaction yönetimi bu metodu çağıran yer tarafından yapılır.
     * Stok kontrolünü de aynı bağlantı üzerinden yapar.
     *
     * @param conn Kullanılacak veritabanı bağlantısı.
     * @param parcaId Hareket gören parçanın ID'si.
     * @param adet Hareket adedi (pozitif olmalı).
     * @param hareketTipi Hareket tipi ("giris" veya "cikis").
     * @return Ekleme başarılı ise true, değilse false döner.
     * @throws SQLException Bağlantı, SQL hatası veya yetersiz stok durumunda fırlatılır.
     */
    public boolean addStokHareketi(Connection conn, int parcaId, int adet, String hareketTipi) throws SQLException {
        if (conn == null) {
            throw new SQLException("Stok hareketi eklemek için geçerli bağlantı sağlanmadı.");
        }
        // Girdi Kontrolleri (Metodu çağıranın sorumluluğu veya burada da yapılabilir)
        if (parcaId <= 0 || adet <= 0 || (!"giris".equalsIgnoreCase(hareketTipi) && !"cikis".equalsIgnoreCase(hareketTipi))) {
             throw new IllegalArgumentException("Geçersiz parça ID, adet veya hareket tipi.");
        }


        // Stok çıkışı yapılacaksa, AYNI BAĞLANTIYI kullanarak yeterli stok var mı kontrol et
        if ("cikis".equalsIgnoreCase(hareketTipi)) {
            int mevcutStok = getParcaStokAdet(conn, parcaId); // Mevcut bağlantı ile stoğu al

            // DEBUG MESAJI (İsteğe Bağlı)
            System.out.println(">>> DEBUG [StokHareketDAO - Transaction İçi]: Stok çıkış kontrolü. Parça ID: " + parcaId +
                               ", İstenen Adet: " + adet + ", Hesaplanan Mevcut Stok: " + mevcutStok);

            if (adet > mevcutStok) {
                // Yetersiz stok durumunda SQLException fırlat, transaction rollback edilsin
                throw new SQLException("Yetersiz stok! Parça ID: " + parcaId + ", Mevcut: " + mevcutStok + ", Çıkış istenen: " + adet);
            }
        }

        // Stok Hareketi Ekleme
        String sql = "INSERT INTO stok_hareketleri(parca_id, adet, hareket_tipi, tarih) VALUES(?, ?, ?, ?)";
        String bugununTarihiStr = LocalDate.now().format(DATE_FORMATTER);

        // Gelen bağlantıyı kullan, try-with-resources PreparedStatement için
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parcaId);
            pstmt.setInt(2, adet);
            pstmt.setString(3, hareketTipi.toLowerCase());
            pstmt.setString(4, bugununTarihiStr);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
        // SQLException olursa metot dışına fırlatılır
    }

    // --- Transaction Dışında Kullanılacak Metotlar (Kendi Bağlantısını Açan) ---

    /**
     * Kendi bağlantısını açarak belirli bir parçanın mevcut stok adedini hesaplar.
     *
     * @param parcaId Stok adedi öğrenilmek istenen parçanın ID'si.
     * @return Parçanın mevcut stok adedi. Hata durumunda 0 döner.
     */
    public int getParcaStokAdet(int parcaId) {
        try (Connection conn = DatabaseManager.getConnection()) {
             if (conn == null) return 0; // Bağlantı hatası
            // Connection alan metodu çağır
            return getParcaStokAdet(conn, parcaId);
        } catch (SQLException e) {
            System.err.println("Parça stok adedi alınırken (kendi bağlantısı) hata (Parça ID: " + parcaId + "): " + e.getMessage());
            return 0; // Hata durumunda 0 dön
        }
    }


    /**
     * Kendi bağlantısını açarak yeni bir stok hareketi ekler (giriş veya çıkış).
     * Transaction gerektirmeyen basit stok hareketleri için kullanılır.
     *
     * @param parcaId Hareket gören parçanın ID'si.
     * @param adet Hareket adedi (pozitif olmalı).
     * @param hareketTipi Hareket tipi ("giris" veya "cikis").
     * @return Ekleme başarılı ise true, değilse false döner.
     */
    public boolean addStokHareketi(int parcaId, int adet, String hareketTipi) {
        // Girdi Kontrolleri
        if (parcaId <= 0) {
            System.err.println("Geçersiz parça ID.");
            return false;
        }
        if (adet <= 0) {
            System.err.println("Stok hareket adedi pozitif olmalıdır.");
            return false;
        }
        if (!"giris".equalsIgnoreCase(hareketTipi) && !"cikis".equalsIgnoreCase(hareketTipi)) {
            System.err.println("Geçersiz hareket tipi: " + hareketTipi + ". Sadece 'giris' veya 'cikis' olabilir.");
            return false;
        }

        // Kendi bağlantısını açıp kapatacak şekilde try-with-resources kullan
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return false; // Bağlantı hatası

            // Connection alan metodu çağır
            // Bu metot SQLException fırlatabilir, onu yakalamalıyız
            return addStokHareketi(conn, parcaId, adet, hareketTipi);

        } catch (SQLException e) {
            // Hata mesajı Connection alan metot içinde loglanmış veya fırlatılmış olabilir.
            // Yetersiz stok hatası da buraya SQLException olarak gelecek.
            System.err.println("Stok hareketi eklerken (kendi bağlantısı) genel hata/yetersiz stok: " + e.getMessage());
            return false;
        }
    }

    // İleride Gerekebilecek Metotlar...
}