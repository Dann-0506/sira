package com.academico.util;

import com.academico.service.AuthService;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class DatabaseManagerUtil {
    private static HikariDataSource dataSource;

    public static void initialize() {
        Dotenv dotenv = Dotenv.load();
        HikariConfig config = new HikariConfig();
        
        config.setJdbcUrl("jdbc:postgresql://" + dotenv.get("DB_HOST") + ":" + 
                          dotenv.get("DB_PORT") + "/" + dotenv.get("DB_NAME"));
        config.setUsername(dotenv.get("DB_USER"));
        config.setPassword(dotenv.get("DB_PASSWORD"));
        
        config.setMaximumPoolSize(10);
        dataSource = new HikariDataSource(config);
        
        createSchemaIfNeeded();
    }

    private static void createSchemaIfNeeded() {
        try (Connection conn = getConnection()) {
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "configuracion", null);
            
            if (!tables.next()) {
                System.out.println("Base de datos nueva detectada. Instalando esquema...");
                ejecutarScript(conn);
            } else {
                System.out.println("Base de datos lista.");
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar esquema: " + e.getMessage());
        }
    }

    private static void ejecutarScript(Connection conn) {
        try (InputStream is = DatabaseManagerUtil.class.getResourceAsStream("/com/academico/db/schema.sql")) {
            if (is == null) {
                System.err.println("No se encontró el archivo schema.sql");
                return;
            }
            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            
            conn.setAutoCommit(false);
            try {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                }

                crearAdministradorPorDefecto(conn);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            System.err.println("Error al incializar la base de datos: " + e.getMessage());
        }
    }

    private static void crearAdministradorPorDefecto(Connection conn) throws SQLException {
        String nombreAdmin = "Administrador del Sistema";
        String correoAdmin = "admin@escuela.edu";
        String passwordPlano = "admin123";

        String checkSql = "SELECT count(*) FROM usuario WHERE email = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, correoAdmin);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }
        }

        AuthService authService = new AuthService();
        String passwordHash = authService.hashearPassword(passwordPlano);

        String insertSql = "INSERT INTO usuario (nombre, email, password_hash, rol, activo) VALUES (?, ?, ?, 'admin', true)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, nombreAdmin);
            insertStmt.setString(2, correoAdmin);
            insertStmt.setString(3, passwordHash);
            insertStmt.executeUpdate();
            
            System.out.println("----------------------------------------");
            System.out.println("USUARIO ADMIN CREADO EXITOSAMENTE:");
            System.out.println("Correo: " + correoAdmin);
            System.out.println("Contraseña: " + passwordPlano);
            System.out.println("----------------------------------------");
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public static boolean isInitialized() {
        return dataSource != null && !dataSource.isClosed();
    }
}