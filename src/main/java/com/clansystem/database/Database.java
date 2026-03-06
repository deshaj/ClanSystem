package com.clansystem.database;

import com.clansystem.ClanSystem;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Database {
    private final ClanSystem plugin;
    private final ExecutorService executor;
    private HikariDataSource dataSource;
    private DatabaseType type;
    
    public Database(ClanSystem plugin) {
        this.plugin = plugin;
        this.executor = Executors.newFixedThreadPool(4, r -> {
            Thread thread = new Thread(r, "ClanSystem-DB");
            thread.setDaemon(true);
            return thread;
        });
    }
    
    public void connect() {
        String typeStr = plugin.getConfigManager().getConfig().getString("database.type", "SQLITE");
        this.type = DatabaseType.valueOf(typeStr.toUpperCase());
        
        HikariConfig config = new HikariConfig();
        
        if (type == DatabaseType.MYSQL) {
            String host = plugin.getConfigManager().getConfig().getString("database.host");
            int port = plugin.getConfigManager().getConfig().getInt("database.port");
            String database = plugin.getConfigManager().getConfig().getString("database.database");
            String username = plugin.getConfigManager().getConfig().getString("database.username");
            String password = plugin.getConfigManager().getConfig().getString("database.password");
            
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true");
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            
            plugin.log("Connecting to MySQL database...");
        } else {
            File dbFile = new File(plugin.getDataFolder(), "data.db");
            config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            config.setDriverClassName("org.sqlite.JDBC");
            
            plugin.log("Connecting to SQLite database...");
        }
        
        config.setMaximumPoolSize(plugin.getConfigManager().getConfig().getInt("database.pool.maximum-size", 10));
        config.setMinimumIdle(plugin.getConfigManager().getConfig().getInt("database.pool.minimum-idle", 2));
        config.setConnectionTimeout(plugin.getConfigManager().getConfig().getLong("database.pool.connection-timeout", 5000));
        config.setPoolName("ClanSystem-Pool");
        
        this.dataSource = new HikariDataSource(config);
        
        initTables();
        
        plugin.log("Database connected successfully!");
    }
    
    private void initTables() {
        try (Connection conn = getConnection()) {
            String[] statements = {
                "CREATE TABLE IF NOT EXISTS clans (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "name VARCHAR(32) NOT NULL UNIQUE, " +
                "owner VARCHAR(36) NOT NULL, " +
                "total_kills INT DEFAULT 0, " +
                "total_deaths INT DEFAULT 0, " +
                "total_playtime BIGINT DEFAULT 0, " +
                "total_money DOUBLE DEFAULT 0, " +
                "created_at BIGINT NOT NULL, " +
                "home_world VARCHAR(64), " +
                "home_x DOUBLE, " +
                "home_y DOUBLE, " +
                "home_z DOUBLE, " +
                "home_yaw FLOAT, " +
                "home_pitch FLOAT" +
                ")",
                "CREATE TABLE IF NOT EXISTS clan_members (" +
                "clan_id VARCHAR(36) NOT NULL, " +
                "player_uuid VARCHAR(36) NOT NULL, " +
                "player_name VARCHAR(16) NOT NULL, " +
                "rank VARCHAR(16) NOT NULL, " +
                "kills INT DEFAULT 0, " +
                "deaths INT DEFAULT 0, " +
                "playtime BIGINT DEFAULT 0, " +
                "joined_at BIGINT NOT NULL, " +
                "PRIMARY KEY (clan_id, player_uuid)" +
                ")",
                "CREATE TABLE IF NOT EXISTS player_data (" +
                "player_uuid VARCHAR(36) PRIMARY KEY, " +
                "clan_id VARCHAR(36)" +
                ")",
                "CREATE TABLE IF NOT EXISTS clan_invitations (" +
                "player_uuid VARCHAR(36) NOT NULL, " +
                "clan_id VARCHAR(36) NOT NULL, " +
                "PRIMARY KEY (player_uuid, clan_id)" +
                ")"
            };
            for (String sql : statements) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.execute();
                }
            }
            plugin.log("Database tables initialized");
        } catch (SQLException e) {
            plugin.error("Database initialization error", e);
        }
    }
    
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    @FunctionalInterface
    public interface SqlFunction<T> {
        T apply(Connection conn) throws SQLException;
    }

    @FunctionalInterface
    public interface SqlConsumer {
        void accept(Connection conn) throws SQLException;
    }

    public <T> CompletableFuture<T> executeAsync(SqlFunction<T> action) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection()) {
                return action.apply(conn);
            } catch (SQLException e) {
                plugin.error("Database error", e);
                throw new RuntimeException(e);
            }
        }, executor);
    }
    
    public CompletableFuture<Void> executeAsync(SqlConsumer action) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                action.accept(conn);
            } catch (SQLException e) {
                plugin.error("Database error", e);
            }
        }, executor);
    }
    
    private void executeSync(SqlConsumer action) {
        try (Connection conn = getConnection()) {
            action.accept(conn);
        } catch (SQLException e) {
            plugin.error("Database initialization error", e);
        }
    }
    
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.log("Database connection closed");
        }
        executor.shutdown();
    }
}