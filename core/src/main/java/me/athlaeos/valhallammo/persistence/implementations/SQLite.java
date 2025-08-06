package me.athlaeos.valhallammo.persistence.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.persistence.ProfilePersistence;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class SQLite extends ProfilePersistence {
    private Connection conn;

    @Override
    public int minimumProfileThreadCount() {
        // 1 thread for loading, 1 thread for saving
        return 2;
    }

    @Override
    public Connection getConnection(boolean migrating) {
        File dataFolder = new File(ValhallaMMO.getInstance().getDataFolder(), "player_data.db");
        if (!dataFolder.exists()){
            if (migrating) {
                ValhallaMMO.logWarning("SQLite database file player_data.db does not exist, cannot migrate from it.");
                return null;
            }

            try {
                if (dataFolder.createNewFile()) ValhallaMMO.logInfo("New player_data.db file created!");
            } catch (IOException e) {
                ValhallaMMO.logSevere("Could not create SQLite database file player_data.db");
            }
        }

        try {
            if (conn != null && !conn.isClosed()){
                return conn;
            }
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            ValhallaMMO.logFine(migrating
                    ? "Old SQLite connection created for migration!"
                    : "SQLite connection created! Deleting this file will reset everyone's progress, so back this file up or ignore it in case you want to delete/reset the configs.");
            return conn;
        } catch (SQLException ex) {
            ValhallaMMO.logSevere("SQLite exception on initialize " + ex);
        } catch (ClassNotFoundException ex) {
            ValhallaMMO.logInfo("You do not have the SQLite JDBC library on your server");
        }
        return null;
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
        return "sqlite";
    }
}
