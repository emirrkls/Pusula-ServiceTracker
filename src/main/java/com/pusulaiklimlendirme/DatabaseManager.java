package com.pusulaiklimlendirme;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

public class DatabaseManager {

    private static final String DB_FILENAME = "servis.db";
    private static final String DB_URL = getDatabaseUrl();

    private DatabaseManager() {
        // Utility class
    }

    private static String getDatabaseUrl() {
        try {
            String userHome = System.getProperty("user.home");
            if (userHome == null) {
                System.err.println("Kritik Hata: Kullanıcı ev dizini alınamadı.");
                return null;
            }
            Path appDataDir = Paths.get(userHome, ".PusulaServisTakip");
            if (!Files.exists(appDataDir)) {
                Files.createDirectories(appDataDir);
                System.out.println("Uygulama veri klasörü oluşturuldu: " + appDataDir);
            } else if (!Files.isDirectory(appDataDir)) {
                System.err.println("Hata: Uygulama veri yolu bir klasör değil: " + appDataDir);
                return null;
            }
            Path dbFilePath = appDataDir.resolve(DB_FILENAME);
            String jdbcUrl = "jdbc:sqlite:" + dbFilePath.toString();
            System.out.println("Veritabanı URL'si olarak ayarlandı: " + jdbcUrl);
            return jdbcUrl;

        // DÜZELTİLMİŞ CATCH BLOĞU: SecurityException kaldırıldı
        } catch (IOException | RuntimeException e) {
            System.err.println("Veritabanı URL'si belirlenirken hata: " + e.getMessage());
            // SecurityException zaten RuntimeException tarafından yakalanır.
            e.printStackTrace();
            return null;
        }
        // Not: Exception'ı ayrıca yakalamaya gerek kalmadı, RuntimeException çoğu beklenmedik durumu kapsar.
        // catch (Exception e) { ... }
    }

    public static Connection getConnection() {
        if (DB_URL == null) {
             System.err.println("Veritabanı URL'si alınamadığı için bağlantı kurulamıyor.");
             return null;
        }
        try {
            Connection newConnection = DriverManager.getConnection(DB_URL);
            try (Statement stmt = newConnection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            } catch (SQLException pragmaEx) {
                 System.err.println("Uyarı: PRAGMA foreign_keys=ON ayarlanamadı. " + pragmaEx.getMessage());
            }
            return newConnection;
        } catch (SQLException e) {
            System.err.println("Veritabanı bağlantı hatası ("+ DB_URL +"): " + e.getMessage());
            return null;
        }
    }

    public static void createTablesIfNotExists(Connection conn) {
        if (conn == null) {
            System.err.println("Tablo oluşturmak için geçerli bağlantı sağlanmadı.");
            return;
        }

       // SQL Sorguları (servis_kayitlari güncellendi)
       String sqlMarkalar = """
               CREATE TABLE IF NOT EXISTS markalar (
                   id INTEGER PRIMARY KEY AUTOINCREMENT,
                   ad TEXT UNIQUE NOT NULL
               )""";
       String sqlModeller = """
               CREATE TABLE IF NOT EXISTS modeller (
                   id INTEGER PRIMARY KEY AUTOINCREMENT,
                   marka_id INTEGER NOT NULL,
                   ad TEXT NOT NULL,
                   UNIQUE(marka_id, ad),
                   FOREIGN KEY(marka_id) REFERENCES markalar(id) ON DELETE CASCADE
               )""";
       String sqlTipler = """
               CREATE TABLE IF NOT EXISTS tipler (
                   id INTEGER PRIMARY KEY AUTOINCREMENT,
                   ad TEXT UNIQUE NOT NULL
               )""";
       String sqlMusteriler = """
               CREATE TABLE IF NOT EXISTS musteriler (
                   id INTEGER PRIMARY KEY AUTOINCREMENT,
                   ad_soyad TEXT NOT NULL,
                   telefon TEXT NOT NULL,
                   adres TEXT
               )""";
       String sqlParcalar = """
               CREATE TABLE IF NOT EXISTS parcalar (
                   id INTEGER PRIMARY KEY AUTOINCREMENT,
                   ad TEXT NOT NULL,
                   marka_id INTEGER NOT NULL,
                   model_id INTEGER NOT NULL,
                   tip_id INTEGER NOT NULL,
                   alis_fiyat REAL NOT NULL,
                   satis_fiyat REAL NOT NULL,
                   FOREIGN KEY(marka_id) REFERENCES markalar(id) ON DELETE RESTRICT,
                   FOREIGN KEY(model_id) REFERENCES modeller(id) ON DELETE RESTRICT,
                   FOREIGN KEY(tip_id) REFERENCES tipler(id) ON DELETE RESTRICT
               )""";
       String sqlStokHareketleri = """
               CREATE TABLE IF NOT EXISTS stok_hareketleri (
                   id INTEGER PRIMARY KEY AUTOINCREMENT,
                   parca_id INTEGER NOT NULL,
                   adet INTEGER NOT NULL,
                   hareket_tipi TEXT CHECK(hareket_tipi IN ('giris', 'cikis')) NOT NULL,
                   tarih TEXT NOT NULL,
                   FOREIGN KEY(parca_id) REFERENCES parcalar(id) ON DELETE CASCADE
               )""";
       String sqlServisKayitlari = """
               CREATE TABLE IF NOT EXISTS servis_kayitlari (
                   id INTEGER PRIMARY KEY AUTOINCREMENT,
                   musteri_id INTEGER NOT NULL,
                   tarih TEXT NOT NULL,
                   islem TEXT NOT NULL,
                   parca_id INTEGER,
                   adet INTEGER,
                   iscilik REAL NOT NULL DEFAULT 0,
                   odeme_tutari REAL NOT NULL DEFAULT 0,
                   kar REAL NOT NULL DEFAULT 0,
                   FOREIGN KEY(musteri_id) REFERENCES musteriler(id) ON DELETE CASCADE,
                   FOREIGN KEY(parca_id) REFERENCES parcalar(id) ON DELETE SET NULL
               )""";

       System.out.println("Tablo oluşturma/kontrol işlemi başlıyor...");
       try (Statement stmt = conn.createStatement()) {
           stmt.execute(sqlMarkalar);
           stmt.execute(sqlTipler);
           stmt.execute(sqlModeller);
           stmt.execute(sqlMusteriler);
           stmt.execute(sqlParcalar);
           stmt.execute(sqlStokHareketleri);
           stmt.execute(sqlServisKayitlari);
           System.out.println("Tüm tablolar başarıyla kontrol edildi/oluşturuldu.");
       } catch (SQLException e) {
           System.err.println("!!! Tablo oluşturma sırasında HATA oluştu: " + e.getMessage());
           e.printStackTrace();
       }
   }
}