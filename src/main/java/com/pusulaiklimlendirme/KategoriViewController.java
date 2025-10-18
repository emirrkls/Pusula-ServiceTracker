package com.pusulaiklimlendirme;

import com.pusulaiklimlendirme.Marka;
import com.pusulaiklimlendirme.Model;
import com.pusulaiklimlendirme.Tip;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton; // For double click

import java.util.List;
import java.util.Optional;

public class KategoriViewController {

    @FXML private TableView<Marka> tblMarkalar;
    @FXML private TableColumn<Marka, Integer> colMarkaId;
    @FXML private TableColumn<Marka, String> colMarkaAd;
    @FXML private Button btnMarkaYeni;
    @FXML private Button btnMarkaDuzenle;
    @FXML private Button btnMarkaSil;

    @FXML private ComboBox<Marka> cmbModelMarkaFilter;
    @FXML private TableView<Model> tblModeller;
    @FXML private TableColumn<Model, Integer> colModelId;
    @FXML private TableColumn<Model, Integer> colModelMarkaId;
    @FXML private TableColumn<Model, String> colModelAd;
    @FXML private Button btnModelYeni;
    @FXML private Button btnModelDuzenle;
    @FXML private Button btnModelSil;

    @FXML private TableView<Tip> tblTipler;
    @FXML private TableColumn<Tip, Integer> colTipId;
    @FXML private TableColumn<Tip, String> colTipAd;
    @FXML private Button btnTipYeni;
    @FXML private Button btnTipDuzenle;
    @FXML private Button btnTipSil;

    private final MarkaDAO markaDAO = new MarkaDAO();
    private final ModelDAO modelDAO = new ModelDAO();
    private final TipDAO tipDAO = new TipDAO();

    private ObservableList<Marka> markaList = FXCollections.observableArrayList();
    private ObservableList<Model> modelList = FXCollections.observableArrayList();
    private ObservableList<Tip> tipList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        System.out.println("KategoriViewController başlatıldı (Model Filtre ve Refresh ile)!");

        configureTableColumns();

        tblMarkalar.setItems(markaList);
        tblModeller.setItems(modelList);
        tblTipler.setItems(tipList);
        cmbModelMarkaFilter.setItems(markaList);

        refreshData();

        addSelectionListeners();
        addDoubleClickListeners();
        addMarkaFilterListener();

        updateButtonStates();
    }


    private void configureTableColumns() {
        colMarkaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMarkaAd.setCellValueFactory(new PropertyValueFactory<>("ad"));

        colModelId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colModelMarkaId.setCellValueFactory(new PropertyValueFactory<>("markaId"));
        colModelAd.setCellValueFactory(new PropertyValueFactory<>("ad"));

        colTipId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTipAd.setCellValueFactory(new PropertyValueFactory<>("ad"));
    }

    private void addSelectionListeners() {
        tblMarkalar.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> updateButtonStates());
        tblModeller.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> updateButtonStates());
        tblTipler.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> updateButtonStates());
    }

     private void addDoubleClickListeners() {
        tblMarkalar.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2 && tblMarkalar.getSelectionModel().getSelectedItem() != null) {
                handleMarkaDuzenle();
            }
        });
        tblModeller.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2 && tblModeller.getSelectionModel().getSelectedItem() != null) {
                handleModelDuzenle();
            }
        });
         tblTipler.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2 && tblTipler.getSelectionModel().getSelectedItem() != null) {
                handleTipDuzenle();
            }
        });
    }

    private void addMarkaFilterListener() {
        cmbModelMarkaFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadModelTable(newVal != null ? newVal.getId() : null);
            tblModeller.getSelectionModel().clearSelection();
        });
    }

    private void updateButtonStates() {
        boolean markaSecili = tblMarkalar.getSelectionModel().getSelectedItem() != null;
        boolean filtreSecili = cmbModelMarkaFilter.getValue() != null;
        btnMarkaDuzenle.setDisable(!markaSecili);
        btnMarkaSil.setDisable(!markaSecili);
        btnModelYeni.setDisable(!filtreSecili && !markaSecili);

        boolean modelSecili = tblModeller.getSelectionModel().getSelectedItem() != null;
        btnModelDuzenle.setDisable(!modelSecili);
        btnModelSil.setDisable(!modelSecili);

        boolean tipSecili = tblTipler.getSelectionModel().getSelectedItem() != null;
        btnTipDuzenle.setDisable(!tipSecili);
        btnTipSil.setDisable(!tipSecili);
    }


    private void loadMarkaTable() {
        Marka seciliFiltre = cmbModelMarkaFilter.getValue();
        Marka seciliTablo = tblMarkalar.getSelectionModel().getSelectedItem();

        markaList.setAll(markaDAO.getAllMarkalar());

        if (seciliFiltre != null) {
             cmbModelMarkaFilter.setValue(markaList.stream().filter(m -> m.getId() == seciliFiltre.getId()).findFirst().orElse(null));
        }
        if (seciliTablo != null) {
             tblMarkalar.getSelectionModel().select(markaList.stream().filter(m -> m.getId() == seciliTablo.getId()).findFirst().orElse(null));
        }
        if (tblMarkalar.getSelectionModel().getSelectedItem() == null) {
            tblMarkalar.getSelectionModel().clearSelection();
        }
    }

    private void loadModelTable(Integer markaId) {
        Model seciliModel = tblModeller.getSelectionModel().getSelectedItem();
        modelList.clear();
        if (markaId != null && markaId > 0) {
            modelList.setAll(modelDAO.getModellerByMarkaId(markaId));
        }
        if (seciliModel != null) {
             tblModeller.getSelectionModel().select(modelList.stream().filter(m -> m.getId() == seciliModel.getId()).findFirst().orElse(null));
        }
        if (tblModeller.getSelectionModel().getSelectedItem() == null) {
            tblModeller.getSelectionModel().clearSelection();
        }
    }

     private void loadTipTable() {
        Tip seciliTip = tblTipler.getSelectionModel().getSelectedItem();
        tipList.setAll(tipDAO.getAllTipler());
         if (seciliTip != null) {
             tblTipler.getSelectionModel().select(tipList.stream().filter(t -> t.getId() == seciliTip.getId()).findFirst().orElse(null));
         }
        if (tblTipler.getSelectionModel().getSelectedItem() == null) {
             tblTipler.getSelectionModel().clearSelection();
        }
    }


    @FXML
    private void handleMarkaYeni() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Yeni Marka Ekle");
        dialog.setHeaderText("Eklenecek markanın adını girin:");
        dialog.setContentText("Marka Adı:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(markaAdi -> {
            if (markaAdi.trim().isEmpty()) { showAlert(AlertType.WARNING, "Giriş Hatası", "Marka adı boş bırakılamaz!"); return; }
            boolean eklendi = markaDAO.addMarka(markaAdi.trim());
            if (eklendi) {
                showAlert(AlertType.INFORMATION, "Başarılı", "'" + markaAdi.trim() + "' markası eklendi.");
                loadMarkaTable();
            } else { showAlert(AlertType.ERROR, "Ekleme Hatası", "'" + markaAdi.trim() + "' markası eklenemedi. (Muhtemelen zaten mevcut)"); }
        });
    }

    @FXML
    private void handleModelYeni() {
         Marka markaFiltre = cmbModelMarkaFilter.getValue();
         Marka markaTablo = tblMarkalar.getSelectionModel().getSelectedItem();
         Marka kullanilacakMarka = null;

         if (markaFiltre != null) kullanilacakMarka = markaFiltre;
         else if (markaTablo != null) kullanilacakMarka = markaTablo;

         if (kullanilacakMarka == null) {
              showAlert(AlertType.INFORMATION, "Marka Seçin", "Yeni model eklemek için lütfen önce Marka Filtresi'nden veya Markalar listesinden bir marka seçin.");
              return;
         }
         cmbModelMarkaFilter.setValue(kullanilacakMarka);

         final Marka finalKullanilacakMarka = kullanilacakMarka;
         TextInputDialog dialog = new TextInputDialog();
         dialog.setTitle("Yeni Model Ekle");
         dialog.setHeaderText(finalKullanilacakMarka.getAd() + " markasına yeni model ekleyin:");
         dialog.setContentText("Model Adı:");

         Optional<String> result = dialog.showAndWait();
         result.ifPresent(modelAdi -> {
              if (modelAdi.trim().isEmpty()) { showAlert(AlertType.WARNING, "Giriş Hatası", "Model adı boş olamaz."); return; }
              boolean eklendi = modelDAO.addModel(finalKullanilacakMarka.getId(), modelAdi.trim());
              if (eklendi) {
                  showAlert(AlertType.INFORMATION, "Başarılı", "'" + modelAdi.trim() + "' modeli, " + finalKullanilacakMarka.getAd() + " markasına eklendi.");
                  loadModelTable(finalKullanilacakMarka.getId());
              } else { showAlert(AlertType.ERROR, "Ekleme Hatası", "Model eklenemedi. (Muhtemelen zaten mevcut)"); }
         });
    }

     @FXML
    private void handleTipYeni() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Yeni Tip Ekle");
        dialog.setHeaderText("Eklenecek tipin adını girin (örn: Buzdolabı):");
        dialog.setContentText("Tip Adı:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(tipAdi -> {
             if (tipAdi.trim().isEmpty()) { showAlert(AlertType.WARNING, "Giriş Hatası", "Tip adı boş bırakılamaz!"); return; }
            boolean eklendi = tipDAO.addTip(tipAdi.trim());
             if (eklendi) {
                showAlert(AlertType.INFORMATION, "Başarılı", "'" + tipAdi.trim() + "' tipi eklendi.");
                loadTipTable();
            } else { showAlert(AlertType.ERROR, "Ekleme Hatası", "'" + tipAdi.trim() + "' tipi eklenemedi. (Muhtemelen zaten mevcut)"); }
        });
    }

    @FXML
    private void handleMarkaDuzenle() {
        Marka seciliMarka = tblMarkalar.getSelectionModel().getSelectedItem();
        if (seciliMarka == null) return;

        TextInputDialog dialog = new TextInputDialog(seciliMarka.getAd());
        dialog.setTitle("Marka Düzenle");
        dialog.setHeaderText("Markanın yeni adını girin:");
        dialog.setContentText("Marka Adı:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(yeniAd -> {
            if (yeniAd.trim().isEmpty()) { showAlert(AlertType.WARNING, "Giriş Hatası", "Marka adı boş bırakılamaz!"); return; }
            if (!yeniAd.trim().equals(seciliMarka.getAd())) {
                boolean guncellendi = markaDAO.updateMarka(seciliMarka.getId(), yeniAd.trim());
                if (guncellendi) {
                    showAlert(AlertType.INFORMATION, "Başarılı", "Marka başarıyla güncellendi.");
                    loadMarkaTable();
                    loadModelTable(cmbModelMarkaFilter.getValue() != null ? cmbModelMarkaFilter.getValue().getId() : null);
                } else { showAlert(AlertType.ERROR, "Güncelleme Hatası", "Marka güncellenemedi."); }
            }
        });
    }

     @FXML
    private void handleModelDuzenle() {
         Model seciliModel = tblModeller.getSelectionModel().getSelectedItem();
         if (seciliModel == null) return;

          TextInputDialog dialog = new TextInputDialog(seciliModel.getAd());
         dialog.setTitle("Model Düzenle");
         dialog.setHeaderText("Modelin yeni adını girin:");
         dialog.setContentText("Model Adı:");

         Optional<String> result = dialog.showAndWait();
         result.ifPresent(yeniAd -> {
              if (yeniAd.trim().isEmpty()) { showAlert(AlertType.WARNING, "Giriş Hatası", "Model adı boş olamaz."); return; }
             if (!yeniAd.trim().equals(seciliModel.getAd())) {
                boolean guncellendi = modelDAO.updateModel(seciliModel.getId(), yeniAd.trim());
                 if (guncellendi) {
                    showAlert(AlertType.INFORMATION, "Başarılı", "Model başarıyla güncellendi.");
                    Marka seciliFiltre = cmbModelMarkaFilter.getValue();
                    loadModelTable(seciliFiltre != null ? seciliFiltre.getId() : null);
                } else { showAlert(AlertType.ERROR, "Güncelleme Hatası", "Model güncellenemedi."); }
            }
        });
    }

     @FXML
    private void handleTipDuzenle() {
        Tip seciliTip = tblTipler.getSelectionModel().getSelectedItem();
        if (seciliTip == null) return;

         TextInputDialog dialog = new TextInputDialog(seciliTip.getAd());
        dialog.setTitle("Tip Düzenle");
        dialog.setHeaderText("Tipin yeni adını girin:");
        dialog.setContentText("Tip Adı:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(yeniAd -> {
             if (yeniAd.trim().isEmpty()) { showAlert(AlertType.WARNING, "Giriş Hatası", "Tip adı boş bırakılamaz!"); return; }
            if (!yeniAd.trim().equals(seciliTip.getAd())) {
                boolean guncellendi = tipDAO.updateTip(seciliTip.getId(), yeniAd.trim());
                 if (guncellendi) {
                    showAlert(AlertType.INFORMATION, "Başarılı", "Tip başarıyla güncellendi.");
                    loadTipTable();
                } else { showAlert(AlertType.ERROR, "Güncelleme Hatası", "Tip güncellenemedi."); }
            }
        });
    }

    @FXML
    private void handleMarkaSil() {
        Marka seciliMarka = tblMarkalar.getSelectionModel().getSelectedItem();
        if (seciliMarka == null) return;

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Marka Silme Onayı");
        alert.setHeaderText("'" + seciliMarka.getAd() + "' markasını silmek istediğinizden emin misiniz?");
        alert.setContentText("Bu markaya ait modeller de silinebilir!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean silindi = markaDAO.deleteMarka(seciliMarka.getId());
            if (silindi) {
                showAlert(AlertType.INFORMATION, "Başarılı", "Marka başarıyla silindi.");
                loadMarkaTable();
                loadModelTable(null);
            } else { showAlert(AlertType.ERROR, "Silme Hatası", "Marka silinemedi! Muhtemelen başka kayıtlarda kullanılıyor."); }
        }
    }

     @FXML
    private void handleModelSil() {
        Model seciliModel = tblModeller.getSelectionModel().getSelectedItem();
        if (seciliModel == null) return;

         Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Model Silme Onayı");
        alert.setHeaderText("'" + seciliModel.getAd() + "' modelini silmek istediğinizden emin misiniz?");
        alert.setContentText("Bu işlem geri alınamaz!");

         Optional<ButtonType> result = alert.showAndWait();
         if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean silindi = modelDAO.deleteModel(seciliModel.getId());
             if (silindi) {
                showAlert(AlertType.INFORMATION, "Başarılı", "Model başarıyla silindi.");
                 Marka seciliFiltre = cmbModelMarkaFilter.getValue();
                 loadModelTable(seciliFiltre != null ? seciliFiltre.getId() : null);
            } else { showAlert(AlertType.ERROR, "Silme Hatası", "Model silinemedi! Muhtemelen başka kayıtlarda (parçalar) kullanılıyor."); }
        }
    }

     @FXML
    private void handleTipSil() {
         Tip seciliTip = tblTipler.getSelectionModel().getSelectedItem();
         if (seciliTip == null) return;

          Alert alert = new Alert(AlertType.CONFIRMATION);
         alert.setTitle("Tip Silme Onayı");
         alert.setHeaderText("'" + seciliTip.getAd() + "' tipini silmek istediğinizden emin misiniz?");
         alert.setContentText("Bu işlem geri alınamaz!");

          Optional<ButtonType> result = alert.showAndWait();
          if (result.isPresent() && result.get() == ButtonType.OK) {
             boolean silindi = tipDAO.deleteTip(seciliTip.getId());
              if (silindi) {
                 showAlert(AlertType.INFORMATION, "Başarılı", "Tip başarıyla silindi.");
                 loadTipTable();
             } else { showAlert(AlertType.ERROR, "Silme Hatası", "Tip silinemedi! Muhtemelen başka kayıtlarda (parçalar) kullanılıyor."); }
         }
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Public method to be called when this tab becomes visible 
     * or when there's an external data change. Reloads the tables and ComboBox. */
    public void refreshData() {
        System.out.println("KategoriView verileri yenileniyor...");
        Marka seciliMarkaFiltre = cmbModelMarkaFilter.getValue();
        Marka seciliMarkaTablo = tblMarkalar.getSelectionModel().getSelectedItem();
        Tip seciliTipTablo = tblTipler.getSelectionModel().getSelectedItem();
        Model seciliModelTablo = tblModeller.getSelectionModel().getSelectedItem();

        markaList.setAll(markaDAO.getAllMarkalar());
        tipList.setAll(tipDAO.getAllTipler());

        cmbModelMarkaFilter.setValue(markaList.stream().filter(m -> m.getId() == (seciliMarkaFiltre != null ? seciliMarkaFiltre.getId() : -1)).findFirst().orElse(null));
        tblMarkalar.getSelectionModel().select(markaList.stream().filter(m -> m.getId() == (seciliMarkaTablo != null ? seciliMarkaTablo.getId() : -1)).findFirst().orElse(null));
        if (tblMarkalar.getSelectionModel().getSelectedItem() == null) tblMarkalar.getSelectionModel().clearSelection();


        tblTipler.getSelectionModel().select(tipList.stream().filter(t -> t.getId() == (seciliTipTablo != null ? seciliTipTablo.getId() : -1)).findFirst().orElse(null));
         if (tblTipler.getSelectionModel().getSelectedItem() == null) tblTipler.getSelectionModel().clearSelection();


        Marka mevcutMarkaFiltre = cmbModelMarkaFilter.getValue();
        Integer markaId = mevcutMarkaFiltre != null ? mevcutMarkaFiltre.getId() : null;
        modelList.clear();
        if (markaId != null && markaId > 0) {
            modelList.setAll(modelDAO.getModellerByMarkaId(markaId));
        }
        if (seciliModelTablo != null) {
             tblModeller.getSelectionModel().select(modelList.stream().filter(m -> m.getId() == seciliModelTablo.getId()).findFirst().orElse(null));
        }
         if (tblModeller.getSelectionModel().getSelectedItem() == null) tblModeller.getSelectionModel().clearSelection();


        updateButtonStates();
        System.out.println("KategoriView verileri yenilendi.");
    }
}