module com.academico {
    // === MÓDULOS DEL JDK Y JAVAFX ===
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;

    // === LIBRERÍAS DE TERCEROS ===
    requires atlantafx.base;            // Tema visual
    requires com.zaxxer.hikari;         // Pool de conexiones
    requires org.postgresql.jdbc;       // Driver DB
    requires io.github.cdimascio.dotenv.java; // Variables de entorno
    requires bcrypt;                    // Seguridad
    requires com.opencsv;               // Carga masiva

    // === PERMISOS DE REFLEXIÓN (Necesarios para FXML y TableViews) ===
    opens com.academico.controller to javafx.fxml;
    opens com.academico            to javafx.fxml;
    opens com.academico.model      to javafx.base;

    // === EXPORTACIÓN DE PAQUETES ===
    exports com.academico;
}