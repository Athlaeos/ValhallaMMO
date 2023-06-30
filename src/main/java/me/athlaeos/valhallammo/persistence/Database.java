package me.athlaeos.valhallammo.persistence;

import java.sql.Connection;

public interface Database {
    Connection getConnection();

    void addColumnIfNotExists(String tableName, String columnName, String columnType);
}
