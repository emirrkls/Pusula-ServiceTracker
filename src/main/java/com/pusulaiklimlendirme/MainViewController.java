package com.pusulaiklimlendirme;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import com.pusulaiklimlendirme.KategoriViewController;
import com.pusulaiklimlendirme.StokViewController;
import com.pusulaiklimlendirme.MusteriServisViewController;

public class MainViewController {

    // Main tabs
    @FXML private TabPane mainTabPane;

    @FXML private Tab tabMusteriServis;
    @FXML private Tab tabStokYonetimi;
    @FXML private Tab tabKategoriler;

    // Controllers for the included FXML files. These are injected automatically by the FXMLLoader.
    @FXML private MusteriServisViewController musteriServisViewController;
    @FXML private StokViewController stokViewController;
    @FXML private KategoriViewController kategoriViewController;

    @FXML
    private void initialize() {
        System.out.println("MainViewController başlatıldı (Sekme Listener ile)!");

        // Add a listener to handle tab selection changes
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null) {
                System.out.println("Aktif sekme: " + newTab.getText());
                // When a new tab is selected, refresh its data. A null check.
                if (newTab == tabStokYonetimi && stokViewController != null) {
                    stokViewController.refreshData();
                } else if (newTab == tabMusteriServis && musteriServisViewController != null) {
                    musteriServisViewController.refreshData();
                } else if (newTab == tabKategoriler && kategoriViewController != null) {
                    kategoriViewController.refreshData();
                }
            }
        });

        // Optional: Load data for the initially selected tab on startup.
        Tab initialTab = mainTabPane.getSelectionModel().getSelectedItem();
        if (initialTab != null) {
             handleTabChange(initialTab);
        } else if (!mainTabPane.getTabs().isEmpty()){
             mainTabPane.getSelectionModel().selectFirst();
        }
    }

    /** Helper method to handle tab changes. Can be used to reduce code duplication. @param selectedTab The newly selected tab. */
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