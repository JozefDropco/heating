package org.dropco.smarthome.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static Connection connection;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || connection.isValid(5))
                connection = DriverManager.getConnection("jdbc:mysql://localhost/heating?"
                        + "user=heating&password=h1e2a3t4i5n6g7");
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
