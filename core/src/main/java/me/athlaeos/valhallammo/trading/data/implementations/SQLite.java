package me.athlaeos.valhallammo.trading.data.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.persistence.Database;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.trading.data.MerchantDataPersistence;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.utility.Callback;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class SQLite extends MerchantDataPersistence implements Database {
    private Connection conn;

    @Override
    public Connection getConnection() {
        File dataFolder = new File(ValhallaMMO.getInstance().getDataFolder(), "trading/merchant_data.db");
        if (!dataFolder.exists()){
            try {
                if (dataFolder.createNewFile()) ValhallaMMO.logInfo("New trading/merchant_data.db file created!");
            } catch (IOException e) {
                ValhallaMMO.logSevere("Could not create SQLite database file merchant_data.db");
            }
        }

        try {
            if(conn != null && !conn.isClosed()) return conn;
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            ValhallaMMO.logFine("SQLite connection created! Deleting this file will reset all custom merchant trades and reputation data.");
            return conn;
        } catch (SQLException ex) {
            ValhallaMMO.logSevere("SQLite exception on initialize " + ex);
        } catch (ClassNotFoundException ex) {
            ValhallaMMO.logSevere("You do not have the SQLite JDBC library on your server, custom trading mechanics are unfortunately disabled");
        }
        return null;
    }

    @Override
    public void createTable(Profile profileType) {
        String query = "CREATE TABLE IF NOT EXISTS merchants (id VARCHAR(40) PRIMARY KEY, data TEXT);";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)){
            stmt.execute();
        } catch (SQLException ex){
            ex.printStackTrace();
        }
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
    public void setData(UUID id, MerchantData data) {
        allData.put(id, data);
    }

    @Override
    public void getData(UUID id, Callback<MerchantData> callback) {
        if (allData.containsKey(id)) {
            callback.whenReady(allData.get(id));
            return;
        }
        new BukkitRunnable(){
            @Override
            public void run() {
                try {
                    PreparedStatement stmt = conn.prepareStatement("SELECT * FROM merchants WHERE id = ?;");
                    stmt.setString(1, id.toString());
                    ResultSet result = stmt.executeQuery();
                    if (result.next()) callback.whenReady(MerchantData.deserialize(result.getString("data")));
                    else callback.whenReady(null);
                } catch (SQLException ex){
                    ValhallaMMO.logSevere("SQLException when trying to fetch MerchantData with id " + id);
                    ex.printStackTrace();
                    callback.whenReady(null);
                }
            }
        }.runTaskAsynchronously(ValhallaMMO.getInstance());
    }

    @Override
    public void saveAllData() {
        for (UUID id : allData.keySet()){
            MerchantData data = allData.get(id);
            saveData(id, data);
        }
    }

    @Override
    public void saveData(UUID id, MerchantData data) {
        String query = "REPLACE INTO merchants (id, data) VALUES (?, ?);";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id.toString());
            stmt.setString(2, data.serialize());
            stmt.execute();
        } catch (SQLException exception){
            ValhallaMMO.getInstance().getServer().getLogger().severe("SQLException when trying to save MerchantData with id " + id);
            exception.printStackTrace();
        }
    }
}
