package me.athlaeos.valhallammo.persistence;

import java.sql.Connection;

public interface Database {
    Connection getConnection();

    void createTable();

    void addColumnIfNotExists(String tableName, String columnName, String columnType);
}
