module com.academico.core {
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires org.postgresql.jdbc;
    requires io.github.cdimascio.dotenv.java;
    requires bcrypt;
    requires com.opencsv;

    opens com.academico.controller to javafx.fxml;

    opens com.academico to javafx.fxml;

    opens com.academico.model to javafx.base;

    exports com.academico;
}