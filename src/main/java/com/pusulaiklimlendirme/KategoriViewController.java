package com.pusulaiklimlendirme;

import com.pusulaiklimlendirme.Marka;
import com.pusulaiklimlendirme.Model;
import com.pusulaiklimlendirme.CihazTuru;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;

import java.util.Optional;

public class KategoriViewController {

    // --- FXML Fields: Marka ---
    @FXML private TableView<Marka> tblMarkalar;
    @FXML private TableColumn<Marka, Integer> colMarkaId;
    @FXML private TableColumn<Marka, String> colMarkaAd;
    @FXML private Button btnMarkaYeni;
    @FXML private Button btnMarkaDuzenle;
    @FXML private Button btnMarkaSil;

    // --- FXML Fields: Model ---
    @FXML private ComboBox<Marka> cmbModelMarkaFilter;
    @FXML private TableView<Model> tblModeller;
    @FXML private TableColumn<Model, Integer> colModelId;
    @FXML private TableColumn<Model, Integer> colModelMarkaId;
    @FXML private TableColumn<Model, String> colModelAd;
    @FXML private Button btnModelYeni;
    @FXML private Button btnModelDuzenle;
    @FXML private Button btnModelSil;

    // --- FXML Fields: Cihaz Türü ---
    @FXML private TableView<CihazTuru> tblCihazTurleri;
    @FXML private TableColumn<CihazTuru, Integer> colCihazTuruId;
    @FXML private TableColumn<CihazTuru, String> colCihazTuruAd;
    @FXML private Button btnCihazTuruYeni;
    @FXML private Button btnCihazTuruDuzenle;
    @FXML private Button btnCihazTuruSil;

    // --- DAO Objects ---
    private final MarkaDAO markaDAO = new MarkaDAO();
    private final ModelDAO modelDAO = new ModelDAO();
    private final CihazTuruDAO cihazTuruDAO = new CihazTuruDAO();

    // --- Observable Lists ---
    private ObservableList<Marka> markaList = FXCollections.observableArrayList();
    private ObservableList<Model> modelList = FXCollections.observableArrayList();
    private ObservableList<CihazTuru> cihazTuruList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        System.out.println("KategoriViewController initialized.");
        
        configureTableColumns();
        
        tblMarkalar.setItems(markaList);
        tblModeller.setItems(modelList);
        tblCihazTurleri.setItems(cihazTuruList);
        cmbModelMarkaFilter.setItems(markaList);
        
        refreshData();
        
        addSelectionListeners();
        addDoubleClickListeners();
        addMarkaFilterListener();
        
        updateButtonStates();
    }

    private void configureTableColumns() {
        // Marka Table
        colMarkaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMarkaAd.setCellValueFactory(new PropertyValueFactory<>("ad"));

        // Model Table
        colModelId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colModelMarkaId.setCellValueFactory(new PropertyValueFactory<>("markaId"));
        colModelAd.setCellValueFactory(new PropertyValueFactory<>("ad"));

        // Cihaz Türü Table
        colCihazTuruId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCihazTuruAd.setCellValueFactory(new PropertyValueFactory<>("ad"));
    }

    private void addSelectionListeners() {
        tblMarkalar.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> updateButtonStates());
        tblModeller.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> updateButtonStates());
        tblCihazTurleri.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> updateButtonStates());
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
        tblCihazTurleri.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2 && tblCihazTurleri.getSelectionModel().getSelectedItem() != null) {
                handleCihazTuruDuzenle();
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
        btnMarkaDuzenle.setDisable(!markaSecili);
        btnMarkaSil.setDisable(!markaSecili);

        boolean modelSecili = tblModeller.getSelectionModel().getSelectedItem() != null;
        btnModelDuzenle.setDisable(!modelSecili);
        btnModelSil.setDisable(!modelSecili);
        
        boolean markaFiltreSecili = cmbModelMarkaFilter.getValue() != null;
        btnModelYeni.setDisable(!markaFiltreSecili && !markaSecili);

        boolean cihazTuruSecili = tblCihazTurleri.getSelectionModel().getSelectedItem() != null;
        btnCihazTuruDuzenle.setDisable(!cihazTuruSecili);
        btnCihazTuruSil.setDisable(!cihazTuruSecili);
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
    }

    private void loadCihazTuruTable() {
        CihazTuru seciliCihazTuru = tblCihazTurleri.getSelectionModel().getSelectedItem();
        cihazTuruList.setAll(cihazTuruDAO.getAllCihazTurleri());
        if (seciliCihazTuru != null) {
            tblCihazTurleri.getSelectionModel().select(cihazTuruList.stream().filter(t -> t.getId() == seciliCihazTuru.getId()).findFirst().orElse(null));
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
            if (markaAdi.trim().isEmpty()) {
                showAlert(AlertType.WARNING, "Giriş Hatası", "Marka adı boş bırakılamaz!");
                return;
            }
            if (markaDAO.addMarka(markaAdi.trim())) {
                showAlert(AlertType.INFORMATION, "Başarılı", "'" + markaAdi.trim() + "' markası eklendi.");
                loadMarkaTable();
            } else {
                showAlert(AlertType.ERROR, "Ekleme Hatası", "'" + markaAdi.trim() + "' markası eklenemedi (muhtemelen zaten mevcut).");
            }
        });
    }

    @FXML
    private void handleModelYeni() {
        Marka kullanilacakMarka = Optional.ofNullable(cmbModelMarkaFilter.getValue())
                                      .orElse(tblMarkalar.getSelectionModel().getSelectedItem());
        if (kullanilacakMarka == null) {
            showAlert(AlertType.INFORMATION, "Marka Seçin", "Yeni model eklemek için lütfen önce bir marka seçin.");
            return;
        }
        cmbModelMarkaFilter.setValue(kullanilacakMarka);

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Yeni Model Ekle");
        dialog.setHeaderText(kullanilacakMarka.getAd() + " markasına yeni model ekleyin:");
        dialog.setContentText("Model Adı:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(modelAdi -> {
            if (modelAdi.trim().isEmpty()) {
                showAlert(AlertType.WARNING, "Giriş Hatası", "Model adı boş olamaz.");
                return;
            }
            if (modelDAO.addModel(kullanilacakMarka.getId(), modelAdi.trim())) {
                showAlert(AlertType.INFORMATION, "Başarılı", "'" + modelAdi.trim() + "' modeli, " + kullanilacakMarka.getAd() + " markasına eklendi.");
                loadModelTable(kullanilacakMarka.getId());
            } else {
                showAlert(AlertType.ERROR, "Ekleme Hatası", "Model eklenemedi (muhtemelen zaten mevcut).");
            }
        });
    }

    @FXML
    private void handleCihazTuruYeni() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Yeni Cihaz Türü Ekle");
        dialog.setHeaderText("Eklenecek cihaz türünün adını girin (örn: Split Klima):");
        dialog.setContentText("Cihaz Türü Adı:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(cihazTuruAdi -> {
            if (cihazTuruAdi.trim().isEmpty()) {
                showAlert(AlertType.WARNING, "Giriş Hatası", "Cihaz türü adı boş bırakılamaz!");
                return;
            }
            if (cihazTuruDAO.addCihazTuru(cihazTuruAdi.trim())) {
                showAlert(AlertType.INFORMATION, "Başarılı", "'" + cihazTuruAdi.trim() + "' cihaz türü eklendi.");
                loadCihazTuruTable();
            } else {
                showAlert(AlertType.ERROR, "Ekleme Hatası", "'" + cihazTuruAdi.trim() + "' cihaz türü eklenemedi (muhtemelen zaten mevcut).");
            }
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
            if (yeniAd.trim().isEmpty()) {
                showAlert(AlertType.WARNING, "Giriş Hatası", "Marka adı boş bırakılamaz!");
                return;
            }
            if (!yeniAd.trim().equals(seciliMarka.getAd())) {
                if (markaDAO.updateMarka(seciliMarka.getId(), yeniAd.trim())) {
                    showAlert(AlertType.INFORMATION, "Başarılı", "Marka başarıyla güncellendi.");
                    loadMarkaTable();
                } else {
                    showAlert(AlertType.ERROR, "Güncelleme Hatası", "Marka güncellenemedi.");
                }
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
            if (yeniAd.trim().isEmpty()) {
                showAlert(AlertType.WARNING, "Giriş Hatası", "Model adı boş olamaz.");
                return;
            }
            if (!yeniAd.trim().equals(seciliModel.getAd())) {
                if (modelDAO.updateModel(seciliModel.getId(), yeniAd.trim())) {
                    showAlert(AlertType.INFORMATION, "Başarılı", "Model başarıyla güncellendi.");
                    loadModelTable(seciliModel.getMarkaId());
                } else {
                    showAlert(AlertType.ERROR, "Güncelleme Hatası", "Model güncellenemedi.");
                }
            }
        });
    }

    @FXML
    private void handleCihazTuruDuzenle() {
        CihazTuru seciliCihazTuru = tblCihazTurleri.getSelectionModel().getSelectedItem();
        if (seciliCihazTuru == null) return;
        TextInputDialog dialog = new TextInputDialog(seciliCihazTuru.getAd());
        dialog.setTitle("Cihaz Türü Düzenle");
        dialog.setHeaderText("Cihaz türünün yeni adını girin:");
        dialog.setContentText("Cihaz Türü Adı:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(yeniAd -> {
            if (yeniAd.trim().isEmpty()) {
                showAlert(AlertType.WARNING, "Giriş Hatası", "Cihaz türü adı boş bırakılamaz!");
                return;
            }
            if (!yeniAd.trim().equals(seciliCihazTuru.getAd())) {
                if (cihazTuruDAO.updateCihazTuru(seciliCihazTuru.getId(), yeniAd.trim())) {
                    showAlert(AlertType.INFORMATION, "Başarılı", "Cihaz türü başarıyla güncellendi.");
                    loadCihazTuruTable();
                } else {
                    showAlert(AlertType.ERROR, "Güncelleme Hatası", "Cihaz türü güncellenemedi.");
                }
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
        alert.setContentText("Bu markaya ait tüm modeller de silinecektir (ON DELETE CASCADE).");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (markaDAO.deleteMarka(seciliMarka.getId())) {
                showAlert(AlertType.INFORMATION, "Başarılı", "Marka başarıyla silindi.");
                loadMarkaTable();
                loadModelTable(null);
            } else {
                showAlert(AlertType.ERROR, "Silme Hatası", "Marka silinemedi! Başka kayıtlarda kullanılıyor olabilir.");
            }
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
            if (modelDAO.deleteModel(seciliModel.getId())) {
                showAlert(AlertType.INFORMATION, "Başarılı", "Model başarıyla silindi.");
                loadModelTable(seciliModel.getMarkaId());
            } else {
                showAlert(AlertType.ERROR, "Silme Hatası", "Model silinemedi! Muhtemelen stoktaki parçalar tarafından kullanılıyor.");
            }
        }
    }

    @FXML
    private void handleCihazTuruSil() {
        CihazTuru seciliCihazTuru = tblCihazTurleri.getSelectionModel().getSelectedItem();
        if (seciliCihazTuru == null) return;
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Cihaz Türü Silme Onayı");
        alert.setHeaderText("'" + seciliCihazTuru.getAd() + "' cihaz türünü silmek istediğinizden emin misiniz?");
        alert.setContentText("Bu işlem geri alınamaz!");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (cihazTuruDAO.deleteCihazTuru(seciliCihazTuru.getId())) {
                showAlert(AlertType.INFORMATION, "Başarılı", "Cihaz türü başarıyla silindi.");
                loadCihazTuruTable();
            } else {
                showAlert(AlertType.ERROR, "Silme Hatası", "Cihaz türü silinemedi! Muhtemelen stoktaki parçalar tarafından kullanılıyor.");
            }
        }
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Public method to be called when this tab becomes visible
     * or when there's an external data change. Reloads the tables and ComboBox.
     */
    public void refreshData() {
        System.out.println("KategoriView data is being refreshed...");
        
        // Preserve selections
        Marka seciliMarkaFiltre = cmbModelMarkaFilter.getValue();
        Marka seciliMarkaTablo = tblMarkalar.getSelectionModel().getSelectedItem();
        Model seciliModelTablo = tblModeller.getSelectionModel().getSelectedItem();
        CihazTuru seciliCihazTuruTablo = tblCihazTurleri.getSelectionModel().getSelectedItem();

        // Refresh lists from DB
        markaList.setAll(markaDAO.getAllMarkalar());
        cihazTuruList.setAll(cihazTuruDAO.getAllCihazTurleri());

        // Restore selections
        if (seciliMarkaFiltre != null) cmbModelMarkaFilter.getSelectionModel().select(seciliMarkaFiltre);
        if (seciliMarkaTablo != null) tblMarkalar.getSelectionModel().select(seciliMarkaTablo);
        
        // Reload model table based on filter (this will clear model selection)
        Integer markaId = cmbModelMarkaFilter.getValue() != null ? cmbModelMarkaFilter.getValue().getId() : null;
        loadModelTable(markaId); // This method already preserves selection if possible
        if (seciliModelTablo != null) tblModeller.getSelectionModel().select(seciliModelTablo); // Re-select if still in list

        if (seciliCihazTuruTablo != null) tblCihazTurleri.getSelectionModel().select(seciliCihazTuruTablo);

        updateButtonStates();
        System.out.println("KategoriView data has been refreshed.");
    }
}