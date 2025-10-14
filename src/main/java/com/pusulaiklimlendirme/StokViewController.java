package com.pusulaiklimlendirme;

// Gerekli Importlar
import com.pusulaiklimlendirme.Marka;
import com.pusulaiklimlendirme.Model;
import com.pusulaiklimlendirme.Parca;
import com.pusulaiklimlendirme.StokGorunum;
import com.pusulaiklimlendirme.Tip;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class StokViewController {

    // --- FXML Alanları ---
    @FXML private TextField txtParcaAdi;
    @FXML private ComboBox<Marka> cmbParcaMarka;
    @FXML private ComboBox<Model> cmbParcaModel;
    @FXML private ComboBox<Tip> cmbParcaTip;
    @FXML private TextField txtParcaAlisFiyat;
    @FXML private TextField txtParcaSatisFiyat;
    @FXML private Button btnParcaEkle;
    @FXML private ComboBox<Parca> cmbStokParcaSec;
    @FXML private TextField txtStokAdet;
    @FXML private Button btnStokGiris;
    @FXML private Button btnStokCikis;
    @FXML private TableView<StokGorunum> tblStokDurumu;
    @FXML private TableColumn<StokGorunum, Integer> colStokId;
    @FXML private TableColumn<StokGorunum, String> colStokParcaAdi;
    @FXML private TableColumn<StokGorunum, String> colStokMarka;
    @FXML private TableColumn<StokGorunum, String> colStokModel;
    @FXML private TableColumn<StokGorunum, String> colStokTip;
    @FXML private TableColumn<StokGorunum, Double> colStokAlis;
    @FXML private TableColumn<StokGorunum, Double> colStokSatis;
    @FXML private TableColumn<StokGorunum, Integer> colStokAdet;
    @FXML private Button btnParcaDuzenle;
    @FXML private Button btnParcaSil;

    // --- DAO Nesneleri ---
    private final MarkaDAO markaDAO = new MarkaDAO();
    private final ModelDAO modelDAO = new ModelDAO();
    private final TipDAO tipDAO = new TipDAO();
    private final ParcaDAO parcaDAO = new ParcaDAO();
    private final StokHareketDAO stokHareketDAO = new StokHareketDAO();

    // --- Listeler ---
    private ObservableList<StokGorunum> stokGorunumObservableList = FXCollections.observableArrayList();
    private ObservableList<Marka> markaComboList = FXCollections.observableArrayList();
    private ObservableList<Model> modelComboList = FXCollections.observableArrayList();
    private ObservableList<Tip> tipComboList = FXCollections.observableArrayList();
    private ObservableList<Parca> parcaStokComboList = FXCollections.observableArrayList();

    // Para formatlama için
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));

    // --- Başlatma Metodu ---
    @FXML
    private void initialize() {
        System.out.println("StokViewController başlatıldı (Düzenle/Sil ve Refresh ile)!");
        configureTableViewColumns();
        cmbParcaMarka.setItems(markaComboList);
        cmbParcaModel.setItems(modelComboList);
        cmbParcaTip.setItems(tipComboList);
        cmbStokParcaSec.setItems(parcaStokComboList);
        tblStokDurumu.setItems(stokGorunumObservableList);
        refreshData();
        addNumericListener(txtParcaAlisFiyat);
        addNumericListener(txtParcaSatisFiyat);
        addNumericListener(txtStokAdet);
        tblStokDurumu.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> updateButtonStates());
        tblStokDurumu.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2 && tblStokDurumu.getSelectionModel().getSelectedItem() != null) {
                handleParcaDuzenle();
            }
        });
        updateButtonStates();
    }

    // --- Olay Yöneticileri (Event Handlers) ---

    @FXML
    private void handleParcaMarkaSecildi() {
        Marka secilenMarka = cmbParcaMarka.getValue();
        loadParcaModelCombo(secilenMarka != null ? secilenMarka.getId() : null);
    }

    @FXML
    private void handleParcaEkle() {
        String parcaAdi = txtParcaAdi.getText();
        Marka marka = cmbParcaMarka.getValue();
        Model model = cmbParcaModel.getValue();
        Tip tip = cmbParcaTip.getValue();
        String alisFiyatStr = txtParcaAlisFiyat.getText();
        String satisFiyatStr = txtParcaSatisFiyat.getText();

        if (parcaAdi == null || parcaAdi.trim().isEmpty() || marka == null || model == null || tip == null ||
            alisFiyatStr == null || alisFiyatStr.trim().isEmpty() || satisFiyatStr == null || satisFiyatStr.trim().isEmpty()) {
            showAlert(AlertType.WARNING, "Eksik Bilgi", "Lütfen tüm parça bilgilerini doldurun!");
            return;
        }

        double alisFiyat, satisFiyat;
        try {
            alisFiyat = parseDouble(alisFiyatStr);
            satisFiyat = parseDouble(satisFiyatStr);
            if (alisFiyat < 0 || satisFiyat < 0) throw new IllegalArgumentException("Fiyatlar negatif olamaz."); // Hata türü değişti
        } catch (IllegalArgumentException e) { // Sadece bunu yakala
            showAlert(AlertType.WARNING, "Geçersiz Fiyat/Girdi", e.getMessage());
            return;
        }

        boolean eklendi = parcaDAO.addParca(parcaAdi.trim(), marka.getAd(), model.getAd(), tip.getAd(), alisFiyat, satisFiyat);

        if (eklendi) {
            showAlert(AlertType.INFORMATION, "Başarılı", "'" + parcaAdi.trim() + "' parçası başarıyla eklendi.");
            clearParcaEkleForm();
            refreshData();
        } else {
            showAlert(AlertType.ERROR, "Parça Ekleme Hatası", "Parça eklenirken bir hata oluştu.");
        }
    }

    @FXML
    private void handleStokGiris() { handleStokHareketi("giris"); }

    @FXML
    private void handleStokCikis() { handleStokHareketi("cikis"); }

    private void handleStokHareketi(String hareketTipi) {
        Parca secilenParca = cmbStokParcaSec.getValue();
        String adetStr = txtStokAdet.getText();

        if (secilenParca == null) { showAlert(AlertType.WARNING, "Eksik Bilgi", "Lütfen işlem yapılacak parçayı seçin!"); return; }
        if (adetStr == null || adetStr.trim().isEmpty()) { showAlert(AlertType.WARNING, "Eksik Bilgi", "Lütfen stok adetini girin!"); return; }

        int adet;
        try {
            adet = Integer.parseInt(adetStr);
            if (adet <= 0) throw new NumberFormatException("Adet pozitif olmalı.");
        } catch (NumberFormatException e) { showAlert(AlertType.WARNING, "Geçersiz Adet", "Lütfen geçerli bir pozitif tam sayı adet girin!"); return; }

        if ("cikis".equals(hareketTipi)) {
             Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Stok Çıkış Onayı");
            confirmationAlert.setHeaderText(adet + " adet '" + secilenParca.getAd() + "' stoktan düşülecek.");
            confirmationAlert.setContentText("Emin misiniz?");
            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) return;
        }

        boolean basarili = stokHareketDAO.addStokHareketi(secilenParca.getId(), adet, hareketTipi);

        if (basarili) {
            String mesajTipi = hareketTipi.equals("giris") ? "girişi" : "çıkışı";
            showAlert(AlertType.INFORMATION, "Başarılı", adet + " adet '" + secilenParca.getAd() + "' stok " + mesajTipi + " başarıyla yapıldı.");
            cmbStokParcaSec.getSelectionModel().clearSelection();
            txtStokAdet.clear();
            loadStokTable(); // Sadece tabloyu yenile
        } else {
             showAlert(AlertType.ERROR, "Stok İşlem Hatası", "Stok hareketi kaydedilemedi! (Yetersiz stok veya başka bir hata)");
        }
    }

    @FXML
    private void handleParcaDuzenle() {
        StokGorunum seciliStok = tblStokDurumu.getSelectionModel().getSelectedItem();
        if (seciliStok == null) return;

        Parca duzenlenecekParca = parcaDAO.getParcaById(seciliStok.getId());
        if (duzenlenecekParca == null) {
            showAlert(AlertType.ERROR, "Hata", "Düzenlenecek parça veritabanında bulunamadı.");
            return;
        }

        Dialog<Parca> dialog = createParcaDuzenleDialog(duzenlenecekParca);
        Optional<Parca> result = dialog.showAndWait();

        result.ifPresent(guncellenmisParca -> {
            boolean basarili = parcaDAO.updateParca(guncellenmisParca);
            if (basarili) {
                showAlert(AlertType.INFORMATION, "Başarılı", "Parça bilgileri güncellendi.");
                refreshData();
            } else {
                showAlert(AlertType.ERROR, "Güncelleme Hatası", "Parça güncellenirken bir hata oluştu.");
            }
        });
    }

    @FXML
    private void handleParcaSil() {
        StokGorunum seciliStok = tblStokDurumu.getSelectionModel().getSelectedItem();
        if (seciliStok == null) return;

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Parça Silme Onayı");
        alert.setHeaderText("'" + seciliStok.getParcaAdi() + "' parçasını silmek istediğinizden emin misiniz?");
        alert.setContentText("Bu parçaya ait stok hareketleri de silinecek ve servis kayıtlarındaki bağlantısı kaldırılacaktır!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean silindi = parcaDAO.deleteParca(seciliStok.getId());
            if (silindi) {
                showAlert(AlertType.INFORMATION, "Başarılı", "Parça başarıyla silindi.");
                refreshData();
            } else {
                showAlert(AlertType.ERROR, "Silme Hatası", "Parça silinirken bir veritabanı hatası oluştu.");
            }
        }
    }

    // --- Yardımcı Metotlar ---

    private void configureTableViewColumns() {
        colStokId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStokParcaAdi.setCellValueFactory(new PropertyValueFactory<>("parcaAdi"));
        colStokMarka.setCellValueFactory(new PropertyValueFactory<>("markaAdi"));
        colStokModel.setCellValueFactory(new PropertyValueFactory<>("modelAdi"));
        colStokTip.setCellValueFactory(new PropertyValueFactory<>("tipAdi"));
        colStokAlis.setCellValueFactory(new PropertyValueFactory<>("alisFiyat"));
        colStokSatis.setCellValueFactory(new PropertyValueFactory<>("satisFiyat"));
        colStokAdet.setCellValueFactory(new PropertyValueFactory<>("stokAdet"));
        colStokAlis.setCellFactory(column -> formatCurrencyCell());
        colStokSatis.setCellFactory(column -> formatCurrencyCell());
    }

    private void loadParcaMarkaCombo() {
        Marka secili = cmbParcaMarka.getValue();
        markaComboList.setAll(markaDAO.getAllMarkalar());
        cmbParcaMarka.setValue(markaComboList.stream().filter(m -> m.getId() == (secili != null ? secili.getId() : -1)).findFirst().orElse(null));
    }

    private void loadParcaModelCombo(Integer markaId) {
        modelComboList.clear();
        if (markaId != null && markaId > 0) {
            modelComboList.setAll(modelDAO.getModellerByMarkaId(markaId));
        }
        cmbParcaModel.setPromptText(markaId != null && markaId > 0 ? "Model Seçin" : "Önce Marka Seçin");
        cmbParcaModel.getSelectionModel().clearSelection();
    }

    private void loadParcaTipCombo() {
        Tip secili = cmbParcaTip.getValue();
        tipComboList.setAll(tipDAO.getAllTipler());
        cmbParcaTip.setValue(tipComboList.stream().filter(t -> t.getId() == (secili != null ? secili.getId() : -1)).findFirst().orElse(null));
    }

    private void loadStokParcaSecCombo() {
        Parca secili = cmbStokParcaSec.getValue();
        parcaStokComboList.setAll(parcaDAO.getAllParcalar());
        Optional<Parca> found = parcaStokComboList.stream().filter(p -> p.getId() == (secili != null ? secili.getId() : -1)).findFirst();
        cmbStokParcaSec.setValue(found.orElse(null));
    }

    private void loadStokTable() {
        StokGorunum seciliStok = tblStokDurumu.getSelectionModel().getSelectedItem();
        stokGorunumObservableList.setAll(parcaDAO.getStokGorunumListesi());
        if (seciliStok != null) {
             Optional<StokGorunum> found = stokGorunumObservableList.stream()
                     .filter(s -> s.getId() == seciliStok.getId())
                     .findFirst();
             tblStokDurumu.getSelectionModel().select(found.orElse(null));
        }
         if (tblStokDurumu.getSelectionModel().getSelectedItem() == null) {
            tblStokDurumu.getSelectionModel().clearSelection();
        }
    }

    private void addNumericListener(TextField textField) {
         textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) return;
            String regex = textField == txtStokAdet ? "\\d+" : "\\d*([.,]?\\d*)";
            if (!newValue.matches(regex)) {
                textField.setText(oldValue);
            }
         });
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearParcaEkleForm() {
        txtParcaAdi.clear();
        cmbParcaMarka.getSelectionModel().clearSelection();
        cmbParcaModel.getItems().clear();
        cmbParcaModel.setPromptText("Önce Marka Seçin");
        cmbParcaTip.getSelectionModel().clearSelection();
        txtParcaAlisFiyat.clear();
        txtParcaSatisFiyat.clear();
    }

    private void updateButtonStates() {
        boolean secimVar = tblStokDurumu.getSelectionModel().getSelectedItem() != null;
        btnParcaDuzenle.setDisable(!secimVar);
        btnParcaSil.setDisable(!secimVar);
    }

    private Dialog<Parca> createParcaDuzenleDialog(Parca parca) {
        Dialog<Parca> dialog = new Dialog<>();
        dialog.setTitle("Parça Düzenle");
        dialog.setHeaderText("Parça bilgilerini güncelleyin (Marka/Model/Tip değiştirilemez).");

        ButtonType okButtonType = new ButtonType("Güncelle", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField adField = new TextField(parca.getAd());
        TextField alisFiyatField = new TextField(String.format(Locale.US, "%.2f", parca.getAlisFiyat()));
        TextField satisFiyatField = new TextField(String.format(Locale.US, "%.2f", parca.getSatisFiyat()));

        addNumericListener(alisFiyatField);
        addNumericListener(satisFiyatField);

        grid.add(new Label("Parça Adı:"), 0, 0); grid.add(adField, 1, 0);
        grid.add(new Label("Alış Fiyatı (₺):"), 0, 1); grid.add(alisFiyatField, 1, 1);
        grid.add(new Label("Satış Fiyatı (₺):"), 0, 2); grid.add(satisFiyatField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        Runnable updateOkButtonState = () -> {
             boolean disable = adField.getText().trim().isEmpty() ||
                              alisFiyatField.getText().trim().isEmpty() ||
                              satisFiyatField.getText().trim().isEmpty();
            okButton.setDisable(disable);
        };
        adField.textProperty().addListener((obs, ov, nv) -> updateOkButtonState.run());
        alisFiyatField.textProperty().addListener((obs, ov, nv) -> updateOkButtonState.run());
        satisFiyatField.textProperty().addListener((obs, ov, nv) -> updateOkButtonState.run());
        updateOkButtonState.run();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                try {
                    String yeniAd = adField.getText().trim();
                    if (yeniAd.isEmpty()) throw new IllegalArgumentException("Parça adı boş olamaz.");

                    double yeniAlis = parseDouble(alisFiyatField.getText());
                    double yeniSatis = parseDouble(satisFiyatField.getText());
                    if (yeniAlis < 0 || yeniSatis < 0) throw new IllegalArgumentException("Fiyatlar negatif olamaz."); // IllegalArgumentException olarak fırlat

                    parca.setAd(yeniAd);
                    parca.setAlisFiyat(yeniAlis);
                    parca.setSatisFiyat(yeniSatis);
                    return parca;
                // DÜZELTİLMİŞ CATCH BLOĞU: Sadece IllegalArgumentException yakala
                } catch (IllegalArgumentException e) {
                    showAlert(AlertType.WARNING, "Geçersiz Giriş", "Lütfen tüm alanları doğru doldurun! Hata: " + e.getMessage());
                    return null; // Dialog kapanmasın
                }
            }
            return null;
        });
        return dialog;
    }

     /**
      * Virgül veya nokta içeren string'i double'a çevirir.
      * Boş değer veya geçersiz format için Exception fırlatır.
      */
     private double parseDouble(String value) throws IllegalArgumentException { // Sadece bunu fırlatması yeterli
         if (value == null || value.trim().isEmpty()) {
             throw new IllegalArgumentException("Fiyat alanı boş bırakılamaz.");
         }
         try {
             return Double.parseDouble(value.trim().replace(',', '.'));
         } catch (NumberFormatException e) {
              // NumberFormatException da bir IllegalArgumentException'dır.
              // İstersek daha spesifik mesaj verebiliriz.
              throw new IllegalArgumentException("Geçersiz sayı formatı: '" + value + "'");
         }
     }

     /**
      * Double değerleri yerel para birimi olarak formatlamak için TableCell döndürür.
      */
     private TableCell<StokGorunum, Double> formatCurrencyCell() {
         return new TableCell<>() {
             @Override
             protected void updateItem(Double item, boolean empty) {
                 super.updateItem(item, empty);
                 if (empty || item == null) {
                     setText(null);
                     setStyle("");
                 } else {
                     setText(currencyFormat.format(item));
                     setStyle("-fx-alignment: CENTER-RIGHT;");
                 }
             }
         };
     }

     // --- REFRESH METODU ---
     public void refreshData() {
         System.out.println("StokView verileri yenileniyor...");
         Marka seciliMarkaCombo = cmbParcaMarka.getValue();
         Tip seciliTipCombo = cmbParcaTip.getValue();
         Parca seciliStokParcaCombo = cmbStokParcaSec.getValue();
         StokGorunum seciliStokTable = tblStokDurumu.getSelectionModel().getSelectedItem();

         markaComboList.setAll(markaDAO.getAllMarkalar());
         tipComboList.setAll(tipDAO.getAllTipler());
         parcaStokComboList.setAll(parcaDAO.getAllParcalar());
         stokGorunumObservableList.setAll(parcaDAO.getStokGorunumListesi());

         cmbParcaMarka.setValue(markaComboList.stream().filter(m -> m.getId() == (seciliMarkaCombo != null ? seciliMarkaCombo.getId() : -1)).findFirst().orElse(null));
         cmbParcaTip.setValue(tipComboList.stream().filter(t -> t.getId() == (seciliTipCombo != null ? seciliTipCombo.getId() : -1)).findFirst().orElse(null));
         cmbStokParcaSec.setValue(parcaStokComboList.stream().filter(p -> p.getId() == (seciliStokParcaCombo != null ? seciliStokParcaCombo.getId() : -1)).findFirst().orElse(null));

         Marka mevcutMarkaSecimi = cmbParcaMarka.getValue();
         loadParcaModelCombo(mevcutMarkaSecimi != null ? mevcutMarkaSecimi.getId() : null);

         if (seciliStokTable != null) {
              Optional<StokGorunum> found = stokGorunumObservableList.stream()
                      .filter(s -> s.getId() == seciliStokTable.getId())
                      .findFirst();
              tblStokDurumu.getSelectionModel().select(found.orElse(null));
         }
         if (tblStokDurumu.getSelectionModel().getSelectedItem() == null) {
             tblStokDurumu.getSelectionModel().clearSelection();
         }
         updateButtonStates();
         System.out.println("StokView verileri yenilendi.");
     }
}