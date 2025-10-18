package com.pusulaiklimlendirme;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

public class App extends Application {

     @Override
    public void start(Stage stage) throws IOException {
        Scene scene = new Scene(loadFXML("MainView"), 1400, 800);
        stage.setTitle("Beyaz Eşya Servis ve Stok Takip Sistemi v1.0 (JavaFX)");
        stage.setScene(scene);
        stage.show();

                try {
            // Load the application icon from resources
            InputStream iconStream = getClass().getResourceAsStream("/icons/app.png");
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
        super.stop();
    }


    public static void main(String[] args) {
        // Initialize the database tables on application startup
        try (Connection initialConnection = DatabaseManager.getConnection()) {
            if (initialConnection == null) {
                 System.err.println("Kritik Hata: Veritabanı bağlantısı kurulamadı. Uygulama başlatılamıyor.");
                 return; // Stop the launch
            }
            // If connection is successful, check/create tables
            DatabaseManager.createTablesIfNotExists(initialConnection);
            // initialConnection will be closed automatically here thanks to try-with-resources.
             System.out.println("Veritabanı ve tablolar hazır.");

        } catch (SQLException e) {
             System.err.println("Veritabanı ilk bağlantı veya tablo oluşturma sırasında kritik hata: " + e.getMessage());
             return; // Stop the launch
        }

         System.out.println("Uygulama başlatılıyor...");
        launch(args);
    }
}