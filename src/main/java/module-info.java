module org.example.library {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires org.hibernate.orm.core;
    requires static lombok;
    requires jakarta.persistence;
    requires java.naming;
    requires jakarta.validation;
    requires jbcrypt;
    requires org.slf4j;

    opens org.example.library to javafx.fxml;
    exports org.example.library;
    exports org.example.library.controllers;
    opens org.example.library.controllers to javafx.fxml;
    opens org.example.library.entities;
}