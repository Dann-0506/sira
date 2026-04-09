package com.academico.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManagerUtil {

    private static HikariDataSource dataSource;

    public static void initialize() {

        Dotenv dotenv = Dotenv.load();

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:postgresql://" + dotenv.get("DB_HOST") + ":" + dotenv.get("DB_PORT") + "/" + dotenv.get("DB_NAME"));
        config.setUsername(dotenv.get("DB_USER"));
        config.setPassword(dotenv.get("DB_PASSWORD"));
        
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.addDataSourceProperty("reWriteBatchedInserts", "true");

        dataSource = new HikariDataSource(config);
        createSchemaIfNeeded();
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new IllegalStateException(
                "DatabaseManager no inicializado. Llama a initialize() primero.");
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

    private static void createSchemaIfNeeded() {
        try (Connection conn = getConnection();
            InputStream is = DatabaseManagerUtil.class
                    .getResourceAsStream("/com/academico/db/schema.sql")) {

            if (is == null) {
                throw new RuntimeException("schema.sql no encontrado en el classpath");
            }

            String fullSql = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            // Ejecuta todo el schema como una sola operación
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(fullSql);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            throw new RuntimeException(
                "Error inicializando esquema de base de datos: " + e.getMessage(), e);
        }
    }
}