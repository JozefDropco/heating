package org.dropco.smarthome.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DBConnection {
    private static Connection connection;
    private static LocalDateTime creation = LocalDateTime.now();

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || connection.isValid(5) || ChronoUnit.MINUTES.between(creation, LocalDateTime.now()) > 5) {
                connection = DriverManager.getConnection("jdbc:mysql://localhost/heating?"
                        + "user=heating&password=h1e2a3t4i5n6g7&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Vienna&characterEncoding=UTF-8");
                creation = LocalDateTime.now();
            }
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
