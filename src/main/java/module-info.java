module com.example.bezierdrawingapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.bezierdrawingapp to javafx.fxml;
    exports com.example.bezierdrawingapp;
}