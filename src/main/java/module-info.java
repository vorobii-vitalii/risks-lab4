module com.example.riskmgmtlab4 {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.desktop;

    opens com.example.riskmgmtlab4 to javafx.fxml;
    exports com.example.riskmgmtlab4;
}