package me.athlaeos.valhallammo.version.conversion_dto;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.utility.ValhallaRunnable;
import org.bukkit.configuration.file.YamlConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection databaseConnection = null;
    private Connection conn;
    private String database = "";

    public DatabaseConnection(){
        this.conn = initializeConnection();
    }

    public static DatabaseConnection getDatabaseConnection(){
        if (databaseConnection == null) databaseConnection = new DatabaseConnection();
        return databaseConnection;
    }

    public Connection getConnection() {
        return conn;
    }

    public String getDatabase() {
        return database;
    }

    private Connection initializeConnection() {
        YamlConfiguration config = ConfigManager.getConfig("config.yml").get();
        String host = config.getString("db_host");
        database = config.getString("db_database");
        String username = config.getString("db_username");
        String password = config.getString("db_password");
        int port = config.getInt("db_port");
        int ping_delay = config.getInt("db_ping_delay");
        try {
            if (conn != null && !conn.isClosed()) {
                return conn;
            }

            synchronized (ValhallaMMO.getInstance()) {
                if (conn != null && !conn.isClosed()) {
                    return conn;
                }
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);

                ValhallaMMO.logInfo("Database connection created!");
            }
        } catch (Exception e) {
            ValhallaMMO.logWarning("Database connection failed, using PersistentDataContainer for profile persistence");
            return null;
        }
        if (conn != null){
            new ValhallaRunnable(){
                @Override
                public void run() {
                    try {
                        conn.prepareStatement("/* ping */ SELECT 1;").execute();
                    } catch (SQLException ex){
                        ValhallaMMO.logWarning("Database ping failed");
                        cancel();
                    }
                }
            }.runTaskTimerAsync(ValhallaMMO.getInstance(), ping_delay, ping_delay);
        }
        return conn;
    }

    public void addColumnIfNotExists(String tableName, String columnName, String columnType){
        try {
            PreparedStatement procedureCreationStatement = conn.prepareStatement(
                    "SELECT " + columnName + " FROM " + tableName + ";");
            procedureCreationStatement.execute();
        } catch (SQLException e){
            try {
                PreparedStatement procedureCreationStatement = conn.prepareStatement(
                        "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType + ";");
                procedureCreationStatement.execute();
            } catch (SQLException ex){
                ValhallaMMO.logSevere("SQLException when trying to add column " + columnName + " " + columnType + " to " + tableName + ". ");
                e.printStackTrace();
            }
        }
    }
}
