module com.pusulaiklimlendirme {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc; // <-- YENİ EKLENEN SATIR

    // Bu satırların paket adınızla eşleştiğinden emin olun
    opens com.pusulaiklimlendirme to javafx.fxml;
    exports com.pusulaiklimlendirme;

    // Eğer ileride GUI Controller sınıflarını başka bir alt pakete taşırsanız
    // (örn: com.pusulaiklimlendirme.controller)
    // o paketi de açmanız gerekebilir:
    // opens com.pusulaiklimlendirme.controller to javafx.fxml;

    // Eğer DAO sınıflarını başka bir alt pakete taşırsanız (örn: com.pusulaiklimlendirme.dao)
    // ve bu DAO'ları GUI katmanında doğrudan kullanırsanız, o paketi de açmanız gerekebilir.
    // Ancak, genellikle DAO'ları sadece servis katmanı kullanır ve GUI servis katmanını kullanır.
    // Bu durumda DAO paketini açmaya GEREK YOKTUR.
    // opens com.pusulaiklimlendirme.dao to javafx.fxml; // Eğer doğrudan GUI'den erişim varsa ve FXML kullanıyorsa GEREKLİ OLABİLİR
    // exports com.pusulaiklimlendirme.dao; // Eğer başka bir modül DAO'ları kullanacaksa GEREKLİ OLABİLİR

}