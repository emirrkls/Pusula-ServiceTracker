package com.pusulaiklimlendirme;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection; // Eklendi
import java.sql.SQLException; // Eklendi

public class App extends Application {

    // ... (start, loadFXML metotları aynı) ...
     @Override
    public void start(Stage stage) throws IOException {
        // Ana FXML dosyamızı yükle
        Scene scene = new Scene(loadFXML("MainView"), 1400, 800);
        stage.setTitle("Beyaz Eşya Servis ve Stok Takip Sistemi v1.0 (JavaFX)");
        stage.setScene(scene);
        stage.show();

                try {
            // İkonu kaynaklardan (resources) yükle
            InputStream iconStream = getClass().getResourceAsStream("/icons/app.png"); // PNG dosyasının adı ve yolu
            if (iconStream != null) {
                Image applicationIcon = new Image(iconStream);
                stage.getIcons().add(applicationIcon);
                 System.out.println("Uygulama ikonu başarıyla yüklendi.");
            } else {
                System.err.println("Hata: Uygulama ikonu bulunamadı! (/icons/app.png)");
            }
        } catch (Exception e) {
            System.err.println("Uygulama ikonu yüklenirken hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
        // --- İkon Ayarlama Sonu ---

        stage.setTitle("Beyaz Eşya Servis ve Stok Takip Sistemi v1.0 (JavaFX)");
        stage.setScene(scene);
        stage.show();
        
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Uygulama kapatılıyor...");
        // DatabaseManager.closeConnection(); // BU SATIR KALDIRILDI
        super.stop();
    }


    public static void main(String[] args) {
        // Uygulama başlangıcında tabloları oluşturmayı dene
        try (Connection initialConnection = DatabaseManager.getConnection()) {
            if (initialConnection == null) {
                 System.err.println("Kritik Hata: Veritabanı bağlantısı kurulamadı. Uygulama başlatılamıyor.");
                 return; // Başlatmayı durdur
            }
            // Bağlantı başarılıysa tabloları kontrol et/oluştur
            DatabaseManager.createTablesIfNotExists(initialConnection);
            // initialConnection try-with-resources sayesinde burada otomatik kapanacak.
             System.out.println("Veritabanı ve tablolar hazır.");

        } catch (SQLException e) {
             // getConnection içinde hata zaten loglanır ama burada da loglayabiliriz.
             System.err.println("Veritabanı ilk bağlantı veya tablo oluşturma sırasında kritik hata: " + e.getMessage());
             return; // Başlatmayı durdur
        }

         System.out.println("Uygulama başlatılıyor...");
        launch(args); // launch() metoduna args geçirmek iyi bir pratiktir.
    }
}