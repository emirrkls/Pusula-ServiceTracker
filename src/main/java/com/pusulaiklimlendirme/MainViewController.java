package com.pusulaiklimlendirme;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
// Diğer Controller sınıflarını import et
import com.pusulaiklimlendirme.KategoriViewController;
import com.pusulaiklimlendirme.StokViewController;
import com.pusulaiklimlendirme.MusteriServisViewController;

public class MainViewController {

    // Ana TabPane
    @FXML private TabPane mainTabPane;

    // Sekmeler (Tablar) - Bunların fx:id'leri FXML'deki ile eşleşmeli
    @FXML private Tab tabMusteriServis;
    @FXML private Tab tabStokYonetimi;
    @FXML private Tab tabKategoriler;

    // Dahil Edilen FXML'lerin Controller'ları için Alanlar
    // İsimlendirme: <fx:include fx:id="xxx"> ise alan adı "xxxController" olur.
    // Bu alanlara FXML Loader otomatik olarak ilgili Controller nesnesini enjekte eder.
    @FXML private MusteriServisViewController musteriServisViewController;
    @FXML private StokViewController stokViewController;
    @FXML private KategoriViewController kategoriViewController;

    // FXML yüklendikten sonra otomatik çağrılan metot
    @FXML
    private void initialize() {
        System.out.println("MainViewController başlatıldı (Sekme Listener ile)!");

        // Sekme seçimi değişikliğini dinleyen bir listener ekle
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null) {
                System.out.println("Aktif sekme: " + newTab.getText());
                // Seçilen yeni sekmeye göre ilgili Controller'ın refresh metodunu çağır
                // Controller'ın null olup olmadığını kontrol etmek iyi bir pratiktir.
                if (newTab == tabStokYonetimi && stokViewController != null) {
                    stokViewController.refreshData();
                } else if (newTab == tabMusteriServis && musteriServisViewController != null) {
                    musteriServisViewController.refreshData();
                } else if (newTab == tabKategoriler && kategoriViewController != null) {
                    kategoriViewController.refreshData();
                }
                // Başka sekmeler varsa buraya eklenir
            }
        });

        // İsteğe Bağlı: Uygulama ilk açıldığında seçili olan sekmenin verisini yükle
        // Bu, initialize içinde zaten yapılıyor olabilir ama garantiye almak için:
        Tab initialTab = mainTabPane.getSelectionModel().getSelectedItem();
        if (initialTab != null) {
             handleTabChange(initialTab); // İlk sekme için de refresh yap
        } else if (!mainTabPane.getTabs().isEmpty()){
             // Eğer başlangıçta seçili sekme yoksa ama sekmeler varsa, ilk sekmeyi seç ve refresh yap
             mainTabPane.getSelectionModel().selectFirst();
             // selectFirst sonrası listener otomatik tetiklenir ve refresh yapar.
             // handleTabChange(mainTabPane.getTabs().get(0));
        }
    }

    /**
     * Sekme değişikliğini ele alan yardımcı metot (isteğe bağlı).
     * Kod tekrarını azaltmak için kullanılabilir.
     * @param selectedTab Seçilen yeni sekme.
     */
    private void handleTabChange(Tab selectedTab) {
         if (selectedTab == tabStokYonetimi && stokViewController != null) {
            stokViewController.refreshData();
        } else if (selectedTab == tabMusteriServis && musteriServisViewController != null) {
            musteriServisViewController.refreshData();
        } else if (selectedTab == tabKategoriler && kategoriViewController != null) {
            kategoriViewController.refreshData();
        }
    }

    // İleride diğer metotlar eklenebilir... (Örn: Çıkış, Hakkında vb.)
}