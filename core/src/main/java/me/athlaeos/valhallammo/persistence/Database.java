package me.athlaeos.valhallammo.persistence;

import me.athlaeos.valhallammo.playerstats.profiles.Profile;

import java.sql.Connection;

public interface Database {
    Connection getConnection();

    void createTable(Profile profileType);

    void addColumnIfNotExists(String tableName, String columnName, String columnType);
}
