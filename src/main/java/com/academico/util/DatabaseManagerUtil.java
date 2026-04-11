package com.academico.util;

import com.academico.service.AuthService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;

/**
 * Utilería para la gestión del Pool de Conexiones a la Base de Datos.
 * Responsabilidad: Inicializar HikariCP, crear el esquema inicial si no existe,
 * y proveer conexiones seguras al resto del sistema.
 */
public class DatabaseManagerUtil {

    private static HikariDataSource dataSource;

    // ==========================================
    // INICIALIZACIÓN Y CONFIGURACIÓN
    // ==========================================

    public static void initialize() throws Exception {
        try {
            Dotenv dotenv = Dotenv.load();
            HikariConfig config = new HikariConfig();
            
            config.setJdbcUrl("jdbc:postgresql://" + dotenv.get("DB_HOST") + ":" + 
                              dotenv.get("DB_PORT") + "/" + dotenv.get("DB_NAME"));
            config.setUsername(dotenv.get("DB_USER"));
            config.setPassword(dotenv.get("DB_PASSWORD"));
            config.setMaximumPoolSize(10);
            
            dataSource = new HikariDataSource(config);
            createSchemaIfNeeded();
            
        } catch (Exception e) {
            throw new Exception("Error crítico al conectar con la base de datos. Verifica tu archivo .env o el servicio de PostgreSQL.");
        }
    }

    private static void createSchemaIfNeeded() throws Exception {
        try (Connection conn = getConnection()) {
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "configuracion", null);
            
            if (!tables.next()) {
                ejecutarScript(conn);
            }
        } catch (SQLException e) {
            throw new Exception("Error al verificar o crear el esquema de la base de datos: " + e.getMessage());
        }
    }

    private static void ejecutarScript(Connection conn) throws Exception {
        try (InputStream is = DatabaseManagerUtil.class.getResourceAsStream("/com/academico/db/schema.sql")) {
            if (is == null) {
                throw new Exception("No se encontró el archivo schema.sql en los recursos del sistema.");
            }
            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            
            conn.setAutoCommit(false);
            try {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                }
                crearAdministradorPorDefecto(conn);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static void crearAdministradorPorDefecto(Connection conn) throws Exception {
        String correoAdmin = "admin@escuela.edu";
        
        String checkSql = "SELECT count(*) FROM usuario WHERE email = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, correoAdmin);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return;
            }
        }

        AuthService authService = new AuthService();
        String passwordHash = authService.hashearPassword("123456");
        String insertSql = "INSERT INTO usuario (nombre, email, password_hash, rol, activo) VALUES (?, ?, ?, 'admin', true)";
        
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, "Administrador del Sistema");
            insertStmt.setString(2, correoAdmin);
            insertStmt.setString(3, passwordHash);
            insertStmt.executeUpdate();
        }
    }

    // ==========================================
    // OPERACIONES DE CONEXIÓN
    // ==========================================

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("El pool de conexiones no está inicializado.");
        }
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