package me.athlaeos.valhallammo.persistence.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.persistence.ProfilePersistence;
import org.bukkit.configuration.file.YamlConfiguration;

import java.sql.*;
import java.util.concurrent.TimeUnit;

public class SQL extends ProfilePersistence {
    private Connection conn;

    @Override
    public int minimumProfileThreadCount() {
        // 1 thread for pinging, 1 thread for saving, 1 thread for loading
        return 3;
    }

    @Override
    public Connection getConnection(boolean migrating) {
        try {
            if (conn != null && !conn.isClosed()) {
                return conn;
            }

            YamlConfiguration config = ConfigManager.getConfig("config.yml").reload().get();
            String host = config.getString("db_host");
            String database = config.getString("db_database");
            String username = config.getString("db_username");
            String password = config.getString("db_password");
            int port = config.getInt("db_port");
            int ping_delay = config.getInt("db_ping_delay");

            synchronized (ValhallaMMO.getInstance()) {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
                if (migrating) return conn;

                ValhallaMMO.logFine("MySQL Database connection created!");
                profileThreads.scheduleAtFixedRate(() -> {
                    try {
                        conn.prepareStatement("/* ping */ SELECT 1;").execute();
                    } catch (SQLException ex) {
                        throw new RuntimeException("Database ping failed", ex);
                    }
                }, ping_delay, ping_delay, TimeUnit.MILLISECONDS);
            }
            return conn;
        } catch (Exception e) {
            ValhallaMMO.logInfo("Database connection failed " + e);
            return null;
        }
    }

    @Override
    public void createTable(Profile profileType) {
        createTable(profileType, this);
    }

    @Override
    public void addColumnIfNotExists(String tableName, String columnName, String columnType) {
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

    @Override
    public String getType() {
        return "mysql";
    }
}
