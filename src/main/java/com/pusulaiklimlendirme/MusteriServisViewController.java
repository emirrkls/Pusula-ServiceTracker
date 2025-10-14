package com.pusulaiklimlendirme;

// Gerekli Importlar
import com.pusulaiklimlendirme.Musteri;
import com.pusulaiklimlendirme.Parca;
import com.pusulaiklimlendirme.ServisGorunum;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale; // Eklendi
import java.text.NumberFormat; // Eklendi
import java.util.Optional;
import java.util.function.Predicate;

public class MusteriServisViewController {

    // --- FXML Alanları ---
    // Müşteri Ekleme
    @FXML private TextField txtMusteriAdSoyad;
    @FXML private TextField txtMusteriTelefon;
    @FXML private TextArea txtMusteriAdres;
    @FXML private Button btnMusteriEkle;

    // Müşteri Listesi
    @FXML private TableView<Musteri> tblMusteriler;
    @FXML private TableColumn<Musteri, Integer> colMusteriId;
    @FXML private TableColumn<Musteri, String> colMusteriAdSoyad;
    @FXML private TableColumn<Musteri, String> colMusteriTelefon;
    @FXML private TableColumn<Musteri, String> colMusteriAdres;
    @FXML private Button btnMusteriDuzenle;
    @FXML private Button btnMusteriSil;

    // Servis Kaydı
    @FXML private TextField txtMusteriAra;
    @FXML private ComboBox<Musteri> cmbServisMusteri;
    @FXML private DatePicker dateServisTarih;
    @FXML private TextField txtServisIslem;
    @FXML private ComboBox<Parca> cmbServisParca;
    @FXML private TextField txtServisAdet;
    @FXML private TextField txtServisIscilik;
    @FXML private TextField txtServisOdeme;
    @FXML private Button btnServisKaydet;

    // Servis Kayıtları Tablosu ve Arama Kutusu
    @FXML private TextField txtServisKaydiAra; // Servis arama kutusu
    @FXML private TableView<ServisGorunum> tblServisKayitlari;
    @FXML private TableColumn<ServisGorunum, Integer> colServisId;
    @FXML private TableColumn<ServisGorunum, String> colServisMusteri;
    @FXML private TableColumn<ServisGorunum, LocalDate> colServisTarih;
    @FXML private TableColumn<ServisGorunum, String> colServisIslem;
    @FXML private TableColumn<ServisGorunum, String> colServisParca;
    @FXML private TableColumn<ServisGorunum, Integer> colServisAdet;
    @FXML private TableColumn<ServisGorunum, Double> colServisIscilik;
    @FXML private TableColumn<ServisGorunum, Double> colServisOdeme;
    @FXML private TableColumn<ServisGorunum, Double> colServisKar;

    // --- DAO Nesneleri ---
    private final MusteriDAO musteriDAO = new MusteriDAO();
    private final ParcaDAO parcaDAO = new ParcaDAO();
    private final ServisKaydiDAO servisKaydiDAO = new ServisKaydiDAO();

    // --- Diğer Alanlar ---
    // Müşteri listeleri
    private ObservableList<Musteri> tumMusterilerList = FXCollections.observableArrayList();
    private FilteredList<Musteri> filtrelenmisMusterilerList;
    // Servis tablosu listeleri
    private ObservableList<ServisGorunum> tumServisGorunumList = FXCollections.observableArrayList();
    private FilteredList<ServisGorunum> filtrelenmisServisGorunumList; // Servis tablosu için filtrelenmiş

    // Para formatlama için
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));


    // --- Başlatma Metodu ---
    @FXML
    private void initialize() {
        System.out.println("MusteriServisViewController başlatıldı (Servis Filtreleme ile)!");

        // 1. Müşteri Bölümünü Ayarla
        configureMusteriTableColumns();
        tblMusteriler.setItems(tumMusterilerList); // Önce tabloyu listeye bağla
        tumMusterilerList.setAll(musteriDAO.getAllMusteriler()); // Sonra listeyi doldur
        configureMusteriFilterAndCombo(); // Filtre ve ComboBox'ı ayarla (ana listeyi kullanır)
        addMusteriTableListeners();
        updateMusteriButtonStates();

        // 2. Servis Kaydı Formunu Ayarla
        loadServisParcaCombo();
        addNumericListener(txtServisAdet);
        addNumericListener(txtServisIscilik);
        addNumericListener(txtServisOdeme);
        dateServisTarih.setValue(LocalDate.now());

        // 3. Servis Tablosunu ve Filtresini Ayarla
        configureServisTableColumns();
        tumServisGorunumList.setAll(servisKaydiDAO.getServisGorunumListesi()); // Ana servis listesini doldur
        configureServisKaydiFilterAndTable(); // Filtre ve tablo bağlama
    }

    // --- Olay Yöneticileri ---

    @FXML
    private void handleMusteriEkle() {
        String adSoyad = txtMusteriAdSoyad.getText();
        String telefon = txtMusteriTelefon.getText();
        String adres = txtMusteriAdres.getText();

        if (adSoyad == null || adSoyad.trim().isEmpty() || telefon == null || telefon.trim().isEmpty()) {
            showAlert(AlertType.WARNING, "Eksik Bilgi", "Müşteri Ad Soyad ve Telefon alanları zorunludur!");
            return;
        }

        Musteri yeniMusteri = new Musteri(0, adSoyad.trim(), telefon.trim(), (adres != null && !adres.trim().isEmpty() ? adres.trim() : null) );
        boolean eklendi = musteriDAO.addMusteri(yeniMusteri);

        if (eklendi) {
            showAlert(AlertType.INFORMATION, "Başarılı", "'" + adSoyad.trim() + "' müşterisi başarıyla eklendi.");
            refreshMusteriData(); // Müşteri verilerini yenile
            clearMusteriEkleForm();
        } else {
            showAlert(AlertType.ERROR, "Müşteri Ekleme Hatası", "Müşteri eklenirken bir hata oluştu.");
        }
    }

    @FXML
    private void handleMusteriDuzenle() {
        Musteri seciliMusteri = tblMusteriler.getSelectionModel().getSelectedItem();
        if (seciliMusteri == null) return;

        Dialog<Musteri> dialog = createMusteriDuzenleDialog(seciliMusteri);
        Optional<Musteri> result = dialog.showAndWait();

        result.ifPresent(guncellenmisMusteri -> {
            boolean basarili = musteriDAO.updateMusteri(guncellenmisMusteri);
            if (basarili) {
                showAlert(AlertType.INFORMATION, "Başarılı", "Müşteri bilgileri güncellendi.");
                refreshMusteriData(); // Müşteri verilerini yenile
                loadServisTable();    // Servis tablosunu da yenile (isim değişmiş olabilir)
            } else {
                showAlert(AlertType.ERROR, "Güncelleme Hatası", "Müşteri güncellenirken bir hata oluştu.");
            }
        });
    }

    @FXML
    private void handleMusteriSil() {
        Musteri seciliMusteri = tblMusteriler.getSelectionModel().getSelectedItem();
        if (seciliMusteri == null) return;

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Müşteri Silme Onayı");
        alert.setHeaderText("'" + seciliMusteri.getAdSoyad() + "' müşterisini silmek istediğinizden emin misiniz?");
        alert.setContentText("DİKKAT: Bu müşteriye ait TÜM servis kayıtları da kalıcı olarak silinecektir!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean silindi = musteriDAO.deleteMusteri(seciliMusteri.getId());
            if (silindi) {
                showAlert(AlertType.INFORMATION, "Başarılı", "Müşteri ve ilişkili servis kayıtları başarıyla silindi.");
                refreshMusteriData(); // Müşteri verilerini yenile
                loadServisTable();    // Servis tablosunu da yenile
            } else {
                showAlert(AlertType.ERROR, "Silme Hatası", "Müşteri silinirken bir veritabanı hatası oluştu.");
            }
        }
    }


    @FXML
    private void handleServisKaydet() {
        Musteri secilenMusteri = cmbServisMusteri.getValue();
        LocalDate servisTarihi = dateServisTarih.getValue();
        String islem = txtServisIslem.getText();
        Parca secilenParca = cmbServisParca.getValue();
        String adetStr = txtServisAdet.getText();
        String iscilikStr = txtServisIscilik.getText();
        String odemeStr = txtServisOdeme.getText();

        if (secilenMusteri == null) { showAlert(AlertType.ERROR, "Müşteri Seçilmedi", "Lütfen servis yapılacak müşteriyi seçin!"); return; }
        if (servisTarihi == null) { showAlert(AlertType.WARNING, "Eksik Bilgi", "Lütfen servis tarihini seçin!"); return; }
        if (islem == null || islem.trim().isEmpty()) { showAlert(AlertType.WARNING, "Eksik Bilgi", "Lütfen yapılan işlemi açıklayın!"); return; }

        Integer parcaId = null;
        Integer adet = null;
        if (secilenParca != null) {
             if (adetStr == null || adetStr.trim().isEmpty()) { showAlert(AlertType.WARNING, "Eksik Bilgi", "Parça seçildiğinde adet girilmesi zorunludur!"); return; }
             try {
                adet = Integer.parseInt(adetStr);
                if (adet <= 0) throw new NumberFormatException("Adet pozitif olmalı.");
                parcaId = secilenParca.getId();
             } catch (NumberFormatException e) { showAlert(AlertType.WARNING, "Geçersiz Adet", "Lütfen geçerli bir pozitif tam sayı adet girin!"); return; }
        } else {
            if (adetStr != null && !adetStr.trim().isEmpty()) { showAlert(AlertType.WARNING, "Tutarsız Giriş", "Parça seçilmediğinde adet girilemez!"); return; }
        }

        double iscilik, odeme;
         try {
             iscilik = (iscilikStr != null && !iscilikStr.trim().isEmpty()) ? parseDouble(iscilikStr) : 0.0;
             odeme = (odemeStr != null && !odemeStr.trim().isEmpty()) ? parseDouble(odemeStr) : 0.0;
             if (iscilik < 0 || odeme < 0) throw new IllegalArgumentException("Tutarlar negatif olamaz."); // IllegalArgumentException daha uygun
         } catch (IllegalArgumentException e) { // Sadece bunu yakala
              showAlert(AlertType.WARNING, "Geçersiz Tutar", "Lütfen işçilik ve ödeme için geçerli sayılar girin! Hata: " + e.getMessage()); return; }

        boolean kaydedildi = servisKaydiDAO.addServisKaydi(secilenMusteri.getId(), servisTarihi, islem.trim(), parcaId, adet, iscilik, odeme);

         if (kaydedildi) {
            showAlert(AlertType.INFORMATION, "Başarılı", "Servis kaydı başarıyla oluşturuldu.");
            clearServisEkleForm();
            loadServisTable(); // Sadece servis tablosunu yenile
             // TODO: Stok tablosunu da yenilemek gerekebilir (MainViewController aracılığıyla)
        } else {
            showAlert(AlertType.ERROR, "Servis Kaydı Hatası", "Servis kaydı oluşturulamadı! (Yetersiz stok veya başka bir veritabanı hatası)");
        }
    }


    // --- Yardımcı Metotlar ---

    private void configureMusteriTableColumns() {
        colMusteriId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMusteriAdSoyad.setCellValueFactory(new PropertyValueFactory<>("adSoyad"));
        colMusteriTelefon.setCellValueFactory(new PropertyValueFactory<>("telefon"));
        colMusteriAdres.setCellValueFactory(new PropertyValueFactory<>("adres"));
    }

    private void configureMusteriFilterAndCombo() {
        filtrelenmisMusterilerList = new FilteredList<>(tumMusterilerList, p -> true);
        SortedList<Musteri> siraliFiltrelenmisListe = new SortedList<>(filtrelenmisMusterilerList);
        cmbServisMusteri.setItems(siraliFiltrelenmisListe);

        txtMusteriAra.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrelenmisMusterilerList.setPredicate(musteri -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return musteri.getAdSoyad().toLowerCase().contains(lowerCaseFilter) ||
                       musteri.getTelefon().toLowerCase().contains(lowerCaseFilter);
            });
        });
    }

     private void addMusteriTableListeners() {
        tblMusteriler.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> updateMusteriButtonStates());
        tblMusteriler.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2 && tblMusteriler.getSelectionModel().getSelectedItem() != null) {
                handleMusteriDuzenle();
            }
        });
    }

     private void updateMusteriButtonStates() {
        boolean secimVar = tblMusteriler.getSelectionModel().getSelectedItem() != null;
        btnMusteriDuzenle.setDisable(!secimVar);
        btnMusteriSil.setDisable(!secimVar);
    }

    private void loadServisParcaCombo() {
        // Seçimi korumak için liste yerine doğrudan DAO'dan dolduralım şimdilik
        // veya parcaComboList eklenebilir StokViewController gibi
        cmbServisParca.getItems().setAll(parcaDAO.getAllParcalar());
    }

    private void configureServisTableColumns() {
        colServisId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colServisMusteri.setCellValueFactory(new PropertyValueFactory<>("musteriAdSoyad"));
        colServisTarih.setCellValueFactory(new PropertyValueFactory<>("tarih"));
        colServisIslem.setCellValueFactory(new PropertyValueFactory<>("islem"));
        colServisParca.setCellValueFactory(new PropertyValueFactory<>("parcaAdi"));
        colServisAdet.setCellValueFactory(new PropertyValueFactory<>("adet"));
        colServisIscilik.setCellValueFactory(new PropertyValueFactory<>("iscilik"));
        colServisOdeme.setCellValueFactory(new PropertyValueFactory<>("odemeTutari"));
        colServisKar.setCellValueFactory(new PropertyValueFactory<>("kar"));

        DateTimeFormatter tarihFormatlayici = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        colServisTarih.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : tarihFormatlayici.format(item));
            }
        });

        Callback<TableColumn<ServisGorunum, Double>, TableCell<ServisGorunum, Double>> fiyatCellFactory =
                column -> new TableCell<>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : currencyFormat.format(item)); // CurrencyFormat kullan
                        setStyle("-fx-alignment: CENTER-RIGHT;");
                    }
                };
        colServisIscilik.setCellFactory(fiyatCellFactory);
        colServisOdeme.setCellFactory(fiyatCellFactory);
        colServisKar.setCellFactory(fiyatCellFactory);
    }

    private void configureServisKaydiFilterAndTable() {
        filtrelenmisServisGorunumList = new FilteredList<>(tumServisGorunumList, p -> true);
        txtServisKaydiAra.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrelenmisServisGorunumList.setPredicate(createServisPredicate(newValue));
        });
        SortedList<ServisGorunum> siraliServisListesi = new SortedList<>(filtrelenmisServisGorunumList);
        siraliServisListesi.comparatorProperty().bind(tblServisKayitlari.comparatorProperty());
        tblServisKayitlari.setItems(siraliServisListesi);
        // System.out.println("Servis kaydı arama filtresi ve tablo ayarlandı.");
    }

    private Predicate<ServisGorunum> createServisPredicate(String searchText) {
        return servis -> {
            if (searchText == null || searchText.isEmpty()) return true;
            String lowerCaseFilter = searchText.toLowerCase();
            return (servis.getMusteriAdSoyad().toLowerCase().contains(lowerCaseFilter) ||
                    servis.getIslem().toLowerCase().contains(lowerCaseFilter) ||
                    (servis.getParcaAdi() != null && servis.getParcaAdi().toLowerCase().contains(lowerCaseFilter)));
        };
    }

    private void loadServisTable() {
        tumServisGorunumList.setAll(servisKaydiDAO.getServisGorunumListesi());
        // Filtre otomatik olarak güncellenir, tablo bağlıdır.
        // System.out.println("Servis Kayıtları Tablosu güncellendi.");
    }


    private void addNumericListener(TextField textField) {
         textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) return;
             boolean isAdetField = textField == txtServisAdet;
             String regex = isAdetField ? "\\d+" : "\\d*([.,]?\\d*)";
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

    private void clearMusteriEkleForm() {
        txtMusteriAdSoyad.clear();
        txtMusteriTelefon.clear();
        txtMusteriAdres.clear();
    }

    private void clearServisEkleForm() {
        txtMusteriAra.clear(); // Arama kutusunu da temizle
        cmbServisMusteri.getSelectionModel().clearSelection();
        dateServisTarih.setValue(LocalDate.now());
        txtServisIslem.clear();
        cmbServisParca.getSelectionModel().clearSelection();
        txtServisAdet.clear();
        txtServisIscilik.clear();
        txtServisOdeme.clear();
    }

    private Dialog<Musteri> createMusteriDuzenleDialog(Musteri musteri) {
        Dialog<Musteri> dialog = new Dialog<>();
        dialog.setTitle("Müşteri Düzenle");
        dialog.setHeaderText("Müşteri bilgilerini güncelleyin.");

        ButtonType okButtonType = new ButtonType("Güncelle", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField adSoyadField = new TextField(musteri.getAdSoyad());
        TextField telefonField = new TextField(musteri.getTelefon());
        TextArea adresArea = new TextArea(musteri.getAdres());
        adresArea.setPrefRowCount(3); adresArea.setWrapText(true);

        grid.add(new Label("Ad Soyad:"), 0, 0); grid.add(adSoyadField, 1, 0);
        grid.add(new Label("Telefon:"), 0, 1); grid.add(telefonField, 1, 1);
        grid.add(new Label("Adres:"), 0, 2);  grid.add(adresArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        // AdSoyad ve Telefon boş olamaz kontrolü
        Runnable updateOkButtonState = () -> {
            boolean disable = adSoyadField.getText().trim().isEmpty() ||
                             telefonField.getText().trim().isEmpty();
           okButton.setDisable(disable);
       };
       adSoyadField.textProperty().addListener((obs, ov, nv) -> updateOkButtonState.run());
       telefonField.textProperty().addListener((obs, ov, nv) -> updateOkButtonState.run());
       updateOkButtonState.run(); // Başlangıç durumu


        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                String yeniAdSoyad = adSoyadField.getText().trim();
                String yeniTelefon = telefonField.getText().trim();
                String yeniAdres = adresArea.getText();

                if (yeniAdSoyad.isEmpty() || yeniTelefon.isEmpty()) return null;

                musteri.setAdSoyad(yeniAdSoyad);
                musteri.setTelefon(yeniTelefon);
                musteri.setAdres(yeniAdres != null && !yeniAdres.trim().isEmpty() ? yeniAdres.trim() : null);
                return musteri;
            }
            return null;
        });
        return dialog;
    }

     /**
      * Virgül veya nokta içeren string'i double'a çevirir.
      * Boş değer için IllegalArgumentException fırlatır.
      */
     private double parseDouble(String value) throws IllegalArgumentException {
         if (value == null || value.trim().isEmpty()) {
             throw new IllegalArgumentException("Fiyat alanı boş bırakılamaz.");
         }
         try {
             return Double.parseDouble(value.trim().replace(',', '.'));
         } catch (NumberFormatException e) {
              throw new IllegalArgumentException("Geçersiz sayı formatı: '" + value + "'");
         }
     }

     /**
      * Double değerleri yerel para birimi olarak formatlamak için TableCell döndürür.
      */
     private TableCell<ServisGorunum, Double> formatCurrencyCell() {
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
     /**
      * Bu sekme görünür olduğunda veya verilerde dışarıdan bir değişiklik olduğunda
      * çağrılacak public metot. Müşteri ve servis verilerini, ilgili ComboBox'ları
      * ve tabloları yeniden yükler.
      */
     public void refreshData() {
         System.out.println("MusteriServisView verileri yenileniyor...");
         refreshMusteriData(); // Müşteri tablosunu ve ComboBox/filtreyi yenile
         loadServisParcaCombo(); // Servis parçaları ComboBox'ını yenile
         loadServisTable(); // Servis kayıtları tablosunu yenile
         // Buton durumları zaten listener'lar ile güncelleniyor ama ilk yükleme için çağrılabilir
         updateMusteriButtonStates();
         System.out.println("MusteriServisView verileri yenilendi.");
     }

     /**
      * Müşteri ile ilgili tüm verileri (tablo, filtre, combobox) yeniler.
      */
     private void refreshMusteriData() {
          // Mevcut ComboBox seçimini ve filtreyi koru
          Musteri seciliComboMusteri = cmbServisMusteri.getValue();
          String mevcutFiltre = txtMusteriAra.getText();
          Musteri seciliTabloMusteri = tblMusteriler.getSelectionModel().getSelectedItem();

          // Ana listeyi güncelle
          tumMusterilerList.setAll(musteriDAO.getAllMusteriler());

          // Filtreyi tekrar uygula (eğer varsa) - Listener bunu otomatik yapmalı
          // Ancak liste değiştiği için manuel tetiklemek gerekebilir.
          if (filtrelenmisMusterilerList != null) {
             filtrelenmisMusterilerList.setPredicate(createMusteriPredicate(mevcutFiltre));
          }

          // ComboBox seçimini geri yükle (ID bazlı)
          cmbServisMusteri.setValue(tumMusterilerList.stream()
              .filter(m -> m.getId() == (seciliComboMusteri != null ? seciliComboMusteri.getId() : -1))
              .findFirst().orElse(null));

         // Müşteri Tablosu seçimini geri yükle (ID bazlı)
         if (seciliTabloMusteri != null) {
              Optional<Musteri> found = tumMusterilerList.stream()
                      .filter(m -> m.getId() == seciliTabloMusteri.getId())
                      .findFirst();
              tblMusteriler.getSelectionModel().select(found.orElse(null));
         }
         if (tblMusteriler.getSelectionModel().getSelectedItem() == null) {
             tblMusteriler.getSelectionModel().clearSelection();
         }
         // System.out.println("Müşteri verileri yenilendi.");
     }

      /**
     * Müşteri filtreleme için Predicate oluşturur (configureMusteriFilterAndCombo'dan çıkarıldı).
     */
    private Predicate<Musteri> createMusteriPredicate(String searchText) {
        return musteri -> {
            if (searchText == null || searchText.isEmpty()) return true;
            String lowerCaseFilter = searchText.toLowerCase();
            return musteri.getAdSoyad().toLowerCase().contains(lowerCaseFilter) ||
                   musteri.getTelefon().toLowerCase().contains(lowerCaseFilter);
        };
    }

}